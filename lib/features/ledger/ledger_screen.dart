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
      appBar: AppBar(
        title: const Text('Riwayat Transaksi'),
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 12, 16, 4),
            child: Row(
              children: [
                _FilterChip(
                  label: 'Semua',
                  selected: _filter == _Filter.all,
                  onTap: () => setState(() => _filter = _Filter.all),
                ),
                const SizedBox(width: 8),
                _FilterChip(
                  label: 'Pemasukan',
                  selected: _filter == _Filter.income,
                  onTap: () => setState(() => _filter = _Filter.income),
                  color: AppColors.incomeLight,
                ),
                const SizedBox(width: 8),
                _FilterChip(
                  label: 'Pengeluaran',
                  selected: _filter == _Filter.expense,
                  onTap: () => setState(() => _filter = _Filter.expense),
                  color: AppColors.expenseLight,
                ),
              ],
            ),
          ),
          Expanded(
            child: entries.isEmpty
                ? const Center(child: Text('Tidak ada data'))
                : ListView.separated(
                    padding: const EdgeInsets.all(16),
                    itemCount: entries.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 8),
                    itemBuilder: (context, i) {
                      final e = entries[i];
                      final isIncome = e['entryType'] == LedgerType.income;
                      final color = isIncome
                          ? AppColors.incomeLight
                          : AppColors.expenseLight;
                      return Card(
                        child: ListTile(
                          leading: CircleAvatar(
                            backgroundColor: color.withOpacity(0.12),
                            child: Icon(
                              isIncome
                                  ? Icons.arrow_downward
                                  : Icons.arrow_upward,
                              color: color,
                              size: 20,
                            ),
                          ),
                          title: Text(e['summary'] as String),
                          subtitle: Text(
                            'Dicatat oleh: ${e['recordedByName']}',
                            style: const TextStyle(fontSize: 11),
                          ),
                          trailing: Text(
                            '${isIncome ? '+' : '-'}${formatRupiah(e['amount'] as int)}',
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              color: color,
                              fontSize: 13,
                            ),
                          ),
                        ),
                      );
                    },
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
    final active = color ?? AppColors.primaryLight;
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 150),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 7),
        decoration: BoxDecoration(
          color: selected ? active : Colors.transparent,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: selected ? active : const Color(0xFFCBD5E1)),
        ),
        child: Text(
          label,
          style: TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.w500,
            color: selected ? Colors.white : const Color(0xFF475569),
          ),
        ),
      ),
    );
  }
}
