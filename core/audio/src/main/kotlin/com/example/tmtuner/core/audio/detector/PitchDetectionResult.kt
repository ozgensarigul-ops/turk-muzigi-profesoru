package com.example.tmtuner.core.audio.detector

/**
 * MPM/NSDF perde algılama sonucu.
 *
 * @param frequency Algılanan temel frekans (Hz). Sinyal perdesiz ise 0.0 döner.
 * @param clarity Perde berraklık / güven katsayısı (0.0 .. 1.0).
 * @param isPitched Sinyalin belirgin bir perdeye sahip olup olmadığı.
 * @param rmsEnergy Sinyalin RMS enerji genliği.
 */
data class PitchDetectionResult(
    val frequency: Double,
    val clarity: Double,
    val isPitched: Boolean,
    val rmsEnergy: Double
) {
    companion object {
        val UNPITCHED = PitchDetectionResult(
            frequency = 0.0,
            clarity = 0.0,
            isPitched = false,
            rmsEnergy = 0.0
        )
    }
}
