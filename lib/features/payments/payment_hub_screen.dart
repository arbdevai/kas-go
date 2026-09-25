import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../core/constants/app_colors.dart';
import '../../core/utils/app_toast.dart';
import '../../core/utils/formatters.dart';
import '../../data/repositories/billing_repository.dart';
import '../../data/repositories/finance_repository.dart';
import '../../data/repositories/organization_repository.dart';
import '../../data/repositories/user_profile_repository.dart';
import '../admin/payment_settings_screen.dart';

class PaymentHubScreen extends StatefulWidget {
  const PaymentHubScreen({super.key});

  @override
  State<PaymentHubScreen> createState() => _PaymentHubScreenState();
}

class _PaymentHubScreenState extends State<PaymentHubScreen> {
  int _selectedTab = 0; // 0: Transfer & QRIS, 1: Tagihan Iuran Saya, 2: Jemput Tunai

  @override
  Widget build(BuildContext context) {
    final orgRepo = OrganizationRepository.instance;
    final userRepo = UserProfileRepository.instance;

    return Scaffold(
      backgroundColor: AppColors.backgroundLight,
      body: SafeArea(
        bottom: false,
        child: AnimatedBuilder(
          animation: Listenable.merge([orgRepo, userRepo]),
          builder: (context, _) {
            final isAdmin = userRepo.current.isAdmin;

            return Column(
              children: [
                // Header Modern
                Padding(
                  padding: const EdgeInsets.fromLTRB(20, 16, 20, 12),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                'Pembayaran Kas',
                                style: Theme.of(context)
                                    .textTheme
                                    .headlineSmall
                                    ?.copyWith(
                                      fontWeight: FontWeight.w800,
                                      color: AppColors.textPrimaryLight,
                                      letterSpacing: -0.5,
                                    ),
                              ),
                              const SizedBox(height: 2),
                              Text(
                                orgRepo.organizationTitle.isNotEmpty
                                    ? orgRepo.organizationTitle
                                    : 'Iuran kas bulanan dan donasi warga',
                                style: const TextStyle(
                                  color: AppColors.textSecondaryLight,
                                  fontSize: 12,
                                ),
                              ),
                            ],
                          ),
                          if (isAdmin)
                            IconButton(
                              onPressed: () {
                                Navigator.push(
                                  context,
                                  MaterialPageRoute<void>(
                                    builder: (_) => const PaymentSettingsScreen(),
                                  ),
                                );
                              },
                              tooltip: 'Kelola Rekening & QRIS',
                              icon: Container(
                                padding: const EdgeInsets.all(8),
                                decoration: BoxDecoration(
                                  color: AppColors.surfaceLavender,
                                  borderRadius: BorderRadius.circular(12),
                                  border:
                                      Border.all(color: AppColors.borderSubtle),
                                ),
                                child: const Icon(
                                  Icons.tune,
                                  color: AppColors.primaryRoyal,
                                  size: 18,
                                ),
                              ),
                            ),
                        ],
                      ),
                      const SizedBox(height: 16),

                      // Segmented Tabs Pill (3 Tab)
                      Container(
                        padding: const EdgeInsets.all(3),
                        decoration: BoxDecoration(
                          color: AppColors.surfaceLavender,
                          borderRadius: BorderRadius.circular(14),
                        ),
                        child: Row(
                          children: [
                            _buildTabButton(0, 'Bayar Online'),
                            _buildTabButton(1, 'Tagihan Saya'),
                            _buildTabButton(2, 'Jemput Tunai'),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),

                // Tab Content
                Expanded(
                  child: AnimatedSwitcher(
                    duration: const Duration(milliseconds: 220),
                    child: KeyedSubtree(
                      key: ValueKey(_selectedTab),
                      child: _buildTabContent(),
                    ),
                  ),
                ),
              ],
            );
          },
        ),
      ),
    );
  }

  Widget _buildTabButton(int index, String label) {
    final isSelected = _selectedTab == index;
    return Expanded(
      child: GestureDetector(
        onTap: () => setState(() => _selectedTab = index),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 8),
          decoration: BoxDecoration(
            color: isSelected ? Colors.white : Colors.transparent,
            borderRadius: BorderRadius.circular(11),
            boxShadow: isSelected
                ? [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.06),
                      blurRadius: 6,
                      offset: const Offset(0, 2),
                    ),
                  ]
                : null,
          ),
          child: Text(
            label,
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: 12,
              fontWeight: isSelected ? FontWeight.bold : FontWeight.w500,
              color: isSelected
                  ? AppColors.primaryRoyal
                  : AppColors.textSecondaryLight,
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildTabContent() {
    switch (_selectedTab) {
      case 0:
        return const _DynamicPaymentMethodsTab();
      case 1:
        return const _MyBillsTab();
      case 2:
      default:
        return const _PickupTab();
    }
  }
}

