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
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
    }
}
