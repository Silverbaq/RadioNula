# RadioNula → Kotlin Multiplatform

Date: 2026-07-31
Status: Approved

## Goal

Restructure RadioNula as a Kotlin Multiplatform project targeting **Android only**, with every
platform-agnostic layer living in `commonMain`. No iOS/desktop target is added now. Adding
`iosArm64` later must require writing UI and a player implementation — nothing else.

Non-goals: Compose Multiplatform, iOS app, desktop/web targets, feature changes, visual changes.

## Constraints

AGP 9.3.1 embeds Kotlin Gradle Plugin **2.2.10** (verified in the AGP POM). Every Kotlin plugin is
pinned to that version:

| Plugin | Version |
| --- | --- |
| `org.jetbrains.kotlin.multiplatform` | 2.2.10 |
| `org.jetbrains.kotlin.plugin.compose` | 2.2.10 |
| `com.google.devtools.ksp` | **2.2.10-2.0.2** |
| `com.android.kotlin.multiplatform.library` | 9.3.1 (ships with AGP) |

The root `build.gradle` currently declares KSP `2.3.6`, which does not match Kotlin 2.2.10. It is
never applied today so nothing fails, but Room needs KSP — the pin is corrected in phase 1.

Existing floors are unchanged: `minSdk 23`, `compileSdk 36`, Java 21.

## Module structure

```
settings.gradle.kts               Groovy → Kotlin DSL
gradle/libs.versions.toml         new: version catalog
build.gradle.kts                  root, plugins declared `apply false`
shared/
  build.gradle.kts                kotlin("multiplatform")
                                  + com.android.kotlin.multiplatform.library
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
        withHostTestBuilder {}
        withDeviceTestBuilder { sourceSetTreeName = "test" }
            .configure { instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
    }
}
```

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
- `data/db/` — Room entity, DAO, `NulaDatabase`
- `features/player/RadioViewModel`
- `features/favorites/FavoritesViewModel`
- `services/mediaplayer/MediaPlayerController` — new interface
- `core/util/Log` — `expect fun logError(tag: String, message: String, cause: Throwable? = null)`
- Koin `sharedModule`

### shared/androidMain

- `actual` Room database builder (needs `Context`)
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
| `javax.xml` DOM | **xmlutil 1.0.1** `core`, pull reader | commonMain |
| `SQLiteOpenHelper` + hand-rolled SQL | **Room 2.8.4** + `androidx.sqlite:sqlite-bundled 2.7.0` | commonMain |
| `lifecycle-viewmodel-ktx` 2.9.1 | `androidx.lifecycle:lifecycle-viewmodel` **2.11.0** (KMP) | commonMain |
| Koin 4.1.0 (`koin-android`) | `koin-core` **4.2.2** + `koin-android` 4.2.2 | common / androidMain |
| `android.util.Log` | `expect`/`actual` `logError` | split |
| Coil 2.7 | unchanged | `:app` |
| media3, Firebase, WebView, Compose | unchanged | `:app` |

No `kotlinx-serialization`. The feed is parsed with xmlutil's pull reader directly, which keeps the
dependency and the R8 keep-rule surface smaller.

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

## Favorites data migration

This is the one place where getting it wrong destroys user data.

The existing database is created by `MyDatabaseHelper` (`SQLiteOpenHelper`):

- file `NulaDB`, `user_version = 1`
- table `NulaTracks (_id INTEGER PRIMARY KEY AUTOINCREMENT, artist TEXT NOT NULL, title TEXT NOT NULL, image TEXT NOT NULL)`

Room refuses to open a database it did not create: there is no `room_master_table`, so identity
verification throws *"Room cannot verify the data integrity"*.

**Solution:** declare `@Database(version = 2)` with a no-op `Migration(1, 2)`. Room runs the
migration on existing installs, then writes its identity hash and adopts the database in place.
Fresh installs are created at version 2 directly.

The entity must generate DDL identical to the legacy table:

