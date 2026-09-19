package com.example.tmtuner.core.musicology.analysis

import com.example.tmtuner.core.musicology.math.Edo53Calculator
import kotlin.math.sqrt

/**
 * 53 Eşit Bölümlü (53-EDO) Koma Frekans Dağılım Histogramı (Pitch Class Profile).
 * İcra edilen ezginin 53 komalık döngüde hangi perdelerde ne kadar süre kaldığını biriktirir.
 */
class PitchHistogram {

    private val bins = FloatArray(53)
    private var sampleCount = 0

    /**
     * Algılanan frekansı ilgili 53-EDO koma hücresine ekler.
     *
     * @param frequency Algılanan perde frekansı (Hz).
     * @param referenceCargah Kaba Çârgâh referans frekansı (Mansur için ~195.55 Hz).
     * @param weight Hücreye eklenecek ağırlık (varsayılan 1.0f).
     */
    fun addPitch(frequency: Double, referenceCargah: Double, weight: Float = 1.0f) {
        if (frequency <= 20.0 || referenceCargah <= 0.0) return
        val index = Edo53Calculator.komaIndex(frequency, referenceCargah)
        bins[index] += weight
        sampleCount++
    }

    /**
     * Histogramın L2 (Öklid) normuna göre normalize edilmiş kopyasını döner.
     */
    fun getNormalizedHistogram(): FloatArray {
        var sumSquares = 0.0f
        for (v in bins) sumSquares += v * v
        val norm = sqrt(sumSquares)

        val normalized = FloatArray(53)
        if (norm > 0.0f) {
            for (i in 0 until 53) {
                normalized[i] = bins[i] / norm
            }
        }
        return normalized
    }

    /**
     * Ham koma hücrelerini döner.
     */
    fun getRawBins(): FloatArray = bins.clone()

    /**
     * Biriken örnek sayısını döner.
     */
    fun getSampleCount(): Int = sampleCount

    /**
     * Histogramı sıfırlar.
     */
    fun reset() {
        for (i in 0 until 53) bins[i] = 0.0f
        sampleCount = 0
    }
}
