package com.example.tmtuner.core.musicology.engine

import com.example.tmtuner.core.musicology.analysis.MakamScorer
import com.example.tmtuner.core.musicology.model.ChesniPatterns
import com.example.tmtuner.core.musicology.model.Interval
import com.example.tmtuner.core.musicology.model.SeyirType
import com.example.tmtuner.core.musicology.registry.MakamRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Uşşâk Makamı Tespit ve Ayrıştırma Rafine Doğrulama Testi.
 * İsmail Hakkı Özkan "Türk Mûsikîsi Nazariyatı ve Usûlleri" (s. 51, 88-91, 118, 143, 203)
 */
class UssakDetectionRefinementTest {

    private lateinit var engine: MakamDetectionEngine

    @Before
    fun setUp() {
        engine = MakamDetectionEngine()
    }

    @Test
    fun testUssakDetectionWithPestSegahStream() {
        var time = 1000L

        // Dügâh, Segâh (1.5 koma pest nüanslı), Çârgâh, Nevâ, Gerdâniye perdelerinden
        // oluşan geleneksel inici-çıkıcı Uşşâk icra akışı (Özkan s. 51 & s. 143)
        val ussakStream = listOf(
            // --- 1. Giriş Bölümü: Nevâ güçlüsü ve Gerdâniye tizinde başlama ---
            PitchEvent(perdeName = "Neva", komaOffset = 0.0, durationMs = 800L, rmsEnergy = 0.95f, timestamp = time.also { time += 800 }),
            PitchEvent(perdeName = "Gerdâniye", komaOffset = 0.0, durationMs = 700L, rmsEnergy = 0.90f, timestamp = time.also { time += 700 }),
            PitchEvent(perdeName = "Neva", komaOffset = 0.0, durationMs = 1200L, rmsEnergy = 0.95f, timestamp = time.also { time += 1200 }), // Yarım karar Nevâ

            // --- 2. Gelişme Bölümü: Nevâ ve Uşşâk 4'lüsü etrafında dolaşma ---
            PitchEvent(perdeName = "Çârgâh", komaOffset = 0.0, durationMs = 600L, rmsEnergy = 0.85f, timestamp = time.also { time += 600 }),
            PitchEvent(perdeName = "Segâh", komaOffset = -1.5, durationMs = 900L, rmsEnergy = 0.90f, timestamp = time.also { time += 900 }), // 1.5 koma pest Segâh
            PitchEvent(perdeName = "Çârgâh", komaOffset = 0.0, durationMs = 500L, rmsEnergy = 0.80f, timestamp = time.also { time += 500 }),
            PitchEvent(perdeName = "Neva", komaOffset = 0.0, durationMs = 1000L, rmsEnergy = 0.95f, timestamp = time.also { time += 1000 }), // Nevâ'da kalış
            PitchEvent(perdeName = "Gerdâniye", komaOffset = 0.0, durationMs = 600L, rmsEnergy = 0.85f, timestamp = time.also { time += 600 }),
            PitchEvent(perdeName = "Neva", komaOffset = 0.0, durationMs = 800L, rmsEnergy = 0.90f, timestamp = time.also { time += 800 }),

            // --- 3. Teslim / Karar Yürüyüşü: Nevâ'dan Dügâh durağına iniş ---
            PitchEvent(perdeName = "Çârgâh", komaOffset = 0.0, durationMs = 600L, rmsEnergy = 0.80f, timestamp = time.also { time += 600 }),
            PitchEvent(perdeName = "Segâh", komaOffset = -1.5, durationMs = 800L, rmsEnergy = 0.85f, timestamp = time.also { time += 800 }), // 1.5 koma pest Segâh
            PitchEvent(perdeName = "Dügâh", komaOffset = 0.0, durationMs = 700L, rmsEnergy = 0.85f, timestamp = time.also { time += 700 }),
            PitchEvent(perdeName = "Segâh", komaOffset = -1.5, durationMs = 500L, rmsEnergy = 0.80f, timestamp = time.also { time += 500 }),
            PitchEvent(perdeName = "Dügâh", komaOffset = 0.0, durationMs = 2000L, rmsEnergy = 1.0f, timestamp = time.also { time += 2000 })  // Tam karar Dügâh
        )

        for (event in ussakStream) {
            engine.feedPitch(event)
        }

        val result = engine.analyze()

        // 1. Analiz Sonucu Boş Olmamalıdır
        assertNotNull("Analiz sonucu boş olmamalıdır", result)

        // 2. Makam: UŞŞÂK
        assertEquals(
            "Makam Uşşâk olarak tespit edilmelidir",
            "ussak",
            result!!.matchedMakam.id
        )
        assertEquals("Makam adı Uşşâk olmalıdır", "Uşşâk", result.matchedMakam.name)

        // 3. Durak: DÜGÂH
        assertEquals(
            "Karar perdesi Dügâh olmalıdır",
            "Dügâh",
            result.detectedDurak
        )

        // 4. Güçlü: NEVÂ
        assertEquals(
            "Güçlü perdesi Nevâ olmalıdır",
            "Nevâ",
            result.detectedGuclu
        )

        // 5. Seyir: İNİCİ_ÇIKICI
        assertEquals(
            "Seyir İnici-Çıkıcı olmalıdır",
            SeyirType.INICI_CIKICI,
            result.detectedSeyir
        )

        // 6. Güven: >= %85
        assertTrue(
            "Güven puanı en az %85 olmalıdır (Gerçekleşen: ${result.confidence * 100}%)",
            result.confidence >= 0.85f
        )
    }

