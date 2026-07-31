# Shared UI: one Compose codebase for Android and iOS

Goal: the iOS app is the Android app. UI and logic live in `:shared`; `:app` and
`iosApp` are thin hosts holding only what the platform SDKs force them to hold.

## Decided

- **Persistence stays `NulaDatabase`.** Room KMP needs KSP, which does not run in
  this KMP module on AGP 9, and the hand-written store already reads the legacy
  `NulaDB` file - the thing that keeps existing Android users' favourites.
- **`:app` becomes a thin host**: `MainActivity` renders one shared composable.
  It keeps `RadioPlaybackService`, media3, Firebase and the Android actuals,
  because those are Android-only SDKs with no multiplatform equivalent.
- **Comments use a KMP WebView library** (`compose-webview-multiplatform`), so
  the screen itself is shared - verified to compile for iOS.
- **The player stays `MediaPlayerController` + per-platform implementations.**
  media3's `MediaSessionService` (notification controls, foreground service,
  audio focus) has no cross-platform equivalent; the KMP media libraries target
  in-app video. iOS gets an AVPlayer actual in `iosMain`. `TuningNoise` is a
  playback detail and lives with each implementation.
- **Crash reporting is not shared.** Crashlytics stays on `:app`; iOS would use
  the Firebase iOS SDK via SPM if it is ever wanted.

## The version ceiling, again

Every dependency below is the newest whose Kotlin/Native klibs the 2.2.10
compiler AGP 9.3.1 embeds can read (ABI <= 2.2.0). Each was found by building.

| Dependency | Newest | Pinned |
|---|---|---|
| Compose Multiplatform | 1.11.1 | 1.10.3 |
| navigation-compose (JetBrains) | 2.10.0-alpha02 | 2.9.2 |
| lifecycle-viewmodel-compose (JetBrains) | 2.11.0 | 2.9.4 |
| coil3 | 3.5.0 | 3.3.0 |
| koin-compose-viewmodel | 4.2.2 | 4.1.1 |
| compose-webview-multiplatform | 2.0.3 | 2.0.3 |

Raising any of them means moving off AGP 9's embedded Kotlin first.

## Phases

1. **Resources and theme.** The 18 live drawables, both Roboto fonts and the
   theme move to `commonMain` + `composeResources`. Dead drawables stay behind to
   be deleted, not moved.
2. **Components and screens.** `Common.kt`, `ChannelArt` (`DrawableResource`
   instead of `Int`), `PlayerScreen`, `FavoritesScreen`, `CommentsScreen`,
   `NulaApp` and the nav graph move to `commonMain`. ViewModel Koin bindings move
   with them via `koin-core-viewmodel`.
3. **The remaining platform seams.** `ConnectivityLiveData` becomes a
   `ConnectivityMonitor` interface in `commonMain` - resolved through Koin like
   `MediaPlayerController`, not expect/actual, because the Android side needs a
   `Context`. Then the iOS `MediaPlayerController` on AVPlayer.
4. **Hosts.** `MainActivity` renders `NulaApp()`; `ContentView` hosts
   `ComposeUIViewController`. `SmokeTest.kt` and its call site go.
5. **Verify.** Both platforms' tests, the Android instrumented suite, a minified
   release build, and the iOS app on a simulator.

---

## Outcome

Done and running on both platforms from one codebase: `commonMain` holds the
theme, `Common.kt`, `PlayerScreen`, `FavoritesScreen`, `CommentsScreen`,
`NulaApp` with its nav graph, and both ViewModel bindings. `MainActivity` is
`NulaTheme { NulaApp() }`; `ContentView` wraps `NulaViewController()`.

Verified: 43 unit tests (26 `:shared` on both the JVM and an iOS simulator, 17
`:app`), 11 instrumented tests on a Pixel 10 Pro emulator, a minified
`assembleRelease`, and both apps launched and screenshotted - Android tuned in
and showed cover art, transport controls and a live "NOW PLAYING" row.

### Two traps worth remembering

- **`com.android.kotlin.multiplatform.library` does not package Compose
  resources.** The plugin creates `copyAndroidMainComposeResourcesToAndroidAssets`
  but never gives it an output, so every drawable and font was generated for the
  iOS frameworks and silently absent from both APKs - `painterResource` threw
  `MissingResourceException`, which the instrumented tests caught. AGP 9 rejects
  the classic `com.android.library` plugin alongside KMP ("not compatible ...
  since AGP 9.0"), so `:shared` copies the prepared resources itself into
  `composeResources/com.radionula.radionula.resources/` and `:app` adds that
  directory through the variant API (`addStaticSourceDirectory` - AGP 9 refuses
  a `Provider` in the source-set API). Revisit when the plugin wires this up.
- **Compose Multiplatform will not launch without
  `CADisableMinimumFrameDurationOnPhone`.** Its own `PlistSanityCheck` throws on
  start, which is a SIGABRT with no message worth reading. `iosApp/Info.plist`
  exists purely for that key and the audio background mode.

### What is per-platform, and why

| Piece | Why it is not shared |
|---|---|
| `CommentsWebView` | Android needs `setAcceptThirdPartyCookies` on the WebView instance for Remark42's iframe login; no wrapper exposes it. iOS uses `compose-webview-multiplatform`. |
| `topBarInsets()` | Android hides the status bar but reserves its height; iOS has a safe area. |
| `MediaPlayerController` | media3's `MediaSessionService` vs AVPlayer + `AVAudioSession` + `MPRemoteCommandCenter`. |
| `ConnectivityMonitor`, `WebSearch` | Koin-resolved interfaces: the Android sides need a `Context`. |
| Launcher icon, splash | `AndroidManifest` reads AAPT resources, not Compose ones, so `nula_intro_logo` and `background` stay duplicated in `app/src/main/res`. |

### Not yet verified

- **iOS playback.** The UI renders and the feed loads, but nothing has tapped
  TUNE IN on the simulator - `simctl` cannot tap, and no UI-automation harness is
  set up. AVPlayer, the audio session, the lock-screen controls and the
  interruption handling are all unexercised.
- **`IosMediaPlayerController` shortcuts**, marked `ponytail:` in the file:
  `timeControlStatus` is polled every 250ms instead of observed through KVO, so a
  burst of static can start up to a quarter second late. No lock-screen artwork.

### The tuning noise, on both platforms

`radionoise.mp3` moved from `app/src/main/res/raw` to
`commonMain/composeResources/files`, so there is one copy of it. iOS reads it
with `Res.readBytes` into an `AVAudioPlayer` and plays it while
`timeControlStatus` is `WaitingToPlayAtSpecifiedRate` - AVPlayer's buffering,
which is the same condition as the Android service's
`STATE_BUFFERING && playWhenReady`. Polling `timeControlStatus` also means
`isPlaying` now comes from the player rather than from whoever called it, so an
interruption or a dead stream reaches the screen.

Android now reaches the file as an asset file descriptor rather than a raw
resource. That depends on AAPT leaving `.mp3` uncompressed and on the asset path
matching `packageOfResClass`, neither of which fails at build time -
`TuningNoiseTest` is the instrumented test that would.
