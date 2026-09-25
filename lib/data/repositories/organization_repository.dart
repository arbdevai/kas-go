import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Model item metode pembayaran (QRIS, Bank, E-Wallet, atau Custom).
class PaymentMethodItem {
  PaymentMethodItem({
    required this.id,
    required this.type,
    required this.title,
    required this.accountNumber,
    required this.accountName,
    this.isActive = true,
    this.qrImageUrl,
    this.instructions,
  });

  final String id;
  final String type; // 'qris', 'bank', 'ewallet', 'custom'
  String title;
  String accountNumber;
  String accountName;
  bool isActive;
  String? qrImageUrl;
  String? instructions;

  Map<String, dynamic> toMap() => {
        'id': id,
        'type': type,
        'title': title,
        'accountNumber': accountNumber,
        'accountName': accountName,
        'isActive': isActive,
        'qrImageUrl': qrImageUrl,
        'instructions': instructions,
      };

  factory PaymentMethodItem.fromMap(Map<String, dynamic> map) =>
      PaymentMethodItem(
        id: map['id'] as String? ?? 'm_${DateTime.now().millisecondsSinceEpoch}',
        type: map['type'] as String? ?? 'bank',
        title: map['title'] as String? ?? '',
        accountNumber: map['accountNumber'] as String? ?? '',
        accountName: map['accountName'] as String? ?? '',
        isActive: map['isActive'] as bool? ?? true,
        qrImageUrl: map['qrImageUrl'] as String?,
        instructions: map['instructions'] as String?,
      );

  PaymentMethodItem copyWith({
    String? id,
    String? type,
    String? title,
    String? accountNumber,
    String? accountName,
    bool? isActive,
    String? qrImageUrl,
    String? instructions,
  }) {
    return PaymentMethodItem(
      id: id ?? this.id,
      type: type ?? this.type,
      title: title ?? this.title,
      accountNumber: accountNumber ?? this.accountNumber,
      accountName: accountName ?? this.accountName,
      isActive: isActive ?? this.isActive,
      qrImageUrl: qrImageUrl ?? this.qrImageUrl,
      instructions: instructions ?? this.instructions,
    );
  }
}

/// Model konfigurasi organisasi (multi-tenant)
class OrganizationConfig {
  OrganizationConfig({
    required this.orgId,
    required this.name,
    required this.scopeArea,
    this.description = 'Sistem Transparansi Keuangan Kas Karang Taruna',
    this.contactPhone,
    required this.paymentMethods,
  });

  String orgId;
  String name;
  String scopeArea;
  String description;
  String? contactPhone;
  List<PaymentMethodItem> paymentMethods;

  String get fullTitle => '$name $scopeArea'.trim();

  Map<String, dynamic> toMap() => {
        'orgId': orgId,
        'name': name,
        'scopeArea': scopeArea,
        'description': description,
        'contactPhone': contactPhone,
        'paymentMethods': paymentMethods.map((m) => m.toMap()).toList(),
      };

  factory OrganizationConfig.fromMap(Map<String, dynamic> map) {
    final rawList = map['paymentMethods'] as List<dynamic>? ?? [];
    return OrganizationConfig(
      orgId: map['orgId'] as String? ?? 'kt-indonesia',
      name: map['name'] as String? ?? 'Karang Taruna',
      scopeArea: map['scopeArea'] as String? ?? 'Unit Organisasi',
      description: map['description'] as String? ??
          'Sistem Transparansi Keuangan Kas Karang Taruna',
      contactPhone: map['contactPhone'] as String?,
      paymentMethods: rawList
          .map((item) =>
              PaymentMethodItem.fromMap(item as Map<String, dynamic>))
          .toList(),
    );
  }

