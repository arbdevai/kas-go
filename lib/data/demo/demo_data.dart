import '../../data/local/tables/app_tables.dart';

/// Data contoh statis untuk mode demo.
/// TIDAK menggunakan rekening, QRIS, atau nama nyata organisasi.
class DemoData {
  DemoData._();

  static final List<Map<String, dynamic>> entries = [
    // 2026 — Pemasukan
    _income('Iuran Agustus - Ahmad', 50000, '2026-08', 'Admin Demo 1'),
    _income('Iuran Agustus - Budi', 50000, '2026-08', 'Admin Demo 1'),
    _income('Iuran Agustus - Citra', 50000, '2026-08', 'Admin Demo 2'),
    _income('Iuran September - Ahmad', 50000, '2026-09', 'Admin Demo 2'),
    _income('Iuran September - Budi', 50000, '2026-09', 'Admin Demo 3'),
    _income('Iuran September - Citra', 50000, '2026-09', 'Admin Demo 3'),
    _income('Donasi HUT RI', 200000, '2026-08', 'Admin Demo 1'),
    // Pengeluaran
    _expense('Konsumsi Rapat Bulanan', 85000, 'Operasional', '2026-08', 'Admin Demo 1'),
    _expense('Hadiah HUT RI', 150000, 'Sosial', '2026-08', 'Admin Demo 2'),
    _expense('ATK & Stempel', 45000, 'Perlengkapan', '2026-09', 'Admin Demo 3'),
    _expense('Banner Kegiatan', 120000, 'Perlengkapan', '2026-09', 'Admin Demo 1'),
  ];

  static Map<String, dynamic> _income(
    String summary,
    int amount,
    String period,
    String recorder,
  ) => {
        'entryType': LedgerType.income,
        'amount': amount,
        'period': period,
        'summary': summary,
        'recordedByName': recorder,
        'occurredAt': DateTime(2026, int.parse(period.split('-')[1]), 15),
      };

  static Map<String, dynamic> _expense(
    String summary,
    int amount,
    String category,
    String period,
    String recorder,
  ) => {
        'entryType': LedgerType.expense,
        'amount': amount,
        'category': category,
        'period': period,
        'summary': summary,
        'recordedByName': recorder,
        'occurredAt': DateTime(2026, int.parse(period.split('-')[1]), 10),
      };

  /// Hitung saldo demo.
  static int get totalBalance {
    var balance = 0;
    for (final e in entries) {
      if (e['entryType'] == LedgerType.income) {
        balance += e['amount'] as int;
      } else {
        balance -= e['amount'] as int;
      }
    }
    return balance;
  }

  static int get totalIncome => entries
      .where((e) => e['entryType'] == LedgerType.income)
      .fold(0, (s, e) => s + (e['amount'] as int));

  static int get totalExpense => entries
      .where((e) => e['entryType'] == LedgerType.expense)
      .fold(0, (s, e) => s + (e['amount'] as int));

  /// Agregat bulanan untuk grafik batang pemasukan.
  static Map<String, int> get monthlyIncome {
    final result = <String, int>{};
    for (final e in entries) {
      if (e['entryType'] == LedgerType.income) {
        final period = e['period'] as String;
        result[period] = (result[period] ?? 0) + (e['amount'] as int);
      }
    }
    return Map.fromEntries(
      result.entries.toList()..sort((a, b) => a.key.compareTo(b.key)),
    );
  }

  /// Agregat per kategori untuk grafik pengeluaran.
  static Map<String, int> get expenseByCategory {
    final result = <String, int>{};
    for (final e in entries) {
      if (e['entryType'] == LedgerType.expense) {
        final cat = e['category'] as String;
        result[cat] = (result[cat] ?? 0) + (e['amount'] as int);
      }
    }
    return Map.fromEntries(
      result.entries.toList()..sort((a, b) => b.value.compareTo(a.value)),
    );
  }

  /// Saldo kumulatif per bulan untuk grafik garis.
  static List<MapEntry<String, int>> get cumulativeBalance {
    final months = <String>{};
    for (final e in entries) {
      if (e['period'] != null) {
        months.add(e['period'] as String);
      }
    }
    final sorted = months.toList()..sort();

    var running = 0;
    return sorted.map((month) {
      for (final e in entries) {
        if (e['period'] == month) {
          if (e['entryType'] == LedgerType.income) {
            running += e['amount'] as int;
          } else {
            running -= e['amount'] as int;
          }
        }
      }
      return MapEntry(month, running);
    }).toList();
  }
}
