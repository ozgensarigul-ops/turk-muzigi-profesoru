package com.example.tmtuner.ui.components

import android.graphics.Paint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * 53-EDO Türk Müziği -9 ile +9 koma aralığını gösteren mikrotonal akort kadranı (Gauge).
 *
 * İsmail Hakkı Özkan nazariyatına göre:
 * - 0 Koma: Tam akort (In Tune)
 * - ±1 Koma: Koma aralığı (~22.6 cent)
 * - ±4 Koma: Bakiyye aralığı (~90.5 cent)
 * - ±9 Koma: Tanini tam ses aralığı (~204 cent)
 * - Segâh İcra Tavrı aktifken (-1 ile -2 koma toleransı): İbre Kehribar (Amber - #FFB300) rengine döner.
 */
@Composable
fun KomaGaugeDial(
    komaOffset: Double,
    centsOffset: Double,
    isInTune: Boolean,
    isSegahNuanceActive: Boolean,
    modifier: Modifier = Modifier
) {
    val clampedKoma = komaOffset.coerceIn(-9.0, 9.0).toFloat()
    val animatedKoma by animateFloatAsState(
        targetValue = clampedKoma,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "needleKomaAnimation"
    )

    // Needle and status colors
    val targetNeedleColor = when {
        isSegahNuanceActive -> Color(0xFFFFB300) // Kehribar (Amber)
        isInTune -> Color(0xFF4CAF50)          // Yeşil (In Tune)
        animatedKoma < -0.3f -> Color(0xFF29B6F6) // Pes (Mavi / Cyan)
        animatedKoma > 0.3f -> Color(0xFFFF7043)  // Tiz (Turuncu / Kırmızı)
        else -> Color(0xFF4CAF50)
    }

    val needleColor by animateColorAsState(
        targetValue = targetNeedleColor,
        animationSpec = tween(durationMillis = 250),
        label = "needleColorAnimation"
    )

    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .padding(horizontal = 16.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val centerX = canvasWidth / 2f
                val centerY = canvasHeight * 0.82f
                val radius = minOf(canvasWidth * 0.44f, canvasHeight * 0.72f)

                // 1. Draw Background Arc (180° to 360°, semi-circle)
                val strokeWidth = 14.dp.toPx()
                drawArc(
                    color = surfaceVariantColor.copy(alpha = 0.5f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // 2. Draw In-Tune Center Zone (around 0 koma: 270° +/- 3°)
                val inTuneAngleSpan = 6f
                drawArc(
                    color = Color(0xFF4CAF50).copy(alpha = 0.4f),
                    startAngle = 270f - (inTuneAngleSpan / 2f),
                    sweepAngle = inTuneAngleSpan,
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )

                // 3. Draw Segâh Nuance Zone (-1 to -2 koma) on the arc
                // Angle for -1 koma: 270 - 10 = 260°
                // Angle for -2 koma: 270 - 20 = 250°
                val segahZoneStartAngle = 250f
                val segahZoneSweepAngle = 10f
                drawArc(
                    color = Color(0xFFFFB300).copy(alpha = if (isSegahNuanceActive) 0.85f else 0.25f),
                    startAngle = segahZoneStartAngle,
                    sweepAngle = segahZoneSweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokeWidth + 2.dp.toPx(), cap = StrokeCap.Butt)
                )

                // 4. Draw Ticks and Labels for -9 to +9 Koma
                val textPaint = Paint().apply {
                    color = onSurfaceColor.toArgb()
                    textSize = 11.sp.toPx()
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }

                val highlightTextPaint = Paint().apply {
                    color = Color(0xFFFFB300).toArgb()
                    textSize = 10.sp.toPx()
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }

                for (koma in -9..9) {
                    val angleDeg = 270f + (koma / 9f) * 90f
                    val angleRad = Math.toRadians(angleDeg.toDouble())

                    val isMajor = (koma == 0 || Math.abs(koma) == 1 || Math.abs(koma) == 4 || Math.abs(koma) == 9)
                    val tickLen = when {
                        koma == 0 -> 20.dp.toPx()
                        isMajor -> 15.dp.toPx()
                        else -> 8.dp.toPx()
                    }
                    val tickStroke = when {
                        koma == 0 -> 3.5.dp.toPx()
                        isMajor -> 2.2.dp.toPx()
                        else -> 1.2.dp.toPx()
                    }
                    val tickColor = when {
                        koma == 0 -> Color(0xFF4CAF50)
                        koma in -2..-1 -> Color(0xFFFFB300)
                        isMajor -> onSurfaceColor.copy(alpha = 0.85f)
                        else -> outlineColor.copy(alpha = 0.45f)
                    }

                    val innerRadius = radius - strokeWidth / 2f - 2.dp.toPx()
                    val outerRadius = innerRadius - tickLen

                    val startX = centerX + (innerRadius * cos(angleRad)).toFloat()
                    val startY = centerY + (innerRadius * sin(angleRad)).toFloat()
                    val endX = centerX + (outerRadius * cos(angleRad)).toFloat()
                    val endY = centerY + (outerRadius * sin(angleRad)).toFloat()

                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = tickStroke,
                        cap = StrokeCap.Round
                    )

                    // Draw labels for key points
                    if (isMajor) {
                        val labelRadius = outerRadius - 13.dp.toPx()
                        val labelX = centerX + (labelRadius * cos(angleRad)).toFloat()
                        val labelY = centerY + (labelRadius * sin(angleRad)).toFloat() + 4.dp.toPx()

                        val label = when (koma) {
                            0 -> "0"
                            -1 -> "-1k"
                            1 -> "+1k"
                            -4 -> "-B"
                            4 -> "+B"
                            -9 -> "-T"
                            9 -> "+T"
                            else -> "$koma"
                        }

                        val paint = if (koma in -2..-1) highlightTextPaint else textPaint
                        drawContext.canvas.nativeCanvas.drawText(label, labelX, labelY, paint)
                    }
                }

                // 5. Draw Animated Needle
                val needleAngleDeg = 270f + (animatedKoma / 9f) * 90f
                val needleAngleRad = Math.toRadians(needleAngleDeg.toDouble())
                val needleLength = radius - 8.dp.toPx()

                val needleEndX = centerX + (needleLength * cos(needleAngleRad)).toFloat()
                val needleEndY = centerY + (needleLength * sin(needleAngleRad)).toFloat()

                // Needle line
                drawLine(
                    color = needleColor,
                    start = Offset(centerX, centerY),
                    end = Offset(needleEndX, needleEndY),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Pivot circle at needle base
                drawCircle(
                    color = needleColor,
                    radius = 9.dp.toPx(),
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = surfaceVariantColor,
                    radius = 4.dp.toPx(),
                    center = Offset(centerX, centerY)
                )
            }
        }

        // Digital Koma and Cent Display
        val sign = if (komaOffset > 0) "+" else ""
        val formattedKoma = String.format(Locale.US, "%s%.1f Koma", sign, komaOffset)
        val formattedCents = String.format(Locale.US, "(%s%.1f cent)", sign, centsOffset)

        Text(
            text = formattedKoma,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = needleColor
        )

        Text(
            text = formattedCents,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
