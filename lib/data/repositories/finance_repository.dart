import 'dart:async';
import 'package:flutter/foundation.dart';
import '../demo/demo_data.dart';
import '../local/tables/app_tables.dart';

/// Model transaksi kas nyata untuk runtime aplikasi.
class TransactionItem {
  TransactionItem({
    required this.id,
    required this.entryType,
    required this.amount,
    required this.summary,
    required this.recordedByName,
    required this.occurredAt,
    this.period,
    this.category,
    this.paymentMethod = 'Tunai',
  });

  final String id;
  final LedgerType entryType;
  final int amount;
  final String summary;
  final String recordedByName;
  final DateTime occurredAt;
  final String? period;
  final String? category;
  final String paymentMethod;

  bool get isIncome => entryType == LedgerType.income;
}

/// Model permintaan jemput kas nyata.
class PickupItem {
  PickupItem({
    required this.id,
    required this.name,
    required this.address,
    required this.phone,
    required this.amount,
    required this.timeSlot,
    required this.createdAt,
    this.status = 'Menunggu',
  });

  final String id;
  final String name;
  final String address;
  final String phone;
  final int amount;
  final String timeSlot;
  final DateTime createdAt;
  String status;
}

/// Repositori keuangan terpadu untuk state runtime persisten.
class FinanceRepository extends ChangeNotifier {
  FinanceRepository._() {
    _initFromSeed();
  }

  static final FinanceRepository instance = FinanceRepository._();

  final List<TransactionItem> _items = [];
  final List<PickupItem> _pickups = [];

  List<TransactionItem> get allTransactions => List.unmodifiable(_items);
  List<PickupItem> get allPickups => List.unmodifiable(_pickups);

  int get totalBalance {
    var balance = 0;
    for (final e in _items) {
      if (e.isIncome) {
        balance += e.amount;
      } else {
        balance -= e.amount;
      }
    }
    return balance;
  }

  int get totalIncome => _items
      .where((e) => e.isIncome)
      .fold(0, (s, e) => s + e.amount);

  int get totalExpense => _items
      .where((e) => !e.isIncome)
      .fold(0, (s, e) => s + e.amount);

  Map<String, int> get monthlyIncome {
    final result = <String, int>{};
    for (final e in _items) {
      if (e.isIncome && e.period != null) {
        result[e.period!] = (result[e.period!] ?? 0) + e.amount;
      }
    }
    return Map.fromEntries(
      result.entries.toList()..sort((a, b) => a.key.compareTo(b.key)),
    );
  }

  Map<String, int> get expenseByCategory {
    final result = <String, int>{};
    for (final e in _items) {
      if (!e.isIncome && e.category != null) {
        result[e.category!] = (result[e.category!] ?? 0) + e.amount;
      }
    }
    return Map.fromEntries(
      result.entries.toList()..sort((a, b) => b.value.compareTo(a.value)),
    );
  }

  List<MapEntry<String, int>> get cumulativeBalance {
    final months = <String>{};
    for (final e in _items) {
      if (e.period != null) months.add(e.period!);
    }
    final sorted = months.toList()..sort();

    var running = 0;
    return sorted.map((month) {
      for (final e in _items) {
        if (e.period == month) {
          if (e.isIncome) {
            running += e.amount;
          } else {
            running -= e.amount;
          }
        }
      }
      return MapEntry(month, running);
    }).toList();
  }

  void _initFromSeed() {
    _items.clear();
    for (var i = 0; i < DemoData.entries.length; i++) {
      final e = DemoData.entries[i];
      _items.add(
        TransactionItem(
          id: 'tx_seed_$i',
          entryType: e['entryType'] as LedgerType,
          amount: e['amount'] as int,
          summary: e['summary'] as String,
          recordedByName: e['recordedByName'] as String,
          occurredAt: e['occurredAt'] as DateTime,
          period: e['period'] as String?,
          category: e['category'] as String?,
          paymentMethod: 'Tunai',
        ),
      );
    }
  }

  /// Tambah transaksi kas masuk baru (Admin) — langsung tersimpan & update saldo.
  void recordIncome({
    required String memberName,
    required String period,
    required int amount,
    required String paymentMethod,
    required String recorderName,
    String? note,
  }) {
    final summary = 'Iuran $period - $memberName${note != null && note.isNotEmpty ? ' ($note)' : ''}';
    final item = TransactionItem(
      id: 'tx_${DateTime.now().millisecondsSinceEpoch}',
      entryType: LedgerType.income,
      amount: amount,
      summary: summary,
      recordedByName: recorderName,
      occurredAt: DateTime.now(),
      period: period,
      paymentMethod: paymentMethod,
    );
    _items.insert(0, item);
    notifyListeners();
  }

  /// Tambah pengeluaran baru (Admin) — langsung memotong saldo kas.
  void recordExpense({
    required String category,
    required String recipient,
    required String description,
    required int amount,
    required String recorderName,
    required DateTime date,
  }) {
    final monthStr = '${date.year}-${date.month.toString().padLeft(2, '0')}';
    final summary = '$description ($recipient)';
    final item = TransactionItem(
      id: 'tx_${DateTime.now().millisecondsSinceEpoch}',
      entryType: LedgerType.expense,
      amount: amount,
      summary: summary,
      recordedByName: recorderName,
      occurredAt: date,
      category: category,
      period: monthStr,
    );
    _items.insert(0, item);
    notifyListeners();
  }

  /// Tambah permintaan jemput kas baru oleh warga.
  void addPickupRequest({
    required String name,
    required String address,
    required String phone,
    required int amount,
    required String timeSlot,
  }) {
    final item = PickupItem(
      id: 'pk_${DateTime.now().millisecondsSinceEpoch}',
      name: name,
      address: address,
      phone: phone,
      amount: amount,
      timeSlot: timeSlot,
      createdAt: DateTime.now(),
    );
    _pickups.insert(0, item);
    notifyListeners();
  }
}
