import com.android.build.api.dsl.KotlinMultiplatformAndroidHostTestCompilation
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
    alias(libs.plugins.composeMultiplatform)
    id("org.jetbrains.kotlin.plugin.compose")
}

compose.resources {
    // Named explicitly: the default would derive it from the Android namespace
    // (com.radionula.shared), which is not where any of this code lives.
    packageOfResClass = "com.radionula.radionula.resources"
}

kotlin {
    androidLibrary {
        namespace = "com.radionula.shared"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }

        // Runs commonTest on the JVM. No device-test builder: the instrumented
        // tests live in :app, which already has a runner configured.
        withHostTestBuilder {}

        // Same as :app's testOptions.unitTests.isReturnDefaultValues: the host
        // test JVM has no real android.jar, so an unmocked call (Log.e, reached
        // through logError) throws instead of being a no-op.
        compilations.withType(KotlinMultiplatformAndroidHostTestCompilation::class.java).configureEach {
            isReturnDefaultValues = true
        }
    }

    // Device and simulator only - iosX64 would need an Intel Mac to be useful.
    // Framework is static so the iOS app links it with no embed-and-sign step.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(libs.navigation.compose.mp)
            implementation(libs.lifecycle.viewmodel.compose.mp)
            implementation(libs.lifecycle.runtime.compose.mp)
            implementation(libs.coil3.compose)
            implementation(libs.coil3.network.ktor)
            implementation(libs.koin.compose.viewmodel)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            // api, not implementation: Flow and StateFlow appear in the public
            // API that :app consumes.
            api(libs.kotlinx.coroutines.core)
            // api, not implementation: RadioViewModel extends ViewModel and
            // :app resolves it through koinViewModel().
            api(libs.androidx.lifecycle.viewmodel)
            implementation(libs.koin.core)
            implementation(libs.ktor.client.core)
            implementation(libs.xmlutil.core)
            // api, not implementation: SQLiteDriver is a constructor parameter of
            // NulaDatabase, which :app instantiates through Koin.
            api(libs.androidx.sqlite.bundled)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            // iOS only: the Android comments WebView stays hand-rolled, because
            // no wrapper exposes the instance setAcceptThirdPartyCookies needs.
            implementation(libs.compose.webview.multiplatform)
        }
    }
}

// The KMP android target's host tests run under testAndroidHostTest, not test -
// so the habitual `./gradlew test` (and CI's :app:testDebug) would silently
// skip every test in this module. Alias it so `test` stays the honest entrypoint.
tasks.register("test") {
    dependsOn("testAndroidHostTest")
}

/**
 * Compose resources for the Android variant, by hand.
 *
 * The Compose plugin only wires its own copy-to-assets task when the Android
 * target comes from the classic com.android.library plugin. With
 * com.android.kotlin.multiplatform.library the task exists but is never given
 * an output, so every drawable and font was generated for the iOS frameworks
 * and silently missing from both APKs - painterResource threw
 * MissingResourceException at runtime. AGP 9 refuses com.android.library next
 * to KMP, so the copy is done here and :app adds the directory as assets.
 *
 * The package segment in the path is what the resource reader looks for; it
 * matches compose.resources.packageOfResClass above.
 */
val androidComposeAssets by tasks.registering(Copy::class) {
    dependsOn("prepareComposeResourcesTaskForCommonMain")
    from(
        layout.buildDirectory.dir(
            "generated/compose/resourceGenerator/preparedResources/commonMain/composeResources"
        )
    )
    into(
        layout.buildDirectory.dir(
            "composeResourcesAndroidAssets/composeResources/com.radionula.radionula.resources"
        )
    )
}
