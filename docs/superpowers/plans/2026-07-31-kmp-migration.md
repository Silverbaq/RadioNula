# RadioNula KMP Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restructure RadioNula as a Kotlin Multiplatform project targeting Android only, with every platform-agnostic layer in `commonMain`, so adding an iOS target later needs only UI and a player implementation.

**Architecture:** A new `:shared` KMP library module (`com.android.kotlin.multiplatform.library`) holds domain, data, repository and ViewModel code in `commonMain`, with a thin `androidMain` for the Ktor engine and two platform functions. `:app` stays a plain `com.android.application` module owning all Compose UI, media3 playback, Firebase and the WebView comments screen, and depends on `:shared`. Android-locked libraries are swapped for multiplatform equivalents: Retrofit→Ktor, `javax.xml` DOM→xmlutil, `SQLiteOpenHelper`→the multiplatform `androidx.sqlite` driver.

**Tech Stack:** Kotlin 2.2.10, AGP 9.3.1, Gradle 9.5, Ktor 3.5.2, xmlutil 0.91.3, `androidx.sqlite:sqlite-bundled` 2.7.0, Koin 4.2.2, androidx.lifecycle 2.10.0, Jetpack Compose (BOM 2026.03.00), media3 1.7.1.

**Spec:** `docs/superpowers/specs/2026-07-31-kmp-migration-design.md`

## Global Constraints

- **AGP 9.3.1 embeds Kotlin Gradle Plugin 2.2.10.** Every Kotlin plugin pins to `2.2.10`. KSP pins to `2.2.10-2.0.2`. Do not bump Kotlin without bumping AGP.
- `minSdk = 23`, `targetSdk = 36`, `compileSdk = 36`, `buildToolsVersion = "36.0.0"`, Java/JVM target **21**.
- `applicationId = "com.radionula.nula"`, `:app` namespace `com.radionula.radionula`, `:shared` namespace `com.radionula.shared`.
- **Kotlin packages stay `com.radionula.radionula.*` in both modules.** Moved files get no import edits. Do not rename packages.
- **No Compose in `:shared`.** The Compose compiler plugin is applied only to `:app`.
- **No `kotlinx-serialization` *usage*, and no xmlutil serialization artifact.** The RSS feed is parsed with xmlutil's pull reader directly. Note `kotlinx-serialization-core` lands on the runtime classpath transitively anyway — via `ktor-client-core`, and via xmlutil `core` at every version — which is unavoidable and harmless. The constraint is about what the code uses and what artifacts are declared, not about the resolved runtime graph.
- **`:shared` requests the KMP plugins without a version** — `id("org.jetbrains.kotlin.multiplatform")`, not `alias(...)`. AGP 9 puts KGP on the plugin classpath untracked, so a versioned request fails. Versionless resolves to the AGP-embedded 2.2.10.
- **No KSP, and therefore no Room, in `:shared`.** KSP `2.2.10-2.0.2` cannot run in a `com.android.kotlin.multiplatform.library` module on this toolchain. Do not add either plugin to `:shared`.
- **The favorites table DDL is frozen** at what `MyDatabaseHelper` wrote: `NulaTracks (_id INTEGER PRIMARY KEY AUTOINCREMENT, artist TEXT NOT NULL, title TEXT NOT NULL, image TEXT NOT NULL)`. Favorites are user data; changing the column list strands them.
- **`PlaylistNetworkDataSourceImpl` must return `null` on any fetch failure**, never propagate. `PlaylistRepositoryImpl`'s early-return on `null` is what protects the polling loop and the UI state.
- **Assertion semantics of migrated tests must not change.** They are the regression net for the whole migration.
- Every task ends with a project that builds, installs and runs. Never leave `master`/`develop` mergeable-but-broken.
- Work happens on the `KMP` branch.
- Commit after every task. **Ask the user before running `git commit`** — the project's `CLAUDE.md` requires it, every time, even when previously approved.

## Deviations from the spec

Corrections found while verifying the spec against the actual code, and while executing it. The spec is updated to match.

1. ~~**The Room migration is a table rebuild, not a no-op.**~~ **Superseded by deviation 6** — Room is gone, so there is no migration at all. Kept for the record because the finding is what made the Room path look expensive before it turned out to be impossible: verified with `sqlite3`, the legacy `_id INTEGER PRIMARY KEY AUTOINCREMENT` reports `pragma table_info` → `notnull = 0` while Room's generated DDL emits explicit `NOT NULL` → `notnull = 1`, and Room's `TableInfo` validation compares that field.
2. **`RadioViewModelTest` stays in `app/src/test` with Mockito.** It has four mocks and ~20 `verify` assertions, two of them on final classes (`ChannelPresenter`, `NulaDatabase`). Rewriting it as hand-rolled spies risks weakening assertions, which the spec forbids. `:app`'s JVM unit tests can exercise `:shared` classes directly, so coverage is unchanged. Upgrade path: rewrite to fakes when an iOS target is added and the test needs to run on Native.
3. **`PlayerScreenTest` needs no changes.** It only ever constructs `PlayerUiState()` with defaults and named arguments, and never references `channelArt`, so replacing that property is source-compatible with the test.
4. **The database path is a Koin binding in `:app`, not an `expect`/`actual` in `androidMain`.** The spec called for an `actual` factory because it needs a `Context`. Koin already performs that platform split — `:app` supplies the path via `androidContext()`, and `commonMain` consumes it. This removes an `expect`/`actual` pair with one implementation. An iOS target supplies its own path from `iosMain` the same way, so nothing is foreclosed.

Two further corrections, both found by Task 2 failing and both verified empirically in-repo:

5. **`:shared` requests its KMP plugins without a version.** `alias(libs.plugins.kotlinMultiplatform)` fails with *"the plugin is already on the classpath with an unknown version, so compatibility cannot be checked"*. AGP 9 declares `kotlin-gradle-plugin` as a plain runtime dependency, so resolving AGP anywhere in the build puts KGP on the plugin classpath with no version Gradle can track — reproduced with an empty root build script and no `:app` in the build at all. Versionless `id("...")` resolves from that classpath, which is the AGP-embedded 2.2.10, so the constraint's intent is preserved exactly. The two catalog plugin aliases are therefore never added.

6. **Room is replaced by the `androidx.sqlite` driver** — see the note at the head of Task 6 for the three verified dead ends (KSP crashes against the AGP 9 KMP plugin, AGP 9 rejects the legacy `com.android.library` + `androidTarget()` combination, and AGP 9.4.0-alpha07 embeds the same Kotlin). This removes the riskiest part of the migration rather than adding risk: with no Room, there is no `TableInfo` validation, no identity hash, and no migration, so the legacy `NulaTracks` table is used exactly as written. The `notnull` mismatch documented in deviation 1 no longer applies to anything.

## File Structure

**Created**

| File | Responsibility |
| --- | --- |
| `gradle/libs.versions.toml` | Single source of dependency and plugin versions |
| `settings.gradle.kts` | Replaces `settings.gradle`; repositories + module includes |
| `build.gradle.kts` | Replaces root `build.gradle`; plugin aliases, all `apply false` |
| `app/build.gradle.kts` | Replaces `app/build.gradle` |
| `shared/build.gradle.kts` | KMP module config, single Android target |
| `shared/src/commonMain/kotlin/com/radionula/radionula/core/util/Platform.kt` | `expect fun logError`, `expect fun epochMillis` |
| `shared/src/androidMain/kotlin/com/radionula/radionula/core/util/Platform.android.kt` | Their `actual`s |
| `shared/src/commonMain/kotlin/com/radionula/radionula/core/di/SharedModule.kt` | Koin module for shared singletons |
| `shared/src/commonMain/kotlin/com/radionula/radionula/services/mediaplayer/MediaPlayerController.kt` | Player interface consumed by `RadioViewModel` |
| `app/src/main/java/com/radionula/radionula/features/player/ChannelArt.kt` | Per-channel drawables, moved out of the ViewModel |
| `app/src/androidTest/java/com/radionula/radionula/LegacyFavoritesMigrationTest.kt` | Proves favorites written by the old `SQLiteOpenHelper` are still readable |

**Moved to `shared/src/commonMain`** (no content change unless a task says otherwise): `domain/model/NulaTrack.kt`, `domain/repository/PlaylistRepository.kt`, `data/repository/PlaylistRepositoryImpl.kt`, `data/db/entity/CurrentSong.kt`, `core/util/channelPresenter.kt`, `data/network/PlaylistNetworkDataSource.kt`, `data/PlaylistApiService.kt`, `data/network/PlaylistNetworkDataSourceImpl.kt`, `data/network/RecentlyPlayedParser.kt`, `data/db/NulaDatabase.kt`, `features/player/RadioViewModel.kt`, `features/favorites/FavoritesViewModel.kt`.

**Moved to `shared/src/commonTest`:** `ChannelPresenterTest.kt`, `PlaylistRepositoryImplTest.kt`, `RecentlyPlayedParserTest.kt`.

**Deleted:** `core/util/MyDatabaseHelper.kt`, `core/exceptions/Exceptions.kt`, `data/network/ConnectivityInterceptor.kt`, `data/network/ConnectivityInterceptorImpl.kt`, `settings.gradle`, `build.gradle`, `app/build.gradle`, `filestructure.txt` (a scratch template, not project documentation).

**Stays in `:app`, modified:** `features/player/PlayerScreen.kt` (reads `state.channel`), `features/player/PlaylistModule.kt`, `data/db/DatabaseModule.kt`, `services/mediaplayer/RadioPlayerModule.kt`, `MyApp.kt`, `services/mediaplayer/MediaplayerPresenter.kt` (implements the new interface).

**Stays in `:app`, untouched:** `MainActivity.kt`, `core/ui/**`, `features/comments/CommentsScreen.kt`, `features/favorites/FavoritesScreen.kt`, `core/util/ConnectivityLiveData.kt`, `services/RadioPlaybackService.kt`, `services/mediaplayer/TuningNoise.kt`, `app/src/test/ConnectionLiveDataTest.kt`, `app/src/test/RadioViewModelTest.kt`, `app/src/androidTest/PlayerScreenTest.kt`.

---

### Task 1: Gradle to Kotlin DSL with a version catalog

Pure build-file conversion. **No source file changes and no dependency version changes**, so the existing test suite is the proof it worked. The one exception is the KSP pin, which is wrong today (`2.3.6` against Kotlin 2.2.10) but never applied, so it fails only once Room needs it in Task 6.

**Files:**
- Create: `gradle/libs.versions.toml`
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `app/build.gradle.kts`
- Delete: `settings.gradle`, `build.gradle`, `app/build.gradle`

**Interfaces:**
- Consumes: nothing.
- Produces: the catalog itself, plus the accessors later tasks build on — `libs.versions.compileSdk`, `libs.versions.minSdk`, `libs.versions.kotlin`, `libs.versions.agp`, `libs.plugins.ksp`, and the library aliases listed in Step 2. Each later task adds the catalog entries it introduces: Task 2 adds `kotlinMultiplatform` and `androidKotlinMultiplatformLibrary`, Task 6 adds `room`. Task 1 does not declare them.

