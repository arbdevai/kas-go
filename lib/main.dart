import 'package:flutter/material.dart';
import 'package:intl/date_symbol_data_local.dart';
import 'app.dart';
import 'core/config/app_config.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  try {
    await initializeDateFormatting('id_ID', null);
  } catch (_) {
    // Fallback gracefully if system locale data cannot be fetched
  }
  runApp(const KasGoApp(config: AppConfig.flavorProduction()));
}