/// Tab 1: Metode Pembayaran Dinamis (QRIS, Bank, E-Wallet)
class _DynamicPaymentMethodsTab extends StatelessWidget {
  const _DynamicPaymentMethodsTab();

  @override
  Widget build(BuildContext context) {
    final orgRepo = OrganizationRepository.instance;
    final activeMethods = orgRepo.activePaymentMethods;

    if (activeMethods.isEmpty) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                width: 56,
                height: 56,
                decoration: const BoxDecoration(
                  color: AppColors.surfaceLavender,
                  shape: BoxShape.circle,
                ),
                child: const Icon(
                  Icons.payment_outlined,
                  color: AppColors.primaryRoyal,
                  size: 28,
                ),
              ),
              const SizedBox(height: 12),
              const Text(
                'Belum Ada Rekening Aktif',
                style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
              ),
              const SizedBox(height: 4),
              const Text(
                'Pengurus belum mengaktifkan metode pembayaran rekening atau QRIS. Silakan hubungi admin kas.',
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: AppColors.textSecondaryLight,
                  fontSize: 12,
                ),
              ),
            ],
          ),
        ),
      );
    }

    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 4, 16, 96),
      children: [
        // Pisahkan QRIS dan Rekening Bank/E-Wallet
        ...activeMethods.map((m) {
          if (m.type == 'qris') {
            return _buildQrisCard(context, m);
          } else {
            return _buildBankCard(context, m);
          }
        }),

        // Langkah Konfirmasi
        Container(
          padding: const EdgeInsets.all(18),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(20),
            border: Border.all(color: AppColors.borderSubtle),
          ),
          child: const Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Icon(Icons.checklist_rtl_outlined,
                      size: 18, color: AppColors.primaryRoyal),
                  SizedBox(width: 8),
                  Text(
                    'Petunjuk Pembayaran Kas',
                    style: TextStyle(
                      fontSize: 13,
                      fontWeight: FontWeight.bold,
                      color: AppColors.textPrimaryLight,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 10),
              const _StepRow(
                number: '1',
                text: 'Transfer atau scan QRIS sesuai nominal iuran.',
              ),
              const _StepRow(
                number: '2',
                text: 'Beri berita transfer: Iuran Kas - [Nama Anda].',
              ),
              const _StepRow(
                number: '3',
                text: 'Buka tab Tagihan Saya lalu klik konfirmasi bayar.',
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildQrisCard(BuildContext context, PaymentMethodItem m) {
    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: AppColors.borderSubtle),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.02),
            blurRadius: 12,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Container(
                padding: const EdgeInsets.all(6),
                decoration: BoxDecoration(
                  color: AppColors.surfaceLavender,
                  borderRadius: BorderRadius.circular(8),
                ),
                child: const Icon(
                  Icons.verified_outlined,
                  color: AppColors.primaryRoyal,
                  size: 18,
                ),
              ),
              const SizedBox(width: 8),
              Flexible(
                child: Text(
                  m.accountName.toUpperCase(),
                  style: const TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.bold,
                    letterSpacing: 0.5,
                    color: AppColors.textPrimaryLight,
                  ),
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            ],
          ),
          const SizedBox(height: 4),
          Text(
            '${m.accountNumber} • Semua E-Wallet & Bank',
            style: const TextStyle(
              fontSize: 11,
              color: AppColors.textSecondaryLight,
            ),
          ),
          const SizedBox(height: 16),

          // Tampilan QR
          Container(
            width: 190,
            height: 190,
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(20),
              border: Border.all(color: AppColors.borderSubtle, width: 2),
            ),
            child: Center(
              child: Icon(
                Icons.qr_code_2,
                size: 160,
                color: AppColors.primaryRoyal.withOpacity(0.85),
              ),
            ),
          ),
          const SizedBox(height: 14),

          Text(
            m.instructions ??
                'Mendukung seluruh m-Banking dan e-Wallet berlogo QRIS.',
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 12,
              color: AppColors.textSecondaryLight,
            ),
          ),
          const SizedBox(height: 14),

          OutlinedButton.icon(
            onPressed: () {
              Clipboard.setData(ClipboardData(text: m.accountNumber));
              AppToast.info(context, 'Kode QRIS disalin');
            },
            icon: const Icon(Icons.copy, size: 16),
            label: const Text('Salin Kode QRIS'),
            style: OutlinedButton.styleFrom(
              foregroundColor: AppColors.primaryRoyal,
              side: const BorderSide(color: AppColors.primaryRoyal),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBankCard(BuildContext context, PaymentMethodItem m) {
    return Container(
      width: double.infinity,
      margin: const EdgeInsets.only(bottom: 16),
      padding: const EdgeInsets.all(22),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [AppColors.heroPurpleStart, AppColors.heroPurpleEnd],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(24),
        boxShadow: [
          BoxShadow(
            color: AppColors.primaryRoyal.withOpacity(0.24),
            blurRadius: 18,
            offset: const Offset(0, 6),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                m.title.toUpperCase(),
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 18,
                  fontWeight: FontWeight.w900,
                  letterSpacing: 1.2,
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.18),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(
                  m.type == 'ewallet' ? 'E-Wallet Resmi' : 'Rekening Kas',
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 10,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 20),

          const Text(
            'Nomor Rekening / Pembayaran',
            style: TextStyle(
              color: AppColors.textOnPurpleMuted,
              fontSize: 11,
            ),
          ),
          const SizedBox(height: 4),
          Row(
            children: [
              Expanded(
                child: Text(
                  m.accountNumber,
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                    letterSpacing: 1.8,
                    fontFeatures: [FontFeature.tabularFigures()],
                  ),
                ),
              ),
              IconButton(
                onPressed: () {
                  Clipboard.setData(ClipboardData(text: m.accountNumber));
                  AppToast.info(context, 'Nomor rekening ${m.title} disalin');
                },
                icon: Container(
                  padding: const EdgeInsets.all(6),
                  decoration: BoxDecoration(
                    color: Colors.white.withOpacity(0.2),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: const Icon(Icons.copy, size: 16, color: Colors.white),
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),

          const Text(
            'Atas Nama',
            style: TextStyle(
              color: AppColors.textOnPurpleMuted,
              fontSize: 11,
            ),
          ),
          Text(
            m.accountName,
            style: const TextStyle(
              color: Colors.white,
              fontSize: 13,
              fontWeight: FontWeight.bold,
            ),
          ),
        ],
      ),
    );
  }
}

/// Tab 2: Tagihan Iuran Bulanan Saya (Warga)
class _MyBillsTab extends StatelessWidget {
  const _MyBillsTab();

  @override
  Widget build(BuildContext context) {
    final billingRepo = BillingRepository.instance;
    final userRepo = UserProfileRepository.instance;
    final currentUserName = userRepo.current.name;

    return AnimatedBuilder(
      animation: billingRepo,
      builder: (context, _) {
        final allBills = billingRepo.allBills;
        final myEntries = billingRepo.getBillsForMember(currentUserName);

        if (allBills.isEmpty) {
          return Center(
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Container(
                    width: 56,
                    height: 56,
                    decoration: const BoxDecoration(
                      color: AppColors.surfaceLavender,
                      shape: BoxShape.circle,
                    ),
                    child: const Icon(
                      Icons.receipt_long_outlined,
                      color: AppColors.primaryRoyal,
                      size: 28,
                    ),
                  ),
                  const SizedBox(height: 12),
                  const Text(
                    'Tidak Ada Tagihan Aktif',
                    style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                  ),
                  const SizedBox(height: 4),
                  const Text(
                    'Pengurus belum menerbitkan tagihan iuran kas bulanan.',
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      color: AppColors.textSecondaryLight,
                      fontSize: 12,
                    ),
                  ),
                ],
              ),
            ),
          );
        }

        return ListView(
          padding: const EdgeInsets.fromLTRB(16, 4, 16, 96),
          children: [
            // Kartu Ringkasan
            Container(
              padding: const EdgeInsets.all(16),
              margin: const EdgeInsets.only(bottom: 14),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(18),
                border: Border.all(color: AppColors.borderSubtle),
              ),
              child: Row(
                children: [
                  Container(
                    width: 40,
                    height: 40,
                    decoration: const BoxDecoration(
                      color: AppColors.surfaceLavender,
                      shape: BoxShape.circle,
                    ),
                    child: const Icon(Icons.person,
                        color: AppColors.primaryRoyal, size: 20),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          currentUserName,
                          style: const TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.bold,
                            color: AppColors.textPrimaryLight,
                          ),
                        ),
                        Text(
                          '${myEntries.length} tagihan tercatat atas nama Anda',
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
            ),

            // Daftar Tagihan
            ...allBills.map((bill) {
              // Cek entri tagihan user untuk bill ini
              final entry = myEntries.firstWhere(
                (e) => e.billId == bill.id,
                orElse: () => MemberBillEntry(
                  id: 'virtual_${bill.id}',
                  billId: bill.id,
                  memberId: 'me',
                  memberName: currentUserName,
                  amount: bill.amount,
                  period: bill.period,
                  status: BillPaymentStatus.belumBayar,
                ),
              );

              final isPaid = entry.isPaid;
              final isWaiting =
                  entry.status == BillPaymentStatus.menungguVerifikasi;

              return Container(
                margin: const EdgeInsets.only(bottom: 12),
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(18),
                  border: Border.all(
                    color: isPaid
                        ? AppColors.incomeGreen.withOpacity(0.3)
                        : AppColors.borderSubtle,
                  ),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Expanded(
                          child: Text(
                            bill.title,
                            style: const TextStyle(
                              fontSize: 14,
                              fontWeight: FontWeight.bold,
                              color: AppColors.textPrimaryLight,
                            ),
                          ),
                        ),
                        Container(
                          padding: const EdgeInsets.symmetric(
                              horizontal: 8, vertical: 3),
                          decoration: BoxDecoration(
                            color: isPaid
                                ? AppColors.incomeGreenBg
                                : isWaiting
                                    ? const Color(0xFFFEF3C7)
                                    : AppColors.expenseRedBg,
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: Text(
                            entry.status.label,
                            style: TextStyle(
                              fontSize: 11,
                              fontWeight: FontWeight.bold,
                              color: isPaid
                                  ? AppColors.incomeGreen
                                  : isWaiting
                                      ? const Color(0xFFB45309)
                                      : AppColors.expenseRed,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 6),
                    Text(
                      'Periode: ${bill.period} • Jatuh Tempo: ${formatTanggal(bill.dueDate)}',
                      style: const TextStyle(
                        fontSize: 11,
                        color: AppColors.textSecondaryLight,
                      ),
                    ),
                    const SizedBox(height: 10),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          formatRupiah(bill.amount),
                          style: const TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.w900,
                            color: AppColors.primaryRoyal,
                          ),
                        ),
                        if (!isPaid && !isWaiting)
                          FilledButton.tonal(
                            onPressed: () {
                              _showConfirmPaymentSheet(context, entry);
                            },
                            child: const Text('Konfirmasi Bayar'),
                          )
                        else if (isWaiting)
                          const Text(
                            'Menunggu verifikasi admin kas',
                            style: TextStyle(
                              fontSize: 11,
                              fontStyle: FontStyle.italic,
                              color: Color(0xFFB45309),
                            ),
                          ),
                      ],
                    ),
                  ],
                ),
              );
            }),
          ],
        );
      },
    );
  }

  void _showConfirmPaymentSheet(BuildContext context, MemberBillEntry entry) {
    String method = 'Transfer Bank';
    showModalBottomSheet<void>(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      backgroundColor: Colors.white,
      builder: (ctx) {
        return SafeArea(
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Konfirmasi Pembayaran Iuran',
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 4),
                Text(
                  'Periode ${entry.period} • Nominal ${formatRupiah(entry.amount)}',
                  style: const TextStyle(
                      fontSize: 12, color: AppColors.textSecondaryLight),
                ),
                const SizedBox(height: 16),
                const Text(
                  'Metode yang Anda Gunakan',
                  style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 6),
                DropdownButtonFormField<String>(
                  value: method,
                  dropdownColor: Colors.white,
                  items: const [
                    DropdownMenuItem(
                        value: 'Transfer Bank', child: Text('Transfer Bank')),
                    DropdownMenuItem(
                        value: 'QRIS', child: Text('Scan QRIS')),
                    DropdownMenuItem(
                        value: 'Tunai', child: Text('Kas Tunai')),
                  ],
                  onChanged: (v) {
                    if (v != null) method = v;
                  },
                ),
                const SizedBox(height: 20),
                SizedBox(
                  width: double.infinity,
                  height: 48,
                  child: ElevatedButton(
                    onPressed: () {
                      BillingRepository.instance.requestPaymentVerification(
                        entryId: entry.id,
                        paymentMethod: method,
                      );
                      Navigator.pop(ctx);
                      AppToast.success(
                        context,
                        'Konfirmasi pembayaran iuran diajukan',
                      );
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.primaryRoyal,
                      foregroundColor: Colors.white,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(14),
                      ),
                    ),
                    child: const Text('Kirim Konfirmasi ke Admin'),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}

/// Tab 3: Request Penjemputan Kas ke Rumah
class _PickupTab extends StatefulWidget {
  const _PickupTab();

  @override
  State<_PickupTab> createState() => _PickupTabState();
}

class _PickupTabState extends State<_PickupTab> {
  final _formKey = GlobalKey<FormState>();
  final _nameCtrl = TextEditingController();
  final _addressCtrl = TextEditingController();
  final _phoneCtrl = TextEditingController();
  final _amountCtrl = TextEditingController(text: '25000');
  String _timeSlot = 'Sore (16:00 - 18:00)';

  static const _slots = [
    'Pagi (08:00 - 11:00)',
    'Siang (13:00 - 15:00)',
    'Sore (16:00 - 18:00)',
    'Malam (19:00 - 21:00)',
  ];

  @override
  void initState() {
    super.initState();
    final profile = UserProfileRepository.instance.current;
    _nameCtrl.text = profile.name;
    _phoneCtrl.text = profile.phone;
    _addressCtrl.text = profile.address;
  }

  @override
  void dispose() {
    _nameCtrl.dispose();
    _addressCtrl.dispose();
    _phoneCtrl.dispose();
    _amountCtrl.dispose();
    super.dispose();
  }

  void _submit() {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    final amount = int.parse(_amountCtrl.text.replaceAll('.', ''));

    FinanceRepository.instance.addPickupRequest(
      name: _nameCtrl.text.trim(),
      address: _addressCtrl.text.trim(),
      phone: _phoneCtrl.text.trim(),
      amount: amount,
      timeSlot: _timeSlot,
    );

    AppToast.success(
      context,
      'Permintaan jemput kas ${formatRupiah(amount)} berhasil diajukan',
    );
  }

  @override
  Widget build(BuildContext context) {
    return Form(
      key: _formKey,
      child: ListView(
        padding: const EdgeInsets.fromLTRB(16, 4, 16, 96),
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
                      'Jemput Setoran Tunai',
                      style: TextStyle(
                        fontSize: 13,
                        fontWeight: FontWeight.bold,
                        color: AppColors.textPrimaryLight,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                const Text(
                  'Petugas akan menjemput setoran iuran tunai langsung ke alamat Anda sesuai jadwal.',
                  style: TextStyle(
                    fontSize: 12,
                    color: AppColors.textSecondaryLight,
                    height: 1.4,
                  ),
                ),
                const SizedBox(height: 16),

                // Nama Lengkap
                const Text(
                  'Nama Lengkap',
                  style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 6),
                TextFormField(
                  controller: _nameCtrl,
                  decoration:
                      const InputDecoration(hintText: 'Nama lengkap Anda'),
                  validator: (v) =>
                      (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
                ),
                const SizedBox(height: 14),

                // Nomor WhatsApp
                const Text(
                  'Nomor WhatsApp Aktif',
                  style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 6),
                TextFormField(
                  controller: _phoneCtrl,
                  keyboardType: TextInputType.phone,
                  decoration: const InputDecoration(
                    hintText: 'Nomor WhatsApp untuk konfirmasi',
                  ),
                  validator: (v) =>
                      (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
                ),
                const SizedBox(height: 14),

                // Alamat Rumah
                const Text(
                  'Alamat Rumah & RT/RW',
                  style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 6),
                TextFormField(
                  controller: _addressCtrl,
                  decoration: const InputDecoration(
                    hintText: 'Contoh: Jl. Melati No. 8 RT 02 / RW 05',
                  ),
                  validator: (v) =>
                      (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
                ),
                const SizedBox(height: 14),

                // Perkiraan Nominal
                const Text(
                  'Nominal Iuran yang Diserahkan (Rp)',
                  style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 6),
                TextFormField(
                  controller: _amountCtrl,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(hintText: 'Contoh: 25000'),
                  validator: (v) =>
                      (v == null || int.tryParse(v.replaceAll('.', '')) == null)
                          ? 'Nominal tidak valid'
                          : null,
                ),
                const SizedBox(height: 14),

                // Waktu Jemput
                const Text(
                  'Pilihan Jam Penjemputan',
                  style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 6),
                DropdownButtonFormField<String>(
                  value: _timeSlot,
                  dropdownColor: Colors.white,
                  items: _slots
                      .map((s) => DropdownMenuItem(value: s, child: Text(s)))
                      .toList(),
                  onChanged: (v) {
                    if (v != null) setState(() => _timeSlot = v);
                  },
                ),
                const SizedBox(height: 24),

                // Tombol Ajukan
                SizedBox(
                  width: double.infinity,
                  height: 48,
                  child: ElevatedButton.icon(
                    onPressed: _submit,
                    icon: const Icon(Icons.send, size: 18),
                    label: const Text(
                      'Ajukan Penjemputan',
                      style: TextStyle(fontWeight: FontWeight.bold),
                    ),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.primaryRoyal,
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
    );
  }
}

class _StepRow extends StatelessWidget {
  const _StepRow({required this.number, required this.text});

  final String number;
  final String text;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 20,
            height: 20,
            decoration: BoxDecoration(
              color: AppColors.surfaceLavender,
              shape: BoxShape.circle,
              border: Border.all(color: AppColors.borderSubtle),
            ),
            child: Center(
              child: Text(
                number,
                style: const TextStyle(
                  fontSize: 10,
                  fontWeight: FontWeight.bold,
                  color: AppColors.primaryRoyal,
                ),
              ),
            ),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Text(
              text,
              style: const TextStyle(
                fontSize: 12,
                color: AppColors.textSecondaryLight,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
