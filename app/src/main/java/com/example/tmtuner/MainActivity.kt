package com.example.tmtuner

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.tmtuner.ui.features.tuner.MicrotonalTunerScreen
import com.example.tmtuner.ui.features.tuner.TunerViewModel
import com.example.tmtuner.ui.theme.TMTunerTheme

class MainActivity : ComponentActivity() {

    private val tunerViewModel: TunerViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tunerViewModel.startListening()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 16 (API 37) pencere katmanını zorunlu opak yap ve kilitleri önle
        val opaqueColor = Color.parseColor("#121212")
        window.statusBarColor = opaqueColor
        window.navigationBarColor = opaqueColor
        window.decorView.setBackgroundColor(opaqueColor)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(opaqueColor),
            navigationBarStyle = SystemBarStyle.dark(opaqueColor)
        )

        setContent {
            TMTunerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MicrotonalTunerScreen(viewModel = tunerViewModel)
                }
            }

            // Mikrofon akışını onCreate içinde main thread'i kilitlemeden asenkron yönet
            LaunchedEffect(Unit) {
                checkPermissionAndStartListening()
            }
        }
    }

    private fun checkPermissionAndStartListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            tunerViewModel.startListening()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}