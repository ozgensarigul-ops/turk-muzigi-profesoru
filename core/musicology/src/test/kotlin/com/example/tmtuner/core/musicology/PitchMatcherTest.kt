package com.example.tmtuner.core.musicology

import com.example.tmtuner.core.musicology.atlas.AeuScaleAtlas
import com.example.tmtuner.core.musicology.atlas.PitchMatcher
import com.example.tmtuner.core.musicology.model.Ahenk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PitchMatcherTest {

    @Test
    fun testAtlasGeneration() {
        val atlasMansur = AeuScaleAtlas.buildPitchAtlas(Ahenk.MANSUR)
        assertEquals(72, atlasMansur.size)

        // 3 oktav: 0..23 (Kaba), 24..47 (Ana), 48..71 (Tîz)
        assertEquals("Kaba Çârgâh", atlasMansur[0].name)
        assertEquals("Çârgâh", atlasMansur[24].name)
        assertEquals("Tîz Çârgâh", atlasMansur[48].name)

        // Temel perdeler
        val fundamentals = atlasMansur.filter { it.isFundamental }
        assertTrue(fundamentals.isNotEmpty())
    }

    @Test
    fun testExactPitchMatching() {
        val atlas = AeuScaleAtlas.buildPitchAtlas(Ahenk.MANSUR)
        val cargahNote = atlas.first { it.name == "Çârgâh" }

        val match = PitchMatcher.matchPitch(cargahNote.frequency, atlas)
        assertNotNull(match)
        assertEquals("Çârgâh", match!!.matchedNote.name)
        assertEquals(0.0f, match.centsOffset, 0.05f)
        assertEquals(0.0f, match.komaOffset, 0.05f)
        assertTrue(match.isInTune)
        assertEquals("Tam İsabet", match.feedbackText)
    }

    @Test
    fun testSharpPitchFeedback() {
        val atlas = AeuScaleAtlas.buildPitchAtlas(Ahenk.MANSUR)
        val dugahNote = atlas.first { it.name == "Dügâh" }
        // 2 koma dik frekans verelim: f * 2^(2/53)
        val sharpFreq = dugahNote.frequency * Math.pow(2.0, 2.0 / 53.0)

        val match = PitchMatcher.matchPitch(sharpFreq, atlas)
        assertNotNull(match)
        assertEquals("Dügâh", match!!.matchedNote.name)
        assertEquals(2.0f, match.komaOffset, 0.1f)
        assertEquals("+2 Koma (Dik)", match.feedbackText)
    }

    @Test
    fun testDroneCalculations() {
        val (noteNameRast, freqRast) = AeuScaleAtlas.calculateDroneFrequency("Rast", Ahenk.MANSUR)
        assertEquals("Râst (Sol)", noteNameRast)
        assertEquals(293.33, freqRast, 0.1)

        val (noteNameDugah, freqDugah) = AeuScaleAtlas.calculateDroneFrequency("Uşşak", Ahenk.MANSUR)
        assertEquals("Dügâh (La)", noteNameDugah)
        assertEquals(330.0, freqDugah, 0.1)
    }
}
