# Panduan Desain & Antarmuka Kas Go (Modern Finansial 2026)

Dokumen ini adalah acuan resmi desain antarmuka (*Design System & UI/UX Style Guide*) aplikasi Kas Go. Semua perubahan antarmuka wajib mengacu pada standar di bawah ini untuk menjaga konsistensi, kebersihan visual, dan kenyamanan pengguna.

---

## 1. Filosofi Desain: Clean White & Precision Violet
- **Dominasi Putih Lapang (80%)**: Latar belakang aplikasi menggunakan kanvas putih dingin bersih (`#F8FAFC`) untuk kesan modern, profesional, dan lapang.
- **Permukaan Kartu Elegan (15%)**: Menggunakan putih murni (`#FFFFFF`) dengan sudut melengkung modern (*squircle*) dan garis batas sangat tipis (`0.75dp - 1dp`, `#E2E8F0`).
- **Aksen Presisi (5%)**: Sentuhan warna *Royal Indigo / Violet* (`#4F46E5`, `#6366F1`) hanya untuk indikator aktif, tombol utama, dan sorotan saldo.
- **Bebas Jargon & Teks Berlebihan**: Mengikuti `copywriting-rules.md`. Bahasa singkat, padat, dan fungsional seperti aplikasi finansial modern (BCA Mobile, Bank Jago, Flip).

---

## 2. Struktur Elemen Halaman Utama (Beranda)

### A. Top Bar Minimalis
- Logo monogram Kas Go ringkas di kiri atas.
- Nama unit organisasi (contoh: *RT 04 / RW 09*) dengan font sedang yang rapi.
- Lonceng notifikasi / tombol status di kanan atas dalam wadah lingkaran berlatar putih halus.

### B. Hero Card Banner (Kartu Saldo & Ringkasan)
- **Visual**: Gradien matte *Deep Royal Indigo to Violet* (`#4F46E5` ke `#6366F1`). Tidak menggunakan efek kilap norak.
- **Konten Menyatu**:
  - Label kecil: "Total Saldo Kas".
  - Nominal saldo utama: Cetak tebal dan tegas (`26sp`, `FontWeight.ExtraBold`).
  - Sub-baris terintegrasi: Kas masuk & kas keluar bulan berjalan dalam 1 kartu, dipisah divider tipis semi-transparan.

### C. Menu Grid Modular (Aksi Cepat 4 Kolom)
- **Layout**: Baris 4 kotak modular (*Masuk, Keluar, Bayar, Rekap*).
- **Wadah**: Squircle bersudut melengkung (`RoundedCornerShape(16.dp)`), latar belakang putih dengan border tipis `1.dp` (`#F1F5F9`).
- **Ikon**: Ikon garis ramping (*linear duotone*) dalam lingkaran pastel lembut (`#EEF2FF`), dengan warna ikon ungu royal (`#4F46E5`).
- **Label**: Maksimal 1 kata per menu, ditempatkan di bawah ikon dengan font tebal (`12sp`).

### D. Grafik Arus Kas Modern (Trend Chart)
- **Bentuk**: Grafik kurva halus (*Smooth Cubic Bézier Curve*) dengan arsiran gradien transparan di bawah garis kurva (*Violet Gradient Fill*).
- **Animasi**: Garis kurva dan arsiran digambar secara bertahap (*smooth animated draw*) saat halaman dibuka.
- **Interaksi**: Titik aktif (*pulse dot*) pada data terakhir dan kemampuan sentuh (*interactive scrubbing*) untuk melihat angka nominal kas per periode.
- **Kontainer**: Ditempatkan dalam satu kontainer putih bersih bersudut `20dp` lengkap dengan filter rentang waktu (Minggu Ini / Bulan Ini).

### E. Grouped List (Anti-Card Fatigue)
- **Larangan**: Dilarang membungkus setiap 1 transaksi ke dalam card terpisah yang membuat layar penuh kotak bertumpuk-tumpuk.
- **Standar**: Seluruh daftar transaksi dikelompokkan ke dalam **1 Kontainer Putih Utuh** (`RoundedCornerShape(20.dp)`):
  - Garis batas halus `0.75dp` (`#E2E8F0`).
  - Pemisah antar transaksi menggunakan garis pembatas ultra-tipis (`0.5dp`, `#F1F5F9`).
  - Tinggi baris kompak (56–60dp): Ikon kategori di kiri, judul & waktu di tengah, nominal di kanan (hijau untuk masuk, slate gelap untuk keluar).

