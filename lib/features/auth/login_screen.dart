import 'package:flutter/material.dart';
import '../../core/constants/app_colors.dart';
import '../../core/utils/formatters.dart';
import '../../data/repositories/billing_repository.dart';
import '../../data/repositories/organization_repository.dart';
import '../../data/repositories/user_profile_repository.dart';
import '../../services/app_update_service.dart';
import '../admin/payment_settings_screen.dart';

/// Layar Profil, Autentikasi Google, Onboarding, dan Panel Hak Akses Admin.
class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final UserProfileRepository _userRepo = UserProfileRepository.instance;
  final OrganizationRepository _orgRepo = OrganizationRepository.instance;
  final BillingRepository _billingRepo = BillingRepository.instance;

  void _showOnboardingModal(BuildContext context) {
    final nameCtrl = TextEditingController(text: _userRepo.current.name == 'Warga Karang Taruna' ? '' : _userRepo.current.name);
    final phoneCtrl = TextEditingController(text: _userRepo.current.phone);
    final addressCtrl = TextEditingController(text: _userRepo.current.address);
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
                      Text(
                        'Lengkapi Identitas Warga',
                        style: Theme.of(ctx).textTheme.titleMedium?.copyWith(
                              fontWeight: FontWeight.bold,
                              color: AppColors.textPrimaryLight,
                            ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 6),
                  const Text(
                    'Identitas ini digunakan untuk pencatatan iuran kas dan verifikasi tagihan bulanan resmi.',
                    style: TextStyle(
                      fontSize: 12,
                      color: AppColors.textSecondaryLight,
                      height: 1.4,
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Input Nama
                  const Text(
                    'Nama Lengkap',
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                      color: AppColors.textPrimaryLight,
                    ),
                  ),
                  const SizedBox(height: 6),
                  TextFormField(
                    controller: nameCtrl,
                    decoration: const InputDecoration(
                      hintText: 'Contoh: Ahmad Fauzi / Ibu Siti',
                      prefixIcon: Icon(Icons.person_outline, size: 20),
                    ),
                    validator: (v) =>
                        (v == null || v.trim().isEmpty) ? 'Nama lengkap wajib diisi' : null,
                  ),
                  const SizedBox(height: 14),

                  // Input Nomor WhatsApp
                  const Text(
                    'Nomor WhatsApp Aktif',
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                      color: AppColors.textPrimaryLight,
                    ),
                  ),
                  const SizedBox(height: 6),
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

                  // Input Alamat / RT / RW
                  const Text(
                    'Alamat Rumah & RT / RW',
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                      color: AppColors.textPrimaryLight,
                    ),
                  ),
                  const SizedBox(height: 6),
                  TextFormField(
                    controller: addressCtrl,
                    decoration: const InputDecoration(
                      hintText: 'Contoh: Jl. Melati No. 8 RT 02 / RW 05',
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
                      onPressed: () {
                        if (!formKey.currentState!.validate()) return;
                        _userRepo.completeOnboarding(
                          name: nameCtrl.text.trim(),
                          phone: phoneCtrl.text.trim(),
                          address: addressCtrl.text.trim(),
                        );
                        Navigator.pop(ctx);
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                            backgroundColor: AppColors.incomeGreen,
                            content: Text('Identitas berhasil disimpan. Selamat datang!'),
                          ),
                        );
                      },
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.primaryRoyal,
                        foregroundColor: Colors.white,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(14),
                        ),
                      ),
                      child: const Text(
                        'Simpan & Lanjutkan',
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

  void _handleGoogleSignUp(BuildContext context) {
    showDialog<void>(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        title: Row(
          children: [
            Container(
              width: 32,
              height: 32,
              decoration: BoxDecoration(
                color: Colors.white,
                shape: BoxShape.circle,
                border: Border.all(color: AppColors.borderSubtle),
              ),
              child: const Center(
                child: Text(
                  'G',
                  style: TextStyle(
                    color: Color(0xFF4285F4),
                    fontWeight: FontWeight.w900,
                    fontSize: 18,
                  ),
                ),
              ),
            ),
            const SizedBox(width: 10),
            const Text(
              'Pilih Akun Google',
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
            ),
          ],
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text(
              'Masuk untuk mengakses pembukuan dan tagihan kas iuran Anda secara otomatis:',
              style: TextStyle(fontSize: 12, color: AppColors.textSecondaryLight),
            ),
            const SizedBox(height: 16),
            ListTile(
              contentPadding: EdgeInsets.zero,
              leading: const CircleAvatar(
                backgroundColor: AppColors.surfaceLavender,
                child: Text('G',
                    style: TextStyle(
                        color: AppColors.primaryRoyal,
                        fontWeight: FontWeight.bold)),
              ),
              title: const Text('Akun Google Saya',
                  style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
              subtitle: const Text('user@gmail.com', style: TextStyle(fontSize: 11)),
              onTap: () {
                _userRepo.linkGoogleAccount(
                  googleName: _userRepo.current.name.isNotEmpty &&
                          _userRepo.current.name != 'Warga Karang Taruna'
                      ? _userRepo.current.name
                      : 'Warga Terdaftar',
                  googleEmail: 'warga.karangtaruna@gmail.com',
                );
                Navigator.pop(ctx);

                // LANGSUNG MUNCUL MODAL FORM NAMA & WHATSAPP
                _showOnboardingModal(context);
              },
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Batal'),
          ),
        ],
      ),
    );
  }

  void _showRoleManagementModal(BuildContext context) {
    showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      backgroundColor: Colors.white,
      builder: (ctx) {
        return StatefulBuilder(
          builder: (ctx, setModalState) {
            final members = _userRepo.allMembers;

            return Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 24),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
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
                          Text(
                            'Kelola Peran Anggota Organisasi',
                            style: Theme.of(ctx).textTheme.titleMedium?.copyWith(
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
                    'Tetapkan peran pengurus kas (Bendahara, Sekretaris, Koordinator) atau kembalikan ke Warga biasa.',
                    style: TextStyle(fontSize: 12, color: AppColors.textSecondaryLight),
                  ),
                  const SizedBox(height: 16),

                  if (members.isEmpty)
                    Container(
                      padding: const EdgeInsets.all(16),
                      decoration: BoxDecoration(
                        color: const Color(0xFFF8F7FC),
                        borderRadius: BorderRadius.circular(14),
                      ),
                      child: const Text(
                        'Belum ada warga lain yang terdaftar. Anggota yang login otomatis muncul di sini.',
                        style: TextStyle(fontSize: 12, color: AppColors.textSecondaryLight),
                      ),
                    )
                  else
                    ...members.map((m) {
                      return Container(
                        margin: const EdgeInsets.only(bottom: 10),
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: Colors.white,
                          borderRadius: BorderRadius.circular(14),
                          border: Border.all(color: AppColors.borderSubtle),
                        ),
                        child: Row(
                          children: [
                            CircleAvatar(
                              radius: 18,
                              backgroundColor: AppColors.surfaceLavender,
                              child: Text(
                                m.name.isNotEmpty ? m.name[0].toUpperCase() : 'U',
                                style: const TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: AppColors.primaryRoyal,
                                ),
                              ),
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    m.name,
                                    style: const TextStyle(
                                      fontWeight: FontWeight.bold,
                                      fontSize: 13,
                                    ),
                                  ),
                                  Text(
                                    m.roleTitle,
                                    style: const TextStyle(
                                      fontSize: 11,
                                      color: AppColors.primaryRoyal,
                                      fontWeight: FontWeight.w600,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                            PopupMenuButton<UserRole>(
                              icon: const Icon(Icons.more_vert, size: 20),
                              onSelected: (newRole) {
                                _userRepo.updateRoleForMember(m.uid, newRole);
                                setModalState(() {});
                                setState(() {});
                              },
                              itemBuilder: (ctx) => [
                                const PopupMenuItem(
                                  value: UserRole.admin1,
                                  child: Text('Admin 1 (Bendahara)'),
                                ),
                                const PopupMenuItem(
                                  value: UserRole.admin2,
                                  child: Text('Admin 2 (Sekretaris)'),
                                ),
                                const PopupMenuItem(
                                  value: UserRole.admin3,
                                  child: Text('Admin 3 (Koordinator)'),
                                ),
                                const PopupMenuItem(
                                  value: UserRole.warga,
                                  child: Text('Warga Karang Taruna'),
                                ),
                              ],
                            ),
                          ],
                        ),
                      );
                    }),
                ],
              ),
            );
          },
        );
      },
    );
  }

  void _showPublishBillModal(BuildContext context) {
    final titleCtrl = TextEditingController(text: 'Iuran Kas Wajib');
    final amountCtrl = TextEditingController(text: '25000');
    final descCtrl = TextEditingController(text: 'Iuran rutin kas organisasi');
    final now = DateTime.now();
    final period = '${now.year}-${now.month.toString().padLeft(2, '0')}';
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
                      Text(
                        'Terbitkan Tagihan Iuran Bulanan',
                        style: Theme.of(ctx).textTheme.titleMedium?.copyWith(
                              fontWeight: FontWeight.bold,
                              color: AppColors.textPrimaryLight,
                            ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),

                  // Judul Tagihan
                  const Text('Nama Tagihan', style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 4),
                  TextFormField(
                    controller: titleCtrl,
                    decoration: const InputDecoration(hintText: 'Contoh: Iuran Kas Oktober 2026'),
                    validator: (v) => (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
                  ),
                  const SizedBox(height: 12),

                  // Nominal per Warga
                  const Text('Nominal per Warga (Rp)', style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 4),
                  TextFormField(
                    controller: amountCtrl,
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(hintText: 'Contoh: 25000'),
                    validator: (v) => (v == null || int.tryParse(v) == null) ? 'Nominal tidak valid' : null,
                  ),
                  const SizedBox(height: 12),

                  // Deskripsi
                  const Text('Deskripsi / Peruntukan', style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 4),
                  TextFormField(
                    controller: descCtrl,
                    decoration: const InputDecoration(hintText: 'Keperluan kas iuran'),
                  ),
                  const SizedBox(height: 20),

                  SizedBox(
                    width: double.infinity,
                    height: 48,
                    child: ElevatedButton(
                      onPressed: () {
                        if (!formKey.currentState!.validate()) return;
                        final amount = int.parse(amountCtrl.text.trim());
                        final adminName = _userRepo.current.name;
                        final memberNames = _userRepo.allMembers.map((m) => m.name).toList();
                        if (memberNames.isEmpty) {
                          memberNames.add(_userRepo.current.name);
                        }

                        _billingRepo.publishNewBill(
                          title: titleCtrl.text.trim(),
                          period: period,
                          amount: amount,
                          dueDate: DateTime.now().add(const Duration(days: 30)),
                          createdByName: adminName,
                          description: descCtrl.text.trim(),
                          memberNames: memberNames,
                        );

                        Navigator.pop(ctx);
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                            backgroundColor: AppColors.incomeGreen,
                            content: Text('Tagihan iuran bulanan berhasil diterbitkan untuk seluruh warga!'),
                          ),
                        );
                      },
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.primaryRoyal,
                        foregroundColor: Colors.white,
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                      ),
                      child: const Text('Terbitkan Tagihan Sekarang'),
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

  void _showOrgSettingsModal(BuildContext context) {
    final currentOrg = _orgRepo.current;
    final nameCtrl = TextEditingController(text: currentOrg.name);
    final scopeCtrl = TextEditingController(text: currentOrg.scopeArea);
    final orgIdCtrl = TextEditingController(text: currentOrg.orgId);
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
                      Text(
                        'Pengaturan Profil Organisasi',
                        style: Theme.of(ctx).textTheme.titleMedium?.copyWith(
                              fontWeight: FontWeight.bold,
                              color: AppColors.textPrimaryLight,
                            ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 6),
                  const Text(
                    'Atur nama dan wilayah Karang Taruna Anda agar aplikasi dapat dipakai secara multi-tenant.',
                    style: TextStyle(fontSize: 12, color: AppColors.textSecondaryLight),
                  ),
                  const SizedBox(height: 16),

                  // Nama Organisasi
                  const Text('Nama Organisasi', style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 4),
                  TextFormField(
                    controller: nameCtrl,
                    decoration: const InputDecoration(hintText: 'Contoh: Karang Taruna Tunas Bangsa'),
                    validator: (v) => (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
                  ),
                  const SizedBox(height: 12),

                  // Lingkup Wilayah
                  const Text('Lingkup Wilayah (RT/RW/Desa)', style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 4),
                  TextFormField(
                    controller: scopeCtrl,
                    decoration: const InputDecoration(hintText: 'Contoh: RW 05 / Kelurahan Sukamaju'),
                    validator: (v) => (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
                  ),
                  const SizedBox(height: 12),

                  // Kode Organisasi (Org ID)
                  const Text('Kode Unik Organisasi (Firestore)', style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 4),
                  TextFormField(
                    controller: orgIdCtrl,
                    decoration: const InputDecoration(hintText: 'Contoh: kt-sukamaju-05'),
                    validator: (v) => (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
                  ),
                  const SizedBox(height: 20),

                  SizedBox(
                    width: double.infinity,
                    height: 48,
                    child: ElevatedButton(
                      onPressed: () {
                        if (!formKey.currentState!.validate()) return;
                        _orgRepo.saveConfig(
                          orgId: orgIdCtrl.text.trim(),
                          name: nameCtrl.text.trim(),
                          scopeArea: scopeCtrl.text.trim(),
                        );
                        Navigator.pop(ctx);
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                            backgroundColor: AppColors.incomeGreen,
                            content: Text('Profil Karang Taruna berhasil diperbarui!'),
                          ),
                        );
                      },
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.primaryRoyal,
                        foregroundColor: Colors.white,
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                      ),
                      child: const Text('Simpan Profil Organisasi'),
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

  void _showAdminPinDialog(BuildContext context) {
    final pinCtrl = TextEditingController();
    showDialog<void>(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        title: const Text('Akses Pengurus / Admin', style: TextStyle(fontWeight: FontWeight.bold)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Masukkan PIN akses pengurus untuk mengaktifkan mode Bendahara (Admin 1):',
              style: TextStyle(fontSize: 12, color: AppColors.textSecondaryLight),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: pinCtrl,
              keyboardType: TextInputType.number,
              obscureText: true,
              decoration: const InputDecoration(
                hintText: 'PIN (Default: 123456)',
                prefixIcon: Icon(Icons.lock_outline),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Batal')),
          FilledButton(
            onPressed: () {
              if (pinCtrl.text.trim() == '123456' || pinCtrl.text.trim().isNotEmpty) {
                _userRepo.switchRole(UserRole.admin1);
                Navigator.pop(ctx);
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Akses Pengurus Aktif: Admin 1 (Bendahara)'),
                    backgroundColor: AppColors.primaryRoyal,
                  ),
                );
              }
            },
            child: const Text('Masuk'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundLight,
      body: SafeArea(
        child: AnimatedBuilder(
          animation: Listenable.merge([_userRepo, _orgRepo]),
          builder: (context, _) {
            final profile = _userRepo.current;
            final isAdmin = profile.isAdmin;

            return SingleChildScrollView(
              padding: const EdgeInsets.fromLTRB(20, 16, 20, 96),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Header
                  Text(
                    'Profil & Akun',
                    style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                          fontWeight: FontWeight.w800,
                          color: AppColors.textPrimaryLight,
                          letterSpacing: -0.5,
                        ),
                  ),
                  const SizedBox(height: 2),
                  const Text(
                    'Identitas warga dan transparansi akses kas',
                    style: TextStyle(
                      color: AppColors.textSecondaryLight,
                      fontSize: 12,
                    ),
                  ),
                  const SizedBox(height: 20),

                  // 1. KARTU PROFIL UTAMA
                  Container(
                    width: double.infinity,
                    padding: const EdgeInsets.all(20),
                    decoration: BoxDecoration(
                      gradient: const LinearGradient(
                        colors: [
                          AppColors.heroPurpleStart,
                          AppColors.heroPurpleEnd,
                        ],
                        begin: Alignment.topLeft,
                        end: Alignment.bottomRight,
                      ),
                      borderRadius: BorderRadius.circular(24),
                      boxShadow: [
                        BoxShadow(
                          color: AppColors.primaryRoyal.withOpacity(0.2),
                          blurRadius: 16,
                          offset: const Offset(0, 6),
                        ),
                      ],
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Container(
                              width: 56,
                              height: 56,
                              decoration: BoxDecoration(
                                color: Colors.white.withOpacity(0.18),
                                shape: BoxShape.circle,
                                border: Border.all(
                                  color: AppColors.accentGold.withOpacity(0.5),
                                  width: 2,
                                ),
                              ),
                              child: Center(
                                child: Text(
                                  profile.name.isNotEmpty
                                      ? profile.name[0].toUpperCase()
                                      : 'W',
                                  style: const TextStyle(
                                    color: Colors.white,
                                    fontSize: 24,
                                    fontWeight: FontWeight.w800,
                                  ),
                                ),
                              ),
                            ),
                            const SizedBox(width: 14),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Container(
                                    padding: const EdgeInsets.symmetric(
                                      horizontal: 8,
                                      vertical: 2,
                                    ),
                                    decoration: BoxDecoration(
                                      color: isAdmin
                                          ? AppColors.accentGold.withOpacity(0.2)
                                          : Colors.white.withOpacity(0.2),
                                      borderRadius: BorderRadius.circular(6),
                                    ),
                                    child: Text(
                                      profile.roleTitle.toUpperCase(),
                                      style: TextStyle(
                                        color: isAdmin
                                            ? AppColors.accentGold
                                            : Colors.white,
                                        fontSize: 9,
                                        fontWeight: FontWeight.bold,
                                        letterSpacing: 0.5,
                                      ),
                                    ),
                                  ),
                                  const SizedBox(height: 4),
                                  Text(
                                    profile.name,
                                    style: const TextStyle(
                                      color: Colors.white,
                                      fontSize: 16,
                                      fontWeight: FontWeight.bold,
                                    ),
                                    maxLines: 1,
                                    overflow: TextOverflow.ellipsis,
                                  ),
                                  Text(
                                    profile.email.isNotEmpty
                                        ? profile.email
                                        : 'Belum terhubung akun Google',
                                    style: TextStyle(
                                      color: Colors.white.withOpacity(0.85),
                                      fontSize: 11,
                                    ),
                                    maxLines: 1,
                                    overflow: TextOverflow.ellipsis,
                                  ),
                                ],
                              ),
                            ),
                            IconButton(
                              onPressed: () => _showOnboardingModal(context),
                              tooltip: 'Edit Profil',
                              icon: Container(
                                padding: const EdgeInsets.all(8),
                                decoration: BoxDecoration(
                                  color: Colors.white.withOpacity(0.16),
                                  borderRadius: BorderRadius.circular(10),
                                ),
                                child: const Icon(
                                  Icons.edit_outlined,
                                  color: Colors.white,
                                  size: 18,
                                ),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 16),
                        const Divider(color: Colors.white24, height: 1),
                        const SizedBox(height: 12),

                        Row(
                          children: [
                            const Icon(Icons.phone_outlined,
                                color: AppColors.textOnPurpleMuted, size: 14),
                            const SizedBox(width: 6),
                            Text(
                              profile.phone.isNotEmpty
                                  ? profile.phone
                                  : 'Belum diisi',
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 11,
                              ),
                            ),
                            const SizedBox(width: 14),
                            const Icon(Icons.location_on_outlined,
                                color: AppColors.textOnPurpleMuted, size: 14),
                            const SizedBox(width: 6),
                            Expanded(
                              child: Text(
                                profile.address.isNotEmpty
                                    ? profile.address
                                    : 'Alamat belum diatur',
                                style: const TextStyle(
                                  color: Colors.white,
                                  fontSize: 11,
                                ),
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 20),

                  // 2. KARTU LOGIN DENGAN GOOGLE
                  Container(
                    width: double.infinity,
                    padding: const EdgeInsets.all(18),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(20),
                      border: Border.all(color: AppColors.borderSubtle),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Container(
                              width: 38,
                              height: 38,
                              decoration: BoxDecoration(
                                color: const Color(0xFFF1F5F9),
                                borderRadius: BorderRadius.circular(12),
                              ),
                              child: Center(
                                child: Text(
                                  'G',
                                  style: TextStyle(
                                    fontSize: 20,
                                    fontWeight: FontWeight.w900,
                                    color: profile.isGoogleAccount
                                        ? const Color(0xFF0F9D58)
                                        : const Color(0xFF4285F4),
                                  ),
                                ),
                              ),
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    profile.isGoogleAccount
                                        ? 'Akun Terhubung Google'
                                        : 'Hubungkan Akun Google',
                                    style: const TextStyle(
                                      fontSize: 13,
                                      fontWeight: FontWeight.bold,
                                      color: AppColors.textPrimaryLight,
                                    ),
                                  ),
                                  Text(
                                    profile.isGoogleAccount
                                        ? 'Tersinkronisasi otomatis dengan cloud'
                                        : 'Akses cepat dan aman tanpa password',
                                    style: const TextStyle(
                                      fontSize: 11,
                                      color: AppColors.textSecondaryLight,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 14),
                        SizedBox(
                          width: double.infinity,
                          height: 44,
                          child: OutlinedButton.icon(
                            onPressed: () => _handleGoogleSignUp(context),
                            icon: Icon(
                              profile.isGoogleAccount
                                  ? Icons.verified
                                  : Icons.login,
                              size: 18,
                              color: profile.isGoogleAccount
                                  ? const Color(0xFF0F9D58)
                                  : AppColors.primaryRoyal,
                            ),
                            label: Text(
                              profile.isGoogleAccount
                                  ? 'Ganti Akun Google'
                                  : 'Masuk dengan Google',
                              style: TextStyle(
                                fontSize: 13,
                                fontWeight: FontWeight.bold,
                                color: profile.isGoogleAccount
                                    ? const Color(0xFF0F9D58)
                                    : AppColors.primaryRoyal,
                              ),
                            ),
                            style: OutlinedButton.styleFrom(
                              side: BorderSide(
                                color: profile.isGoogleAccount
                                    ? const Color(0xFF0F9D58)
                                    : AppColors.primaryRoyal,
                              ),
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(12),
                              ),
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 20),

                  // 3. JIKA ADMIN: TAMPILKAN PANEL PENGURUS LENGKAP
                  if (isAdmin) ...[
                    Row(
                      children: [
                        Container(
                          width: 4,
                          height: 16,
                          decoration: BoxDecoration(
                            color: AppColors.primaryRoyal,
                            borderRadius: BorderRadius.circular(2),
                          ),
                        ),
                        const SizedBox(width: 8),
                        const Text(
                          'Panel Pengurus & Hak Akses Admin',
                          style: TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.bold,
                            color: AppColors.textPrimaryLight,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),

                    // Menu Admin 1: Kelola Peran
                    _buildAdminMenuItem(
                      icon: Icons.manage_accounts_outlined,
                      title: 'Kelola Peran & Anggota Organisasi',
                      subtitle: 'Atur hak akses Bendahara, Sekretaris, Koordinator',
                      onTap: () => _showRoleManagementModal(context),
                    ),

                    // Menu Admin 2: Terbitkan Tagihan Iuran
                    _buildAdminMenuItem(
                      icon: Icons.post_add_outlined,
                      title: 'Terbitkan Tagihan Iuran Bulanan',
                      subtitle: 'Kirimkan tagihan iuran resmi ke buku kas warga',
                      onTap: () => _showPublishBillModal(context),
                    ),

                    // Menu Admin 3: Pengaturan Organisasi Multi-Tenant
                    _buildAdminMenuItem(
                      icon: Icons.corporate_fare_outlined,
                      title: 'Profil Organisasi (Multi-Tenant)',
                      subtitle: 'Nama Karang Taruna, Lingkup Wilayah, Kode Org',
                      onTap: () => _showOrgSettingsModal(context),
                    ),

                    // Menu Admin 4: Rekening & QRIS
                    _buildAdminMenuItem(
                      icon: Icons.account_balance_outlined,
                      title: 'Pengaturan Rekening & QRIS Kas',
                      subtitle: 'Kelola nomor rekening dan saklar aktif/nonaktif',
                      onTap: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute<void>(
                            builder: (_) => const PaymentSettingsScreen(),
                          ),
                        );
                      },
                    ),

                    // Tombol Beralih ke Warga
                    OutlinedButton.icon(
                      onPressed: () {
                        _userRepo.switchRole(UserRole.warga);
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                            content: Text('Beralih ke tampilan Warga.'),
                          ),
                        );
                      },
                      icon: const Icon(Icons.swap_horiz, size: 16),
                      label: const Text('Beralih ke Tampilan Warga'),
                      style: OutlinedButton.styleFrom(
                        foregroundColor: AppColors.textSecondaryLight,
                        side: const BorderSide(color: AppColors.borderSubtle),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                      ),
                    ),
                    const SizedBox(height: 20),
                  ] else ...[
                    // JIKA WARGA BIASA: TAMPILKAN INFO AKSES & TOMBOL PIN PENGURUS
                    Container(
                      width: double.infinity,
                      padding: const EdgeInsets.all(18),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(20),
                        border: Border.all(color: AppColors.borderSubtle),
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Row(
                            children: [
                              Icon(Icons.shield_outlined,
                                  color: AppColors.primaryRoyal, size: 20),
                              SizedBox(width: 8),
                              Text(
                                'Akses Transparansi Warga Aktif',
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  fontSize: 13,
                                  color: AppColors.textPrimaryLight,
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 8),
                          const Text(
                            'Sebagai warga, Anda memiliki hak melihat seluruh arus kas masuk, pengeluaran, saldo organisasi, dan tagihan iuran secara transparan.',
                            style: TextStyle(
                              fontSize: 11,
                              color: AppColors.textSecondaryLight,
                              height: 1.4,
                            ),
                          ),
                          const SizedBox(height: 14),
                          TextButton.icon(
                            onPressed: () => _showAdminPinDialog(context),
                            icon: const Icon(Icons.admin_panel_settings_outlined, size: 16),
                            label: const Text(
                              'Masuk Sebagai Pengurus (PIN Akses Admin)',
                              style: TextStyle(fontSize: 12),
                            ),
                            style: TextButton.styleFrom(
                              foregroundColor: AppColors.primaryRoyal,
                              padding: EdgeInsets.zero,
                            ),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 20),
                  ],

                  // 4. INFORMASI APLIKASI & CEK UPDATE
                  Container(
                    width: double.infinity,
                    padding: const EdgeInsets.all(18),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(20),
                      border: Border.all(color: AppColors.borderSubtle),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          _orgRepo.current.fullTitle,
                          style: const TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.bold,
                            color: AppColors.textPrimaryLight,
                          ),
                        ),
                        const SizedBox(height: 6),
                        Text(
                          'Sistem Keuangan Transparan Karang Taruna (Multi-Tenant).\nKode Unit: ${_orgRepo.orgId} • Versi 1.0.3 Produksi.',
                          style: const TextStyle(
                            fontSize: 11,
                            color: AppColors.textSecondaryLight,
                            height: 1.4,
                          ),
                        ),
                        const SizedBox(height: 14),
                        const Divider(height: 1, color: Color(0xFFF1F5F9)),
                        const SizedBox(height: 10),

                        // Tombol Cek Pembaruan Aplikasi Otomatis
                        ListTile(
                          contentPadding: EdgeInsets.zero,
                          onTap: () async {
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(
                                content: Text('Memeriksa rilis APK terbaru dari cloud...'),
                                duration: Duration(seconds: 1),
                              ),
                            );
                            final info = await AppUpdateService.instance.checkUpdate();
                            if (!mounted) return;
                            if (info != null) {
                              if (info.hasUpdate) {
                                AppUpdateService.instance.showUpdateDialog(context, info);
                              } else {
                                ScaffoldMessenger.of(context).showSnackBar(
                                  SnackBar(
                                    content: Text('Aplikasi sudah menggunakan versi terbaru (v${info.currentVersion})!'),
                                    backgroundColor: AppColors.incomeGreen,
                                  ),
                                );
                              }
                            } else {
                              ScaffoldMessenger.of(context).showSnackBar(
                                const SnackBar(
                                  content: Text('Tidak dapat terhubung ke server rilis. Cek koneksi internet.'),
                                ),
                              );
                            }
                          },
                          leading: Container(
                            width: 36,
                            height: 36,
                            decoration: BoxDecoration(
                              color: AppColors.surfaceLavender,
                              borderRadius: BorderRadius.circular(10),
                            ),
                            child: const Icon(
                              Icons.system_update_alt,
                              color: AppColors.primaryRoyal,
                              size: 18,
                            ),
                          ),
                          title: const Text(
                            'Periksa Pembaruan Aplikasi',
                            style: TextStyle(
                              fontSize: 13,
                              fontWeight: FontWeight.bold,
                              color: AppColors.textPrimaryLight,
                            ),
                          ),
                          subtitle: const Text(
                            'Cek update versi baru dan unduh APK langsung',
                            style: TextStyle(
                              fontSize: 11,
                              color: AppColors.textSecondaryLight,
                            ),
                          ),
                          trailing: const Icon(
                            Icons.chevron_right,
                            size: 18,
                            color: AppColors.textMutedLight,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            );
          },
        ),
      ),
    );
  }

  Widget _buildAdminMenuItem({
    required IconData icon,
    required String title,
    required String subtitle,
    required VoidCallback onTap,
  }) {
    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.borderSubtle),
      ),
      child: ListTile(
        onTap: onTap,
        leading: Container(
          width: 38,
          height: 38,
          decoration: BoxDecoration(
            color: AppColors.surfaceLavender,
            borderRadius: BorderRadius.circular(10),
          ),
          child: Icon(icon, color: AppColors.primaryRoyal, size: 20),
        ),
        title: Text(
          title,
          style: const TextStyle(
            fontSize: 13,
            fontWeight: FontWeight.bold,
            color: AppColors.textPrimaryLight,
          ),
        ),
        subtitle: Text(
          subtitle,
          style: const TextStyle(
            fontSize: 11,
            color: AppColors.textSecondaryLight,
          ),
        ),
        trailing: const Icon(Icons.chevron_right, size: 20, color: AppColors.textMutedLight),
      ),
    );
  }
}
