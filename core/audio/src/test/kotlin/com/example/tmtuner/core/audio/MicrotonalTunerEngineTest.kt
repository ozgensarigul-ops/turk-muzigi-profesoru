package com.example.tmtuner.core.audio

import com.example.tmtuner.core.audio.engine.MicrotonalTunerEngine
import com.example.tmtuner.core.audio.model.SegahNuanceMode
import com.example.tmtuner.core.audio.model.TuningDirection
import com.example.tmtuner.core.musicology.model.Ahenk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

class MicrotonalTunerEngineTest {

    private val engine = MicrotonalTunerEngine()
    private val delta = 0.01

    // =========================================================================
    // 1. ΔKoma ve ΔCents Matematiksel Formül Doğrulaması (Özkan s. 74-77)
    // =========================================================================

    @Test
    fun testDeltaKomaFormulaPrecision() {
        val targetFreq = 440.0

        // 1. Sıfır sapma
        assertEquals(0.0, engine.calculateDeltaKoma(440.0, targetFreq), delta)
        assertEquals(0.0, engine.calculateDeltaCents(440.0, targetFreq), delta)

        // 2. Tam 1 koma tiz sapma: f = 440 * 2^(1/53)
        val oneKomaSharp = targetFreq * 2.0.pow(1.0 / 53.0)
        val deltaKoma1 = engine.calculateDeltaKoma(oneKomaSharp, targetFreq)
        assertEquals(1.0, deltaKoma1, delta)
        assertEquals(1200.0 / 53.0, engine.calculateDeltaCents(oneKomaSharp, targetFreq), delta) // ≈ 22.64 cent

        // 3. Tam 1 koma pes sapma: f = 440 * 2^(-1/53)
        val oneKomaFlat = targetFreq * 2.0.pow(-1.0 / 53.0)
        val deltaKomaMinus1 = engine.calculateDeltaKoma(oneKomaFlat, targetFreq)
        assertEquals(-1.0, deltaKomaMinus1, delta)

        // 4. Tanini aralığı (9 koma): f = 440 * 2^(9/53)
        val nineKomaTanini = targetFreq * 2.0.pow(9.0 / 53.0)
        assertEquals(9.0, engine.calculateDeltaKoma(nineKomaTanini, targetFreq), delta)

        // 5. Bakiyye aralığı (4 koma): f = 440 * 2^(4/53)
        val fourKomaBakiyye = targetFreq * 2.0.pow(4.0 / 53.0)
        assertEquals(4.0, engine.calculateDeltaKoma(fourKomaBakiyye, targetFreq), delta)
    }

    // =========================================================================
    // 2. Mansur Dügâh (440.0 Hz) Birim Testi
    // =========================================================================

    @Test
    fun testMansurDugah440HzAnalysis() {
        val detectedFreq = 440.0

        val result = engine.analyzePitch(
            detectedFrequency = detectedFreq,
            clarity = 0.98,
            ahenk = Ahenk.MANSUR
        )

        assertNotNull("Mansur Dügâh sonucu null olmamalı", result)
        assertEquals("Dügâh", result?.perdeName)
        assertEquals(Ahenk.MANSUR, result?.ahenk)
        assertEquals(440.0, result?.targetFrequency ?: 0.0, delta)
        assertEquals(0.0, result?.komaDifference ?: 99.0, delta)
        assertEquals(0.0, result?.centsDifference ?: 99.0, delta)
        assertTrue("Mansur Dügâh tam akortlu olmalı", result?.isTuned == true)
        assertEquals(TuningDirection.IN_TUNE, result?.direction)
    }

    // =========================================================================
    // 3. Bolâhenk Dügâh (329.63 Hz) Birim Testi
    // =========================================================================

    @Test
    fun testBolahenkDugah329HzAnalysis() {
        // Bolâhenk düzeninde Dügâh 12-TET ≈ 329.6275 Hz / Pisagor 330.0 Hz
        val detectedFreq = 329.63

        val result = engine.analyzePitch(
            detectedFrequency = detectedFreq,
            clarity = 0.96,
            ahenk = Ahenk.BOLAHENK
        )

        assertNotNull("Bolâhenk Dügâh sonucu null olmamalı", result)
        assertEquals("Dügâh", result?.perdeName)
        assertEquals(Ahenk.BOLAHENK, result?.ahenk)
        // 329.63 Hz ile 330.0 Hz arasındaki koma farkı < 0.1 koma olup tam tolerans içindedir
        assertTrue("Bolâhenk Dügâh tolerans dahilinde olmalı (|ΔKoma| < 0.15)", kotlin.math.abs(result?.komaDifference ?: 99.0) < 0.15)
        assertTrue("Bolâhenk Dügâh akortlu olmalı", result?.isTuned == true)
        assertEquals(TuningDirection.IN_TUNE, result?.direction)
    }

    // =========================================================================
    // 4. Kaba Çârgâh (256.0 Hz) Birim Testi
    // =========================================================================

