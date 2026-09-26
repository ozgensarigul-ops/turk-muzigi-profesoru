package com.example.tmtuner.core.musicology.engine

import com.example.tmtuner.core.musicology.model.Ahenk
import com.example.tmtuner.core.musicology.model.SeyirType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * [VAKA-007] Doğrulama Testi:
 * Kız Neyi Âhenginde (Dügâh = Si / B4 ≈ 494-495 Hz) Tatyos Efendi Uşşâk Peşrevi İcrası
 *
 * İsmail Hakkı Özkan "Türk Mûsikîsi Nazariyatı ve Usûlleri" (s. 46-56, 87, 88-94, 143)
 * kurallarına tam uyumlu olarak:
 * 1. Transpozisyon Motoru: Kız Neyi (+9 koma tiz) icrası 9 koma PESTLEŞTİRİLEREK yerindeki
 *    Mansur dizisine (Dügâh = 440 Hz) indirgenmelidir.
 * 2. Hiyerarşik Güçlü: Güçlü perdesi daima durağın tiz tarafında (üzerinde / 4. derece Nevâ)
 *    yer almalı; asla durağın altında (Kaba Çârgâh vb.) bir güçlü atanamaz.
 * 3. Ezgisel Seyir: Eserin yalnızca ilk saniyelerine bakarak "Çıkıcı" denilemez;
 *    orta kısımdaki Nevâ dolaşımı ve teslimdeki Dügâh inişi ile seyir "İnici-Çıkıcı" tespit edilmelidir.
 * 4. Segâh İcra Toleransı: Uşşâk dörtlüsündeki geleneksel 1-2 komalık pest Segâh icrası
 *    [-2.0, +0.5] koma penceresinde güven bonusu ile Uşşâk makamını doğrulamalıdır.
 */
class KizNeyiUssakDetectionTest {

    private lateinit var engine: MakamDetectionEngine

    @Before
    fun setUp() {
        engine = MakamDetectionEngine(ahenk = Ahenk.KIZ_NEYI)
    }

    @Test
    fun testTatyosEfendiUssakPesreviInKizNeyiPitchStream() {
        var time = 1000L

        // Kız Neyi Diyapazonundaki Mutlak Frekanslar (Mansur * 9/8):
        // Dügâh = Si4 ≈ 495.0 Hz
        // Segâh (Geleneksel Uşşâk -1.5 koma pest icrası) ≈ 536.0 Hz
        // Çârgâh = Do#5 ≈ 586.67 Hz (Mansur Neva frekansı)
        // Nevâ (Güçlü) = Mi5 ≈ 660.0 Hz (Mansur Hüseyni frekansı)
        // Hüseynî = Fa#5 ≈ 742.5 Hz
        // Acem = Sol5 ≈ 782.2 Hz (Mansur 695.31 Hz * 9/8)
        // Râst (Yeden) = La4 = 440.0 Hz (Mansur Dügâh frekansı)

        val kizNeyiUssakStream = listOf(
            // --- 1. Giriş Bölümü: Nevâ güçlüsü ve çevresinde başlangıç ---
            Triple(660.0, 900L, 0.95f),  // Nevâ (Güçlü Mi5)
            Triple(742.5, 600L, 0.85f),  // Hüseynî
            Triple(782.2, 500L, 0.85f),  // Acem (695.31 * 9/8)
            Triple(742.5, 600L, 0.80f),  // Hüseynî
            Triple(660.0, 1200L, 0.95f), // Yarım Karar Nevâ (Güçlüde kalış)

            // --- 2. Gelişme Bölümü: Nevâ dolaşımı ve Uşşâk 4'lüsüne geçiş ---
            Triple(660.0, 800L, 0.90f),  // Nevâ
            Triple(586.67, 600L, 0.85f), // Çârgâh
            Triple(536.0, 900L, 0.90f),  // Segâh (Geleneksel -1.5 koma pest icra)
            Triple(586.67, 500L, 0.80f), // Çârgâh
            Triple(660.0, 1100L, 0.95f), // Nevâ (Israrlı güçlü kalışı)
            Triple(742.5, 600L, 0.85f),  // Hüseynî
            Triple(660.0, 700L, 0.85f),  // Nevâ

            // --- 3. Teslim / Karar Yürüyüşü: Tizden Dügâh durağına iniş ---
            Triple(586.67, 600L, 0.80f), // Çârgâh
            Triple(536.0, 800L, 0.85f),  // Segâh (Pest nüans)
            Triple(495.0, 800L, 0.85f),  // Dügâh (Karar yürüyüşü)
            Triple(440.0, 400L, 0.75f),  // Râst (Yeden)
            Triple(536.0, 600L, 0.80f),  // Segâh
            Triple(495.0, 2000L, 1.0f)   // Dügâh (Tam Karar)
        )

        for ((freq, dur, rms) in kizNeyiUssakStream) {
            engine.feedFrequency(
                frequency = freq,
                durationMs = dur,
                rmsEnergy = rms,
                timestamp = time.also { time += dur },
                inputAhenk = Ahenk.KIZ_NEYI
            )
        }

        val result = engine.analyze()

        // 1. Analiz Sonucu Boş Olmamalıdır
        assertNotNull("Kız Neyi analiz sonucu boş olmamalıdır", result)

        // 2. Makam Tespiti: "ussak" / "Uşşâk" (Zîrgûleli Hicaz olmamalıdır!)
        assertEquals(
            "Kız Neyi icrasında makam Uşşâk olmalıdır (Zîrgûleli Hicaz hatası düzeltilmeli)",
            "ussak",
            result!!.matchedMakam.id
        )
        assertEquals("Makam adı Uşşâk olmalıdır", "Uşşâk", result.matchedMakam.name)

        // 3. Durak (Karar) Perdesi: "Dügâh" (Kaba Hicaz olmamalıdır!)
        assertEquals(
            "Karar perdesi Dügâh olmalıdır (Kaba Hicaz hatası düzeltilmeli)",
            "Dügâh",
            result.detectedDurak
        )

        // 4. Güçlü (Yarım Karar) Perdesi: "Nevâ" (Kaba Çârgâh olamaz!)
        assertEquals(
            "Güçlü perdesi 4. derece Nevâ olmalıdır (Durağın altındaki Kaba Çârgâh atanamaz - Özkan s. 88)",
            "Nevâ",
            result.detectedGuclu
        )

        // 5. Ezgisel Seyir Tipi: "İnici-Çıkıcı" (Çıkıcı olmamalıdır!)
        assertEquals(
            "Uşşâk Peşrevi ezgisel seyri İnici-Çıkıcı olmalıdır (Özkan s. 51 & 143)",
            SeyirType.INICI_CIKICI,
            result.detectedSeyir
        )

        // 6. Güven Katsayısı Yüksek Olmalıdır
        assertTrue(
            "Uşşâk güven katsayısı %80 üzerinde olmalıdır (Gerçekleşen: ${result.confidence})",
            result.confidence >= 0.80f
        )
    }

