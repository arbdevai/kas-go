# Panduan Penggunaan Aplikasi Kas Go

Panduan lengkap penggunaan fitur aplikasi **Kas Go** bagi Admin Pengurus Karang Taruna dan Warga Masyarakat.

---

## 1. Navigasi Utama (Floating Dock 2026)

Aplikasi memiliki 4 menu navigasi pada bagian bawah layar:
1. **Beranda**: Ringkasan saldo kas, tombol aksi cepat, banner edukasi, 3 grafik keuangan, dan riwayat mutasi terbaru.
2. **Transparansi**: Buku kas terbuka seluruh warga dengan filter *Semua*, *Pemasukan*, dan *Pengeluaran*.
3. **Bayar Kas**: Pusat pembayaran non-tunai (Scan QRIS, Transfer BCA) dan form jemput kas keliling ke rumah.
4. **Akun**: Profil pengguna, tombol edit identitas, integrasi akun Google, dan pemilihan peran 3 Admin.

---

## 2. Panduan untuk Warga

### A. Memantau Saldo & Transparansi Keuangan
1. Buka menu **Beranda** untuk melihat total saldo kas terkini, jumlah pemasukan, dan jumlah pengeluaran.
2. Ketuk ikon mata (*Sembunyikan/Tampilkan*) pada kartu saldo utama jika ingin menyembunyikan nominal saldo saat berada di tempat umum.
3. Di bagian **Analisis Keuangan**, ketuk tab:
   - **Pemasukan**: Grafik batang arus kas masuk bulanan.
   - **Pengeluaran**: Distribusi belanja kas per kategori (Operasional, Sosial, Perlengkapan, Kegiatan).
   - **Tren Saldo**: Pertumbuhan saldo bersih akumulatif dari waktu ke waktu.
4. Buka menu **Transparansi** untuk memeriksa setiap rupiah uang kas masuk dan keluar secara rinci beserta nama admin yang mencatatnya.

### B. Membayar Iuran Kas via QRIS
1. Buka menu **Bayar Kas** ➔ pilih tab **Scan QRIS**.
2. Buka aplikasi m-Banking (BCA, Mandiri, BRI, dll.) atau E-Wallet (GoPay, OVO, Dana).
3. Pindai kode QRIS Karang Taruna RW 05 yang tertera di layar.
4. Simpan bukti transfer untuk dokumentasi. Admin akan memverifikasi mutasi dan memasukkannya ke buku kas.

### C. Membayar Iuran Kas via Transfer Bank BCA
1. Buka menu **Bayar Kas** ➔ pilih tab **Transfer BCA**.
2. Ketuk tombol salin pada nomor rekening: **`8830 1928 3401`** (a.n. **KARANG TARUNA RW 05**).
3. Transfer iuran melalui m-BCA dengan berita: `Iuran Kas - [Nama Anda]`.

### D. Mengajukan Penjemputan Kas ke Rumah (*Door-to-Door*)
1. Buka menu **Bayar Kas** ➔ pilih tab **Jemput Tunai**.
2. Isi formulir:
   - Nama Lengkap Warga
   - Alamat Rumah & RT/RW
   - Nomor WhatsApp / HP aktif
   - Estimasi nominal uang kas yang akan disetorkan
   - Pilihan waktu ada di rumah (Pagi, Siang, Sore, atau Malam)
3. Ketuk **Ajukan Penjemputan Kas**. Petugas Kas Keliling (Admin 3) akan menerima notifikasi dan mendatangi rumah Anda sesuai jadwal.

### E. Menghubungkan Akun Google & Mengubah Profil
1. Buka menu **Akun**.
2. Ketuk tombol **Daftar Sekarang dengan Google** untuk menghubungkan akun.
3. Ketuk tombol pensil (**Edit Profil**) pada kartu profil atas untuk memperbarui nama lengkap, nomor telepon, atau alamat rumah Anda.

---

## 3. Panduan untuk 3 Admin Pengurus

### A. Memilih Akun Admin Pencatat
1. Buka menu **Akun**.
2. Pada bagian **Pilih Akun Admin Pencatat**, pilih posisi Anda:
   - **Admin 1 (Bendahara)**
   - **Admin 2 (Sekretaris)**
   - **Admin 3 (Koordinator Lapangan)**
3. Akun yang dipilih akan otomatis menjadi identitas resmi (*audit trail*) yang tercantum di setiap transaksi yang Anda catat.

### B. Mencatat Kas Masuk (Iuran / Donasi / Setoran Keliling)
1. Dari menu **Beranda**, ketuk tombol **Catat Kas Masuk** pada menu aksi cepat (atau ketuk tombol bulat `+` di kanan bawah).
2. Pilih nama anggota/warga dari daftar atau masukkan nama warga baru.
3. Masukkan periode iuran (format: `YYYY-MM`, contoh: `2026-09`).
4. Masukkan nominal uang kas (misal: `50000`).
5. Pilih metode penyetoran (*Tunai*, *QRIS*, *Transfer BCA*, atau *Jemput*).
6. Tambahkan catatan jika ada (opsional).
7. Ketuk **Simpan Kas Masuk**. Saldo total kas akan otomatis bertambah seketika dan tercatat di buku transparansi serta disinkronkan ke Cloud Firestore.

### C. Mencatat Pengeluaran Organisasi
1. Dari menu **Beranda**, ketuk tombol **Catat Pengeluaran** (atau tombol `+` ➔ *Catat Pengeluaran*).
2. Pilih kategori keperluan:
   - **Operasional**: Konsumsi rapat, transportasi pengurus, kas kecil.
   - **Sosial**: Santunan warga, bantuan duka, kerja bakti.
   - **Perlengkapan**: Pembelian tenda, sound system, kursi, spanduk.
   - **Kegiatan**: Lomba 17-an, peringatan hari besar, turnamen olahraga.
3. Masukkan nominal pengeluaran.
4. Masukkan nama toko, vendor, atau penerima dana.
5. Tuliskan uraian keperluan secara transparan.
6. Pilih tanggal pembelanjaan.
7. Ketuk **Simpan Pengeluaran**. Saldo kas akan otomatis berkurang secara akurat.