- [ ] **Step 1: Record the current test baseline**

Run: `./gradlew :app:testDebugUnitTest`

Expected: PASS. Note the number of tests executed — the same number must pass at the end of this task. If it fails before any change, stop and report; the plan assumes a green baseline.

- [ ] **Step 2: Create the version catalog**

Create `gradle/libs.versions.toml`:

```toml
[versions]
agp = "9.3.1"
kotlin = "2.2.10"
# Must track the Kotlin version AGP embeds. AGP 9.3.1 embeds KGP 2.2.10.
ksp = "2.2.10-2.0.2"
googleServices = "4.4.3"
crashlyticsPlugin = "3.0.4"

compileSdk = "36"
minSdk = "23"
targetSdk = "36"
buildTools = "36.0.0"

appcompat = "1.7.1"
material = "1.12.0"
coreKtx = "1.16.0"
activityCompose = "1.11.0"
composeBom = "2026.03.00"
navigationCompose = "2.9.1"
lifecycle = "2.9.1"
coroutines = "1.10.2"
koin = "4.1.0"
retrofit = "3.0.0"
coil = "2.7.0"
media3 = "1.7.1"
firebaseCrashlytics = "19.4.4"
firebaseAnalytics = "22.5.0"

junit = "4.13.2"
mockito = "5.18.0"
mockitoKotlin = "2.2.0"
archCoreTesting = "2.2.0"
androidxTestCore = "1.6.1"
androidxTestRules = "1.6.1"
androidxTestRunner = "1.7.0"
androidxTestExtJunit = "1.2.1"
espresso = "3.7.0"

[plugins]
androidApplication = { id = "com.android.application", version.ref = "agp" }
composeCompiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
googleServices = { id = "com.google.gms.google-services", version.ref = "googleServices" }
crashlytics = { id = "com.google.firebase.crashlytics", version.ref = "crashlyticsPlugin" }

[libraries]
androidx-appcompat = { module = "androidx.appcompat:appcompat", version.ref = "appcompat" }
android-material = { module = "com.google.android.material:material", version.ref = "material" }
androidx-core-ktx = { module = "androidx.core:core-ktx", version.ref = "coreKtx" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activityCompose" }
androidx-navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigationCompose" }

androidx-lifecycle-viewmodel-ktx = { module = "androidx.lifecycle:lifecycle-viewmodel-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }

compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
compose-material3 = { module = "androidx.compose.material3:material3" }
compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
compose-runtime-livedata = { module = "androidx.compose.runtime:runtime-livedata" }
compose-ui-test-junit4 = { module = "androidx.compose.ui:ui-test-junit4" }
compose-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest" }

kotlinx-coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }

koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }
koin-androidx-compose = { module = "io.insert-koin:koin-androidx-compose", version.ref = "koin" }

retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }

coil = { module = "io.coil-kt:coil", version.ref = "coil" }
coil-compose = { module = "io.coil-kt:coil-compose", version.ref = "coil" }

media3-exoplayer = { module = "androidx.media3:media3-exoplayer", version.ref = "media3" }
media3-session = { module = "androidx.media3:media3-session", version.ref = "media3" }
media3-common = { module = "androidx.media3:media3-common", version.ref = "media3" }

firebase-crashlytics-ktx = { module = "com.google.firebase:firebase-crashlytics-ktx", version.ref = "firebaseCrashlytics" }
firebase-analytics = { module = "com.google.firebase:firebase-analytics", version.ref = "firebaseAnalytics" }

junit = { module = "junit:junit", version.ref = "junit" }
mockito-core = { module = "org.mockito:mockito-core", version.ref = "mockito" }
mockito-android = { module = "org.mockito:mockito-android", version.ref = "mockito" }
mockito-kotlin = { module = "com.nhaarman.mockitokotlin2:mockito-kotlin", version.ref = "mockitoKotlin" }
androidx-arch-core-testing = { module = "androidx.arch.core:core-testing", version.ref = "archCoreTesting" }
androidx-test-core = { module = "androidx.test:core", version.ref = "androidxTestCore" }
androidx-test-rules = { module = "androidx.test:rules", version.ref = "androidxTestRules" }
androidx-test-runner = { module = "androidx.test:runner", version.ref = "androidxTestRunner" }
androidx-test-ext-junit = { module = "androidx.test.ext:junit", version.ref = "androidxTestExtJunit" }
espresso-core = { module = "androidx.test.espresso:espresso-core", version.ref = "espresso" }
```

- [ ] **Step 3: Create `settings.gradle.kts` and delete `settings.gradle`**

Create `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

rootProject.name = "RadioNula"

include(":app")
```

Then `git rm settings.gradle`.

`dependencyResolutionManagement` replaces the old root `allprojects { repositories { ... } }` and the stray `repositories { mavenCentral() }` at the bottom of `app/build.gradle`. Both are dropped, not carried over.

- [ ] **Step 4: Create root `build.gradle.kts` and delete root `build.gradle`**

Create `build.gradle.kts`:

```kotlin
// Top-level build file. Plugins are declared here and applied in modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.crashlytics) apply false
}
```

Then `git rm build.gradle`.

This replaces the `buildscript { classpath ... }` block. AGP now comes through the plugins DSL, which is what the `:shared` module needs in Task 2 in order to use `alias(...)`.

- [ ] **Step 5: Create `app/build.gradle.kts` and delete `app/build.gradle`**

Create `app/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.crashlytics)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()

    defaultConfig {
        applicationId = "com.radionula.nula"
        // 23 is Compose's floor - androidx.compose.material ships minSdk 23.
        // This drops Android 5.0 and 5.1.
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 24
        versionName = "2.3.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    namespace = "com.radionula.radionula"

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.android.material)
    implementation(libs.androidx.core.ktx)

    implementation(libs.retrofit)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.coil)
    implementation(libs.coil.compose)

    // Compose
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // collectAsStateWithLifecycle
    implementation(libs.androidx.lifecycle.runtime.compose)
    // ConnectivityLiveData drives its callback off onActive/onInactive, which
    // observeAsState honours - so it stays a LiveData rather than being rewritten.
    implementation(libs.compose.runtime.livedata)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media3.common)

    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.analytics)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.androidx.arch.core.testing)

    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.mockito.android)
    // 3.6.1 calls InputManager.getInstance(), which newer Android removed, so
    // every Compose test died in Espresso.onIdle before reaching an assertion.
    androidTestImplementation(libs.espresso.core)
}
```

Then `git rm app/build.gradle`.

- [ ] **Step 6: Verify the conversion changed nothing**

Run: `./gradlew :app:testDebugUnitTest :app:assembleDebug`

Expected: PASS, with the same test count as Step 1. If Gradle reports an unresolved dependency, the catalog alias in Step 2 does not match the coordinate in Step 5 — fix the alias, do not add the coordinate as a literal string.

- [ ] **Step 7: Install and smoke-test**

Run: `./gradlew :app:installDebug`

Then on the device: open the app, tap tune in, confirm audio plays and the track title appears. This is a build-system-only change, so anything broken here is a conversion mistake, not a migration risk.

- [ ] **Step 8: Commit**

Ask the user for permission to commit, then:

```bash
git add gradle/libs.versions.toml settings.gradle.kts build.gradle.kts app/build.gradle.kts
git rm settings.gradle build.gradle app/build.gradle
git commit -m "build: convert to Kotlin DSL with a version catalog

Moves AGP off the legacy buildscript classpath and onto the plugins DSL so
the KMP module in the next commit can use alias(). Repositories move to
settings dependencyResolutionManagement.

Corrects the KSP pin from 2.3.6 to 2.2.10-2.0.2. AGP 9.3.1 embeds KGP
2.2.10; the old pin was never applied, so it would have failed only once
Room needed it."
```

---

### Task 2: Create the `:shared` module

An empty KMP module wired into the build, with one trivial test to prove the test task works. No production code moves yet — this task exists so that a broken KMP build configuration is diagnosed on its own, not tangled with a code move.

**Files:**
- Create: `shared/build.gradle.kts`
- Create: `shared/src/commonMain/kotlin/com/radionula/radionula/core/util/Platform.kt`
- Create: `shared/src/androidMain/kotlin/com/radionula/radionula/core/util/Platform.android.kt`
- Create: `shared/src/commonTest/kotlin/com/radionula/radionula/core/util/PlatformTest.kt`
- Modify: `settings.gradle.kts`
- Modify: `app/build.gradle.kts`
- Modify: `gradle/libs.versions.toml`

**Interfaces:**
- Consumes: catalog accessors from Task 1.
- Produces:
  - `fun logError(tag: String, message: String, cause: Throwable? = null)` in package `com.radionula.radionula.core.util`
  - `fun epochMillis(): Long` in the same package
  - the Gradle project `:shared`, on `:app`'s `implementation` configuration
  - the shared unit-test task, confirmed as `:shared:testAndroidHostTest`

- [ ] **Step 1: Add the KMP plugin and shared dependencies to the catalog**

In `gradle/libs.versions.toml`, add to `[versions]`:

```toml
kotlinxCoroutinesCore = "1.10.2"
```

Add to `[libraries]`:

```toml
kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinxCoroutinesCore" }
```

**Do not add catalog plugin aliases for the KMP plugins.** They would be unused: the two plugins `:shared` needs are already on the plugin classpath via AGP, and must be requested *without* a version — see Step 2. A catalog alias carries a version by construction, which is exactly what breaks here.

