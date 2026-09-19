package com.example.tmtuner.core.musicology.model

/**
 * Türk Müziği makamlarının yapı taşları olan Dörtlü (Tetrachord) ve Beşli (Pentachord) çeşnileri.
 * İsmail Hakkı Özkan Nazariyatı (s. 50-70) temel alınmıştır.
 */
data class Cesni(
    val id: String,
    val name: String,
    val isPentachord: Boolean, // true: Beşli (31 koma), false: Dörtlü (22 koma)
    val intervals: List<Interval>,
    val description: String
) {
    /** Çeşninin toplam koma değeri (Tam Dörtlü için 22 koma, Tam Beşli için 31 koma) */
    val totalKoma: Int get() = intervals.sumOf { it.komaValue }

    /** Çeşninin harf formülü (Örn: "T-K-S-T", "K-S-T", "S-A12-S") */
    val formulaString: String get() = intervals.joinToString("-") { it.abbreviation }

    companion object {
        val RAST_BESLISI = Cesni(
            id = "rast_beslisi",
            name = "Râst Beşlisi",
            isPentachord = true,
            intervals = listOf(Interval.TANINI, Interval.BUYUK_MUCENNEP, Interval.KUCUK_MUCENNEP, Interval.TANINI),
            description = "Rast makamı ve ailesinin temel gövdesi (T-K-S-T)"
        )

        val USSAK_DORTLUSU = Cesni(
            id = "ussak_dortlusu",
            name = "Uşşâk Dörtlüsü",
            isPentachord = false,
            intervals = listOf(Interval.BUYUK_MUCENNEP, Interval.KUCUK_MUCENNEP, Interval.TANINI),
            description = "Uşşak, Beyati, Isfahan makamlarının temel gövdesi (K-S-T)"
        )

        val BUSELIK_BESLISI = Cesni(
            id = "buselik_beslisi",
            name = "Bûselik Beşlisi",
            isPentachord = true,
            intervals = listOf(Interval.TANINI, Interval.BAKIYE, Interval.TANINI, Interval.TANINI),
            description = "Bûselik ve Nihavend makamlarının temel gövdesi (T-B-T-T)"
        )

        val HICAZ_DORTLUSU = Cesni(
            id = "hicaz_dortlusu",
            name = "Hicaz Dörtlüsü",
            isPentachord = false,
            intervals = listOf(Interval.KUCUK_MUCENNEP, Interval.ARTIK_IKILI_12, Interval.KUCUK_MUCENNEP),
            description = "Hicaz, Uzzal, Humayun makamlarının temel gövdesi (S-A12-S)"
        )

        val HUSEYNI_BESLISI = Cesni(
            id = "huseyni_beslisi",
            name = "Hüseynî Beşlisi",
            isPentachord = true,
            intervals = listOf(Interval.BUYUK_MUCENNEP, Interval.KUCUK_MUCENNEP, Interval.TANINI, Interval.TANINI),
            description = "Hüseyni, Muhayyer, Gülizar makamlarının temel gövdesi (K-S-T-T)"
        )

        val CARGAH_BESLISI = Cesni(
            id = "cargah_beslisi",
            name = "Çârgâh Beşlisi",
            isPentachord = true,
            intervals = listOf(Interval.TANINI, Interval.TANINI, Interval.BAKIYE, Interval.TANINI),
            description = "Çârgâh ve Mahur makamlarının temel gövdesi (T-T-B-T)"
        )

        val KURDI_DORTLUSU = Cesni(
            id = "kurdi_dortlusu",
            name = "Kürdî Dörtlüsü",
            isPentachord = false,
            intervals = listOf(Interval.BAKIYE, Interval.TANINI, Interval.TANINI),
            description = "Kürdî ve Kürdilihicazkar makamlarının temel gövdesi (B-T-T)"
        )

        val SEGAH_BESLISI = Cesni(
            id = "segah_beslisi",
            name = "Segâh Beşlisi",
            isPentachord = true,
            intervals = listOf(Interval.KUCUK_MUCENNEP, Interval.TANINI, Interval.TANINI, Interval.BUYUK_MUCENNEP),
            description = "Segâh, Hüzzam, Evcara makamlarının temel gövdesi (S-T-T-K)"
        )

        val ALL_CESNIS = listOf(
            RAST_BESLISI,
            USSAK_DORTLUSU,
            BUSELIK_BESLISI,
            HICAZ_DORTLUSU,
            HUSEYNI_BESLISI,
            CARGAH_BESLISI,
            KURDI_DORTLUSU,
            SEGAH_BESLISI
        )
    }
}
