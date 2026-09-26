import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:http/http.dart' as http;
import 'package:url_launcher/url_launcher.dart';
import '../core/constants/app_colors.dart';
import '../core/utils/app_toast.dart';

class AppUpdateInfo {
  AppUpdateInfo({
    required this.remoteVersion,
    required this.currentVersion,
    required this.hasUpdate,
    required this.title,
    required this.changelog,
    required this.apkUrl,
    required this.htmlUrl,
  });

  final String remoteVersion;
  final String currentVersion;
  final bool hasUpdate;
  final String title;
  final String changelog;
  final String apkUrl;
  final String htmlUrl;
}

class AppUpdateService {
  AppUpdateService._();

  static final AppUpdateService instance = AppUpdateService._();

  /// Versi aplikasi saat ini
  static const String currentVersion = '1.0.5';

  /// Repositori rilis GitHub
  static const String repoOwner = 'arbdevai';
  static const String repoName = 'kas-go';

  Future<AppUpdateInfo?> checkUpdate() async {
    try {
      final url = Uri.parse(
          'https://api.github.com/repos/$repoOwner/$repoName/releases/latest');
      final response = await http.get(
        url,
        headers: {'Accept': 'application/vnd.github.v3+json'},
      ).timeout(const Duration(seconds: 8));

      if (response.statusCode == 200) {
        final data = json.decode(response.body) as Map<String, dynamic>;
        final tagName = (data['tag_name'] as String? ?? '').replaceAll('v', '');
        final title = data['name'] as String? ?? 'Pembaruan Tersedia';
        final changelog =
            data['body'] as String? ?? 'Pembaruan stabilitas dan performa.';
        final htmlUrl = data['html_url'] as String? ??
            'https://github.com/$repoOwner/$repoName/releases';

        // Cari direct APK asset
        String apkUrl = htmlUrl;
        final assets = data['assets'] as List<dynamic>? ?? [];
        for (final a in assets) {
          final assetMap = a as Map<String, dynamic>;
          final name = assetMap['name'] as String? ?? '';
          if (name.endsWith('.apk')) {
            apkUrl = assetMap['browser_download_url'] as String? ?? apkUrl;
            break;
          }
        }

        final hasUpdate = _isNewer(tagName, currentVersion);

        return AppUpdateInfo(
          remoteVersion: tagName,
          currentVersion: currentVersion,
          hasUpdate: hasUpdate,
          title: title,
          changelog: changelog,
          apkUrl: apkUrl,
          htmlUrl: htmlUrl,
        );
      }
    } catch (e) {
      if (kDebugMode) {
        print('Error checking app update: $e');
      }
    }
    return null;
  }

  bool _isNewer(String remote, String current) {
    try {
      final rParts = remote.split('.').map(int.parse).toList();
      final cParts = current.split('.').map(int.parse).toList();

      for (var i = 0; i < rParts.length && i < cParts.length; i++) {
        if (rParts[i] > cParts[i]) return true;
        if (rParts[i] < cParts[i]) return false;
      }
      return rParts.length > cParts.length;
    } catch (_) {
      return remote != current && remote.isNotEmpty;
    }
  }

  /// Tampilkan Dialog / BottomSheet Pembaruan lengkap dengan Changelog
  void showUpdateDialog(BuildContext context, AppUpdateInfo info) {
    showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      backgroundColor: Colors.white,
      builder: (ctx) {
        return SafeArea(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Row(
                      children: [
                        Container(
                          width: 36,
                          height: 36,
                          decoration: const BoxDecoration(
                            color: AppColors.surfaceLavender,
                            shape: BoxShape.circle,
                          ),
                          child: const Icon(
                            Icons.system_update_alt,
                            color: AppColors.primaryRoyal,
                            size: 20,
                          ),
                        ),
                        const SizedBox(width: 10),
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text(
                              'Pembaruan Versi Tersedia',
                              style: TextStyle(
                                fontSize: 15,
                                fontWeight: FontWeight.bold,
                                color: AppColors.textPrimaryLight,
                              ),
                            ),
                            Text(
                              'Versi v${info.remoteVersion} (Saat ini: v${info.currentVersion})',
                              style: const TextStyle(
                                fontSize: 11,
                                color: AppColors.textSecondaryLight,
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                    IconButton(
                      icon: const Icon(Icons.close, size: 20),
                      onPressed: () => Navigator.pop(ctx),
                    ),
                  ],
                ),
                const SizedBox(height: 16),

                // Judul Rilis
                Text(
                  info.title,
                  style: const TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.bold,
                    color: AppColors.primaryRoyal,
                  ),
                ),
                const SizedBox(height: 8),

                // Kontainer Changelog
                Container(
                  width: double.infinity,
                  constraints: const BoxConstraints(maxHeight: 200),
                  padding: const EdgeInsets.all(14),
                  decoration: BoxDecoration(
                    color: const Color(0xFFF8F7FC),
                    borderRadius: BorderRadius.circular(16),
                    border: Border.all(color: AppColors.borderSubtle),
                  ),
                  child: SingleChildScrollView(
                    child: Text(
                      info.changelog.trim().isNotEmpty
                          ? info.changelog.trim()
                          : 'Pembaruan aplikasi resmi.',
                      style: const TextStyle(
                        fontSize: 12,
                        color: AppColors.textPrimaryLight,
                        height: 1.5,
                      ),
                    ),
                  ),
                ),
                const SizedBox(height: 18),

                // Tombol Aksi Unduh APK Nyata
                SizedBox(
                  width: double.infinity,
                  height: 48,
                  child: ElevatedButton.icon(
                    onPressed: () {
                      Navigator.pop(ctx);
                      _launchDownload(context, info.apkUrl);
                    },
                    icon: const Icon(Icons.download, size: 18),
                    label: Text(
                      'Unduh APK (v${info.remoteVersion})',
                      style: const TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.primaryRoyal,
                      foregroundColor: Colors.white,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(14),
                      ),
                    ),
                  ),
                ),
                const SizedBox(height: 10),

                // Tombol Salin Tautan
                SizedBox(
                  width: double.infinity,
                  height: 44,
                  child: OutlinedButton.icon(
                    onPressed: () {
                      Clipboard.setData(ClipboardData(text: info.apkUrl));
                      AppToast.info(context, 'Tautan unduh APK disalin');
                    },
                    icon: const Icon(Icons.copy, size: 16),
                    label: const Text('Salin Tautan Unduh'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: AppColors.textSecondaryLight,
                      side: const BorderSide(color: AppColors.borderSubtle),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Future<void> _launchDownload(BuildContext context, String url) async {
    try {
      final uri = Uri.parse(url);
      final launched = await launchUrl(
        uri,
        mode: LaunchMode.externalApplication,
      );
      if (!launched) {
        await launchUrl(uri);
      }
      if (context.mounted) {
        AppToast.success(context, 'Mengunduh file APK di browser...');
      }
    } catch (_) {
      await Clipboard.setData(ClipboardData(text: url));
      if (context.mounted) {
        AppToast.info(context, 'Tautan unduh APK disalin ke clipboard');
      }
    }
  }
}
