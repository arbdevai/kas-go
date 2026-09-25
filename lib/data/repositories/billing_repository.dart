import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'finance_repository.dart';

/// Status tagihan iuran per warga
enum BillPaymentStatus {
  belumBayar,
  menungguVerifikasi,
  lunas,
}

extension BillPaymentStatusExt on BillPaymentStatus {
  String get label {
    switch (this) {
      case BillPaymentStatus.belumBayar:
        return 'Belum Bayar';
      case BillPaymentStatus.menungguVerifikasi:
        return 'Menunggu Verifikasi';
      case BillPaymentStatus.lunas:
        return 'Lunas';
    }
  }

  String get code {
    switch (this) {
      case BillPaymentStatus.belumBayar:
        return 'belum_bayar';
      case BillPaymentStatus.menungguVerifikasi:
        return 'menunggu_verifikasi';
      case BillPaymentStatus.lunas:
        return 'lunas';
    }
  }

  static BillPaymentStatus fromCode(String code) {
    switch (code) {
      case 'lunas':
        return BillPaymentStatus.lunas;
      case 'menunggu_verifikasi':
        return BillPaymentStatus.menungguVerifikasi;
      case 'belum_bayar':
      default:
        return BillPaymentStatus.belumBayar;
    }
  }
}

/// Model Tagihan Bulanan Resmi yang Diterbitkan Admin
class MonthlyBill {
  MonthlyBill({
    required this.id,
    required this.title,
    required this.period, // Format: 'YYYY-MM', cth: '2026-09'
    required this.amount,
    required this.dueDate,
    required this.createdByName,
    required this.createdAt,
    this.description,
    this.isPublished = true,
  });

  final String id;
  final String title;
  final String period;
  final int amount;
  final DateTime dueDate;
  final String createdByName;
  final DateTime createdAt;
  final String? description;
  final bool isPublished;

  Map<String, dynamic> toMap() => {
        'id': id,
        'title': title,
        'period': period,
        'amount': amount,
        'dueDate': dueDate.toIso8601String(),
        'createdByName': createdByName,
        'createdAt': createdAt.toIso8601String(),
        'description': description,
        'isPublished': isPublished,
      };

  factory MonthlyBill.fromMap(Map<String, dynamic> map) => MonthlyBill(
        id: map['id'] as String,
        title: map['title'] as String? ?? 'Iuran Kas',
        period: map['period'] as String? ?? '',
        amount: (map['amount'] as num?)?.toInt() ?? 0,
        dueDate: DateTime.tryParse(map['dueDate'] as String? ?? '') ??
            DateTime.now(),
        createdByName: map['createdByName'] as String? ?? 'Admin',
        createdAt: DateTime.tryParse(map['createdAt'] as String? ?? '') ??
            DateTime.now(),
        description: map['description'] as String?,
        isPublished: map['isPublished'] as bool? ?? true,
      );
}

/// Model Status Tagihan per Warga
class MemberBillEntry {
  MemberBillEntry({
    required this.id,
    required this.billId,
    required this.memberId,
    required this.memberName,
    required this.amount,
    required this.period,
    this.status = BillPaymentStatus.belumBayar,
    this.paidAt,
    this.paymentMethod,
    this.recordedByName,
    this.transactionId,
  });

  final String id;
  final String billId;
  final String memberId;
  final String memberName;
  final int amount;
  final String period;
  BillPaymentStatus status;
  DateTime? paidAt;
  String? paymentMethod;
  String? recordedByName;
  String? transactionId;

  bool get isPaid => status == BillPaymentStatus.lunas;

  Map<String, dynamic> toMap() => {
        'id': id,
        'billId': billId,
        'memberId': memberId,
        'memberName': memberName,
        'amount': amount,
        'period': period,
        'status': status.code,
        'paidAt': paidAt?.toIso8601String(),
        'paymentMethod': paymentMethod,
        'recordedByName': recordedByName,
        'transactionId': transactionId,
      };

