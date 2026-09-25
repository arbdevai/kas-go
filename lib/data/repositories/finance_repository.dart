import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../services/firebase_sync_service.dart';
import '../local/tables/app_tables.dart';

/// Model transaksi kas nyata untuk runtime aplikasi dengan Audit Trail lengkap.
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
    this.recordedByUid,
    this.editedByName,
    this.editedAt,
    this.editReason,
    this.billingId,
  });

  final String id;
  final LedgerType entryType;
  int amount;
  String summary;
  final String recordedByName;
  final DateTime occurredAt;
  String? period;
  String? category;
  String paymentMethod;
  String? recordedByUid;

  // Audit trail pengeditan/koreksi kas
  String? editedByName;
  DateTime? editedAt;
  String? editReason;

  // Tautan ke tagihan bulanan
  String? billingId;

  bool get isIncome => entryType == LedgerType.income;
  bool get wasEdited => editedByName != null && editedByName!.isNotEmpty;

  Map<String, dynamic> toMap() => {
        'id': id,
        'entryType': entryType.name,
        'amount': amount,
        'summary': summary,
        'recordedByName': recordedByName,
        'occurredAt': occurredAt.toIso8601String(),
        'period': period,
        'category': category,
        'paymentMethod': paymentMethod,
        'recordedByUid': recordedByUid,
        'editedByName': editedByName,
        'editedAt': editedAt?.toIso8601String(),
        'editReason': editReason,
        'billingId': billingId,
      };

  factory TransactionItem.fromMap(Map<String, dynamic> map) => TransactionItem(
        id: map['id'] as String,
        entryType: (map['entryType'] as String?) == 'expense'
            ? LedgerType.expense
            : LedgerType.income,
        amount: (map['amount'] as num?)?.toInt() ?? 0,
        summary: map['summary'] as String? ?? '',
        recordedByName: map['recordedByName'] as String? ?? 'Admin',
        occurredAt: DateTime.tryParse(map['occurredAt'] as String? ?? '') ??
            DateTime.now(),
        period: map['period'] as String?,
        category: map['category'] as String?,
        paymentMethod: map['paymentMethod'] as String? ?? 'Tunai',
        recordedByUid: map['recordedByUid'] as String?,
        editedByName: map['editedByName'] as String?,
        editedAt: map['editedAt'] != null
            ? DateTime.tryParse(map['editedAt'] as String)
            : null,
        editReason: map['editReason'] as String?,
        billingId: map['billingId'] as String?,
      );
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

  Map<String, dynamic> toMap() => {
        'id': id,
        'name': name,
        'address': address,
        'phone': phone,
        'amount': amount,
        'timeSlot': timeSlot,
        'createdAt': createdAt.toIso8601String(),
        'status': status,
      };

  factory PickupItem.fromMap(Map<String, dynamic> map) => PickupItem(
        id: map['id'] as String,
        name: map['name'] as String? ?? '',
        address: map['address'] as String? ?? '',
        phone: map['phone'] as String? ?? '',
        amount: (map['amount'] as num?)?.toInt() ?? 0,
        timeSlot: map['timeSlot'] as String? ?? '',
        createdAt: DateTime.tryParse(map['createdAt'] as String? ?? '') ??
            DateTime.now(),
        status: map['status'] as String? ?? 'Menunggu',
      );
}

/// Repositori keuangan terpadu untuk state runtime persisten (Clean Production).
class FinanceRepository extends ChangeNotifier {
  FinanceRepository._() {
    _loadFromStorage();
  }

  static final FinanceRepository instance = FinanceRepository._();

  static const String _txKey = 'kas_go_transactions_v2';
  static const String _pickupsKey = 'kas_go_pickups_v2';

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
      if (e.period != null) {
        months.add(e.period!);
      }
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

  Future<void> _loadFromStorage() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final rawTx = prefs.getString(_txKey);
      final rawPickups = prefs.getString(_pickupsKey);

      if (rawTx != null && rawTx.isNotEmpty) {
        final list = json.decode(rawTx) as List<dynamic>;
        _items.clear();
        for (final item in list) {
          _items.add(TransactionItem.fromMap(item as Map<String, dynamic>));
        }
      }

      if (rawPickups != null && rawPickups.isNotEmpty) {
        final list = json.decode(rawPickups) as List<dynamic>;
        _pickups.clear();
        for (final item in list) {
          _pickups.add(PickupItem.fromMap(item as Map<String, dynamic>));
        }
      }

      notifyListeners();
      FirebaseSyncService.instance.syncDashboardSummary();
    } catch (e) {
      if (kDebugMode) {
        print('Error loading FinanceRepository: $e');
      }
    }
  }

  Future<void> _persist() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final encodedTx = json.encode(_items.map((e) => e.toMap()).toList());
      final encodedPickups =
          json.encode(_pickups.map((e) => e.toMap()).toList());

      await prefs.setString(_txKey, encodedTx);
      await prefs.setString(_pickupsKey, encodedPickups);
    } catch (e) {
      if (kDebugMode) {
        print('Error persisting FinanceRepository: $e');
      }
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
    String? billingId,
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
      category: 'Iuran Warga',
      billingId: billingId,
    );
    _items.insert(0, item);
    notifyListeners();
    _persist();
    FirebaseSyncService.instance.pushTransactionToCloud(item);
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
      paymentMethod: 'Kas Tunai',
    );
    _items.insert(0, item);
    notifyListeners();
    _persist();
    FirebaseSyncService.instance.pushTransactionToCloud(item);
  }

  /// Koreksi / Edit transaksi kas dengan riwayat Audit Trail transparan
  void editTransaction({
    required String id,
    required int newAmount,
    required String newSummary,
    required String? newCategory,
    required String editorName,
    required String editReason,
  }) {
    final index = _items.indexWhere((e) => e.id == id);
    if (index < 0) return;

    final item = _items[index];
    item.amount = newAmount;
    item.summary = newSummary;
    if (newCategory != null) {
      item.category = newCategory;
    }
    item.editedByName = editorName;
    item.editedAt = DateTime.now();
    item.editReason = editReason;

    notifyListeners();
    _persist();
    FirebaseSyncService.instance.pushTransactionToCloud(item);
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
    _persist();
  }
}
