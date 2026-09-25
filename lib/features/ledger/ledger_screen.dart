import 'package:flutter/material.dart';
import '../../core/constants/app_colors.dart';
import '../../core/utils/app_toast.dart';
import '../../core/utils/formatters.dart';
import '../../data/repositories/billing_repository.dart';
import '../../data/repositories/finance_repository.dart';
import '../../data/repositories/user_profile_repository.dart';

class LedgerScreen extends StatefulWidget {
  const LedgerScreen({super.key});

  @override
  State<LedgerScreen> createState() => _LedgerScreenState();
}

enum _Filter { all, income, expense }
enum _MainTab { transactions, recap }

class _LedgerScreenState extends State<LedgerScreen> {
  _MainTab _mainTab = _MainTab.transactions;
  _Filter _filter = _Filter.all;

  @override
  Widget build(BuildContext context) {
    final financeRepo = FinanceRepository.instance;
    final billingRepo = BillingRepository.instance;
    final userRepo = UserProfileRepository.instance;

    return Scaffold(
      backgroundColor: AppColors.backgroundLight,
      body: SafeArea(
        bottom: false,
        child: AnimatedBuilder(
          animation: Listenable.merge([financeRepo, billingRepo, userRepo]),
          builder: (context, _) {
            final allItems = financeRepo.allTransactions;
            final filtered = allItems.where((tx) {
              if (_filter == _Filter.income) {
                return tx.isIncome;
              }
              if (_filter == _Filter.expense) {
                return !tx.isIncome;
              }
              return true;
            }).toList();

            return Column(
              crossAxisAlignment: CrossAxisAlignment.start,
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
                                'Buku Kas',
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
                              const Text(
                                'Riwayat mutasi kas dan rekapitulasi iuran',
                                style: TextStyle(
                                  color: AppColors.textSecondaryLight,
                                  fontSize: 12,
                                ),
                              ),
                            ],
                          ),
                          Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 10,
                              vertical: 4,
                            ),
                            decoration: BoxDecoration(
                              color: AppColors.surfaceLavender,
                              borderRadius: BorderRadius.circular(12),
                            ),
                            child: Text(
                              '${allItems.length} Transaksi',
                              style: const TextStyle(
                                color: AppColors.primaryRoyal,
                                fontSize: 11,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 14),

                      // Segmented Tab Utama: Mutasi Kas vs Rekapitulasi
                      Container(
                        padding: const EdgeInsets.all(3),
                        decoration: BoxDecoration(
                          color: AppColors.surfaceLavender,
                          borderRadius: BorderRadius.circular(14),
                        ),
                        child: Row(
                          children: [
                            Expanded(
                              child: _buildMainTabButton(
                                tab: _MainTab.transactions,
                                label: 'Mutasi Kas',
                                icon: Icons.receipt_long,
                              ),
                            ),
                            Expanded(
                              child: _buildMainTabButton(
                                tab: _MainTab.recap,
                                label: 'Rekapitulasi',
                                icon: Icons.analytics_outlined,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),

                // Konten Tab
                Expanded(
                  child: _mainTab == _MainTab.transactions
                      ? _buildTransactionsTab(filtered, userRepo.current.isAdmin)
                      : _buildRecapTab(financeRepo, billingRepo),
                ),
              ],
            );
          },
        ),
      ),
    );
  }

  Widget _buildMainTabButton({
    required _MainTab tab,
    required String label,
    required IconData icon,
  }) {
    final isSelected = _mainTab == tab;
    return GestureDetector(
      onTap: () => setState(() => _mainTab = tab),
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
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              icon,
              size: 15,
              color: isSelected
                  ? AppColors.primaryRoyal
                  : AppColors.textSecondaryLight,
            ),
            const SizedBox(width: 6),
            Text(
              label,
              style: TextStyle(
                fontSize: 12,
                fontWeight: isSelected ? FontWeight.bold : FontWeight.w600,
                color: isSelected
                    ? AppColors.primaryRoyal
                    : AppColors.textSecondaryLight,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTransactionsTab(List<TransactionItem> filtered, bool isAdmin) {
    return Column(
      children: [
        // Filter Chips Row
        Padding(
          padding: const EdgeInsets.fromLTRB(20, 0, 20, 10),
          child: SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(
              children: [
                _FilterChip(
                  label: 'Semua Transaksi',
                  selected: _filter == _Filter.all,
                  onTap: () => setState(() => _filter = _Filter.all),
                ),
                const SizedBox(width: 8),
                _FilterChip(
                  label: 'Pemasukan Kas',
                  selected: _filter == _Filter.income,
                  onTap: () => setState(() => _filter = _Filter.income),
                  color: AppColors.incomeGreen,
                ),
                const SizedBox(width: 8),
                _FilterChip(
                  label: 'Pengeluaran Kas',
                  selected: _filter == _Filter.expense,
                  onTap: () => setState(() => _filter = _Filter.expense),
                  color: AppColors.expenseRed,
                ),
              ],
            ),
          ),
        ),

        // List Transaksi
        Expanded(
          child: filtered.isEmpty
              ? Center(
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
                          Icons.receipt_outlined,
                          color: AppColors.primaryRoyal,
                          size: 28,
                        ),
                      ),
                      const SizedBox(height: 12),
                      const Text(
                        'Belum ada transaksi kas',
                        style: TextStyle(
                          color: AppColors.textPrimaryLight,
                          fontWeight: FontWeight.bold,
                          fontSize: 14,
                        ),
                      ),
                      const SizedBox(height: 4),
                      const Text(
                        'Catatan arus kas masuk & keluar akan muncul di sini',
                        style: TextStyle(
                          color: AppColors.textSecondaryLight,
                          fontSize: 12,
                        ),
                      ),
                    ],
                  ),
                )
              : ListView.builder(
                  padding: const EdgeInsets.fromLTRB(16, 0, 16, 96),
                  itemCount: filtered.length,
                  itemBuilder: (context, i) {
                    final tx = filtered[i];
                    final isIncome = tx.isIncome;
                    final color = isIncome
                        ? AppColors.incomeGreen
                        : AppColors.expenseRed;
                    final bgColor = isIncome
                        ? AppColors.incomeGreenBg
                        : AppColors.expenseRedBg;
                    final categoryOrPeriod = tx.category ?? tx.period ?? 'Umum';

                    return Card(
                      margin: const EdgeInsets.only(bottom: 8),
                      elevation: 0,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(16),
                        side: const BorderSide(
                          color: AppColors.borderSubtle,
                          width: 1,
                        ),
                      ),
                      color: Colors.white,
                      child: InkWell(
                        borderRadius: BorderRadius.circular(16),
                        onTap: () => _showAuditTrailModal(context, tx, isAdmin),
                        child: Padding(
                          padding: const EdgeInsets.all(14),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                children: [
                                  Container(
                                    width: 36,
                                    height: 36,
                                    decoration: BoxDecoration(
                                      color: bgColor,
                                      shape: BoxShape.circle,
                                    ),
                                    child: Icon(
                                      isIncome
                                          ? Icons.arrow_downward
                                          : Icons.arrow_upward,
                                      color: color,
                                      size: 18,
                                    ),
                                  ),
                                  const SizedBox(width: 12),
                                  Expanded(
                                    child: Column(
                                      crossAxisAlignment:
                                          CrossAxisAlignment.start,
                                      children: [
                                        Text(
                                          tx.summary,
                                          style: const TextStyle(
                                            fontWeight: FontWeight.bold,
                                            fontSize: 13,
                                            color: AppColors.textPrimaryLight,
                                          ),
                                          maxLines: 1,
                                          overflow: TextOverflow.ellipsis,
                                        ),
                                        const SizedBox(height: 2),
                                        Text(
                                          formatTanggalDanJam(tx.occurredAt),
                                          style: const TextStyle(
                                            fontSize: 11,
                                            color: AppColors.textSecondaryLight,
                                          ),
                                        ),
                                      ],
                                    ),
                                  ),
                                  const SizedBox(width: 8),
                                  Text(
                                    '${isIncome ? '+' : '-'}${formatRupiah(tx.amount)}',
                                    style: TextStyle(
                                      fontWeight: FontWeight.w800,
                                      color: color,
                                      fontSize: 14,
                                      fontFeatures: const [
                                        FontFeature.tabularFigures(),
                                      ],
                                    ),
                                  ),
                                ],
                              ),
                              const SizedBox(height: 8),
                              const Divider(height: 1, color: Color(0xFFF1F5F9)),
                              const SizedBox(height: 8),

                              // Info Petugas & Status Koreksi
                              Row(
                                mainAxisAlignment:
                                    MainAxisAlignment.spaceBetween,
                                children: [
                                  Row(
                                    children: [
                                      const Icon(
                                        Icons.person_outline,
                                        size: 13,
                                        color: AppColors.textSecondaryLight,
                                      ),
                                      const SizedBox(width: 4),
                                      Text(
                                        'Oleh: ${tx.recordedByName}',
                                        style: const TextStyle(
                                          fontSize: 11,
                                          color: AppColors.textSecondaryLight,
                                          fontWeight: FontWeight.w500,
                                        ),
                                      ),
                                    ],
                                  ),
                                  Row(
                                    children: [
                                      Container(
                                        padding: const EdgeInsets.symmetric(
                                          horizontal: 6,
                                          vertical: 2,
                                        ),
                                        decoration: BoxDecoration(
                                          color: AppColors.surfaceLavender,
                                          borderRadius:
                                              BorderRadius.circular(6),
                                        ),
                                        child: Text(
                                          categoryOrPeriod,
                                          style: const TextStyle(
                                            fontSize: 10,
                                            fontWeight: FontWeight.bold,
                                            color: AppColors.primaryRoyal,
                                          ),
                                        ),
                                      ),
                                      if (tx.wasEdited) ...[
                                        const SizedBox(width: 6),
                                        Container(
                                          padding: const EdgeInsets.symmetric(
                                            horizontal: 6,
                                            vertical: 2,
                                          ),
                                          decoration: BoxDecoration(
                                            color: const Color(0xFFFEF3C7),
                                            borderRadius:
                                                BorderRadius.circular(6),
                                          ),
                                          child: const Text(
                                            'Telah Dikoreksi',
                                            style: TextStyle(
                                              fontSize: 10,
                                              fontWeight: FontWeight.bold,
                                              color: Color(0xFFB45309),
                                            ),
                                          ),
                                        ),
                                      ],
                                    ],
                                  ),
                                ],
                              ),
                            ],
                          ),
                        ),
                      ),
                    );
                  },
                ),
        ),
      ],
    );
  }

  Widget _buildRecapTab(
    FinanceRepository financeRepo,
    BillingRepository billingRepo,
  ) {
    final balance = financeRepo.totalBalance;
    final totalIn = financeRepo.totalIncome;
    final totalOut = financeRepo.totalExpense;
    final recaps = billingRepo.getAllRecaps();
    final expenseCats = financeRepo.expenseByCategory;

    return SingleChildScrollView(
      padding: const EdgeInsets.fromLTRB(16, 4, 16, 96),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // 1. KARTU REKAPITULASI ARUS KAS GLOBAL
          Container(
            padding: const EdgeInsets.all(18),
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                colors: [AppColors.heroPurpleStart, AppColors.heroPurpleEnd],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
              borderRadius: BorderRadius.circular(20),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'REKAP TOTAL SALDO ORGANISASI',
                  style: TextStyle(
                    color: AppColors.textOnPurpleMuted,
                    fontSize: 11,
                    fontWeight: FontWeight.bold,
                    letterSpacing: 0.5,
                  ),
                ),
                const SizedBox(height: 6),
                Text(
                  formatRupiah(balance),
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 24,
                    fontWeight: FontWeight.w900,
                  ),
                ),
                const SizedBox(height: 16),
                Row(
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text(
                            'Total Kas Masuk',
                            style: TextStyle(
                              color: AppColors.textOnPurpleMuted,
                              fontSize: 11,
                            ),
                          ),
                          Text(
                            formatRupiah(totalIn),
                            style: const TextStyle(
                              color: Color(0xFF6EE7B7),
                              fontSize: 14,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ],
                      ),
                    ),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text(
                            'Total Pengeluaran',
                            style: TextStyle(
                              color: AppColors.textOnPurpleMuted,
                              fontSize: 11,
                            ),
                          ),
                          Text(
                            formatRupiah(totalOut),
                            style: const TextStyle(
                              color: Color(0xFFFCA5A5),
                              fontSize: 14,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(height: 20),

          // 2. REKAPITULASI TAGIHAN IURAN WARGA
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
                'Rekapitulasi Tagihan Iuran Warga',
                style: TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.bold,
                  color: AppColors.textPrimaryLight,
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          if (recaps.isEmpty)
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(18),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: AppColors.borderSubtle),
              ),
              child: const Text(
                'Belum ada tagihan bulanan yang diterbitkan oleh admin.',
                style: TextStyle(
                  color: AppColors.textSecondaryLight,
                  fontSize: 12,
                ),
              ),
            )
          else
            ...recaps.map((r) {
              return Container(
                margin: const EdgeInsets.only(bottom: 10),
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(18),
                  border: Border.all(color: AppColors.borderSubtle),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          r.bill.title,
                          style: const TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.bold,
                            color: AppColors.textPrimaryLight,
                          ),
                        ),
                        Text(
                          '${formatRupiah(r.bill.amount)} / warga',
                          style: const TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.bold,
                            color: AppColors.primaryRoyal,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    ClipRRect(
                      borderRadius: BorderRadius.circular(4),
                      child: LinearProgressIndicator(
                        value: r.collectionPercentage / 100,
                        backgroundColor: const Color(0xFFF1F5F9),
                        color: AppColors.incomeGreen,
                        minHeight: 6,
                      ),
                    ),
                    const SizedBox(height: 8),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          'Lunas: ${r.paidCount} / ${r.totalMembers} Warga (${r.collectionPercentage.toStringAsFixed(0)}%)',
                          style: const TextStyle(
                            fontSize: 11,
                            color: AppColors.incomeGreen,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        Text(
                          'Terkumpul: ${formatRupiah(r.totalCollected)}',
                          style: const TextStyle(
                            fontSize: 11,
                            fontWeight: FontWeight.bold,
                            color: AppColors.textPrimaryLight,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              );
            }),
          const SizedBox(height: 20),

          // 3. REKAPITULASI PENGELUARAN PER KATEGORI
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
                'Rekap Pengeluaran per Kategori',
                style: TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.bold,
                  color: AppColors.textPrimaryLight,
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          if (expenseCats.isEmpty)
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(18),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: AppColors.borderSubtle),
              ),
              child: const Text(
                'Belum ada catatan pengeluaran kas.',
                style: TextStyle(
                  color: AppColors.textSecondaryLight,
                  fontSize: 12,
                ),
              ),
            )
          else
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(18),
                border: Border.all(color: AppColors.borderSubtle),
              ),
              child: Column(
                children: expenseCats.entries.map((e) {
                  final pct = totalOut > 0 ? (e.value / totalOut) : 0.0;
                  return Padding(
                    padding: const EdgeInsets.symmetric(vertical: 6),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(
                              e.key,
                              style: const TextStyle(
                                fontSize: 12,
                                fontWeight: FontWeight.bold,
                                color: AppColors.textPrimaryLight,
                              ),
                            ),
                            Text(
                              '${formatRupiah(e.value)} (${(pct * 100).toStringAsFixed(0)}%)',
                              style: const TextStyle(
                                fontSize: 12,
                                fontWeight: FontWeight.bold,
                                color: AppColors.expenseRed,
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 4),
                        ClipRRect(
                          borderRadius: BorderRadius.circular(4),
                          child: LinearProgressIndicator(
                            value: pct,
                            backgroundColor: const Color(0xFFF1F5F9),
                            color: AppColors.expenseRed,
                            minHeight: 5,
                          ),
                        ),
                      ],
                    ),
                  );
                }).toList(),
              ),
            ),
        ],
      ),
    );
  }

  void _showAuditTrailModal(
    BuildContext context,
    TransactionItem tx,
    bool isAdmin,
  ) {
    showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      backgroundColor: Colors.white,
      builder: (ctx) {
        return SafeArea(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Header Sheet
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
                          'Detail Transaksi',
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

                // Nominal & Jenis
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: tx.isIncome
                        ? AppColors.incomeGreenBg
                        : AppColors.expenseRedBg,
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        tx.isIncome ? 'KAS MASUK' : 'PENGELUARAN KAS',
                        style: TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.bold,
                          color: tx.isIncome
                              ? AppColors.incomeGreen
                              : AppColors.expenseRed,
                          letterSpacing: 0.5,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        '${tx.isIncome ? '+' : '-'}${formatRupiah(tx.amount)}',
                        style: TextStyle(
                          fontSize: 22,
                          fontWeight: FontWeight.w900,
                          color: tx.isIncome
                              ? AppColors.incomeGreen
                              : AppColors.expenseRed,
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 16),

                // Detail Rincian
                _buildAuditRow('Keperluan / Uraian', tx.summary),
                _buildAuditRow('Waktu Transaksi', formatTanggalDanJam(tx.occurredAt)),
                _buildAuditRow('Dicatat Oleh', tx.recordedByName),
                _buildAuditRow('Metode Pembayaran', tx.paymentMethod),
                if (tx.category != null)
                  _buildAuditRow('Kategori', tx.category!),
                if (tx.period != null)
                  _buildAuditRow('Periode Iuran', tx.period!),

                // Riwayat Koreksi jika pernah diedit
                if (tx.wasEdited) ...[
                  const SizedBox(height: 12),
                  Container(
                    width: double.infinity,
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: const Color(0xFFFFFBEB),
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(color: const Color(0xFFFDE68A)),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Row(
                          children: [
                            Icon(Icons.edit_note,
                                size: 16, color: Color(0xFFB45309)),
                            SizedBox(width: 6),
                            Text(
                              'Catatan Koreksi',
                              style: TextStyle(
                                fontSize: 11,
                                fontWeight: FontWeight.bold,
                                color: Color(0xFFB45309),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 6),
                        Text(
                          'Diedit oleh: ${tx.editedByName}',
                          style: const TextStyle(
                            fontSize: 11,
                            fontWeight: FontWeight.w600,
                            color: Color(0xFF92400E),
                          ),
                        ),
                        if (tx.editedAt != null)
                          Text(
                            'Waktu koreksi: ${formatTanggalDanJam(tx.editedAt!)}',
                            style: const TextStyle(
                              fontSize: 10,
                              color: Color(0xFF92400E),
                            ),
                          ),
                        if (tx.editReason != null)
                          Text(
                            'Alasan: ${tx.editReason}',
                            style: const TextStyle(
                              fontSize: 11,
                              fontStyle: FontStyle.italic,
                              color: Color(0xFF78350F),
                            ),
                          ),
                      ],
                    ),
                  ),
                ],

                // Tombol Koreksi khusus Admin
                if (isAdmin) ...[
                  const SizedBox(height: 18),
                  SizedBox(
                    width: double.infinity,
                    height: 44,
                    child: OutlinedButton.icon(
                      onPressed: () {
                        Navigator.pop(ctx);
                        _showEditTransactionDialog(context, tx);
                      },
                      icon: const Icon(Icons.edit, size: 16),
                      label: const Text('Koreksi Transaksi'),
                      style: OutlinedButton.styleFrom(
                        foregroundColor: AppColors.primaryRoyal,
                        side: const BorderSide(color: AppColors.primaryRoyal),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                      ),
                    ),
                  ),
                ],
              ],
            ),
          ),
        );
      },
    );
  }

  void _showEditTransactionDialog(BuildContext context, TransactionItem tx) {
    final amountCtrl = TextEditingController(text: tx.amount.toString());
    final summaryCtrl = TextEditingController(text: tx.summary);
    final reasonCtrl = TextEditingController();
    final formKey = GlobalKey<FormState>();

    showDialog<void>(
      context: context,
      builder: (ctx) {
        return AlertDialog(
          shape:
              RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
          title: const Text(
            'Koreksi Transaksi Kas',
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
          content: Form(
            key: formKey,
            child: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Nominal (Rp)',
                    style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 4),
                  TextFormField(
                    controller: amountCtrl,
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(hintText: 'Contoh: 50000'),
                    validator: (v) =>
                        (v == null || int.tryParse(v) == null || int.parse(v) <= 0)
                            ? 'Nominal tidak valid'
                            : null,
                  ),
                  const SizedBox(height: 12),
                  const Text(
                    'Uraian / Keperluan',
                    style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 4),
                  TextFormField(
                    controller: summaryCtrl,
                    decoration:
                        const InputDecoration(hintText: 'Uraian transaksi'),
                    validator: (v) =>
                        (v == null || v.trim().isEmpty) ? 'Wajib diisi' : null,
                  ),
                  const SizedBox(height: 12),
                  const Text(
                    'Alasan Koreksi (Wajib untuk Audit)',
                    style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 4),
                  TextFormField(
                    controller: reasonCtrl,
                    decoration: const InputDecoration(
                      hintText: 'Contoh: Penyesuaian nota belanja toko',
                    ),
                    validator: (v) =>
                        (v == null || v.trim().isEmpty) ? 'Alasan wajib diisi' : null,
                  ),
                ],
              ),
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(ctx),
              child: const Text('Batal'),
            ),
            FilledButton(
              onPressed: () {
                if (!formKey.currentState!.validate()) return;
                final newAmount = int.parse(amountCtrl.text.trim());
                final newSummary = summaryCtrl.text.trim();
                final reason = reasonCtrl.text.trim();
                final currentAdmin =
                    UserProfileRepository.instance.current.name;

                FinanceRepository.instance.editTransaction(
                  id: tx.id,
                  newAmount: newAmount,
                  newSummary: newSummary,
                  newCategory: tx.category,
                  editorName: currentAdmin,
                  editReason: reason,
                );

                Navigator.pop(ctx);
                AppToast.success(context, 'Koreksi transaksi berhasil disimpan');
              },
              child: const Text('Simpan Koreksi'),
            ),
          ],
        );
      },
    );
  }

  Widget _buildAuditRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 140,
            child: Text(
              label,
              style: const TextStyle(
                fontSize: 12,
                color: AppColors.textSecondaryLight,
              ),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: const TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.bold,
                color: AppColors.textPrimaryLight,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _FilterChip extends StatelessWidget {
  const _FilterChip({
    required this.label,
    required this.selected,
    required this.onTap,
    this.color,
  });

  final String label;
  final bool selected;
  final VoidCallback onTap;
  final Color? color;

  @override
  Widget build(BuildContext context) {
    final active = color ?? AppColors.primaryRoyal;
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 7),
        decoration: BoxDecoration(
          color: selected ? active : Colors.white,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(
            color: selected ? active : AppColors.borderSubtle,
            width: 1,
          ),
        ),
        child: Text(
          label,
          style: TextStyle(
            color: selected ? Colors.white : AppColors.textSecondaryLight,
            fontSize: 12,
            fontWeight: selected ? FontWeight.bold : FontWeight.w500,
          ),
        ),
      ),
    );
  }
}