  factory MemberBillEntry.fromMap(Map<String, dynamic> map) => MemberBillEntry(
        id: map['id'] as String,
        billId: map['billId'] as String,
        memberId: map['memberId'] as String,
        memberName: map['memberName'] as String? ?? 'Warga',
        amount: (map['amount'] as num?)?.toInt() ?? 0,
        period: map['period'] as String? ?? '',
        status: BillPaymentStatusExt.fromCode(map['status'] as String? ?? ''),
        paidAt: map['paidAt'] != null
            ? DateTime.tryParse(map['paidAt'] as String)
            : null,
        paymentMethod: map['paymentMethod'] as String?,
        recordedByName: map['recordedByName'] as String?,
        transactionId: map['transactionId'] as String?,
      );
}

/// Rekapitulasi Tagihan Iuran Warga
class BillingRecap {
  BillingRecap({
    required this.bill,
    required this.totalMembers,
    required this.paidCount,
    required this.unpaidCount,
    required this.totalCollected,
    required this.totalTarget,
  });

  final MonthlyBill bill;
  final int totalMembers;
  final int paidCount;
  final int unpaidCount;
  final int totalCollected;
  final int totalTarget;

  double get collectionPercentage =>
      totalTarget > 0 ? (totalCollected / totalTarget) * 100 : 0.0;
}

/// Repository Manajemen Tagihan Bulanan & Rekapitulasi Iuran
class BillingRepository extends ChangeNotifier {
  BillingRepository._() {
    _loadFromStorage();
  }

  static final BillingRepository instance = BillingRepository._();

  static const String _billsKey = 'kas_go_monthly_bills';
  static const String _entriesKey = 'kas_go_member_bill_entries';

  final List<MonthlyBill> _bills = [];
  final List<MemberBillEntry> _entries = [];

  List<MonthlyBill> get allBills => List.unmodifiable(_bills);
  List<MemberBillEntry> get allEntries => List.unmodifiable(_entries);

  /// Ambil daftar tagihan untuk warga tertentu (berdasarkan nama/ID)
  List<MemberBillEntry> getBillsForMember(String memberNameOrId) {
    return _entries
        .where((e) =>
            e.memberId == memberNameOrId ||
            e.memberName.toLowerCase().contains(memberNameOrId.toLowerCase()))
        .toList();
  }

  /// Ambil entri tagihan untuk periode tertentu
  List<MemberBillEntry> getEntriesForBill(String billId) {
    return _entries.where((e) => e.billId == billId).toList();
  }

  /// Dapatkan rekapitulasi iuran untuk sebuah tagihan bulanan
  BillingRecap getRecapForBill(MonthlyBill bill) {
    final entries = getEntriesForBill(bill.id);
    final totalMembers = entries.length;
    final paidEntries = entries.where((e) => e.isPaid).toList();
    final paidCount = paidEntries.length;
    final unpaidCount = totalMembers - paidCount;
    final totalCollected =
        paidEntries.fold(0, (sum, item) => sum + item.amount);
    final totalTarget = totalMembers * bill.amount;

    return BillingRecap(
      bill: bill,
      totalMembers: totalMembers,
      paidCount: paidCount,
      unpaidCount: unpaidCount,
      totalCollected: totalCollected,
      totalTarget: totalTarget,
    );
  }

  /// Dapatkan ringkasan seluruh tagihan bulanan (Global Rekap Tagihan)
  List<BillingRecap> getAllRecaps() {
    return _bills.map((b) => getRecapForBill(b)).toList();
  }

