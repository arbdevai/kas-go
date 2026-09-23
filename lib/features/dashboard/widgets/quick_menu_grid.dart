import 'package:flutter/material.dart';
import '../../../core/constants/app_colors.dart';

class QuickMenuItem {
  const QuickMenuItem({
    required this.icon,
    required this.line1,
    required this.line2,
    required this.onTap,
    this.badgeText,
  });

  final IconData icon;
  final String line1;
  final String line2;
  final VoidCallback onTap;
  final String? badgeText;
}

/// Menu Grid 4 kolom di bawah Hero Card dengan teks label 2 baris.
class QuickMenuGrid extends StatelessWidget {
  const QuickMenuGrid({
    super.key,
    required this.onAddIncome,
    required this.onAddExpense,
    required this.onPayIuran,
    required this.onRequestPickup,
  });

  final VoidCallback onAddIncome;
  final VoidCallback onAddExpense;
  final VoidCallback onPayIuran;
  final VoidCallback onRequestPickup;

  @override
  Widget build(BuildContext context) {
    final items = [
      QuickMenuItem(
        icon: Icons.add_circle_outline,
        line1: 'Catat',
        line2: 'Kas Masuk',
        onTap: onAddIncome,
        badgeText: 'Admin',
      ),
      QuickMenuItem(
        icon: Icons.remove_circle_outline,
        line1: 'Catat',
        line2: 'Pengeluaran',
        onTap: onAddExpense,
        badgeText: 'Admin',
      ),
      QuickMenuItem(
        icon: Icons.qr_code_2,
        line1: 'Bayar',
        line2: 'Iuran Warga',
        onTap: onPayIuran,
      ),
      QuickMenuItem(
        icon: Icons.two_wheeler_outlined,
        line1: 'Jemput',
        line2: 'Kas Keliling',
        onTap: onRequestPickup,
      ),
    ];

    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: items.map((item) {
        return Expanded(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 4),
            child: InkWell(
              onTap: item.onTap,
              borderRadius: BorderRadius.circular(16),
              child: Padding(
                padding: const EdgeInsets.symmetric(vertical: 8),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Stack(
                      clipBehavior: Clip.none,
                      children: [
                        Container(
                          width: 52,
                          height: 52,
                          decoration: BoxDecoration(
                            color: AppColors.surfaceLavender,
                            borderRadius: BorderRadius.circular(16),
                            border: Border.all(
                              color: AppColors.borderSubtle,
                              width: 1,
                            ),
                          ),
                          child: Icon(
                            item.icon,
                            color: AppColors.primaryRoyal,
                            size: 26,
                          ),
                        ),
                        if (item.badgeText != null)
                          Positioned(
                            top: -4,
                            right: -4,
                            child: Container(
                              padding: const EdgeInsets.symmetric(
                                horizontal: 5,
                                vertical: 1,
                              ),
                              decoration: BoxDecoration(
                                color: AppColors.primarySoft,
                                borderRadius: BorderRadius.circular(6),
                              ),
                              child: Text(
                                item.badgeText!,
                                style: const TextStyle(
                                  color: Colors.white,
                                  fontSize: 9,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ),
                          ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    Text(
                      item.line1,
                      style: Theme.of(context).textTheme.bodySmall?.copyWith(
                            fontWeight: FontWeight.w600,
                            color: AppColors.textPrimaryLight,
                            height: 1.1,
                          ),
                      textAlign: TextAlign.center,
                    ),
                    Text(
                      item.line2,
                      style: Theme.of(context).textTheme.bodySmall?.copyWith(
                            color: AppColors.textSecondaryLight,
                            fontSize: 11,
                            height: 1.1,
                          ),
                      textAlign: TextAlign.center,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ],
                ),
              ),
            ),
          ),
        );
      }).toList(),
    );
  }
}
