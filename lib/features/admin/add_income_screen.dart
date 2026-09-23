import 'package:flutter/material.dart';
import 'package:kas_go/core/constants/app_colors.dart';
import 'package:kas_go/core/utils/formatters.dart';
import 'package:kas_go/data/repositories/finance_repository.dart';

class AddIncomeScreen extends StatefulWidget {
  const AddIncomeScreen({super.key});

  @override
  State<AddIncomeScreen> createState() => _AddIncomeScreenState();
}

class _AddIncomeScreenState extends State<AddIncomeScreen> {
  final _formKey = GlobalKey<FormState>();
  String _member = 'Ahmad (RT 01)';
  String _recorder = 'Admin 1 (Bendahara)';
  String _period = '2026-09';
  int _amount = 50000;
  String _method = 'Tunai';
  final _notesCtrl = TextEditingController();
  final _amountCtrl = TextEditingController(text: '50000');

  static const _members = [
    'Ahmad (RT 01)',
    'Budi Santoso (RT 02)',
    'Citra Dewi (RT 02)',
    'Dedi Kurniawan (RT 03)',
    'Eka Pratama (RT 04)',
    'Fajar Ramadhan (RT 05)',
  ];

  static const _recorders = [
    'Admin 1 (Bendahara)',
    'Admin 2 (Sekretaris)',
    'Admin 3 (Koordinator Lapangan)',
  ];

  static const _methods = ['Tunai', 'QRIS', 'Transfer BCA', 'Jemput'];

  @override
  void dispose() {
    _notesCtrl.dispose();
    _amountCtrl.dispose();
    super.dispose();
  }