    @Test
    fun testKabaCargah256HzAnalysis() {
        val detectedFreq = 256.0

        val result = engine.analyzePitch(
            detectedFrequency = detectedFreq,
            clarity = 0.99,
            ahenk = Ahenk.MANSUR,
            usePhysicalMansur = true // 256.0 Hz Fizik Kaba Çârgâh referansı
        )

        assertNotNull("Kaba Çârgâh sonucu null olmamalı", result)
        assertEquals("Kaba Çârgâh", result?.perdeName)
        assertEquals(256.0, result?.targetFrequency ?: 0.0, delta)
        assertEquals(0.0, result?.komaDifference ?: 99.0, delta)
        assertTrue("Kaba Çârgâh tam akortlu olmalı", result?.isTuned == true)
        assertEquals(TuningDirection.IN_TUNE, result?.direction)
        assertEquals(0, result?.octaveIndex) // Kaba oktav
    }

    // =========================================================================
    // 5. Segâh Perdesi İcra Toleransı Testleri (Özkan s. 51)
    // =========================================================================

    @Test
    fun testSegahToleranceModes() {
        // Mansur fizik düzeninde Segâh perdesi teorik frekansı: 256 * (4096 / 2187) ≈ 479.444 Hz
        val theoreticalSegahFreq = 256.0 * (4096.0 / 2187.0)

        // 1. Durum: İcracı 1 koma pest çaldığında (f = f_segah * 2^(-1/53))
        val pest1Freq = theoreticalSegahFreq * 2.0.pow(-1.0 / 53.0)

        // Teori modunda (NONE): 1 koma pest olduğundan akort dışı sayılır
        val noneResult = engine.analyzePitch(pest1Freq, ahenk = Ahenk.MANSUR, usePhysicalMansur = true, segahMode = SegahNuanceMode.NONE)
        assertNotNull(noneResult)
        assertEquals("Segâh", noneResult?.perdeName)
        assertEquals(-1.0, noneResult?.komaDifference ?: 0.0, delta)
        assertFalse("Nüanssız modda 1 koma pest akort dışı olmalı", noneResult?.isTuned == true)

        // 1 Koma Pest İcra Modunda (PEST_1_KOMA): Hedef kaydırıldığı için tam akortlu sayılır
        val pest1Result = engine.analyzePitch(pest1Freq, ahenk = Ahenk.MANSUR, usePhysicalMansur = true, segahMode = SegahNuanceMode.PEST_1_KOMA)
        assertNotNull(pest1Result)
        assertEquals("Segâh", pest1Result?.perdeName)
        assertEquals(0.0, pest1Result?.komaDifference ?: 99.0, delta)
        assertTrue("1 Koma Pest modunda akortlu olmalı", pest1Result?.isTuned == true)
        assertTrue(pest1Result?.isSegahToleranceApplied == true)

        // 2. Durum: İcracı 2 koma pest çaldığında (Uşşak/Hüseynî tavrı: f = f_segah * 2^(-2/53))
        val pest2Freq = theoreticalSegahFreq * 2.0.pow(-2.0 / 53.0)
        val pest2Result = engine.analyzePitch(pest2Freq, ahenk = Ahenk.MANSUR, usePhysicalMansur = true, segahMode = SegahNuanceMode.PEST_2_KOMA)
        assertNotNull(pest2Result)
        assertEquals("Segâh", pest2Result?.perdeName)
        assertEquals(0.0, pest2Result?.komaDifference ?: 99.0, delta)
        assertTrue("2 Koma Pest modunda akortlu olmalı", pest2Result?.isTuned == true)

        // 3. Durum: Esnek İcra Toleransı Modu (TOLERANT_RANGE: -2.0 .. 0.0 koma)
        val midTolerantFreq = theoreticalSegahFreq * 2.0.pow(-1.4 / 53.0) // -1.4 koma pest icra
        val tolerantResult = engine.analyzePitch(midTolerantFreq, ahenk = Ahenk.MANSUR, usePhysicalMansur = true, segahMode = SegahNuanceMode.TOLERANT_RANGE)
        assertNotNull(tolerantResult)
        assertEquals("Segâh", tolerantResult?.perdeName)
        assertTrue("Esnek Segâh toleransında -1.4 koma akortlu kabul edilmeli", tolerantResult?.isTuned == true)
        assertTrue(tolerantResult?.isSegahToleranceApplied == true)
    }

    // =========================================================================
    // 6. Uçtan Uca Ses Tamponu Analizi (PCM-16 Buffer End-to-End)
    // =========================================================================

    @Test
    fun testAnalyzeAudioBufferEndToEnd() {
        val sampleRate = 44100
        val targetFreq = 440.0 // Mansur Dügâh
        val numSamples = 2048

        // 16-bit PCM sinüs tamponu oluştur
        val shortBuffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val sample = (0.8 * sin(2.0 * PI * targetFreq * t) * 32767.0).toInt().toShort()
            shortBuffer[i] = sample
        }

        val result = engine.analyzeAudioBuffer(shortBuffer, sampleRate, ahenk = Ahenk.MANSUR)

        assertNotNull("Uçtan uca tampon analizi sonuç üretmeli", result)
        assertEquals("Dügâh", result?.perdeName)
        assertEquals(440.0, result?.detectedFrequency ?: 0.0, 0.2)
        assertEquals(0.0, result?.komaDifference ?: 99.0, 0.05)
        assertTrue(result?.isTuned == true)
    }
}
