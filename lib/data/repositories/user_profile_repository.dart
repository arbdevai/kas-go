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
        return 'Warga';
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
    this.password = '',
    this.isLoggedIn = false,
  });

  final String uid;
  String name;
  String email;
  String phone;
  String address;
  String password;
  UserRole role;
  bool isLoggedIn;

  String get roleTitle => role.label;
  bool get isAdmin => role != UserRole.warga;

  Map<String, dynamic> toMap() => {
        'uid': uid,
        'name': name,
        'email': email,
        'phone': phone,
        'address': address,
        'password': password,
        'role': role.code,
        'isLoggedIn': isLoggedIn,
      };

  factory UserProfile.fromMap(Map<String, dynamic> map) => UserProfile(
        uid: map['uid'] as String? ?? 'u_${DateTime.now().millisecondsSinceEpoch}',
        name: map['name'] as String? ?? 'Warga',
        email: map['email'] as String? ?? '',
        phone: map['phone'] as String? ?? '',
        address: map['address'] as String? ?? '',
        password: map['password'] as String? ?? '',
        role: UserRoleExt.fromCode(map['role'] as String? ?? 'warga'),
        isLoggedIn: map['isLoggedIn'] as bool? ?? false,
      );
}

class UserProfileRepository extends ChangeNotifier {
  UserProfileRepository._() {
    _loadFromStorage();
  }

  static final UserProfileRepository instance = UserProfileRepository._();

  static const String _userKey = 'kas_go_user_session_v3';
  static const String _membersListKey = 'kas_go_user_accounts_v3';

  // State pengguna saat ini
  UserProfile _current = UserProfile(
    uid: 'guest',
    name: 'Tamu',
    email: '',
    phone: '',
    address: '',
    role: UserRole.warga,
    isLoggedIn: false,
  );

  final List<UserProfile> _members = [];

  UserProfile get current => _current;
  bool get isAuthenticated => _current.isLoggedIn;
  List<UserProfile> get allMembers => List.unmodifiable(_members);

  Future<void> _loadFromStorage() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final rawUser = prefs.getString(_userKey);
      final rawMembers = prefs.getString(_membersListKey);

      if (rawMembers != null && rawMembers.isNotEmpty) {
        final list = json.decode(rawMembers) as List<dynamic>;
        _members.clear();
        for (final item in list) {
          _members.add(UserProfile.fromMap(item as Map<String, dynamic>));
        }
      }

      if (rawUser != null && rawUser.isNotEmpty) {
        final decoded = json.decode(rawUser) as Map<String, dynamic>;
        _current = UserProfile.fromMap(decoded);
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

  /// Pendaftaran Akun Warga Baru
  Future<String?> register({
    required String name,
    required String phone,
    required String address,
    required String email,
    required String password,
  }) async {
    final cleanEmail = email.trim().toLowerCase();
    final cleanPhone = phone.trim();

    // Cek apakah email atau nomor telepon sudah terdaftar
    final exists = _members.any((m) =>
        (cleanEmail.isNotEmpty && m.email.toLowerCase() == cleanEmail) ||
        (cleanPhone.isNotEmpty && m.phone == cleanPhone));

    if (exists) {
      return 'Email atau nomor WhatsApp sudah terdaftar';
    }

    final newProfile = UserProfile(
      uid: 'u_${DateTime.now().millisecondsSinceEpoch}',
      name: name.trim(),
      phone: cleanPhone,
      address: address.trim(),
      email: cleanEmail,
      password: password,
      role: UserRole.warga,
      isLoggedIn: true,
    );

    _members.add(newProfile);
    _current = newProfile;

    notifyListeners();
    await _persist();
    return null; // Sukses
  }

  /// Masuk / Login Akun
  Future<String?> login({
    required String identifier,
    required String password,
  }) async {
    final cleanId = identifier.trim().toLowerCase();

    // Kredensial Admin Default (untuk setup awal jika belum ada admin)
    if ((cleanId == 'admin' || cleanId == 'admin@kasgo.id') &&
        (password == 'admin123' || password == '123456')) {
      final existingAdmin = _members.firstWhere(
        (m) => m.role == UserRole.admin1,
        orElse: () => UserProfile(
          uid: 'admin_root',
          name: 'Bendahara Utama',
          email: 'admin@kasgo.id',
          phone: '0812-0000-0000',
          address: 'Kantor Kas',
          password: password,
          role: UserRole.admin1,
          isLoggedIn: true,
        ),
      );

      existingAdmin.isLoggedIn = true;
      _current = existingAdmin;
      if (!_members.any((m) => m.uid == existingAdmin.uid)) {
        _members.add(existingAdmin);
      }

      notifyListeners();
      await _persist();
      return null;
    }

    // Cari di daftar anggota
    final match = _members.firstWhere(
      (m) =>
          (m.email.toLowerCase() == cleanId || m.phone == identifier.trim()) &&
          m.password == password,
      orElse: () => UserProfile(
        uid: '',
        name: '',
        email: '',
        phone: '',
        address: '',
        role: UserRole.warga,
      ),
    );

    if (match.uid.isEmpty) {
      return 'Email/WhatsApp atau kata sandi tidak cocok';
    }

    match.isLoggedIn = true;
    _current = match;

    notifyListeners();
    await _persist();
    return null;
  }

  /// Keluar / Logout dari sesi aplikasi
  Future<void> logout() async {
    _current.isLoggedIn = false;
    _current = UserProfile(
      uid: 'guest',
      name: 'Tamu',
      email: '',
      phone: '',
      address: '',
      role: UserRole.warga,
      isLoggedIn: false,
    );

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
      _members[idx].name = _current.name;
      _members[idx].phone = _current.phone;
      _members[idx].address = _current.address;
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

  /// Ganti role akun aktif
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
