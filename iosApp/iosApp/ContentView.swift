import SwiftUI
import Shared

/// The Compose UI from :shared, filling the window. Nothing else belongs here -
/// the screens, the navigation and the player are all shared code.
struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        NulaViewControllerKt.NulaViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
