# RadioNula → Kotlin Multiplatform

Date: 2026-07-31
Status: Approved

## Goal

Restructure RadioNula as a Kotlin Multiplatform project targeting **Android only**, with every
platform-agnostic layer living in `commonMain`. No iOS/desktop target is added now. Adding
`iosArm64` later must require writing UI and a player implementation — nothing else.

Non-goals: Compose Multiplatform, iOS app, desktop/web targets, feature changes, visual changes.

## Constraints

AGP 9.3.1 embeds Kotlin Gradle Plugin **2.2.10** (verified in the AGP POM), and that pins the whole
Kotlin toolchain. Bumping AGP does not help: 9.4.0-alpha07 embeds 2.2.10 as well.

| Plugin | Version | How it is requested |
| --- | --- | --- |
| `org.jetbrains.kotlin.plugin.compose` | 2.2.10 | catalog alias, in `:app` |
| `org.jetbrains.kotlin.multiplatform` | 2.2.10 (AGP-embedded) | **versionless `id(...)`**, in `:shared` |
| `com.android.kotlin.multiplatform.library` | ships with AGP | **versionless `id(...)`**, in `:shared` |

**The two `:shared` plugins must be requested without a version.** AGP 9 declares
`kotlin-gradle-plugin` as a plain runtime dependency, so resolving AGP anywhere in the build puts
KGP on the plugin classpath with no version Gradle can track. A versioned request then fails with
*"the plugin is already on the classpath with an unknown version, so compatibility cannot be
checked"* — reproduced with an empty root build script and `:app` excluded from the build entirely.
Versionless resolves from that classpath, which is 2.2.10, so the pin is honoured in effect.

**KSP cannot be used in a Kotlin Multiplatform module on this toolchain**, which is why there is no
Room in this design. Verified: KSP `2.2.10-2.0.2` crashes against
`com.android.kotlin.multiplatform.library` with `KotlinMultiplatformAndroidCompilationImpl cannot be
cast to KotlinJvmAndroidCompilation`, and the legacy `com.android.library` + `androidTarget()`
combination is rejected outright — *"not compatible with the org.jetbrains.kotlin.multiplatform
plugin since AGP 9.0"*. `2.2.10-2.0.2` is the newest KSP in the Kotlin 2.2.10 line.

The root `build.gradle` declared KSP `2.3.6`, which matches no Kotlin version this project can use.
It was never applied, so nothing failed. Phase 1 corrects the pin to `2.2.10-2.0.2`; once Room drops
out, KSP has no consumer at all and phase 7 removes the plugin and its catalog entry outright.

Existing floors are unchanged: `minSdk 23`, `compileSdk 36`, Java 21.

## Module structure

```
settings.gradle.kts               Groovy → Kotlin DSL
gradle/libs.versions.toml         new: version catalog
build.gradle.kts                  root, plugins declared `apply false`
shared/
  build.gradle.kts                org.jetbrains.kotlin.multiplatform
                                  + com.android.kotlin.multiplatform.library
                                  (both requested versionless — see Constraints)
  src/commonMain/kotlin/
  src/androidMain/kotlin/
  src/commonTest/kotlin/
app/
  build.gradle.kts                com.android.application — Android only, Compose
```

`:app` depends on `:shared`. `:shared` declares only the Android target for now:

```kotlin
kotlin {
    androidLibrary {
        namespace = "com.radionula.shared"
        compileSdk = 36
        minSdk = 23
        compilerOptions { jvmTarget = JvmTarget.JVM_21 }
        // Runs commonTest on the JVM as :shared:testAndroidHostTest.
        withHostTestBuilder {}
    }
}
```

No `withDeviceTestBuilder`: the one instrumented test in this migration lives in `:app`, which
already has a runner configured.

Adding iOS later is: add the three `ios*` targets plus `binaries.framework`, and write the
Swift UI and a `MediaPlayerController` actual. No module moves.

