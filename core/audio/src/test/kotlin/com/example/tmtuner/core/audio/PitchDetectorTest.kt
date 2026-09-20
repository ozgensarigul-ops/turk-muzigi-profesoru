package com.example.tmtuner.core.audio

import com.example.tmtuner.core.audio.detector.PitchDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class PitchDetectorTest {

    private val detector = PitchDetector()

    /**
     * Test sinyali üretici (Sinüs dalgası).
     */
    private fun generateSineWave(
        frequency: Double,
        sampleRate: Int,
        numSamples: Int = 2048,
        amplitude: Double = 0.8
    ): DoubleArray {
        val buffer = DoubleArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            buffer[i] = amplitude * sin(2.0 * PI * frequency * t)
        }
        return buffer
    }

    /**
     * Harmonikli test sinyali üretici.
     */
    private fun generateHarmonicWave(
        fundamentalFreq: Double,
        sampleRate: Int,
        numSamples: Int = 2048
    ): DoubleArray {
        val buffer = DoubleArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Temel frekans (1.0) + 2. Harmonik (0.5) + 3. Harmonik (0.25)
            buffer[i] = 0.6 * sin(2.0 * PI * fundamentalFreq * t) +
                    0.3 * sin(2.0 * PI * (2 * fundamentalFreq) * t) +
                    0.15 * sin(2.0 * PI * (3 * fundamentalFreq) * t)
        }
        return buffer
    }

    // =========================================================================
    // 1. 44100 Hz Örnekleme Frekansı Testleri
    // =========================================================================

    @Test
    fun testDetectMansurDugah440HzAt44100() {
        val sampleRate = 44100
        val targetFreq = 440.0
        val buffer = generateSineWave(targetFreq, sampleRate)

        val result = detector.detectPitch(buffer, sampleRate)

        assertTrue("Sinyal perdelendirilmeli", result.isPitched)
        assertTrue("Berraklık skoru yüksek olmalı (> 0.90)", result.clarity > 0.90)
        assertEquals(targetFreq, result.frequency, 0.2) // 0.2 Hz tolerans
    }

    @Test
    fun testDetectBolahenkDugah329HzAt44100() {
        val sampleRate = 44100
        val targetFreq = 329.63
        val buffer = generateSineWave(targetFreq, sampleRate)

        val result = detector.detectPitch(buffer, sampleRate)

        assertTrue(result.isPitched)
        assertTrue(result.clarity > 0.90)
        assertEquals(targetFreq, result.frequency, 0.2)
    }

    @Test
    fun testDetectKabaCargah256HzAt44100() {
        val sampleRate = 44100
        val targetFreq = 256.0
        val buffer = generateSineWave(targetFreq, sampleRate)

        val result = detector.detectPitch(buffer, sampleRate)

        assertTrue(result.isPitched)
        assertTrue(result.clarity > 0.90)
        assertEquals(targetFreq, result.frequency, 0.2)
    }

    // =========================================================================
    // 2. 48000 Hz Örnekleme Frekansı Testleri
    // =========================================================================

    @Test
    fun testDetectMansurDugah440HzAt48000() {
        val sampleRate = 48000
        val targetFreq = 440.0
        val buffer = generateSineWave(targetFreq, sampleRate)

        val result = detector.detectPitch(buffer, sampleRate)

        assertTrue(result.isPitched)
        assertTrue(result.clarity > 0.90)
        assertEquals(targetFreq, result.frequency, 0.2)
    }

    @Test
    fun testDetectBolahenkDugah329HzAt48000() {
        val sampleRate = 48000
        val targetFreq = 329.63
        val buffer = generateSineWave(targetFreq, sampleRate)

        val result = detector.detectPitch(buffer, sampleRate)

        assertTrue(result.isPitched)
        assertTrue(result.clarity > 0.90)
        assertEquals(targetFreq, result.frequency, 0.2)
    }

    @Test
    fun testDetectKabaCargah256HzAt48000() {
        val sampleRate = 48000
        val targetFreq = 256.0
        val buffer = generateSineWave(targetFreq, sampleRate)

        val result = detector.detectPitch(buffer, sampleRate)

        assertTrue(result.isPitched)
        assertTrue(result.clarity > 0.90)
        assertEquals(targetFreq, result.frequency, 0.2)
    }

    // =========================================================================
    // 3. Harmonik Zengin Sinyal & Farklı Tampon Tipleri
    // =========================================================================

    @Test
    fun testHarmonicRichWaveDetection() {
        val sampleRate = 44100
        val fundamental = 440.0
        val buffer = generateHarmonicWave(fundamental, sampleRate)

        val result = detector.detectPitch(buffer, sampleRate)

        assertTrue("Harmonik zengin sinyal algılanmalı", result.isPitched)
        assertEquals(fundamental, result.frequency, 0.3)
    }

    @Test
    fun testFloatArrayAndShortArrayBuffers() {
        val sampleRate = 44100
        val targetFreq = 440.0
        val doubleBuffer = generateSineWave(targetFreq, sampleRate)

        // FloatArray testi
        val floatBuffer = FloatArray(doubleBuffer.size) { doubleBuffer[it].toFloat() }
        val floatResult = detector.detectPitch(floatBuffer, sampleRate)
        assertTrue(floatResult.isPitched)
        assertEquals(targetFreq, floatResult.frequency, 0.2)

        // ShortArray (PCM-16) testi
        val shortBuffer = ShortArray(doubleBuffer.size) { (doubleBuffer[it] * 32000).toInt().toShort() }
        val shortResult = detector.detectPitch(shortBuffer, sampleRate)
        assertTrue(shortResult.isPitched)
        assertEquals(targetFreq, shortResult.frequency, 0.2)
    }

    @Test
    fun testSilenceRejection() {
        val sampleRate = 44100
        val silentBuffer = DoubleArray(2048) { 0.0001 }

        val result = detector.detectPitch(silentBuffer, sampleRate)

        assertFalse("Sessizlikte perdelendirme yapılmamalı", result.isPitched)
        assertEquals(0.0, result.frequency, 0.0001)
    }
}
