package com.example.tmtuner.core.musicology.model

/**
 * Türk Müziği icrasında kullanılan geleneksel Ahenk sistemleri.
 * İsmail Hakkı Özkan ve Türk Müziği icra standardı temel alınmıştır.
 */
enum class Ahenk(
    val displayName: String,
    val referenceLa: Double,
    val description: String
) {
    MANSUR(
        displayName = "Mansur (Fizik Çârgâh = 256 Hz)",
        referenceLa = 440.0,
        description = "Ana referans ahengi (Standart Diyapazon - Mansur Ney boyu)"
    ),
    BOLAHENK(
        displayName = "Bolâhenk (Nevâ = 440 Hz / Dügâh = 330 Hz)",
        referenceLa = 586.0,
        description = "4 ses tiz / Nısfiye boyu. Dügâh 330 Hz, Nevâ 440 Hz referansı"
    ),
    KIZ(
        displayName = "Kız (Neyi Başkayıtsız / La = 415 Hz)",
        referenceLa = 415.0,
        description = "Yaklaşık 1 tam ses pes (Kız Ney boyu)"
    ),
    SUPURDE(
        displayName = "Süpürde (La = 523 Hz)",
        referenceLa = 523.0,
        description = "3 ses tiz (Süpürde Ney boyu)"
    );

    /**
     * Seçili ahenk için Kaba Çârgâh temel referans frekansını döner.
     * Mansur'da 440 / 2.25 = ~195.55 Hz (Orta Çârgâh 391.11 Hz, Fizik Kaba Çârgâh 256 Hz)
     */
    fun getKabaCargahBaseFrequency(): Double {
        return referenceLa / 2.25
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
