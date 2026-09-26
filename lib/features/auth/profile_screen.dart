import 'package:flutter/material.dart';
import '../../core/constants/app_colors.dart';
import '../../core/utils/app_toast.dart';
import '../../data/repositories/billing_repository.dart';
import '../../data/repositories/organization_repository.dart';
import '../../data/repositories/user_profile_repository.dart';
import '../../services/app_update_service.dart';
import '../admin/payment_settings_screen.dart';

/// Layar Profil & Pengaturan Akun di dalam MainShell (Tab 4).
class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  final _userRepo = UserProfileRepository.instance;
  final _orgRepo = OrganizationRepository.instance;
  final _billingRepo = BillingRepository.instance;

  void _showEditProfileModal(BuildContext context, UserProfile profile) {
    final nameCtrl = TextEditingController(text: profile.name);
    final phoneCtrl = TextEditingController(text: profile.phone);
    final addressCtrl = TextEditingController(text: profile.address);
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
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text(
                        'Edit Data Diri',
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                          color: AppColors.textPrimaryLight,
                        ),
                      ),
                      IconButton(
                        icon: const Icon(Icons.close, size: 20),
                        onPressed: () => Navigator.pop(ctx),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  const Text('Nama Lengkap',
                      style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 4),
                  TextFormField(
                    controller: nameCtrl,
                    validator: (v) =>
                        (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
                  ),
                  const SizedBox(height: 12),
                  const Text('Nomor WhatsApp',
                      style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 4),
                  TextFormField(
                    controller: phoneCtrl,
                    keyboardType: TextInputType.phone,
                    validator: (v) =>
                        (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
                  ),
                  const SizedBox(height: 12),
                  const Text('Alamat & RT/RW',
                      style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 4),
                  TextFormField(
                    controller: addressCtrl,
                    validator: (v) =>
                        (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
                  ),
                  const SizedBox(height: 20),
                  SizedBox(
                    width: double.infinity,
                    height: 46,
                    child: ElevatedButton(
                      onPressed: () {
                        if (!formKey.currentState!.validate()) return;
                        _userRepo.updateProfile(
                          name: nameCtrl.text.trim(),
                          phone: phoneCtrl.text.trim(),
                          address: addressCtrl.text.trim(),
                        );
                        Navigator.pop(ctx);
                        AppToast.success(context, 'Data profil diperbarui');
                      },
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.primaryRoyal,
                        foregroundColor: Colors.white,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                      ),
                      child: const Text('Simpan Perubahan'),
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
                      const Text(
                        'Kelola Peran Anggota',
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                          color: AppColors.textPrimaryLight,
                        ),
                      ),
                      IconButton(
                        icon: const Icon(Icons.close, size: 20),
                        onPressed: () => Navigator.pop(ctx),
                      ),
                    ],
                  ),
                  const SizedBox(height: 6),
                  const Text(
                    'Pilih peran pengurus atau kembalikan ke peran warga.',
                    style: TextStyle(fontSize: 12, color: AppColors.textSecondaryLight),
                  ),
                  const SizedBox(height: 16),
                  if (members.isEmpty)
                    Container(
                      padding: const EdgeInsets.all(14),
                      decoration: BoxDecoration(
                        color: const Color(0xFFF8F7FC),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: const Text(
                        'Belum ada akun lain yang terdaftar.',
                        style: TextStyle(fontSize: 12, color: AppColors.textSecondaryLight),
                      ),
                    )
                  else
                    ...members.map((m) {
                      return Container(
                        margin: const EdgeInsets.only(bottom: 8),
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
                                m.name.isNotEmpty ? m.name[0].toUpperCase() : 'W',
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
                                AppToast.success(context, 'Peran ${m.name} diubah ke ${newRole.label}');
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
                                  child: Text('Warga'),
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
    final titleCtrl = TextEditingController(text: 'Iuran Kas');
    final amountCtrl = TextEditingController(text: '25000');
    final descCtrl = TextEditingController(text: 'Iuran rutin');
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
                  const Text(
                    'Terbitkan Tagihan Iuran',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                      color: AppColors.textPrimaryLight,
                    ),
                  ),
                  const SizedBox(height: 16),
                  const Text('Nama Tagihan',
                      style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 4),
                  TextFormField(
                    controller: titleCtrl,
                    validator: (v) =>
                        (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
                  ),
                  const SizedBox(height: 12),
                  const Text('Nominal per Warga (Rp)',
                      style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 4),
                  TextFormField(
                    controller: amountCtrl,
                    keyboardType: TextInputType.number,
                    validator: (v) =>
                        (v == null || int.tryParse(v) == null) ? 'Nominal tidak valid' : null,
                  ),
                  const SizedBox(height: 12),
                  const Text('Keterangan',
                      style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 4),
                  TextFormField(
                    controller: descCtrl,
                  ),
                  const SizedBox(height: 20),
                  SizedBox(
                    width: double.infinity,
                    height: 46,
                    child: ElevatedButton(
                      onPressed: () {
                        if (!formKey.currentState!.validate()) return;
                        final amount = int.parse(amountCtrl.text.trim());
                        final adminName = _userRepo.current.name;
                        final memberNames =
                            _userRepo.allMembers.map((m) => m.name).toList();
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
                        AppToast.success(context, 'Tagihan iuran berhasil diterbitkan');
                      },
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.primaryRoyal,
                        foregroundColor: Colors.white,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                      ),
                      child: const Text('Terbitkan Tagihan'),
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
                  const Text(
                    'Profil Organisasi',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                      color: AppColors.textPrimaryLight,
                    ),
                  ),
                  const SizedBox(height: 16),
                  const Text('Nama Organisasi',
                      style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 4),
                  TextFormField(
                    controller: nameCtrl,
                    validator: (v) =>
                        (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
                  ),
                  const SizedBox(height: 12),
                  const Text('Lingkup Wilayah (RT/RW/Desa)',
                      style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 4),
                  TextFormField(
                    controller: scopeCtrl,
                    validator: (v) =>
                        (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
                  ),
                  const SizedBox(height: 12),
                  const Text('Kode Unik Database',
                      style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 4),
                  TextFormField(
                    controller: orgIdCtrl,
                    validator: (v) =>
                        (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
                  ),
                  const SizedBox(height: 20),
                  SizedBox(
                    width: double.infinity,
                    height: 46,
                    child: ElevatedButton(
                      onPressed: () {
                        if (!formKey.currentState!.validate()) return;
                        _orgRepo.saveConfig(
                          orgId: orgIdCtrl.text.trim(),
                          name: nameCtrl.text.trim(),
                          scopeArea: scopeCtrl.text.trim(),
                        );
                        Navigator.pop(ctx);
                        AppToast.success(context, 'Profil organisasi disimpan');
                      },
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.primaryRoyal,
                        foregroundColor: Colors.white,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                      ),
                      child: const Text('Simpan Profil'),
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
                    'Informasi akun dan pengaturan kas',
                    style: TextStyle(
                      color: AppColors.textSecondaryLight,
                      fontSize: 12,
                    ),
                  ),
                  const SizedBox(height: 20),

                  // Kartu Profil
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
                              width: 52,
                              height: 52,
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
                                    fontSize: 22,
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
                                        : profile.phone,
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
                              onPressed: () => _showEditProfileModal(context, profile),
                              tooltip: 'Edit Data Diri',
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
                        const SizedBox(height: 14),
                        const Divider(color: Colors.white24, height: 1),
                        const SizedBox(height: 10),
                        Row(
                          children: [
                            const Icon(Icons.phone_outlined,
                                color: AppColors.textOnPurpleMuted, size: 14),
                            const SizedBox(width: 6),
                            Text(
                              profile.phone.isNotEmpty ? profile.phone : '-',
                              style: const TextStyle(color: Colors.white, fontSize: 11),
                            ),
                            const SizedBox(width: 14),
                            const Icon(Icons.location_on_outlined,
                                color: AppColors.textOnPurpleMuted, size: 14),
                            const SizedBox(width: 6),
                            Expanded(
                              child: Text(
                                profile.address.isNotEmpty ? profile.address : '-',
                                style: const TextStyle(color: Colors.white, fontSize: 11),
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

                  // Panel Pengurus khusus Admin
                  if (isAdmin) ...[
                    const Text(
                      'Menu Pengurus',
                      style: TextStyle(
                        fontSize: 13,
                        fontWeight: FontWeight.bold,
                        color: AppColors.textPrimaryLight,
                      ),
                    ),
                    const SizedBox(height: 10),
                    _buildActionCard(
                      icon: Icons.manage_accounts_outlined,
                      title: 'Kelola Peran Anggota',
                      subtitle: 'Atur hak akses Bendahara, Sekretaris, Koordinator',
                      onTap: () => _showRoleManagementModal(context),
                    ),
                    _buildActionCard(
                      icon: Icons.post_add_outlined,
                      title: 'Terbitkan Tagihan Iuran',
                      subtitle: 'Kirimkan tagihan iuran baru ke warga',
                      onTap: () => _showPublishBillModal(context),
                    ),
                    _buildActionCard(
                      icon: Icons.corporate_fare_outlined,
                      title: 'Profil Organisasi',
                      subtitle: 'Nama organisasi, lingkup wilayah, dan kode unit',
                      onTap: () => _showOrgSettingsModal(context),
                    ),
                    _buildActionCard(
                      icon: Icons.account_balance_outlined,
                      title: 'Rekening & QRIS Kas',
                      subtitle: 'Pengaturan metode pembayaran kas resmi',
                      onTap: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute<void>(
                            builder: (_) => const PaymentSettingsScreen(),
                          ),
                        );
                      },
                    ),
                    const SizedBox(height: 10),
                  ],

                  // Informasi Aplikasi & Update
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
                        const SizedBox(height: 4),
                        const Text(
                          'Kas Go • Versi 1.0.5',
                          style: TextStyle(
                            fontSize: 11,
                            color: AppColors.textSecondaryLight,
                          ),
                        ),
                        const SizedBox(height: 12),
                        const Divider(height: 1, color: Color(0xFFF1F5F9)),
                        const SizedBox(height: 8),

                        ListTile(
                          contentPadding: EdgeInsets.zero,
                          onTap: () async {
                            AppToast.info(context, 'Memeriksa rilis terbaru...');
                            final info = await AppUpdateService.instance.checkUpdate();
                            if (!context.mounted) return;
                            if (info != null) {
                              if (info.hasUpdate) {
                                AppUpdateService.instance.showUpdateDialog(context, info);
                              } else {
                                AppToast.success(context, 'Aplikasi sudah versi terbaru (v${info.currentVersion})');
                              }
                            } else {
                              AppToast.error(context, 'Tidak dapat terhubung ke server');
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
                            'Periksa Pembaruan',
                            style: TextStyle(
                              fontSize: 13,
                              fontWeight: FontWeight.bold,
                              color: AppColors.textPrimaryLight,
                            ),
                          ),
                          subtitle: const Text(
                            'Cek versi baru dan unduh APK',
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
                  const SizedBox(height: 20),

                  // Tombol Keluar / Logout
                  SizedBox(
                    width: double.infinity,
                    height: 46,
                    child: OutlinedButton.icon(
                      onPressed: () async {
                        await _userRepo.logout();
                        if (context.mounted) {
                          AppToast.info(context, 'Anda telah keluar');
                        }
                      },
                      icon: const Icon(Icons.logout, size: 16, color: Colors.red),
                      label: const Text(
                        'Keluar dari Akun',
                        style: TextStyle(color: Colors.red, fontWeight: FontWeight.bold),
                      ),
                      style: OutlinedButton.styleFrom(
                        side: const BorderSide(color: Color(0xFFFCA5A5)),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                      ),
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

  Widget _buildActionCard({
    required IconData icon,
    required String title,
    required String subtitle,
    required VoidCallback onTap,
  }) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.borderSubtle),
      ),
      child: ListTile(
        onTap: onTap,
        leading: Container(
          width: 36,
          height: 36,
          decoration: BoxDecoration(
            color: AppColors.surfaceLavender,
            borderRadius: BorderRadius.circular(10),
          ),
          child: Icon(icon, color: AppColors.primaryRoyal, size: 18),
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
        trailing: const Icon(Icons.chevron_right, size: 18, color: AppColors.textMutedLight),
      ),
    );
  }
}
