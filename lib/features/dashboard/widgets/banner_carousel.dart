import 'package:flutter/material.dart';
import '../../../core/constants/app_colors.dart';

class BannerItem {
  const BannerItem({
    required this.imagePath,
    required this.tag,
    required this.title,
    required this.subtitle,
  });

  final String imagePath;
  final String tag;
  final String title;
  final String subtitle;
}

/// Banner carousel visual menggunakan custom banner PNG yang di-generate.
class BannerCarousel extends StatefulWidget {
  const BannerCarousel({super.key});

  @override
  State<BannerCarousel> createState() => _BannerCarouselState();
}

class _BannerCarouselState extends State<BannerCarousel> {
  final PageController _pageController = PageController(viewportFraction: 0.92);
  int _currentPage = 0;

  static const _banners = [
    BannerItem(
      imagePath: 'assets/images/banners/banner_transparency.png',
      tag: 'TRANSPARANSI',
      title: 'Laporan Keuangan Terbuka',
      subtitle: 'Pencatatan kas masuk dan keluar dilengkapi identitas petugas',
    ),
    BannerItem(
      imagePath: 'assets/images/banners/banner_keliling.png',
      tag: 'KAS KELILING',
      title: 'Layanan Jemput Kas',
      subtitle: 'Pengambilan setoran langsung ke alamat sesuai jadwal',
    ),
    BannerItem(
      imagePath: 'assets/images/banners/banner_qris_bca.png',
      tag: 'PEMBAYARAN',
      title: 'Pembayaran Non-Tunai',
      subtitle: 'Penyetoran melalui QRIS resmi atau transfer rekening bank',
    ),
  ];

  @override
  void dispose() {
    _pageController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        SizedBox(
          height: 120,
          child: PageView.builder(
            controller: _pageController,
            itemCount: _banners.length,
            onPageChanged: (idx) => setState(() => _currentPage = idx),
            itemBuilder: (ctx, idx) {
              final b = _banners[idx];
              return Padding(
                padding: const EdgeInsets.symmetric(horizontal: 4),
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(16),
                  child: Stack(
                    children: [
                      // Background Image PNG
                      Positioned.fill(
                        child: Image.asset(
                          b.imagePath,
                          fit: BoxFit.cover,
                          errorBuilder: (_, __, ___) => Container(
                            decoration: const BoxDecoration(
                              gradient: LinearGradient(
                                colors: [
                                  AppColors.primaryRoyal,
                                  AppColors.primarySoft,
                                ],
                              ),
                            ),
                          ),
                        ),
                      ),
                      // Soft gradient overlay for text readability
                      Positioned.fill(
                        child: Container(
                          decoration: BoxDecoration(
                            gradient: LinearGradient(
                              colors: [
                                Colors.black.withOpacity(0.65),
                                Colors.black.withOpacity(0.15),
                              ],
                              begin: Alignment.centerLeft,
                              end: Alignment.centerRight,
                            ),
                          ),
                        ),
                      ),
                      // Banner Content
                      Padding(
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Container(
                              padding: const EdgeInsets.symmetric(
                                horizontal: 7,
                                vertical: 2,
                              ),
                              decoration: BoxDecoration(
                                color: AppColors.accentGold,
                                borderRadius: BorderRadius.circular(4),
                              ),
                              child: Text(
                                b.tag,
                                style: const TextStyle(
                                  color: Color(0xFF451A03),
                                  fontSize: 9,
                                  fontWeight: FontWeight.w800,
                                  letterSpacing: 0.5,
                                ),
                              ),
                            ),
                            const SizedBox(height: 6),
                            Text(
                              b.title,
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 14,
                                fontWeight: FontWeight.bold,
                              ),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                            const SizedBox(height: 2),
                            Text(
                              b.subtitle,
                              style: TextStyle(
                                color: Colors.white.withOpacity(0.85),
                                fontSize: 11,
                              ),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              );
            },
          ),
        ),
        const SizedBox(height: 10),
        // Dots indicator
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: List.generate(_banners.length, (idx) {
            final isActive = idx == _currentPage;
            return AnimatedContainer(
              duration: const Duration(milliseconds: 250),
              margin: const EdgeInsets.symmetric(horizontal: 3),
              width: isActive ? 18 : 6,
              height: 4,
              decoration: BoxDecoration(
                color: isActive
                    ? AppColors.primaryRoyal
                    : AppColors.borderSubtle,
                borderRadius: BorderRadius.circular(2),
              ),
            );
          }),
        ),
      ],
    );
  }
}
