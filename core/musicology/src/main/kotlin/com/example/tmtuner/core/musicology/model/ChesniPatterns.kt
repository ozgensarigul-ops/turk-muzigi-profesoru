package com.example.tmtuner.core.musicology.model

/**
 * Türk Müziği Çeşni Şablonu ve İcra Toleransları Modeli.
 *
 * @param id Çeşni benzersiz kimliği
 * @param name Çeşni adı (örn: "Uşşâk Dörtlüsü")
 * @param isPentachord true ise Beşli (31 koma), false ise Dörtlü (22 koma)
 * @param intervals Aralık dizisi (AEU teorik aralıkları)
 * @param theoreticalKomas Teorik aralık koma değerleri (örn: Uşşâk için [8, 5, 9])
 * @param intervalTolerances İcracının geleneksel perde baskı tolerans koridorları
 * @param segahBonusScore Segâh perdesi algılandığında eklenecek güven puanı
 * @param description Müzikolojik açıklama
 */
data class ChesniPattern(
    val id: String,
    val name: String,
    val isPentachord: Boolean,
    val intervals: List<Interval>,
    val theoreticalKomas: List<Int>,
    val intervalTolerances: List<ClosedFloatingPointRange<Double>>,
    val segahBonusScore: Double = 0.0,
    val description: String = ""
) {
    val totalKoma: Int get() = theoreticalKomas.sum()
    val formulaString: String get() = intervals.joinToString("-") { it.abbreviation }

    /** Dügâh -> Segâh aralık kabul koridoru (Uşşâk için [6.0 .. 8.5] koma) */
    val dugahToSegahTolerance: ClosedFloatingPointRange<Double>
        get() = intervalTolerances.getOrElse(0) { 6.0..8.5 }

    /** Segâh -> Çârgâh aralık kabul koridoru (Uşşâk için [4.5 .. 7.5] koma) */
    val segahToCargahTolerance: ClosedFloatingPointRange<Double>
        get() = intervalTolerances.getOrElse(1) { 4.5..7.5 }

    /** Çârgâh -> Nevâ aralık kabul koridoru (Uşşâk için [8.0 .. 10.0] koma) */
    val cargahToNevaTolerance: ClosedFloatingPointRange<Double>
        get() = intervalTolerances.getOrElse(2) { 8.0..10.0 }
}

/**
 * İsmail Hakkı Özkan'ın "Türk Mûsikîsi Nazariyatı ve Usûlleri" (s. 50-70, s. 51 & 143)
 * referanslı Çeşni Şablonları Kataloğu.
 */
object ChesniPatterns {

    /**
     * UŞŞÂK DÖRTLÜSÜ (USSAK_QUARTET) Şablonu:
     * - Teorik aralıklar (Özkan s. 51 & 143):
     *     Dügâh -> Segâh: Büyük Mücenneb (K = 8 koma)
     *     Segâh -> Çârgâh: Küçük Mücenneb (S = 5 koma)
     *     Çârgâh -> Nevâ: Tanini (T = 9 koma)
     *     Toplam: 22 koma (Tam Dörtlü)
     * - İcra toleransı:
     *     Geleneksel Türk Müziği icrasında Segâh perdesi 1-2 koma pest basılır.
     *     Dügâh -> Segâh kabul koridoru: [6.0 .. 8.5] koma
     *     Segâh -> Çârgâh kabul koridoru: [4.5 .. 7.5] koma
     *     Çârgâh -> Nevâ kabul koridoru: [8.0 .. 10.0] koma
     * - Güven Bonusu:
     *     Segâh perdesi tespit edildiğinde Uşşâk çeşnisi eşleşmesine +25 güven puanı eklenir.
     */
    val USSAK_QUARTET = ChesniPattern(
        id = "ussak_dortlusu",
        name = "Uşşâk Dörtlüsü",
        isPentachord = false,
        intervals = listOf(Interval.BUYUK_MUCENNEP, Interval.KUCUK_MUCENNEP, Interval.TANINI),
        theoreticalKomas = listOf(8, 5, 9),
        intervalTolerances = listOf(6.0..8.5, 4.5..7.5, 8.0..10.0),
        segahBonusScore = 25.0,
        description = "Dügâh'ta Uşşâk Dörtlüsü (K-S-T, 8-5-9 koma, Segâh icra toleranslı [6.0..8.5] & [4.5..7.5])"
    )

