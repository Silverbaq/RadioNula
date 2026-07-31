import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.crashlytics)
}

/**
 * Release signing credentials, from outside the repo only.
 *
 * Looked up in local.properties (gitignored) first, then the environment. No
 * key material and no password belongs in a tracked file - the existing
 * keystore lives in CI as a base64 secret, and base64.txt is gitignored
 * precisely because *.jks and *.keystore do not match it.
 */
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use(::load)
}

fun signingSecret(name: String): String? =
    (localProperties.getProperty(name) ?: System.getenv(name))?.takeIf { it.isNotBlank() }

val releaseStoreFile = signingSecret("RELEASE_STORE_FILE")?.let(rootProject::file)
val releaseStorePassword = signingSecret("RELEASE_STORE_PASSWORD")
val releaseKeyAlias = signingSecret("RELEASE_KEY_ALIAS")
val releaseKeyPassword = signingSecret("RELEASE_KEY_PASSWORD")

/**
 * All four present and the keystore actually on disk, or no release signing
 * config is registered at all.
 *
 * Declaring one unconditionally would break `assembleRelease` for anyone
 * without the keystore, and would break CI, which signs by injecting
 * `android.injected.signing.*` on the command line instead.
 */
val canSignRelease = releaseStoreFile?.exists() == true &&
        releaseStorePassword != null &&
        releaseKeyAlias != null &&
        releaseKeyPassword != null

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

    signingConfigs {
        if (canSignRelease) {
            create("release") {
                storeFile = releaseStoreFile
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
                // Both schemes: v1 for the API 23 floor, v2+ for everything since.
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Null when the credentials are absent, which leaves the release
            // build unsigned rather than failing it. That is what CI relies on:
            // it injects android.injected.signing.* instead.
            signingConfig = signingConfigs.findByName("release")
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
    implementation(project(":shared"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)

    implementation(libs.kotlinx.coroutines.android)

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
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    // 3.6.1 calls InputManager.getInstance(), which newer Android removed, so
    // every Compose test died in Espresso.onIdle before reaching an assertion.
    androidTestImplementation(libs.espresso.core)
}
