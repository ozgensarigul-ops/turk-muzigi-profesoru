package com.example.tmtuner.core.audio.engine

import com.example.tmtuner.core.audio.detector.PitchDetector
import com.example.tmtuner.core.audio.model.SegahNuanceMode
import com.example.tmtuner.core.audio.model.TunerPitchResult
import com.example.tmtuner.core.audio.model.TuningDirection
import com.example.tmtuner.core.musicology.atlas.AeuScaleAtlas
import com.example.tmtuner.core.musicology.model.Ahenk
import com.example.tmtuner.core.musicology.model.PerdeNote
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow

/**
 * İsmail Hakkı Özkan "Türk Mûsikîsi Nazariyatı ve Usûlleri" (s. 74-77)
 * 53-EDO mikrotonal koma cetveline ve ΔKoma formülüne dayanan tuner analiz motoru.
 *
 * Temel Matematiksel Formül:
 * ΔKoma = 53 * log2(f_detected / f_target)
 * ΔCents = ΔKoma * (1200 / 53) = 1200 * log2(f_detected / f_target)
 *
 * İsmail Hakkı Özkan s. 51 gereği Segâh perdesinde -1.0 / -2.0 koma icra toleransını tam destekler.
 */
class MicrotonalTunerEngine(
    val pitchDetector: PitchDetector = PitchDetector(),
    val defaultInTuneToleranceKoma: Double = 0.5
) {

    /**
     * İki frekans arasındaki 53-EDO koma farkını hesaplar:
     * ΔKoma = 53 * log2(f_detected / f_target)
     */
    fun calculateDeltaKoma(detectedFrequency: Double, targetFrequency: Double): Double {
        if (detectedFrequency <= 0.0 || targetFrequency <= 0.0) return 0.0
        return 53.0 * log2(detectedFrequency / targetFrequency)
    }

    /**
     * İki frekans arasındaki sent (cent) farkını hesaplar:
     * ΔCents = 1200 * log2(f_detected / f_target)
     */
    fun calculateDeltaCents(detectedFrequency: Double, targetFrequency: Double): Double {
        if (detectedFrequency <= 0.0 || targetFrequency <= 0.0) return 0.0
        return 1200.0 * log2(detectedFrequency / targetFrequency)
    }

    /**
     * Belirtilen koma ofsetine göre frekansı kaydırır:
     * f_shifted = baseFrequency * 2^(komaOffset / 53)
     */
    fun shiftFrequencyByKoma(baseFrequency: Double, komaOffset: Double): Double {
        return baseFrequency * 2.0.pow(komaOffset / 53.0)
    }

    /**
     * Doğrudan tespit edilmiş bir frekansı (Hz) 53-EDO perde atlası üzerinde analiz eder.
     *
     * @param detectedFrequency Algılanan frekans (Hz).
     * @param clarity MPM berraklık skoru (0.0 .. 1.0).
     * @param ahenk İcra ahengi (MANSUR, BOLAHENK, KIZ vb.).
     * @param segahMode Segâh icra toleransı modu (NONE, PEST_1_KOMA, PEST_2_KOMA, TOLERANT_RANGE).
     * @param inTuneToleranceKoma Akortlu kabul edilme koma toleransı (varsayılan 0.5 koma).
     */
    fun analyzePitch(
        detectedFrequency: Double,
        clarity: Double = 1.0,
        ahenk: Ahenk = Ahenk.MANSUR,
        usePhysicalMansur: Boolean = false,
        segahMode: SegahNuanceMode = SegahNuanceMode.NONE,
        inTuneToleranceKoma: Double = defaultInTuneToleranceKoma
    ): TunerPitchResult? {
        if (detectedFrequency < 20.0 || detectedFrequency > 5000.0) return null

        val atlas = AeuScaleAtlas.buildPitchAtlas(ahenk, usePhysicalMansur)
        if (atlas.isEmpty()) return null

        var bestNote: PerdeNote? = null
        var minAbsKomaDiff = Double.MAX_VALUE
        var bestEffectiveTargetFreq = 0.0
        var bestKomaDiff = 0.0
        var isSegahApplied = false

        for (note in atlas) {
            val isSegah = isSegahPerde(note.name)

            // Özkan s. 51 gereği Segâh nüans frekansı
            val effectiveTarget = if (isSegah && segahMode != SegahNuanceMode.NONE) {
                shiftFrequencyByKoma(note.frequency, segahMode.komaOffset)
            } else {
                note.frequency
            }

            val komaDiff = calculateDeltaKoma(detectedFrequency, effectiveTarget)
            val absDiff = abs(komaDiff)

            if (absDiff < minAbsKomaDiff) {
                minAbsKomaDiff = absDiff
                bestNote = note
                bestEffectiveTargetFreq = effectiveTarget
                bestKomaDiff = komaDiff
                if (isSegah && segahMode != SegahNuanceMode.NONE) {
                    isSegahApplied = true
                }
            }
        }

        val note = bestNote ?: return null
        val isSegah = isSegahPerde(note.name)

        // Segâh TOLERANT_RANGE modu kontrolü
        val (inTune, finalKomaDiff, segahToleranceActive) = if (isSegah && segahMode == SegahNuanceMode.TOLERANT_RANGE) {
            val rawDiffFromTheoretical = calculateDeltaKoma(detectedFrequency, note.frequency)
            // -2.0 ile 0.0 koma aralığı ve ±tolerans dahilinde akortlu kabul edilir
            val withinTolerantRange = rawDiffFromTheoretical >= (-2.0 - inTuneToleranceKoma) &&
                    rawDiffFromTheoretical <= (0.0 + inTuneToleranceKoma)
            Triple(withinTolerantRange, rawDiffFromTheoretical, true)
        } else {
            val tuned = minAbsKomaDiff <= inTuneToleranceKoma
            Triple(tuned, bestKomaDiff, isSegahApplied)
        }

        val centsDiff = finalKomaDiff * (1200.0 / 53.0)
        val direction = TuningDirection.fromKomaDifference(finalKomaDiff, inTuneToleranceKoma)

        return TunerPitchResult(
            detectedFrequency = detectedFrequency,
            targetFrequency = bestEffectiveTargetFreq,
            perdeName = note.name,
            mutlakKoma = note.mutlakKoma,
            oktavKomaMod = note.oktavKomaMod,
            komaDifference = finalKomaDiff,
            centsDifference = centsDiff,
            isTuned = inTune,
            clarity = clarity,
            ahenk = ahenk,
            isSegahToleranceApplied = segahToleranceActive,
            direction = direction,
            octaveIndex = note.octaveIndex,
            octaveName = note.octaveName
        )
    }

    /**
     * FloatArray ses tamponunu MPM ile analiz edip 53-EDO mikrotonal akort sonucunu döner.
     */
    fun analyzeAudioBuffer(
        buffer: FloatArray,
        sampleRate: Int,
        ahenk: Ahenk = Ahenk.MANSUR,
        usePhysicalMansur: Boolean = false,
        segahMode: SegahNuanceMode = SegahNuanceMode.NONE,
        inTuneToleranceKoma: Double = defaultInTuneToleranceKoma
    ): TunerPitchResult? {
        val detection = pitchDetector.detectPitch(buffer, sampleRate)
        if (!detection.isPitched || detection.frequency <= 0.0) return null
        return analyzePitch(
            detectedFrequency = detection.frequency,
            clarity = detection.clarity,
            ahenk = ahenk,
            usePhysicalMansur = usePhysicalMansur,
            segahMode = segahMode,
            inTuneToleranceKoma = inTuneToleranceKoma
        )
    }

    /**
     * DoubleArray ses tamponunu MPM ile analiz edip 53-EDO mikrotonal akort sonucunu döner.
     */
    fun analyzeAudioBuffer(
        buffer: DoubleArray,
        sampleRate: Int,
        ahenk: Ahenk = Ahenk.MANSUR,
        usePhysicalMansur: Boolean = false,
        segahMode: SegahNuanceMode = SegahNuanceMode.NONE,
        inTuneToleranceKoma: Double = defaultInTuneToleranceKoma
    ): TunerPitchResult? {
        val detection = pitchDetector.detectPitch(buffer, sampleRate)
        if (!detection.isPitched || detection.frequency <= 0.0) return null
        return analyzePitch(
            detectedFrequency = detection.frequency,
            clarity = detection.clarity,
            ahenk = ahenk,
            usePhysicalMansur = usePhysicalMansur,
            segahMode = segahMode,
            inTuneToleranceKoma = inTuneToleranceKoma
        )
    }

    /**
     * ShortArray (16-bit PCM) ses tamponunu MPM ile analiz edip 53-EDO mikrotonal akort sonucunu döner.
     */
    fun analyzeAudioBuffer(
        buffer: ShortArray,
        sampleRate: Int,
        ahenk: Ahenk = Ahenk.MANSUR,
        usePhysicalMansur: Boolean = false,
        segahMode: SegahNuanceMode = SegahNuanceMode.NONE,
        inTuneToleranceKoma: Double = defaultInTuneToleranceKoma
    ): TunerPitchResult? {
        val detection = pitchDetector.detectPitch(buffer, sampleRate)
        if (!detection.isPitched || detection.frequency <= 0.0) return null
        return analyzePitch(
            detectedFrequency = detection.frequency,
            clarity = detection.clarity,
            ahenk = ahenk,
            usePhysicalMansur = usePhysicalMansur,
            segahMode = segahMode,
            inTuneToleranceKoma = inTuneToleranceKoma
        )
    }

    private fun isSegahPerde(name: String): Boolean {
        return name.contains("Segâh", ignoreCase = true) || name.contains("Segah", ignoreCase = true)
    }
}
