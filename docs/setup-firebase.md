# Status Integrasi Firebase

Firebase belum terhubung ke aplikasi Android Compose yang dibangun oleh GitHub Actions.
Project ID yang muncul di `FirebaseSyncService.kt` dan file aturan Firestore saja tidak
membuat autentikasi atau sinkronisasi aktif. Saat ini data profil, peran, kas, tagihan,
dan permintaan jemput disimpan lokal di tiap perangkat. Jangan gunakan APK ini sebagai
sumber data kas bersama untuk warga dan pengurus sebelum integrasi server selesai.

Sebelum mengaktifkan sinkronisasi, implementasikan dan verifikasi semua hal berikut:

1. Firebase Authentication yang memverifikasi identitas; pemilih email Google Android
   saat ini hanya memilih alamat dan bukan proses autentikasi.
2. Registrasi anggota yang disimpan di Firestore, serta provisioning pengurus yang tidak
   memberi hak admin kepada pengguna pertama di setiap perangkat.
3. Repository untuk transaksi, tagihan, bukti pembayaran, dan penjemputan yang memakai
   Firestore dan aturan di `firebase/firestore.rules`.
4. Proses admin awal yang aman untuk mengisi `organizations/{orgId}/config/admin_slots`.
5. Konfigurasi aplikasi Android dari project Firebase dan alur deploy rules yang
   memakai kredensial deploy terbatas. Jangan commit kredensial atau private key.

`google-services.json` bukan rahasia server, tetapi tetap harus berasal dari project
Firebase yang benar. Service account atau private key tidak boleh dimasukkan ke APK,
repository, atau chat. Setelah integrasi dibuat, uji akses dengan akun warga dan admin
terpisah sebelum APK dipakai untuk pencatatan kas sebenarnya.
