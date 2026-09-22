import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import '../../core/config/app_config.dart';
import '../../core/constants/app_colors.dart';
import '../../core/utils/formatters.dart';
import '../../data/demo/demo_data.dart';
import 'widgets/charts.dart';
import 'widgets/stat_tile.dart';

/// Layar utama Dashboard Transparansi Kas.
class DashboardScreen extends StatelessWidget {
  const DashboardScreen({super.key, required this.config});

  final AppConfig config;

  @override
  Widget build(BuildContext context) {
    final isDemo = config.isDemo;

    return Scaffold(
      appBar: AppBar(
        title: Text(config.appName),
        backgroundColor: AppColors.primaryLight,
        foregroundColor: Colors.white,
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 12),
            child: Chip(
              label: Text(isDemo ? 'Demo' : 'Live',
                  style: const TextStyle(fontSize: 11)),
              backgroundColor: Colors.white24,
              side: BorderSide.none,
            ),
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (isDemo)
              Card(
                color: const Color(0xFFFEF3C7),
                child: Padding(
                  padding: const EdgeInsets.all(12),
                  child: Row(
                    children: [
                      const Icon(Icons.info_outline,
                          size: 20, color: Color(0xFFB45309)),
                      const SizedBox(width: 10),
                      Expanded(
                        child: Text(
                          'Mode Demo: data contoh untuk uji coba. Bukan data nyata.',
                          style:
                              Theme.of(context).textTheme.bodySmall?.copyWith(
                                    color: const Color(0xFF92400E),
                                  ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            if (isDemo) const SizedBox(height: 12),

            Text(
              'Ringkasan Kas',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
            ),
            const SizedBox(height: 12),

            Row(
              children: [
                Expanded(
                  child: StatTile(
                    title: 'Pemasukan',
                    amount: DemoData.totalIncome,
                    color: Theme.of(context).brightness == Brightness.dark
                        ? AppColors.incomeDark
                        : AppColors.incomeLight,
                    icon: Icons.arrow_downward,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: StatTile(
                    title: 'Pengeluaran',
                    amount: DemoData.totalExpense,
                    color: Theme.of(context).brightness == Brightness.dark
                        ? AppColors.expenseDark
                        : AppColors.expenseLight,
                    icon: Icons.arrow_upward,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),

            SizedBox(
              width: double.infinity,
              child: StatTile(
                title: 'Total Saldo',
                amount: DemoData.totalBalance,
                color: Theme.of(context).brightness == Brightness.dark
                    ? AppColors.balanceDark
                    : AppColors.balanceLight,
                icon: Icons.account_balance_wallet,
              ),
            ),
            const SizedBox(height: 20),

            Text(
              'Grafik Keuangan',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
            ),
            const SizedBox(height: 12),

            IncomeChart(data: DemoData.monthlyIncome),
            const SizedBox(height: 12),

            ExpenseCategoryChart(data: DemoData.expenseByCategory),
            const SizedBox(height: 12),

            BalanceHistoryChart(data: DemoData.cumulativeBalance),
            const SizedBox(height: 20),

            Text(
              'Riwayat Terakhir',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
            ),
            const SizedBox(height: 12),

            ...DemoData.entries.take(5).map(
              (e) {
                final isIncome = e['entryType'].toString().contains('income');
                return Card(
                  child: ListTile(
                    leading: CircleAvatar(
                      backgroundColor: isIncome
                          ? AppColors.incomeLight.withOpacity(0.15)
                          : AppColors.expenseLight.withOpacity(0.15),
                      child: Icon(
                        isIncome
                            ? Icons.arrow_downward
                            : Icons.arrow_upward,
                        color: isIncome
                            ? AppColors.incomeLight
                            : AppColors.expenseLight,
                      ),
                    ),
                    title: Text(e['summary'] as String),
                    subtitle: Text(
                      'Dicatat oleh: ${e['recordedByName']}',
                      style: const TextStyle(fontSize: 11),
                    ),
                    trailing: Text(
                      formatRupiah(e['amount'] as int),
                      style: TextStyle(
                        fontWeight: FontWeight.bold,
                        color: isIncome
                            ? AppColors.incomeLight
                            : AppColors.expenseLight,
                      ),
                    ),
                  ),
                );
              },
            ),
          ],
        ),
      ),
    );
  }
}