  /// Terbitkan Tagihan Bulanan Baru oleh Admin
  Future<MonthlyBill> publishNewBill({
    required String title,
    required String period,
    required int amount,
    required DateTime dueDate,
    required String createdByName,
    String? description,
    required List<String> memberNames,
  }) async {
    final billId = 'bill_${DateTime.now().millisecondsSinceEpoch}';
    final bill = MonthlyBill(
      id: billId,
      title: title,
      period: period,
      amount: amount,
      dueDate: dueDate,
      createdByName: createdByName,
      createdAt: DateTime.now(),
      description: description,
    );

    _bills.insert(0, bill);

    // Otomatis terbitkan entri tagihan ke setiap warga aktif
    for (final name in memberNames) {
      if (name.trim().isEmpty) continue;
      _entries.add(
        MemberBillEntry(
          id: 'mb_${DateTime.now().millisecondsSinceEpoch}_${name.hashCode}',
          billId: billId,
          memberId: 'mem_${name.hashCode}',
          memberName: name.trim(),
          amount: amount,
          period: period,
          status: BillPaymentStatus.belumBayar,
        ),
      );
    }

    notifyListeners();
    await _persist();
    return bill;
  }

  /// Verifikasi Pembayaran Tagihan Warga oleh Admin:
  /// 1. Ubah status tagihan jadi LUNAS
  /// 2. Otomatis catat kas masuk ke Buku Kas (FinanceRepository)
  /// 3. Total saldo kas langsung bertambah!
  Future<void> confirmBillPayment({
    required String entryId,
    required String paymentMethod,
    required String verifiedByName,
  }) async {
    final index = _entries.indexWhere((e) => e.id == entryId);
    if (index < 0) return;

    final entry = _entries[index];
    if (entry.isPaid) return; // Sudah lunas

    final now = DateTime.now();
    entry.status = BillPaymentStatus.lunas;
    entry.paidAt = now;
    entry.paymentMethod = paymentMethod;
    entry.recordedByName = verifiedByName;

    // OTOMATIS CATAT KAS MASUK KE BUKU KAS & UPDATE SALDO
    FinanceRepository.instance.recordIncome(
      memberName: entry.memberName,
      period: entry.period,
      amount: entry.amount,
      paymentMethod: paymentMethod,
      recorderName: verifiedByName,
      note: 'Iuran ${entry.period} (Lunas Terverifikasi)',
      billingId: entry.billId,
    );

    notifyListeners();
    await _persist();
  }

  /// Ajukan konfirmasi bayar oleh warga (status -> menunggu verifikasi)
  Future<void> requestPaymentVerification({
    required String entryId,
    required String paymentMethod,
  }) async {
    final index = _entries.indexWhere((e) => e.id == entryId);
    if (index < 0) return;

    final entry = _entries[index];
    entry.status = BillPaymentStatus.menungguVerifikasi;
    entry.paymentMethod = paymentMethod;
    notifyListeners();
    await _persist();
  }

  Future<void> _loadFromStorage() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final rawBills = prefs.getString(_billsKey);
      final rawEntries = prefs.getString(_entriesKey);

      if (rawBills != null && rawBills.isNotEmpty) {
        final list = json.decode(rawBills) as List<dynamic>;
        _bills.clear();
        for (final item in list) {
          _bills.add(MonthlyBill.fromMap(item as Map<String, dynamic>));
        }
      }

      if (rawEntries != null && rawEntries.isNotEmpty) {
        final list = json.decode(rawEntries) as List<dynamic>;
        _entries.clear();
        for (final item in list) {
          _entries.add(MemberBillEntry.fromMap(item as Map<String, dynamic>));
        }
      }

      notifyListeners();
    } catch (e) {
      if (kDebugMode) {
        print('Error loading billing repository: $e');
      }
    }
  }

  Future<void> _persist() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final encodedBills = json.encode(_bills.map((b) => b.toMap()).toList());
      final encodedEntries =
          json.encode(_entries.map((e) => e.toMap()).toList());

      await prefs.setString(_billsKey, encodedBills);
      await prefs.setString(_entriesKey, encodedEntries);
    } catch (e) {
      if (kDebugMode) {
        print('Error saving billing repository: $e');
      }
    }
  }
}
