package com.example.tmtuner.data.ml

import com.example.tmtuner.data.models.SymbTrMakam
import kotlin.math.sqrt

data class MakamRecognitionResult(
    val makamAdi: String,
    val confidence: Float,
    val tespitEdilenCesni: String
)

object MakamClassifier {

    fun classifyMakam(
        inputHistogram: FloatArray,
        trainingVectors: List<MakamPitchDistributionVector>
    ): MakamRecognitionResult {
        if (inputHistogram.size != 53 || trainingVectors.isEmpty()) {
            return MakamRecognitionResult("Bilinmeyen", 0.0f, "Çeşni Yok")
        }

        var inSumSquares = 0.0f
        for (v in inputHistogram) inSumSquares += v * v
        val inNorm = sqrt(inSumSquares)
        val normalizedInput = FloatArray(53)
        if (inNorm > 0.0f) {
            for (i in 0 until 53) normalizedInput[i] = inputHistogram[i] / inNorm
        }

        var bestMatch: MakamPitchDistributionVector? = null
        var maxScore = -1.0f

        for (target in trainingVectors) {
            var dotProduct = 0.0f
            for (i in 0 until 53) {
                dotProduct += normalizedInput[i] * target.komaHistogram[i]
            }
            if (dotProduct > maxScore) {
                maxScore = dotProduct
                bestMatch = target
            }
        }

        val detectedMakam = bestMatch?.makamAdi ?: "Bilinmeyen"
        val detectedCesni = when {
            detectedMakam.contains("Rast", ignoreCase = true) -> "Râst Beşlisi (T-K-S-T)"
            detectedMakam.contains("Uşşak", ignoreCase = true) -> "Uşşâk Dörtlüsü (K-S-T)"
            detectedMakam.contains("Buselik", ignoreCase = true) -> "Bûselik Beşlisi (T-B-T-T)"
            detectedMakam.contains("Hicaz", ignoreCase = true) -> "Hicaz Dörtlüsü (S-A12-S)"
            detectedMakam.contains("Hüseyni", ignoreCase = true) -> "Hüseynî Beşlisi (K-S-T-T)"
            else -> "Genel Makam Dizisi"
        }

        return MakamRecognitionResult(
            makamAdi = detectedMakam,
            confidence = maxScore.coerceIn(0.0f, 1.0f),
            tespitEdilenCesni = detectedCesni
        )
    }
}
