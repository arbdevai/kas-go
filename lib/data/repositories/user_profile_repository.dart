import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

enum UserRole {
  admin1,
  admin2,
  admin3,
  warga,
}

extension UserRoleExt on UserRole {
  String get code => name;

  String get label {
    switch (this) {
      case UserRole.admin1:
        return 'Admin 1 (Bendahara)';
      case UserRole.admin2:
        return 'Admin 2 (Sekretaris)';
      case UserRole.admin3:
        return 'Admin 3 (Koordinator Lapangan)';
      case UserRole.warga:
        return 'Warga Karang Taruna';
    }
  }

  static UserRole fromCode(String code) {
    switch (code) {
      case 'admin1':
        return UserRole.admin1;
      case 'admin2':
        return UserRole.admin2;
      case 'admin3':
        return UserRole.admin3;
      case 'warga':
      default:
        return UserRole.warga;
    }
  }
}

class UserProfile {
  UserProfile({
    required this.uid,
    required this.name,
    required this.email,
    required this.phone,
    required this.address,
    required this.role,
    this.isGoogleAccount = false,
    this.isOnboarded = false,
  });

  final String uid;
  String name;
  String email;
  String phone;
  String address;
  UserRole role;
  bool isGoogleAccount;
  bool isOnboarded;

  String get roleTitle => role.label;
  bool get isAdmin => role != UserRole.warga;

  Map<String, dynamic> toMap() => {
        'uid': uid,
        'name': name,
        'email': email,
        'phone': phone,
        'address': address,
        'role': role.code,
        'isGoogleAccount': isGoogleAccount,
        'isOnboarded': isOnboarded,
      };

  factory UserProfile.fromMap(Map<String, dynamic> map) => UserProfile(
        uid: map['uid'] as String? ?? 'u_${DateTime.now().millisecondsSinceEpoch}',
        name: map['name'] as String? ?? 'Warga Karang Taruna',
        email: map['email'] as String? ?? '',
        phone: map['phone'] as String? ?? '',
        address: map['address'] as String? ?? '',
        role: UserRoleExt.fromCode(map['role'] as String? ?? 'warga'),
        isGoogleAccount: map['isGoogleAccount'] as bool? ?? false,
        isOnboarded: map['isOnboarded'] as bool? ?? false,
      );
}

class UserProfileRepository extends ChangeNotifier {
  UserProfileRepository._() {
    _loadFromStorage();
  }

  static final UserProfileRepository instance = UserProfileRepository._();

  static const String _userKey = 'kas_go_user_profile_v2';
  static const String _membersListKey = 'kas_go_registered_members_v2';

  // Default awal adalah WARGA (Aman & Tidak punya akses admin)
  UserProfile _current = UserProfile(
    uid: 'u_default',
    name: 'Warga Karang Taruna',
    email: '',
    phone: '',
    address: '',
    role: UserRole.warga,
    isGoogleAccount: false,
    isOnboarded: false,
  );

  final List<UserProfile> _members = [];

  UserProfile get current => _current;
  List<UserProfile> get allMembers => List.unmodifiable(_members);

  Future<void> _loadFromStorage() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final rawUser = prefs.getString(_userKey);
      final rawMembers = prefs.getString(_membersListKey);

      if (rawUser != null && rawUser.isNotEmpty) {
        final decoded = json.decode(rawUser) as Map<String, dynamic>;
        _current = UserProfile.fromMap(decoded);
      }

      if (rawMembers != null && rawMembers.isNotEmpty) {
        final list = json.decode(rawMembers) as List<dynamic>;
        _members.clear();
        for (final item in list) {
          _members.add(UserProfile.fromMap(item as Map<String, dynamic>));
        }
      }

      notifyListeners();
    } catch (e) {
      if (kDebugMode) {
        print('Error loading UserProfileRepository: $e');
      }
    }
  }

  Future<void> _persist() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final userJson = json.encode(_current.toMap());
      await prefs.setString(_userKey, userJson);

      final membersJson =
          json.encode(_members.map((m) => m.toMap()).toList());
      await prefs.setString(_membersListKey, membersJson);
    } catch (e) {
      if (kDebugMode) {
        print('Error saving UserProfileRepository: $e');
      }
    }
  }

  /// Login atau tautkan akun Google
  Future<void> linkGoogleAccount({
    required String googleName,
    required String googleEmail,
  }) async {
    _current.name = googleName;
    _current.email = googleEmail;
    _current.isGoogleAccount = true;
    notifyListeners();
    await _persist();
  }

  /// Selesaikan modal onboarding (Wajib Nama, Nomor WhatsApp, Alamat)
  Future<void> completeOnboarding({
    required String name,
    required String phone,
    required String address,
  }) async {
    _current.name = name.trim();
    _current.phone = phone.trim();
    _current.address = address.trim();
    _current.isOnboarded = true;

    // Masukkan ke daftar anggota terdaftar
    final idx = _members.indexWhere((m) => m.uid == _current.uid);
    if (idx >= 0) {
      _members[idx] = _current;
    } else {
      _members.add(_current);
    }

    notifyListeners();
    await _persist();
  }

  /// Update data profil pengguna
  Future<void> updateProfile({
    required String name,
    required String phone,
    required String address,
  }) async {
    _current.name = name.trim();
    _current.phone = phone.trim();
    _current.address = address.trim();

    final idx = _members.indexWhere((m) => m.uid == _current.uid);
    if (idx >= 0) {
      _members[idx] = _current;
    }

    notifyListeners();
    await _persist();
  }

  /// Admin mengubah role anggota lain di halaman Admin
  Future<void> updateRoleForMember(String memberUid, UserRole newRole) async {
    final idx = _members.indexWhere((m) => m.uid == memberUid);
    if (idx >= 0) {
      _members[idx].role = newRole;
      if (_current.uid == memberUid) {
        _current.role = newRole;
      }
      notifyListeners();
      await _persist();
    }
  }

  /// Khusus untuk pengaturan awal / inisialisasi pengurus pertama
  Future<void> switchRole(UserRole newRole) async {
    _current.role = newRole;
    final idx = _members.indexWhere((m) => m.uid == _current.uid);
    if (idx >= 0) {
      _members[idx].role = newRole;
    }
    notifyListeners();
    await _persist();
  }
}