### Package naming

Kotlin packages stay `com.radionula.radionula.*` in **both** modules. Only the Gradle `namespace`
differs (`com.radionula.shared` vs `com.radionula.radionula`), and `namespace` affects only the
generated `R` class and the manifest — not Kotlin packages. Splitting one package across two Gradle
modules is legal on the JVM (no JPMS here), so moved files need no import edits. This is deliberate:
it removes the large majority of the diff.

## Code placement

### shared/commonMain

- `domain/model/NulaTrack`
- `domain/repository/PlaylistRepository`
- `data/repository/PlaylistRepositoryImpl`
- `data/db/entity/CurrentSong`
- `core/util/ChannelPresenter`
- `data/PlaylistApiService` — rewritten on Ktor
- `data/network/RecentlyPlayedParser` — rewritten on xmlutil
- `data/network/PlaylistNetworkDataSource` + `Impl`
- `data/db/NulaDatabase.kt` — the favourites store, hand-written SQL over the `androidx.sqlite` driver
- `features/player/RadioViewModel`
- `features/favorites/FavoritesViewModel`
- `services/mediaplayer/MediaPlayerController` — new interface
- `core/util/Log` — `expect fun logError(tag: String, message: String, cause: Throwable? = null)`
- Koin `sharedModule`

### shared/androidMain

- Nothing database-specific. The database file path needs a `Context`, so `:app` supplies it as a
  Koin binding and `commonMain` consumes it — Koin already performs the platform split, and an
  `expect`/`actual` pair with one implementation buys nothing. An iOS target supplies its own
  path from `iosMain` the same way.
- Ktor `client-okhttp` dependency. `PlaylistApiService` constructs `HttpClient { }` in `commonMain`
  with **no engine argument** — Ktor resolves the single engine on the classpath via `ServiceLoader`,
  so no `expect`/`actual` is needed. Adding `client-darwin` to a future `iosMain` works the same way.
- `actual` `logError` → `android.util.Log.e`
- Koin `androidSharedModule` supplying `androidContext()`-derived singletons

### Stays in `:app`

`MainActivity`, `MyApp`, every composable and theme file, all `R` resources, `ChannelArt`,
`RadioPlaybackService`, `TuningNoise`, the media3 `MediaPlayerController` implementation,
`CommentsScreen` (WebView), `ConnectivityLiveData`, Firebase Crashlytics/Analytics, Coil.

## Library changes

| Current | Replacement | Source set |
| --- | --- | --- |
| Retrofit 3.0.0 + OkHttp | Ktor **3.5.2** `client-core` | commonMain |
| — | Ktor `client-okhttp` | androidMain |
| `javax.xml` DOM | **xmlutil 0.91.3** `core`, pull reader | commonMain |
| `SQLiteOpenHelper` + hand-rolled SQL | **`androidx.sqlite:sqlite-bundled 2.7.0`** (driver only, no Room) | commonMain |
| `lifecycle-viewmodel-ktx` 2.9.1 | `androidx.lifecycle:lifecycle-viewmodel` **2.10.0** (KMP) | commonMain |
| Koin 4.1.0 (`koin-android`) | `koin-core` **4.2.2** + `koin-android` 4.2.2 | common / androidMain |
| `android.util.Log` | `expect`/`actual` `logError` | split |
| Coil 2.7 | unchanged | `:app` |
| media3, Firebase, WebView, Compose | unchanged | `:app` |

No `kotlinx-serialization` *usage*, and no xmlutil serialization artifact: the feed is parsed with
xmlutil's pull reader directly. `kotlinx-serialization-core` still reaches the runtime classpath
transitively through `ktor-client-core` and through xmlutil `core` itself at every version — that is
unavoidable and harmless. The constraint is about what the code uses and what is declared.

### Code changes forced by the move

