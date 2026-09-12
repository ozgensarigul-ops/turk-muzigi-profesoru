package com.example.tmtuner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tmtuner.data.models.AhenkType

data class TemelPerdeUiModel(
    val ad: String,
    val bati: String,
    val pisagorOrani: String,
    val mutlakKoma: Int,
    val bolahenkFreq: Double,
    val mansurFreq: Double
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val tunerViewModel: TMTunerViewModel = viewModel()
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

    var ahenkExpanded by remember { mutableStateOf(false) }

    val temelPerdeler = remember {
        listOf(
            TemelPerdeUiModel("Râst", "G4", "3/2", 31, 293.33, 384.00),
            TemelPerdeUiModel("Dügâh", "A4", "27/16", 40, 330.00, 432.00),
            TemelPerdeUiModel("Segâh", "B4 (1k b.)", "4096/2187", 48, 366.23, 479.44),
            TemelPerdeUiModel("Çârgâh", "C4", "2/1", 53, 391.11, 512.00),
            TemelPerdeUiModel("Nevâ", "D4", "9/8", 62, 440.00, 576.00),
            TemelPerdeUiModel("Hüseynî", "E4", "81/64", 71, 495.00, 648.00),
            TemelPerdeUiModel("Eviç", "F#4 (4k d.)", "729/512", 80, 549.66, 719.18),
            TemelPerdeUiModel("Gerdâniye", "G5", "3/1", 84, 586.66, 768.00)
        )
    }

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

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "8 Temel Perde Matrisi (53-EDO & Pisagor)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Perde", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                    Text("Batı", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("Koma", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                    Text("Frekans", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                }
                HorizontalDivider()

                temelPerdeler.forEach { perde ->
                    val rawFreq = if (selectedAhenk == AhenkType.MANSUR) perde.mansurFreq else perde.bolahenkFreq
                    val nuanceAdjusted = if (applySegahNuance && perde.ad == "Segâh") {
                        rawFreq * Math.pow(2.0, -1.0 / 53.0)
                    } else rawFreq
                    val finalFreq = if (isEbAltoSax) nuanceAdjusted * (27.0 / 16.0) else nuanceAdjusted

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(perde.ad, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.2f))
                        Text(perde.bati, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                        Text("${perde.mutlakKoma}", fontSize = 13.sp, modifier = Modifier.weight(0.8f))
                        Text(
                            String.format(java.util.Locale.US, "%.2f Hz", finalFreq),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1.2f),
                            textAlign = TextAlign.End
                        )
                    }
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