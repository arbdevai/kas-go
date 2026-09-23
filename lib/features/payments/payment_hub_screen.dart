import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:kas_go/core/constants/app_colors.dart';

class PaymentHubScreen extends StatelessWidget {
  const PaymentHubScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 3,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Pembayaran Kas'),
          bottom: const TabBar(
            tabs: [
              Tab(text: 'QRIS'),
              Tab(text: 'Transfer BCA'),
              Tab(text: 'Jemput di Rumah'),
            ],
          ),
        ),
        body: const TabBarView(
          children: [
            _QrisTab(),
            _BcaTab(),
            _PickupTab(),
          ],
        ),
      ),
    );
  }
}

class _QrisTab extends StatelessWidget {
  const _QrisTab();

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(24),
      children: [
        Card(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              children: [
                Icon(
                  Icons.qr_code_2,
                  size: 160,
                  color: AppColors.primaryRoyal.withOpacity(0.4),
                ),
                const SizedBox(height: 16),
                const Text(
                  'QRIS Karang Taruna',
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 8),
                const Text(
                  'QRIS resmi akan tampil di sini setelah akun produksi terkonfigurasi.',
                  textAlign: TextAlign.center,
                  style: TextStyle(fontSize: 13, color: Colors.grey),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 16),
        const Text(
          'Cara bayar:\n1. Buka aplikasi m-banking atau e-wallet Anda.\n2. Pindai kode QR di atas.\n3. Masukkan nominal iuran kas.\n4. Konfirmasi pembayaran. Admin akan memverifikasi secara manual.',
          style: TextStyle(fontSize: 13, height: 1.5),
        ),
      ],
    );
  }
}

class _BcaTab extends StatelessWidget {
  const _BcaTab();

  static const _accNo = '000-000-0000';
  static const _accName = 'Karang Taruna (Demo)';

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Card(
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Transfer Bank BCA',
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 16),
                const Text('Nomor Rekening',
                    style: TextStyle(fontSize: 12, color: Colors.grey)),
                const SizedBox(height: 4),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      _accNo,
                      style: TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                        letterSpacing: 1.2,
                      ),
                    ),
                    IconButton(
                      tooltip: 'Salin',
                      icon: const Icon(Icons.copy),
                      onPressed: () {
                        Clipboard.setData(const ClipboardData(text: _accNo));
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                            content: Text('Nomor rekening disalin'),
                          ),
                        );
                      },
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                const Text('Atas Nama',
                    style: TextStyle(fontSize: 12, color: Colors.grey)),
                const SizedBox(height: 4),
                const Text(
                  _accName,
                  style: TextStyle(fontSize: 15, fontWeight: FontWeight.w500),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 16),
        const Card(
          color: Color(0xFFFEF3C7),
          child: Padding(
            padding: EdgeInsets.all(12),
            child: Text(
              'Perhatian: Verifikasi pembayaran transfer dilakukan secara manual oleh salah satu dari 3 admin. Simpan bukti transfer Anda.',
              style: TextStyle(fontSize: 12, color: Color(0xFF92400E)),
            ),
          ),
        ),
      ],
    );
  }
}

class _PickupTab extends StatefulWidget {
  const _PickupTab();

  @override
  State<_PickupTab> createState() => _PickupTabState();
}

class _PickupTabState extends State<_PickupTab> {
  final _formKey = GlobalKey<FormState>();
  final _nameCtrl = TextEditingController();
  final _addrCtrl = TextEditingController();
  final _phoneCtrl = TextEditingController();
  final _notesCtrl = TextEditingController();

  @override
  void dispose() {
    _nameCtrl.dispose();
    _addrCtrl.dispose();
    _phoneCtrl.dispose();
    _notesCtrl.dispose();
    super.dispose();
  }

  void _submit() {
    if (!_formKey.currentState!.validate()) return;
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Mode demo: permohonan jemput tidak tersimpan.'),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Form(
      key: _formKey,
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          const Text(
            'Admin keliling akan datang ke rumah Anda untuk mengambil setoran kas.',
            style: TextStyle(fontSize: 13, color: Colors.grey),
          ),
          const SizedBox(height: 16),
          TextFormField(
            controller: _nameCtrl,
            decoration: const InputDecoration(
              labelText: 'Nama lengkap',
              border: OutlineInputBorder(),
            ),
            validator: (v) =>
                (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
          ),
          const SizedBox(height: 12),
          TextFormField(
            controller: _addrCtrl,
            decoration: const InputDecoration(
              labelText: 'Alamat / RT / RW',
              border: OutlineInputBorder(),
            ),
            validator: (v) =>
                (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
          ),
          const SizedBox(height: 12),
          TextFormField(
            controller: _phoneCtrl,
            keyboardType: TextInputType.phone,
            decoration: const InputDecoration(
              labelText: 'Nomor WhatsApp / HP',
              border: OutlineInputBorder(),
            ),
            validator: (v) =>
                (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
          ),
          const SizedBox(height: 12),
          TextFormField(
            controller: _notesCtrl,
            maxLines: 2,
            decoration: const InputDecoration(
              labelText: 'Catatan waktu luang (cth. sore setelah 16:00)',
              border: OutlineInputBorder(),
            ),
          ),
          const SizedBox(height: 24),
          SizedBox(
            height: 48,
            child: FilledButton(
              onPressed: _submit,
              child: const Text('Ajukan Permohonan Jemput'),
            ),
          ),
        ],
      ),
    );
  }
}
