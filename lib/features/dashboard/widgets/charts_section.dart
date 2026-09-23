import 'package:flutter/material.dart';
import '../../../core/constants/app_colors.dart';
import 'charts.dart';

/// Bagian Visualisasi Grafik dengan Segmented Tab.
///
/// Menghindari penumpukan 3 kartu grafik besar secara vertikal yang boros layar.
class ChartsSection extends StatefulWidget {
  const ChartsSection({
    super.key,
    required this.monthlyIncome,
    required this.expenseByCategory,
    required this.cumulativeBalance,
  });

  final Map<String, int> monthlyIncome;
  final Map<String, int> expenseByCategory;
  final List<MapEntry<String, int>> cumulativeBalance;

  @override
  State<ChartsSection> createState() => _ChartsSectionState();
}

class _ChartsSectionState extends State<ChartsSection> {
  int _selectedTab = 0;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: AppColors.borderSubtle, width: 1),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.02),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header Judul
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
              Text(
                'Analisis Keuangan Karang Taruna',
                style: Theme.of(context).textTheme.titleSmall?.copyWith(
                      fontWeight: FontWeight.bold,
                      color: AppColors.textPrimaryLight,
                    ),
              ),
            ],
          ),
          const SizedBox(height: 12),

          // Segmented Tabs Pill
          Container(
            padding: const EdgeInsets.all(3),
            decoration: BoxDecoration(
              color: AppColors.surfaceLavender,
              borderRadius: BorderRadius.circular(12),
            ),
            child: Row(
              children: [
                _buildSegmentButton(0, 'Pemasukan'),
                _buildSegmentButton(1, 'Pengeluaran'),
                _buildSegmentButton(2, 'Tren Saldo'),
              ],
            ),
          ),
          const SizedBox(height: 14),

          // Tampilan Chart Aktif
          AnimatedSwitcher(
            duration: const Duration(milliseconds: 250),
            child: KeyedSubtree(
              key: ValueKey(_selectedTab),
              child: _buildActiveChart(),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSegmentButton(int index, String label) {
    final isSelected = _selectedTab == index;
    return Expanded(
      child: GestureDetector(
        onTap: () => setState(() => _selectedTab = index),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 7),
          decoration: BoxDecoration(
            color: isSelected ? Colors.white : Colors.transparent,
            borderRadius: BorderRadius.circular(9),
            boxShadow: isSelected
                ? [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.06),
                      blurRadius: 4,
                      offset: const Offset(0, 2),
                    ),
                  ]
                : null,
          ),
          child: Text(
            label,
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: 11,
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

  Widget _buildActiveChart() {
    switch (_selectedTab) {
      case 0:
        return IncomeChart(data: widget.monthlyIncome);
      case 1:
        return ExpenseCategoryChart(data: widget.expenseByCategory);
      case 2:
      default:
        return BalanceHistoryChart(data: widget.cumulativeBalance);
    }
  }
}
