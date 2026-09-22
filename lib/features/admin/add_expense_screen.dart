import 'package:flutter/material.dart';

class AddExpenseScreen extends StatefulWidget {
  const AddExpenseScreen({super.key});

  @override
  State<AddExpenseScreen> createState() => _AddExpenseScreenState();
}

class _AddExpenseScreenState extends State<AddExpenseScreen> {
  final _formKey = GlobalKey<FormState>();
  String _category = 'Operasional';
  int? _amount;
  final _recipientCtrl = TextEditingController();
  final _descCtrl = TextEditingController();
  DateTime _date = DateTime.now();

  static const _categories = [
    'Operasional',
    'Sosial',
    'Perlengkapan',
    'Kegiatan',
  ];

  @override
  void dispose() {
    _recipientCtrl.dispose();
    _descCtrl.dispose();
    super.dispose();
  }

  void _save() {
    if (!_formKey.currentState!.validate()) return;
    _formKey.currentState!.save();
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Mode demo: data contoh — tidak tersimpan.'),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Catat Pengeluaran')),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            DropdownButtonFormField<String>(
              initialValue: _category,
              decoration: const InputDecoration(
                labelText: 'Kategori',
                border: OutlineInputBorder(),
              ),
              items: _categories
                  .map((c) => DropdownMenuItem(value: c, child: Text(c)))
                  .toList(),
              onChanged: (v) => setState(() => _category = v ?? 'Operasional'),
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
            TextFormField(
              controller: _recipientCtrl,
              decoration: const InputDecoration(
                labelText: 'Penerima / Vendor',
                border: OutlineInputBorder(),
              ),
              validator: (v) =>
                  (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _descCtrl,
              maxLines: 2,
              decoration: const InputDecoration(
                labelText: 'Keperluan / Keterangan',
                border: OutlineInputBorder(),
              ),
              validator: (v) =>
                  (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
            ),
            const SizedBox(height: 12),
            ListTile(
              contentPadding: EdgeInsets.zero,
              title: const Text('Tanggal kejadian'),
              subtitle: Text(
                '${_date.day}/${_date.month}/${_date.year}',
              ),
              trailing: const Icon(Icons.calendar_today, size: 20),
              onTap: () async {
                final picked = await showDatePicker(
                  context: context,
                  initialDate: _date,
                  firstDate: DateTime(2025),
                  lastDate: DateTime(2027),
                );
                if (picked != null) setState(() => _date = picked);
              },
            ),
            const SizedBox(height: 8),
            OutlinedButton.icon(
              onPressed: null, // Disabled in demo mode
              icon: const Icon(Icons.photo_camera),
              label: const Text('Upload bukti nota (khusus mode produksi)'),
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
