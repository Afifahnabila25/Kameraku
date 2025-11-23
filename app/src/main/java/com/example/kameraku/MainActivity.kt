package com.example.kameraku

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.example.kameraku.ui.theme.KamerakuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KamerakuTheme {
                // State untuk menyimpan status izin
                var hasPermission by remember { mutableStateOf(false) }

                // Launcher untuk meminta izin [cite: 130-131]
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { granted -> hasPermission = granted }
                )

                // Cek izin saat pertama kali dijalankan [cite: 133]
                LaunchedEffect(Unit) {
                    launcher.launch(Manifest.permission.CAMERA)
                }

                if (hasPermission) {
                    // Jika izin diberikan, tampilkan layar kamera
                    CameraScreen()
                } else {
                    // Tampilan jika izin belum diberikan (Bisa diganti Text biasa)
                    androidx.compose.material3.Text("Aplikasi membutuhkan izin kamera")
                }
            }
        }
    }
}