1. **`RadioViewModel` drops `ChannelArt`.** It currently holds three `@DrawableRes Int`s, which
   cannot exist in `commonMain`. `PlayerUiState` exposes `channel: ChannelPresenter.Channel`
   instead, and `PlayerScreen` maps channel → drawables in `:app`. `ChannelArt`, `CLASSIC_ART` and
   `getChannelLogo` move to `:app` unchanged in behaviour.

2. **`MediaplayerPresenter` splits.** `commonMain` gets:

   ```kotlin
   interface MediaPlayerController {
       val isPlaying: StateFlow<Boolean>
       val channelIndex: StateFlow<Int>
       fun tuneIn(channelIndex: Int)
       fun nextChannel()
       fun pauseRadio()
   }
   ```

   The existing media3 `MediaController` class becomes the `:app` implementation and is bound in
   Koin from `:app`. `RadioViewModel` depends on the interface.

3. **`ConnectivityInterceptor`, `ConnectivityInterceptorImpl` and `NoConnectivityException` are
   deleted.** See "Connectivity awareness" below — the deletion is scoped to the OkHttp plumbing and
   changes no user-visible behaviour.

4. **`RecentlyPlayedParser` keeps its external-entity protection.** The current DOM parser sets
   `isExpandEntityReferences = false`. The xmlutil equivalent is the *generic* reader
   (`xmlStreaming.newGenericReader(xml)`), which does not resolve external entities — the platform
   readers can. The generic reader is required, not optional. Exact 1.0.1 API surface is confirmed
   against the library at implementation time; `RecentlyPlayedParserTest` pins the behaviour.

## Connectivity awareness

The app has **two independent** connectivity mechanisms today. Only the second one is affected by
the Ktor swap, and the distinction is what makes the deletion safe.

**1. The "no internet" screen — untouched.**

```
MainActivity → ConnectivityLiveData(ConnectivityManager)
             → NulaApp(connected) → NoConnectionOverlay()
```

`ConnectivityLiveData` registers a `ConnectivityManager.NetworkCallback` on `onActive` and
unregisters on `onInactive`, which is exactly the contract `observeAsState` honours. It has no
reference to `ConnectivityInterceptor`, to OkHttp, or to the data layer, and nothing in the data
layer references it. It stays in `:app` together with `ConnectionLiveDataTest`, the
`runtime-livedata` Compose dependency, and the `MainActivity` wiring — all unchanged by this
migration. **The overlay keeps working, driven by the same callback as today.**

**2. Fetch-time offline handling — same outcome, different layer.**

Today: the interceptor checks `ConnectivityManager` and throws `NoConnectivityException` before a
socket is opened. `PlaylistNetworkDataSourceImpl` catches it, logs `"No internet"`, returns `null`.
`PlaylistRepositoryImpl.fetchCurrentPlaylist()` sees `null` and returns early without emitting, so
no stale track is published, and `autoFetchPlaylist()` retries on its 30-second loop.

After: Ktor attempts the request, it fails with an `IOException`, and the **existing** catch-all in
`PlaylistNetworkDataSourceImpl` logs and returns `null`. Everything downstream is byte-for-byte the
same path: no emission, no stale track, same 30-second retry cadence, same recovery the moment the
network returns.

The two deliberate differences:

- An offline poll now opens a failing connection instead of short-circuiting locally. It fails
  immediately (no route / DNS failure), so this is not user-visible; it is not worth an
  Android-only `ConnectivityManager` check inside `commonMain` to avoid.
- The log line becomes the generic `"Could not read <feed>"` rather than a separate `"No internet"`.

Requirement: `PlaylistNetworkDataSourceImpl` must keep returning `null` on **any** fetch failure
rather than propagating. `PlaylistRepositoryImpl`'s early-return on `null` is what protects the
polling loop and the UI state, and it must not change.

Out of scope, noted for later: connectivity awareness lives in `:app` because the UI is Android-only.
When an iOS UI is added it will need a `commonMain` connectivity abstraction. Adding one now would be
an interface with a single implementation and no second consumer.

