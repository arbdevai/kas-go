import 'dart:io';
import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';

import 'tables/app_tables.dart';

part 'app_database.g.dart';

@DriftDatabase(tables: [
  LedgerEntries,
  Members,
  PaymentReports,
  PickupRequests,
  Outbox,
  SyncState,
  DashboardCache,
  ReceiptUploads,
])
class AppDatabase extends _$AppDatabase {
  AppDatabase([QueryExecutor? e]) : super(e ?? _openConnection());

  @override
  int get schemaVersion => 1;

  static LazyDatabase _openConnection() {
    return LazyDatabase(() async {
      final dbFolder = await getApplicationDocumentsDirectory();
      final file = File(p.join(dbFolder.path, 'kas_go.sqlite'));
      return NativeDatabase.createInBackground(file);
    });
  }

  // --- Helpers & Queries ---

  /// Hitung saldo terkonfirmasi: Pemasukan - Pengeluaran.
  Future<int> calculateConfirmedBalance() async {
    final entries = await (select(ledgerEntries)
          ..where((t) => t.isConfirmed.equals(true)))
        .get();

    var balance = 0;
    for (final e in entries) {
      if (e.entryType == LedgerType.income) {
        balance += e.amount;
      } else if (e.entryType == LedgerType.expense) {
        balance -= e.amount;
      }
    }
    return balance;
  }

  /// Masukkan operasi ke antrean outbox secara atomik dengan draft lokal.
  Future<void> queueLedgerEntry({
    required LedgerEntriesCompanion entry,
    required String operationId,
    required String payloadJson,
  }) {
    return transaction(() async {
      await into(ledgerEntries).insert(entry);
      await into(outbox).insert(
        OutboxCompanion.insert(
          operationId: operationId,
          kind: 'ledger_entry',
          payloadJson: payloadJson,
          status: OutboxStatus.queued,
          createdAt: DateTime.now(),
        ),
      );
    });
  }

  /// Ambil riwayat kas terkonfirmasi, urut tanggal terbaru.
  Stream<List<LedgerEntry>> watchConfirmedEntries({int limit = 50}) {
    return (select(ledgerEntries)
          ..where((t) => t.isConfirmed.equals(true))
          ..orderBy([
            (t) => OrderingTerm(
                  expression: t.occurredAt,
                  mode: OrderingMode.desc,
                ),
          ])
          ..limit(limit))
        .watch();
  }

  /// Ambil antrean outbox yang belum terkirim.
  Future<List<OutboxData>> getPendingOutbox() {
    return (select(outbox)
          ..where((t) => t.status.equals(OutboxStatus.queued.name))
          ..orderBy([(t) => OrderingTerm.asc(t.createdAt)]))
        .get();
  }
}
