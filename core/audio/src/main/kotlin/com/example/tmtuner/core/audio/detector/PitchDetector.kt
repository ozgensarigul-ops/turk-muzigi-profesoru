package com.example.tmtuner.core.audio.detector

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * McLeod Pitch Method (MPM) ve Normalized Square Difference Function (NSDF)
 * algoritmasına dayalı yüksek hassasiyetli mikrotonal perde algılayıcı.
 *
 * Philip McLeod & Geoff Wyvill (2005) kuramı temel alınmıştır:
 * 1. NSDF fonksiyonu hesabı: r(τ) = 2 * c(τ) / m(τ)
 * 2. Sıfır geçişleri ve yerel pozitif tepe (peak) tespiti
 * 3. Oktav sıçramalarını önleyen dinamik eşikli ilk tepe seçimi (smallest period)
 * 4. Alt-örnek (sub-sample) hassasiyetinde 3-nokta parabolik interpolasyon
 *
 * 44100 Hz ve 48000 Hz örnekleme frekanslarında Float, Double ve Short (PCM-16) tamponları destekler.
 */
class PitchDetector(
    val minFrequency: Double = 50.0,
    val maxFrequency: Double = 2000.0,
    val clarityThreshold: Double = 0.70,
    val peakCutoffRatio: Double = 0.90,
    val silenceThreshold: Double = 0.005
) {

    /**
     * FloatArray ses tamponundan temel frekansı algılar.
     */
    fun detectPitch(buffer: FloatArray, sampleRate: Int): PitchDetectionResult {
        val doubleBuffer = DoubleArray(buffer.size) { buffer[it].toDouble() }
        return detectPitch(doubleBuffer, sampleRate)
    }

    /**
     * ShortArray (16-bit PCM) ses tamponundan temel frekansı algılar.
     */
    fun detectPitch(buffer: ShortArray, sampleRate: Int): PitchDetectionResult {
        val doubleBuffer = DoubleArray(buffer.size) { buffer[it].toDouble() / 32768.0 }
        return detectPitch(doubleBuffer, sampleRate)
    }

    /**
     * DoubleArray ses tamponundan MPM/NSDF algoritması ile temel frekansı algılar.
     */
    fun detectPitch(buffer: DoubleArray, sampleRate: Int): PitchDetectionResult {
        val n = buffer.size
        if (n < 64 || sampleRate <= 0) return PitchDetectionResult.UNPITCHED

        // 1. RMS Enerji kontrolü (Sessizlik filtresi)
        var sumSquares = 0.0
        for (sample in buffer) {
            sumSquares += sample * sample
        }
        val rms = sqrt(sumSquares / n)
        if (rms < silenceThreshold) {
            return PitchDetectionResult(
                frequency = 0.0,
                clarity = 0.0,
                isPitched = false,
                rmsEnergy = rms
            )
        }

        // 2. Minimum ve maksimum lag aralıkları
        val minLag = max(1, (sampleRate / maxFrequency).toInt())
        val maxLag = min(n - 2, (sampleRate / minFrequency).toInt())
        if (minLag >= maxLag) return PitchDetectionResult.UNPITCHED

        // 3. NSDF (Normalized Square Difference Function) Hesaplama
        // r(τ) = 2 * sum(x[i] * x[i+τ]) / sum(x[i]^2 + x[i+τ]^2)
        val nsdf = DoubleArray(maxLag + 2)
        for (tau in 0..maxLag + 1) {
            var crossCorrelation = 0.0
            var energy = 0.0
            val len = n - tau
            for (i in 0 until len) {
                val a = buffer[i]
                val b = buffer[i + tau]
                crossCorrelation += a * b
                energy += a * a + b * b
            }
            nsdf[tau] = if (energy > 0.0) (2.0 * crossCorrelation) / energy else 0.0
        }

        // 4. Sıfır Geçişleri ve Pozitif Tepe (Peak) Tespiti
        val peakIndices = ArrayList<Int>()
        var inPositiveRegion = false
        var curMaxTau = -1
        var curMaxVal = Double.NEGATIVE_INFINITY

        for (tau in minLag..maxLag) {
            val v = nsdf[tau]
            if (v > 0.0) {
                inPositiveRegion = true
                if (v > curMaxVal) {
                    curMaxVal = v
                    curMaxTau = tau
                }
            } else {
                if (inPositiveRegion && curMaxTau != -1) {
                    // Yerel maksimumun tepe olup olmadığını kontrol et
                    if (isLocalPeak(nsdf, curMaxTau)) {
                        peakIndices.add(curMaxTau)
                    }
                    curMaxTau = -1
                    curMaxVal = Double.NEGATIVE_INFINITY
                }
                inPositiveRegion = false
            }
        }
        if (inPositiveRegion && curMaxTau != -1 && isLocalPeak(nsdf, curMaxTau)) {
            peakIndices.add(curMaxTau)
        }

        if (peakIndices.isEmpty()) {
            return PitchDetectionResult(
                frequency = 0.0,
                clarity = 0.0,
                isPitched = false,
                rmsEnergy = rms
            )
        }

        // 5. Global Maksimum ve Eşik Belirleme
        var highestPeakVal = Double.NEGATIVE_INFINITY
        for (peakTau in peakIndices) {
            val v = nsdf[peakTau]
            if (v > highestPeakVal) {
                highestPeakVal = v
            }
        }

        if (highestPeakVal < clarityThreshold) {
            return PitchDetectionResult(
                frequency = 0.0,
                clarity = highestPeakVal.coerceIn(0.0, 1.0),
                isPitched = false,
                rmsEnergy = rms
            )
        }

        // 6. En Küçük Periyot (Smallest Period) Seçimi - Oktav Sıçramalarını Önler
        val cutoff = highestPeakVal * peakCutoffRatio
        var selectedTau = peakIndices[0]
        for (peakTau in peakIndices) {
            if (nsdf[peakTau] >= cutoff) {
                selectedTau = peakTau
                break
            }
        }

        // 7. Parabolik İnterpolasyon (Sub-sample Peak Refinement)
        val alpha = nsdf[selectedTau - 1]
        val beta = nsdf[selectedTau]
        val gamma = nsdf[selectedTau + 1]

        val denominator = 2.0 * (2.0 * beta - alpha - gamma)
        val delta = if (abs(denominator) > 1e-12) (gamma - alpha) / denominator else 0.0
        val refinedTau = selectedTau + delta
        val refinedClarity = (beta + 0.5 * delta * (gamma - alpha)).coerceIn(0.0, 1.0)

        if (refinedTau <= 0.0) {
            return PitchDetectionResult.UNPITCHED
        }

        val frequency = sampleRate / refinedTau
        val isPitched = frequency in minFrequency..maxFrequency && refinedClarity >= clarityThreshold

        return PitchDetectionResult(
            frequency = if (isPitched) frequency else 0.0,
            clarity = refinedClarity,
            isPitched = isPitched,
            rmsEnergy = rms
        )
    }

    private fun isLocalPeak(nsdf: DoubleArray, tau: Int): Boolean {
        if (tau <= 0 || tau >= nsdf.size - 1) return false
        return nsdf[tau] > nsdf[tau - 1] && nsdf[tau] >= nsdf[tau + 1]
    }
}
