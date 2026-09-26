package com.example.tmtuner.core.musicology.model

/**
 * Geleneksel Türk Musikisi Ney çeşitleri, açkı perdeleri ve ahenk karşılıkları.
 * İsmail Hakkı Özkan "Türk Mûsikîsi Nazariyatı ve Usûlleri" standardı temel alınmıştır.
 *
 * Ney sazları, alt arka delik (aşîrân perdesi) kapalıyken tüm ön delikler açık üflendiğinde
 * veren "karar / açkı" (Râst) perdesine göre adlandırılır.
 */
enum class NeyType(
    val displayName: String,
    val ahenk: Ahenk,
    val rastNoteInWestern: String,
    val komaShiftFromMansur: Int, // Mansur Ney referansına göre koma farkı
    val ratioFromMansur: Double = 1.0, // Mansur Ney referansına göre frekans oranı
    val approximateLengthCm: String,
    val description: String
) {
    BOLAHENK(
        displayName = "Bolâhenk Ney",
        ahenk = Ahenk.BOLAHENK,
        rastNoteInWestern = "Sol (G4)",
        komaShiftFromMansur = -22, // Tam Dörtlü pes (-22 koma, 3/4 oranı, Nevâ = 440 Hz referanslı)
        ratioFromMansur = 3.0 / 4.0,
        approximateLengthCm = "102-105 cm",
        description = "Mansur'a göre Tam Dörtlü pes (-22 koma, 3/4 oranı). Nevâ = 440 Hz, Dügâh = 330 Hz."
    ),

    BOLAHENK_NISFIYE(
        displayName = "Bolâhenk Nısfiye",
        ahenk = Ahenk.BOLAHENK,
        rastNoteInWestern = "Sol (G5)",
        komaShiftFromMansur = 31, // 1 oktav tiz Bolâhenk (-22 + 53 = +31 koma / oran: 3/2)
        ratioFromMansur = 1.5,
        approximateLengthCm = "50-52 cm",
        description = "Bolâhenk ney boyunun yarısı (nısfiyesi) olup 1 oktav tizidir."
    ),

    SUPURDE(
        displayName = "Süpürde",
        ahenk = Ahenk.SUPURDE,
        rastNoteInWestern = "Fa (F5)",
        komaShiftFromMansur = 13,
        ratioFromMansur = 523.2 / 440.0,
        approximateLengthCm = "58-60 cm",
        description = "Orta-tiz ney boyu. Râst perdesi piyanoda Fa tınlar."
    ),

    MUSTAHSEN(
        displayName = "Müstahsen",
        ahenk = Ahenk.MUSTAHSEN,
        rastNoteInWestern = "Mi (E5)",
        komaShiftFromMansur = 9,
        ratioFromMansur = 495.0 / 440.0,
        approximateLengthCm = "64-66 cm",
        description = "Orta boy ney. Râst perdesi piyanoda Mi tınlar."
    ),

    YILDIZ(
        displayName = "Yıldız",
        ahenk = Ahenk.YILDIZ,
        rastNoteInWestern = "Mib (Eb5)",
        komaShiftFromMansur = 5,
        ratioFromMansur = 469.33 / 440.0,
        approximateLengthCm = "68-70 cm",
        description = "Orta boy ney. Râst perdesi piyanoda Mib tınlar."
    ),

    KIZ(
        displayName = "Kız Ney",
        ahenk = Ahenk.KIZ,
        rastNoteInWestern = "La (A4)",
        komaShiftFromMansur = 9, // 1 Tanini tiz (+9 koma, 9/8 oranı)
        ratioFromMansur = 9.0 / 8.0,
        approximateLengthCm = "68-70 cm",
        description = "Mansur'a göre 1 Tanini tiz (+9 koma, 9/8 oranı). Dügâh = Si (B4), Râst = La (A4)."
    ),

    MANSUR(
        displayName = "Mansur Ney",
        ahenk = Ahenk.MANSUR,
        rastNoteInWestern = "Do (C5)",
        komaShiftFromMansur = 0, // Ana referans (0 koma, 1/1 oranı)
        ratioFromMansur = 1.0,
        approximateLengthCm = "78-81 cm",
        description = "Geleneksel Türk musikisinin ana referans ney boyu (Diyapazon Dügâh = 440 Hz / Fizik Kaba Çârgâh = 256 Hz)."
    ),

    SAH(
        displayName = "Şah Ney",
        ahenk = Ahenk.SAH,
        rastNoteInWestern = "Si (B4)",
        komaShiftFromMansur = -9,
        ratioFromMansur = 391.11 / 440.0,
        approximateLengthCm = "86-88 cm",
        description = "Pes ve heybetli tınıya sahip büyük boy ney."
    ),

    DAVUD(
        displayName = "Davud Ney",
        ahenk = Ahenk.DAVUD,
        rastNoteInWestern = "Sib (Bb4)",
        komaShiftFromMansur = -13,
        ratioFromMansur = 366.27 / 440.0,
        approximateLengthCm = "92-95 cm",
        description = "Çok pes ney boyu. Râst perdesi piyanoda Sib tınlar."
    ),

    BOLAHENK_KABA(
        displayName = "Kaba Bolâhenk",
        ahenk = Ahenk.BOLAHENK,
        rastNoteInWestern = "Sol (G4)",
        komaShiftFromMansur = -22, // Tam Dörtlü pes (-22 koma, 3/4 oranı)
        ratioFromMansur = 3.0 / 4.0,
        approximateLengthCm = "102-105 cm",
        description = "Bolâhenk Ney ile aynı boydadır (Tam Dörtlü pes)."
    );

    companion object {
        fun fromString(name: String): NeyType {
            return when {
                name.contains("Nısfiye", ignoreCase = true) -> BOLAHENK_NISFIYE
                name.contains("Kaba", ignoreCase = true) && name.contains("Bolahenk", ignoreCase = true) -> BOLAHENK
                name.contains("Bolahenk", ignoreCase = true) || name.contains("Bolâhenk", ignoreCase = true) -> BOLAHENK
                name.contains("Süpürde", ignoreCase = true) || name.contains("Supurde", ignoreCase = true) -> SUPURDE
                name.contains("Müstahsen", ignoreCase = true) || name.contains("Mustahsen", ignoreCase = true) -> MUSTAHSEN
                name.contains("Yıldız", ignoreCase = true) || name.contains("Yildiz", ignoreCase = true) -> YILDIZ
                name.contains("Kız", ignoreCase = true) || name.contains("Kiz", ignoreCase = true) -> KIZ
                name.contains("Şah", ignoreCase = true) || name.contains("Sah", ignoreCase = true) -> SAH
                name.contains("Davud", ignoreCase = true) -> DAVUD
                else -> MANSUR
            }
        }

        fun fromAhenk(ahenk: Ahenk): NeyType {
            return when (ahenk) {
                Ahenk.BOLAHENK -> BOLAHENK
                Ahenk.SUPURDE -> SUPURDE
                Ahenk.MUSTAHSEN -> MUSTAHSEN
                Ahenk.YILDIZ -> YILDIZ
                Ahenk.KIZ -> KIZ
                Ahenk.MANSUR -> MANSUR
                Ahenk.SAH -> SAH
                Ahenk.DAVUD -> DAVUD
            }
        }
    }
}
