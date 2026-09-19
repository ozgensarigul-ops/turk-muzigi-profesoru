package com.example.tmtuner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tmtuner.data.models.AhenkType

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // İzin durum yönetimi
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val tunerViewModel: TMTunerViewModel = viewModel()
                    
                    // Canlı mikrofon akışını başlat
                    LaunchedEffect(Unit) {
                        tunerViewModel.startMicrophoneAnalysis()
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        MakamScreen(viewModel = tunerViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun MakamScreen(viewModel: TMTunerViewModel) {
    val selectedAhenk by viewModel.selectedAhenk.collectAsState()
    val isEbAltoSax by viewModel.isEbAltoSax.collectAsState()
    val applySegahNuance by viewModel.applySegahNuance.collectAsState()
    val currentPitch by viewModel.currentPitch.collectAsState()
    val recognitionResult by viewModel.recognitionResult.collectAsState()

    var ahenkExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Makam & Perde Analiz Laboratuvarı",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Canlı Analiz Sonuç Kartı
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Canlı DSP Frekans & Makam Sınıflandırma", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentPitch?.let { "${String.format(java.util.Locale.US, "%.2f", it.frequency)} Hz" } ?: "Ses Bekleniyor...",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = currentPitch?.let { "Perde: ${it.noteName} (${it.centsOffset} cent)" } ?: "Mikrofon Dinleniyor",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = recognitionResult?.let { "Tespit Edilen Makam: ${it.makamAdi} (%${(it.confidence * 100).toInt()})" } ?: "Makam Taranıyor...",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Sistem & Enstrüman Parametreleri",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ahenk Düzeni:", color = Color.Gray)
                    Box {
                        Button(onClick = { ahenkExpanded = true }) {
                            Text(selectedAhenk.name)
                        }
                        DropdownMenu(
                            expanded = ahenkExpanded,
                            onDismissRequest = { ahenkExpanded = false }
                        ) {
                            AhenkType.values().forEach { ahenk ->
                                DropdownMenuItem(
                                    text = { Text(ahenk.name) },
                                    onClick = {
                                        viewModel.setAhenk(ahenk)
                                        ahenkExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Eb Alto Saksafon Transpoze", fontWeight = FontWeight.SemiBold)
                        Text("Duyulan ses x 27/16 Pisagor çarpanı", fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isEbAltoSax,
                        onCheckedChange = { viewModel.setEbAltoSax(it) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Segâh İcra Tavrı (-1 Koma)", fontWeight = FontWeight.SemiBold)
                        Text("Geleneksel pestleşme nüansı", fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = applySegahNuance,
                        onCheckedChange = { viewModel.setSegahNuance(it) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DroneScreen(viewModel = viewModel)
    }
}

@Composable
fun DroneScreen(viewModel: TMTunerViewModel) {
    val isPlaying by viewModel.isDronePlaying.collectAsState()
    val volume by viewModel.droneVolume.collectAsState()
    val currentAhenk by viewModel.selectedAhenk.collectAsState()
    val kararSesi = viewModel.getDroneNoteName()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Hedef Karar Sesi", color = Color.Gray, fontSize = 18.sp)
        Text(kararSesi, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("(${currentAhenk.name})", fontSize = 16.sp, color = Color.LightGray, modifier = Modifier.padding(top = 8.dp))
        Spacer(modifier = Modifier.height(24.dp))
        LargeFloatingActionButton(
            onClick = { viewModel.toggleDrone() },
            containerColor = if (isPlaying) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Drone Kontrol"
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Ses Seviyesi: ${(volume * 100).toInt()}%", fontSize = 16.sp)
        Slider(
            value = volume,
            onValueChange = { viewModel.setDroneVolume(it) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}