  void _save() {
    if (!_formKey.currentState!.validate()) {
      return;
    }
    _formKey.currentState!.save();

    // Simpan ke repository nyata & update saldo
    final parts = _recorder.split(' ');
    FinanceRepository.instance.recordIncome(
      memberName: _member,
      period: _period,
      amount: _amount,
      paymentMethod: _method,
      recorderName: '${parts[0]} ${parts[1]}',
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
                'Kas Masuk ${formatRupiah(_amount)} berhasil disimpan!',
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
    return Scaffold(
      backgroundColor: AppColors.backgroundLight,
      body: SafeArea(
        child: Column(
          children: [
            // Top Bar Modern
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
                        'Pencatatan iuran warga oleh Admin',
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

            // Form Container
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
                          // Admin Pencatat
                          const Text(
                            'Admin Pencatat (Audit)',
                            style: TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                              color: AppColors.textPrimaryLight,
                            ),
                          ),
                          const SizedBox(height: 6),
                          DropdownButtonFormField<String>(
                            value: _recorder,
                            dropdownColor: Colors.white,
                            style: const TextStyle(
                              color: AppColors.textPrimaryLight,
                              fontSize: 13,
                              fontWeight: FontWeight.w600,
                            ),
                            decoration: InputDecoration(
                              contentPadding: const EdgeInsets.symmetric(
                                horizontal: 14,
                                vertical: 12,
                              ),
                              border: OutlineInputBorder(
                                borderRadius: BorderRadius.circular(12),
                                borderSide: const BorderSide(
                                  color: AppColors.borderSubtle,
                                ),
                              ),
                            ),
                            items: _recorders
                                .map((r) => DropdownMenuItem(
                                      value: r,
                                      child: Text(
                                        r,
                                        style: const TextStyle(
                                          fontSize: 13,
                                          color: AppColors.textPrimaryLight,
                                        ),
                                      ),
                                    ))
                                .toList(),
                            onChanged: (v) => setState(() => _recorder = v!),
                          ),
                          const SizedBox(height: 16),

                          // Nama Warga
                          const Text(
                            'Nama Anggota / Warga',
                            style: TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                              color: AppColors.textPrimaryLight,
                            ),
                          ),
                          const SizedBox(height: 6),
                          DropdownButtonFormField<String>(
                            value: _member,
                            dropdownColor: Colors.white,
                            style: const TextStyle(
                              color: AppColors.textPrimaryLight,
                              fontSize: 13,
                              fontWeight: FontWeight.w600,
                            ),
                            decoration: InputDecoration(
                              contentPadding: const EdgeInsets.symmetric(
                                horizontal: 14,
                                vertical: 12,
                              ),
                              border: OutlineInputBorder(
                                borderRadius: BorderRadius.circular(12),
                                borderSide: const BorderSide(
                                  color: AppColors.borderSubtle,
                                ),
                              ),
                            ),
                            items: _members
                                .map((m) => DropdownMenuItem(
                                      value: m,
                                      child: Text(
                                        m,
                                        style: const TextStyle(
                                          fontSize: 13,
                                          color: AppColors.textPrimaryLight,
                                        ),
                                      ),
                                    ))
                                .toList(),
                            onChanged: (v) => setState(() => _member = v!),
                          ),
                          const SizedBox(height: 16),

                          // Periode Iuran
                          const Text(
                            'Periode Iuran',
                            style: TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                              color: AppColors.textPrimaryLight,
                            ),
                          ),
                          const SizedBox(height: 6),
                          TextFormField(
                            initialValue: _period,
                            style: const TextStyle(
                              color: AppColors.textPrimaryLight,
                              fontSize: 14,
                            ),
                            decoration: InputDecoration(
                              hintText: 'YYYY-MM (mis. 2026-09)',
                              contentPadding: const EdgeInsets.symmetric(
                                horizontal: 14,
                                vertical: 12,
                              ),
                              border: OutlineInputBorder(
                                borderRadius: BorderRadius.circular(12),
                                borderSide: const BorderSide(
                                  color: AppColors.borderSubtle,
                                ),
                              ),
                            ),
                            validator: (v) =>
                                (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
                            onSaved: (v) => _period = v!.trim(),
                          ),
                          const SizedBox(height: 16),

                          // Nominal Kas Masuk
                          const Text(
                            'Nominal Uang Kas',
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
                            style: const TextStyle(
                              color: AppColors.textPrimaryLight,
                              fontSize: 14,
                              fontWeight: FontWeight.bold,
                            ),
                            decoration: InputDecoration(
                              prefixText: 'Rp ',
                              prefixStyle: const TextStyle(
                                fontWeight: FontWeight.bold,
                                color: AppColors.primaryRoyal,
                              ),
                              contentPadding: const EdgeInsets.symmetric(
                                horizontal: 14,
                                vertical: 12,
                              ),
                              border: OutlineInputBorder(
                                borderRadius: BorderRadius.circular(12),
                                borderSide: const BorderSide(
                                  color: AppColors.borderSubtle,
                                ),
                              ),
                            ),
                            validator: (v) {
                              final n = int.tryParse((v ?? '').replaceAll('.', ''));
                              if (n == null || n <= 0) {
                                return 'Masukkan nominal valid';
                              }
                              return null;
                            },
                            onSaved: (v) =>
                                _amount = int.parse(v!.replaceAll('.', '')),
                          ),
                          const SizedBox(height: 16),

                          // Metode Pembayaran
                          const Text(
                            'Metode Penyetoran',
                            style: TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                              color: AppColors.textPrimaryLight,
                            ),
                          ),
                          const SizedBox(height: 8),
                          Wrap(
                            spacing: 8,
                            children: _methods.map((m) {
                              final isSelected = _method == m;
                              return ChoiceChip(
                                label: Text(m),
                                selected: isSelected,
                                onSelected: (_) => setState(() => _method = m),
                                selectedColor: AppColors.primaryRoyal,
                                labelStyle: TextStyle(
                                  color: isSelected ? Colors.white : AppColors.textPrimaryLight,
                                  fontWeight: isSelected ? FontWeight.bold : FontWeight.w500,
                                  fontSize: 12,
                                ),
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(10),
                                ),
                              );
                            }).toList(),
                          ),
                          const SizedBox(height: 16),

                          // Catatan
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
                            maxLines: 2,
                            decoration: InputDecoration(
                              hintText: 'Contoh: Titip lewat ketua RT',
                              contentPadding: const EdgeInsets.symmetric(
                                horizontal: 14,
                                vertical: 12,
                              ),
                              border: OutlineInputBorder(
                                borderRadius: BorderRadius.circular(12),
                                borderSide: const BorderSide(
                                  color: AppColors.borderSubtle,
                                ),
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 20),

                    // Tombol Simpan
                    SizedBox(
                      height: 50,
                      child: ElevatedButton(
                        onPressed: _save,
                        style: ElevatedButton.styleFrom(
                          backgroundColor: AppColors.primaryRoyal,
                          foregroundColor: Colors.white,
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(16),
                          ),
                          elevation: 2,
                        ),
                        child: const Text(
                          'Simpan Kas Masuk',
                          style: TextStyle(
                            fontSize: 15,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
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
