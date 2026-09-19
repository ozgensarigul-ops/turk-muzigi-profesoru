package com.example.tmtuner.core.musicology

import com.example.tmtuner.core.musicology.math.Edo53Calculator
import com.example.tmtuner.core.musicology.model.TranspositionMode
import org.junit.Assert.assertEquals
import org.junit.Test

class TranspositionTest {

    @Test
    fun testEbAltoSaxTransposition() {
        val concertPitch = 330.0 // Konsert Dügâh (La)

        // 1. Konsert sesinden enstrümanın yazılı sesine (toInstrumentPitch - büyük 6'lı tiz: x 27/16)
        val writtenPitch = TranspositionMode.EB_ALTO_SAX.toInstrumentPitch(concertPitch, asMinorThird = false)
        assertEquals(330.0 * (27.0 / 16.0), writtenPitch, 0.0001)
        assertEquals(556.875, writtenPitch, 0.0001)

        // 2. Enstrümanın yazılı sesinden tınlayan konsert sesine (toConcertPitch - büyük 6'lı pest: x 16/27)
        val soundingConcert = TranspositionMode.EB_ALTO_SAX.toConcertPitch(writtenPitch, asMinorThird = false)
        assertEquals(330.0, soundingConcert, 0.0001)

        // 3. Küçük 3'lü tiz yönü (asMinorThird = true: x 32/27)
        val soundingMinorThird = TranspositionMode.EB_ALTO_SAX.toConcertPitch(writtenPitch, asMinorThird = true)
        assertEquals(writtenPitch * (32.0 / 27.0), soundingMinorThird, 0.0001)

        // 4. Edo53Calculator yardımcı fonksiyon doğrulaması
        val calculated = Edo53Calculator.toEbAltoPitch(concertPitch)
        assertEquals(556.875, calculated, 0.0001)
    }

    @Test
    fun testBbInstrumentsTransposition() {
        val concertPitch = 330.0 // Konsert Dügâh (La)

        // 1. Konsert sesinden enstrümanın yazılı sesine (toInstrumentPitch - 1 tam ses tiz: x 9/8)
        val writtenPitch = TranspositionMode.BB_INSTRUMENTS.toInstrumentPitch(concertPitch)
        val expectedWritten = 330.0 * (9.0 / 8.0) // 371.25 Hz
        assertEquals(expectedWritten, writtenPitch, 0.0001)

        // 2. Enstrümanın yazılı sesinden tınlayan konsert sesine (toConcertPitch - 1 tam ses pest: x 8/9)
        val soundingConcert = TranspositionMode.BB_INSTRUMENTS.toConcertPitch(writtenPitch)
        assertEquals(concertPitch, soundingConcert, 0.0001)

        // 3. Edo53Calculator yardımcı fonksiyon doğrulaması
        val calculated = Edo53Calculator.toBbPitch(concertPitch)
        assertEquals(expectedWritten, calculated, 0.0001)
    }

    @Test
    fun testConcertMode() {
        val pitch = 440.0
        assertEquals(pitch, TranspositionMode.CONCERT.toConcertPitch(pitch), 0.0001)
        assertEquals(pitch, TranspositionMode.CONCERT.toInstrumentPitch(pitch), 0.0001)
    }
}