    val RAST_PENTACHORD = ChesniPattern(
        id = "rast_beslisi",
        name = "Râst Beşlisi",
        isPentachord = true,
        intervals = listOf(Interval.TANINI, Interval.BUYUK_MUCENNEP, Interval.KUCUK_MUCENNEP, Interval.TANINI),
        theoreticalKomas = listOf(9, 8, 5, 9),
        intervalTolerances = listOf(8.0..10.0, 7.0..9.0, 4.0..6.0, 8.0..10.0),
        segahBonusScore = 0.0,
        description = "Rast perdesinde Rast Beşlisi (T-K-S-T, 9-8-5-9 koma = 31 koma)"
    )

    val BUSELIK_PENTACHORD = ChesniPattern(
        id = "buselik_beslisi",
        name = "Bûselik Beşlisi",
        isPentachord = true,
        intervals = listOf(Interval.TANINI, Interval.BAKIYE, Interval.TANINI, Interval.TANINI),
        theoreticalKomas = listOf(9, 4, 9, 9),
        intervalTolerances = listOf(8.0..10.0, 3.5..5.5, 8.0..10.0, 8.0..10.0),
        segahBonusScore = 0.0,
        description = "Bûselik perdesinde Bûselik Beşlisi (T-B-T-T, 9-4-9-9 koma = 31 koma)"
    )

    val HICAZ_QUARTET = ChesniPattern(
        id = "hicaz_dortlusu",
        name = "Hicaz Dörtlüsü",
        isPentachord = false,
        intervals = listOf(Interval.KUCUK_MUCENNEP, Interval.ARTIK_IKILI_12, Interval.KUCUK_MUCENNEP),
        theoreticalKomas = listOf(5, 12, 5),
        intervalTolerances = listOf(4.0..6.5, 11.0..13.5, 4.0..6.5),
        segahBonusScore = 0.0,
        description = "Hicaz Dörtlüsü (S-A12-S, 5-12-5 koma = 22 koma)"
    )

    val HUSEYNI_PENTACHORD = ChesniPattern(
        id = "huseyni_beslisi",
        name = "Hüseynî Beşlisi",
        isPentachord = true,
        intervals = listOf(Interval.BUYUK_MUCENNEP, Interval.KUCUK_MUCENNEP, Interval.TANINI, Interval.TANINI),
        theoreticalKomas = listOf(8, 5, 9, 9),
        intervalTolerances = listOf(6.0..8.5, 4.5..7.5, 8.0..10.0, 8.0..10.0),
        segahBonusScore = 15.0,
        description = "Dügâh'ta Hüseynî Beşlisi (K-S-T-T, 8-5-9-9 koma = 31 koma)"
    )

    val CARGAH_PENTACHORD = ChesniPattern(
        id = "cargah_beslisi",
        name = "Çârgâh Beşlisi",
        isPentachord = true,
        intervals = listOf(Interval.TANINI, Interval.TANINI, Interval.BAKIYE, Interval.TANINI),
        theoreticalKomas = listOf(9, 9, 4, 9),
        intervalTolerances = listOf(8.0..10.0, 8.0..10.0, 3.5..5.5, 8.0..10.0),
        segahBonusScore = 0.0,
        description = "Çârgâh Beşlisi (T-T-B-T, 9-9-4-9 koma = 31 koma)"
    )

    val KURDI_QUARTET = ChesniPattern(
        id = "kurdi_dortlusu",
        name = "Kürdî Dörtlüsü",
        isPentachord = false,
        intervals = listOf(Interval.BAKIYE, Interval.TANINI, Interval.TANINI),
        theoreticalKomas = listOf(4, 9, 9),
        intervalTolerances = listOf(3.5..5.5, 8.0..10.0, 8.0..10.0),
        segahBonusScore = 0.0,
        description = "Kürdî Dörtlüsü (B-T-T, 4-9-9 koma = 22 koma)"
    )

    val SEGAH_PENTACHORD = ChesniPattern(
        id = "segah_beslisi",
        name = "Segâh Beşlisi",
        isPentachord = true,
        intervals = listOf(Interval.KUCUK_MUCENNEP, Interval.TANINI, Interval.TANINI, Interval.BUYUK_MUCENNEP),
        theoreticalKomas = listOf(5, 9, 9, 8),
        intervalTolerances = listOf(4.0..6.5, 8.0..10.0, 8.0..10.0, 7.0..9.0),
        segahBonusScore = 15.0,
        description = "Segâh Beşlisi (S-T-T-K, 5-9-9-8 koma = 31 koma)"
    )
}
