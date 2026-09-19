package com.example.tmtuner.core.musicology.analysis

import com.example.tmtuner.core.musicology.model.Cesni
import kotlin.math.sqrt

/**
 * Makam sınıflandırma referans ağırlık vektörü.
 */
data class MakamDistributionVector(
    val makamId: String,
    val makamName: String,
    val komaHistogram: FloatArray, // 53 elemanlı normalize dağılım
    val durakKoma: Int,
    val gucluKoma: Int,
    val segahNuanceToleranceApplied: Boolean = false
)

/**
 * Makam sınıflandırma ve seyir tespiti sonucu.
 */
data class MakamClassificationResult(
    val makamName: String,
    val confidence: Float,
    val detectedCesni: String,
    val segahNuanceOffsetApplied: Int = 0
)

/**
 * 53-EDO Koma histogramı üzerinden Kosinüs Benzerliği (Cosine Similarity)
 * ve çeşni kural tabanı ile canlı makam tespit motoru.
 * Uşşak ve Segâh çeşnilerinde Segâh perdesinin geleneksel -1 ve -2 komalık
 * pest icra toleransını tam olarak destekler.
 */
object MakamClassifierEngine {

    /** Segâh perdesinin 53-EDO dizisindeki nominal koma indeksi (Rast=0 kabul edildiğinde) */
    const val SEGAH_KOMA_INDEX = 17

    /**
     * Verilen girdi koma histogramını referans makam vektörleri ile karşılaştırır.
     *
     * @param inputHistogram 53 komalık girdi frekans dağılımı.
     * @param referenceVectors Karşılaştırma yapılacak referans makam vektörleri.
     * @param segahNuanceOffset İcracının Segâh perdesinde uyguladığı geleneksel pestleşme ofseti (0, -1 veya -2 koma).
     */
    fun classify(
        inputHistogram: FloatArray,
        referenceVectors: List<MakamDistributionVector>,
        segahNuanceOffset: Int = 0
    ): MakamClassificationResult {
        if (inputHistogram.size != 53 || referenceVectors.isEmpty()) {
            return MakamClassificationResult("Bilinmeyen", 0.0f, "Çeşni Yok", segahNuanceOffset)
        }

        // Girdi histogramını hazırla (gerekirse Segâh tolerans ofsetini harmanla)
        val workingInput = inputHistogram.clone()
        if (segahNuanceOffset != 0) {
            val targetNuanceBin = (SEGAH_KOMA_INDEX + segahNuanceOffset + 53) % 53
            // Pestleşmiş Segâh enerjisini nominal hücreye de yansıtarak benzerlik skorunu destekle
            val nuanceEnergy = workingInput[targetNuanceBin]
            if (nuanceEnergy > 0f) {
                workingInput[SEGAH_KOMA_INDEX] += nuanceEnergy * 0.8f
            }
        }

        // Girdi histogramını L2 normuna göre normalize et
        var inSumSquares = 0.0f
        for (v in workingInput) inSumSquares += v * v
        val inNorm = sqrt(inSumSquares)

        val normalizedInput = FloatArray(53)
        if (inNorm > 0.0f) {
            for (i in 0 until 53) normalizedInput[i] = workingInput[i] / inNorm
        }

        var bestMatch: MakamDistributionVector? = null
        var maxScore = -1.0f

        for (target in referenceVectors) {
            var dotProduct = 0.0f
            for (i in 0 until 53) {
                dotProduct += normalizedInput[i] * target.komaHistogram[i]
            }
            if (dotProduct > maxScore) {
                maxScore = dotProduct
                bestMatch = target
            }
        }

        val detectedMakam = bestMatch?.makamName ?: "Bilinmeyen"
        val detectedCesni = resolveCesniForMakam(detectedMakam)

        return MakamClassificationResult(
            makamName = detectedMakam,
            confidence = maxScore.coerceIn(0.0f, 1.0f),
            detectedCesni = detectedCesni,
            segahNuanceOffsetApplied = segahNuanceOffset
        )
    }

