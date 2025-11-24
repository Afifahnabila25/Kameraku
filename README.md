Kameraku 📸
Kameraku adalah aplikasi kamera Android sederhana yang dibangun menggunakan Kotlin dan Jetpack Compose. Aplikasi ini memanfaatkan CameraX untuk fungsionalitas kamera dan MediaStore API untuk penyimpanan gambar yang aman (Scoped Storage).

🛠 Teknologi yang Digunakan
Bahasa: Kotlin

UI Framework: Jetpack Compose (Material3)

Camera Library: Android CameraX (camera-core, camera-camera2, camera-lifecycle, camera-view)

Dependency Injection: Manual (melalui Context dan Compose CompositionLocal)

📂 Alur Kerja Kode (Code Flow)
Berikut adalah penjelasan detail mengenai bagaimana fitur-fitur utama diimplementasikan dalam kode:

1. Penanganan Izin (Permission Handling)
Lokasi File: MainActivity.kt

Aplikasi tidak langsung membuka kamera saat dijalankan. Ia memastikan pengguna memberikan izin terlebih dahulu.

Pengecekan Awal: Menggunakan state hasPermission untuk melacak status izin.

Launcher Izin: rememberLauncherForActivityResult dengan kontrak ActivityResultContracts.RequestPermission() disiapkan untuk menangani dialog sistem Android.

Eksekusi: Saat aplikasi dimulai (LaunchedEffect(Unit)), aplikasi memicu peluncuran permintaan izin untuk Manifest.permission.CAMERA.

Logika UI:

Jika hasPermission == true -> Tampilkan CameraScreen().

Jika hasPermission == false -> Tampilkan pesan teks "Aplikasi membutuhkan izin kamera".

2. Implementasi Preview Kamera
Lokasi File: CameraScreen.kt

Karena PreviewView (komponen CameraX) adalah View berbasis XML (Legacy View), kita perlu membungkusnya agar bisa digunakan di Jetpack Compose.

AndroidView: Digunakan untuk me-render PreviewView di dalam layout Compose.

Lifecycle Binding: CameraX diikat ke siklus hidup aplikasi (lifecycleOwner) menggunakan ProcessCameraProvider. Ini memastikan kamera otomatis berhenti saat aplikasi diminimalkan untuk menghemat baterai.

3. Logika Switch Kamera (Rotasi Lensa)
Lokasi File: CameraScreen.kt

Fitur ini memungkinkan pengguna menukar antara kamera depan dan belakang.

State Lensa: Variabel lensFacing menyimpan status lensa saat ini (default: CameraSelector.DEFAULT_BACK_CAMERA).

Tombol Switch: Saat tombol ditekan, logika sederhana menukar nilai lensFacing:

Kotlin

lensFacing = if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
    CameraSelector.DEFAULT_FRONT_CAMERA
} else {
    CameraSelector.DEFAULT_BACK_CAMERA
}
Re-Binding (LaunchedEffect): Blok LaunchedEffect(lensFacing) akan dijalankan ulang setiap kali nilai lensFacing berubah. Langkah-langkahnya:

cameraProvider.unbindAll(): Melepas koneksi kamera yang sedang aktif.

cameraProvider.bindToLifecycle(...): Mengikat ulang kamera dengan lensFacing yang baru (Depan/Belakang).

4. Pengambilan & Penyimpanan Foto (MediaStore)
Lokasi File: CameraScreen.kt -> Fungsi takePhoto()

Aplikasi menggunakan MediaStore API, yang merupakan cara standar dan aman untuk menyimpan media di Android modern (Android 10+), tanpa memerlukan izin Write External Storage secara eksplisit untuk file yang dibuat sendiri.

ContentValues: Menyiapkan metadata untuk foto baru:

DISPLAY_NAME: Nama file unik berdasarkan waktu (KameraKu_{timestamp}.jpg).

MIME_TYPE: Format gambar (image/jpeg).

RELATIVE_PATH: Folder tujuan (Pictures/KameraKu). Catatan: Ini hanya berlaku untuk Android P ke atas.

OutputOptions: Memberitahu CameraX untuk menyimpan hasil foto ke MediaStore.Images.Media.EXTERNAL_CONTENT_URI menggunakan ContentResolver.

Eksekusi: imageCapture.takePicture(...) mengambil gambar dan menyimpannya secara asinkron. Callback OnImageSavedCallback menangani hasil sukses atau gagal (menampilkan Toast).

📋 Struktur Folder Utama
MainActivity.kt: Titik masuk aplikasi (Entry Point) & Manajemen Izin.

CameraScreen.kt:

UI Kamera (Preview, Tombol Shutter, Tombol Switch).

Logika CameraX (bindToLifecycle).

Logika takePhoto (MediaStore).

ui/theme/: Berisi definisi tema, warna, dan tipografi aplikasi.

📱 Persyaratan Sistem
Min SDK: 24 (Android 7.0 Nougat)

Target SDK: 36 (Android 15)

Compile SDK: 36
