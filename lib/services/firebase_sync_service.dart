import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import '../data/repositories/finance_repository.dart';
import '../data/repositories/organization_repository.dart';

/// Layanan sinkronisasi Firebase Firestore hemat kuota (Spark Free Tier).
///
/// Membaca ringkasan aggregate 1 dokumen untuk transparansi warga dan
/// mem-publish transaksi kas baru ke cloud saat terhubung internet.
class FirebaseSyncService {
  FirebaseSyncService._();

  static final FirebaseSyncService instance = FirebaseSyncService._();

  static const String projectId = 'kas-go-app-kt26-e6d6f';
  String get orgId => OrganizationRepository.instance.orgId;
  static const String firestoreBaseUrl =
      'https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents';

  bool _isSyncing = false;
  DateTime? _lastSyncTime;

  bool get isSyncing => _isSyncing;
  DateTime? get lastSyncTime => _lastSyncTime;

  /// Sinkronisasi ringan: baca 1 dokumen ringkasan kas dari Firestore.
  Future<bool> syncDashboardSummary() async {
    if (_isSyncing) {
      return false;
    }
    _isSyncing = true;

    try {
      final url = Uri.parse('$firestoreBaseUrl/organizations/$orgId/aggregates/dashboard');
      final response = await http.get(url).timeout(const Duration(seconds: 8));

      if (response.statusCode == 200) {
        final data = json.decode(response.body) as Map<String, dynamic>;
        final fields = data['fields'] as Map<String, dynamic>?;

        if (fields != null) {
          final balanceStr = fields['total_balance']?['integerValue'] as String?;
          if (balanceStr != null) {
            _lastSyncTime = DateTime.now();
            if (kDebugMode) {
              print('Firebase Cloud Sync OK: balance $balanceStr');
            }
          }
        }
        _isSyncing = false;
        return true;
      }
    } catch (e) {
      if (kDebugMode) {
        print('Offline / Firebase sync skipped: $e');
      }
    } finally {
      _isSyncing = false;
    }
    return false;
  }

  /// Kirim transaksi kas baru ke Cloud Firestore.
  Future<bool> pushTransactionToCloud(TransactionItem item) async {
    try {
      final url = Uri.parse('$firestoreBaseUrl/organizations/$orgId/ledger/${item.id}');
      final body = json.encode({
        'fields': {
          'type': {'stringValue': item.isIncome ? 'income' : 'expense'},
          'amount': {'integerValue': item.amount.toString()},
          'summary': {'stringValue': item.summary},
          'recorded_by': {'stringValue': item.recordedByName},
          'occurred_at': {'timestampValue': item.occurredAt.toUtc().toIso8601String()},
          'period': {'stringValue': item.period ?? ''},
          'category': {'stringValue': item.category ?? ''},
        }
      });

      final response = await http
          .patch(url, body: body, headers: {'Content-Type': 'application/json'})
          .timeout(const Duration(seconds: 8));

      return response.statusCode == 200;
    } catch (_) {
      // Jika offline, data sudah aman di memori lokal SQLite Drift
      return false;
    }
  }
}
