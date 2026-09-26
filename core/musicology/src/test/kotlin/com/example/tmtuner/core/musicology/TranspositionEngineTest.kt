package com.example.tmtuner.core.musicology

import com.example.tmtuner.core.musicology.engine.TranspositionEngine
import com.example.tmtuner.core.musicology.model.Ahenk
import com.example.tmtuner.core.musicology.model.NeyType
import com.example.tmtuner.core.musicology.model.TransposingInstrument
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class TranspositionEngineTest {

    private val delta = 0.001

    // =========================================================================
    // 1. Mansur, Bolâhenk ve Kız Referans Testleri (İsmail Hakkı Özkan s. 87)
    // =========================================================================

    @Test
    fun testMansurReferenceFrequencies() {
        // Mansur Fizik Kaba Çârgâh = 256.0 Hz
        assertEquals(256.0, TranspositionEngine.MANSUR_KABA_CARGAH_HZ, delta)
        assertEquals(256.0, TranspositionEngine.getKabaCargahFrequency(Ahenk.MANSUR), delta)

        // Mansur Standart Diyapazon Dügâh (La4) = 440.0 Hz (0 koma, 1/1 oranı)
        assertEquals(440.0, TranspositionEngine.MANSUR_DUGAH_HZ, delta)
        assertEquals(440.0, TranspositionEngine.getDugahFrequency(Ahenk.MANSUR), delta)
        assertEquals(0, TranspositionEngine.MANSUR_KOMA_SHIFT)
        assertEquals(1.0, TranspositionEngine.MANSUR_RATIO, delta)

        // Mansur Fizik Dügâh: 256 * (27 / 16) = 432.0 Hz
        assertEquals(432.0, TranspositionEngine.MANSUR_DUGAH_PHYSICAL_HZ, delta)
    }

    @Test
    fun testBolahenkReferenceFrequencies() {
        // Bolâhenk Nevâ = 440.0 Hz
        assertEquals(440.0, TranspositionEngine.BOLAHENK_NEVA_HZ, delta)

        // Bolâhenk Pisagor Dügâh = 440.0 * (3 / 4) = 330.0 Hz (Tam Dörtlü pes: -22 koma, 3/4 oranı)
        assertEquals(330.0, TranspositionEngine.BOLAHENK_DUGAH_PYTHAGOREAN_HZ, delta)
        assertEquals(330.0, TranspositionEngine.getDugahFrequency(Ahenk.BOLAHENK, use12TetForBolahenk = false), delta)
        assertEquals(-22, TranspositionEngine.BOLAHENK_KOMA_FROM_MANSUR)
        assertEquals(3.0 / 4.0, TranspositionEngine.BOLAHENK_RATIO_FROM_MANSUR, delta)

        // Bolâhenk 12-TET Tampere Dügâh ≈ 329.6275 Hz (Dügâh ≈ 329.63 Hz)
        val tetDugah = TranspositionEngine.getDugahFrequency(Ahenk.BOLAHENK, use12TetForBolahenk = true)
        assertEquals(329.628, tetDugah, 0.01)

        // Bolâhenk Kaba Çârgâh = 440 * (4 / 9) ≈ 195.5556 Hz
        assertEquals(195.5556, TranspositionEngine.BOLAHENK_KABA_CARGAH_HZ, 0.001)
        assertEquals(195.5556, TranspositionEngine.getKabaCargahFrequency(Ahenk.BOLAHENK), 0.001)
    }

    @Test
    fun testKizReferenceFrequencies() {
        // Kız Düzeni: Mansur'a göre 1 Tanini tiz (+9 koma, 9/8 oranı, Dügâh = Si / B4 = 495.0 Hz - Özkan s. 87)
        assertEquals(440.0 * (9.0 / 8.0), TranspositionEngine.KIZ_DUGAH_HZ, delta)
        assertEquals(495.0, TranspositionEngine.KIZ_DUGAH_HZ, 0.01)
        assertEquals(495.0, TranspositionEngine.getDugahFrequency(Ahenk.KIZ), 0.01)
        assertEquals(9, TranspositionEngine.KIZ_KOMA_FROM_MANSUR)
        assertEquals(9.0 / 8.0, TranspositionEngine.KIZ_RATIO_FROM_MANSUR, delta)
    }

    // =========================================================================
    // 2. Eb Alto Saksafon Transpozisyon Testleri (Büyük 6'lı Pes: 16/27 ve 27/16)
    // =========================================================================

    @Test
    fun testEbAltoSaxMajorSixthTransposition() {
        val instrument = TransposingInstrument.EB_ALTO_SAX

        // 1. Yazılı -> Konsert çarpanı: Büyük 6'lı pes (x 16/27)
        val writtenPitch = 440.0 // Yazılı La4
        val soundingPitch = TranspositionEngine.toConcertPitch(writtenPitch, instrument) // Varsayılan asMinorThird = false
        val expectedSounding = writtenPitch * (16.0 / 27.0) // ≈ 260.7407 Hz (Konsert Do4)
        assertEquals(expectedSounding, soundingPitch, delta)

        // 2. Konsert -> Yazılı çarpanı: Büyük 6'lı tiz (x 27/16 = 1.6875)
        val reconstructedPitch = TranspositionEngine.toInstrumentPitch(soundingPitch, instrument)
        assertEquals(writtenPitch, reconstructedPitch, delta)

        // Çarpan sabitlerinin doğrudan doğrulanması
        assertEquals(16.0 / 27.0, TranspositionEngine.EB_ALTO_WRITTEN_TO_CONCERT_RATIO, delta)
        assertEquals(27.0 / 16.0, TranspositionEngine.EB_ALTO_CONCERT_TO_WRITTEN_RATIO, delta)
    }

    @Test
    fun testEbAltoSaxMinorThirdOptionalTransposition() {
        val instrument = TransposingInstrument.EB_ALTO_SAX

        // Aynı oktavda küçük 3'lü tiz modu (asMinorThird = true: x 32/27)
        val writtenPitch = 261.6256
        val expectedSoundingEb = writtenPitch * (32.0 / 27.0) // ≈ 310.075 Hz
        val actualSounding = TranspositionEngine.toConcertPitch(writtenPitch, instrument, asMinorThird = true)
        assertEquals(expectedSoundingEb, actualSounding, delta)

        // Konsert sesinden yazılı perdeye (küçük 3'lü pest: x 27/32)
        val actualInstrument = TranspositionEngine.toInstrumentPitch(actualSounding, instrument, asMinorThird = true)
        assertEquals(writtenPitch, actualInstrument, delta)
    }

    // =========================================================================
    // 3. Bb Tenor Saksafon Transpozisyon Testleri (Büyük İkili Pest Transpoze)
    // =========================================================================

    @Test
    fun testBbTenorSaxMajorSecondTransposition() {
        val instrument = TransposingInstrument.BB_TENOR_SAX

        // Yazılı Do (261.6256 Hz) üflendiğinde konsert Sib tınlar (büyük 2'li pest / Tanini: x 8/9)
        val writtenPitch = 261.6256
        val expectedSoundingBb = writtenPitch * (8.0 / 9.0) // ≈ 232.556 Hz
        val actualSounding = TranspositionEngine.toConcertPitch(writtenPitch, instrument)
        assertEquals(expectedSoundingBb, actualSounding, delta)

        // Konsert Sib duyulduğunda icracının okuduğu Do'ya çevrilir (büyük 2'li tiz: x 9/8 = 1.125)
        val actualInstrument = TranspositionEngine.toInstrumentPitch(actualSounding, instrument)
        assertEquals(writtenPitch, actualInstrument, delta)
    }

    @Test
    fun testBbSopranoAndClarinetTransposition() {
        val instrument = TransposingInstrument.BB_SOPRANO_SAX_CLARINET

        val writtenPitch = 440.0
        val sounding = TranspositionEngine.toConcertPitch(writtenPitch, instrument)
        assertEquals(440.0 * (8.0 / 9.0), sounding, delta)

        val restored = TranspositionEngine.toInstrumentPitch(sounding, instrument)
        assertEquals(writtenPitch, restored, delta)
    }

    @Test
    fun testConcertInstrumentNoShift() {
        val instrument = TransposingInstrument.CONCERT_C
        val freq = 440.0
        assertEquals(freq, TranspositionEngine.toConcertPitch(freq, instrument), delta)
        assertEquals(freq, TranspositionEngine.toInstrumentPitch(freq, instrument), delta)
    }

    // =========================================================================
    // 4. Ney Çeşitleri ve Ahenkler Arası Transpozisyon
    // =========================================================================

    @Test
    fun testNeyTypesHierarchyAndShifts() {
        // Mansur Ney referans (0 koma, 1/1 oranı)
        assertEquals(0, NeyType.MANSUR.komaShiftFromMansur)
        assertEquals(1.0, NeyType.MANSUR.ratioFromMansur, delta)
        assertEquals(Ahenk.MANSUR, NeyType.MANSUR.ahenk)

        // Kız Ney: Mansur'a göre 1 Tanini tiz (+9 koma, 9/8 oranı - Özkan s. 87)
        assertEquals(9, NeyType.KIZ.komaShiftFromMansur)
        assertEquals(9.0 / 8.0, NeyType.KIZ.ratioFromMansur, delta)
        assertEquals(Ahenk.KIZ, NeyType.KIZ.ahenk)

        // Bolâhenk Ney: Mansur'a göre Tam Dörtlü pes (-22 koma, 3/4 oranı)
        assertEquals(-22, NeyType.BOLAHENK.komaShiftFromMansur)
        assertEquals(3.0 / 4.0, NeyType.BOLAHENK.ratioFromMansur, delta)
        assertEquals(Ahenk.BOLAHENK, NeyType.BOLAHENK.ahenk)

        // Bolâhenk Nısfiye: 1 oktav tiz Bolâhenk (+31 koma)
        assertEquals(31, NeyType.BOLAHENK_NISFIYE.komaShiftFromMansur)
        assertEquals(1.5, NeyType.BOLAHENK_NISFIYE.ratioFromMansur, delta)

        // Süpürde Ney
        assertEquals(13, NeyType.SUPURDE.komaShiftFromMansur)
    }

    @Test
    fun testTransposeBetweenNeys() {
        val mansurFreq = 384.0

        // Mansur'dan Kız Ney'e transpozisyon (+22 koma / Tam Dörtlü tiz: frekans artar)
        val kizFreq = TranspositionEngine.transposeBetweenNeys(mansurFreq, NeyType.MANSUR, NeyType.KIZ)
        assertTrue("Kız Ney Mansur'dan daha tiz olmalı", kizFreq > mansurFreq)

        // Tersine Kız'dan Mansur'a geri transpoze edildiğinde tam eşit olmalıdır
        val backToMansurFromKiz = TranspositionEngine.transposeBetweenNeys(kizFreq, NeyType.KIZ, NeyType.MANSUR)
        assertEquals(mansurFreq, backToMansurFromKiz, delta)

        // Mansur'dan Bolâhenk Ney'e transpozisyon (-22 koma / Tam Dörtlü pes: frekans azalır)
        val bolahenkFreq = TranspositionEngine.transposeBetweenNeys(mansurFreq, NeyType.MANSUR, NeyType.BOLAHENK)
        assertTrue("Bolâhenk Ney Mansur'dan daha pes olmalı", bolahenkFreq < mansurFreq)

        // Tersine Bolâhenk'ten Mansur'a geri transpoze edildiğinde tam eşit olmalıdır
        val backToMansurFromBolahenk = TranspositionEngine.transposeBetweenNeys(bolahenkFreq, NeyType.BOLAHENK, NeyType.MANSUR)
        assertEquals(mansurFreq, backToMansurFromBolahenk, delta)

        // Analitik oran ile transpozisyon (useExactRatio = true)
        val kizExact = TranspositionEngine.transposeBetweenNeys(mansurFreq, NeyType.MANSUR, NeyType.KIZ, useExactRatio = true)
        assertEquals(mansurFreq * (9.0 / 8.0), kizExact, delta)

        val bolahenkExact = TranspositionEngine.transposeBetweenNeys(mansurFreq, NeyType.MANSUR, NeyType.BOLAHENK, useExactRatio = true)
        assertEquals(mansurFreq * (3.0 / 4.0), bolahenkExact, delta)
    }

    @Test
    fun testTransposeBetweenAhenks() {
        val mansurLa = 440.0

        // Mansur -> Kız Âhengi (1 Tanini tiz: x 9/8)
        val kizLa = TranspositionEngine.transposeBetweenAhenks(mansurLa, Ahenk.MANSUR, Ahenk.KIZ)
        assertEquals(440.0 * (9.0 / 8.0), kizLa, delta)
        assertEquals(495.0, kizLa, 0.01)

        // Mansur -> Bolâhenk Âhengi (Tam Dörtlü pes: x 3/4)
        val bolahenkLa = TranspositionEngine.transposeBetweenAhenks(mansurLa, Ahenk.MANSUR, Ahenk.BOLAHENK)
        assertEquals(330.0, bolahenkLa, delta)

        // Bolâhenk -> Kız Âhengi (330 * (495/330) = 495.0 Hz)
        val bolahenkToKiz = TranspositionEngine.transposeBetweenAhenks(bolahenkLa, Ahenk.BOLAHENK, Ahenk.KIZ)
        assertEquals(kizLa, bolahenkToKiz, delta)
    }

    // =========================================================================
    // 5. 53-EDO Koma Düzeyinde Transpozisyon
    // =========================================================================

    @Test
    fun testKomaShiftsForTransposingInstruments() {
        // Râst (31. koma) perdesi Bb Tenor Saksafon (+9 koma) için kaydırıldığında 40 (Dügâh) olmalıdır
        val rastKoma = 31
        val shiftedBb = TranspositionEngine.transposeKomaForInstrument(rastKoma, TransposingInstrument.BB_TENOR_SAX)
        assertEquals(40, shiftedBb)

        // Konsert Çârgâh (0. koma) Eb Alto Saksafon (+40 koma / büyük 6'lı) için kaydırıldığında 40 (Dügâh) olmalıdır
        val cargahKoma = 0
        val shiftedEb = TranspositionEngine.transposeKomaForInstrument(cargahKoma, TransposingInstrument.EB_ALTO_SAX)
        assertEquals(40, shiftedEb)
    }

    @Test
    fun testRoundTripInvertibility() {
        val testFrequencies = doubleArrayOf(195.55, 256.0, 293.33, 330.0, 366.27, 440.0, 495.0, 586.66)
        val instruments = TransposingInstrument.values()

        for (freq in testFrequencies) {
            for (inst in instruments) {
                // Varsayılan büyük 6'lı / enstrüman standart modu
                val concert = TranspositionEngine.toConcertPitch(freq, inst)
                val backToInst = TranspositionEngine.toInstrumentPitch(concert, inst)
                assertEquals("Invertibility failed for instrument ${inst.name} at freq $freq", freq, backToInst, 0.0001)

                // Küçük 3'lü modu
                val concertThird = TranspositionEngine.toConcertPitch(freq, inst, asMinorThird = true)
                val backToInstThird = TranspositionEngine.toInstrumentPitch(concertThird, inst, asMinorThird = true)
                assertEquals("Minor third invertibility failed for instrument ${inst.name} at freq $freq", freq, backToInstThird, 0.0001)
            }
        }
    }

    @Test
    fun testAhenkDirectionalTransposition() {
        // Kız Neyi icrası (495 Hz = Si) sisteme girerken Mansur'a dönüştürülmek için
        // 9 KOMA PESTLEŞTİRİLİR (495 * 8/9 = 440 Hz = Dügâh)
        val kizLiveFrequency = 495.0
        val concertFromKiz = TranspositionEngine.toConcertPitch(kizLiveFrequency, Ahenk.KIZ)
        assertEquals(440.0, concertFromKiz, delta)

        // Mansur 440 Hz Kız Neyi icrasına dönüştürülürken 9 KOMA TİZLEŞTİRİLİR (440 * 9/8 = 495 Hz)
        val icraForKiz = TranspositionEngine.toIcraPitch(440.0, Ahenk.KIZ)
        assertEquals(495.0, icraForKiz, delta)

        // Bolâhenk icrası (330 Hz) Mansur'a dönüştürülürken 22 KOMA TİZLEŞTİRİLİR (330 * 4/3 = 440 Hz)
        val concertFromBolahenk = TranspositionEngine.toConcertPitch(330.0, Ahenk.BOLAHENK)
        assertEquals(440.0, concertFromBolahenk, delta)

        // Koma dönüşüm yönü: Kız Neyi koma indeksi 9 koma eksiltilmelidir
        val kizDugahKoma = 49 // Si / B
        val concertKoma = TranspositionEngine.transposeKomaToConcert(kizDugahKoma, Ahenk.KIZ)
        assertEquals(40, concertKoma) // 49 - 9 = 40 (Dügâh / La)

        val restoredKoma = TranspositionEngine.transposeKomaFromConcert(concertKoma, Ahenk.KIZ)
        assertEquals(kizDugahKoma, restoredKoma)
    }
}
