package com.example.tmtuner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tmtuner.core.musicology.model.MakamDetectionResult
import com.example.tmtuner.core.musicology.model.SeyirType
import kotlin.math.roundToInt

/**
 * Türk Müziği Canlı Makam ve Seyir Gösterge Paneli (MakamSeyirPanel).
 *
 * İsmail Hakkı Özkan nazariyatına uygun olarak:
 * - 53-EDO Makam Tanıma Motoru (MakamDetectionEngine) çıktısını görselleştirir.
 * - Dinamik renkli Güven Rozeti (%80+ yeşil, %60-%80 kehribar, <%60 gri) ve LinearProgressIndicator.
 * - Çıkıcı (↑), İnici-Çıkıcı (↕) veya İnici (↓) seyir karakterini vurgular.
 * - Durak (Karar) ve Güçlü perdelerini rozetler halinde sunar.
 * - Son 20-30 saniyelik perde süre dağılımını mini histogram barlarıyla çizer.
 */
@Composable
fun MakamSeyirPanel(
    detectionResult: MakamDetectionResult?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A) // Koyu tema #121212 ile uyumlu kart yüzeyi
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (detectionResult != null && detectionResult.confidence >= 0.70f) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            } else {
                Color(0xFF2C2C2C)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 1. Üst Başlık ve Rozet Çubuğu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (detectionResult != null) Color(0xFF00E676) else Color(0xFF757575)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CANLI MAKÂM & SEYİR ANALİZİ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (detectionResult != null) {
                    val confPercent = (detectionResult.confidence * 100).roundToInt()
                    val (badgeColor, badgeContainer) = when {
                        confPercent >= 80 -> Color(0xFF00E676) to Color(0xFF003314)
                        confPercent >= 60 -> Color(0xFFFFB300) to Color(0xFF332400)
                        else -> Color(0xFF9E9E9E) to Color(0xFF242424)
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "%$confPercent Güven",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (detectionResult == null) {
                // Bekleme Durumu (Placeholder)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Ezgi icra edildikçe makam ve seyir otomatik belirlenecektir...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // 2. Makam Adı ve Dizi Tanımı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = detectionResult.matchedMakam.name.uppercase(),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = Color(0xFFFFFFFF),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "53-EDO: ${detectionResult.matchedMakam.scalePerdeler.joinToString(" - ")}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // İnce Güven Skoru Çizgisi (LinearProgressIndicator)
                val confPercent = (detectionResult.confidence * 100).roundToInt()
                val progressColor = when {
                    confPercent >= 80 -> Color(0xFF00E676)
                    confPercent >= 60 -> Color(0xFFFFB300)
                    else -> Color(0xFF757575)
                }

                LinearProgressIndicator(
                    progress = { detectionResult.confidence.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = progressColor,
                    trackColor = Color(0xFF2C2C2C),
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Seyir, Durak ve Güçlü Rozetleri
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Seyir Rozeti
                    val (seyirText, seyirColor) = when (detectionResult.detectedSeyir) {
                        SeyirType.CIKICI -> "↑ Çıkıcı Seyir" to Color(0xFF00B0FF)
                        SeyirType.INICI_CIKICI -> "↕ İnici-Çıkıcı" to Color(0xFFFF9100)
                        SeyirType.INICI -> "↓ İnici Seyir" to Color(0xFFE040FB)
                    }

                    InfoChip(
                        label = "Seyir",
                        value = seyirText,
                        color = seyirColor,
                        modifier = Modifier.weight(1f)
                    )

                    // Durak (Karar) Rozeti
                    InfoChip(
                        label = "Durak",
                        value = detectionResult.detectedDurak.uppercase(),
                        color = Color(0xFF00E676),
                        modifier = Modifier.weight(1f)
                    )

                    // Güçlü Rozeti
                    InfoChip(
                        label = "Güçlü",
                        value = detectionResult.detectedGuclu.uppercase(),
                        color = Color(0xFF448AFF),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Mini Perde Süre Histogramı
                if (detectionResult.pitchHistogram.isNotEmpty()) {
                    MiniPitchHistogramView(
                        histogram = detectionResult.pitchHistogram,
                        durak = detectionResult.detectedDurak,
                        guclu = detectionResult.detectedGuclu
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF222222),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MiniPitchHistogramView(
    histogram: Map<String, Double>,
    durak: String,
    guclu: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF141414))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Perde Süre Yoğunluğu (Son 20-30 Sn)",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "■ Durak  ■ Güçlü",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // En çok tınlayan ilk 8 perdeyi al
        val sortedEntries = histogram.entries
            .filter { it.value > 0.01 }
            .sortedByDescending { it.value }
            .take(8)

        if (sortedEntries.isNotEmpty()) {
            val maxWeight = sortedEntries.maxOf { it.value }.coerceAtLeast(0.01)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                for ((perde, weight) in sortedEntries) {
                    val normalizedHeight = (weight / maxWeight).coerceIn(0.1, 1.0).toFloat()
                    val isDurak = perde.equals(durak, ignoreCase = true)
                    val isGuclu = perde.equals(guclu, ignoreCase = true)

                    val barColor = when {
                        isDurak -> Color(0xFF00E676)
                        isGuclu -> Color(0xFF448AFF)
                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .height((34 * normalizedHeight).dp)
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(barColor, barColor.copy(alpha = 0.5f))
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = perde.take(5),
                            fontSize = 8.sp,
                            fontWeight = if (isDurak || isGuclu) FontWeight.Bold else FontWeight.Normal,
                            color = if (isDurak || isGuclu) Color.White else MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
