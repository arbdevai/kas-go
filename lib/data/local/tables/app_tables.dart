import 'package:drift/drift.dart';

/// Jenis transaksi kas.
enum LedgerType { income, expense }

/// Metode pembayaran setoran warga.
enum PaymentMethod { cash, qris, bankTransfer, pickup }

/// Status laporan pembayaran warga (verifikasi manual admin).
enum PaymentReportStatus { pending, verified, rejected }

/// Status permintaan jemput kas.
enum PickupStatus { pending, scheduled, completed, cancelled }

/// Status antrean outbox sinkronisasi.
enum OutboxStatus { queued, sending, failed, done }

/// Tabel utama: entri ledger terkonfirmasi maupun draft lokal.
///
/// Immutability untuk data terkonfirmasi ditegakkan di level aplikasi
/// dan aturan server; koreksi dilakukan via transaksi pembalik.
class LedgerEntries extends Table {
  TextColumn get id => text()();
  TextColumn get entryType => text().map(const LedgerTypeConverter())();
  IntColumn get amount => integer()(); // Rupiah, selalu positif
  TextColumn get period => text().nullable()(); // "2026-09" untuk iuran
  TextColumn get category => text().nullable()();
  DateTimeColumn get occurredAt => dateTime()();
  IntColumn get serverRevision => integer().nullable()();
  TextColumn get summary => text()();
  TextColumn get recordedByUid => text()();
  TextColumn get recordedByName => text()();
  TextColumn get reversalOf => text().nullable()();
  TextColumn get sourceReportId => text().nullable()();
  TextColumn get pickupId => text().nullable()();
  TextColumn get paymentMethod =>
      text().map(const PaymentMethodConverter()).nullable()();
  BoolColumn get isConfirmed => boolean().withDefault(const Constant(false))();

  @override
  Set<Column> get primaryKey => {id};
}

class LedgerTypeConverter extends TypeConverter<LedgerType, String> {
  const LedgerTypeConverter();

  @override
  LedgerType fromSql(String fromDb) => LedgerType.values.byName(fromDb);

  @override
  String toSql(LedgerType value) => value.name;
}

class PaymentMethodConverter extends TypeConverter<PaymentMethod, String> {
  const PaymentMethodConverter();

  @override
  PaymentMethod fromSql(String fromDb) => PaymentMethod.values.byName(fromDb);

  @override
  String toSql(PaymentMethod value) => value.name;
}

class Members extends Table {
  TextColumn get id => text()();
  TextColumn get name => text()();
  TextColumn get phone => text().nullable()();
  BoolColumn get isActive => boolean().withDefault(const Constant(true))();

  @override
  Set<Column> get primaryKey => {id};
}

class PaymentReports extends Table {
  TextColumn get id => text()();
  TextColumn get memberId => text()();
  IntColumn get amount => integer()();
  TextColumn get period => text()();
  TextColumn get status =>
      text().map(const PaymentReportStatusConverter())();
  TextColumn get proofAssetId => text().nullable()();
  TextColumn get note => text().nullable()();
  DateTimeColumn get createdAt => dateTime()();

  @override
  Set<Column> get primaryKey => {id};
}

class PaymentReportStatusConverter
    extends TypeConverter<PaymentReportStatus, String> {
  const PaymentReportStatusConverter();

  @override
  PaymentReportStatus fromSql(String fromDb) =>
      PaymentReportStatus.values.byName(fromDb);

  @override
  String toSql(PaymentReportStatus value) => value.name;
}

class PickupRequests extends Table {
  TextColumn get id => text()();
  TextColumn get requesterName => text()();
  TextColumn get address => text()();
  TextColumn get phone => text()();
  IntColumn get estimatedAmount => integer().nullable()();
  DateTimeColumn get preferredAt => dateTime().nullable()();
  TextColumn get note => text().nullable()();
  TextColumn get status => text().map(const PickupStatusConverter())();
  IntColumn get version => integer().withDefault(const Constant(1))();

  @override
  Set<Column> get primaryKey => {id};
}

class PickupStatusConverter extends TypeConverter<PickupStatus, String> {
  const PickupStatusConverter();

  @override
  PickupStatus fromSql(String fromDb) => PickupStatus.values.byName(fromDb);

  @override
  String toSql(PickupStatus value) => value.name;
}

/// Antrean operasi offline yang stabil untuk sinkronisasi.
class Outbox extends Table {
  TextColumn get operationId => text()();
  TextColumn get kind => text()(); // ledger_entry, pickup_claim, dll.
  TextColumn get payloadJson => text()();
  TextColumn get status => text().map(const OutboxStatusConverter())();
  IntColumn get attemptCount => integer().withDefault(const Constant(0))();
  TextColumn get lastError => text().nullable()();
  DateTimeColumn get createdAt => dateTime()();

  @override
  Set<Column> get primaryKey => {operationId};
}

class OutboxStatusConverter extends TypeConverter<OutboxStatus, String> {
  const OutboxStatusConverter();

  @override
  OutboxStatus fromSql(String fromDb) => OutboxStatus.values.byName(fromDb);

  @override
  String toSql(OutboxStatus value) => value.name;
}

class SyncState extends Table {
  TextColumn get scope => text()();
  IntColumn get lastRevision => integer().withDefault(const Constant(0))();
  DateTimeColumn get syncedAt => dateTime().nullable()();

  @override
  Set<Column> get primaryKey => {scope};
}

class DashboardCache extends Table {
  TextColumn get scope => text()();
  TextColumn get payloadJson => text()();
  DateTimeColumn get cachedAt => dateTime()();

  @override
  Set<Column> get primaryKey => {scope};
}

/// Antrean upload foto bukti (path disalin ke direktori aplikasi).
class ReceiptUploads extends Table {
  TextColumn get id => text()();
  TextColumn get localPath => text()();
  TextColumn get remoteAssetId => text().nullable()();
  TextColumn get status => text().map(const OutboxStatusConverter())();
  DateTimeColumn get createdAt => dateTime()();

  @override
  Set<Column> get primaryKey => {id};
}
