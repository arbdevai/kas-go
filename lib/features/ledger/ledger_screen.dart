import 'package:flutter/material.dart';
import 'package:kas_go/core/constants/app_colors.dart';
import 'package:kas_go/core/utils/formatters.dart';
import 'package:kas_go/data/demo/demo_data.dart';
import 'package:kas_go/data/local/tables/app_tables.dart';

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
    final entries = DemoData.entries.where((e) {
      if (_filter == _Filter.income) {
        return e['entryType'] == LedgerType.income;
      }
      if (_filter == _Filter.expense) {
        return e['entryType'] == LedgerType.expense;
      }
      return true;
    }).toList();

    return Scaffold(
      backgroundColor: AppColors.backgroundLight,
      appBar: AppBar(
        title: const Text('Transparansi Kas'),
        backgroundColor: Colors.transparent,
      ),
      body: Column(
        children: [
          // Filter Chips Row
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 12),
            child: Row(
              children: [
                _FilterChip(
                  label: 'Semua Transaksi',
                  selected: _filter == _Filter.all,
                  onTap: () => setState(() => _filter = _Filter.all),
                ),
                const SizedBox(width: 8),
                _FilterChip(
                  label: 'Pemasukan',
                  selected: _filter == _Filter.income,
                  onTap: () => setState(() => _filter = _Filter.income),
                  color: AppColors.incomeGreen,
                ),
                const SizedBox(width: 8),
                _FilterChip(
                  label: 'Pengeluaran',
                  selected: _filter == _Filter.expense,
                  onTap: () => setState(() => _filter = _Filter.expense),
                  color: AppColors.expenseRed,
                ),
              ],
            ),
          ),

          // Grouped Surface List (No Card Fatigue)
          Expanded(
            child: entries.isEmpty
                ? const Center(
                    child: Text(
                      'Tidak ada riwayat transaksi pada filter ini',
                      style: TextStyle(color: AppColors.textSecondaryLight),
                    ),
                  )
                : SingleChildScrollView(
                    padding: const EdgeInsets.fromLTRB(16, 0, 16, 24),
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
                        itemCount: entries.length,
                        separatorBuilder: (_, __) => const Divider(
                          height: 1,
                          indent: 68,
                          color: Color(0xFFF1F5F9),
                        ),
                        itemBuilder: (context, i) {
                          final e = entries[i];
                          final isIncome = e['entryType'] == LedgerType.income;
                          final color = isIncome
                              ? AppColors.incomeGreen
                              : AppColors.expenseRed;
                          final bgColor = isIncome
                              ? AppColors.incomeGreenBg
                              : AppColors.expenseRedBg;
                          final amount = e['amount'] as int;
                          final summary = e['summary'] as String;
                          final recorder = e['recordedByName'] as String;
                          final detail = e['category'] ?? e['period'] ?? 'Kas';

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
                                        summary,
                                        style: const TextStyle(
                                          fontWeight: FontWeight.w600,
                                          fontSize: 13,
                                          color: AppColors.textPrimaryLight,
                                        ),
                                        maxLines: 1,
                                        overflow: TextOverflow.ellipsis,
                                      ),
                                      const SizedBox(height: 3),
                                      Text(
                                        'Oleh: $recorder • $detail',
                                        style: const TextStyle(
                                          fontSize: 11,
                                          color: AppColors.textSecondaryLight,
                                        ),
                                        maxLines: 1,
                                        overflow: TextOverflow.ellipsis,
                                      ),
                                    ],
                                  ),
                                ),
                                const SizedBox(width: 10),
                                Text(
                                  '${isIncome ? '+' : '-'}${formatRupiah(amount)}',
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
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 7),
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
