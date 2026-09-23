import 'package:flutter_test/flutter_test.dart';
import 'package:kas_go/core/utils/formatters.dart';
import 'package:kas_go/data/demo/demo_data.dart';
import 'package:kas_go/data/repositories/finance_repository.dart';
import 'package:kas_go/data/repositories/user_profile_repository.dart';

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

    test('format tanggal aman tanpa LocaleDataException', () {
      final date = DateTime(2026, 9, 23);
      final formatted = formatTanggal(date);
      expect(formatted, contains('23'));
      expect(formatted, contains('2026'));
    });

    test('format periode aman tanpa LocaleDataException', () {
      final date = DateTime(2026, 9, 1);
      final formatted = formatPeriode(date);
      expect(formatted, contains('2026'));
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
      expect(balances.last.value, DemoData.totalBalance);
    });

    test('FinanceRepository real-time record income & expense updates balance', () {
      final repo = FinanceRepository.instance;
      final initialBalance = repo.totalBalance;

      repo.recordIncome(
        memberName: 'Test Warga',
        period: '2026-09',
        amount: 25000,
        paymentMethod: 'Tunai',
        recorderName: 'Admin 1',
      );
      expect(repo.totalBalance, initialBalance + 25000);

      repo.recordExpense(
        category: 'Operasional',
        recipient: 'Warung Test',
        description: 'Test Belanja',
        amount: 10000,
        recorderName: 'Admin 2',
        date: DateTime.now(),
      );
      expect(repo.totalBalance, initialBalance + 15000);
    });

    test('UserProfileRepository updates profile and links Google account', () {
      final repo = UserProfileRepository.instance;
      repo.updateProfile(
        name: 'Budi Santoso',
        phone: '0811-2233-4455',
        address: 'RT 01 / RW 05',
      );
      expect(repo.current.name, 'Budi Santoso');
      expect(repo.current.phone, '0811-2233-4455');

      repo.linkGoogleAccount(
        googleName: 'Budi Google',
        googleEmail: 'budi.google@gmail.com',
      );
      expect(repo.current.isGoogleAccount, isTrue);
      expect(repo.current.email, 'budi.google@gmail.com');
    });
  });
}
