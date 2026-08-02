import SwiftUI
import FirebaseCore
import FirebaseCrashlytics
import Shared

@main
struct iOSApp: App {
    init() {
        // Crashlytics only - :app's google-services.json has Analytics too, the
        // iOS app deliberately does not, which is what PrivacyInfo declares.
        // Kotlin/Native aborts on an uncaught exception, so the crash arrives as
        // a signal rather than an NSException: symbolised, but with the Kotlin
        // frames in the native stack.
        FirebaseApp.configure()
        IosModuleKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                // Compose draws its own insets; the safe area is handled inside.
                .ignoresSafeArea()
        }
    }
}
