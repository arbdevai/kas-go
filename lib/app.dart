import 'package:flutter/material.dart';
import 'core/config/app_config.dart';
import 'core/constants/app_colors.dart';
import 'core/theme/app_theme.dart';
import 'features/admin/add_expense_screen.dart';
import 'features/admin/add_income_screen.dart';
import 'features/auth/login_screen.dart';
import 'features/dashboard/dashboard_screen.dart';
import 'features/ledger/ledger_screen.dart';
import 'features/payments/payment_hub_screen.dart';

/// Root widget aplikasi Kas Go dengan bottom navigation.
class KasGoApp extends StatefulWidget {
  const KasGoApp({super.key, required this.config});

  final AppConfig config;

  @override
  State<KasGoApp> createState() => _KasGoAppState();
}

class _KasGoAppState extends State<KasGoApp> {
  int _currentIndex = 0;

  late final List<Widget> _screens = [
    DashboardScreen(config: widget.config),
    const LedgerScreen(),
    const PaymentHubScreen(),
    const LoginScreen(),
  ];

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Kas Go',
      theme: AppTheme.light(),
      darkTheme: AppTheme.dark(),
      themeMode: ThemeMode.system,
      home: Scaffold(
        body: IndexedStack(
          index: _currentIndex,
          children: _screens,
        ),
        floatingActionButton: _currentIndex == 0 || _currentIndex == 1
            ? FloatingActionButton(
                backgroundColor: AppColors.primaryLight,
                foregroundColor: Colors.white,
                onPressed: () => _showAddActionModal(context),
                tooltip: 'Catat Kas / Pengeluaran',
                child: const Icon(Icons.add),
              )
            : null,
        bottomNavigationBar: NavigationBar(
          selectedIndex: _currentIndex,
          onDestinationSelected: (idx) {
            setState(() {
              _currentIndex = idx;
            });
          },
          destinations: const [
            NavigationDestination(
              icon: Icon(Icons.dashboard_outlined),
              selectedIcon: Icon(Icons.dashboard),
              label: 'Beranda',
            ),
            NavigationDestination(
              icon: Icon(Icons.receipt_long_outlined),
              selectedIcon: Icon(Icons.receipt_long),
              label: 'Transparansi',
            ),
            NavigationDestination(
              icon: Icon(Icons.payments_outlined),
              selectedIcon: Icon(Icons.payments),
              label: 'Bayar Kas',
            ),
            NavigationDestination(
              icon: Icon(Icons.person_outline),
              selectedIcon: Icon(Icons.person),
              label: 'Akun',
            ),
          ],
        ),
      ),
    );
  }

  void _showAddActionModal(BuildContext context) {
    showModalBottomSheet<void>(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (ctx) {
        return SafeArea(
          child: Padding(
            padding: const EdgeInsets.symmetric(vertical: 20, horizontal: 16),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Catat Transaksi Baru (Admin)',
                  style: Theme.of(ctx).textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                ),
                const SizedBox(height: 16),
                ListTile(
                  leading: const CircleAvatar(
                    backgroundColor: Color(0xFFDCFCE7),
                    child: Icon(Icons.arrow_downward, color: Color(0xFF16A34A)),
                  ),
                  title: const Text('Catat Kas Masuk'),
                  subtitle: const Text('Iuran warga, donasi, kas keliling'),
                  onTap: () {
                    Navigator.pop(ctx);
                    Navigator.push(
                      context,
                      MaterialPageRoute<void>(
                        builder: (_) => const AddIncomeScreen(),
                      ),
                    );
                  },
                ),
                ListTile(
                  leading: const CircleAvatar(
                    backgroundColor: Color(0xFFFFEDD5),
                    child: Icon(Icons.arrow_upward, color: Color(0xFFEA580C)),
                  ),
                  title: const Text('Catat Pengeluaran'),
                  subtitle: const Text('Beli perlengkapan, operasional, konsumsi'),
                  onTap: () {
                    Navigator.pop(ctx);
                    Navigator.push(
                      context,
                      MaterialPageRoute<void>(
                        builder: (_) => const AddExpenseScreen(),
                      ),
                    );
                  },
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}