    /**
     * Tespit edilen makama karşılık gelen çeşni tanımlamasını döner.
     */
    fun resolveCesniForMakam(makamName: String): String {
        return when {
            makamName.contains("Rast", ignoreCase = true) -> "${Cesni.RAST_BESLISI.name} (${Cesni.RAST_BESLISI.formulaString})"
            makamName.contains("Uşşak", ignoreCase = true) || makamName.contains("Ussak", ignoreCase = true) -> "${Cesni.USSAK_DORTLUSU.name} (${Cesni.USSAK_DORTLUSU.formulaString})"
            makamName.contains("Segah", ignoreCase = true) || makamName.contains("Segâh", ignoreCase = true) -> "${Cesni.SEGAH_BESLISI.name} (${Cesni.SEGAH_BESLISI.formulaString})"
            makamName.contains("Buselik", ignoreCase = true) || makamName.contains("Bûselik", ignoreCase = true) -> "${Cesni.BUSELIK_BESLISI.name} (${Cesni.BUSELIK_BESLISI.formulaString})"
            makamName.contains("Hicaz", ignoreCase = true) -> "${Cesni.HICAZ_DORTLUSU.name} (${Cesni.HICAZ_DORTLUSU.formulaString})"
            makamName.contains("Hüseyni", ignoreCase = true) || makamName.contains("Huseyni", ignoreCase = true) -> "${Cesni.HUSEYNI_BESLISI.name} (${Cesni.HUSEYNI_BESLISI.formulaString})"
            makamName.contains("Çârgâh", ignoreCase = true) || makamName.contains("Cargah", ignoreCase = true) -> "${Cesni.CARGAH_BESLISI.name} (${Cesni.CARGAH_BESLISI.formulaString})"
            makamName.contains("Kürdî", ignoreCase = true) || makamName.contains("Kurdi", ignoreCase = true) -> "${Cesni.KURDI_DORTLUSU.name} (${Cesni.KURDI_DORTLUSU.formulaString})"
            else -> "Genel Makam Dizisi"
        }
    }

    /**
     * Temel makamlar için varsayılan referans dağılım vektörlerini üretir.
     * Uşşak ve Segâh çeşnileri için Segâh perdesinde -1 ve -2 komalık pest icra toleransını uygular.
     */
    fun createDefaultReferenceVectors(): List<MakamDistributionVector> {
        return listOf(
            createVector("rast", "Rast", 0, 9, intArrayOf(0, 9, 17, 22, 31, 40, 48, 53)),
            // Uşşak: Dügâh (9), Segâh (17; -1 ve -2 pest toleranslı: 16 ve 15), Çârgâh (22), Nevâ (31)
            createVectorWithNuance("ussak", "Uşşak", 9, 31, intArrayOf(9, 17, 22, 31, 40, 44, 53, 62), applySegahNuance = true),
            // Segâh: Durak Segâh (17; -1 ve -2 pest toleranslı: 16 ve 15), Güçlü Nevâ (31)
            createVectorWithNuance("segah", "Segâh", 17, 31, intArrayOf(17, 22, 31, 40, 48, 53, 62), applySegahNuance = true),
            createVector("buselik", "Buselik", 9, 31, intArrayOf(9, 18, 22, 31, 40, 44, 53, 62)),
            createVector("hicaz", "Hicaz", 9, 31, intArrayOf(9, 14, 26, 31, 40, 44, 53, 62)),
            createVector("huseyni", "Hüseyni", 9, 40, intArrayOf(9, 17, 22, 31, 40, 48, 53, 62))
        )
    }

    private fun createVector(
        id: String,
        name: String,
        durak: Int,
        guclu: Int,
        komaIndices: IntArray
    ): MakamDistributionVector {
        return createVectorWithNuance(id, name, durak, guclu, komaIndices, applySegahNuance = false)
    }

    private fun createVectorWithNuance(
        id: String,
        name: String,
        durak: Int,
        guclu: Int,
        komaIndices: IntArray,
        applySegahNuance: Boolean
    ): MakamDistributionVector {
        val hist = FloatArray(53)
        for (idx in komaIndices) {
            hist[idx % 53] += 1.0f
        }
        hist[durak % 53] += 3.0f // Durak perdesine yüksek ağırlık
        hist[guclu % 53] += 2.0f // Güçlü perdesine ağırlık

        if (applySegahNuance) {
            // Segâh perdesi (17. koma) için -1 koma (16) ve -2 koma (15) geleneksel pest icra toleransı ağırlıkları
            hist[(SEGAH_KOMA_INDEX - 1 + 53) % 53] += 1.8f
            hist[(SEGAH_KOMA_INDEX - 2 + 53) % 53] += 1.2f
        }

        var sumSquares = 0.0f
        for (v in hist) sumSquares += v * v
        val norm = sqrt(sumSquares)
        if (norm > 0f) {
            for (i in hist.indices) hist[i] /= norm
        }

        return MakamDistributionVector(
            makamId = id,
            makamName = name,
            komaHistogram = hist,
            durakKoma = durak,
            gucluKoma = guclu,
            segahNuanceToleranceApplied = applySegahNuance
        )
    }
}
