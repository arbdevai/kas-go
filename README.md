# Kas Go — Aplikasi Kas Karang Taruna (Android 2026)

<p align="center">
  <img src="assets/images/app_icon.png" width="128" height="128" alt="Kas Go Icon" style="border-radius: 28px;" />
</p>

<p align="center">
  <b>Aplikasi Android modern, transparan, dan offline-first untuk pengelolaan keuangan Karang Taruna.</b><br>
  Pencatatan kas keliling oleh 3 Admin dengan audit trail lengkap, buku kas terbuka untuk seluruh warga, pembayaran via QRIS/BCA, serta fitur penjemputan uang kas door-to-door.
</p>

---

## 📱 Unduh Aplikasi (APK Android)

APK produksi resmi dikompilasi secara otomatis melalui **GitHub Actions CI** dengan tanda tangan konsisten (*v1+v2+v3 keystore*).

👉 **[Unduh Kas Go APK Terbaru di GitHub Releases](https://github.com/arbdevai/kas-go/releases)**
- **Versi Terkini**: `v1.0.2` (Build +3)
- **Tautan Langsung**: [kas-go-app.apk](https://github.com/arbdevai/kas-go/releases/download/v1.0.1/kas-go-app.apk)
- **Kompatibilitas**: Android 7.0 (Nougat / API 24) hingga Android 15+

> **Catatan Instalasi**: Karena menggunakan sertifikat keystore permanen terstandarisasi, pembaruan APK selanjutnya di smartphone Android Anda akan **langsung ter-update** tanpa perlu mencopot (*uninstall*) versi lama.

---

## 🌟 Fitur Utama

### 1. Transparansi Penuh untuk Warga
- **Buku Kas Terbuka**: Warga dapat melihat setiap mutasi kas masuk dan keluar lengkap dengan nominal, tanggal, dan nama admin yang mencatatnya.
- **Kartu Saldo Utama (Hero Card)**: Tampilan saldo real-time dengan opsi sembunyikan nominal saat di tempat umum.
- **3 Macam Visualisasi Grafik (Data Viz 2026)**:
  - **Arus Kas Masuk**: Grafik batang tren iuran & donasi bulanan.
  - **Distribusi Pengeluaran**: Pengelompokan biaya per kategori (Operasional, Sosial, Perlengkapan, Kegiatan).
  - **Pertumbuhan Saldo Bersih**: Grafik garis akumulatif pertumbuhan kas Karang Taruna.

### 2. Multi-Admin Pencatat (3 Slot dengan Audit Trail)
- **3 Peran Admin Khusus**:
  - **Admin 1**: Bendahara (Pencatatan kas masuk & pengeluaran penuh).
  - **Admin 2**: Sekretaris (Dokumentasi transaksi & verifikasi laporan).
  - **Admin 3**: Koordinator Lapangan (Pencatatan keliling door-to-door).
- Setiap transaksi mencatat identitas akun pencatat secara transparan untuk mencegah kecurigaan atau kesalahan pembukuan.

### 3. Kemudahan Pembayaran Iuran Warga
- **Scan QRIS Resmi**: Kode QRIS Karang Taruna terintegrasi (kompatibel BCA, Mandiri, BRI, GoPay, OVO, Dana).
- **Transfer Bank BCA**: Informasi rekening organisasi dengan fitur 1 sentuhan untuk menyalin nomor rekening.
- **Layanan Jemput Kas Keliling (*Door-to-Door*)**: Formulir pengajuan penjemputan kas tunai ke rumah bagi warga lansia atau yang berhalangan hadir saat rapat bulanan.

### 4. Profil Akun & Integrasi Google
- Masuk / Daftar cepat menggunakan akun Google.
- Formulir edit profil: Ubah nama lengkap, nomor WhatsApp, dan alamat rumah/RT/RW kapan saja.

---

## 🎨 Konsep UI/UX 2026 (Zero AI Slop)

Aplikasi didesain khusus agar terasa seperti aplikasi mobile perbankan profesional, bukan template web yang dipaksakan ke layar HP:
- **Tema Putih Porselen + Ungu Royal Premium**: Latar belakang bersih sejuk di mata (`#FAFAFC`), aksen dominan *Royal Amethyst* (`#5B21B6`), *Soft Iris* (`#7C3AED`), dan *Lavender Wash* (`#F5F3FF`).
- **Tanpa TopAppBar Web Jadul**: Digantikan oleh *Hero Card* bertekstur satin ungu royal yang menyatu mulus dari status bar perangkat (*edge-to-edge*).
- **Floating Dock Navigation Bar 2026**: Dok navigasi bawah melayang modern dengan sudut melengkung 24px dan animasi mikro saat berpindah tab.
- **Grouped Transaction Surface (Bebas Card Fatigue)**: Seluruh mutasi transaksi dirangkum dalam satu kontainer permukaan putih utuh dengan garis pembatas tipis 0.5px.
- **Startup Splash Screen Mulus**: Animasi logo monogram emas dengan waktu buka cepat (< 1.2 detik).

---

## 🛠️ Stack Teknologi

| Komponen | Teknologi | Alasan Pemilihan |
|---|---|---|
| **Framework** | Flutter 3.22 / Dart 3.4 | Performa native AOT 60/120fps di Android, single codebase |
| **Local Database** | Drift (SQLite) | Offline-first, type-safe, performa kueri tinggi, zero latency |
| **Cloud Backend** | Google Cloud Firestore (Jakarta) | Sinkronisasi cloud hemat kuota (Spark free tier optimization) |
| **Autentikasi** | Firebase Auth & Google Sign-In | Autentikasi aman tanpa menyimpan password di database lokal |
| **Visualisasi** | fl_chart + Dataviz Validated Palette | Grafik interaktif ramah aksesibilitas dan ramah buta warna |
| **Penyimpanan Gambar** | Cloudinary REST API | Pengunggahan bukti nota foto pengeluaran hemat kuota |
| **CI/CD** | GitHub Actions Workflow | Kompilasi APK rilis otomatis, analisis kode, dan publishing resmi |

---

## 📁 Struktur Direktori Proyek

```
go-kar/
├── .github/
│   └── workflows/
│       ├── android-ci.yml        # CI otomatis: flutter analyze, test, dan rilis APK
│       └── firebase-rules.yml    # Validasi sintaks keamanan Firestore rules
├── android/
│   └── app/
│       ├── keystore/
│       │   └── kasgo_release.p12 # Keystore RSA 2048 permanen untuk signing konsisten
│       └── google-services.json  # Konfigurasi koneksi Firebase project resmi
├── assets/
│   └── images/
│       ├── app_icon.png          # Ikon launcher APK monogram emas
│       ├── logo.png              # Logo branding Karang Taruna
│       └── banners/              # Banner informasi custom PNG (Transparansi, Keliling, QRIS)
├── docs/
│   ├── arsitektur.md             # Dokumen teknis arsitektur software & data flow
│   ├── panduan-penggunaan.md     # Panduan cara pakai untuk Warga & 3 Admin
│   ├── download-apk.md           # Panduan cara instalasi APK di HP Android
│   ├── setup-firebase.md         # Langkah setup Cloud Firestore & Auth
│   └── setup-cloudinary.md       # Langkah setup penyimpanan nota Cloudinary
├── firebase/
│   ├── firestore.rules           # Aturan keamanan Firestore (proteksi 3 admin & warga)
│   └── firestore.indexes.json
├── lib/
│   ├── main.dart                 # Entry point, inisialisasi locale id_ID & Firebase
│   ├── app.dart                  # Root widget, Floating Dock Navbar, tema M3
│   ├── core/
│   │   ├── config/               # AppConfig (Production vs Demo flavor)
│   │   ├── constants/            # AppColors (Palet Ungu Royal & Kontras Tinggi)
│   │   ├── theme/                # AppTheme (Light & Dark theme)
│   │   └── utils/                # Formatters (Rupiah integer, tanggal Indonesia)
│   ├── data/
│   │   ├── local/                # AppDatabase (Drift SQLite schema & DAOs)
│   │   └── repositories/
│   │       ├── finance_repository.dart       # State transaksi kas real-time
│   │       └── user_profile_repository.dart  # State profil, peran, & Google Auth
│   ├── features/
│   │   ├── dashboard/            # HeroCard, QuickMenuGrid, Banner, Charts, Mutasi
│   │   ├── ledger/               # Buku transparansi kas lengkap dengan filter
│   │   ├── payments/             # Tab Scan QRIS, Transfer BCA, Form Jemput Tunai
│   │   ├── admin/                # Form Catat Kas Masuk & Catat Pengeluaran
│   │   ├── auth/                 # Halaman Profil, Ganti Peran Admin, Google Sign-In
│   │   └── splash/               # Animasi startup pembuka aplikasi
│   └── services/
│       └── firebase_sync_service.dart # Sinkronisasi 1 dokumen hemat kuota Firestore
├── test/
│   ├── app_config_test.dart
│   └── finance_calculation_test.dart # Unit tests kalkulasi saldo & tanggal
├── pubspec.yaml
└── README.md
```

---

## 📖 Dokumentasi Lengkap

Untuk panduan lebih mendalam, silakan baca berkas dokumentasi di folder `docs/`:
- 📐 **[Arsitektur Teknis & Database](docs/arsitektur.md)**: Penjelasan arsitektur local-first, skema Drift SQLite, dan optimasi kuota Firestore.
- 📱 **[Panduan Penggunaan](docs/panduan-penggunaan.md)**: Panduan langkah demi langkah untuk warga dan 3 admin pengurus.
- 📥 **[Cara Download & Pasang APK](docs/download-apk.md)**: Petunjuk instalasi APK Android di smartphone.
- 🔥 **[Setup Backend Firebase](docs/setup-firebase.md)**: Informasi project Firebase Jakarta, deployment rules, dan admin slots.
- ☁️ **[Setup Cloudinary](docs/setup-cloudinary.md)**: Petunjuk penyimpanan bukti kuitansi belanja organisasi.

---

## 👥 Pengembang & Lisensi

Dikembangkan untuk organisasi **Karang Taruna Indonesia**.  
Dikelola di bawah repositori privat: **[arbdevai/kas-go](https://github.com/arbdevai/kas-go)**.