## Favorites data compatibility

This is the one place where getting it wrong destroys user data — but dropping Room turned it from
the migration hazard it was into a compatibility requirement.

The existing database is created by `MyDatabaseHelper` (`SQLiteOpenHelper`):

- file `NulaDB`, `user_version = 1`
- table `NulaTracks (_id INTEGER PRIMARY KEY AUTOINCREMENT, artist TEXT NOT NULL, title TEXT NOT NULL, image TEXT NOT NULL)`

`androidx.sqlite` is a driver, not an ORM. There is no `room_master_table`, no identity hash, no
`TableInfo` validation and therefore **no migration**. The requirement is simply that the new code
opens the same file and speaks the same schema:

- **Same path.** `:app` supplies `context.getDatabasePath("NulaDB").absolutePath` — the exact file
  `SQLiteOpenHelper` used.
- **Same DDL, frozen.** `CREATE TABLE IF NOT EXISTS` with the column list above runs on every open,
  which is what `MyDatabaseHelper`'s `onCreate` and `onUpgrade` both did. Idempotent, so an existing
  database is untouched and a fresh install gets the schema it always had.
- **Same ids.** `_id` is the favourites' identity for deletion, so `selectAllTracks` reads it back
  and `removeTrack` deletes by it. Ids must not be reassigned.

`NulaDatabase` keeps its current public surface (`insertTrack`, `selectAllTracks`, `removeTrack`, all
`suspend`) so `RadioViewModel` and `FavoritesViewModel` do not change. The manual
`withContext(Dispatchers.IO)` wrappers stay — the driver is synchronous.
`MyDatabaseHelper` is deleted.

**Verification (blocking):** an instrumented test that seeds a database file with the legacy
`SQLiteOpenHelper` schema plus rows, opens it through `NulaDatabase`, and asserts the rows are
readable with unchanged ids and that a delete removes the intended row. It lives in
`app/src/androidTest`, which already has a runner and depends on `:shared`. Phase 5 is not done until
it passes, and a real install-over-the-top upgrade has been checked by hand.

## Tests

| Test | Destination | Change |
| --- | --- | --- |
| `ChannelPresenterTest` | `shared/src/commonTest` | JUnit → `kotlin.test` |
| `RecentlyPlayedParserTest` | `shared/src/commonTest` | JUnit → `kotlin.test` |
| `PlaylistRepositoryImplTest` | `shared/src/commonTest` | Mockito → hand-written fake |
| `RadioViewModelTest` | **stays `app/src/test`** | `MediaplayerPresenter` → `MediaPlayerController` only |
| `ConnectionLiveDataTest` | stays `app/src/test` | none |
| `PlayerScreenTest` | stays `app/src/androidTest` | **none** |
| legacy-DB compatibility test | `app/src/androidTest` | new |

Mockito and mockito-kotlin are JVM-only and cannot be used from `commonTest`.
`PlaylistRepositoryImplTest` fakes exactly one interface, so a hand-written fake is smaller than the
library it replaces. `kotlinx-coroutines-test` is multiplatform and stays.

`RadioViewModelTest` stays on Mockito in `app/src/test`. It has four mocks and roughly twenty
`verify` assertions, two of them against final classes (`ChannelPresenter`, `NulaDatabase`), and
rewriting it as hand-rolled spies risks weakening assertions — which this section forbids. `:app`
depends on `:shared`, so the test still exercises the moved ViewModel and coverage is unchanged.
Rewrite it to fakes when an iOS target needs it to run on Native.

`PlayerScreenTest` needs no edits: it only constructs `PlayerUiState()` with defaults and named
arguments and never references `channelArt`, so replacing that property is source-compatible.

Backticked test method names are replaced with underscores in everything that moves to `commonTest` —
names containing spaces do not compile on Kotlin/Native.

Assertion semantics must not change: these tests are the regression net for the whole migration.

