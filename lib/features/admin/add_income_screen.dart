import 'package:flutter/material.dart';
import 'package:kas_go/core/constants/app_colors.dart';

class AddIncomeScreen extends StatefulWidget {
  const AddIncomeScreen({super.key});

  @override
  State<AddIncomeScreen> createState() => _AddIncomeScreenState();
}

class _AddIncomeScreenState extends State<AddIncomeScreen> {
  final _formKey = GlobalKey<FormState>();
  String _member = 'Ahmad';
  String _period = '2026-09';
  int _amount = 0;
  String _method = 'Tunai';
  final _notesCtrl = TextEditingController();

  static const _members = ['Ahmad', 'Budi', 'Citra'];
  static const _methods = ['Tunai', 'QRIS', 'Transfer BCA', 'Jemput'];

  @override
  void dispose() {
    _notesCtrl.dispose();
    super.dispose();
  }

  void _save() {
    if (!_formKey.currentState!.validate()) return;
    _formKey.currentState!.save();
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
          'Mode demo: kas Rp $_amount dari $_member ($_method) dicatat (simulasi).',
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Catat Kas Masuk')),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            DropdownButtonFormField<String>(
              value: _member,
              decoration: const InputDecoration(
                labelText: 'Nama anggota',
                border: OutlineInputBorder(),
              ),
              items: _members
                  .map((m) => DropdownMenuItem(value: m, child: Text(m)))
                  .toList(),
              onChanged: (v) => setState(() => _member = v ?? 'Ahmad'),
            ),
            const SizedBox(height: 12),
            TextFormField(
              initialValue: _period,
              decoration: const InputDecoration(
                labelText: 'Periode (cth. 2026-09)',
                border: OutlineInputBorder(),
              ),
              validator: (v) =>
                  (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
              onSaved: (v) => _period = v!.trim(),
            ),
            const SizedBox(height: 12),
            TextFormField(
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(
                labelText: 'Nominal (Rp)',
                border: OutlineInputBorder(),
                prefixText: 'Rp ',
              ),
              validator: (v) {
                final n = int.tryParse((v ?? '').replaceAll('.', ''));
                if (n == null || n <= 0) return 'Masukkan nominal valid';
                return null;
              },
              onSaved: (v) =>
                  _amount = int.parse(v!.replaceAll('.', '')),
            ),
            const SizedBox(height: 12),
            Text('Metode pembayaran',
                style: Theme.of(context).textTheme.titleSmall),
            const SizedBox(height: 8),
            Wrap(
              spacing: 8,
              children: _methods
                  .map(
                    (m) => ChoiceChip(
                      label: Text(m),
                      selected: _method == m,
                      onSelected: (_) => setState(() => _method = m),
                      selectedColor:
                          AppColors.incomeGreenBg,
                    ),
                  )
                  .toList(),
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _notesCtrl,
              maxLines: 3,
              decoration: const InputDecoration(
                labelText: 'Catatan (opsional)',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 24),
            SizedBox(
              height: 48,
              child: FilledButton(
                onPressed: _save,
                child: const Text('Simpan'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