```kotlin
@Entity(tableName = "NulaTracks")
data class NulaTrackEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") val id: Int = 0,
    val artist: String,
    val title: String,
    val image: String,
)
```

`fallbackToDestructiveMigration` is **not** used anywhere — it would silently wipe favorites.

`NulaDatabase`'s current public surface (`insertTrack`, `selectAllTracks`, `removeTrack`, all
`suspend`) is preserved so `RadioViewModel` and `FavoritesViewModel` do not change. Room's own
`Dispatchers.IO` query context replaces the manual `withContext(Dispatchers.IO)` wrappers.
`MyDatabaseHelper` is deleted.

**Verification (blocking):** an instrumented test that seeds a database file with the legacy
`SQLiteOpenHelper` schema plus rows, opens it through Room, and asserts the rows are readable and
the ids are unchanged. It lives in `app/src/androidTest`, which already has a runner and depends on
`:shared` — no new source set is introduced for one test. Phase 5 is not done until this passes.

## Tests

| Test | Destination | Change |
| --- | --- | --- |
| `ChannelPresenterTest` | `shared/src/commonTest` | JUnit → `kotlin.test` |
| `RecentlyPlayedParserTest` | `shared/src/commonTest` | JUnit → `kotlin.test` |
| `PlaylistRepositoryImplTest` | `shared/src/commonTest` | Mockito → hand-written fake |
| `RadioViewModelTest` | `shared/src/commonTest` | Mockito → hand-written fakes |
| `ConnectionLiveDataTest` | stays `app/src/test` | none |
| `PlayerScreenTest` | stays `app/src/androidTest` | drawable assertions follow `ChannelArt` move |
| legacy-DB migration test | `app/src/androidTest` | new |

Mockito and mockito-kotlin are JVM-only and cannot be used from `commonTest`. Each migrating test
fakes exactly one or two interfaces (`PlaylistNetworkDataSource`, `MediaPlayerController`,
`PlaylistRepository`, `NulaDatabase`), so hand-written fakes replace them without a mocking library.
`kotlinx-coroutines-test` is multiplatform and stays.

Assertion semantics must not change: these tests are the regression net for the whole migration.

## Migration order

Each phase ends with a project that builds, installs and runs. No phase leaves the app broken.

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
5. **Room swap.** Entity/DAO/database in `commonMain`, builder `actual` in `androidMain`, no-op
   1→2 migration, delete `MyDatabaseHelper`. *Done when:* the legacy-DB instrumented test passes
   and favorites saved by the pre-migration build are still listed after upgrade.
6. **ViewModels.** `RadioViewModel` and `FavoritesViewModel` → `commonMain`; `ChannelArt` moves to
   `:app`; `MediaPlayerController` interface split with the media3 implementation bound from `:app`.
   *Done when:* moved tests pass, `PlayerScreenTest` passes, playback and channel skip work from
   both the UI and the notification.
7. **Cleanup.** Drop Retrofit/OkHttp/mockito dependencies that no longer have a consumer, remove
   dead imports, confirm the release build. *Done when:* `assembleRelease` succeeds and a minified
   build plays audio, lists favorites and loads comments on a device.

## Risks

- **R8 / release build.** Room and Ktor are new to the minified build. `android.r8.strictFullModeForKeepRules=false` is already set. Phase 7 explicitly verifies a minified release on device rather than assuming the debug build generalises.
- **Room identity hash.** Covered above; the instrumented test is the gate.
- **Compose compiler.** Applied only to `:app`. `:shared` contains no composables, so the plugin is not added there.
- **Firebase.** `google-services` and the Crashlytics plugin stay on `:app` only; `google-services.json` does not move.
- **KSP for one target.** Room's compiler is registered via `add("kspAndroid", ...)`. When iOS targets are added, matching `kspIos*` entries must be added too, or Room silently generates nothing for them.
- **xmlutil 1.0.1** is a recent major release; the pull-reader accessor was renamed from `XmlStreaming` to `xmlStreaming` across the 1.0 line. The exact call is confirmed against the library during phase 4, with `RecentlyPlayedParserTest` as the check.
