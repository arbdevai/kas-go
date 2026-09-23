import 'package:flutter/material.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/utils/formatters.dart';

/// Kartu Ringkasan Saldo Utama Kas Karang Taruna.
class HeroCard extends StatefulWidget {
  const HeroCard({
    super.key,
    required this.totalBalance,
    required this.totalIncome,
    required this.totalExpense,
  });

  final int totalBalance;
  final int totalIncome;
  final int totalExpense;

  @override
  State<HeroCard> createState() => _HeroCardState();
}

class _HeroCardState extends State<HeroCard> {
  bool _showBalance = true;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [AppColors.heroPurpleStart, AppColors.heroPurpleEnd],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(20),
        boxShadow: [
          BoxShadow(
            color: AppColors.primaryRoyal.withOpacity(0.18),
            blurRadius: 16,
            offset: const Offset(0, 6),
          ),
        ],
      ),
      padding: const EdgeInsets.all(20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header: Nama Organisasi
          Row(
            children: [
              Container(
                width: 38,
                height: 38,
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.14),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: const Icon(
                  Icons.account_balance_wallet_outlined,
                  color: Colors.white,
                  size: 20,
                ),
              ),
              const SizedBox(width: 12),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Karang Taruna RW 05',
                    style: Theme.of(context).textTheme.labelMedium?.copyWith(
                          color: AppColors.textOnPurpleMuted,
                          fontWeight: FontWeight.w600,
                        ),
                  ),
                  const Text(
                    'Kas Organisasi',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 15,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
            ],
          ),
          const SizedBox(height: 20),

          // Label Saldo & Toggle Tampilkan/Sembunyikan
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'Saldo Kas',
                style: TextStyle(
                  color: AppColors.textOnPurpleMuted,
                  fontSize: 12,
                  letterSpacing: 0.3,
                ),
              ),
              GestureDetector(
                onTap: () => setState(() => _showBalance = !_showBalance),
                child: Row(
                  children: [
                    Icon(
                      _showBalance
                          ? Icons.visibility_outlined
                          : Icons.visibility_off_outlined,
                      color: AppColors.textOnPurpleMuted,
                      size: 15,
                    ),
                    const SizedBox(width: 4),
                    Text(
                      _showBalance ? 'Sembunyikan' : 'Tampilkan',
                      style: const TextStyle(
                        color: AppColors.textOnPurpleMuted,
                        fontSize: 11,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 4),

          // Angka Saldo Utama
          Text(
            _showBalance ? formatRupiah(widget.totalBalance) : 'Rp ••••••••',
            style: const TextStyle(
              color: Colors.white,
              fontSize: 30,
              fontWeight: FontWeight.w800,
              letterSpacing: -0.5,
              fontFeatures: [FontFeature.tabularFigures()],
            ),
          ),
          const SizedBox(height: 18),

          // Ringkasan Pemasukan & Pengeluaran
          Row(
            children: [
              Expanded(
                child: Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 12,
                    vertical: 8,
                  ),
                  decoration: BoxDecoration(
                    color: Colors.white.withOpacity(0.12),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(4),
                        decoration: BoxDecoration(
                          color: AppColors.incomeGreen.withOpacity(0.35),
                          shape: BoxShape.circle,
                        ),
                        child: const Icon(
                          Icons.arrow_downward,
                          size: 13,
                          color: Color(0xFF6EE7B7),
                        ),
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text(
                              'Pemasukan',
                              style: TextStyle(
                                color: AppColors.textOnPurpleMuted,
                                fontSize: 10,
                              ),
                            ),
                            Text(
                              _showBalance
                                  ? formatRupiah(widget.totalIncome)
                                  : 'Rp ••••',
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 12,
                                fontWeight: FontWeight.bold,
                                fontFeatures: [FontFeature.tabularFigures()],
                              ),
                              overflow: TextOverflow.ellipsis,
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 12,
                    vertical: 8,
                  ),
                  decoration: BoxDecoration(
                    color: Colors.white.withOpacity(0.12),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(4),
                        decoration: BoxDecoration(
                          color: AppColors.expenseRed.withOpacity(0.35),
                          shape: BoxShape.circle,
                        ),
                        child: const Icon(
                          Icons.arrow_upward,
                          size: 13,
                          color: Color(0xFFFCA5A5),
                        ),
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text(
                              'Pengeluaran',
                              style: TextStyle(
                                color: AppColors.textOnPurpleMuted,
                                fontSize: 10,
                              ),
                            ),
                            Text(
                              _showBalance
                                  ? formatRupiah(widget.totalExpense)
                                  : 'Rp ••••',
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 12,
                                fontWeight: FontWeight.bold,
                                fontFeatures: [FontFeature.tabularFigures()],
                              ),
                              overflow: TextOverflow.ellipsis,
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
