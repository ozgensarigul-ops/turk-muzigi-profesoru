package com.example.tmtuner.core.musicology.model

/**
 * Arel-Ezgi-Uzdilek (AEU) ve İsmail Hakkı Özkan nazariyatında tanımlı
 * temel mikrotonal aralıklar ve 53-EDO koma değerleri.
 */
enum class Interval(
    val intervalName: String,
    val abbreviation: String,
    val komaValue: Int,
    val centValue: Double,
    val ratioString: String,
    val ratioValue: Double
) {
    FAZLA_KOMA(
        intervalName = "Fazla / Koma",
        abbreviation = "F",
        komaValue = 1,
        centValue = 22.6415,
        ratioString = "≈ 1.0132",
        ratioValue = 1.013164
    ),
    EKSIK_BAKIYE(
        intervalName = "Eksik Bakiye",
        abbreviation = "E",
        komaValue = 3,
        centValue = 67.9245,
        ratioString = "≈ 1.0401",
        ratioValue = 1.0401
    ),
    BAKIYE(
        intervalName = "Bakiye",
        abbreviation = "B",
        komaValue = 4,
        centValue = 90.225,
        ratioString = "256/243",
        ratioValue = 256.0 / 243.0
    ),
    KUCUK_MUCENNEP(
        intervalName = "Küçük Mücennep",
        abbreviation = "S",
        komaValue = 5,
        centValue = 114.44,
        ratioString = "2187/2048",
        ratioValue = 2187.0 / 2048.0
    ),
    BUYUK_MUCENNEP(
        intervalName = "Büyük Mücennep",
        abbreviation = "K",
        komaValue = 8,
        centValue = 180.45,
        ratioString = "65536/59049",
        ratioValue = 65536.0 / 59049.0
    ),
    TANINI(
        intervalName = "Tanini (Tam Ses)",
        abbreviation = "T",
        komaValue = 9,
        centValue = 203.91,
        ratioString = "9/8",
        ratioValue = 9.0 / 8.0
    ),
    ARTIK_IKILI_12(
        intervalName = "Artık İkili (12 Koma)",
        abbreviation = "A12",
        komaValue = 12,
        centValue = 271.698,
        ratioString = "19683/16384",
        ratioValue = 19683.0 / 16384.0
    ),
    ARTIK_IKILI_13(
        intervalName = "Artık İkili (13 Koma)",
        abbreviation = "A13",
        komaValue = 13,
        centValue = 294.339,
        ratioString = "≈ 1.185",
        ratioValue = 1.185
    );

    companion object {
        /** Tam sekizli (Oktav) = 53 koma */
        const val OKTAV_KOMA = 53

        /** Tam sekizli cent = 1200 Cent */
        const val OKTAV_CENT = 1200.0

        /**
         * Kısaltmaya göre (T, K, S, B, F, E, A12, A13) aralığı döner.
         */
        fun fromAbbreviation(abbr: String): Interval? {
            return entries.firstOrNull { it.abbreviation.equals(abbr, ignoreCase = true) }
        }
    }
}
