import 'package:flutter_test/flutter_test.dart';
import 'package:kas_go/core/config/app_config.dart';

void main() {
  group('Konfigurasi Aplikasi', () {
    test('mode default adalah demo', () {
      const config = AppConfig.flavorDemo();
      expect(config.isDemo, isTrue);
      expect(config.useFirebase, isFalse);
    });

    test('mode production mengaktifkan firebase', () {
      const config = AppConfig.flavorProduction();
      expect(config.isDemo, isFalse);
      expect(config.useFirebase, isTrue);
    });

    test('app id demo dan production berbeda', () {
      const demo = AppConfig.flavorDemo();
      const prod = AppConfig.flavorProduction();
      expect(demo.appId, isNot(prod.appId));
    });
  });
}
