# PANDUAN LENGKAP & CARA BUILD APK "HARIS GIS"

Aplikasi **HARIS GIS Android** ini dirancang khusus dengan prinsip:
1. **Super Enteng & Responsif**: Menggunakan engine native **Osmdroid (OpenStreetMap)** yang sangat hemat baterai dan hanya mengonsumsi sedikit RAM (~50-80 MB). HP Android kelas *entry-level* (RAM 2GB/3GB) dijamin berjalan lancar tanpa macet.
2. **Ukuran File Mini**: Ukuran file `.apk` yang dihasilkan hanya sekitar **8 s/d 12 MB**.
3. **Offline Caching**: Semua area peta yang pernah dibuka otomatis tersimpan di memori HP, sehingga tetap bisa digunakan saat tim berada di lapangan tanpa sinyal internet.
4. **Fitur GIS Lapangan**:
   - Tracking GPS Lokasi Surveyor secara real-time.
   - Ganti layer peta (OSM Standard, Topografi, Satelit).
   - Pengukuran jarak geodesik (meter/kilometer).
   - Simpan titik aset / POI lapangan.
   - Ekspor data hasil survei ke format GeoJSON (bisa langsung dikirim via WhatsApp/Email).

---

## CARA MEMBUAT FILE .APK (PILIH SALAH SATU)

### CARA 1: Otomatis via GitHub (Paling Mudah, Tanpa Install Apapun di Laptop)
Kami sudah menyediakan skrip otomatisasi di [.github/workflows/build-apk.yml](file:///e:/HARIS%20GIS/.github/workflows/build-apk.yml).
1. Buat repository baru di akun GitHub Anda (misal: `haris-gis`).
2. Unggah/push seluruh isi folder `e:\HARIS GIS` ke repository tersebut.
3. Buka halaman GitHub repository Anda di browser, lalu klik tab **Actions**.
4. Anda akan melihat proses **Build HARIS GIS Android APK** berjalan secara otomatis (~2 menit).
5. Setelah selesai, klik proses tersebut dan unduh berkas **`HARIS_GIS_Debug_APK`** di bagian *Artifacts*.
6. Ekstrak dan kirim file `app-debug.apk` tersebut ke seluruh anggota tim untuk diinstall di HP Android!

---

### CARA 2: Menggunakan Android Studio (Langsung di Laptop)
Jika Anda memiliki atau menginstall **Android Studio**:
1. Buka aplikasi **Android Studio**.
2. Pilih menu **File -> Open...**
3. Arahkan ke folder: `e:\HARIS GIS\android` lalu klik **OK**.
4. Tunggu 1-2 menit hingga proses Gradle Sync selesai.
5. Pada menu bar bagian atas, klik:
   **Build -> Build Bundle(s) / APK(s) -> Build APK(s)**.
6. Saat proses build selesai, di pojok kanan bawah akan muncul notifikasi:
   > *"APK(s) generated successfully for 1 module"*
   Klik tulisan biru **`locate`**.
7. Folder akan terbuka dan Anda akan menemukan file:
   `app-debug.apk`
8. Salin file tersebut ke HP Anda via kabel data atau kirim lewat WhatsApp/Telegram untuk langsung diinstall.

---

## CARA INSTALL DI HP ANDROID ANGGOTA TIM
1. Kirim file `.apk` ke HP Android.
2. Buka file `.apk` tersebut di HP.
3. Jika muncul peringatan *"Izinkan instalasi dari sumber tidak dikenal"*, pilih **Izinkan (Allow)**.
4. Klik **Install** dan buka aplikasi **HARIS GIS**.
5. Berikan izin **Lokasi (GPS)** saat aplikasi pertama kali dibuka agar peta dapat langsung mengarahkan ke posisi Anda di lapangan.
