package com.example.tmtuner.core.musicology

import com.example.tmtuner.core.musicology.analysis.MakamClassifierEngine
import com.example.tmtuner.core.musicology.model.Cesni
import com.example.tmtuner.core.musicology.model.Interval
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CesniMakamTest {

    @Test
    fun testAralikKomaDegerleri() {
        assertEquals(9, Interval.TANINI.komaValue)
        assertEquals(8, Interval.BUYUK_MUCENNEP.komaValue)
        assertEquals(5, Interval.KUCUK_MUCENNEP.komaValue)
        assertEquals(4, Interval.BAKIYE.komaValue)
        assertEquals(1, Interval.FAZLA_KOMA.komaValue)
        assertEquals(3, Interval.EKSIK_BAKIYE.komaValue)
        assertEquals(12, Interval.ARTIK_IKILI_12.komaValue)
        assertEquals(13, Interval.ARTIK_IKILI_13.komaValue)

        // Çârgâh sekizlisi: T + T + B + T + T + T + B = 53 koma
        val sekizli = Interval.TANINI.komaValue +
                Interval.TANINI.komaValue +
                Interval.BAKIYE.komaValue +
                Interval.TANINI.komaValue +
                Interval.TANINI.komaValue +
                Interval.TANINI.komaValue +
                Interval.BAKIYE.komaValue
        assertEquals(53, sekizli)
    }

    @Test
    fun testCesniToplamlari() {
        // Rast Beşlisi: T + K + S + T = 31 koma (Tam Beşli)
        assertEquals(31, Cesni.RAST_BESLISI.totalKoma)
        assertEquals("T-K-S-T", Cesni.RAST_BESLISI.formulaString)
        assertTrue(Cesni.RAST_BESLISI.isPentachord)

        // Uşşak Dörtlüsü: K + S + T = 22 koma (Tam Dörtlü)
        assertEquals(22, Cesni.USSAK_DORTLUSU.totalKoma)
        assertEquals("K-S-T", Cesni.USSAK_DORTLUSU.formulaString)

        // Bûselik Beşlisi: T + B + T + T = 31 koma (Tam Beşli)
        assertEquals(31, Cesni.BUSELIK_BESLISI.totalKoma)
        assertEquals("T-B-T-T", Cesni.BUSELIK_BESLISI.formulaString)

        // Hicaz Dörtlüsü: S + A12 + S = 22 koma (Tam Dörtlü)
        assertEquals(22, Cesni.HICAZ_DORTLUSU.totalKoma)
        assertEquals("S-A12-S", Cesni.HICAZ_DORTLUSU.formulaString)

        // Hüseynî Beşlisi: K + S + T + T = 31 koma (Tam Beşli)
        assertEquals(31, Cesni.HUSEYNI_BESLISI.totalKoma)

        // Çârgâh Beşlisi: T + T + B + T = 31 koma (Tam Beşli)
        assertEquals(31, Cesni.CARGAH_BESLISI.totalKoma)

        // Kürdî Dörtlüsü: B + T + T = 22 koma (Tam Dörtlü)
        assertEquals(22, Cesni.KURDI_DORTLUSU.totalKoma)

        // Segâh Beşlisi: S + T + T + K = 31 koma (Tam Beşli)
        assertEquals(31, Cesni.SEGAH_BESLISI.totalKoma)
        assertEquals("S-T-T-K", Cesni.SEGAH_BESLISI.formulaString)
        assertTrue(Cesni.SEGAH_BESLISI.isPentachord)
    }

    @Test
    fun testRastClassification() {
        val referenceVectors = MakamClassifierEngine.createDefaultReferenceVectors()

        // Rast dizisi: Rast (0), Dügâh (9), Segâh (17), Çârgâh (22), Neva (31)
        val rastHistogram = FloatArray(53)
        rastHistogram[0] += 5.0f // Durak
        rastHistogram[31] += 3.0f // Güçlü
        rastHistogram[9] += 2.0f
        rastHistogram[17] += 2.0f
        rastHistogram[22] += 2.0f

        val result = MakamClassifierEngine.classify(rastHistogram, referenceVectors)
        assertEquals("Rast", result.makamName)
        assertTrue(result.confidence > 0.8f)
        assertTrue(result.detectedCesni.contains("Râst Beşlisi"))
    }

    @Test
    fun testUssakWithPestSegahNuanceOffset() {
        val referenceVectors = MakamClassifierEngine.createDefaultReferenceVectors()

        // Uşşak icrasında Segâh perdesi (17) yerine -1 koma (16) veya -2 koma (15) basıldığında
        val ussakPestHistogram = FloatArray(53)
        ussakPestHistogram[9] += 5.0f  // Durak Dügâh
        ussakPestHistogram[31] += 3.0f // Güçlü Nevâ
        ussakPestHistogram[16] += 2.5f // Segâh perdesi -1 koma pest icra edildi
        ussakPestHistogram[22] += 2.0f // Çârgâh
        ussakPestHistogram[40] += 1.5f // Hüseynî

        val resultMinus1 = MakamClassifierEngine.classify(ussakPestHistogram, referenceVectors, segahNuanceOffset = -1)
        assertEquals("Uşşak", resultMinus1.makamName)
        assertTrue(resultMinus1.confidence > 0.7f)
        assertTrue(resultMinus1.detectedCesni.contains("Uşşâk Dörtlüsü"))

        // -2 koma pest icra toleransı testi
        val ussakPestMinus2 = FloatArray(53)
        ussakPestMinus2[9] += 5.0f
        ussakPestMinus2[31] += 3.0f
        ussakPestMinus2[15] += 2.5f // Segâh perdesi -2 koma pest icra edildi
        ussakPestMinus2[22] += 2.0f

        val resultMinus2 = MakamClassifierEngine.classify(ussakPestMinus2, referenceVectors, segahNuanceOffset = -2)
        assertEquals("Uşşak", resultMinus2.makamName)
        assertTrue(resultMinus2.confidence > 0.7f)
    }

    @Test
    fun testSegahWithPestTolerance() {
        val referenceVectors = MakamClassifierEngine.createDefaultReferenceVectors()

        // Segâh icrasında Durak Segâh (17) perdesi -1 koma pest (16) icra edildiğinde
        val segahPestHistogram = FloatArray(53)
        segahPestHistogram[16] += 5.0f // Durak Segâh (-1 koma pest)
        segahPestHistogram[31] += 3.0f // Güçlü Nevâ
        segahPestHistogram[22] += 2.0f // Çârgâh
        segahPestHistogram[40] += 2.0f // Hüseynî
        segahPestHistogram[48] += 2.0f // Eviç

        val result = MakamClassifierEngine.classify(segahPestHistogram, referenceVectors, segahNuanceOffset = -1)
        assertEquals("Segâh", result.makamName)
        assertTrue(result.confidence > 0.7f)
        assertTrue(result.detectedCesni.contains("Segâh Beşlisi"))
    }
}
