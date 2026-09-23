import 'package:flutter/foundation.dart';

enum UserRole {
  admin1,
  admin2,
  admin3,
  warga,
}

class UserProfile {
  UserProfile({
    required this.name,
    required this.email,
    required this.phone,
    required this.address,
    required this.role,
    this.isGoogleAccount = false,
  });

  String name;
  String email;
  String phone;
  String address;
  UserRole role;
  bool isGoogleAccount;

  String get roleTitle {
    switch (role) {
      case UserRole.admin1:
        return 'Admin 1 (Bendahara)';
      case UserRole.admin2:
        return 'Admin 2 (Sekretaris)';
      case UserRole.admin3:
        return 'Admin 3 (Koordinator Lapangan)';
      case UserRole.warga:
        return 'Warga Terdaftar';
    }
  }

  bool get isAdmin => role != UserRole.warga;
}

class UserProfileRepository extends ChangeNotifier {
  UserProfileRepository._();

  static final UserProfileRepository instance = UserProfileRepository._();

  final UserProfile _current = UserProfile(
    name: 'Ahmad Fauzi',
    email: 'ahmad.fauzi@gmail.com',
    phone: '0812-9876-5432',
    address: 'Jl. Melati No. 8 RT 02 / RW 05',
    role: UserRole.admin1,
    isGoogleAccount: false,
  );

  UserProfile get current => _current;

  void updateProfile({
    required String name,
    required String phone,
    required String address,
  }) {
    _current.name = name;
    _current.phone = phone;
    _current.address = address;
    notifyListeners();
  }

  void switchRole(UserRole newRole) {
    _current.role = newRole;
    notifyListeners();
  }

  void linkGoogleAccount({
    required String googleName,
    required String googleEmail,
  }) {
    _current.name = googleName;
    _current.email = googleEmail;
    _current.isGoogleAccount = true;
    notifyListeners();
  }
}
