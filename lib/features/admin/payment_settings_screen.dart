import 'package:flutter/material.dart';
import '../../core/constants/app_colors.dart';
import '../../data/repositories/organization_repository.dart';

/// Layar Pengaturan Metode Pembayaran Dinamis (Admin).
///
/// Mengatur nomor rekening, QRIS, dan e-wallet beserta saklar switch aktif/nonaktif.
class PaymentSettingsScreen extends StatefulWidget {
  const PaymentSettingsScreen({super.key});

  @override
  State<PaymentSettingsScreen> createState() => _PaymentSettingsScreenState();
}

class _PaymentSettingsScreenState extends State<PaymentSettingsScreen> {
  final _orgRepo = OrganizationRepository.instance;

  void _showEditMethodModal(BuildContext context, PaymentMethodItem method) {
    final titleCtrl = TextEditingController(text: method.title);
    final accNoCtrl = TextEditingController(text: method.accountNumber);
    final accNameCtrl = TextEditingController(text: method.accountName);
    final instrCtrl = TextEditingController(text: method.instructions ?? '');
    final qrUrlCtrl = TextEditingController(text: method.qrImageUrl ?? '');
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
                            'Edit ${method.title}',
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
                  const SizedBox(height: 16),

                  // Label / Nama Metode
                  const Text(
                    'Nama Bank / Metode Pembayaran',
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                      color: AppColors.textPrimaryLight,
                    ),
                  ),
                  const SizedBox(height: 6),
                  TextFormField(
                    controller: titleCtrl,
                    decoration: const InputDecoration(
                      hintText: 'Contoh: Bank BCA / DANA / QRIS',
                    ),
                    validator: (v) =>
                        (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
                  ),
                  const SizedBox(height: 14),

                  // Nomor Rekening / NMID / No HP
                  Text(
                    method.type == 'qris'
                        ? 'NMID / Kode Merchant QRIS'
                        : 'Nomor Rekening / No HP E-Wallet',
                    style: const TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                      color: AppColors.textPrimaryLight,
                    ),
                  ),
                  const SizedBox(height: 6),
                  TextFormField(
                    controller: accNoCtrl,
                    decoration: InputDecoration(
                      hintText: method.type == 'qris'
                          ? 'Contoh: ID1020261928340'
                          : 'Contoh: 883019283401 atau 0812xxxx',
                    ),
                    validator: (v) =>
                        (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
                  ),
                  const SizedBox(height: 14),

                  // Atas Nama
                  const Text(
                    'Atas Nama Rekening / Merchant',
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                      color: AppColors.textPrimaryLight,
                    ),
                  ),
                  const SizedBox(height: 6),
                  TextFormField(
                    controller: accNameCtrl,
                    decoration: const InputDecoration(
                      hintText: 'Contoh: KAS KARANG TARUNA TUNAS BANGSA',
                    ),
                    validator: (v) =>
                        (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
                  ),
                  const SizedBox(height: 14),

                  // Khusus QRIS: URL gambar atau link QR
                  if (method.type == 'qris') ...[
                    const Text(
                      'Link / URL Gambar QRIS (Opsional)',
                      style: TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
                        color: AppColors.textPrimaryLight,
                      ),
                    ),
                    const SizedBox(height: 6),
                    TextFormField(
                      controller: qrUrlCtrl,
                      decoration: const InputDecoration(
                        hintText: 'https://... atau biarkan kosong untuk barcode bawaan',
                      ),
                    ),
                    const SizedBox(height: 14),
                  ],

