# Kas Go — Aplikasi Kas Karang Taruna

Aplikasi Android modern untuk pengelolaan kas Karang Taruna: pencatatan pemasukan/pengeluaran oleh 3 admin, laporan transparan untuk warga, serta fitur jemput kas di rumah.

## Stack Teknologi
- **Flutter 3.x / Dart** — Android-first
- **Drift (SQLite)** — Database lokal offline-first
- **Firebase Auth + Firestore** (Spark) — Sinkronisasi cloud & autentikasi
- **Cloudinary** — Upload foto bukti pengeluaran
- **Riverpod** — State management
- **fl_chart** — 3 grafik transparansi keuangan

## Build APK (via GitHub Actions)
Seluruh kompilasi dilakukan di GitHub Actions CI, bukan lokal.
1. Push ke branch → CI otomatis jalan
2. Buka tab **Actions** di GitHub
3. Download APK dari bagian **Artifacts**

## Setup Firebase (setelah akun tersambung)
Lihat panduan: `docs/setup-firebase.md`

## Setup Cloudinary (setelah akun tersambung)
Lihat panduan: `docs/setup-cloudinary.md`
