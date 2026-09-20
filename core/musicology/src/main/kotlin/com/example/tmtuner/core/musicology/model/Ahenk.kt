package com.example.tmtuner.core.musicology.model

/**
 * Türk Müziği icrasında kullanılan geleneksel Ahenk sistemleri.
 * İsmail Hakkı Özkan ve Türk Müziği icra standardı temel alınmıştır.
 */
enum class Ahenk(
    val displayName: String,
    val referenceLa: Double,
    val description: String,
    val komaShiftFromMansur: Int = 0,
    val ratioFromMansur: Double = 1.0
) {
    MANSUR(
        displayName = "Mansur (Fizik Çârgâh = 256 Hz / Dügâh = 440 Hz)",
        referenceLa = 440.0,
        description = "Ana referans ahengi (Standart Diyapazon - Mansur Ney boyu. Kaba Çârgâh 256 Hz, Dügâh 440 Hz)",
        komaShiftFromMansur = 0,
        ratioFromMansur = 1.0
    ),
    BOLAHENK(
        displayName = "Bolâhenk (Nevâ = 440 Hz / Dügâh ≈ 329.63-330 Hz)",
        referenceLa = 330.0,
        description = "Mansur'a göre Tam Dörtlü pes (-22 koma, 3/4 oranı). Nevâ = 440 Hz, Dügâh = 330 Hz referansı",
        komaShiftFromMansur = -22,
        ratioFromMansur = 3.0 / 4.0
    ),
    KIZ(
        displayName = "Kız (Dügâh = Re5 / La ≈ 586.67 Hz)",
        referenceLa = 440.0 * (4.0 / 3.0), // 586.6666666666666 Hz
        description = "Mansur'a göre Tam Dörtlü tiz (+22 koma, 4/3 oranı). Dügâh ≈ 586.67 Hz (Re5)",
        komaShiftFromMansur = 22,
        ratioFromMansur = 4.0 / 3.0
    ),
    SUPURDE(
        displayName = "Süpürde (La = 523.2 Hz)",
        referenceLa = 523.2,
        description = "3 ses tiz (Süpürde Ney boyu)",
        komaShiftFromMansur = 13,
        ratioFromMansur = 523.2 / 440.0
    ),
    MUSTAHSEN(
        displayName = "Müstahsen (La = 495 Hz)",
        referenceLa = 495.0,
        description = "2 ses tiz (Müstahsen Ney boyu)",
        komaShiftFromMansur = 9,
        ratioFromMansur = 495.0 / 440.0
    ),
    YILDIZ(
        displayName = "Yıldız (La = 469.3 Hz)",
        referenceLa = 469.33,
        description = "1.5 ses tiz (Yıldız Ney boyu)",
        komaShiftFromMansur = 5,
        ratioFromMansur = 469.33 / 440.0
    ),
    SAH(
        displayName = "Şah (La = 391.1 Hz)",
        referenceLa = 391.11,
        description = "Yaklaşık 2 ses pes (Şah Ney boyu)",
        komaShiftFromMansur = -9,
        ratioFromMansur = 391.11 / 440.0
    ),
    DAVUD(
        displayName = "Davud (La = 366.3 Hz)",
        referenceLa = 366.27,
        description = "2.5 ses pes (Davud Ney boyu)",
        komaShiftFromMansur = -13,
        ratioFromMansur = 366.27 / 440.0
    );

    /**
     * Seçili ahenk için Kaba Çârgâh temel referans frekansını döner.
     * Mansur'da 440 * (16/27) = ~260.74 Hz (Diyapazon) veya Fizik Kaba Çârgâh 256 Hz.
     * Bolâhenk'te 330 * (16/27) = 195.5556 Hz.
     */
    fun getKabaCargahBaseFrequency(): Double {
        return referenceLa * (16.0 / 27.0)
    }

    companion object {
        fun fromString(name: String): Ahenk {
            return when {
                name.contains("Bolahenk", ignoreCase = true) || name.contains("Bolâhenk", ignoreCase = true) -> BOLAHENK
                name.contains("Kız", ignoreCase = true) || name.contains("Kiz", ignoreCase = true) -> KIZ
                name.contains("Süpürde", ignoreCase = true) || name.contains("Supurde", ignoreCase = true) -> SUPURDE
                else -> MANSUR
            }
        }
    }
}
