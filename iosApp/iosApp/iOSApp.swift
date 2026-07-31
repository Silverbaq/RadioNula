import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
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
