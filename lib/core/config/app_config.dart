/// Konfigurasi lingkungan aplikasi Kas Go.
///
/// Ada dua konfigurasi eksplisit:
/// - Demo: data contoh lokal, TANPA rekening/QR pembayaran nyata.
/// - Production: sinkronisasi Firebase live (butuh google-services.json asli).
enum AppFlavor { demo, production }

class AppConfig {
  const AppConfig.flavorDemo()
      : flavor = AppFlavor.demo,
        appName = 'Kas Go (Demo)',
        appId = 'id.or.karangtaruna.kasgo.demo',
        useFirebase = false;

  const AppConfig.flavorProduction()
      : flavor = AppFlavor.production,
        appName = 'Kas Go',
        appId = 'id.or.karangtaruna.kasgo',
        useFirebase = true;

  /// Flavor aktif, dipilih lewat --dart-define=APP_FLAVOR=production.
  static AppConfig get current {
    const flavor = String.fromEnvironment('APP_FLAVOR', defaultValue: 'demo');
    if (flavor == 'production') {
      return const AppConfig.flavorProduction();
    }
    return const AppConfig.flavorDemo();
  }

  final AppFlavor flavor;
  final String appName;
  final String appId;
  final bool useFirebase;

  bool get isDemo => flavor == AppFlavor.demo;
}
