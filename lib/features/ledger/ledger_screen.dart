import 'package:flutter/material.dart';
import 'package:kas_go/core/constants/app_colors.dart';
import 'package:kas_go/core/utils/formatters.dart';
import 'package:kas_go/data/local/tables/app_tables.dart';
import 'package:kas_go/data/repositories/finance_repository.dart';

class LedgerScreen extends StatefulWidget {
  const LedgerScreen({super.key});

  @override
  State<LedgerScreen> createState() => _LedgerScreenState();
}

enum _Filter { all, income, expense }

class _LedgerScreenState extends State<LedgerScreen> {
  _Filter _filter = _Filter.all;

  @override
  Widget build(BuildContext context) {
    final repo = FinanceRepository.instance;

    return Scaffold(
      backgroundColor: AppColors.backgroundLight,
      body: SafeArea(
        bottom: false,
        child: AnimatedBuilder(
          animation: repo,
          builder: (context, _) {
            final allItems = repo.allTransactions;
            final filtered = allItems.where((tx) {
              if (_filter == _Filter.income) return tx.isIncome;
              if (_filter == _Filter.expense) return !tx.isIncome;
              return true;
            }).toList();

            return Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Header Modern (Bukan AppBar Kaku)
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
                                'Transparansi Kas',
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
                                'Buku kas terbuka real-time seluruh warga',
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
                              '${filtered.length} Transaksi',
                              style: const TextStyle(
                                color: AppColors.primaryRoyal,
                                fontSize: 11,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),

                      // Filter Chips Row
                      SingleChildScrollView(
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
                    ],
                  ),
                ),

                // Daftar Transaksi Grouped Surface (Tanpa Card Fatigue)
                Expanded(
                  child: filtered.isEmpty
                      ? const Center(
                          child: Text(
                            'Tidak ada riwayat transaksi pada filter ini',
                            style: TextStyle(
                              color: AppColors.textSecondaryLight,
                              fontSize: 13,
                            ),
                          ),
                        )
                      : SingleChildScrollView(
                          padding: const EdgeInsets.fromLTRB(16, 0, 16, 96),
                          child: Container(
                            decoration: BoxDecoration(
                              color: Colors.white,
                              borderRadius: BorderRadius.circular(20),
                              border: Border.all(
                                color: AppColors.borderSubtle,
                                width: 1,
                              ),
                              boxShadow: [
                                BoxShadow(
                                  color: Colors.black.withOpacity(0.02),
                                  blurRadius: 10,
                                  offset: const Offset(0, 4),
                                ),
                              ],
                            ),
                            child: ListView.separated(
                              shrinkWrap: true,
                              physics: const NeverScrollableScrollPhysics(),
                              itemCount: filtered.length,
                              separatorBuilder: (_, __) => const Divider(
                                height: 1,
                                indent: 68,
                                color: Color(0xFFF1F5F9),
                              ),
                              itemBuilder: (context, i) {
                                final tx = filtered[i];
                                final isIncome = tx.isIncome;
                                final color = isIncome
                                    ? AppColors.incomeGreen
                                    : AppColors.expenseRed;
                                final bgColor = isIncome
                                    ? AppColors.incomeGreenBg
                                    : AppColors.expenseRedBg;
                                final detail =
                                    tx.category ?? tx.period ?? tx.paymentMethod;

                                return Padding(
                                  padding: const EdgeInsets.symmetric(
                                    horizontal: 16,
                                    vertical: 13,
                                  ),
                                  child: Row(
                                    children: [
                                      Container(
                                        width: 40,
                                        height: 40,
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
                                                fontWeight: FontWeight.w600,
                                                fontSize: 13,
                                                color:
                                                    AppColors.textPrimaryLight,
                                              ),
                                              maxLines: 1,
                                              overflow: TextOverflow.ellipsis,
                                            ),
                                            const SizedBox(height: 3),
                                            Text(
                                              'Oleh: ${tx.recordedByName} • $detail',
                                              style: const TextStyle(
                                                fontSize: 11,
                                                color:
                                                    AppColors.textSecondaryLight,
                                              ),
                                              maxLines: 1,
                                              overflow: TextOverflow.ellipsis,
                                            ),
                                          ],
                                        ),
                                      ),
                                      const SizedBox(width: 10),
                                      Text(
                                        '${isIncome ? '+' : '-'}${formatRupiah(tx.amount)}',
                                        style: TextStyle(
                                          fontWeight: FontWeight.w700,
                                          color: color,
                                          fontSize: 13,
                                          fontFeatures: const [
                                            FontFeature.tabularFigures(),
                                          ],
                                        ),
                                      ),
                                    ],
                                  ),
                                );
                              },
                            ),
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
            fontSize: 11,
            fontWeight: selected ? FontWeight.bold : FontWeight.w500,
            color: selected ? Colors.white : AppColors.textSecondaryLight,
          ),
        ),
      ),
    );
  }
}
