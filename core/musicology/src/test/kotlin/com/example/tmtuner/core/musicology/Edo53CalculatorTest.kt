package com.example.tmtuner.core.musicology

import com.example.tmtuner.core.musicology.math.Edo53Calculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class Edo53CalculatorTest {

    @Test
    fun testHolderianKomaConstants() {
        assertEquals(53, Edo53Calculator.KOMA_PER_OCTAVE)
        assertEquals(1200.0 / 53.0, Edo53Calculator.HOLDER_KOMA_CENT, 0.0001)
    }

    @Test
    fun testOzkanPythagoreanIntervalRatios() {
        // T: 9/8, K: 65536/59049, S: 2187/2048, B: 256/243, F: 531441/524288
        assertEquals(9.0 / 8.0, Edo53Calculator.RATIO_T, 0.00001)
        assertEquals(65536.0 / 59049.0, Edo53Calculator.RATIO_K, 0.00001)
        assertEquals(2187.0 / 2048.0, Edo53Calculator.RATIO_S, 0.00001)
        assertEquals(256.0 / 243.0, Edo53Calculator.RATIO_B, 0.00001)
        assertEquals(531441.0 / 524288.0, Edo53Calculator.RATIO_F, 0.00001)

        // Tanini / Büyük Mücennep = Fazla (T / K == F)
        assertEquals(Edo53Calculator.RATIO_F, Edo53Calculator.RATIO_T / Edo53Calculator.RATIO_K, 0.000001)
    }

    @Test
    fun testBolahenkAnalyticalFrequencies() {
        // Bolâhenk Düzeni: Nevâ = 440.0 Hz referansı
        // Dügâh (mutlak koma 40) -> 330.0 Hz (Tam eşitlik)
        val dugah = Edo53Calculator.toBolahenkPitch(40)
        assertEquals(330.0, dugah, 0.001)

        // Nevâ (mutlak koma 49 / 62) -> 440.0 Hz
        val neva49 = Edo53Calculator.toBolahenkPitch(49)
        assertEquals(440.0, neva49, 0.001)

        val neva62 = Edo53Calculator.toBolahenkPitch(62)
        assertEquals(440.0, neva62, 0.001)

        // Râst (mutlak koma 31) -> (440 * 4/9) * 1.5 = 880/3 ≈ 293.333 Hz
        val rast = Edo53Calculator.toBolahenkPitch(31)
        assertEquals(293.333, rast, 0.01)

        // Segâh (mutlak koma 48) -> (440 * 4/9) * (4096/2187) = 7208960/19683 ≈ 366.253 Hz
        val segah = Edo53Calculator.toBolahenkPitch(48)
        assertEquals(366.253, segah, 0.01)
    }

    @Test
    fun testMansurAnalyticalFrequencies() {
        // Mansur Fizik Düzeni: Kaba Çârgâh = 256.0 Hz referansı
        // Kaba Çârgâh (mutlak koma 0) -> 256.0 Hz
        val kabaCargah = Edo53Calculator.toMansurPitch(0)
        assertEquals(256.0, kabaCargah, 0.001)

        // Râst (mutlak koma 31) -> 256.0 * 1.5 = 384.0 Hz
        val rast = Edo53Calculator.toMansurPitch(31)
        assertEquals(384.0, rast, 0.001)

        // Dügâh (mutlak koma 40) -> 256.0 * 27/16 = 432.0 Hz
        val dugah = Edo53Calculator.toMansurPitch(40)
        assertEquals(432.0, dugah, 0.001)

        // Segâh (mutlak koma 48/49) -> 256.0 * 4096/2187 = 1048576/2187 ≈ 479.459 Hz
        val segah = Edo53Calculator.toMansurPitch(48)
        assertEquals(479.459, segah, 0.01)
    }

    @Test
    fun testNuanceTolerance() {
        val segahMutlakKoma = 49
        val standartFreq = Edo53Calculator.toBolahenkPitch(segahMutlakKoma)
        val nuanceEksi1Freq = Edo53Calculator.applyNuanceOffset(standartFreq, -1)
        val nuanceEksi2Freq = Edo53Calculator.applyNuanceOffset(standartFreq, -2)

        assertTrue(nuanceEksi1Freq < standartFreq)
        assertTrue(nuanceEksi2Freq < nuanceEksi1Freq)

        val birKomaOrani = 2.0.pow(1.0 / 53.0)
        assertEquals(standartFreq / birKomaOrani, nuanceEksi1Freq, 0.01)
        assertEquals(standartFreq / (birKomaOrani * birKomaOrani), nuanceEksi2Freq, 0.01)
    }

    @Test
    fun testCentsAndKomasBetween() {
        // Oktav arası: 2:1 frekans oranı = 1200 cent, 53 koma
        val cents = Edo53Calculator.centsBetween(880.0, 440.0)
        assertEquals(1200.0, cents, 0.001)

        val komas = Edo53Calculator.komasBetween(880.0, 440.0)
        assertEquals(53.0, komas, 0.001)
    }
}