                  // Petunjuk Transfer
                  const Text(
                    'Petunjuk Tambahan untuk Warga',
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                      color: AppColors.textPrimaryLight,
                    ),
                  ),
                  const SizedBox(height: 6),
                  TextFormField(
                    controller: instrCtrl,
                    decoration: const InputDecoration(
                      hintText: 'Contoh: Beri berita transfer "Iuran Kas - [Nama]"',
                    ),
                  ),
                  const SizedBox(height: 24),

                  // Tombol Simpan
                  SizedBox(
                    width: double.infinity,
                    height: 48,
                    child: ElevatedButton(
                      onPressed: () {
                        if (!formKey.currentState!.validate()) return;
                        method.title = titleCtrl.text.trim();
                        method.accountNumber = accNoCtrl.text.trim();
                        method.accountName = accNameCtrl.text.trim();
                        method.instructions = instrCtrl.text.trim().isNotEmpty
                            ? instrCtrl.text.trim()
                            : null;
                        method.qrImageUrl = qrUrlCtrl.text.trim().isNotEmpty
                            ? qrUrlCtrl.text.trim()
                            : null;

                        _orgRepo.updatePaymentMethod(method);
                        Navigator.pop(ctx);
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                            content: Text('Metode pembayaran berhasil disimpan!'),
                            backgroundColor: AppColors.incomeGreen,
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
                        'Simpan Pengaturan',
                        style: TextStyle(fontWeight: FontWeight.bold),
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

  void _showAddCustomMethodModal(BuildContext context) {
    final titleCtrl = TextEditingController();
    final accNoCtrl = TextEditingController();
    final accNameCtrl = TextEditingController();
    final instrCtrl = TextEditingController();
    String type = 'bank';
    final formKey = GlobalKey<FormState>();

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
                                'Tambah Rekening / E-Wallet Baru',
                                style: Theme.of(ctx)
                                    .textTheme
                                    .titleMedium
                                    ?.copyWith(
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
                      const SizedBox(height: 16),

                      // Jenis Metode
                      const Text(
                        'Tipe Pembayaran',
                        style: TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.bold,
                          color: AppColors.textPrimaryLight,
                        ),
                      ),
                      const SizedBox(height: 6),
                      DropdownButtonFormField<String>(
                        value: type,
                        dropdownColor: Colors.white,
                        items: const [
                          DropdownMenuItem(
                              value: 'bank', child: Text('Transfer Bank')),
                          DropdownMenuItem(
                              value: 'ewallet', child: Text('E-Wallet / Dompet Digital')),
                          DropdownMenuItem(
                              value: 'custom', child: Text('Metode Lain / Kustom')),
                        ],
                        onChanged: (v) {
                          if (v != null) setModalState(() => type = v);
                        },
                      ),
                      const SizedBox(height: 14),

                      // Nama Bank / E-Wallet
                      const Text(
                        'Nama Bank / Layanan',
                        style: TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.bold,
                          color: AppColors.textPrimaryLight,
                        ),
                      ),
                      const SizedBox(height: 6),
                      TextFormField(
                        controller: titleCtrl,
                        decoration: const InputDecoration(
                          hintText: 'Contoh: Bank BSI / GoPay / OVO',
                        ),
                        validator: (v) =>
                            (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
                      ),
                      const SizedBox(height: 14),

                      // Nomor Rekening / No HP
                      const Text(
                        'Nomor Rekening / No HP',
                        style: TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.bold,
                          color: AppColors.textPrimaryLight,
                        ),
                      ),
                      const SizedBox(height: 6),
                      TextFormField(
                        controller: accNoCtrl,
                        decoration: const InputDecoration(
                          hintText: 'Nomor rekening atau nomor telepon',
                        ),
                        validator: (v) =>
                            (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
                      ),
                      const SizedBox(height: 14),

                      // Atas Nama
                      const Text(
                        'Atas Nama',
                        style: TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.bold,
                          color: AppColors.textPrimaryLight,
                        ),
                      ),
                      const SizedBox(height: 6),
                      TextFormField(
                        controller: accNameCtrl,
                        decoration: const InputDecoration(
                          hintText: 'Nama pemilik rekening kas',
                        ),
                        validator: (v) =>
                            (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
                      ),
                      const SizedBox(height: 14),

                      // Petunjuk
                      const Text(
                        'Petunjuk Pembayaran (Opsional)',
                        style: TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.bold,
                          color: AppColors.textPrimaryLight,
                        ),
                      ),
                      const SizedBox(height: 6),
                      TextFormField(
                        controller: instrCtrl,
                        decoration: const InputDecoration(
                          hintText: 'Contoh: Mohon sertakan berita transfer',
                        ),
                      ),
                      const SizedBox(height: 24),

                      // Tombol Tambah
                      SizedBox(
                        width: double.infinity,
                        height: 48,
                        child: ElevatedButton(
                          onPressed: () {
                            if (!formKey.currentState!.validate()) return;
                            final newItem = PaymentMethodItem(
                              id: 'pm_${DateTime.now().millisecondsSinceEpoch}',
                              type: type,
                              title: titleCtrl.text.trim(),
                              accountNumber: accNoCtrl.text.trim(),
                              accountName: accNameCtrl.text.trim(),
                              instructions: instrCtrl.text.trim().isNotEmpty
                                  ? instrCtrl.text.trim()
                                  : null,
                              isActive: true,
                            );

                            _orgRepo.updatePaymentMethod(newItem);
                            Navigator.pop(ctx);
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(
                                content: Text(
                                    'Metode pembayaran baru berhasil ditambahkan!'),
                                backgroundColor: AppColors.incomeGreen,
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
                            'Tambahkan Metode Pembayaran',
                            style: TextStyle(fontWeight: FontWeight.bold),
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
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundLight,
      body: SafeArea(
        child: AnimatedBuilder(
          animation: _orgRepo,
          builder: (context, _) {
            final methods = _orgRepo.current.paymentMethods;

            return Column(
              children: [
                // Top Bar
                Padding(
                  padding: const EdgeInsets.fromLTRB(16, 12, 16, 8),
                  child: Row(
                    children: [
                      IconButton(
                        onPressed: () => Navigator.pop(context),
                        icon: Container(
                          padding: const EdgeInsets.all(8),
                          decoration: BoxDecoration(
                            color: Colors.white,
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(color: AppColors.borderSubtle),
                          ),
                          child: const Icon(Icons.arrow_back, size: 18),
                        ),
                      ),
                      const SizedBox(width: 8),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Metode Pembayaran Kas',
                            style:
                                Theme.of(context).textTheme.titleMedium?.copyWith(
                                      fontWeight: FontWeight.bold,
                                      color: AppColors.textPrimaryLight,
                                    ),
                          ),
                          const Text(
                            'Kelola QRIS, rekening bank, & saklar aktif/nonaktif',
                            style: TextStyle(
                              color: AppColors.textSecondaryLight,
                              fontSize: 11,
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),

                // List Metode Pembayaran
                Expanded(
                  child: ListView(
                    padding: const EdgeInsets.fromLTRB(16, 8, 16, 32),
                    children: [
                      // Banner Edukasi
                      Container(
                        padding: const EdgeInsets.all(14),
                        decoration: BoxDecoration(
                          color: AppColors.surfaceLavender,
                          borderRadius: BorderRadius.circular(16),
                          border: Border.all(color: AppColors.borderSubtle),
                        ),
                        child: const Row(
                          children: [
                            Icon(Icons.info_outline,
                                color: AppColors.primaryRoyal, size: 20),
                            SizedBox(width: 10),
                            Expanded(
                              child: Text(
                                'Aktifkan atau nonaktifkan metode pembayaran sesuai rekening resmi organisasi. Warga hanya dapat melihat metode yang berstatus aktif.',
                                style: TextStyle(
                                  fontSize: 12,
                                  color: AppColors.textPrimaryLight,
                                  height: 1.4,
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 16),

                      // Item-Item Metode
                      ...methods.map((m) {
                        return Container(
                          margin: const EdgeInsets.only(bottom: 12),
                          padding: const EdgeInsets.all(16),
                          decoration: BoxDecoration(
                            color: Colors.white,
                            borderRadius: BorderRadius.circular(18),
                            border: Border.all(
                              color: m.isActive
                                  ? AppColors.primaryRoyal.withOpacity(0.3)
                                  : AppColors.borderSubtle,
                              width: m.isActive ? 1.5 : 1.0,
                            ),
                            boxShadow: [
                              BoxShadow(
                                color: Colors.black.withOpacity(0.02),
                                blurRadius: 8,
                                offset: const Offset(0, 3),
                              ),
                            ],
                          ),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                children: [
                                  Container(
                                    padding: const EdgeInsets.all(8),
                                    decoration: BoxDecoration(
                                      color: m.isActive
                                          ? AppColors.surfaceLavender
                                          : const Color(0xFFF1F5F9),
                                      borderRadius: BorderRadius.circular(10),
                                    ),
                                    child: Icon(
                                      m.type == 'qris'
                                          ? Icons.qr_code_2
                                          : Icons.account_balance,
                                      color: m.isActive
                                          ? AppColors.primaryRoyal
                                          : AppColors.textMutedLight,
                                      size: 20,
                                    ),
                                  ),
                                  const SizedBox(width: 12),
                                  Expanded(
                                    child: Column(
                                      crossAxisAlignment:
                                          CrossAxisAlignment.start,
                                      children: [
                                        Text(
                                          m.title,
                                          style: const TextStyle(
                                            fontSize: 14,
                                            fontWeight: FontWeight.bold,
                                            color: AppColors.textPrimaryLight,
                                          ),
                                        ),
                                        Text(
                                          m.accountNumber.isNotEmpty
                                              ? m.accountNumber
                                              : '(Nomor rekening belum diatur)',
                                          style: TextStyle(
                                            fontSize: 12,
                                            fontWeight: FontWeight.w600,
                                            color: m.accountNumber.isNotEmpty
                                                ? AppColors.textSecondaryLight
                                                : Colors.orange,
                                          ),
                                        ),
                                      ],
                                    ),
                                  ),
                                  // Switch Toggle
                                  Switch(
                                    value: m.isActive,
                                    activeColor: AppColors.primaryRoyal,
                                    onChanged: (val) {
                                      _orgRepo.togglePaymentMethod(m.id, val);
                                    },
                                  ),
                                ],
                              ),
                              const SizedBox(height: 10),
                              const Divider(height: 1, color: Color(0xFFF1F5F9)),
                              const SizedBox(height: 8),

                              Row(
                                mainAxisAlignment:
                                    MainAxisAlignment.spaceBetween,
                                children: [
                                  Text(
                                    'Atas Nama: ${m.accountName.isNotEmpty ? m.accountName : "-"}',
                                    style: const TextStyle(
                                      fontSize: 11,
                                      color: AppColors.textSecondaryLight,
                                    ),
                                  ),
                                  Row(
                                    children: [
                                      TextButton.icon(
                                        onPressed: () =>
                                            _showEditMethodModal(context, m),
                                        icon: const Icon(Icons.edit_outlined,
                                            size: 14),
                                        label: const Text('Ubah Data',
                                            style: TextStyle(fontSize: 11)),
                                        style: TextButton.styleFrom(
                                          foregroundColor:
                                              AppColors.primaryRoyal,
                                          padding: const EdgeInsets.symmetric(
                                              horizontal: 8),
                                        ),
                                      ),
                                      if (m.type == 'custom' ||
                                          m.id.startsWith('pm_')) ...[
                                        IconButton(
                                          icon: const Icon(Icons.delete_outline,
                                              size: 16, color: Colors.red),
                                          onPressed: () {
                                            _orgRepo.deletePaymentMethod(m.id);
                                          },
                                        ),
                                      ],
                                    ],
                                  ),
                                ],
                              ),
                            ],
                          ),
                        );
                      }),
                      const SizedBox(height: 12),

                      // Tombol Tambah Rekening / E-Wallet Baru
                      OutlinedButton.icon(
                        onPressed: () => _showAddCustomMethodModal(context),
                        icon: const Icon(Icons.add_circle_outline, size: 18),
                        label: const Text('Tambah Rekening / E-Wallet Lain'),
                        style: OutlinedButton.styleFrom(
                          foregroundColor: AppColors.primaryRoyal,
                          side: const BorderSide(
                              color: AppColors.primaryRoyal, width: 1.2),
                          padding: const EdgeInsets.symmetric(vertical: 12),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(14),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            );
          },
        ),
      ),
    );
  }
}
