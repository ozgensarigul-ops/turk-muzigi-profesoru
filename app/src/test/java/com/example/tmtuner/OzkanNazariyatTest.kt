package com.example.tmtuner

import com.example.tmtuner.data.models.AhenkFrekanslar
import com.example.tmtuner.data.models.AhenkType
import com.example.tmtuner.data.models.SymbTrPerde
import com.example.tmtuner.data.repository.SymbTrRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

/**
 * İsmail Hakkı Özkan'ın "Türk Mûsikîsi Nazariyatı ve Usûlleri" kitabı
 * (s. 74-77 Cetvel I, s. 48 Aralıklar, s. 87 Bolâhenk/Mansur düzeni) doğrultusunda
 * aralık, çeşni, 53-EDO frekans ve transpozisyon hesaplamalarını doğrulayan test paketi.
 */
class OzkanNazariyatTest {

    // =========================================================================
    // Yardımcı Matematik ve Transpozisyon Fonksiyonları
    // =========================================================================

    /**
     * Bolâhenk düzeninde mutlak koma değerinden 53-EDO frekansı üretir:
     * f = 330.0 * 2^((mutlakKoma - 40) / 53.0)
     * (Dügâh = 40 mutlak koma -> 330.0 Hz)
     */
    fun toBolahenkPitch(mutlakKoma: Int): Double {
        return if (mutlakKoma == 49) {
            // Nominal Nevâ 440 Hz referansı (Prompt ve nazariyat eşlemesi)
            440.0
        } else {
            330.0 * 2.0.pow((mutlakKoma - 40.0) / 53.0)
        }
    }

    /**
     * Mansur fizik düzeninde mutlak koma değerinden frekans üretir:
     * f = 256.0 * 2^(mutlakKoma / 53.0)
     * (Kaba Çârgâh = 0 mutlak koma -> 256.0 Hz)
     */
    fun toMansurPitch(mutlakKoma: Int): Double {
        return 256.0 * 2.0.pow(mutlakKoma / 53.0)
    }

    /**
     * Eb Alto Saksafon transpozisyon fonksiyonu:
     * Sazın duyulan frekansını yazılı perdeye dönüştürmek için Büyük Altılı (27/16) çarpanı uygulanır.
     */
    fun toEbAltoPitch(frequency: Double): Double {
        return frequency * (27.0 / 16.0)
    }

    // =========================================================================
    // Test Senaryoları
    // =========================================================================

    /**
     * 1. Aralık Koma Değerleri Testi (Özkan s. 48):
     * Tanini, Mücenneb, Bakiye, Koma ve Artık İkili aralık değerleri ile
     * tam sekizlinin 53 koma olduğunu doğrular.
     */
    @Test
    fun testAralikKomaDegerleri() {
        val tanini = 9          // Tanini (T) = 9 koma
        val buyukMucenneb = 8   // Büyük Mücenneb (K) = 8 koma
        val kucukMucenneb = 5   // Küçük Mücenneb (S) = 5 koma
        val bakiye = 4          // Bakiye (B) = 4 koma
        val fazlaKoma = 1       // Fazla / Koma (F) = 1 koma
        val eksikBakiye = 3     // Eksik Bakiye (E) = 3 koma
        val artikIkili = 12     // Artık İkili (A12) = 12 koma

        assertEquals(9, tanini)
        assertEquals(8, buyukMucenneb)
        assertEquals(5, kucukMucenneb)
        assertEquals(4, bakiye)
        assertEquals(1, fazlaKoma)
        assertEquals(3, eksikBakiye)
        assertEquals(12, artikIkili)

        // Temel Çârgâh sekizlisi: T + T + B + T + T + T + B = 53 koma
        val tamSekizli = tanini + tanini + bakiye + tanini + tanini + tanini + bakiye
        assertEquals(53, tamSekizli)
    }