- [ ] **Step 2: Create `shared/build.gradle.kts`**

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // Versionless on purpose, and load-bearing.
    //
    // AGP 9 declares kotlin-gradle-plugin as a plain runtime dependency, so
    // resolving AGP anywhere in the build puts KGP on the plugin classpath with
    // no version Gradle can track. Requesting either of these *with* a version
    // then fails outright: "the plugin is already on the classpath with an
    // unknown version, so compatibility cannot be checked". Versionless
    // resolves from that classpath - which is the AGP-embedded 2.2.10, the
    // version this project pins to anyway.
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    androidLibrary {
        namespace = "com.radionula.shared"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }

        // Runs commonTest on the JVM. No device-test builder: the one
        // instrumented test in this migration lives in :app, which already
        // has a runner configured.
        withHostTestBuilder {}
    }

    sourceSets {
        commonMain.dependencies {
            // api, not implementation: Flow and StateFlow appear in the public
            // API that :app consumes.
            api(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
```

- [ ] **Step 3: Include the module and depend on it**

In `settings.gradle.kts`, after `include(":app")`:

```kotlin
include(":shared")
```

In `app/build.gradle.kts`, as the first entry of the `dependencies` block:

```kotlin
    implementation(project(":shared"))
```

- [ ] **Step 4: Write the failing test**

Create `shared/src/commonTest/kotlin/com/radionula/radionula/core/util/PlatformTest.kt`:

```kotlin
package com.radionula.radionula.core.util

import kotlin.test.Test
import kotlin.test.assertTrue

class PlatformTest {

    @Test
    fun epoch_millis_is_a_real_wall_clock_time() {
        // Sanity check that the actual is wired up at all: any plausible
        // wall-clock value is after 2020-01-01.
        assertTrue(epochMillis() > 1_577_836_800_000L)
    }
}
```

- [ ] **Step 5: Run the test to verify it fails**

Run: `./gradlew :shared:testAndroidHostTest`

Expected: FAIL with "Unresolved reference 'epochMillis'".

`:shared:testAndroidHostTest` is the confirmed task name under the `androidLibrary` DSL — verified against `:shared:tasks --all`, which describes it as "Run unit tests for the androidMain build". Every later task uses it. If the failure is anything other than the unresolved reference, stop and report: a plugin or configuration problem is being masked.

- [ ] **Step 6: Write the expect declarations**

Create `shared/src/commonMain/kotlin/com/radionula/radionula/core/util/Platform.kt`:

```kotlin
package com.radionula.radionula.core.util

/**
 * The two things this project needs from the platform that Kotlin common does
 * not provide. Kept in one file so the list stays visible and short.
 */

/** Error logging. On Android this is the same Log.e output as before. */
expect fun logError(tag: String, message: String, cause: Throwable? = null)

/**
 * Wall-clock milliseconds since the epoch, for the feed's cache-buster query.
 *
 * kotlin.time.Clock would cover this in common code, but it is still an
 * experimental stdlib API - an expect fun avoids the opt-in and the churn when
 * it stabilises.
 */
expect fun epochMillis(): Long
```

- [ ] **Step 7: Write the Android actuals**

Create `shared/src/androidMain/kotlin/com/radionula/radionula/core/util/Platform.android.kt`:

```kotlin
package com.radionula.radionula.core.util

import android.util.Log

actual fun logError(tag: String, message: String, cause: Throwable?) {
    if (cause == null) Log.e(tag, message) else Log.e(tag, message, cause)
}

actual fun epochMillis(): Long = System.currentTimeMillis()
```

- [ ] **Step 8: Run the tests to verify they pass**

Run: `./gradlew :shared:testAndroidHostTest :app:testDebugUnitTest :app:assembleDebug`

Expected: PASS. `:app`'s test count is unchanged from Task 1.

- [ ] **Step 9: Commit**

Ask the user for permission to commit, then:

```bash
git add shared gradle/libs.versions.toml settings.gradle.kts app/build.gradle.kts
git commit -m "build: add the :shared KMP module

Single Android target for now via com.android.kotlin.multiplatform.library.
Adding iOS later means adding the ios* targets and binaries.framework here,
with no module moves.

Carries the two platform functions the shared code will need: logError,
replacing direct android.util.Log calls, and epochMillis for the feed's
cache-buster."
```

---

### Task 3: Move the portable core to `commonMain`

Moves the code that is already platform-agnostic, plus the two tests that need no mocking library. The `PlaylistNetworkDataSource` *interface* must move in this task — `PlaylistRepositoryImpl` will not compile in `commonMain` without it — while its Retrofit-backed implementation stays in `:app` until Task 4.

**Files:**
- Move: `app/src/main/.../domain/model/NulaTrack.kt` → `shared/src/commonMain/kotlin/com/radionula/radionula/domain/model/NulaTrack.kt`
- Move: `app/src/main/.../domain/repository/PlaylistRepository.kt` → `shared/src/commonMain/kotlin/com/radionula/radionula/domain/repository/PlaylistRepository.kt`
- Move: `app/src/main/.../data/repository/PlaylistRepositoryImpl.kt` → `shared/src/commonMain/kotlin/com/radionula/radionula/data/repository/PlaylistRepositoryImpl.kt`
- Move: `app/src/main/.../data/db/entity/CurrentSong.kt` → `shared/src/commonMain/kotlin/com/radionula/radionula/data/db/entity/CurrentSong.kt`
- Move: `app/src/main/.../core/util/channelPresenter.kt` → `shared/src/commonMain/kotlin/com/radionula/radionula/core/util/channelPresenter.kt`
- Move: `app/src/main/.../data/network/PlaylistNetworkDataSource.kt` → `shared/src/commonMain/kotlin/com/radionula/radionula/data/network/PlaylistNetworkDataSource.kt`
- Move: `app/src/test/.../ChannelPresenterTest.kt` → `shared/src/commonTest/kotlin/com/radionula/radionula/ChannelPresenterTest.kt`
- Move: `app/src/test/.../PlaylistRepositoryImplTest.kt` → `shared/src/commonTest/kotlin/com/radionula/radionula/PlaylistRepositoryImplTest.kt`
- Modify: `shared/build.gradle.kts`, `gradle/libs.versions.toml`
- Create: `shared/src/commonMain/kotlin/com/radionula/radionula/core/di/SharedModule.kt`
- Modify: `app/src/main/.../features/player/PlaylistModule.kt`

**Interfaces:**
- Consumes: `:shared` project dependency and `logError`/`epochMillis` from Task 2.
- Produces, all in `commonMain`, all with signatures unchanged from today:
  - `class NulaTrack(val artist: String, val title: String, val image: String, val id: Int = -1)` with `companion object { val EMPTY }`
  - `data class CurrentSong(val artist: String, val cover: String, val title: String)`
  - `class ChannelPresenter` with `enum class Channel(url, xmlPath, displayName, commentsUrl)`, `val currentChannel: Channel`, `fun select(index: Int): Channel`
  - `interface PlaylistRepository` — `currentPlaylist(): Flow<List<NulaTrack>>`, `currentSong(): Flow<CurrentSong>`, `suspend fun fetchCurrentPlaylist()`, `setChannel(Channel)`, `autoFetchPlaylist()`, `clearSession()`
  - `interface PlaylistNetworkDataSource` — `suspend fun fetchPlaylist(channel: Channel): List<NulaTrack>?`
  - `class PlaylistRepositoryImpl(playlistNetworkDataSource, coroutineScope)`
  - `val sharedModule: org.koin.core.module.Module`

- [ ] **Step 1: Add Koin to the catalog and to `commonMain`**

In `gradle/libs.versions.toml`, change the `koin` version and add `koin-core`:

```toml
koin = "4.2.2"
```

```toml
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
```

`koin-android` and `koin-androidx-compose` already share the `koin` ref, so they move to 4.2.2 together. Keeping them on one version is required — mixed Koin versions fail at runtime, not at compile time.

In `shared/build.gradle.kts`, add to `commonMain.dependencies`:

```kotlin
            implementation(libs.koin.core)
```

Only `koin-core` goes into `commonMain`. Koin's `viewModel { }` DSL is **not** used in shared code — every `viewModel { }` binding stays in `:app`, which already has `koin-android`. That avoids pulling `koin-core-viewmodel` into the shared module for no present benefit.

- [ ] **Step 2: Move the six production files with `git mv`**

```bash
cd /Users/silverbaq/Github/android/RadioNula
S=shared/src/commonMain/kotlin/com/radionula/radionula
A=app/src/main/java/com/radionula/radionula
mkdir -p $S/domain/model $S/domain/repository $S/data/repository $S/data/db/entity $S/core/util $S/data/network
git mv $A/domain/model/NulaTrack.kt              $S/domain/model/
git mv $A/domain/repository/PlaylistRepository.kt $S/domain/repository/
git mv $A/data/repository/PlaylistRepositoryImpl.kt $S/data/repository/
git mv $A/data/db/entity/CurrentSong.kt          $S/data/db/entity/
git mv $A/core/util/channelPresenter.kt          $S/core/util/
git mv $A/data/network/PlaylistNetworkDataSource.kt $S/data/network/
```

Do not edit the contents of any of these six files. Their packages already match, and every symbol they reference either moved with them or is `kotlinx.coroutines`, which `commonMain` now has.

- [ ] **Step 3: Move the two tests and convert them to `kotlin.test`**

```bash
mkdir -p shared/src/commonTest/kotlin/com/radionula/radionula
git mv app/src/test/java/com/radionula/radionula/ChannelPresenterTest.kt shared/src/commonTest/kotlin/com/radionula/radionula/
git mv app/src/test/java/com/radionula/radionula/PlaylistRepositoryImplTest.kt shared/src/commonTest/kotlin/com/radionula/radionula/
```

In `ChannelPresenterTest.kt`, replace the import block and drop the runner. The class body and every assertion stay exactly as they are:

```kotlin
package com.radionula.radionula

import com.radionula.radionula.core.util.ChannelPresenter
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ChannelPresenterTest {

    lateinit var channelPresenter : ChannelPresenter

    @BeforeTest
    fun before() {
        channelPresenter= ChannelPresenter()
    }
```

`@RunWith(JUnit4::class)` and `@Before` are removed; `@BeforeTest` replaces the latter. The six `@Test` methods keep their bodies verbatim, including the bare `assert(...)` calls, which are Kotlin stdlib and work in common code.

- [ ] **Step 4: Rewrite `PlaylistRepositoryImplTest` against a fake**

Replace the whole file with the version below. It mocks exactly one interface, so a fake is smaller than the mocking library it replaces. Backtick method names become underscores: backticked names with spaces do not compile on Kotlin/Native, and this file is meant to keep working when an iOS target is added.

**Every assertion is carried over unchanged.** Only the stubbing mechanism differs.

```kotlin
package com.radionula.radionula

import com.radionula.radionula.core.util.ChannelPresenter
import com.radionula.radionula.data.network.PlaylistNetworkDataSource
import com.radionula.radionula.data.repository.PlaylistRepositoryImpl
import com.radionula.radionula.domain.model.NulaTrack
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistRepositoryImplTest {

    /**
     * Replaces the Mockito mock. Only one method to stub, and the tests only
     * ever need "what does the next fetch return", so a mutable property is
     * the whole fake.
     */
    private class FakeNetworkDataSource : PlaylistNetworkDataSource {
        var nextResult: List<NulaTrack>? = null

        override suspend fun fetchPlaylist(channel: ChannelPresenter.Channel): List<NulaTrack>? =
            nextResult
    }

    private val dataSource = FakeNetworkDataSource()

    private fun repository(scope: TestScope) = PlaylistRepositoryImpl(dataSource, scope)

    private fun feed(vararg titles: String) = titles.map { NulaTrack("Artist", it, "cover-$it") }

    @Test
    fun the_playlist_is_only_what_this_session_heard_not_the_feed_history() = runTest {
        // The feed carries the current track plus ten played before the app opened.
        dataSource.nextResult = feed("Current", "Older", "Oldest")
        val repository = repository(this)

        repository.fetchCurrentPlaylist()

        val playlist = repository.currentPlaylist().first()
        assertEquals(listOf("Current"), playlist.map { it.title })
    }

    @Test
    fun a_new_track_is_prepended_to_the_session_history() = runTest {
        val repository = repository(this)
        dataSource.nextResult = feed("First")
        repository.fetchCurrentPlaylist()

        dataSource.nextResult = feed("Second")
        repository.fetchCurrentPlaylist()

        assertEquals(listOf("Second", "First"), repository.currentPlaylist().first().map { it.title })
    }

    @Test
    fun the_same_track_fetched_twice_is_not_repeated() = runTest {
        val repository = repository(this)
        dataSource.nextResult = feed("Same")

        repository.fetchCurrentPlaylist()
        repository.fetchCurrentPlaylist()

        assertEquals(listOf("Same"), repository.currentPlaylist().first().map { it.title })
    }

    @Test
    fun clearSession_leaves_a_new_subscriber_with_nothing_replayed() = runTest {
        val repository = repository(this)
        dataSource.nextResult = feed("Heard last time")
        repository.fetchCurrentPlaylist()

        repository.clearSession()

        // This is the reopened-app case: the repository outlives the activity, so
        // a fresh ViewModel must not be handed the previous session's replay.
        assertNull(withTimeoutOrNull(100) { repository.currentPlaylist().first() })
        assertNull(withTimeoutOrNull(100) { repository.currentSong().first() })
    }

    @Test
    fun history_restarts_from_empty_after_clearSession() = runTest {
        val repository = repository(this)
        dataSource.nextResult = feed("Heard last time")
        repository.fetchCurrentPlaylist()
        repository.clearSession()

        dataSource.nextResult = feed("Heard this time")
        repository.fetchCurrentPlaylist()

        assertEquals(
            listOf("Heard this time"),
            repository.currentPlaylist().first().map { it.title }
        )
    }

    @Test
    fun clearSession_stops_the_polling_loop() = runTest {
        val repository = repository(this)
        dataSource.nextResult = feed("Playing")

        repository.autoFetchPlaylist()
        repository.clearSession()

        // A live poll loop would keep this scope busy and runTest would never finish.
    }

    @Test
    fun a_failed_fetch_leaves_the_session_untouched() = runTest {
        val repository = repository(this)
        dataSource.nextResult = feed("Playing")
        repository.fetchCurrentPlaylist()

        dataSource.nextResult = null
        repository.fetchCurrentPlaylist()

        assertEquals(listOf("Playing"), repository.currentPlaylist().first().map { it.title })
    }
}
```

- [ ] **Step 5: Run the shared tests to verify they pass**

Run: `./gradlew :shared:testAndroidHostTest`

Expected: PASS, 14 tests (6 from `ChannelPresenterTest`, 7 from `PlaylistRepositoryImplTest`, 1 from `PlatformTest`).

- [ ] **Step 6: Create the shared Koin module**

Create `shared/src/commonMain/kotlin/com/radionula/radionula/core/di/SharedModule.kt`:

```kotlin
package com.radionula.radionula.core.di

import com.radionula.radionula.core.util.ChannelPresenter
import com.radionula.radionula.data.repository.PlaylistRepositoryImpl
import com.radionula.radionula.domain.repository.PlaylistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Everything the shared module can wire up on its own.
 *
 * ViewModel bindings stay in :app: Koin's viewModel { } DSL would pull
 * koin-core-viewmodel into commonMain, and there is no second platform
 * consuming it yet.
 */
val sharedModule = module {
    factory<CoroutineScope>(named("default")) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    factory<CoroutineScope>(named("main")) { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
    factory<CoroutineScope>(named("ioScope")) { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

    single { ChannelPresenter() }
    single<PlaylistRepository> {
        PlaylistRepositoryImpl(
            playlistNetworkDataSource = get(),
            coroutineScope = get(named("ioScope")),
        )
    }
}
```

`Dispatchers.IO` is available in `commonMain` as of kotlinx-coroutines 1.9. If the compiler rejects it, the project's coroutines version is older than the catalog claims — check `libs.versions.toml` rather than working around it.

- [ ] **Step 7: Remove the moved definitions from `:app`'s Koin modules**

Delete `app/src/main/java/com/radionula/radionula/core/di/AppModule.kt` — all three `CoroutineScope` factories now live in `sharedModule`.

In `app/src/main/java/com/radionula/radionula/features/player/PlaylistModule.kt`, delete the `single { ChannelPresenter() }` and `single<PlaylistRepository> { ... }` definitions and the now-unused imports, leaving:

```kotlin
package com.radionula.radionula.features.player

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import com.radionula.radionula.data.PlaylistApiService
import com.radionula.radionula.data.network.ConnectivityInterceptorImpl
import com.radionula.radionula.data.network.PlaylistNetworkDataSource
import com.radionula.radionula.data.network.PlaylistNetworkDataSourceImpl

val playlistModule = module {
    single {
        ConnectivityInterceptorImpl(
            androidContext()
        )
    }
    single { PlaylistApiService(get()) }
    single<PlaylistNetworkDataSource> {
        PlaylistNetworkDataSourceImpl(
            get()
        )
    }
    viewModel { RadioViewModel(get(), get(), get(), get()) }
}
```

In `app/src/main/java/com/radionula/radionula/MyApp.kt`, swap `appModule` for `sharedModule`:

```kotlin
import com.radionula.radionula.core.di.sharedModule
```

```kotlin
            modules(sharedModule, playlistModule, radioPlayerModule, databaseModule)
```

- [ ] **Step 8: Run everything and install**

Run: `./gradlew :shared:testAndroidHostTest :app:testDebugUnitTest :app:installDebug`

Expected: PASS. `:app`'s unit tests drop by 13 (the two moved test classes) and the remainder still pass.

On the device: open the app, tune in, confirm audio plays, the track title updates and the playlist fills. A Koin misconfiguration from Step 7 crashes on launch, so a successful launch is the check that DI is still complete.

- [ ] **Step 9: Commit**

Ask the user for permission to commit, then:

```bash
git add -A shared app gradle/libs.versions.toml
git commit -m "refactor: move the portable core to commonMain

Models, ChannelPresenter, the playlist repository and the network data source
interface are already platform-agnostic, so they move unmodified - the Kotlin
package is identical in both modules, so no imports change.

The data source *interface* has to move with the repository or the repository
will not compile in commonMain. Its Retrofit implementation stays in :app
until the Ktor swap.

PlaylistRepositoryImplTest swaps Mockito for a six-line fake and loses its
backtick method names, which do not compile on Kotlin/Native. Assertions are
unchanged. Koin goes to 4.2.2 across all three artifacts at once."
```

---

### Task 4: Replace Retrofit with Ktor

Moves the whole network call into `commonMain` and deletes the connectivity interceptor. Offline behaviour is preserved by the existing catch-all in `PlaylistNetworkDataSourceImpl`, which is what the airplane-mode check at the end verifies.

**Files:**
- Move + rewrite: `app/src/main/.../data/PlaylistApiService.kt` → `shared/src/commonMain/kotlin/com/radionula/radionula/data/PlaylistApiService.kt`
- Move + rewrite: `app/src/main/.../data/network/PlaylistNetworkDataSourceImpl.kt` → `shared/src/commonMain/kotlin/com/radionula/radionula/data/network/PlaylistNetworkDataSourceImpl.kt`
- Delete: `app/src/main/.../data/network/ConnectivityInterceptor.kt`, `app/src/main/.../data/network/ConnectivityInterceptorImpl.kt`, `app/src/main/.../core/exceptions/Exceptions.kt`
- Modify: `shared/build.gradle.kts`, `gradle/libs.versions.toml`, `shared/.../core/di/SharedModule.kt`, `app/src/main/.../features/player/PlaylistModule.kt`, `app/build.gradle.kts`

**Interfaces:**
- Consumes: `PlaylistNetworkDataSource`, `ChannelPresenter`, `NulaTrack`, `logError`, `epochMillis`, `sharedModule` from Task 3. `RecentlyPlayedParser` still lives in `:app` at this point and **cannot be called from `commonMain`** — `:app` already depends on `:shared`, so the reverse dependency would be circular. The parse function is therefore injected: `typealias PlaylistFeedParser = (xml: String) -> List<NulaTrack>` declared in `commonMain`, bound in `:app` to `RecentlyPlayedParser::parse`. This adds one Koin definition (16, up from 15) and makes `PlaylistNetworkDataSourceImpl` take two constructor parameters. **Task 5 removes all of it.**
- Produces:
  - `class PlaylistApiService(private val client: HttpClient)` with `suspend fun getPlaylist(xmlPath: String, cacheBuster: Long): String`
  - `fun nulaHttpClient(): HttpClient`
  - `class PlaylistNetworkDataSourceImpl(apiPlaylistApiService: PlaylistApiService)` in `commonMain`

- [ ] **Step 1: Add Ktor to the catalog and the module**

In `gradle/libs.versions.toml`, add to `[versions]`:

```toml
ktor = "3.5.2"
```

Add to `[libraries]`:

```toml
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
```

In `shared/build.gradle.kts`, add to `commonMain.dependencies`:

```kotlin
            implementation(libs.ktor.client.core)
```

and add a new `androidMain` block inside `sourceSets`, after `commonTest`:

```kotlin
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
```

The engine is not named in code. `HttpClient { }` with no engine argument resolves the single engine on the classpath through `ServiceLoader`, so a future `iosMain` only needs `ktor-client-darwin` added here.

- [ ] **Step 2: Rewrite `PlaylistApiService` on Ktor**

```bash
git mv app/src/main/java/com/radionula/radionula/data/PlaylistApiService.kt \
       shared/src/commonMain/kotlin/com/radionula/radionula/data/PlaylistApiService.kt
```

Replace its contents with:

```kotlin
package com.radionula.radionula.data

import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders

class PlaylistApiService(private val client: HttpClient) {

    /**
     * Fetches a "recently played" RSS feed, e.g. "recently_played_ch4.xml".
     * [cacheBuster] mirrors what radionula.com itself sends - the feeds are
     * static files and get cached aggressively otherwise.
     */
    suspend fun getPlaylist(xmlPath: String, cacheBuster: Long): String =
        client.get(xmlPath) {
            header(HttpHeaders.CacheControl, "no-store")
            parameter("t", cacheBuster)
        }.bodyAsText()
}

private const val BASE_URL = "https://radionula.com/"

fun nulaHttpClient(): HttpClient = HttpClient {
    // Retrofit already threw HttpException on a non-2xx when the body was
    // returned directly, and the data source's catch-all handled it. This keeps
    // that behaviour explicit rather than relying on the engine's default.
    expectSuccess = true
    defaultRequest { url(BASE_URL) }
}
```

The Retrofit `companion object invoke` factory is gone: the client is now a Koin singleton, and the service is a plain class rather than an interface with a generated implementation.

- [ ] **Step 3: Move and rewrite `PlaylistNetworkDataSourceImpl`**

```bash
git mv app/src/main/java/com/radionula/radionula/data/network/PlaylistNetworkDataSourceImpl.kt \
       shared/src/commonMain/kotlin/com/radionula/radionula/data/network/PlaylistNetworkDataSourceImpl.kt
```

Replace its contents with:

```kotlin
package com.radionula.radionula.data.network

import com.radionula.radionula.core.util.ChannelPresenter
import com.radionula.radionula.core.util.epochMillis
import com.radionula.radionula.core.util.logError
import com.radionula.radionula.data.PlaylistApiService
import com.radionula.radionula.domain.model.NulaTrack

class PlaylistNetworkDataSourceImpl(
        private val apiPlaylistApiService: PlaylistApiService
) : PlaylistNetworkDataSource {

    override suspend fun fetchPlaylist(channel: ChannelPresenter.Channel): List<NulaTrack>? {
        // Returns null on *any* failure, deliberately. The repository's
        // early-return on null is what keeps a failed poll from clearing the
        // session or publishing a stale track, and being offline is just one
        // more failure now that the connectivity interceptor is gone.
        try {
            val xml = apiPlaylistApiService.getPlaylist(channel.xmlPath, epochMillis())
            return RecentlyPlayedParser.parse(xml)
        } catch (e: Exception) {
            logError("Playlist", "Could not read ${channel.xmlPath}", e)
        }
        return null
    }
}
```

`System.currentTimeMillis()` becomes `epochMillis()`, and `android.util.Log.e` becomes `logError`. The `NoConnectivityException` catch arm is gone along with the exception.

- [ ] **Step 4: Delete the interceptor trio**

```bash
git rm app/src/main/java/com/radionula/radionula/data/network/ConnectivityInterceptor.kt \
       app/src/main/java/com/radionula/radionula/data/network/ConnectivityInterceptorImpl.kt \
       app/src/main/java/com/radionula/radionula/core/exceptions/Exceptions.kt
```

Verify nothing else referenced them:

```bash
grep -rn "ConnectivityInterceptor\|NoConnectivityException" app/src shared/src
```

Expected: no output. `ConnectivityLiveData` and `NoConnectionOverlay` must **not** appear — they are the separate mechanism that drives the "no internet" screen, and they are untouched.

- [ ] **Step 5: Move the network wiring into `sharedModule`**

In `shared/.../core/di/SharedModule.kt`, add the imports:

```kotlin
import com.radionula.radionula.data.PlaylistApiService
import com.radionula.radionula.data.network.PlaylistNetworkDataSource
import com.radionula.radionula.data.network.PlaylistNetworkDataSourceImpl
import com.radionula.radionula.data.nulaHttpClient
```

and the definitions, above the existing `single { ChannelPresenter() }`:

```kotlin
    single { nulaHttpClient() }
    single { PlaylistApiService(get()) }
    single<PlaylistNetworkDataSource> { PlaylistNetworkDataSourceImpl(get()) }
```

In `app/.../features/player/PlaylistModule.kt`, remove all three network definitions and their imports, leaving only:

```kotlin
package com.radionula.radionula.features.player

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val playlistModule = module {
    viewModel { RadioViewModel(get(), get(), get(), get()) }
}
```

- [ ] **Step 6: Drop Retrofit from `:app`**

In `app/build.gradle.kts`, delete `implementation(libs.retrofit)`. In `gradle/libs.versions.toml`, delete the `retrofit` version and the `retrofit` library entry.

- [ ] **Step 7: Build and run the tests**

Run: `./gradlew :shared:testAndroidHostTest :app:testDebugUnitTest :app:installDebug`

Expected: PASS, same counts as Task 3.

- [ ] **Step 8: Verify online behaviour on the device**

Tune in and switch through all three channels. Confirm the track title and cover art update on each, and that the notification shows the track. A Ktor URL-resolution mistake shows up here as a permanently empty playlist while audio still plays, because the stream URL is absolute and never went through Ktor.

- [ ] **Step 9: Verify offline behaviour on the device**

With the app open and playing, enable airplane mode. Confirm all four:

1. The "no internet" overlay appears. This is `ConnectivityLiveData`, and it must be unaffected.
2. The app does not crash, and logcat shows `Could not read recently_played_chN.xml` rather than an uncaught exception.
3. The playlist keeps its existing entries — no stale track is appended and nothing is cleared.
4. Disable airplane mode. Within 30 seconds the playlist resumes updating, without restarting the app.

Any failure here means `PlaylistNetworkDataSourceImpl` is propagating instead of returning `null`. Fix that, not the repository.

- [ ] **Step 10: Commit**

Ask the user for permission to commit, then:

```bash
git add -A shared app gradle/libs.versions.toml
git commit -m "refactor: replace Retrofit with Ktor

The whole fetch path is commonMain now. The engine is never named in code:
HttpClient { } resolves the single engine on the classpath, so adding iOS
later means adding ktor-client-darwin to androidMain's sibling source set.

Deletes ConnectivityInterceptor, its impl and NoConnectivityException. They
turned offline into a distinct exception before a socket was opened; Ktor
throws on its own and the data source already catches everything and returns
null, which is what keeps the poll loop and UI state intact. The 'no
internet' overlay runs off ConnectivityLiveData and is untouched.

expectSuccess = true so a non-2xx fails at the call instead of arriving as an
error body that fails to parse."
```

---

### Task 5: Replace the DOM parser with xmlutil

**Files:**
- Move + rewrite: `app/src/main/.../data/network/RecentlyPlayedParser.kt` → `shared/src/commonMain/kotlin/com/radionula/radionula/data/network/RecentlyPlayedParser.kt`
- Move: `app/src/test/.../RecentlyPlayedParserTest.kt` → `shared/src/commonTest/kotlin/com/radionula/radionula/RecentlyPlayedParserTest.kt`
- Modify: `shared/build.gradle.kts`, `gradle/libs.versions.toml`

**Interfaces:**
- Consumes: `NulaTrack` from Task 3, and the `PlaylistFeedParser` seam Task 4 introduced.
- Produces: `object RecentlyPlayedParser` with `fun parse(xml: String): List<NulaTrack>` — same signature and same behaviour as the DOM version it replaces — now in `commonMain`.

**This task also dismantles Task 4's temporary parser seam.** Task 4 could not call `RecentlyPlayedParser` from `commonMain` (the parser was in `:app`, and `:shared` cannot depend back on `:app`), so it injected the parse function. Once the parser is in `commonMain` that indirection has no reason to exist, and leaving it behind would be a permanent abstraction created solely by a transient constraint. Step 6 removes it.

- [ ] **Step 1: Add xmlutil to the catalog and the module**

In `gradle/libs.versions.toml`, add to `[versions]`:

```toml
xmlutil = "0.91.3"
```

Add to `[libraries]`:

```toml
xmlutil-core = { module = "io.github.pdvrieze.xmlutil:core", version.ref = "xmlutil" }
```

In `shared/build.gradle.kts`, add to `commonMain.dependencies`:

```kotlin
            implementation(libs.xmlutil.core)
```

Only `core` — the serialization artifact is not needed. (`core` itself declares `kotlinx-serialization-core` on the *runtime* classpath at every version, as does `ktor-client-core`; the compile classpath stays clean, and the constraint is about usage and declared artifacts.)

- [ ] **Step 2: Move the test to `commonTest` and convert it**

```bash
git mv app/src/test/java/com/radionula/radionula/RecentlyPlayedParserTest.kt \
       shared/src/commonTest/kotlin/com/radionula/radionula/RecentlyPlayedParserTest.kt
```

Change only the imports and the method names. The `feed` fixture, the `itemFeed` helper and **every assertion stay exactly as they are**:

```kotlin
package com.radionula.radionula

import com.radionula.radionula.data.network.RecentlyPlayedParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
```

Rename the five backticked methods, keeping their bodies and comments verbatim:

| From | To |
| --- | --- |
| `` `current track is first, artist and title split on the separator` `` | `current_track_is_first_artist_and_title_split_on_the_separator` |
| `` `only the first separator splits, so titles keep their own dashes` `` | `only_the_first_separator_splits_so_titles_keep_their_own_dashes` |
| `` `missing cover and missing separator do not drop the track` `` | `missing_cover_and_missing_separator_do_not_drop_the_track` |
| `` `cdata markers are stripped` `` | `cdata_markers_are_stripped` |
| `` `items without a title are skipped` `` | `items_without_a_title_are_skipped` |

- [ ] **Step 3: Run the test to verify it fails**

Run: `./gradlew :shared:testAndroidHostTest --tests "*RecentlyPlayedParserTest*"`

Expected: FAIL with "Unresolved reference: RecentlyPlayedParser" — the parser is still in `:app`, which `:shared` cannot see.

- [ ] **Step 4: Write the xmlutil parser**

```bash
git mv app/src/main/java/com/radionula/radionula/data/network/RecentlyPlayedParser.kt \
       shared/src/commonMain/kotlin/com/radionula/radionula/data/network/RecentlyPlayedParser.kt
```

Replace its contents with:

```kotlin
package com.radionula.radionula.data.network

import com.radionula.radionula.domain.model.NulaTrack
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.XmlStreaming

/**
 * Parses the RSS 0.92 feeds at https://radionula.com/recently_played_chN.xml.
 *
 * The first <item> is the currently playing track, the rest is history, newest
 * first. <title> holds "Artist - Title" and the cover art sits in <image><url>.
 */
object RecentlyPlayedParser {

    fun parse(xml: String): List<NulaTrack> {
        // The *generic* reader, not the platform one: the feed is remote, and
        // the generic reader does not resolve external entities. This is what
        // isExpandEntityReferences = false did on the DOM parser.
        val reader = XmlStreaming.newGenericReader(xml)
        val tracks = mutableListOf<NulaTrack>()

        while (reader.hasNext()) {
            if (reader.next() == EventType.START_ELEMENT && reader.localName == "item") {
                reader.readItem()?.let(tracks::add)
            }
        }
        return tracks
    }

    /**
     * Reads to the end of the <item> the reader is positioned on.
     *
     * Scoping matters: the feed has a <title> on <channel> too, and only the
     * ones inside an item are tracks.
     */
    private fun XmlReader.readItem(): NulaTrack? {
        var title: String? = null
        var cover: String? = null
        var inImage = false

        while (hasNext()) {
            when (next()) {
                EventType.START_ELEMENT -> when (localName) {
                    "image" -> inImage = true
                    "title" -> title = elementText()
                    // First image's url only, matching the DOM version's item(0).
                    "url" -> if (inImage && cover == null) cover = elementText()
                }

                EventType.END_ELEMENT -> when (localName) {
                    "image" -> inImage = false
                    "item" -> return toTrack(title, cover)
                }

                else -> Unit
            }
        }
        return toTrack(title, cover)
    }

    /** Concatenates the text of the element the reader is positioned on. */
    private fun XmlReader.elementText(): String {
        val text = StringBuilder()
        while (hasNext()) {
            when (next()) {
                EventType.TEXT, EventType.CDSECT, EventType.ENTITY_REF -> text.append(this.text)
                EventType.END_ELEMENT -> return text.toString().trim()
                else -> Unit
            }
        }
        return text.toString().trim()
    }

    private fun toTrack(rawTitle: String?, cover: String?): NulaTrack? {
        val title = rawTitle?.stripCdata()?.takeIf { it.isNotEmpty() } ?: return null
        val parts = title.split(" - ", limit = 2)
        return NulaTrack(
            artist = parts[0].trim(),
            title = parts.getOrElse(1) { "" }.trim(),
            image = cover.orEmpty(),
        )
    }

    /** Some feed generators escape the CDATA markers instead of emitting real CDATA. */
    private fun String.stripCdata(): String =
            replace("<![CDATA[", "").replace("]]>", "").trim()
}
```

Two things to check if this does not compile or a test fails:

- **Accessor name:** on 0.91.x it is `XmlStreaming` (capital X); the lowercase `xmlStreaming` belongs to the 1.0 line, which this project cannot use. Do not switch to a convenience `parse`-from-string helper — those use the platform reader and lose the external-entity protection.
- **`cdata_markers_are_stripped` fails:** the fixture uses `&lt;![CDATA[...]]&gt;`, so the markers arrive as escaped text. If `elementText()` returns an empty or partial string, the generic reader is emitting `ENTITY_REF` events whose `text` is the entity name rather than its replacement. Log the event sequence for that fixture before changing the accumulator.

- [ ] **Step 5: Run the parser tests to verify they pass**

Run: `./gradlew :shared:testAndroidHostTest --tests "*RecentlyPlayedParserTest*"`

Expected: PASS, 5 tests.

- [ ] **Step 6: Remove Task 4's parser seam**

The parser is in `commonMain` now, so the injected parse function is dead weight. Remove all three parts:

1. In `shared/src/commonMain/.../data/network/PlaylistNetworkDataSource.kt`, delete the `PlaylistFeedParser` typealias and its doc comment. The file goes back to holding only the `PlaylistNetworkDataSource` interface.
2. In `shared/src/commonMain/.../data/network/PlaylistNetworkDataSourceImpl.kt`, drop the `parse` constructor parameter and call `RecentlyPlayedParser.parse(xml)` directly:

```kotlin
class PlaylistNetworkDataSourceImpl(
        private val apiPlaylistApiService: PlaylistApiService
) : PlaylistNetworkDataSource {

    override suspend fun fetchPlaylist(channel: ChannelPresenter.Channel): List<NulaTrack>? {
        // Returns null on *any* failure, deliberately. The repository's
        // early-return on null is what keeps a failed poll from clearing the
        // session or publishing a stale track, and being offline is just one
        // more failure now that the connectivity interceptor is gone.
        try {
            val xml = apiPlaylistApiService.getPlaylist(channel.xmlPath, epochMillis())
            return RecentlyPlayedParser.parse(xml)
        } catch (e: Exception) {
            logError("Playlist", "Could not read ${channel.xmlPath}", e)
        }
        return null
    }
}
```

3. In `app/src/main/.../features/player/PlaylistModule.kt`, delete the `single<PlaylistFeedParser> { ... }` definition, its comment, and both now-unused imports. The module goes back to just the `viewModel { RadioViewModel(...) }` binding.
4. In `sharedModule`, `single<PlaylistNetworkDataSource> { PlaylistNetworkDataSourceImpl(get()) }` now takes a single `get()`.

Afterwards `grep -rn "PlaylistFeedParser" app/src shared/src` must return nothing, and the Koin count returns to **15**.

- [ ] **Step 7: Run everything and install**

Run: `./gradlew :shared:testAndroidHostTest :app:testDebugUnitTest :app:installDebug`

Expected: PASS. `:shared` is now 19 tests; `:app`'s drops by 5. Koin reports `Started 15 definitions` — if it still says 16, the seam was not fully removed.

On the device: tune in and confirm the artist, title and cover art are correct on all three channels. The unit tests use a trimmed fixture, so this is the check against the real feed.

- [ ] **Step 8: Commit**

Ask the user for permission to commit, then:

```bash
git add -A shared app gradle/libs.versions.toml
git commit -m "refactor: parse the feed with xmlutil instead of javax.xml

Streaming pull reader over the RSS, scoped so the channel-level <title> is
not mistaken for a track. Only the xmlutil core artifact: the serialization
one would pull in kotlinx-serialization for no gain.

Uses newGenericReader rather than the platform reader. The feed is remote and
the generic reader does not resolve external entities, which is what
isExpandEntityReferences = false bought on the DOM parser.

RecentlyPlayedParserTest moves to commonTest with its fixtures and assertions
unchanged."
```

---

### Task 6: Replace `SQLiteOpenHelper` with the multiplatform SQLite driver

The one task that can destroy user data.

Room was the original choice here and is **not usable**. Verified in-repo: KSP `2.2.10-2.0.2` crashes against `com.android.kotlin.multiplatform.library` with `KotlinMultiplatformAndroidCompilationImpl cannot be cast to KotlinJvmAndroidCompilation`; the legacy `com.android.library` + `androidTarget()` combination is rejected outright by AGP 9 (*"not compatible with the org.jetbrains.kotlin.multiplatform plugin since AGP 9.0"*); and AGP 9.4.0-alpha07 embeds the same KGP 2.2.10, so no newer KSP is reachable. No KSP means no Room.

`androidx.sqlite` provides the driver Room would have sat on, with no code generation. That turns the risky part of this task into a non-event: without Room's `TableInfo` validation there is **no identity hash, no schema check, and no migration**. The existing `NulaTracks` table is used exactly as `MyDatabaseHelper` wrote it.

**Files:**
- Move + rewrite: `app/src/main/.../data/db/NulaDatabase.kt` → `shared/src/commonMain/kotlin/com/radionula/radionula/data/db/NulaDatabase.kt`
- Create: `app/src/androidTest/java/com/radionula/radionula/LegacyFavoritesMigrationTest.kt`
- Delete: `app/src/main/.../core/util/MyDatabaseHelper.kt`
- Modify: `shared/build.gradle.kts`, `gradle/libs.versions.toml`, `shared/.../core/di/SharedModule.kt`, `app/src/main/.../data/db/DatabaseModule.kt`

**Interfaces:**
- Consumes: `NulaTrack` and `sharedModule` from Task 3.
- Produces:
  - `class NulaDatabase(private val driver: SQLiteDriver, private val databasePath: String)` keeping today's exact public surface: `suspend fun insertTrack(track: NulaTrack): Long`, `suspend fun selectAllTracks(): List<NulaTrack>`, `suspend fun removeTrack(track: NulaTrack): Int`. `RadioViewModel` and `FavoritesViewModel` are **not** touched by this task.
  - a Koin `single<SQLiteDriver>` in `sharedModule`, and a `single<String>(named("databasePath"))` supplied from `:app` because it needs `Context`

- [ ] **Step 1: Add the sqlite driver**

In `gradle/libs.versions.toml`, add to `[versions]`:

```toml
androidxSqlite = "2.7.0"
```

Add to `[libraries]`:

```toml
androidx-sqlite-bundled = { module = "androidx.sqlite:sqlite-bundled", version.ref = "androidxSqlite" }
```

In `shared/build.gradle.kts`, add to `commonMain.dependencies`:

```kotlin
            // api, not implementation: SQLiteDriver is a constructor parameter of
            // NulaDatabase, which :app instantiates through Koin.
            api(libs.androidx.sqlite.bundled)
```

Do **not** add the KSP or Room plugins to this module. See the note at the top of this task.

- [ ] **Step 2: Rewrite `NulaDatabase` over the driver**

```bash
git mv app/src/main/java/com/radionula/radionula/data/db/NulaDatabase.kt \
       shared/src/commonMain/kotlin/com/radionula/radionula/data/db/NulaDatabase.kt
```

Replace its contents with:

```kotlin
package com.radionula.radionula.data.db

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.execSQL
import com.radionula.radionula.domain.model.NulaTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The favourites store.
 *
 * Same three suspend functions the SQLiteOpenHelper version exposed, so the
 * ViewModels are unchanged - and deliberately the same table, with the same
 * DDL, so a database written by the old build is picked up as-is. There is no
 * migration and no schema version to bump.
 *
 * ponytail: a connection is opened per operation rather than held open. These
 * are three user-triggered queries against one small table. If the favourites
 * list ever grows enough for that to show, hold a single connection behind a
 * Mutex rather than reaching for a connection pool.
 */
class NulaDatabase(
    private val driver: SQLiteDriver,
    private val databasePath: String,
) {

    suspend fun insertTrack(track: NulaTrack): Long = withConnection { connection ->
        connection
            .prepare("INSERT INTO $TABLE ($COLUMN_ARTIST, $COLUMN_TITLE, $COLUMN_IMAGE) VALUES (?, ?, ?)")
            .use { statement ->
                statement.bindText(1, track.artist)
                statement.bindText(2, track.title)
                statement.bindText(3, track.image)
                statement.step()
            }
        // The old ContentValues insert returned the new row id, and
        // FavoritesViewModel's contract still says Long.
        connection.prepare("SELECT last_insert_rowid()").use { statement ->
            if (statement.step()) statement.getLong(0) else -1L
        }
    }

    suspend fun selectAllTracks(): List<NulaTrack> = withConnection { connection ->
        connection
            .prepare("SELECT $COLUMN_ID, $COLUMN_ARTIST, $COLUMN_TITLE, $COLUMN_IMAGE FROM $TABLE")
            .use { statement ->
                buildList {
                    while (statement.step()) {
                        add(
                            NulaTrack(
                                artist = statement.getText(1),
                                title = statement.getText(2),
                                image = statement.getText(3),
                                id = statement.getInt(0),
                            )
                        )
                    }
                }
            }
    }

    suspend fun removeTrack(track: NulaTrack): Int = withConnection { connection ->
        connection.prepare("DELETE FROM $TABLE WHERE $COLUMN_ID = ?").use { statement ->
            statement.bindInt(1, track.id)
            statement.step()
        }
        connection.prepare("SELECT changes()").use { statement ->
            if (statement.step()) statement.getInt(0) else 0
        }
    }

    /**
     * CREATE TABLE IF NOT EXISTS on every open, which is exactly what
     * MyDatabaseHelper's onCreate and onUpgrade both did. Idempotent, so a
     * database from the old build needs nothing done to it, and a fresh install
     * gets the same schema it always had.
     */
    private suspend fun <T> withConnection(block: (SQLiteConnection) -> T): T =
        withContext(Dispatchers.IO) {
            val connection = driver.open(databasePath)
            try {
                connection.execSQL(CREATE_TRACKS)
                block(connection)
            } finally {
                connection.close()
            }
        }

    private companion object {
        const val TABLE = "NulaTracks"
        const val COLUMN_ID = "_id"
        const val COLUMN_ARTIST = "artist"
        const val COLUMN_TITLE = "title"
        const val COLUMN_IMAGE = "image"

        // Byte-for-byte the DDL MyDatabaseHelper used. Favourites are user data;
        // changing this column list strands them.
        const val CREATE_TRACKS = """
            CREATE TABLE IF NOT EXISTS NulaTracks (
                _id INTEGER PRIMARY KEY AUTOINCREMENT,
                artist TEXT NOT NULL,
                title TEXT NOT NULL,
                image TEXT NOT NULL
            )
        """
    }
}
```

Then delete the helper:

```bash
git rm app/src/main/java/com/radionula/radionula/core/util/MyDatabaseHelper.kt
```

- [ ] **Step 3: Wire it up in Koin**

In `shared/.../core/di/SharedModule.kt`, add the imports:

```kotlin
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.radionula.radionula.data.db.NulaDatabase
import org.koin.core.qualifier.named
```

(`named` may already be imported for the CoroutineScope qualifiers — do not duplicate it.)

Add the definitions:

```kotlin
    single<SQLiteDriver> { BundledSQLiteDriver() }
    // The database path comes from :app - it needs a Context.
    single { NulaDatabase(get(), get(named("databasePath"))) }
```

Replace `app/src/main/java/com/radionula/radionula/data/db/DatabaseModule.kt` with:

```kotlin
package com.radionula.radionula.data.db

import com.radionula.radionula.features.favorites.FavoritesViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val databaseModule = module {
    // getDatabasePath("NulaDB") is the exact file SQLiteOpenHelper used, which
    // is what makes an existing install's favourites just appear.
    single(named("databasePath")) {
        androidContext().getDatabasePath("NulaDB").absolutePath
    }
    viewModel { FavoritesViewModel(get()) }
}
```

The `single { NulaDatabase(androidContext()) }` that used to live here is gone — `NulaDatabase` is built in `sharedModule` now.

- [ ] **Step 4: Write the failing legacy-database test**

Create `app/src/androidTest/java/com/radionula/radionula/LegacyFavoritesMigrationTest.kt`:

```kotlin
package com.radionula.radionula

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.radionula.radionula.data.db.NulaDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Favourites are user data. This is the gate on the SQLiteOpenHelper ->
 * androidx.sqlite swap: it recreates the exact database an installed 2.3.0 has,
 * opens it with the new code, and checks the rows came through untouched.
 */
@RunWith(AndroidJUnit4::class)
class LegacyFavoritesMigrationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var dbFile: File

    @Before
    fun createLegacyDatabase() {
        dbFile = context.getDatabasePath("MigrationTestNulaDB")
        dbFile.parentFile?.mkdirs()
        dbFile.delete()
        File("${dbFile.path}-wal").delete()
        File("${dbFile.path}-shm").delete()

        // Byte-for-byte what MyDatabaseHelper wrote, including user_version = 1
        // and the absence of an explicit NOT NULL on _id.
        SQLiteDatabase.openOrCreateDatabase(dbFile, null).use { legacy ->
            legacy.execSQL(
                """
                CREATE TABLE IF NOT EXISTS NulaTracks (
                    _id INTEGER PRIMARY KEY AUTOINCREMENT,
                    artist TEXT NOT NULL,
                    title TEXT NOT NULL,
                    image TEXT NOT NULL
                )
                """.trimIndent()
            )
            legacy.execSQL(
                "INSERT INTO NulaTracks (artist, title, image) VALUES " +
                    "('Izit', 'Make Way For The Solos', 'cover-a')," +
                    "('Adi Oasis', 'Serena', 'cover-b')"
            )
            legacy.version = 1
        }
    }

    @Test
    fun favorites_saved_before_the_driver_swap_survive_it() = runBlocking {
        val database = NulaDatabase(BundledSQLiteDriver(), dbFile.absolutePath)

        val tracks = database.selectAllTracks().sortedBy { it.id }

        assertEquals(2, tracks.size)
        assertEquals("Izit", tracks[0].artist)
        assertEquals("Make Way For The Solos", tracks[0].title)
        assertEquals("cover-a", tracks[0].image)
        assertEquals("Adi Oasis", tracks[1].artist)
        // Ids are the favourites' identity for deletion, so they must not shift.
        assertEquals(1, tracks[0].id)
        assertEquals(2, tracks[1].id)
    }

    @Test
    fun a_track_removed_from_a_legacy_database_is_the_right_one() = runBlocking {
        val database = NulaDatabase(BundledSQLiteDriver(), dbFile.absolutePath)
        val before = database.selectAllTracks().sortedBy { it.id }

        val removed = database.removeTrack(before[0])

        assertEquals(1, removed)
        val after = database.selectAllTracks()
        assertEquals(1, after.size)
        assertEquals("Adi Oasis", after[0].artist)
    }
}
```

Deliberately no `androidx.room:room-testing` / `MigrationTestHelper`: there is no Room and no migration to replay. Building the legacy file by hand with `SQLiteDatabase` is what proves the real compatibility claim.

**A third test is required, covering `insertTrack`.** The two tests above prove reads and deletes are safe against a legacy database, but `insertTrack` is the only function that creates user data and the only one that exercises `last_insert_rowid()`'s connection scoping — a connection-per-operation implementation reading that value from the wrong connection would return a stale or zero id, and nothing else here would catch it. Reusing the same legacy-seeded fixture, the test must insert a track, assert via a follow-up `selectAllTracks()` that it persisted with the right fields, assert the returned id is the real new rowid and collides with neither pre-existing id, and assert both pre-existing rows are still present and unchanged.

- [ ] **Step 5: Run the instrumented tests**

Run: `./gradlew :app:connectedDebugAndroidTest --tests "*LegacyFavoritesMigrationTest*"`

Expected: PASS, 3 tests.

If `selectAllTracks` returns an empty list, the `databasePath` is not the file the test wrote — check that `NulaDatabase` opens the path it is given and does not derive its own. If ids come back as `0`, the `getInt(0)` column index is wrong: the `SELECT` lists `_id` first.

- [ ] **Step 6: Run the whole suite and install**

Run: `./gradlew :shared:testAndroidHostTest :app:testDebugUnitTest :app:connectedDebugAndroidTest :app:installDebug`

Expected: PASS. `:shared` stays at 19 tests, `:app`'s unit tests are unchanged by this task, and `:app`'s instrumented tests gain 2.

- [ ] **Step 7: Verify a genuine upgrade**

The instrumented test builds its own database file. This step is the one that exercises the real one, at the real path, written by the real old code.

1. `git stash --include-untracked`, then `./gradlew :app:installDebug`. Open the app, tune in, and add two favourites with the heart button. Confirm they appear on the favourites screen.
2. `git stash pop`, then `./gradlew :app:installDebug` — an install **over the top**, not an uninstall.
3. Open the favourites screen. Both tracks must still be listed, and deleting one must remove that one.

This step needs a human for the sensory parts. Report it as UNVERIFIED with the exact steps for the human to run, and do not claim it passed.

- [ ] **Step 8: Stage**

```bash
git add -A shared app gradle/libs.versions.toml
```

Do not commit. Report the staged file list. The controller commits with:

```
refactor: replace SQLiteOpenHelper with the multiplatform SQLite driver

Room was the plan here and cannot be used: KSP 2.2.10-2.0.2 crashes against
com.android.kotlin.multiplatform.library, AGP 9 rejects the legacy
com.android.library + androidTarget() combination outright, and AGP
9.4.0-alpha07 embeds the same Kotlin, so no newer KSP is reachable.

androidx.sqlite is the driver Room would have sat on, without the code
generation. It also removes the risk Room introduced: no TableInfo validation
means no identity hash, no schema check and no migration, so the existing
NulaTracks table is used exactly as MyDatabaseHelper wrote it.

NulaDatabase keeps its three suspend functions, so the ViewModels are
untouched. An instrumented test builds a database with the legacy schema and
proves the rows and their ids survive.
```

---

### Task 7: Move the ViewModels to `commonMain`

**Files:**
- Move + rewrite: `app/src/main/.../features/player/RadioViewModel.kt` → `shared/src/commonMain/kotlin/com/radionula/radionula/features/player/RadioViewModel.kt`
- Move: `app/src/main/.../features/favorites/FavoritesViewModel.kt` → `shared/src/commonMain/kotlin/com/radionula/radionula/features/favorites/FavoritesViewModel.kt`
- Create: `shared/src/commonMain/kotlin/com/radionula/radionula/services/mediaplayer/MediaPlayerController.kt`
- Create: `app/src/main/java/com/radionula/radionula/features/player/ChannelArt.kt`
- Modify: `app/src/main/.../services/mediaplayer/MediaplayerPresenter.kt`, `app/src/main/.../features/player/PlayerScreen.kt`, `app/src/test/.../RadioViewModelTest.kt`
- Modify: `shared/build.gradle.kts`, `gradle/libs.versions.toml`

**Interfaces:**
- Consumes: everything from Tasks 3–6.
- Produces:
  - `interface MediaPlayerController` — `val isPlaying: StateFlow<Boolean>`, `val channelIndex: StateFlow<Int>`, `fun tuneIn(channelIndex: Int)`, `fun nextChannel()`, `fun pauseRadio()`
  - `data class PlayerUiState(showTuneIn: Boolean = true, isPlaying: Boolean = false, cover: String = "", tracks: List<NulaTrack> = emptyList(), channel: ChannelPresenter.Channel = ChannelPresenter.Channel.Classic)` — **`channelArt` is replaced by `channel`**
  - `class RadioViewModel(playlistReposetory, channelPresenter, mediaplayerPresenter: MediaPlayerController, nulaDatabase)`
  - in `:app`: `data class ChannelArt(logo: Int, skip: Int, pause: Int)` and `fun ChannelPresenter.Channel.art(): ChannelArt`

- [ ] **Step 1: Add the KMP lifecycle-viewmodel dependency**

In `gradle/libs.versions.toml`, bump lifecycle and add the base artifact:

```toml
lifecycle = "2.10.0"
```

```toml
androidx-lifecycle-viewmodel = { module = "androidx.lifecycle:lifecycle-viewmodel", version.ref = "lifecycle" }
```

All four lifecycle artifacts share the `lifecycle` ref, so `-ktx`, `-compose` and `-runtime-compose` move to 2.10.0 together. Mixing lifecycle versions produces duplicate-class and `NoSuchMethodError` failures.

**2.10.0, not 2.11.0.** 2.11.0's `-compose` artifacts declare `minCompileSdk=37` and this project is on `compileSdk 36`; 2.10.0 needs 35 at most and is fully KMP. Bumping `compileSdk` to satisfy a lifecycle patch is out of scope and a far wider blast radius than a version pin.

In `shared/build.gradle.kts`, add to `commonMain.dependencies`:

```kotlin
            // api, not implementation: RadioViewModel extends ViewModel and
            // :app resolves it through koinViewModel().
            api(libs.androidx.lifecycle.viewmodel)
```

- [ ] **Step 2: Define the player interface**

Create `shared/src/commonMain/kotlin/com/radionula/radionula/services/mediaplayer/MediaPlayerController.kt`:

```kotlin
package com.radionula.radionula.services.mediaplayer

import kotlinx.coroutines.flow.StateFlow

/**
 * What the player looks like to the ViewModel.
 *
 * isPlaying and channelIndex are reported by the player itself, not set by the
 * UI, which is how an audio-focus pause, a notification tap or a headset
 * button reaches the screen.
 *
 * The media3 implementation lives in :app - media3 is Android-only, and this
 * is the seam an iOS AVPlayer implementation would sit behind.
 */
interface MediaPlayerController {
    val isPlaying: StateFlow<Boolean>
    val channelIndex: StateFlow<Int>

    fun tuneIn(channelIndex: Int)
    fun nextChannel()
    fun pauseRadio()
}
```

- [ ] **Step 3: Make the media3 presenter implement it**

In `app/src/main/java/com/radionula/radionula/services/mediaplayer/MediaplayerPresenter.kt`, change the class declaration and mark the members as overrides. Everything else in the file, including the `withController` and `connect` internals and the `ponytail:` note, stays as-is:

```kotlin
class MediaplayerPresenter(private val context: Context) : MediaPlayerController {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _channelIndex = MutableStateFlow(0)
    override val channelIndex: StateFlow<Int> = _channelIndex

    override fun tuneIn(channelIndex: Int) = withController { controller ->
```

Also add `override` to `nextChannel()` and `pauseRadio()`.

In `app/src/main/java/com/radionula/radionula/services/mediaplayer/RadioPlayerModule.kt`, bind the interface:

```kotlin
package com.radionula.radionula.services.mediaplayer

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val radioPlayerModule = module {
    single { TuningNoise(androidContext()) }
    single<MediaPlayerController> { MediaplayerPresenter(androidContext()) }
}
```

- [ ] **Step 4: Move `ChannelArt` into `:app`**

Create `app/src/main/java/com/radionula/radionula/features/player/ChannelArt.kt`:

```kotlin
package com.radionula.radionula.features.player

import androidx.annotation.DrawableRes
import com.radionula.radionula.R
import com.radionula.radionula.core.util.ChannelPresenter

/** The three per-channel drawables the player swaps together. */
data class ChannelArt(
    @param:DrawableRes val logo: Int,
    @param:DrawableRes val skip: Int,
    @param:DrawableRes val pause: Int,
)

/**
 * Lives in :app, not in the ViewModel: these are R.drawable ints, and R is
 * Android-only. The ViewModel reports which channel is live and the screen
 * decides what that looks like.
 */
fun ChannelPresenter.Channel.art(): ChannelArt = when (this) {
    ChannelPresenter.Channel.Classic -> ChannelArt(
        R.drawable.nula_channel1, R.drawable.skip_channel1, R.drawable.pause_channel1
    )
    ChannelPresenter.Channel.Ch2 -> ChannelArt(
        R.drawable.nula_channel2, R.drawable.skip_channel2, R.drawable.pause_channel2
    )
    ChannelPresenter.Channel.Smoky -> ChannelArt(
        R.drawable.nula_channel3, R.drawable.skip_channel3, R.drawable.pause_channel3
    )
}
```

- [ ] **Step 5: Move and rewrite `RadioViewModel`**

```bash
S=shared/src/commonMain/kotlin/com/radionula/radionula
mkdir -p $S/features/player $S/features/favorites
git mv app/src/main/java/com/radionula/radionula/features/player/RadioViewModel.kt $S/features/player/
git mv app/src/main/java/com/radionula/radionula/features/favorites/FavoritesViewModel.kt $S/features/favorites/
```

`FavoritesViewModel.kt` needs **no content change** — it references only `NulaDatabase`, `NulaTrack` and coroutines, all of which are in `commonMain` now.

In `RadioViewModel.kt`, make exactly these changes and leave every comment and every other line intact:

Remove these imports:

```kotlin
import androidx.annotation.DrawableRes
import com.radionula.radionula.R
```

Change the `MediaplayerPresenter` import to the interface:

```kotlin
import com.radionula.radionula.services.mediaplayer.MediaPlayerController
```

Delete the `ChannelArt` data class and the `CLASSIC_ART` val (they now live in `:app`), and change `PlayerUiState`:

```kotlin
data class PlayerUiState(
    val showTuneIn: Boolean = true,
    val isPlaying: Boolean = false,
    val cover: String = "",
    val tracks: List<NulaTrack> = emptyList(),
    val channel: ChannelPresenter.Channel = ChannelPresenter.Channel.Classic,
)
```

Change the constructor parameter type:

```kotlin
    private val mediaplayerPresenter: MediaPlayerController,
```

Replace the `channelArt` state holder with the channel:

```kotlin
    private val currentChannel = MutableStateFlow(ChannelPresenter.Channel.Classic)
```

In the `combine`, swap the source and the mapped field:

```kotlin
        mediaplayerPresenter.isPlaying,
        currentChannel,
        tunedIn,
    ) { song, playlist, playing, channel, tuned ->
        PlayerUiState(
            showTuneIn = !tuned,
            isPlaying = playing,
            cover = song.cover,
            tracks = playlist,
            channel = channel,
        )
```

And in `onChannelChanged`, replace the art assignment and delete `getChannelLogo` entirely:

```kotlin
    private suspend fun onChannelChanged(index: Int) {
        val channel = channelPresenter.select(index)
        playlistReposetory.setChannel(channel)
        currentChannel.value = channel

        // Nothing is fetched until the radio has been started, so a cold start
        // shows no playlist rather than tracks this session never heard.
        if (tunedIn.value) playlistReposetory.fetchCurrentPlaylist()
    }
```

- [ ] **Step 6: Point `PlayerScreen` at the new state**

In `app/src/main/java/com/radionula/radionula/features/player/PlayerScreen.kt`, resolve the art once at the top of `PlayerScreen` and use it in the three places that referenced `state.channelArt`:

```kotlin
    val art = state.channel.art()

    Box(modifier.fillMaxSize().background(Brown)) {
```

Then `logo = state.channelArt.logo` → `logo = art.logo`, `painterResource(state.channelArt.skip)` → `painterResource(art.skip)`, and `painterResource(state.channelArt.pause)` → `painterResource(art.pause)`.

- [ ] **Step 7: Update `RadioViewModelTest` for the renamed type**

In `app/src/test/java/com/radionula/radionula/RadioViewModelTest.kt`, change the import:

```kotlin
import com.radionula.radionula.services.mediaplayer.MediaPlayerController
```

and the field:

```kotlin
    private val mediaplayerPresenter: MediaPlayerController = mock()
```

That is the only change. This test stays in `app/src/test` with Mockito — see "Deviations from the spec". Every assertion stays as it is; `:shared` classes are on `:app`'s test classpath, so it exercises the moved ViewModel unchanged.

- [ ] **Step 8: Run everything and install**

Run: `./gradlew :shared:testAndroidHostTest :app:testDebugUnitTest :app:connectedDebugAndroidTest :app:installDebug`

Expected: PASS with unchanged counts. `PlayerScreenTest` needs no edits — it only ever builds `PlayerUiState()` with defaults and named arguments and never touched `channelArt`.

On the device, verify the whole player, since this task rewires the UI's state source:

1. Tune in — audio plays, the channel-1 logo shows and the tune-in button does not come back.
2. Skip twice — the logo, skip and pause artwork change with each channel, and the playlist follows.
3. Pause, then press skip — the same channel resumes rather than skipping forward.
4. Switch channel from the notification with the app backgrounded, then reopen — the artwork matches the channel actually playing.
5. Add a favourite from the heart button — the toast appears and the track is in the favourites list.

- [ ] **Step 9: Commit**

Ask the user for permission to commit, then:

```bash
git add -A shared app gradle/libs.versions.toml
git commit -m "refactor: move the ViewModels to commonMain

PlayerUiState carries the channel instead of three R.drawable ints, since R
is Android-only. The channel -> artwork mapping is now an extension in :app,
which is where it belongs: the ViewModel says what is playing, the screen
decides what that looks like.

MediaplayerPresenter is split behind a MediaPlayerController interface. The
media3 implementation stays in :app and is bound there; this is the seam an
iOS AVPlayer would sit behind.

RadioViewModelTest stays in app/src/test on Mockito. It has four mocks and
around twenty verify assertions, two on final classes, and rewriting it as
hand-rolled spies risks weakening them for no coverage gain while the only
target is Android. Rewrite it to fakes when an iOS target needs it.

All lifecycle artifacts go to 2.10.0 together for the KMP viewmodel."
```

---

### Task 8: Clean up and verify the release build

**Files:**
- Modify: `gradle/libs.versions.toml`, `app/build.gradle.kts`
- Delete: `filestructure.txt`
- Modify: `app/proguard-rules.pro` (only if the release build needs it)

**Interfaces:**
- Consumes: everything from Tasks 1–7.
- Produces: nothing new.

- [ ] **Step 1: Find dependencies with no remaining consumer**

Run:

```bash
grep -rn "okhttp3\|retrofit2" app/src shared/src
grep -rn "javax.xml\|org.w3c.dom\|org.xml.sax" app/src shared/src
grep -rn "SQLiteOpenHelper\|ContentValues" app/src shared/src
```

Expected: no output from any of the three. Anything found is a leftover import from Tasks 4–6 — delete it.

- [ ] **Step 2: Delete the scratch template**

```bash
git rm filestructure.txt
```

It is a generic package-layout template referencing Hilt, WorkManager and FCM, none of which this project uses. It documents nothing about RadioNula.

- [ ] **Step 3: Confirm the dependency list matches reality**

Read `app/build.gradle.kts`, `build.gradle.kts` and `gradle/libs.versions.toml`. Every `[libraries]` and `[plugins]` entry must be referenced, and every `implementation` must have a consumer in source. Remove any orphan.

Two specific ones to expect:

- **KSP.** Task 1 corrected its pin to `2.2.10-2.0.2`, but with Room out of the design nothing applies it. Remove `alias(libs.plugins.ksp) apply false` from the root `build.gradle.kts`, and the `ksp` entries from both `[versions]` and `[plugins]`.
- **The `retrofit` entry**, if Task 4 did not already remove it.

`mockito-kotlin`, `mockito-core` and `androidx-arch-core-testing` are still used by `RadioViewModelTest` and `ConnectionLiveDataTest` — keep them.

Run: `./gradlew :shared:testAndroidHostTest :app:testDebugUnitTest`

Expected: PASS. A removed-but-needed dependency fails here.

- [ ] **Step 4: Build the release variant**

Run: `./gradlew :app:assembleRelease`

Expected: PASS. If R8 fails on a missing class from the sqlite driver, Ktor or xmlutil, add a keep rule to `app/proguard-rules.pro` naming the specific class — do not broaden an existing rule to a whole package, and do not disable minification.

- [ ] **Step 5: Install the release build and verify it end to end**

Run: `./gradlew :app:installRelease`

A minified build behaves differently from debug, and all three swapped libraries are new to it. Verify on the device:

1. Tune in — audio plays and the track title and cover appear. *(Ktor and xmlutil survived R8.)*
2. Add a favourite, force-stop the app, reopen, open favourites — the track is there. *(The sqlite driver survived R8.)*
3. Open comments — the Remark42 thread loads. *(WebView unaffected.)*
4. Airplane mode on — the overlay appears and the app does not crash. Airplane mode off — the playlist resumes within 30 seconds.
5. Swipe the app away while playing — audio stops.

- [ ] **Step 6: Commit**

Ask the user for permission to commit, then:

```bash
git add -A
git commit -m "chore: drop dependencies the KMP migration made unused

Removes the Retrofit and OkHttp coordinates now that Ktor owns the fetch
path, and deletes filestructure.txt - a generic package-layout template
mentioning Hilt, WorkManager and FCM, none of which this project uses.

Verified against a minified release build on device: the sqlite driver, Ktor
and xmlutil all survive R8."
```

- [ ] **Step 7: Report the final state**

Summarise for the user:

- the test counts in `:shared` and `:app` before and after
- which files ended up in `commonMain`, `androidMain` and `:app`
- exactly what adding an iOS target would now require: three `ios*` targets plus `binaries.framework` in `shared/build.gradle.kts`, `ktor-client-darwin` in `iosMain`, a `MediaPlayerController` implementation over `AVPlayer`, a connectivity abstraction in `commonMain`, a rewrite of `RadioViewModelTest` to fakes, and the SwiftUI app itself
- any `ponytail:` comment or keep rule added along the way
