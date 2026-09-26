package com.example.tmtuner.core.musicology.engine

import com.example.tmtuner.core.musicology.model.SeyirType
import com.example.tmtuner.core.musicology.registry.MakamRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MakamDetectionEngineTest {

    private lateinit var engine: MakamDetectionEngine

    @Before
    fun setUp() {
        engine = MakamDetectionEngine()
    }

    @Test
    fun testUssakMakamWithPestSegah() {
        var time = 1000L

        // Uşşâk makamı ezgisel seyri:
        // Nevâ güçlüsü çevresinde inici-çıkıcı başlama, Segâh'ta (-1.5 koma pest) gezinme ve Dügâh'ta karar
        val ussakMelody = listOf(
            PitchEvent(perdeName = "Neva", komaOffset = 0.0, durationMs = 800L, rmsEnergy = 0.9f, timestamp = time.also { time += 800 }),
            PitchEvent(perdeName = "Hüseynî", komaOffset = 0.0, durationMs = 600L, rmsEnergy = 0.8f, timestamp = time.also { time += 600 }),
            PitchEvent(perdeName = "Acem", komaOffset = 0.0, durationMs = 500L, rmsEnergy = 0.8f, timestamp = time.also { time += 500 }),
            PitchEvent(perdeName = "Hüseynî", komaOffset = 0.0, durationMs = 500L, rmsEnergy = 0.8f, timestamp = time.also { time += 500 }),
            PitchEvent(perdeName = "Neva", komaOffset = 0.0, durationMs = 1200L, rmsEnergy = 0.95f, timestamp = time.also { time += 1200 }), // Yarım karar Nevâ
            PitchEvent(perdeName = "Çârgâh", komaOffset = 0.0, durationMs = 600L, rmsEnergy = 0.85f, timestamp = time.also { time += 600 }),
            PitchEvent(perdeName = "Segâh", komaOffset = -1.5, durationMs = 900L, rmsEnergy = 0.85f, timestamp = time.also { time += 900 }), // -1.5 koma pest Segâh
            PitchEvent(perdeName = "Dügâh", komaOffset = 0.0, durationMs = 700L, rmsEnergy = 0.8f, timestamp = time.also { time += 700 }),
            PitchEvent(perdeName = "Rast", komaOffset = 0.0, durationMs = 400L, rmsEnergy = 0.7f, timestamp = time.also { time += 400 }), // Yeden
            PitchEvent(perdeName = "Segâh", komaOffset = -1.5, durationMs = 700L, rmsEnergy = 0.8f, timestamp = time.also { time += 700 }),
            PitchEvent(perdeName = "Dügâh", komaOffset = 0.0, durationMs = 1500L, rmsEnergy = 1.0f, timestamp = time.also { time += 1500 }) // Tam karar Dügâh
        )

        for (event in ussakMelody) {
            engine.feedPitch(event)
        }

        val result = engine.analyze()
        assertNotNull("Analiz sonucu boş olmamalıdır", result)
        assertEquals("Tespit edilen makam Uşşâk olmalıdır", "ussak", result!!.matchedMakam.id)
        assertEquals("Tespit edilen durak Dügâh olmalıdır", "Dügâh", result.detectedDurak)
        assertEquals("Tespit edilen güçlü Nevâ olmalıdır", "Nevâ", result.detectedGuclu)
        assertEquals("Uşşâk seyri İnici-Çıkıcı olmalıdır", SeyirType.INICI_CIKICI, result.detectedSeyir)
        assertTrue(
            "Pest Segâh icrasıyla Uşşâk güven katsayısı %85 veya üzerinde olmalıdır (Gerçekleşen: ${result.confidence})",
            result.confidence >= 0.85f
        )
    }

    @Test
    fun testRastMakamAscendingSeyir() {
        var time = 1000L

        // Râst makamı ezgisel seyri:
        // Râst perdesinden çıkıcı başlama, Nevâ'da yarım karar, Gerdâniye'ye kadar çıkış ve Râst'ta karar
        val rastMelody = listOf(
            PitchEvent(perdeName = "Rast", komaOffset = 0.0, durationMs = 1000L, rmsEnergy = 0.9f, timestamp = time.also { time += 1000 }),
            PitchEvent(perdeName = "Dügâh", komaOffset = 0.0, durationMs = 600L, rmsEnergy = 0.8f, timestamp = time.also { time += 600 }),
            PitchEvent(perdeName = "Segâh", komaOffset = 0.0, durationMs = 700L, rmsEnergy = 0.85f, timestamp = time.also { time += 700 }),
            PitchEvent(perdeName = "Çârgâh", komaOffset = 0.0, durationMs = 600L, rmsEnergy = 0.8f, timestamp = time.also { time += 600 }),
            PitchEvent(perdeName = "Neva", komaOffset = 0.0, durationMs = 1300L, rmsEnergy = 0.95f, timestamp = time.also { time += 1300 }), // Yarım Karar Nevâ
            PitchEvent(perdeName = "Hüseynî", komaOffset = 0.0, durationMs = 700L, rmsEnergy = 0.8f, timestamp = time.also { time += 700 }),
            PitchEvent(perdeName = "Eviç", komaOffset = 0.0, durationMs = 600L, rmsEnergy = 0.8f, timestamp = time.also { time += 600 }),
            PitchEvent(perdeName = "Gerdâniye", komaOffset = 0.0, durationMs = 800L, rmsEnergy = 0.85f, timestamp = time.also { time += 800 }),
            PitchEvent(perdeName = "Neva", komaOffset = 0.0, durationMs = 700L, rmsEnergy = 0.8f, timestamp = time.also { time += 700 }),
            PitchEvent(perdeName = "Çârgâh", komaOffset = 0.0, durationMs = 500L, rmsEnergy = 0.75f, timestamp = time.also { time += 500 }),
            PitchEvent(perdeName = "Segâh", komaOffset = 0.0, durationMs = 600L, rmsEnergy = 0.8f, timestamp = time.also { time += 600 }),
            PitchEvent(perdeName = "Dügâh", komaOffset = 0.0, durationMs = 600L, rmsEnergy = 0.8f, timestamp = time.also { time += 600 }),
            PitchEvent(perdeName = "Irak", komaOffset = 0.0, durationMs = 400L, rmsEnergy = 0.7f, timestamp = time.also { time += 400 }), // Yeden Irak
            PitchEvent(perdeName = "Rast", komaOffset = 0.0, durationMs = 1600L, rmsEnergy = 1.0f, timestamp = time.also { time += 1600 }) // Tam Karar Râst
        )

        for (event in rastMelody) {
            engine.feedPitch(event)
        }

        val result = engine.analyze()
        assertNotNull(result)
        assertEquals("Tespit edilen makam Râst olmalıdır", "rast", result!!.matchedMakam.id)
        assertEquals("Tespit edilen durak Râst olmalıdır", "Râst", result.detectedDurak)
        assertEquals("Tespit edilen güçlü Nevâ olmalıdır", "Nevâ", result.detectedGuclu)
        assertEquals("Râst makamı Çıkıcı seyre sahip olmalıdır", SeyirType.CIKICI, result.detectedSeyir)
        assertTrue("Râst güven katsayısı yüksek olmalıdır (Gerçekleşen: ${result.confidence})", result.confidence >= 0.85f)
    }

    @Test
    fun testHuseyniVsUssakDiscrimination() {
        var time = 1000L

        // Hüseynî makamı ezgisel seyri:
        // Hüseynî perdesi ve çevresinde başlayıp Hüseynî'de ısrarlı kalış (Güçlü: Hüseynî),
        // Acem yerine Eviç perdesi kullanımı, Segâh pest tınlaması ve Dügâh'ta karar
        val huseyniMelody = listOf(
            PitchEvent(perdeName = "Hüseynî", komaOffset = 0.0, durationMs = 1200L, rmsEnergy = 0.95f, timestamp = time.also { time += 1200 }), // Güçlü Hüseynî
            PitchEvent(perdeName = "Eviç", komaOffset = 0.0, durationMs = 800L, rmsEnergy = 0.85f, timestamp = time.also { time += 800 }), // Hüseynî'nin 6. derecesi Eviç!
            PitchEvent(perdeName = "Gerdâniye", komaOffset = 0.0, durationMs = 700L, rmsEnergy = 0.8f, timestamp = time.also { time += 700 }),
            PitchEvent(perdeName = "Hüseynî", komaOffset = 0.0, durationMs = 1400L, rmsEnergy = 0.95f, timestamp = time.also { time += 1400 }), // Israrlı yarım karar
            PitchEvent(perdeName = "Neva", komaOffset = 0.0, durationMs = 700L, rmsEnergy = 0.8f, timestamp = time.also { time += 700 }),
            PitchEvent(perdeName = "Çârgâh", komaOffset = 0.0, durationMs = 600L, rmsEnergy = 0.75f, timestamp = time.also { time += 600 }),
            PitchEvent(perdeName = "Segâh", komaOffset = -1.5, durationMs = 800L, rmsEnergy = 0.85f, timestamp = time.also { time += 800 }), // Pest Segâh
            PitchEvent(perdeName = "Dügâh", komaOffset = 0.0, durationMs = 800L, rmsEnergy = 0.8f, timestamp = time.also { time += 800 }),
            PitchEvent(perdeName = "Rast", komaOffset = 0.0, durationMs = 400L, rmsEnergy = 0.7f, timestamp = time.also { time += 400 }), // Yeden
            PitchEvent(perdeName = "Dügâh", komaOffset = 0.0, durationMs = 1600L, rmsEnergy = 1.0f, timestamp = time.also { time += 1600 }) // Tam karar Dügâh
        )

        for (event in huseyniMelody) {
            engine.feedPitch(event)
        }

        val result = engine.analyze()
        assertNotNull(result)
        assertEquals("Hüseynî güçlüsü ve Eviç perdesi varlığında Hüseynî makamı seçilmelidir", "huseyni", result!!.matchedMakam.id)
        assertEquals("Tespit edilen durak Dügâh olmalıdır", "Dügâh", result.detectedDurak)
        assertEquals("Tespit edilen güçlü Hüseynî olmalıdır", "Hüseynî", result.detectedGuclu)
    }

    @Test
    fun testHicazFamilyDiscrimination() {
        val engineHumayun = MakamDetectionEngine()
        var time1 = 1000L

        // Hümâyûn ezgisi: Dügâh'ta Hicaz 4'lüsü (Dügâh, Dik Kürdî, Hicâz, Nevâ) + Nevâ'da Bûselik 5'lisi (Hüseynî, Acem, Gerdâniye, Muhayyer)
        // Acem perdesi kesinlikle basılır, Eviç asla basılmaz!
        val humayunMelody = listOf(
            PitchEvent(perdeName = "Neva", komaOffset = 0.0, durationMs = 800L, rmsEnergy = 0.9f, timestamp = time1.also { time1 += 800 }),
            PitchEvent(perdeName = "Hicaz", komaOffset = 0.0, durationMs = 600L, rmsEnergy = 0.85f, timestamp = time1.also { time1 += 600 }),
            PitchEvent(perdeName = "Dik Kürdî", komaOffset = 0.0, durationMs = 500L, rmsEnergy = 0.8f, timestamp = time1.also { time1 += 500 }),
            PitchEvent(perdeName = "Neva", komaOffset = 0.0, durationMs = 1000L, rmsEnergy = 0.95f, timestamp = time1.also { time1 += 1000 }), // Güçlü Nevâ
            PitchEvent(perdeName = "Hüseynî", komaOffset = 0.0, durationMs = 600L, rmsEnergy = 0.8f, timestamp = time1.also { time1 += 600 }),
            PitchEvent(perdeName = "Acem", komaOffset = 0.0, durationMs = 900L, rmsEnergy = 0.9f, timestamp = time1.also { time1 += 900 }), // Ayırt edici ses: Acem!
            PitchEvent(perdeName = "Gerdâniye", komaOffset = 0.0, durationMs = 700L, rmsEnergy = 0.85f, timestamp = time1.also { time1 += 700 }),
            PitchEvent(perdeName = "Hüseynî", komaOffset = 0.0, durationMs = 500L, rmsEnergy = 0.8f, timestamp = time1.also { time1 += 500 }),
            PitchEvent(perdeName = "Neva", komaOffset = 0.0, durationMs = 700L, rmsEnergy = 0.85f, timestamp = time1.also { time1 += 700 }),
            PitchEvent(perdeName = "Hicaz", komaOffset = 0.0, durationMs = 700L, rmsEnergy = 0.85f, timestamp = time1.also { time1 += 700 }),
            PitchEvent(perdeName = "Dik Kürdî", komaOffset = 0.0, durationMs = 600L, rmsEnergy = 0.8f, timestamp = time1.also { time1 += 600 }),
            PitchEvent(perdeName = "Dügâh", komaOffset = 0.0, durationMs = 1500L, rmsEnergy = 1.0f, timestamp = time1.also { time1 += 1500 }) // Karar Dügâh
        )

        for (ev in humayunMelody) engineHumayun.feedPitch(ev)
        val humayunResult = engineHumayun.analyze()
        assertNotNull(humayunResult)
        assertEquals("Acem perdesiyle Hümâyûn makamı tespit edilmelidir", "humayun", humayunResult!!.matchedMakam.id)
        assertEquals("Tespit edilen durak Dügâh olmalıdır", "Dügâh", humayunResult.detectedDurak)
        assertEquals("Tespit edilen güçlü Nevâ olmalıdır", "Nevâ", humayunResult.detectedGuclu)

        val engineHicaz = MakamDetectionEngine()
        var time2 = 1000L

        // Hicaz ezgisi: Dügâh'ta Hicaz 4'lüsü + Nevâ'da Râst 5'lisi (Hüseynî, Eviç, Gerdâniye, Muhayyer)
        // Eviç perdesi basılır, Acem basılmaz!
        val hicazMelody = listOf(
            PitchEvent(perdeName = "Neva", komaOffset = 0.0, durationMs = 800L, rmsEnergy = 0.9f, timestamp = time2.also { time2 += 800 }),
            PitchEvent(perdeName = "Hicaz", komaOffset = 0.0, durationMs = 600L, rmsEnergy = 0.85f, timestamp = time2.also { time2 += 600 }),
            PitchEvent(perdeName = "Dik Kürdî", komaOffset = 0.0, durationMs = 500L, rmsEnergy = 0.8f, timestamp = time2.also { time2 += 500 }),
            PitchEvent(perdeName = "Neva", komaOffset = 0.0, durationMs = 1000L, rmsEnergy = 0.95f, timestamp = time2.also { time2 += 1000 }), // Güçlü Nevâ
            PitchEvent(perdeName = "Hüseynî", komaOffset = 0.0, durationMs = 600L, rmsEnergy = 0.8f, timestamp = time2.also { time2 += 600 }),
            PitchEvent(perdeName = "Eviç", komaOffset = 0.0, durationMs = 900L, rmsEnergy = 0.9f, timestamp = time2.also { time2 += 900 }), // Ayırt edici ses: Eviç!
            PitchEvent(perdeName = "Gerdâniye", komaOffset = 0.0, durationMs = 700L, rmsEnergy = 0.85f, timestamp = time2.also { time2 += 700 }),
            PitchEvent(perdeName = "Hüseynî", komaOffset = 0.0, durationMs = 500L, rmsEnergy = 0.8f, timestamp = time2.also { time2 += 500 }),
            PitchEvent(perdeName = "Neva", komaOffset = 0.0, durationMs = 700L, rmsEnergy = 0.85f, timestamp = time2.also { time2 += 700 }),
            PitchEvent(perdeName = "Hicaz", komaOffset = 0.0, durationMs = 700L, rmsEnergy = 0.85f, timestamp = time2.also { time2 += 700 }),
            PitchEvent(perdeName = "Dik Kürdî", komaOffset = 0.0, durationMs = 600L, rmsEnergy = 0.8f, timestamp = time2.also { time2 += 600 }),
            PitchEvent(perdeName = "Dügâh", komaOffset = 0.0, durationMs = 1500L, rmsEnergy = 1.0f, timestamp = time2.also { time2 += 1500 }) // Karar Dügâh
        )

        for (ev in hicazMelody) engineHicaz.feedPitch(ev)
        val hicazResult = engineHicaz.analyze()
        assertNotNull(hicazResult)
        assertEquals("Eviç perdesiyle Hicaz makamı tespit edilmelidir", "hicaz", hicazResult!!.matchedMakam.id)
        assertEquals("Tespit edilen durak Dügâh olmalıdır", "Dügâh", hicazResult.detectedDurak)
    }

    @Test
    fun testKarcigarMakamDetection() {
        var time = 1000L

        // Karcığar ezgisi:
        // Alt çeşni: Dügâh'ta Uşşâk 4'lüsü (Dügâh, Segâh -1.5k, Çârgâh, Nevâ)
        // Üst çeşni: Nevâ'da Hicaz 5'lisi (Nevâ, Hisâr, Şehnâz, Muhayyer)
        val karcigarMelody = listOf(
            PitchEvent(perdeName = "Neva", komaOffset = 0.0, durationMs = 1000L, rmsEnergy = 0.9f, timestamp = time.also { time += 1000 }), // Güçlü Nevâ
            PitchEvent(perdeName = "Hisar", komaOffset = 0.0, durationMs = 700L, rmsEnergy = 0.85f, timestamp = time.also { time += 700 }), // Nevâ'da Hicaz (Hisâr)
            PitchEvent(perdeName = "Şehnâz", komaOffset = 0.0, durationMs = 800L, rmsEnergy = 0.9f, timestamp = time.also { time += 800 }), // Nevâ'da Hicaz (Şehnâz)
            PitchEvent(perdeName = "Hisar", komaOffset = 0.0, durationMs = 600L, rmsEnergy = 0.8f, timestamp = time.also { time += 600 }),
            PitchEvent(perdeName = "Neva", komaOffset = 0.0, durationMs = 1200L, rmsEnergy = 0.95f, timestamp = time.also { time += 1200 }), // Yarım karar
            PitchEvent(perdeName = "Çârgâh", komaOffset = 0.0, durationMs = 600L, rmsEnergy = 0.8f, timestamp = time.also { time += 600 }),
            PitchEvent(perdeName = "Segâh", komaOffset = -1.5, durationMs = 900L, rmsEnergy = 0.85f, timestamp = time.also { time += 900 }), // Uşşâk nüanslı Segâh
            PitchEvent(perdeName = "Dügâh", komaOffset = 0.0, durationMs = 700L, rmsEnergy = 0.8f, timestamp = time.also { time += 700 }),
            PitchEvent(perdeName = "Rast", komaOffset = 0.0, durationMs = 400L, rmsEnergy = 0.7f, timestamp = time.also { time += 400 }), // Yeden Râst
            PitchEvent(perdeName = "Segâh", komaOffset = -1.5, durationMs = 600L, rmsEnergy = 0.8f, timestamp = time.also { time += 600 }),
            PitchEvent(perdeName = "Dügâh", komaOffset = 0.0, durationMs = 1500L, rmsEnergy = 1.0f, timestamp = time.also { time += 1500 }) // Karar Dügâh
        )

        for (event in karcigarMelody) {
            engine.feedPitch(event)
        }

        val result = engine.analyze()
        assertNotNull("Analiz sonucu boş olmamalıdır", result)
        assertEquals("Uşşâk 4'lüsü ve Nevâ'da Hicaz içeren ezgi Karcığar olarak tespit edilmelidir", "karcigar", result!!.matchedMakam.id)
        assertEquals("Tespit edilen durak Dügâh olmalıdır", "Dügâh", result.detectedDurak)
        assertEquals("Tespit edilen güçlü Nevâ olmalıdır", "Nevâ", result.detectedGuclu)
    }

    @Test
    fun testSuznakMakamDetection() {
        var time = 1000L

        // Basit Sûz'nâk ezgisi:
        // Alt çeşni: Râst perdesinde Râst 5'lisi (Râst, Dügâh, Segâh, Çârgâh, Nevâ)
        // Üst çeşni: Nevâ'da Hicaz 4'lüsü (Nevâ, Hisâr, Şehnâz, Gerdâniye)
        // Karar: Râst perdesi, Yeden: Irak
        val suznakMelody = listOf(
            PitchEvent(perdeName = "Neva", komaOffset = 0.0, durationMs = 1100L, rmsEnergy = 0.95f, timestamp = time.also { time += 1100 }), // Güçlü Nevâ
            PitchEvent(perdeName = "Hisar", komaOffset = 0.0, durationMs = 800L, rmsEnergy = 0.85f, timestamp = time.also { time += 800 }), // Hicaz çeşnisi Hisâr
            PitchEvent(perdeName = "Şehnâz", komaOffset = 0.0, durationMs = 700L, rmsEnergy = 0.85f, timestamp = time.also { time += 700 }), // Hicaz çeşnisi Şehnâz
            PitchEvent(perdeName = "Gerdâniye", komaOffset = 0.0, durationMs = 600L, rmsEnergy = 0.8f, timestamp = time.also { time += 600 }),
            PitchEvent(perdeName = "Hisar", komaOffset = 0.0, durationMs = 600L, rmsEnergy = 0.8f, timestamp = time.also { time += 600 }),
            PitchEvent(perdeName = "Neva", komaOffset = 0.0, durationMs = 1200L, rmsEnergy = 0.95f, timestamp = time.also { time += 1200 }), // Yarım karar
            PitchEvent(perdeName = "Çârgâh", komaOffset = 0.0, durationMs = 600L, rmsEnergy = 0.8f, timestamp = time.also { time += 600 }),
            PitchEvent(perdeName = "Segâh", komaOffset = 0.0, durationMs = 800L, rmsEnergy = 0.85f, timestamp = time.also { time += 800 }), // Râst 5'lisi
            PitchEvent(perdeName = "Dügâh", komaOffset = 0.0, durationMs = 700L, rmsEnergy = 0.8f, timestamp = time.also { time += 700 }),
            PitchEvent(perdeName = "Irak", komaOffset = 0.0, durationMs = 400L, rmsEnergy = 0.75f, timestamp = time.also { time += 400 }), // Yeden Irak
            PitchEvent(perdeName = "Rast", komaOffset = 0.0, durationMs = 1600L, rmsEnergy = 1.0f, timestamp = time.also { time += 1600 }) // Tam Karar Râst
        )

        for (event in suznakMelody) {
            engine.feedPitch(event)
        }

        val result = engine.analyze()
        assertNotNull("Analiz sonucu boş olmamalıdır", result)
        assertEquals("Râst 5'lisi üzerine Nevâ'da Hicaz çeşnisi Sûz'nâk olarak tespit edilmelidir", "basit_suznak", result!!.matchedMakam.id)
        assertEquals("Tespit edilen durak Râst olmalıdır", "Râst", result.detectedDurak)
        assertEquals("Tespit edilen güçlü Nevâ olmalıdır", "Nevâ", result.detectedGuclu)
    }

    @Test
    fun testMakamRegistryCoreMakamsIntegrity() {
        val makams = MakamRegistry.CORE_MAKAMS
        assertEquals(13, makams.size)

        // Tüm makamların 53-EDO Pisagorik aralıklarının toplamı tam 53 koma olmalıdır!
        for (makam in makams) {
            assertEquals("Makam ${makam.name} aralıkları toplamı 53 olmalıdır", 53, makam.scaleIntervals.sum())
        }

        // Yeni makamların özel aralık dizileri
        val humayun = MakamRegistry.getById("humayun")!!
        assertEquals(listOf(5, 12, 5, 9, 4, 9, 9), humayun.scaleIntervals)

        val hicaz = MakamRegistry.getById("hicaz")!!
        assertEquals(listOf(5, 12, 5, 9, 8, 5, 9), hicaz.scaleIntervals)

        val karcigar = MakamRegistry.getById("karcigar")!!
        assertEquals(listOf(8, 5, 9, 9, 5, 12, 5), karcigar.scaleIntervals)

        val suznak = MakamRegistry.getById("basit_suznak")!!
        assertEquals(listOf(9, 8, 5, 9, 5, 12, 5), suznak.scaleIntervals)

        val neva = MakamRegistry.getById("neva")!!
        assertEquals(listOf(8, 5, 9, 9, 8, 5, 9), neva.scaleIntervals)
    }
}
