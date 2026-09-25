import 'package:flutter/material.dart';
import '../../core/constants/app_colors.dart';
import '../../core/utils/formatters.dart';
import '../../data/repositories/finance_repository.dart';
import '../../data/repositories/organization_repository.dart';
import '../../data/repositories/user_profile_repository.dart';

class AddIncomeScreen extends StatefulWidget {
  const AddIncomeScreen({super.key});

  @override
  State<AddIncomeScreen> createState() => _AddIncomeScreenState();
}

class _AddIncomeScreenState extends State<AddIncomeScreen> {
  final _formKey = GlobalKey<FormState>();
  final _memberCtrl = TextEditingController();
  final _amountCtrl = TextEditingController(text: '25000');
  final _notesCtrl = TextEditingController();

  late String _period;
  String _method = 'Tunai';
  late String _recorder;

  @override
  void initState() {
    super.initState();
    final now = DateTime.now();
    _period = '${now.year}-${now.month.toString().padLeft(2, '0')}';
    final profile = UserProfileRepository.instance.current;
    _recorder = '${profile.name} (${profile.roleTitle})';
  }

  @override
  void dispose() {
    _memberCtrl.dispose();
    _amountCtrl.dispose();
    _notesCtrl.dispose();
    super.dispose();
  }

  void _save() {
    if (!_formKey.currentState!.validate()) {
      return;
    }
    _formKey.currentState!.save();

    final amount = int.parse(_amountCtrl.text.replaceAll('.', '').trim());
    final memberName = _memberCtrl.text.trim();

    // Simpan ke repository & update saldo
    FinanceRepository.instance.recordIncome(
      memberName: memberName,
      period: _period,
      amount: amount,
      paymentMethod: _method,
      recorderName: _recorder,
      note: _notesCtrl.text.trim().isNotEmpty ? _notesCtrl.text.trim() : null,
    );

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        backgroundColor: AppColors.incomeGreen,
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        content: Row(
          children: [
            const Icon(Icons.check_circle_outline, color: Colors.white, size: 20),
            const SizedBox(width: 10),
            Expanded(
              child: Text(
                'Kas Masuk ${formatRupiah(amount)} untuk $memberName berhasil dicatat!',
                style: const TextStyle(fontWeight: FontWeight.bold),
              ),
            ),
          ],
        ),
      ),
    );

    Navigator.pop(context);
  }

  @override
  Widget build(BuildContext context) {
    final activeMethods = OrganizationRepository.instance.activePaymentMethods;
    final methodOptions = activeMethods.isNotEmpty
        ? activeMethods.map((m) => m.title).toList()
        : ['Tunai', 'QRIS', 'Transfer Bank', 'Jemput Tunai'];

    if (!methodOptions.contains(_method) && methodOptions.isNotEmpty) {
      _method = methodOptions.first;
    }

    return Scaffold(
      backgroundColor: AppColors.backgroundLight,
      body: SafeArea(
        child: Column(
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
                        'Catat Kas Masuk',
                        style: Theme.of(context).textTheme.titleMedium?.copyWith(
                              fontWeight: FontWeight.bold,
                              color: AppColors.textPrimaryLight,
                            ),
                      ),
                      const Text(
                        'Pencatatan iuran & kas masuk organisasi',
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

            // Form
            Expanded(
              child: Form(
                key: _formKey,
                child: ListView(
                  padding: const EdgeInsets.fromLTRB(16, 8, 16, 32),
                  children: [
                    Container(
                      padding: const EdgeInsets.all(20),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(20),
                        border: Border.all(color: AppColors.borderSubtle),
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          // Petugas Pencatat
                          const Text(
                            'Petugas Pencatat (Audit)',
                            style: TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                              color: AppColors.textPrimaryLight,
                            ),
                          ),
                          const SizedBox(height: 6),
                          Container(
                            width: double.infinity,
                            padding: const EdgeInsets.symmetric(
                              horizontal: 14,
                              vertical: 12,
                            ),
                            decoration: BoxDecoration(
                              color: const Color(0xFFF8F7FC),
                              borderRadius: BorderRadius.circular(12),
                              border: Border.all(color: AppColors.borderSubtle),
                            ),
                            child: Row(
                              children: [
                                const Icon(Icons.verified_user_outlined,
                                    size: 16, color: AppColors.primaryRoyal),
                                const SizedBox(width: 8),
                                Expanded(
                                  child: Text(
                                    _recorder,
                                    style: const TextStyle(
                                      fontSize: 13,
                                      fontWeight: FontWeight.w600,
                                      color: AppColors.textPrimaryLight,
                                    ),
                                  ),
                                ),
                              ],
                            ),
                          ),
                          const SizedBox(height: 16),

                          // Nama Warga Dinamis
                          const Text(
                            'Nama Warga / Pembayar',
                            style: TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                              color: AppColors.textPrimaryLight,
                            ),
                          ),
                          const SizedBox(height: 6),
                          TextFormField(
                            controller: _memberCtrl,
                            decoration: const InputDecoration(
                              hintText: 'Contoh: Bpk. Joko (RT 02)',
                              prefixIcon: Icon(Icons.person_outline, size: 20),
                            ),
                            validator: (v) =>
                                (v == null || v.trim().isEmpty) ? 'Nama warga wajib diisi' : null,
                          ),
                          const SizedBox(height: 16),

                          // Periode Iuran
                          const Text(
                            'Periode Kas / Iuran',
                            style: TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                              color: AppColors.textPrimaryLight,
                            ),
                          ),
                          const SizedBox(height: 6),
                          TextFormField(
                            initialValue: _period,
                            decoration: const InputDecoration(
                              hintText: 'YYYY-MM (Contoh: 2026-09)',
                              prefixIcon: Icon(Icons.calendar_today_outlined, size: 18),
                            ),
                            validator: (v) =>
                                (v == null || v.trim().isEmpty) ? 'Periode wajib diisi' : null,
                            onSaved: (v) => _period = v!.trim(),
                          ),
                          const SizedBox(height: 16),

                          // Nominal Kas Masuk
                          const Text(
                            'Nominal Kas Masuk (Rp)',
                            style: TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                              color: AppColors.textPrimaryLight,
                            ),
                          ),
                          const SizedBox(height: 6),
                          TextFormField(
                            controller: _amountCtrl,
                            keyboardType: TextInputType.number,
                            decoration: const InputDecoration(
                              hintText: 'Contoh: 25000',
                              prefixIcon: Icon(Icons.payments_outlined, size: 20),
                            ),
                            validator: (v) {
                              if (v == null || v.trim().isEmpty) {
                                return 'Nominal wajib diisi';
                              }
                              final num = int.tryParse(v.replaceAll('.', ''));
                              if (num == null || num <= 0) {
                                return 'Nominal harus angka lebih dari 0';
                              }
                              return null;
                            },
                          ),
                          const SizedBox(height: 16),

                          // Metode Pembayaran
                          const Text(
                            'Metode Pembayaran',
                            style: TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                              color: AppColors.textPrimaryLight,
                            ),
                          ),
                          const SizedBox(height: 6),
                          DropdownButtonFormField<String>(
                            value: _method,
                            dropdownColor: Colors.white,
                            items: methodOptions
                                .map((m) => DropdownMenuItem(
                                      value: m,
                                      child: Text(m, style: const TextStyle(fontSize: 13)),
                                    ))
                                .toList(),
                            onChanged: (v) {
                              if (v != null) setState(() => _method = v);
                            },
                          ),
                          const SizedBox(height: 16),

                          // Catatan Tambahan
                          const Text(
                            'Catatan Tambahan (Opsional)',
                            style: TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                              color: AppColors.textPrimaryLight,
                            ),
                          ),
                          const SizedBox(height: 6),
                          TextFormField(
                            controller: _notesCtrl,
                            decoration: const InputDecoration(
                              hintText: 'Contoh: Titipan iuran 2 bulan sekaligus',
                            ),
                          ),
                          const SizedBox(height: 24),

                          // Tombol Simpan
                          SizedBox(
                            width: double.infinity,
                            height: 48,
                            child: ElevatedButton.icon(
                              onPressed: _save,
                              icon: const Icon(Icons.check, size: 18),
                              label: const Text(
                                'Simpan Kas Masuk',
                                style: TextStyle(
                                  fontSize: 14,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                              style: ElevatedButton.styleFrom(
                                backgroundColor: AppColors.incomeGreen,
                                foregroundColor: Colors.white,
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(14),
                                ),
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
