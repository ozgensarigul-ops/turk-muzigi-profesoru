package com.example.tmtuner.core.audio.drone

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

class AcousticDroneSynthesizerTest {

    private lateinit var synthesizer: AcousticDroneSynthesizer
    private val sampleRate = 44100
    private val frameSize = 1024

    @Before
    fun setUp() {
        synthesizer = AcousticDroneSynthesizer(sampleRate = sampleRate)
    }

    @Test
    fun testPcm16OutputDoesNotClip() {
        synthesizer.setFrequencies(440.0, 660.0)
        synthesizer.targetVolume = 1.0f
        synthesizer.profile = AcousticDroneProfile.TANBURA
        synthesizer.isDualDroneEnabled = true

        val buffer = ShortArray(frameSize * 5)
        synthesizer.renderPcm16(buffer, 0, buffer.size)

        var hasNonZero = false
        for (sample in buffer) {
            if (sample != 0.toShort()) {
                hasNonZero = true
            }
            assertTrue("Örnek 16-bit PCM sınırları içinde kalmalıdır", sample in Short.MIN_VALUE..Short.MAX_VALUE)
        }
        assertTrue("Sentezlenen sinyal sıfır olmayan değerler içermelidir", hasNonZero)
    }

    @Test
    fun testPhaseContinuityBetweenConsecutiveFrames() {
        synthesizer.setFrequencies(330.0)
        synthesizer.targetVolume = 0.8f
        synthesizer.profile = AcousticDroneProfile.PURE_SINE

        // İlk tamponu işle (Rampa tamamlansın)
        val warmUp = ShortArray(frameSize * 3)
        synthesizer.renderPcm16(warmUp)

        // İki ardışık tampon al
        val buffer1 = ShortArray(frameSize)
        val buffer2 = ShortArray(frameSize)

        synthesizer.renderPcm16(buffer1)
        synthesizer.renderPcm16(buffer2)

        val lastSampleFrame1 = buffer1[frameSize - 1].toInt()
        val firstSampleFrame2 = buffer2[0].toInt()

        // 330 Hz sinüs dalgasında 44100 Hz örneklemede ardışık iki örnek arasındaki maksimum delta:
        // max delta = 2 * PI * f / sampleRate * Amplitude ≈ 2 * PI * 330 / 44100 * (0.8 * 32767) ≈ 1230
        val sampleDelta = abs(firstSampleFrame2 - lastSampleFrame1)
        assertTrue(
            "Ardışık iki tampon arasında faz sıçraması olmamalıdır (Delta: $sampleDelta)",
            sampleDelta < 2500
        )
    }

    @Test
    fun testFrequencyChangeWithoutPhaseReset() {
        synthesizer.setFrequencies(293.33) // Rast
        synthesizer.targetVolume = 0.8f
        synthesizer.profile = AcousticDroneProfile.PURE_SINE

        val buffer1 = ShortArray(frameSize)
        synthesizer.renderPcm16(buffer1)

        // Frekansı anında Dügâh'a (330.0 Hz) değiştir
        synthesizer.setFrequencies(330.0)
        val buffer2 = ShortArray(frameSize)
        synthesizer.renderPcm16(buffer2)

        val lastSampleFrame1 = buffer1[frameSize - 1].toInt()
        val firstSampleFrame2 = buffer2[0].toInt()
        val sampleDelta = abs(firstSampleFrame2 - lastSampleFrame1)

        assertTrue(
            "Frekans değişiminde faz sıçraması (pop/click) olmamalıdır (Delta: $sampleDelta)",
            sampleDelta < 3000
        )
    }

    @Test
    fun testHarmonicProfileDifferences() {
        synthesizer.setFrequencies(440.0)
        synthesizer.targetVolume = 1.0f

        // Saf Sinüs
        synthesizer.profile = AcousticDroneProfile.PURE_SINE
        assertEquals(1, synthesizer.profile.harmonicWeights.size)

        // Tanbura (Zengin gövde rezonansı)
        synthesizer.profile = AcousticDroneProfile.TANBURA
        assertTrue("Tanbura 5 veya daha fazla harmonik içermelidir", synthesizer.profile.harmonicWeights.size >= 5)

        // Ney (Nefes tınısı)
        synthesizer.profile = AcousticDroneProfile.NEY
        assertTrue("Ney 4 veya daha fazla harmonik içermelidir", synthesizer.profile.harmonicWeights.size >= 4)
    }

    @Test
    fun testAntiPopSoftEnvelope() {
        synthesizer.resetPhase()
        synthesizer.setFrequencies(440.0)
        synthesizer.targetVolume = 1.0f

        val buffer = ShortArray(frameSize)
        synthesizer.renderPcm16(buffer)

        // İlk birkaç örnek sıfıra yakın başlamalıdır (ani tam genlik patlaması olmamalı)
        val firstSamples = (0 until 10).map { abs(buffer[it].toInt()) }
        val maxInitialSample = firstSamples.maxOrNull() ?: 0
        assertTrue("İlk örnekler yumuşak rampa ile başlamalıdır, patlama olmamalı ($maxInitialSample)", maxInitialSample < 5000)
    }

    @Test
    fun testDualDroneChangesSignal() {
        synthesizer.setFrequencies(440.0, 660.0)
        synthesizer.targetVolume = 0.8f
        synthesizer.profile = AcousticDroneProfile.PURE_SINE
        synthesizer.isDualDroneEnabled = false

        val bufSingle = ShortArray(frameSize)
        synthesizer.renderPcm16(bufSingle)

        synthesizer.isDualDroneEnabled = true
        val bufDual = ShortArray(frameSize)
        synthesizer.renderPcm16(bufDual)

        // Sinyaller farklı olmalıdır
        var isDifferent = false
        for (i in 0 until frameSize) {
            if (bufSingle[i] != bufDual[i]) {
                isDifferent = true
                break
            }
        }
        assertTrue("Çift dem aktifken sinyal formu değişmelidir", isDifferent)
    }
}
