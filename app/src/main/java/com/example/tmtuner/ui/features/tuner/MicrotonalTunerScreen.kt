package com.example.tmtuner.ui.features.tuner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tmtuner.core.audio.model.SegahNuanceMode
import com.example.tmtuner.core.musicology.model.Ahenk
import com.example.tmtuner.core.musicology.model.NeyType
import com.example.tmtuner.core.musicology.model.TransposingInstrument
import com.example.tmtuner.ui.components.KomaGaugeDial
import java.util.Locale

/**
 * 53-EDO Türk Müziği Mikrotonal Akort Ekranı (MicrotonalTunerScreen).
 *
 * Özellikler:
 * - -9 ile +9 koma kadranı (KomaGaugeDial)
 * - Segâh İcra Nüansı (-1 / -2 koma) rozeti ve kehribar (#FFB300) ibre modu
 * - Dinamik Ahenk (Mansur, Bolâhenk, Kız vb.) seçimi
 * - Transpoze Enstrüman (Konsert Do, Eb Alto Sax, Bb Tenor Sax) seçimi
 * - Ney Çeşitleri (Mansur Ney, Kız Ney, Bolâhenk Ney vb.) hızlı seçimi
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MicrotonalTunerScreen(
    viewModel: TunerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var ahenkMenuExpanded by remember { mutableStateOf(false) }
    var instrumentMenuExpanded by remember { mutableStateOf(false) }
    var neyMenuExpanded by remember { mutableStateOf(false) }

    val amberColor = Color(0xFFFFB300)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Mikrotonal Akort",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = "53-EDO AEU Sistemi & İsmail Hakkı Özkan Cetveli",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Segâh Nuance Badge (Özkan s. 51)
        AnimatedVisibility(
            visible = uiState.isSegahNuanceActive,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                color = amberColor.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, amberColor)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Segâh Rozeti",
                        tint = amberColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Segâh İcra Nüansı Aktif",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFE65100)
                        )
                        Text(
                            text = "İsmail Hakkı Özkan s. 51: Geleneksel -1/-2 koma pest icra toleransı tanındı.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 3. Main Dial Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Perde Name Big Readout
                Text(
                    text = uiState.perdeName,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (uiState.isSegahNuanceActive) amberColor else MaterialTheme.colorScheme.primary
                )

                if (uiState.octaveName.isNotEmpty()) {
                    Text(
                        text = "${uiState.octaveName} Oktav (${uiState.mutlakKoma}. Koma)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Koma Gauge Dial (-9 to +9 Koma)
                KomaGaugeDial(
                    komaOffset = uiState.komaDifference,
                    centsOffset = uiState.centsDifference,
                    isInTune = uiState.isTuned,
                    isSegahNuanceActive = uiState.isSegahNuanceActive
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Frequency Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Hedef Frekans", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text(
                            text = if (uiState.targetFrequency > 0) {
                                String.format(Locale.US, "%.2f Hz", uiState.targetFrequency)
                            } else "—",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Algılanan Ses", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text(
                            text = if (uiState.detectedFrequency > 0) {
                                String.format(Locale.US, "%.2f Hz", uiState.detectedFrequency)
                            } else "—",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = if (uiState.isTuned) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (uiState.selectedInstrument != TransposingInstrument.CONCERT_C) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Duyulan Konsert", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = if (uiState.concertFrequency > 0) {
                                    String.format(Locale.US, "%.2f Hz", uiState.concertFrequency)
                                } else "—",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Microphone Toggle Button
        FloatingActionButton(
            onClick = { viewModel.toggleListening() },
            containerColor = if (uiState.isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = if (uiState.isRecording) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = if (uiState.isRecording) "Mikrofonu Durdur" else "Mikrofonu Başlat",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = uiState.statusMessage,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Selectors & Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Ahenk ve Enstrüman Seçenekleri",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Ahenk Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ahenk Düzeni", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(uiState.selectedAhenk.displayName, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }

                    Box {
                        Button(onClick = { ahenkMenuExpanded = true }) {
                            Text(uiState.selectedAhenk.name)
                        }
                        DropdownMenu(
                            expanded = ahenkMenuExpanded,
                            onDismissRequest = { ahenkMenuExpanded = false }
                        ) {
                            Ahenk.entries.forEach { ahenk ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(ahenk.name, fontWeight = FontWeight.Bold)
                                            Text(ahenk.displayName, fontSize = 11.sp)
                                        }
                                    },
                                    onClick = {
                                        viewModel.setAhenk(ahenk)
                                        ahenkMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Instrument Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enstrüman Sınıfı", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(uiState.selectedInstrument.displayName, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }

                    Box {
                        Button(onClick = { instrumentMenuExpanded = true }) {
                            Text(uiState.selectedInstrument.name)
                        }
                        DropdownMenu(
                            expanded = instrumentMenuExpanded,
                            onDismissRequest = { instrumentMenuExpanded = false }
                        ) {
                            TransposingInstrument.entries.forEach { inst ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(inst.displayName, fontWeight = FontWeight.Bold)
                                            Text(inst.description, fontSize = 11.sp)
                                        }
                                    },
                                    onClick = {
                                        viewModel.setInstrument(inst)
                                        instrumentMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ney Type Quick Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ney Çeşidi", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(
                            "${uiState.selectedNeyType.displayName} (${uiState.selectedNeyType.approximateLengthCm})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Box {
                        OutlinedButton(onClick = { neyMenuExpanded = true }) {
                            Text(uiState.selectedNeyType.displayName)
                        }
                        DropdownMenu(
                            expanded = neyMenuExpanded,
                            onDismissRequest = { neyMenuExpanded = false }
                        ) {
                            NeyType.entries.forEach { ney ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(ney.displayName, fontWeight = FontWeight.Bold)
                                            Text("Râst: ${ney.rastNoteInWestern} | Boy: ${ney.approximateLengthCm}", fontSize = 11.sp)
                                        }
                                    },
                                    onClick = {
                                        viewModel.setNeyType(ney, syncAhenk = true)
                                        neyMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Segâh Nuance Mode Selector Chips
                Text("Segâh İcra Nüansı Modu (Özkan s. 51):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SegahNuanceMode.entries.forEach { mode ->
                        FilterChip(
                            selected = uiState.segahMode == mode,
                            onClick = { viewModel.setSegahMode(mode) },
                            label = { Text(mode.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = amberColor.copy(alpha = 0.25f),
                                selectedLabelColor = Color(0xFFBF360C)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Physical Mansur vs Concert Pitch Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Fizik Kaba Çârgâh = 256 Hz", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(
                            if (uiState.usePhysicalMansur) "Mansur Çârgâh 256 Hz (Fizik Düzeni)" else "Standart Diyapazon (Dügâh = 440 Hz)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Switch(
                        checked = uiState.usePhysicalMansur,
                        onCheckedChange = { viewModel.setUsePhysicalMansur(it) }
                    )
                }
            }
        }
    }
}