## Migration order

Each phase ends with a project that builds, installs and runs. No phase leaves the app broken.

The implementation plan splits phase 4 into two tasks — Ktor, then xmlutil — so each is
independently reviewable and revertible. Eight tasks, same order and same gates.

1. **Build plumbing.** `settings.gradle` and both `build.gradle` files → Kotlin DSL. Introduce
   `gradle/libs.versions.toml`. Correct the KSP pin to `2.2.10-2.0.2`. No source changes.
   *Done when:* `./gradlew assembleDebug` and `testDebugUnitTest` pass, app runs.
2. **Empty `:shared`.** Create the module with the KMP + `androidLibrary` plugins; `:app` depends on
   it. No code in it yet. *Done when:* build green.
3. **Move portable code.** `NulaTrack`, `CurrentSong`, `ChannelPresenter`, `PlaylistRepository`,
   `PlaylistRepositoryImpl`, the `PlaylistNetworkDataSource` **interface** and their tests →
   `commonMain`/`commonTest`. The interface has to move in this phase — `PlaylistRepositoryImpl`
   will not compile in `commonMain` without it. Its Retrofit-backed implementation stays in `:app`
   until phase 4 and is bound there in Koin. Add the `logError` expect/actual.
   *Done when:* moved tests pass in `:shared`, app runs.
4. **Network swap.** Retrofit → Ktor, DOM → xmlutil, delete the connectivity interceptor trio.
   `PlaylistApiService`, both data-source files and the parser land in `commonMain`.
   *Done when:* `RecentlyPlayedParserTest` passes unchanged; the running app shows live track
   metadata on all three channels; and, with the device in airplane mode, the "no internet" overlay
   still appears, the app does not crash, no stale track is published, and the playlist resumes
   updating within 30 seconds of the network returning.
5. **Favourites store swap.** `NulaDatabase` rewritten on the `androidx.sqlite` driver in
   `commonMain`, database path supplied from `:app` through Koin, `MyDatabaseHelper` deleted. The
   `NulaTracks` DDL is frozen and no migration is involved. *Done when:* the legacy-DB instrumented
   test passes and favorites saved by the pre-migration build are still listed, with working
   deletion, after an install over the top.
6. **ViewModels.** `RadioViewModel` and `FavoritesViewModel` → `commonMain`; `ChannelArt` moves to
   `:app`; `MediaPlayerController` interface split with the media3 implementation bound from `:app`.
   *Done when:* moved tests pass, `PlayerScreenTest` passes, playback and channel skip work from
   both the UI and the notification.
7. **Cleanup.** Drop Retrofit/OkHttp and the now-consumerless KSP plugin and catalog entry, remove
   dead imports, confirm the release build. *Done when:* `assembleRelease` succeeds and a minified
   build plays audio, lists favorites and loads comments on a device.

## Risks

- **R8 / release build.** The `androidx.sqlite` driver (which ships native libraries) and Ktor are new to the minified build. `android.r8.strictFullModeForKeepRules=false` is already set. Phase 7 explicitly verifies a minified release on device rather than assuming the debug build generalises.
- **Favourites data loss.** The DDL and the database path are frozen; the instrumented test plus a hand-checked install-over-the-top are the gate. No destructive fallback exists to hide a mistake.
- **Compose compiler.** Applied only to `:app`. `:shared` contains no composables, so the plugin is not added there.
- **Firebase.** `google-services` and the Crashlytics plugin stay on `:app` only; `google-services.json` does not move.
- **No annotation processing in `:shared`.** KSP cannot run in a KMP module on this toolchain (see Constraints), so anything needing codegen — Room, a serialization processor, a DI processor — cannot live in `commonMain` until that changes. Koin's runtime DSL and hand-written SQL are chosen partly for this reason.
- **xmlutil is pinned to 0.91.3, not the 1.0.x line.** 1.0.x is built against Kotlin 2.4.0 and its metadata cannot be read by the 2.2.10 compiler AGP 9.3.1 embeds (`compiler version 2.2.0 can read versions up to 2.3.0`). 0.91.3 is the newest usable release — verified by building each candidate. The accessor is `XmlStreaming.newGenericReader`; the lowercase `xmlStreaming` rename belongs to the 1.0 line and does not apply here.

