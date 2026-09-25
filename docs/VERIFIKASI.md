# Verifikasi proyek

Tanggal: 25 September 2026.

## Build dan tes otomatis

Perintah:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

- Build debug: **berhasil**.
- Unit test: **12 lulus, 0 gagal, 0 error, 0 dilewati**.
- Android lint: **0 error, 5 warning**. Empat warning menyarankan versi dependency/Gradle yang lebih baru; satu menyarankan konfigurasi backup Android 12. Data aplikasi hanya tersimpan di memori dan backup dinonaktifkan.
- Lingkungan: Windows, JDK 25, Gradle 9.5.1, AGP 9.1.1, Android SDK 37.0, Build-Tools 36.0.0.

Laporan lengkap dapat dibuat ulang dengan perintah di atas. Laporan HTML berada di `app/build/reports/`.

## Perangkat pengujian

Emulator Pixel 8, Android API 35. APK debug berhasil dipasang dan activity berhasil dibuka. Saat startup emulator bersamaan dengan build pertama, aplikasi dan System UI sempat mengalami timeout. Setelah daemon build dihentikan dan startup emulator selesai, peluncuran ulang berhasil (`Status: ok`, cold launch sekitar 4,5 detik). Verifikasi ini bukan pengukuran performa perangkat nyata.

Screenshot di folder ini diambil dari aplikasi yang berjalan pada emulator, bukan mockup.

## Pemeriksaan interaksi pada emulator

Semua pemeriksaan berikut berhasil menggunakan interaksi layar dan pembacaan hierarki UI:

- Jeda menghentikan aliran; status menampilkan DIJEDA.
- Memilih Teknologi menampilkan kartu Teknologi dan menyembunyikan kartu Kampus.
- Membuka kartu menampilkan detail dan status Sudah dibaca.
- Kembali ke daftar menampilkan penghitung 01.
- Membuka ulang kartu yang sama mempertahankan penghitung 01.
- Simulasi error detail menampilkan Ada kendala dan tombol Coba lagi.
- Menekan Coba lagi memuat detail dengan sukses.
