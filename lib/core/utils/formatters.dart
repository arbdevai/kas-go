import 'package:intl/intl.dart';

/// Format mata uang Rupiah (integer, tanpa pecahan).
String formatRupiah(int amount) {
  final formatter = NumberFormat.currency(
    locale: 'id_ID',
    symbol: 'Rp ',
    decimalDigits: 0,
  );
  return formatter.format(amount);
}

/// Format tanggal Indonesia singkat, mis. "22 Sep 2026".
String formatTanggal(DateTime date) {
  return DateFormat('d MMM yyyy', 'id_ID').format(date);
}

/// Format periode bulan, mis. "September 2026".
String formatPeriode(DateTime date) {
  return DateFormat('MMMM yyyy', 'id_ID').format(date);
}