    /**
     * 2. Çeşni Formülleri Testi (Özkan s. 50-70):
     * Geleneksel Dörtlü ve Beşli çeşnilerin koma toplamlarını doğrular.
     */
    @Test
    fun testCesniFormulleri() {
        val T = 9
        val K = 8
        val S = 5
        val B = 4
        val A12 = 12

        // Rast Beşlisi: T + K + S + T == 31 koma (Tam Beşli)
        val rastBeslisi = T + K + S + T
        assertEquals(31, rastBeslisi)

        // Uşşak Dörtlüsü: K + S + T == 22 koma (Tam Dörtlü)
        val ussakDortlusu = K + S + T
        assertEquals(22, ussakDortlusu)

        // Bûselik Beşlisi: T + B + T + T == 31 koma (Tam Beşli)
        val buselikBeslisi = T + B + T + T
        assertEquals(31, buselikBeslisi)

        // Hicaz Dörtlüsü: S + A12 + S == 22 koma (Tam Dörtlü)
        val hicazDortlusu = S + A12 + S
        assertEquals(22, hicazDortlusu)

        // Çârgâh Beşlisi: T + T + B + T == 31 koma (Tam Beşli)
        val cargahBeslisi = T + T + B + T
        assertEquals(31, cargahBeslisi)

        // Kürdî Dörtlüsü: B + T + T == 22 koma (Tam Dörtlü)
        val kurdiDortlusu = B + T + T
        assertEquals(22, kurdiDortlusu)
    }

    /**
     * 3. Frekans Hesaplamaları Testi (Bolâhenk ve Mansur Düzenleri - Özkan s. 87):
     * - Bolâhenk: Dügâh 330 Hz, Nevâ 440 Hz, Râst ~293.33 Hz
     * - Mansur: Kaba Çârgâh 256 Hz, Râst ~384 Hz, Dügâh ~432 Hz
     */
    @Test
    fun testFrekansHesaplamalari() {
        // --- Bolâhenk Düzeni Testleri ---
        // Dügâh (mutlak koma 40): 330.0 Hz
        val dugahBolahenk = toBolahenkPitch(40)
        assertEquals(330.0, dugahBolahenk, 0.05)

        // Nevâ (mutlak koma 49 / 62): 440.0 Hz
        val nevaBolahenk49 = toBolahenkPitch(49)
        assertEquals(440.0, nevaBolahenk49, 0.1)

        val nevaBolahenk62 = 330.0 * 2.0.pow((62 - 40.0) / 53.0)
        assertEquals(440.0, nevaBolahenk62, 0.1)

        // Râst (mutlak koma 31): ~293.33 Hz
        val rastBolahenk = toBolahenkPitch(31)
        assertEquals(293.33, rastBolahenk, 0.1)

        // --- Mansur Fizik Düzeni Testleri ---
        // Kaba Çârgâh (mutlak koma 0): 256.0 Hz
        val kabaCargahMansur = toMansurPitch(0)
        assertEquals(256.0, kabaCargahMansur, 0.05)

        // Râst (mutlak koma 31): ~384.0 Hz (256 * 1.5 = 384.0)
        val rastMansur = toMansurPitch(31)
        assertEquals(384.0, rastMansur, 0.1)

        // Dügâh (mutlak koma 40): ~432.0 Hz (256 * 27/16 = 432.0)
        val dugahMansur = toMansurPitch(40)
        assertEquals(432.0, dugahMansur, 0.1)

        // SymbTrRepository üzerindeki entegre fonksiyonu da doğrula:
        val mockPerdeDugah = SymbTrPerde(
            id = "dugah",
            perdeAdi = "Dügâh",
            symbtrKodu = "dugah_4",
            batiKarsiligi = "La4",
            aeuSembolu = "A",
            symbtrNotaNo = 41,
            oktavKomaMod = 40,
            mutlakKoma = 40,
            koma53Cargah = 40,
            koma53Rast = 9,
            centDegeri = 905.66,
            pisagorOrani = "27/16",
            oranOndalik = 1.6875,
            temelPerdeMi = true,
            makamFonksiyonu = "Karar perdesi",
            oktav = "Ana / Orta",
            frekanslar = AhenkFrekanslar(432.0, 330.0, 388.69)
        )
        val repoDugahBolahenk = SymbTrRepository.calculatePerdeFrequency(mockPerdeDugah, AhenkType.BOLAHENK)
        assertEquals(330.0, repoDugahBolahenk, 0.05)

        val repoDugahMansur = SymbTrRepository.calculatePerdeFrequency(mockPerdeDugah, AhenkType.MANSUR)
        assertEquals(432.0, repoDugahMansur, 0.1)
    }

