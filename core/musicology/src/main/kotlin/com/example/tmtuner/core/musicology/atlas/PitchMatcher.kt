package com.example.tmtuner.core.musicology.atlas

import com.example.tmtuner.core.musicology.math.Edo53Calculator
import com.example.tmtuner.core.musicology.math.TuningCalculator
import com.example.tmtuner.core.musicology.model.PerdeNote
import kotlin.math.abs
import kotlin.math.log2

/**
 * Frekans eşleme sonucu.
 */
data class PitchMatchResult(
    val matchedNote: PerdeNote,
    val targetFrequency: Double,
    val centsOffset: Float,
    val komaOffset: Float,
    val gaugePosition: Float,
    val isInTune: Boolean,
    val feedbackText: String,
    val octaveIndex: Int
)

/**
 * Mikrofon veya DSP katmanından gelen ham frekansı,
 * Türk Müziği perde atlası üzerindeki en yakın mikrotonal perdeye eşleyen motor.
 */
object PitchMatcher {

    /**
     * Frekansı en yakın Türk Müziği perdesine eşler.
     *
     * @param frequency Akort edilecek frekans (Hz).
     * @param atlas 72 perdelik AEU perde atlası.
     * @param segahNuanceOffset Segâh icrası için koma pestleşme toleransı (0, -1, -2 koma).
     */
    fun matchPitch(
        frequency: Double,
        atlas: List<PerdeNote>,
        segahNuanceOffset: Int = 0
    ): PitchMatchResult? {
        if (frequency <= 20.0 || atlas.isEmpty()) return null

        var closestNote: PerdeNote? = null
        var minDifference = Double.MAX_VALUE
        var effectiveTargetFreq = 0.0

        for (note in atlas) {
            val noteFreq = if (note.name.contains("Segâh", ignoreCase = true) && segahNuanceOffset != 0) {
                Edo53Calculator.applyNuanceOffset(note.frequency, segahNuanceOffset)
            } else {
                note.frequency
            }

            val diff = abs(noteFreq - frequency)
            if (diff < minDifference) {
                minDifference = diff
                closestNote = note
                effectiveTargetFreq = noteFreq
            }
        }

        val note = closestNote ?: return null

        val centsOffset = (1200.0 * log2(frequency / effectiveTargetFreq)).toFloat()
        val komaOffset = TuningCalculator.centsToKoma(centsOffset)
        val gaugePosition = TuningCalculator.clampToTaniniGauge(komaOffset)
        val inTune = TuningCalculator.isInTune(komaOffset)
        val feedback = TuningCalculator.formatKomaFeedback(komaOffset)

        return PitchMatchResult(
            matchedNote = note,
            targetFrequency = effectiveTargetFreq,
            centsOffset = centsOffset,
            komaOffset = komaOffset,
            gaugePosition = gaugePosition,
            isInTune = inTune,
            feedbackText = feedback,
            octaveIndex = note.octaveIndex
        )
    }
}
