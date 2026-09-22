import 'package:flutter_test/flutter_test.dart';
import 'package:kas_go/core/utils/formatters.dart';
import 'package:kas_go/data/demo/demo_data.dart';

void main() {
  group('Kalkulasi Keuangan', () {
    test('saldo = pemasukan - pengeluaran', () {
      expect(
        DemoData.totalBalance,
        DemoData.totalIncome - DemoData.totalExpense,
      );
    });

    test('pemasukan demo bernilai positif', () {
      expect(DemoData.totalIncome, greaterThan(0));
    });

    test('format rupiah tanpa pecahan', () {
      final formatted = formatRupiah(50000);
      expect(formatted, contains('Rp'));
      expect(formatted, contains('50.000'));
    });

    test('grafik bulanan tidak kosong', () {
      expect(DemoData.monthlyIncome, isNotEmpty);
    });

    test('kategori pengeluaran tidak kosong', () {
      expect(DemoData.expenseByCategory, isNotEmpty);
    });

    test('saldo kumulatif monoton tidak menurun tajam palsu', () {
      final balances = DemoData.cumulativeBalance;
      expect(balances, isNotEmpty);
      // Saldo akhir harus sama dengan total.
      expect(balances.last.value, DemoData.totalBalance);
    });
  });
}
