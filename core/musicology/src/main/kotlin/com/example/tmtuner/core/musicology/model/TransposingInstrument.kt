package com.example.tmtuner.core.musicology.model

/**
 * Batı nefeslileri ve orkestra enstrümanları için transpozisyon sınıfları.
 *
 * Enstrüman Perdesi (Written Pitch): İcracının notada gördüğü ve parmak bastığı perde.
 * Konsert Perdesi (Sounding Pitch): Akustik olarak ortamda tınlayan ve mikrofonun algıladığı mutlak ses.
 */
enum class TransposingInstrument(
    val displayName: String,
    val keyNote: String,
    val komaOffsetFromConcert: Int, // Konsert sesinden enstrüman yazılı sesine geçerken 53-EDO koma farkı
    val description: String
) {
    CONCERT_C(
        displayName = "Konsert (Do) Sazları",
        keyNote = "C",
        komaOffsetFromConcert = 0,
        description = "Ney, Ud, Kanun, Keman, Flüt, Piyano. Duyulan ses ile yazılı perde birebir aynıdır."
    ) {
        override fun toConcertPitch(instrumentPitch: Double, asMinorThird: Boolean): Double = instrumentPitch
        override fun toInstrumentPitch(concertPitch: Double, asMinorThird: Boolean): Double = concertPitch
    },

    EB_ALTO_SAX(
        displayName = "Alto Saksafon (Eb)",
        keyNote = "Eb",
        komaOffsetFromConcert = 40, // Büyük 6'lı tiz (40 koma: Konsert -> Yazılı perdesi)
        description = "Eb Alto Saksafon (büyük 6'lı pes transpoze). Yazılı Do üflendiğinde konsert Mib tınlar (16/27)."
    ) {
        /**
         * İcracının üflediği yazılı frekanstan akustik konsert frekansına dönüşüm:
         * - asMinorThird = false (Varsayılan): Tınlayan oktavda büyük 6'lı pest (x 16/27)
         * - asMinorThird = true: Aynı oktavda küçük 3'lü tiz (x 32/27)
         */
        override fun toConcertPitch(instrumentPitch: Double, asMinorThird: Boolean): Double {
            return if (asMinorThird) {
                instrumentPitch * (32.0 / 27.0)
            } else {
                instrumentPitch * (16.0 / 27.0)
            }
        }

        /**
         * Akustik konsert frekansından icracının bastığı yazılı perdeye dönüşüm:
         * - asMinorThird = false (Varsayılan): Büyük 6'lı tiz (x 27/16 = 1.6875)
         * - asMinorThird = true: Küçük 3'lü pest (x 27/32)
         */
        override fun toInstrumentPitch(concertPitch: Double, asMinorThird: Boolean): Double {
            return if (asMinorThird) {
                concertPitch * (27.0 / 32.0)
            } else {
                concertPitch * (27.0 / 16.0)
            }
        }
    },

    BB_TENOR_SAX(
        displayName = "Tenor Saksafon (Bb)",
        keyNote = "Bb",
        komaOffsetFromConcert = 9, // Tanini (9 koma: Konsert -> Yazılı)
        description = "Bb Tenor Saksafon (büyük ikili pest transpoze). Yazılı Do üflendiğinde konsert Sib tınlar."
    ) {
        /**
         * Yazılı Do -> Konsert Sib (Tanini pest: x 8/9 veya oktavlı x 4/9)
         */
        override fun toConcertPitch(instrumentPitch: Double, asMinorThird: Boolean): Double {
            return instrumentPitch * (8.0 / 9.0)
        }

        /**
         * Konsert Sib -> Yazılı Do (Tanini tiz: x 9/8 = 1.125)
         */
        override fun toInstrumentPitch(concertPitch: Double, asMinorThird: Boolean): Double {
            return concertPitch * (9.0 / 8.0)
        }
    },

    BB_SOPRANO_SAX_CLARINET(
        displayName = "Soprano Saksafon & Bb Klarnet",
        keyNote = "Bb",
        komaOffsetFromConcert = 9,
        description = "Bb Soprano Saksafon, Bb Klarnet ve Trompet. Tanini (9 koma) pest transpoze."
    ) {
        override fun toConcertPitch(instrumentPitch: Double, asMinorThird: Boolean): Double {
            return instrumentPitch * (8.0 / 9.0)
        }

        override fun toInstrumentPitch(concertPitch: Double, asMinorThird: Boolean): Double {
            return concertPitch * (9.0 / 8.0)
        }
    };

    abstract fun toConcertPitch(instrumentPitch: Double, asMinorThird: Boolean = false): Double
    abstract fun toInstrumentPitch(concertPitch: Double, asMinorThird: Boolean = false): Double

    companion object {
        const val EB_ALTO_WRITTEN_TO_CONCERT: Double = 16.0 / 27.0
        const val EB_ALTO_CONCERT_TO_WRITTEN: Double = 27.0 / 16.0

        fun fromDisplayName(name: String): TransposingInstrument {
            return when {
                name.contains("Alto", ignoreCase = true) || name.contains("Eb", ignoreCase = true) -> EB_ALTO_SAX
                name.contains("Tenor", ignoreCase = true) -> BB_TENOR_SAX
                name.contains("Soprano", ignoreCase = true) || name.contains("Klarnet", ignoreCase = true) || name.contains("Bb", ignoreCase = true) -> BB_SOPRANO_SAX_CLARINET
                else -> CONCERT_C
            }
        }
    }
}