    @Test
    fun testChesniPatternsUssakQuartetSpecification() {
        val pattern = ChesniPatterns.USSAK_QUARTET
        assertEquals("ussak_dortlusu", pattern.id)
        assertEquals("Uşşâk Dörtlüsü", pattern.name)
        assertEquals(false, pattern.isPentachord)
        assertEquals(listOf(Interval.BUYUK_MUCENNEP, Interval.KUCUK_MUCENNEP, Interval.TANINI), pattern.intervals)
        assertEquals(listOf(8, 5, 9), pattern.theoreticalKomas)
        assertEquals(22, pattern.totalKoma)

        // İcra tolerans koridorları: Dügâh->Segâh [6.0..8.5], Segâh->Çârgâh [4.5..7.5]
        assertEquals(6.0, pattern.dugahToSegahTolerance.start, 0.001)
        assertEquals(8.5, pattern.dugahToSegahTolerance.endInclusive, 0.001)
        assertEquals(4.5, pattern.segahToCargahTolerance.start, 0.001)
        assertEquals(7.5, pattern.segahToCargahTolerance.endInclusive, 0.001)

        // Segâh algılandığında +25 güven puanı
        assertEquals(25.0, pattern.segahBonusScore, 0.001)
    }

    @Test
    fun testCargahAndSuznakPenaltiesWhenDurakIsDugahWithSegah() {
        val cargah = MakamRegistry.getById("cargah")!!
        val suznak = MakamRegistry.getById("basit_suznak")!!

        val rawHistogram = mapOf(
            "dugah" to 5000.0,
            "segah" to 3000.0,
            "cargah" to 2000.0,
            "neva" to 4000.0
        )
        val totalWeight = 14000.0

        // 1. Durak Dügâh ve Segâh mevcutken Çârgâh puanı 0.0'a sıfırlanmalıdır
        val cargahScore = MakamScorer.scoreMakam(
            makam = cargah,
            detectedDurak = "dugah",
            detectedGuclu = "neva",
            detectedSeyir = SeyirType.INICI_CIKICI,
            rawHistogram = rawHistogram,
            totalWeightedDuration = totalWeight,
            hasSegah = true,
            avgSegahOffset = -1.5
        )
        assertEquals(0.0, cargahScore, 0.001)

        // 2. Durak Dügâh iken Sûz'nâk seçilemez (skor -100.0 olmalı)
        val suznakScore = MakamScorer.scoreMakam(
            makam = suznak,
            detectedDurak = "dugah",
            detectedGuclu = "neva",
            detectedSeyir = SeyirType.INICI_CIKICI,
            rawHistogram = rawHistogram,
            totalWeightedDuration = totalWeight,
            hasSegah = true,
            avgSegahOffset = -1.5
        )
        assertEquals(-100.0, suznakScore, 0.001)
    }
}
