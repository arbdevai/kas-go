# Panduan Setup Firebase (dilakukan setelah akun tersambung)

Aplikasi Kas Go menggunakan Firebase Auth (email/password) dan Firestore (Spark tier gratis).

## Langkah-langkah
1. Buat project di [Firebase Console](https://console.firebase.google.com/).
2. Tambahkan aplikasi Android dengan package name:
   - Demo: `id.or.karangtaruna.kasgo.demo`
   - Production: `id.or.karangtaruna.kasgo`
3. Download `google-services.json` dan simpan sebagai GitHub Secret `GOOGLE_SERVICES_JSON` untuk build production.
4. Aktifkan **Authentication → Email/Password**.
5. Buat database **Firestore** (mode production, location asia-southeast2 disarankan).
6. Deploy aturan keamanan dari folder `firebase/`:
   ```bash
   firebase deploy --only firestore:rules
   ```
7. Daftarkan 3 akun admin lewat Authentication, lalu tulis UID mereka ke dokumen `organizations/ORG_ID/config/admin_slots` secara manual (provisioning berprivilege).
8. Daftarkan akun warga sebagai member di `organizations/ORG_ID/members/{uid}`.

## Catatan Penting
- Jangan pernah commit `google-services.json` asli ke repo.
- Pantau penggunaan harian di Firebase Console → Usage agar tidak melebihi kuota gratis (50rb reads, 20rb writes per hari).