---

## 3. Komponen Form & Input Modern
- **Bukan Gaya Bawaan Lama**: Menghilangkan `OutlinedTextField` standar yang memotong garis border.
- **Wadah Input Squircle**: Sudut lengkung `14.dp`, latar putih bersih, dengan border `1.dp` abu-abu terang (`#E2E8F0`).
- **Label Luar**: Label diletakkan rapi di atas kolom input dengan font kecil (`12sp`, `#64748B`), bukan melompat-lompat di atas garis.
- **Fokus Interaktif**: Saat aktif diketik, border beralih halus ke `#4F46E5` (`1.5dp`) dengan transisi warna halus.
- **Tombol Form**: Tinggi 48–50dp, sudut membulat `14dp`, teks *Sentence case* tegas.

---

## 4. Modern Slim Floating Dock (Navigasi Bawah)
- **Dimensi**: Tinggi kompak 54dp, melayang 12dp di atas tepi bawah layar, margin horizontal 16dp.
- **Latar & Bayangan**: Putih bersih solid (`#FFFFFF`), border halus `1.dp` (`#E2E8F0`), bayangan lembut ambient (`6dp`, bukan drop shadow pekat).
- **Status Tab Aktif**: Kapsul melengkung warna pastel (*soft violet pill* `#EEF2FF`) dengan ikon dan teks ungu royal (`#4F46E5`).
- **Status Tab Non-Aktif**: Ikon abu-abu ramping (`#64748B`) tanpa teks, menjaga kerapian navigasi.

---

## 5. Standar Animasi & Transisi
- **Transisi Antar Layar**: Menggunakan `Crossfade` atau `AnimatedContent` dengan transisi halus (`tween(300)`).
- **Munculnya Data (Entrance)**: Efek `fadeIn(tween(350))` dikombinasikan dengan pergeseran vertikal halus (`slideInVertically`).
- **Respons Sentuhan**: Efek klik halus (*subtle ripple / scale tap*) tanpa jeda kaku.
- **Gambar Kurva**: Menggunakan `Animatable(0f)` menuju `1f` dengan *FastOutSlowInEasing* untuk menggambar garis kurva grafik secara dinamis.

---

## 6. Tabel Token Warna Resmi

| Nama Token | Nilai Hex | Peruntukan |
| :--- | :--- | :--- |
| `CanvasBackground` | `#F8FAFC` | Latar belakang seluruh layar aplikasi |
| `CardSurface` | `#FFFFFF` | Latar kartu, grouped container, dialog, dan dock |
| `BorderSubtle` | `#E2E8F0` | Garis tepi kontainer dan kartu (0.75–1dp) |
| `DividerLight` | `#F1F5F9` | Garis pemisah baris data (0.5dp) |
| `PrimaryRoyal` | `#4F46E5` | Warna utama, tombol primer, tab aktif, ikon menu |
| `PrimaryGradientEnd` | `#6366F1` | Titik akhir gradien kartu hero |
| `SurfaceLavender` | `#EEF2FF` | Latar belakang ikon menu, kapsul tab, badge |
| `TextPrimary` | `#0F172A` | Teks judul, nominal uang, teks utama |
| `TextSecondary` | `#64748B` | Label form, tanggal, deskripsi singkat |
| `TextMuted` | `#94A3B8` | Teks keterangan pendukung, versi aplikasi |
| `IncomeGreen` | `#10B981` | Kas masuk, indikator surplus, status lunas |
| `IncomeGreenBg` | `#ECFDF5` | Latar belakang badge / ikon kas masuk |
| `ExpenseRed` | `#EF4444` | Kas keluar |
| `ExpenseRedBg` | `#FEF2F2` | Latar belakang badge / ikon kas keluar |

---

## 7. Ketentuan Konsistensi Antar Layar
Setiap layar wajib mengadopsi format ini secara seragam:
1. **Beranda**: Hero card banner + Menu grid 4 kotak + Trend chart modern + Grouped list transaksi terakhir.
2. **Buku Kas**: Kapsul filter periode (Pill tabs) + Satu grouped container putih untuk seluruh histori mutasi.
3. **Bayar Kas**: Status iuran dalam grouped container + Opsi pembayaran (QRIS/Transfer) berpenampilan bersih.
4. **Profil & Pengaturan**: Kartu profil ringkas + Menu grouped list (gaya pengaturan modern iOS/fintech).
