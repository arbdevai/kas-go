# Panduan Setup Cloudinary (dilakukan setelah akun tersambung)

Aplikasi Kas Go menggunakan Cloudinary untuk menyimpan foto bukti kuitansi/nota pengeluaran.

## Keamanan
- **JANGAN** menaruh API Secret Cloudinary di dalam kode Dart, APK, atau GitHub repo.
- Untuk produksi, gunakan signing endpoint terautentikasi (misalnya Cloudflare Worker) yang memverifikasi token Firebase Auth pengguna sebelum membuat signature upload.

## Langkah-langkah Awal
1. Daftar akun gratis di [Cloudinary](https://cloudinary.com/).
2. Buat upload preset unsigned terbatas (atau gunakan signed upload via worker).
3. Batasi folder tujuan (misalnya: `kas-go/receipts/`).
4. Batasi ukuran maksimal gambar (maksimal 2MB, kompresi client-side dianjurkan).
5. Simpan `cloud_name` di GitHub Secrets bila diperlukan untuk build.
