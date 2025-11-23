package com.example.kameraku

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch // Icon Switch
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // STATE 1: Menyimpan pilihan kamera (Depan/Belakang)
    // Default mulai dari kamera belakang [cite: 41]
    var lensFacing by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val previewView = remember { PreviewView(context) }

    // Re-launch efek ini setiap kali 'lensFacing' berubah (saat tombol ditekan)
    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()

        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)

        val imageCaptureUseCase = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        imageCapture = imageCaptureUseCase

        try {
            // PENTING: Unbind dulu sebelum bind ulang
            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                lensFacing, // Gunakan state yang dipilih (Depan/Belakang)
                preview,
                imageCaptureUseCase
            )
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal switch kamera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }




    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Tombol Switch Kamera (Pojok Kanan Atas)
        IconButton(
            onClick = {
                // Logika Toggle: Jika sekarang Belakang -> Ubah ke Depan, dan sebaliknya
                lensFacing = if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd) // Posisi di atas kanan
                .padding(16.dp)
                .statusBarsPadding() // Agar tidak tertutup status bar
        ) {
            // Menggunakan Icon CameraSwitch (pastikan library icons terinstall)
            // Atau ganti Icons.Default.Refresh jika belum ada
            Icon(
                imageVector = Icons.Default.Cameraswitch,
                contentDescription = "Switch Camera",
                tint = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(32.dp)
            )
        }

        // Tombol Ambil Foto (Tengah Bawah)
        FloatingActionButton(
            onClick = {
                imageCapture?.let { capture ->
                    takePhoto(context, capture, lensFacing) // Kirim info lensa untuk rotasi
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
        ) {
            Icon(Icons.Default.Camera, contentDescription = "Ambil Foto")
        }
    }
}

// ... (Fungsi getCameraProvider TETAP SAMA seperti sebelumnya) ...

// Update sedikit di fungsi takePhoto untuk menangani rotasi kamera depan (Mirroring)
fun takePhoto(context: Context, imageCapture: ImageCapture, lensFacing: CameraSelector) {
    val name = "KameraKu_${System.currentTimeMillis()}.jpg"

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/KameraKu")
        }
    }

    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        .build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                Toast.makeText(context, "Foto tersimpan!", Toast.LENGTH_SHORT).show()
            }
            override fun onError(exc: ImageCaptureException) {
                Toast.makeText(context, "Gagal: ${exc.message}", Toast.LENGTH_SHORT).show()
            }
        }
    )
}
// Tempel ini di paling bawah file, di luar fungsi CameraScreen()
suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
    cameraProviderFuture.addListener({
        continuation.resume(cameraProviderFuture.get())
    }, ContextCompat.getMainExecutor(this))
}