/// Konfigurasi lingkungan aplikasi Kas Go.
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

  /// Flavor default sekarang adalah production (versi nyata).
  static AppConfig get current {
    const flavor = String.fromEnvironment('APP_FLAVOR', defaultValue: 'production');
    if (flavor == 'demo') {
      return const AppConfig.flavorDemo();
    }
    return const AppConfig.flavorProduction();
  }

  final AppFlavor flavor;
  final String appName;
  final String appId;
  final bool useFirebase;

  bool get isDemo => flavor == AppFlavor.demo;
}