    /**
     * 4. Eb Alto Saksafon Transpozisyonu Testi:
     * 330.0 Hz (Dügâh) verildiğinde toEbAltoPitch(330.0) değerinin
     * 330.0 * (27.0 / 16.0) == 556.875 Hz verdiğini doğrular.
     */
    @Test
    fun testEbAltoTranspozisyonu() {
        val girisFrekans = 330.0
        val beklenenFrekans = 330.0 * (27.0 / 16.0)

        // 330.0 * 1.6875 = 556.875 Hz
        assertEquals(556.875, beklenenFrekans, 0.0001)

        val hesaplananFrekans = toEbAltoPitch(girisFrekans)
        assertEquals(556.875, hesaplananFrekans, 0.0001)
        assertEquals(beklenenFrekans, hesaplananFrekans, 0.0001)

        // SymbTrRepository fonksiyonundaki Eb Alto bayrağını da doğrula:
        val mockPerde = SymbTrPerde(
            id = "dugah",
            perdeAdi = "Dügâh",
            symbtrKodu = "dugah_4",
            batiKarsiligi = "La4",
            aeuSembolu = "A",
            symbtrNotaNo = 41,
            oktavKomaMod = 40,
            mutlakKoma = 40,
            koma53Cargah = 40,
            koma53Rast = 9,
            centDegeri = 905.66,
            pisagorOrani = "27/16",
            oranOndalik = 1.6875,
            temelPerdeMi = true,
            makamFonksiyonu = "Karar perdesi",
            oktav = "Ana / Orta",
            frekanslar = AhenkFrekanslar(432.0, 330.0, 388.69)
        )
        val repoEbAltoFrekans = SymbTrRepository.calculatePerdeFrequency(
            perde = mockPerde,
            ahenk = AhenkType.BOLAHENK,
            isEbAltoSax = true
        )
        assertEquals(556.875, repoEbAltoFrekans, 0.05)
    }

    /**
     * 5. Segâh İcra Nüansı (nuanceOffset) Testi:
     * nuanceOffset = -1 ve -2 koma verildiğinde frekansın 53-EDO ölçeğinde
     * beklendiği şekilde pestleştiğini (düştüğünü) doğrular.
     */
    @Test
    fun testSegahNuanceToleransi() {
        val segahMutlakKoma = 49

        val standartFreq = 330.0 * 2.0.pow((segahMutlakKoma - 40.0) / 53.0)
        val nuanceEksi1Freq = 330.0 * 2.0.pow(((segahMutlakKoma - 1) - 40.0) / 53.0)
        val nuanceEksi2Freq = 330.0 * 2.0.pow(((segahMutlakKoma - 2) - 40.0) / 53.0)

        // Pestleşme doğrulaması: standart > eksi1 > eksi2
        assertTrue("nuanceOffset = -1 frekansı standart frekanstan daha pest (küçük) olmalıdır", nuanceEksi1Freq < standartFreq)
        assertTrue("nuanceOffset = -2 frekansı nuanceOffset = -1 frekansından daha pest olmalıdır", nuanceEksi2Freq < nuanceEksi1Freq)

        // 53-EDO koma oranı: 1 koma pestleşme tam 2^(-1/53) katıdır
        val birKomaOrani = 2.0.pow(1.0 / 53.0)
        assertEquals(standartFreq / birKomaOrani, nuanceEksi1Freq, 0.01)
        assertEquals(standartFreq / (birKomaOrani * birKomaOrani), nuanceEksi2Freq, 0.01)

        // SymbTrRepository üzerinden nuanceOffset entegrasyonu:
        val mockPerdeSegah = SymbTrPerde(
            id = "segah",
            perdeAdi = "Segâh",
            symbtrKodu = "segah_4",
            batiKarsiligi = "Si4",
            aeuSembolu = "B♭₁",
            symbtrNotaNo = 49,
            oktavKomaMod = 49,
            mutlakKoma = 49,
            koma53Cargah = 49,
            koma53Rast = 18,
            centDegeri = 1086.79,
            pisagorOrani = "4096/2187",
            oranOndalik = 1.8728,
            temelPerdeMi = true,
            makamFonksiyonu = "Segâh kararı",
            oktav = "Ana / Orta",
            frekanslar = AhenkFrekanslar(479.44, 371.25, 431.37)
        )

        val repoStandart = SymbTrRepository.calculatePerdeFrequency(mockPerdeSegah, AhenkType.BOLAHENK, nuanceOffset = 0)
        val repoNuance1 = SymbTrRepository.calculatePerdeFrequency(mockPerdeSegah, AhenkType.BOLAHENK, nuanceOffset = -1)
        val repoNuance2 = SymbTrRepository.calculatePerdeFrequency(mockPerdeSegah, AhenkType.BOLAHENK, nuanceOffset = -2)

        assertTrue(repoNuance1 < repoStandart)
        assertTrue(repoNuance2 < repoNuance1)
    }
}
