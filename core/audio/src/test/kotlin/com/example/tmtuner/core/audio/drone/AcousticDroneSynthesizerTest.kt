package com.example.tmtuner.core.audio.drone

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

class AcousticDroneSynthesizerTest {

    private lateinit var synthesizer: AcousticDroneSynthesizer
    private val sampleRate = 48000
    private val frameSize = 2048

    @Before
    fun setUp() {
        synthesizer = AcousticDroneSynthesizer(sampleRate = sampleRate)
    }

    @Test
    fun testPcm16OutputDoesNotClipAndRespectsHeadroom() {
        synthesizer.setFrequencies(440.0, 660.0)
        synthesizer.targetVolume = 1.0f
        synthesizer.profile = AcousticDroneProfile.TANBURA
        synthesizer.isDualDroneEnabled = true

        // Rampa tamamlansın diye warm-up tamponu
        val warmUp = ShortArray(frameSize * 5)
        synthesizer.renderPcm16(warmUp)

        val buffer = ShortArray(frameSize * 10)
        synthesizer.renderPcm16(buffer, 0, buffer.size)

        var hasNonZero = false
        var maxSampleMagnitude = 0

        // 0.85 katsayısı gereği 32767 * 0.85 = 27851.95. Güvenlik marjı ile tepe 28000'i geçmemelidir.
        val maxAllowedHeadroomSample = (32767 * 0.85 + 2).toInt()

        for (sample in buffer) {
            val mag = abs(sample.toInt())
            if (mag > maxSampleMagnitude) {
                maxSampleMagnitude = mag
            }
            if (sample != 0.toShort()) {
                hasNonZero = true
            }
            assertTrue("Örnek 16-bit PCM sınırları içinde kalmalıdır", sample in Short.MIN_VALUE..Short.MAX_VALUE)
            assertTrue("0.85 headroom katsayısı aşılmamalıdır (Sample: $sample, İzin Verilen: $maxAllowedHeadroomSample)", mag <= maxAllowedHeadroomSample)
        }

        assertTrue("Sentezlenen sinyal sıfır olmayan değerler içermelidir", hasNonZero)
        assertTrue("Sinyal beklenen genlik seviyesine ulaşmalıdır (Maks: $maxSampleMagnitude)", maxSampleMagnitude > 15000)
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

        val sampleDelta = abs(firstSampleFrame2 - lastSampleFrame1)
        assertTrue(
            "Ardışık iki tampon arasında faz sıçraması olmamalıdır (Delta: $sampleDelta)",
            sampleDelta < 2500
        )
    }

    @Test
    fun testFrequencyChangeWith50msSmoothing() {
        synthesizer.setFrequencies(293.33) // Rast
        synthesizer.targetVolume = 0.8f
        synthesizer.profile = AcousticDroneProfile.PURE_SINE

        // Warm up
        val warmUp = ShortArray(frameSize * 3)
        synthesizer.renderPcm16(warmUp)

        val buffer1 = ShortArray(frameSize)
        synthesizer.renderPcm16(buffer1)

        // Frekansı anında Dügâh'a (330.0 Hz) değiştir (50 ms rampa tetiklenir)
        synthesizer.setFrequencies(330.0)
        val buffer2 = ShortArray(frameSize)
        synthesizer.renderPcm16(buffer2)

        val lastSampleFrame1 = buffer1[frameSize - 1].toInt()
        val firstSampleFrame2 = buffer2[0].toInt()
        val sampleDelta = abs(firstSampleFrame2 - lastSampleFrame1)

        assertTrue(
            "Frekans değişiminde faz ve türev sıçraması (pop/click) olmamalıdır (Delta: $sampleDelta)",
            sampleDelta < 2500
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

        var isDifferent = false
        for (i in 0 until frameSize) {
            if (bufSingle[i] != bufDual[i]) {
                isDifferent = true
                break
            }
        }
        assertTrue("Çift dem aktifken sinyal formu değişmelidir", isDifferent)
    }

    @Test
    fun testContinuousPlayback60SecondsWaveformIntegrity() {
        // 60 saniye kesintisiz sentez simülasyonu: 48000 Hz'de 2.880.000 örnek (~1406 frame)
        synthesizer.setFrequencies(293.33) // Rast
        synthesizer.targetVolume = 0.9f
        synthesizer.profile = AcousticDroneProfile.TANBURA
        synthesizer.isDualDroneEnabled = true

        val buffer = ShortArray(frameSize)
        val totalFrames = (60 * sampleRate) / frameSize

        var previousSample = 0
        var totalSamplesChecked = 0
        var maxObservedStep = 0

        val notes = listOf(293.33, 330.0, 391.11, 440.0, 495.0, 586.66)

        for (frame in 0 until totalFrames) {
            // Her ~10 saniyede bir dinamik perde değişimi simüle et
            if (frame > 0 && frame % (totalFrames / notes.size) == 0) {
                val nextNote = notes[(frame / (totalFrames / notes.size)) % notes.size]
                synthesizer.setFrequencies(nextNote)
            }

            synthesizer.renderPcm16(buffer)

            for (i in 0 until frameSize) {
                val currentSample = buffer[i].toInt()
                if (totalSamplesChecked > 0) {
                    val step = abs(currentSample - previousSample)
                    if (step > maxObservedStep) {
                        maxObservedStep = step
                    }
                    // Harmonikler dahil maksimum izin verilen türev sıçraması (pop/yırtılma kontrolü)
                    assertTrue(
                        "Dalga formunda kopma veya cızırtı sıçraması tespit edildi (Örnek: $totalSamplesChecked, Step: $step)",
                        step < 12000
                    )
                }
                assertTrue("Sinyal PCM-16 sınırları içinde kalmalıdır", currentSample in -32768..32767)
                previousSample = currentSample
                totalSamplesChecked++
            }
        }

        assertTrue("En az 60 saniyelik örnek işlenmiş olmalıdır", totalSamplesChecked >= 60 * sampleRate - frameSize)
        assertTrue("Sinyal aktif olmalıdır (Maksimum gözlenen adım: $maxObservedStep)", maxObservedStep > 500)
    }
}
