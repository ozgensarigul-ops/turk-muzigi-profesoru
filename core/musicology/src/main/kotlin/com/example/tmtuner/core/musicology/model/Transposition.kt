package com.example.tmtuner.core.musicology.model

/**
 * Batı nefeslileri ve transpoze sazlar için Türk makam müziği transpozisyon modları.
 *
 * Enstrüman Perdesi (Instrument / Written Pitch): İcracının notada okuduğu / parmak bastığı perde.
 * Konsert Perdesi (Concert / Sounding Pitch): Havada tınlayan, mikrofonun işittiği mutlak perde.
 */
enum class TranspositionMode(
    val displayName: String,
    val description: String
) {
    CONCERT(
        displayName = "Konsert (Do)",
        description = "Doğal perde. Duyulan frekans doğrudan Türk müziği perdesi ile eşleşir."
    ) {
        override fun toConcertPitch(instrumentPitch: Double, asMinorThird: Boolean): Double = instrumentPitch
        override fun toInstrumentPitch(concertPitch: Double, asMinorThird: Boolean): Double = concertPitch
    },

    BB_INSTRUMENTS(
        displayName = "Tenor / Soprano Saksafon (Bb)",
        description = "Tanini (Yazılı Do -> Konsert Sib). Tenor/Soprano Saksafon, Bb Klarnet ve Trompet için."
    ) {
        // Yazılı Do, tınlayan Sib'ye dönüşür (1 tam ses / Tanini 9/8 pestleşir: x 8/9)
        override fun toConcertPitch(instrumentPitch: Double, asMinorThird: Boolean): Double = instrumentPitch * (8.0 / 9.0)
        // Duyulan Sib'yi icracının bastığı Do'ya çevirmek için 1 tam ses (9/8) tizleştirir
        override fun toInstrumentPitch(concertPitch: Double, asMinorThird: Boolean): Double = concertPitch * (9.0 / 8.0)
    },

    EB_ALTO_SAX(
        displayName = "Alto Saksafon (Eb)",
        description = "Yazılı Do -> Konsert Mib (Küçük 3'lü tiz: x32/27 veya tınlayan oktavda Büyük 6'lı pest: x16/27)."
    ) {
        // Yazılı Do -> Konsert Mib:
        // - asMinorThird = true: Aynı oktavda küçük 3'lü tiz (x 32/27)
        // - asMinorThird = false (varsayılan): Tınlayan oktavda büyük 6'lı pest (x 16/27)
        override fun toConcertPitch(instrumentPitch: Double, asMinorThird: Boolean): Double {
            return if (asMinorThird) {
                instrumentPitch * (32.0 / 27.0)
            } else {
                instrumentPitch * (16.0 / 27.0)
            }
        }

        // Duyulan Konsert sesini Alto Saksafon icracısının parmak pozisyonuna (Yazılı Do) çevirir:
        // - asMinorThird = true: Küçük 3'lü pest (x 27/32)
        // - asMinorThird = false (varsayılan): Büyük 6'lı tiz (x 27/16 = 1.6875)
        override fun toInstrumentPitch(concertPitch: Double, asMinorThird: Boolean): Double {
            return if (asMinorThird) {
                concertPitch * (27.0 / 32.0)
            } else {
                concertPitch * (27.0 / 16.0)
            }
        }
    };

    /**
     * İcracının bastığı/okuduğu yazılı frekansı tınlayan konsert frekansına dönüştürür.
     */
    abstract fun toConcertPitch(instrumentPitch: Double, asMinorThird: Boolean = false): Double

    /**
     * Tınlayan konsert frekansını (mikrofon girdisi), enstrüman icracısının okuduğu/bastığı frekansa dönüştürür.
     */
    abstract fun toInstrumentPitch(concertPitch: Double, asMinorThird: Boolean = false): Double

    // Geriye dönük uyumluluk köprüsü
    fun toTargetPitch(detectedFrequency: Double): Double = toInstrumentPitch(detectedFrequency)
    fun fromTargetPitch(targetFrequency: Double): Double = toConcertPitch(targetFrequency)

    companion object {
        fun fromDisplayName(name: String): TranspositionMode {
            return when {
                name.contains("Alto", ignoreCase = true) || name.contains("Eb", ignoreCase = true) -> EB_ALTO_SAX
                name.contains("Tenor", ignoreCase = true) || name.contains("Bb", ignoreCase = true) || name.contains("Klarnet", ignoreCase = true) -> BB_INSTRUMENTS
                else -> CONCERT
            }
        }
    }
}
