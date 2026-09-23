import 'package:flutter/material.dart';
import '../../core/config/app_config.dart';
import '../../core/constants/app_colors.dart';
import '../../data/demo/demo_data.dart';
import '../admin/add_expense_screen.dart';
import '../admin/add_income_screen.dart';
import '../ledger/ledger_screen.dart';
import '../payments/payment_hub_screen.dart';
import 'widgets/banner_carousel.dart';
import 'widgets/charts_section.dart';
import 'widgets/grouped_transaction_list.dart';
import 'widgets/hero_card.dart';
import 'widgets/quick_menu_grid.dart';

/// Layar utama Dashboard Transparansi Kas (Modern 2026, Tanpa TopAppBar kaku).
class DashboardScreen extends StatelessWidget {
  const DashboardScreen({
    super.key,
    required this.config,
    this.onNavigateTab,
  });

  final AppConfig config;
  final ValueChanged<int>? onNavigateTab;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundLight,
      body: SafeArea(
        bottom: false,
        child: SingleChildScrollView(
          padding: const EdgeInsets.fromLTRB(16, 12, 16, 32),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // 1. HERO CARD (Pengganti TopAppBar web-like)
              HeroCard(
                totalBalance: DemoData.totalBalance,
                totalIncome: DemoData.totalIncome,
                totalExpense: DemoData.totalExpense,
                isDemo: config.isDemo,
              ),
              const SizedBox(height: 18),

              // 2. MENU GRID (4 Kolom, Teks 2 Baris)
              QuickMenuGrid(
                onAddIncome: () => Navigator.push(
                  context,
                  MaterialPageRoute<void>(
                    builder: (_) => const AddIncomeScreen(),
                  ),
                ),
                onAddExpense: () => Navigator.push(
                  context,
                  MaterialPageRoute<void>(
                    builder: (_) => const AddExpenseScreen(),
                  ),
                ),
                onPayIuran: () {
                  if (onNavigateTab != null) {
                    onNavigateTab!(2);
                  } else {
                    Navigator.push(
                      context,
                      MaterialPageRoute<void>(
                        builder: (_) => const PaymentHubScreen(),
                      ),
                    );
                  }
                },
                onRequestPickup: () {
                  if (onNavigateTab != null) {
                    onNavigateTab!(2);
                  } else {
                    Navigator.push(
                      context,
                      MaterialPageRoute<void>(
                        builder: (_) => const PaymentHubScreen(),
                      ),
                    );
                  }
                },
              ),
              const SizedBox(height: 20),

              // 3. BANNER CAROUSEL VISUAL
              const BannerCarousel(),
              const SizedBox(height: 20),

              // 4. BAGIAN GRAFIK TERPADU (Segmented Tabs)
              ChartsSection(
                monthlyIncome: DemoData.monthlyIncome,
                expenseByCategory: DemoData.expenseByCategory,
                cumulativeBalance: DemoData.cumulativeBalance,
              ),
              const SizedBox(height: 20),

              // 5. RIWAYAT KAS TERPADU (Single Grouped Surface — No Card Fatigue!)
              GroupedTransactionList(
                entries: DemoData.entries.take(5).toList(),
                onViewAll: () {
                  if (onNavigateTab != null) {
                    onNavigateTab!(1);
                  } else {
                    Navigator.push(
                      context,
                      MaterialPageRoute<void>(
                        builder: (_) => const LedgerScreen(),
                      ),
                    );
                  }
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}