    @Test
    fun testDurakGucluHierarchyEnforcement() {
        // Güçlü durağın altında çıkamaz test senaryosu:
        // Karar Dügâh olduğunda güçlü durağın tiz tarafında (Nevâ veya Hüseynî) olmalıdır.
        val stream = listOf(
            Triple(660.0, 1000L, 0.9f), // Nevâ (Mi5)
            Triple(495.0, 1000L, 0.9f)  // Dügâh (Si4)
        )
        for ((freq, dur, rms) in stream) {
            engine.feedFrequency(freq, dur, rms, inputAhenk = Ahenk.KIZ_NEYI)
        }
        val result = engine.analyze()
        assertNotNull(result)
        assertEquals("Dügâh", result!!.detectedDurak)
        assertEquals("Nevâ", result.detectedGuclu)
    }

    @Test
    fun testKizNeyiRawStreamWithHarmonicsAndBreathNoise() {
        // Fiziksel cihaz simülasyonu:
        // Nefes üfleme anında 1648 Hz (3. harmonik) ve geçişlerde 270 Hz (anlık nefes kaçağı/dip)
        // içeren ham sinyal akışının doğru filtrelenerek Uşşâk makamına ulaşması.
        val rawStreamWithNoise = listOf(
            Triple(660.0, 900L, 0.95f),  // Nevâ (Güçlü Mi5)
            Triple(742.5, 600L, 0.85f),  // Hüseynî
            Triple(782.2, 500L, 0.85f),  // Acem
            Triple(742.5, 600L, 0.80f),  // Hüseynî
            Triple(660.0, 1000L, 0.95f), // Nevâ
            Triple(270.0, 80L, 0.15f),   // Anlık nefes kaçağı / dip gürültü (270 Hz)
            Triple(586.67, 600L, 0.85f), // Çârgâh
            Triple(1648.0, 100L, 0.90f), // Segâh üflemesinde 3. harmonik sıçraması (1648 Hz)
            Triple(536.0, 800L, 0.90f),  // Segâh (Geleneksel pest icra)
            Triple(660.0, 1000L, 0.95f), // Nevâ
            Triple(586.67, 500L, 0.80f), // Çârgâh
            Triple(536.0, 700L, 0.85f),  // Segâh
            Triple(495.0, 800L, 0.90f),  // Dügâh
            Triple(440.0, 400L, 0.75f),  // Râst (Yeden)
            Triple(495.0, 2000L, 1.0f)   // Dügâh (Tam Karar)
        )

        var time = 1000L
        for ((freq, dur, rms) in rawStreamWithNoise) {
            engine.feedFrequency(
                frequency = freq,
                durationMs = dur,
                rmsEnergy = rms,
                timestamp = time.also { time += dur },
                inputAhenk = Ahenk.KIZ_NEYI
            )
        }

        val result = engine.analyze()
        assertNotNull("Sonuç boş olmamalıdır", result)
        assertEquals("Makam Uşşâk tespit edilmelidir", "ussak", result!!.matchedMakam.id)
        assertEquals("Durak Dügâh olmalıdır", "Dügâh", result.detectedDurak)
        assertEquals("Güçlü Nevâ olmalıdır", "Nevâ", result.detectedGuclu)
        assertEquals("Seyir İnici-Çıkıcı olmalıdır", SeyirType.INICI_CIKICI, result.detectedSeyir)
        assertTrue("Güven skoru >= 0.80 olmalıdır", result.confidence >= 0.80f)
    }
}