---

## Post-migration status

The migration shipped across 8 tasks. Recorded here because these outlive the
scratch workspace the execution ran in.

### Verified

`commonMain` contains no `java.*`, `javax.*` or `android.*` usage, and every
dependency it relies on publishes iOS artifacts (coroutines `Dispatchers.IO` via
`concurrentMain`, xmlutil `XmlStreaming` via `commonMain`, lifecycle-viewmodel,
androidx.sqlite, Koin, Ktor). A minified release build succeeds with **no new R8
keep rules**, and the sqlite driver, Ktor and xmlutil all survive minification
with real feed data reaching the media session.

Tests: 36 unit (26 `:shared`, 17 `:app` — 26+17 counts one shared `PlatformTest`
added by the migration) and 11 instrumented. All 35 pre-migration tests are
accounted for: 18 moved to `commonTest`, 17 stayed in `:app`, none dropped.

### Deliberate decisions worth knowing

- **`BundledSQLiteDriver` costs 4.24 MB** of native libraries — 36% of the release
  APK, where the pre-migration build shipped none. Kept deliberately, for a SQLite
  version identical across all devices and OS versions. Revisit alongside ABI
  splits or an app bundle if download size becomes a concern; `.gitlab-ci.yml`
  currently publishes an APK, so every user gets all four ABIs.
- **`:shared` declares only an Android target**, so the compiler does not enforce
  the "no Android APIs in `commonMain`" property — it was hand-audited. Adding
  `iosArm64()` would make it compiler-enforced, and is the single highest-value
  follow-up. It needs four small pieces: `actual fun logError`, `actual fun
  epochMillis`, a `ktor-client-darwin` dependency, and a `databasePath` binding.
- **The spec's claim that iOS needs "only UI and a player implementation"
  understates it** by those four items. All four live inside `:shared` or the iOS
  app — no module moves and no code migration — which is the substance of the
  claim, but the sentence is optimistic.

### Known gaps, not introduced by this migration

- **No auto-reconnect when connectivity returns.** `ConnectivityLiveData` drives
  only the offline overlay, and `RadioPlaybackService` has no `onPlayerError`
  handler, so ExoPlayer sits in an error state after airplane-mode-off until the
  next manual player action. The *feed poll* does recover within 30 s; the *audio
  stream* does not resume. `RadioPlaybackService` is touched by zero migration
  commits.
- **No release signing config** — `:app:installRelease` does not exist.
- **`android.r8.strictFullModeForKeepRules=false`** in `gradle.properties` says it
  is worth revisiting "once a minified release has been shipped and verified".
  That condition is now met, and nothing tracks it.
- **Substantial dead resources** orphaned by the earlier Fragments→Compose
  migration: ~60 files across drawables, mipmaps, assets, and 8 of 9 strings
  (including three dead stream URLs pointing at the retired
  `streaming.radionula.com:8800`). No `R.string.` reference exists anywhere.

### One parked review finding

The test pinning `RecentlyPlayedParser`'s external-entity protection asserts the
*property* (entities are not resolved) rather than the call site. It cannot
currently distinguish `newGenericReader` from `newReader`, because xmlutil 0.91.3
resolves only `core-jvmcommon` here with no StAX or Android factory on the
classpath, so both overloads land on `GenericFactory` and both are safe. The test
guards the property correctly and will fail once the property becomes breakable.
Kept as-is: asserting the reader's type would be brittle, and forcing a StAX
factory into `commonTest` would add a non-multiplatform dependency to prove a
negative.
