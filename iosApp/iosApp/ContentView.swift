import SwiftUI
import shared

struct ContentView: View {
    var body: some View {
        ComposeView()
            .edgesIgnoringSafeArea(.all)
            .ignoresSafeArea(.all, edges: .bottom)
    }
}

struct ComposeView: UIViewControllerRepresentable {

    func makeUIViewController(context: Context) -> UIViewController {
        Main_iosKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
