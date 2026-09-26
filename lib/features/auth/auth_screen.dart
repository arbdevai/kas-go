import 'package:flutter/material.dart';
import '../../core/config/app_config.dart';
import '../../core/constants/app_colors.dart';
import '../../core/utils/app_toast.dart';
import '../../data/repositories/organization_repository.dart';
import '../../data/repositories/user_profile_repository.dart';

/// Layar Masuk Utama (Auth Gate).
///
/// Pengguna wajib masuk sebelum dapat mengakses pembukuan dan riwayat kas.
class AuthScreen extends StatefulWidget {
  const AuthScreen({super.key, required this.config});

  final AppConfig config;

  @override
  State<AuthScreen> createState() => _AuthScreenState();
}

class _AuthScreenState extends State<AuthScreen> {
  final _userRepo = UserProfileRepository.instance;
  final _orgRepo = OrganizationRepository.instance;

  void _showGoogleAuthFlow(BuildContext context) {
    final emailCtrl = TextEditingController();
    final formKey = GlobalKey<FormState>();

    showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      backgroundColor: Colors.white,
      builder: (ctx) {
        return Padding(
          padding: EdgeInsets.only(
            left: 20,
            right: 20,
            top: 24,
            bottom: MediaQuery.of(ctx).viewInsets.bottom + 24,
          ),
          child: Form(
            key: formKey,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Row(
                      children: [
                        _buildGoogleIcon(size: 24),
                        const SizedBox(width: 10),
                        const Text(
                          'Masuk Akun Google',
                          style: TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                            color: AppColors.textPrimaryLight,
                          ),
                        ),
                      ],
                    ),
                    IconButton(
                      icon: const Icon(Icons.close, size: 20),
                      onPressed: () => Navigator.pop(ctx),
                    ),
                  ],
                ),
                const SizedBox(height: 6),
                const Text(
                  'Masukkan alamat email Google untuk verifikasi akun:',
                  style: TextStyle(
                    fontSize: 12,
                    color: AppColors.textSecondaryLight,
                  ),
                ),
                const SizedBox(height: 16),
                const Text(
                  'Email Google (@gmail.com)',
                  style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 6),
                TextFormField(
                  controller: emailCtrl,
                  keyboardType: TextInputType.emailAddress,
                  autofocus: true,
                  decoration: const InputDecoration(
                    hintText: 'nama.anda@gmail.com',
                    prefixIcon: Icon(Icons.email_outlined, size: 20),
                  ),
                  validator: (v) {
                    if (v == null || v.trim().isEmpty) {
                      return 'Email wajib diisi';
                    }
                    if (!v.contains('@') || !v.contains('.')) {
                      return 'Format email tidak valid';
                    }
                    return null;
                  },
                ),
                const SizedBox(height: 20),
                SizedBox(
                  width: double.infinity,
                  height: 48,
                  child: ElevatedButton(
                    onPressed: () async {
                      if (!formKey.currentState!.validate()) return;
                      final email = emailCtrl.text.trim();
                      Navigator.pop(ctx);

                      // Cek apakah akun sudah terdaftar
                      final existingUser = _userRepo.checkGoogleAccount(email);
                      if (existingUser != null) {
                        // USER LAMA -> Langsung login
                        await _userRepo.loginWithExistingGoogle(existingUser);
                        if (mounted) {
                          AppToast.success(
                            context,
                            'Selamat datang kembali, ${existingUser.name}',
                          );
                        }
                      } else {
                        // USER BARU -> Buka formulir nama & nomor WhatsApp
                        if (mounted) {
                          _showNewUserOnboarding(context, email);
                        }
                      }
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.primaryRoyal,
                      foregroundColor: Colors.white,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(14),
                      ),
                    ),
                    child: const Text(
                      'Lanjutkan',
                      style: TextStyle(fontWeight: FontWeight.bold),
                    ),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  void _showNewUserOnboarding(BuildContext context, String googleEmail) {
    final nameCtrl = TextEditingController();
    final phoneCtrl = TextEditingController();
    final addressCtrl = TextEditingController();
    final formKey = GlobalKey<FormState>();

    showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      isDismissible: false,
      enableDrag: false,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      backgroundColor: Colors.white,
      builder: (ctx) {
        return Padding(
          padding: EdgeInsets.only(
            left: 20,
            right: 20,
            top: 24,
            bottom: MediaQuery.of(ctx).viewInsets.bottom + 24,
          ),
          child: Form(
            key: formKey,
            child: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Container(
                        width: 4,
                        height: 18,
                        decoration: BoxDecoration(
                          color: AppColors.primaryRoyal,
                          borderRadius: BorderRadius.circular(2),
                        ),
                      ),
                      const SizedBox(width: 8),
                      const Text(
                        'Pendaftaran Warga Baru',
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                          color: AppColors.textPrimaryLight,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 6),
                  Text(
                    'Akun: $googleEmail',
                    style: const TextStyle(
                      fontSize: 12,
                      color: AppColors.primaryRoyal,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Nama Lengkap
                  const Text('Nama Lengkap',
                      style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 4),
                  TextFormField(
                    controller: nameCtrl,
                    decoration: const InputDecoration(
                      hintText: 'Nama lengkap sesuai KTP',
                      prefixIcon: Icon(Icons.person_outline, size: 20),
                    ),
                    validator: (v) =>
                        (v == null || v.trim().isEmpty) ? 'Nama wajib diisi' : null,
                  ),
                  const SizedBox(height: 14),

                  // Nomor WhatsApp
                  const Text('Nomor WhatsApp',
                      style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 4),
                  TextFormField(
                    controller: phoneCtrl,
                    keyboardType: TextInputType.phone,
                    decoration: const InputDecoration(
                      hintText: 'Contoh: 0812-3456-7890',
                      prefixIcon: Icon(Icons.phone_outlined, size: 20),
                    ),
                    validator: (v) =>
                        (v == null || v.trim().isEmpty) ? 'Nomor WhatsApp wajib diisi' : null,
                  ),
                  const SizedBox(height: 14),

                  // Alamat Rumah
                  const Text('Alamat & RT/RW',
                      style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 4),
                  TextFormField(
                    controller: addressCtrl,
                    decoration: const InputDecoration(
                      hintText: 'Contoh: RT 02 / RW 05',
                      prefixIcon: Icon(Icons.location_on_outlined, size: 20),
                    ),
                    validator: (v) =>
                        (v == null || v.trim().isEmpty) ? 'Alamat wajib diisi' : null,
                  ),
                  const SizedBox(height: 22),

                  // Tombol Selesai
                  SizedBox(
                    width: double.infinity,
                    height: 48,
                    child: ElevatedButton(
                      onPressed: () async {
                        if (!formKey.currentState!.validate()) return;
                        final name = nameCtrl.text.trim();
                        final phone = phoneCtrl.text.trim();
                        final address = addressCtrl.text.trim();

                        final err = await _userRepo.registerWithGoogle(
                          email: googleEmail,
                          name: name,
                          phone: phone,
                          address: address,
                        );

                        if (!context.mounted) return;
                        if (err != null) {
                          AppToast.error(context, err);
                        } else {
                          Navigator.pop(ctx);
                          AppToast.success(
                            context,
                            'Pendaftaran berhasil. Selamat datang, $name!',
                          );
                        }
                      },
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.incomeGreen,
                        foregroundColor: Colors.white,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(14),
                        ),
                      ),
                      child: const Text(
                        'Daftar & Masuk',
                        style: TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }

  void _showAdminPinModal(BuildContext context) {
    final pinCtrl = TextEditingController();
    showDialog<void>(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        title: const Text(
          'Akses Pengurus',
          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Masukkan PIN akses pengurus untuk mengelola kas organisasi:',
              style: TextStyle(fontSize: 12, color: AppColors.textSecondaryLight),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: pinCtrl,
              keyboardType: TextInputType.number,
              obscureText: true,
              decoration: const InputDecoration(
                hintText: 'PIN Pengurus (Default: 123456)',
                prefixIcon: Icon(Icons.lock_outline),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Batal'),
          ),
          FilledButton(
            onPressed: () async {
              final pin = pinCtrl.text.trim();
              final success = await _userRepo.loginWithAdminPin(pin);
              if (!ctx.mounted) return;
              Navigator.pop(ctx);
              if (success) {
                AppToast.success(context, 'Masuk sebagai Bendahara Kas');
              } else {
                AppToast.error(context, 'PIN pengurus tidak valid');
              }
            },
            child: const Text('Masuk'),
          ),
        ],
      ),
    );
  }

  Widget _buildGoogleIcon({double size = 20}) {
    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        color: Colors.white,
        shape: BoxShape.circle,
        border: Border.all(color: AppColors.borderSubtle, width: 0.8),
      ),
      child: Center(
        child: Text(
          'G',
          style: TextStyle(
            color: const Color(0xFF4285F4),
            fontWeight: FontWeight.w900,
            fontSize: size * 0.65,
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundLight,
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 32),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                // Logo Kas Go
                Container(
                  width: 80,
                  height: 80,
                  decoration: BoxDecoration(
                    gradient: const LinearGradient(
                      colors: [
                        AppColors.heroPurpleStart,
                        AppColors.heroPurpleEnd,
                      ],
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                    ),
                    shape: BoxShape.circle,
                    boxShadow: [
                      BoxShadow(
                        color: AppColors.primaryRoyal.withOpacity(0.25),
                        blurRadius: 20,
                        offset: const Offset(0, 8),
                      ),
                    ],
                  ),
                  child: const Center(
                    child: Icon(
                      Icons.account_balance_wallet,
                      color: AppColors.accentGold,
                      size: 38,
                    ),
                  ),
                ),
                const SizedBox(height: 18),

                // Judul Aplikasi
                const Text(
                  'Kas Go',
                  style: TextStyle(
                    fontSize: 28,
                    fontWeight: FontWeight.w900,
                    letterSpacing: -0.5,
                    color: AppColors.textPrimaryLight,
                  ),
                ),
                const SizedBox(height: 4),
                AnimatedBuilder(
                  animation: _orgRepo,
                  builder: (context, _) {
                    return Text(
                      _orgRepo.organizationTitle.isNotEmpty
                          ? _orgRepo.organizationTitle
                          : 'Aplikasi Kas Karang Taruna',
                      style: const TextStyle(
                        fontSize: 13,
                        fontWeight: FontWeight.w500,
                        color: AppColors.textSecondaryLight,
                      ),
                      textAlign: TextAlign.center,
                    );
                  },
                ),
                const SizedBox(height: 36),

                // Card Utama Masuk
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(24),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(24),
                    border: Border.all(color: AppColors.borderSubtle),
                    boxShadow: [
                      BoxShadow(
                        color: Colors.black.withOpacity(0.03),
                        blurRadius: 16,
                        offset: const Offset(0, 6),
                      ),
                    ],
                  ),
                  child: Column(
                    children: [
                      const Text(
                        'Silakan masuk untuk melanjutkan',
                        style: TextStyle(
                          fontSize: 13,
                          fontWeight: FontWeight.w600,
                          color: AppColors.textSecondaryLight,
                        ),
                      ),
                      const SizedBox(height: 20),

                      // Tombol Masuk dengan Google Nyata
                      SizedBox(
                        width: double.infinity,
                        height: 52,
                        child: OutlinedButton(
                          onPressed: () => _showGoogleAuthFlow(context),
                          style: OutlinedButton.styleFrom(
                            side: const BorderSide(
                              color: AppColors.primaryRoyal,
                              width: 1.5,
                            ),
                            backgroundColor: Colors.white,
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(16),
                            ),
                          ),
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              _buildGoogleIcon(size: 24),
                              const SizedBox(width: 12),
                              const Text(
                                'Masuk dengan Google',
                                style: TextStyle(
                                  fontSize: 14,
                                  fontWeight: FontWeight.bold,
                                  color: AppColors.primaryRoyal,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                      const SizedBox(height: 18),

                      // Pembatas
                      Row(
                        children: [
                          const Expanded(child: Divider(color: Color(0xFFF1F5F9))),
                          Padding(
                            padding: const EdgeInsets.symmetric(horizontal: 10),
                            child: Text(
                              'atau',
                              style: TextStyle(
                                fontSize: 11,
                                color: Colors.grey.shade400,
                              ),
                            ),
                          ),
                          const Expanded(child: Divider(color: Color(0xFFF1F5F9))),
                        ],
                      ),
                      const SizedBox(height: 14),

                      // Tombol Masuk sebagai Pengurus
                      TextButton.icon(
                        onPressed: () => _showAdminPinModal(context),
                        icon: const Icon(
                          Icons.admin_panel_settings_outlined,
                          size: 18,
                          color: AppColors.primaryRoyal,
                        ),
                        label: const Text(
                          'Masuk sebagai Pengurus',
                          style: TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.bold,
                            color: AppColors.primaryRoyal,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 32),

                // Footer
                const Text(
                  'Kas Go • Versi 1.0.5',
                  style: TextStyle(
                    fontSize: 11,
                    color: AppColors.textMutedLight,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