  static OrganizationConfig initialDefault() {
    return OrganizationConfig(
      orgId: 'kt-pemuda',
      name: 'Karang Taruna',
      scopeArea: 'Unit Pengurus Kas',
      description: 'Sistem Pencatatan dan Transparansi Kas Terbuka',
      contactPhone: '0812-3456-7890',
      paymentMethods: [
        PaymentMethodItem(
          id: 'qris',
          type: 'qris',
          title: 'QRIS Kas Resmi',
          accountNumber: 'NMID-ID1020261928340',
          accountName: 'Kas Karang Taruna',
          isActive: true,
          instructions: 'Pindai kode QRIS menggunakan m-Banking atau E-Wallet apa saja.',
        ),
        PaymentMethodItem(
          id: 'bca',
          type: 'bank',
          title: 'Bank BCA',
          accountNumber: '883019283401',
          accountName: 'Kas Karang Taruna',
          isActive: true,
          instructions: 'Transfer nominal dan cantumkan berita: Iuran Kas - [Nama Anda]',
        ),
        PaymentMethodItem(
          id: 'bri',
          type: 'bank',
          title: 'Bank BRI',
          accountNumber: '',
          accountName: 'Kas Karang Taruna',
          isActive: false,
          instructions: 'Transfer ke rekening kas BRI resmi.',
        ),
        PaymentMethodItem(
          id: 'mandiri',
          type: 'bank',
          title: 'Bank Mandiri',
          accountNumber: '',
          accountName: 'Kas Karang Taruna',
          isActive: false,
          instructions: 'Transfer ke rekening kas Mandiri resmi.',
        ),
        PaymentMethodItem(
          id: 'dana',
          type: 'ewallet',
          title: 'DANA',
          accountNumber: '',
          accountName: 'Kas Karang Taruna',
          isActive: false,
          instructions: 'Kirim saldo DANA ke nomor kas resmi.',
        ),
      ],
    );
  }
}

/// Repository Pengaturan Organisasi Dinamis & Multi-Tenant
class OrganizationRepository extends ChangeNotifier {
  OrganizationRepository._() {
    _loadFromStorage();
  }

  static final OrganizationRepository instance = OrganizationRepository._();

  static const String _prefKey = 'kas_go_organization_config';

  OrganizationConfig _config = OrganizationConfig.initialDefault();

  OrganizationConfig get current => _config;
  String get orgId => _config.orgId;
  String get organizationTitle => _config.fullTitle;
  List<PaymentMethodItem> get activePaymentMethods =>
      _config.paymentMethods.where((m) => m.isActive).toList();

  Future<void> _loadFromStorage() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final raw = prefs.getString(_prefKey);
      if (raw != null && raw.isNotEmpty) {
        final decoded = json.decode(raw) as Map<String, dynamic>;
        _config = OrganizationConfig.fromMap(decoded);
        notifyListeners();
      }
    } catch (e) {
      if (kDebugMode) {
        print('Error loading organization config: $e');
      }
    }
  }

  Future<void> saveConfig({
    required String orgId,
    required String name,
    required String scopeArea,
    String? description,
    String? contactPhone,
  }) async {
    _config.orgId = orgId.trim();
    _config.name = name.trim();
    _config.scopeArea = scopeArea.trim();
    if (description != null) {
      _config.description = description.trim();
    }
    if (contactPhone != null) {
      _config.contactPhone = contactPhone.trim();
    }
    notifyListeners();
    await _persist();
  }

  Future<void> togglePaymentMethod(String id, bool isActive) async {
    for (final method in _config.paymentMethods) {
      if (method.id == id) {
        method.isActive = isActive;
        break;
      }
    }
    notifyListeners();
    await _persist();
  }

  Future<void> updatePaymentMethod(PaymentMethodItem updated) async {
    final index = _config.paymentMethods.indexWhere((m) => m.id == updated.id);
    if (index >= 0) {
      _config.paymentMethods[index] = updated;
    } else {
      _config.paymentMethods.add(updated);
    }
    notifyListeners();
    await _persist();
  }

  Future<void> deletePaymentMethod(String id) async {
    _config.paymentMethods.removeWhere((m) => m.id == id);
    notifyListeners();
    await _persist();
  }

  Future<void> _persist() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final encoded = json.encode(_config.toMap());
      await prefs.setString(_prefKey, encoded);
    } catch (e) {
      if (kDebugMode) {
        print('Error saving organization config: $e');
      }
    }
  }
}
