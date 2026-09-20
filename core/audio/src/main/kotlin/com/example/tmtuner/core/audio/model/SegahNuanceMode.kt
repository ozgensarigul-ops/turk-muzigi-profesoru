package com.example.tmtuner.core.audio.model

/**
 * İsmail Hakkı Özkan s. 51 gereği Segâh perdesinde geleneksel icra toleransı modları.
 *
 * Segâh perdesi teorik Pisagor oranında 49 veya 48 koma iken,
 * icrada (Segâh, Uşşak, Hüseynî vb. makamlarda) -1.0 veya -2.0 koma pestleştirilerek üflenir/çalınır.
 */
enum class SegahNuanceMode(
    val displayName: String,
    val komaOffset: Double,
    val description: String
) {
    /** Teorik Pisagor frekansı (nüanssız / 0 koma ofset) */
    NONE(
        displayName = "Teorik (Nüanssız)",
        komaOffset = 0.0,
        description = "Özkan s. 74 Tablo I saf Pisagor Segâh frekansı (49 mutlak koma)."
    ),

    /** 1 koma pest icra nüansı */
    PEST_1_KOMA(
        displayName = "1 Koma Pest (Geleneksel)",
        komaOffset = -1.0,
        description = "Geleneksel icrada Segâh perdesinin 1 koma pest çalınması."
    ),

    /** 2 koma pest icra nüansı */
    PEST_2_KOMA(
        displayName = "2 Koma Pest (Uşşak/Hüseynî Tavrı)",
        komaOffset = -2.0,
        description = "Özellikle Uşşak ve Hüseynî icrasında yaygın 2 koma pest icra tavrı."
    ),

    /** -2.0 ile 0.0 koma arasındaki tüm aralığı geçerli icra toleransı kabul eden esnek mod */
    TOLERANT_RANGE(
        displayName = "Esnek İcra Toleransı (-2.0 .. 0.0 Koma)",
        komaOffset = -1.0,
        description = "Segâh perdesinde -2.0 ile 0.0 koma arasındaki icrayı tam akortlu kabul eder."
    )
}
