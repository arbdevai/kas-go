# Arsitektur Teknis Kas Go

Dokumen ini menjelaskan arsitektur perangkat lunak, skema database, strategi sinkronisasi cloud, dan keamanan aplikasi mobile **Kas Go** (Karang Taruna Financial Transparency App).

---

## 1. Ikhtisar Arsitektur

Aplikasi Kas Go mengadopsi pola **Local-First Architecture** dengan Flutter & Dart:

```
┌────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                   │
│  (Widgets, Screens, Theme, Animations, 3 Chart Viz)   │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                    REPOSITORY LAYER                    │
│  - FinanceRepository (State, Kalkulasi, Saldo)         │
│  - UserProfileRepository (Profil, Peran, Google Auth)  │
└─────────────┬────────────────────────────┬─────────────┘
              │                            │
┌─────────────▼──────────────┐ ┌───────────▼─────────────┐
│      LOCAL DATABASE        │ │      CLOUD BACKEND      │
│  - SQLite via Drift        │ │  - Cloud Firestore      │
│  - Tabel: LedgerEntries,   │ │    (Region: Jakarta)    │
│    Members, Outbox, dll.   │ │  - Firebase Auth        │
│  - Offline Outbox Queue    │ │  - Spark Free Tier Opt. │
└────────────────────────────┘ └─────────────────────────┘
```

- **Offline-First**: Pengguna (khususnya 3 admin saat keliling lapangan) dapat mencatat transaksi tanpa koneksi internet. Data langsung tersimpan di SQLite lokal dan masuk antrean sinkronisasi (*Outbox*).
- **Reactive UI**: Tampilan dashboard, saldo kas, dan riwayat transaksi mendengarkan perubahan state secara real-time via `ChangeNotifier` & `AnimatedBuilder`.
- **Zero AI Slop**: Tidak menggunakan template generik. Menggunakan Hero Card terpadu, Floating Navigation Dock 2026, dan grouped surface tanpa *card fatigue*.

---

## 2. Struktur Database Lokal (Drift SQLite)

Database lokal diimplementasikan pada `lib/data/local/app_database.dart` dengan tabel utama:

| Tabel | Deskripsi | Field Kunci |
|---|---|---|
| `ledger_entries` | Buku besar kas masuk & pengeluaran terkonfirmasi | `id`, `entryType`, `amount`, `summary`, `recordedByName`, `recordedByUid`, `occurredAt`, `category`, `period`, `isConfirmed` |
| `members` | Data warga & anggota Karang Taruna | `id`, `name`, `phone`, `isActive` |
| `payment_reports` | Laporan konfirmasi transfer/QRIS warga | `id`, `memberId`, `amount`, `period`, `status`, `proofAssetId` |
| `pickup_requests` | Pengajuan penjemputan kas door-to-door | `id`, `requesterName`, `address`, `phone`, `estimatedAmount`, `timeSlot`, `status` |
| `outbox` | Antrean transaksi offline untuk dikirim ke cloud | `operationId`, `kind`, `payloadJson`, `status`, `attemptCount`, `createdAt` |
| `sync_state` | Cursor penanda revisi sinkronisasi terakhir | `scope`, `lastRevision`, `syncedAt` |

---

## 3. Strategi Optimasi Firebase (Spark Free Tier)

Firebase Spark memiliki kuota gratis harian (50.000 reads, 20.000 writes). Kas Go dioptimalkan agar tidak melebihi kuota tersebut:

1. **Dokumen Ringkasan Tunggal (`organizations/{orgId}/aggregates/dashboard`)**:
   - Warga yang hanya membuka dashboard untuk memantau saldo **hanya membaca 1 dokumen** agregat, bukan ratusan baris transaksi riwayat.
2. **Sinkronisasi Tambahan (*Delta Sync*)**:
   - Aplikasi hanya meminta transaksi dengan revisi lebih baru daripada revisi lokal terakhir (`revision > last_sync_revision`).
3. **Immutability Buku Kas**:
   - Transaksi yang sudah terkonfirmasi di server bersifat *immutable* (tidak dapat diubah/dihapus diam-diam). Koreksi kesalahan dilakukan melalui transaksi pembalik (*reversal transaction*) dengan referensi audit lengkap.

---

## 4. Keamanan & Peran Pengguna (Role-Based Access)

Aplikasi membagi hak akses ke dalam 2 tingkatan utama:

### 3 Slot Admin (Audit Trail Penuh):
- **Admin 1**: Bendahara (Pencatatan kas masuk & pengeluaran penuh).
- **Admin 2**: Sekretaris (Dokumentasi transaksi & verifikasi laporan transfer).
- **Admin 3**: Koordinator Lapangan (Pencatatan kas keliling & penjemputan tunai door-to-door).
- Setiap transaksi mencatat `recorded_by_name` dan `recorded_by_uid`.

### Warga (Transparansi Publik):
- Memantau saldo kas dan ringkasan keuangan.
- Melihat grafik tren kas masuk, pengeluaran per kategori, dan saldo kumulatif.
- Mengakses informasi pembayaran QRIS & rekening bank resmi.
- Mengajukan permintaan jemput kas ke rumah (*door-to-door*).
- Menghubungkan akun Google & mengedit profil identitas diri.

---

## 5. Sistem Tanda Tangan Konsisten (Keystore v1+v2+v3)

Untuk menjamin bahwa setiap pembaruan aplikasi dapat langsung dipasang tanpa mencopot (*uninstall*) versi sebelumnya:
- File kunci PKCS#12 RSA 2048 (`android/app/keystore/kasgo_release.p12`) disimpan secara permanen di repositori.
- GitHub Actions CI otomatis mengikat kunci ini pada setiap proses kompilasi rilis.
- Tanda tangan APK memenuhi skema Android v1 (JAR signing), v2 (APK signature scheme), dan v3 (APK key rotation scheme).
