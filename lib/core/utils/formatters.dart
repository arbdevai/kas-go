import 'package:intl/intl.dart';

/// Format mata uang Rupiah (integer, tanpa pecahan).
String formatRupiah(int amount) {
  try {
    final formatter = NumberFormat.currency(
      locale: 'id_ID',
      symbol: 'Rp ',
      decimalDigits: 0,
    );
    return formatter.format(amount);
  } catch (_) {
    final str = amount.toString();
    final buffer = StringBuffer();
    for (var i = 0; i < str.length; i++) {
      if (i > 0 && (str.length - i) % 3 == 0) {
        buffer.write('.');
      }
      buffer.write(str[i]);
    }
    return 'Rp $buffer';
  }
}

/// Format tanggal Indonesia singkat, mis. "22 Sep 2026".
String formatTanggal(DateTime date) {
  try {
    return DateFormat('d MMM yyyy', 'id_ID').format(date);
  } catch (_) {
    const months = [
      'Jan',
      'Feb',
      'Mar',
      'Apr',
      'Mei',
      'Jun',
      'Jul',
      'Agu',
      'Sep',
      'Okt',
      'Nov',
      'Des',
    ];
    final m =
        (date.month >= 1 && date.month <= 12) ? months[date.month - 1] : '';
    return '${date.day} $m ${date.year}';
  }
}

/// Format periode bulan, mis. "September 2026".
String formatPeriode(DateTime date) {
  try {
    return DateFormat('MMMM yyyy', 'id_ID').format(date);
  } catch (_) {
    const months = [
      'Januari',
      'Februari',
      'Maret',
      'April',
      'Mei',
      'Juni',
      'Juli',
      'Agustus',
      'September',
      'Oktober',
      'November',
      'Desember',
    ];
    final m =
        (date.month >= 1 && date.month <= 12) ? months[date.month - 1] : '';
    return '$m ${date.year}';
  }
}
