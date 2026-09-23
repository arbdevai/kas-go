# Panduan Download APK Kas Go

Seluruh proses kompilasi APK dilakukan secara otomatis oleh **GitHub Actions**.

## Cara Download APK (Direkomendasikan via GitHub Releases):
1. Buka halaman Releases di GitHub: **[https://github.com/arbdevai/kas-go/releases](https://github.com/arbdevai/kas-go/releases)**
2. Pada rilis terbaru (mis. **Kas Go Demo v1.0.0**), lihat bagian **Assets**.
3. Klik langsung file **`kas-go-demo-debug.apk`** untuk mengunduhnya ke smartphone atau laptop Anda.
4. Pasang (install) file `.apk` di smartphone Android Anda.

## Alternatif (via GitHub Actions):
1. Buka repository: `https://github.com/arbdevai/kas-go`
2. Klik tab **Actions** → pilih run terbaru.
3. Di bagian **Artifacts**, unduh `kas-go-demo-debug-apk`.

> **Catatan Instalasi:**
> - Pada smartphone Android, aktifkan opsi *"Install from unknown sources"* (Izinkan pemasangan aplikasi dari sumber tidak dikenal) bila diminta.
> - Aplikasi sudah ditandatangani dengan keystore konsisten (v1+v2+v3), sehingga saat Anda memasang versi APK terbaru di kemudian hari, aplikasi akan **langsung ter-update** tanpa perlu menghapus (uninstall) versi lama.
