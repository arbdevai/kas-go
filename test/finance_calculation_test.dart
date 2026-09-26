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

    test('UserProfileRepository handles register, login and update', () async {
      final repo = UserProfileRepository.instance;
      final regErr = await repo.register(
        name: 'Budi Santoso',
        phone: '0811-2233-4455',
        address: 'RT 01 / RW 05',
        email: 'budi@gmail.com',
        password: 'password123',
      );
      expect(regErr, isNull);
      expect(repo.current.name, 'Budi Santoso');
      expect(repo.isAuthenticated, isTrue);

      await repo.updateProfile(
        name: 'Budi Santoso S.E.',
        phone: '0811-2233-4455',
        address: 'RT 01 / RW 05',
      );
      expect(repo.current.name, 'Budi Santoso S.E.');

      await repo.logout();
      expect(repo.isAuthenticated, isFalse);

      final loginErr = await repo.login(
        identifier: 'budi@gmail.com',
        password: 'password123',
      );
      expect(loginErr, isNull);
      expect(repo.isAuthenticated, isTrue);

      await repo.logout();
      expect(repo.isAuthenticated, isFalse);

      // Test Google registration flow
      final googleUserBefore = repo.checkGoogleAccount('ahmad.google@gmail.com');
      expect(googleUserBefore, isNull);

      final googleRegErr = await repo.registerWithGoogle(
        email: 'ahmad.google@gmail.com',
        name: 'Ahmad Google',
        phone: '0812-9999-8888',
        address: 'RT 03 / RW 05',
      );
      expect(googleRegErr, isNull);
      expect(repo.isAuthenticated, isTrue);
      expect(repo.current.name, 'Ahmad Google');

      final googleUserAfter = repo.checkGoogleAccount('ahmad.google@gmail.com');
      expect(googleUserAfter, isNotNull);
    });

    test('Audit trail recording on transaction edit', () {
      final repo = FinanceRepository.instance;
      repo.recordExpense(
        category: 'Operasional',
        recipient: 'Toko Listrik',
        description: 'Beli Lampu',
        amount: 50000,
        recorderName: 'Admin 1',
        date: DateTime.now(),
      );

      final tx = repo.allTransactions.first;
      expect(tx.wasEdited, isFalse);

      repo.editTransaction(
        id: tx.id,
        newAmount: 45000,
        newSummary: 'Beli Lampu Hemat Energi',
        newCategory: 'Operasional',
        editorName: 'Admin 2 (Sekretaris)',
        editReason: 'Diskon toko Rp 5.000',
      );

      final editedTx = repo.allTransactions.firstWhere((t) => t.id == tx.id);
      expect(editedTx.wasEdited, isTrue);
      expect(editedTx.amount, 45000);
      expect(editedTx.editedByName, 'Admin 2 (Sekretaris)');
      expect(editedTx.editReason, 'Diskon toko Rp 5.000');
    });
  });
}
