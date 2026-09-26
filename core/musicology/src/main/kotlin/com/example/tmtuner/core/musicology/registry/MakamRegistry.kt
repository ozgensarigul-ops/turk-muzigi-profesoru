package com.example.tmtuner.core.musicology.registry

import com.example.tmtuner.core.musicology.model.MakamProfile
import com.example.tmtuner.core.musicology.model.SeyirType

/**
 * İsmail Hakkı Özkan'ın "Türk Mûsikîsi Nazariyatı ve Usûlleri" kitabına ve
 * Arel-Ezgi-Uzdilek (AEU) sistemine dayalı Temel Makam Kayıt Merkezi (Registry).
 */
object MakamRegistry {

    /**
     * ÇÂRGÂH Makamı (Özkan s. 118-121):
     * - Durak: Çârgâh (Do)
     * - Güçlü: Gerdâniye (Sol, 1. mertebe), Rast (Sol pes, 2. mertebe)
     * - Yeden: Bûselik (Si, 4 koma / Bakiye pest)
     * - Seyir: Çıkıcı
     * - Dizi: Çârgâh perdesinde Çârgâh beşlisine Gerdâniye'de Çârgâh dörtlüsünün eklenmesi
     * - Aralıklar: T-T-B-T-T-T-B (9, 9, 4, 9, 9, 9, 4 koma = 53 koma)
     */
    val CARGAH = MakamProfile(
        id = "cargah",
        name = "Çârgâh",
        durakPerde = "Çârgâh",
        gucluPerde = "Gerdâniye",
        yedenPerde = "Bûselik",
        seyir = SeyirType.CIKICI,
        scaleIntervals = listOf(9, 9, 4, 9, 9, 9, 4),
        scalePerdeler = listOf("Çârgâh", "Neva", "Hüseynî", "Acem", "Gerdâniye", "Muhayyer", "Tîz Bûselik", "Tîz Çârgâh"),
        asmaKararPerdeleri = listOf("Rast")
    )

    /**
     * BÛSELİK Makamı (Özkan s. 122-128):
     * - Durak: Dügâh (La)
     * - Güçlü: Hüseynî (Mi, 1. mertebe), Neva (Re, 2. mertebe)
     * - Yeden: Nîm Zîrgüle (Sol bakiye diyez, 5 koma / Küçük Mücenneb pest)
     * - Seyir: Çıkıcı (veya inici-çıkıcı icralar da mevcuttur; Özkan klasik tanım: Çıkıcı)
     * - Dizi: Dügâh'ta Bûselik beşlisine Hüseynî'de Kürdî veya Hicaz dörtlüsünün eklenmesi
     * - Aralıklar: T-B-T-T-B-T-T (9, 4, 9, 9, 4, 9, 9 koma = 53 koma)
     */
    val BUSELIK = MakamProfile(
        id = "buselik",
        name = "Bûselik",
        durakPerde = "Dügâh",
        gucluPerde = "Hüseynî",
        yedenPerde = "Nîm Zîrgüle",
        seyir = SeyirType.CIKICI,
        scaleIntervals = listOf(9, 4, 9, 9, 4, 9, 9),
        scalePerdeler = listOf("Dügâh", "Bûselik", "Çârgâh", "Neva", "Hüseynî", "Acem", "Gerdâniye", "Muhayyer"),
        asmaKararPerdeleri = listOf("Çârgâh", "Neva")
    )

    /**
     * KÜRDÎ Makamı (Özkan s. 133-136):
     * - Durak: Dügâh (La)
     * - Güçlü: Nevâ (Re)
     * - Yeden: Râst (Sol, 9 koma / Tanini pest)
     * - Seyir: Çıkıcı
     * - Dizi: Dügâh'ta Kürdî dörtlüsüne Nevâ'da Bûselik beşlisinin eklenmesi
     * - Aralıklar: B-T-T-T-B-T-T (4, 9, 9, 9, 4, 9, 9 koma = 53 koma)
     */
    val KURDI = MakamProfile(
        id = "kurdi",
        name = "Kürdî",
        durakPerde = "Dügâh",
        gucluPerde = "Neva",
        yedenPerde = "Rast",
        seyir = SeyirType.CIKICI,
        scaleIntervals = listOf(4, 9, 9, 9, 4, 9, 9),
        scalePerdeler = listOf("Dügâh", "Kürdî", "Çârgâh", "Neva", "Hüseynî", "Acem", "Gerdâniye", "Muhayyer"),
        asmaKararPerdeleri = listOf("Çârgâh")
    )

    /**
     * RÂST Makamı (Özkan s. 137-142):
     * - Durak: Râst (Sol)
     * - Güçlü: Nevâ (Re)
     * - Yeden: Irak (Fa bakiye diyez, 5 koma / Küçük Mücenneb pest)
     * - Seyir: Çıkıcı
     * - Dizi: Rast perdesinde Rast beşlisine Neva'da Rast dörtlüsünün eklenmesi
     * - Aralıklar: T-K-S-T-T-K-S (9, 8, 5, 9, 9, 8, 5 koma = 53 koma)
     */
    val RAST = MakamProfile(
        id = "rast",
        name = "Râst",
        durakPerde = "Rast",
        gucluPerde = "Neva",
        yedenPerde = "Irak",
        seyir = SeyirType.CIKICI,
        scaleIntervals = listOf(9, 8, 5, 9, 9, 8, 5),
        scalePerdeler = listOf("Rast", "Dügâh", "Segâh", "Çârgâh", "Neva", "Hüseynî", "Eviç", "Gerdâniye"),
        asmaKararPerdeleri = listOf("Segâh", "Dügâh")
    )

    /**
     * UŞŞÂK Makamı (Özkan s. 143-148):
     * - Durak: Dügâh (La)
     * - Güçlü: Nevâ (Re)
     * - Yeden: Râst (Sol, 9 koma / Tanini pest)
     * - Seyir: İnici-Çıkıcı
     * - Dizi: Dügâh'ta Uşşâk dörtlüsüne Nevâ'da Bûselik beşlisinin eklenmesi
     * - Aralıklar: K-S-T-T-B-T-T (8, 5, 9, 9, 4, 9, 9 koma = 53 koma)
     * - Segâh toleransı: [-2.0, +0.5] koma (Segâh 1-2 koma pest icra edilir)
     */
    val USSAK = MakamProfile(
        id = "ussak",
        name = "Uşşâk",
        durakPerde = "Dügâh",
        gucluPerde = "Neva",
        yedenPerde = "Rast",
        seyir = SeyirType.INICI_CIKICI,
        scaleIntervals = listOf(8, 5, 9, 9, 4, 9, 9),
        scalePerdeler = listOf("Dügâh", "Segâh", "Çârgâh", "Neva", "Hüseynî", "Acem", "Gerdâniye", "Muhayyer"),
        asmaKararPerdeleri = listOf("Segâh", "Çârgâh"),
        segahToleranceKoma = -2.0..0.5
    )

    /**
     * HÜSEYNÎ Makamı (Özkan s. 179-184):
     * - Durak: Dügâh (La)
     * - Güçlü: Hüseynî (Mi, 1. mertebe), Nevâ (Re, 2. mertebe)
     * - Yeden: Râst (Sol, 9 koma / Tanini pest)
     * - Seyir: İnici-Çıkıcı
     * - Dizi: Dügâh'ta Hüseynî beşlisine Hüseynî'de Uşşâk dörtlüsünün eklenmesi
     * - Aralıklar: K-S-T-T-K-S-T (8, 5, 9, 9, 8, 5, 9 koma = 53 koma)
     * - Segâh toleransı: [-2.0, +0.5] koma
     */
    val HUSEYNI = MakamProfile(
        id = "huseyni",
        name = "Hüseynî",
        durakPerde = "Dügâh",
        gucluPerde = "Hüseynî",
        yedenPerde = "Rast",
        seyir = SeyirType.INICI_CIKICI,
        scaleIntervals = listOf(8, 5, 9, 9, 8, 5, 9),
        scalePerdeler = listOf("Dügâh", "Segâh", "Çârgâh", "Neva", "Hüseynî", "Eviç", "Gerdâniye", "Muhayyer"),
        asmaKararPerdeleri = listOf("Neva", "Segâh", "Çârgâh"),
        segahToleranceKoma = -2.0..0.5
    )

    /**
     * HÜMÂYÛN Makamı (Özkan s. 159-163):
     * - Durak: Dügâh (La)
     * - Güçlü: Nevâ (Re)
     * - Yeden: Râst (Sol, 9 koma / Tanini pest)
     * - Seyir: İnici-Çıkıcı
     * - Dizi: Dügâh'ta Hicaz dörtlüsüne Nevâ'da Bûselik beşlisinin eklenmesi
     * - Aralıklar: S-A12-S-T-B-T-T (5, 12, 5, 9, 4, 9, 9 koma = 53 koma)
     * - Ayırt edici ses: Acem (Fa natürel, Eviç kesinlikle kullanılmaz)
     */
    val HUMAYUN = MakamProfile(
        id = "humayun",
        name = "Hümâyûn",
        durakPerde = "Dügâh",
        gucluPerde = "Neva",
        yedenPerde = "Rast",
        seyir = SeyirType.INICI_CIKICI,
        scaleIntervals = listOf(5, 12, 5, 9, 4, 9, 9),
        scalePerdeler = listOf("Dügâh", "Dik Kürdî", "Kürdî", "Hicâz", "Nîm Hicâz", "Dik Hicâz", "Neva", "Hüseynî", "Acem", "Gerdâniye", "Muhayyer"),
        asmaKararPerdeleri = listOf("Çârgâh", "Hüseynî")
    )

    /**
     * HİCAZ Makamı (Özkan s. 164-168):
     * - Durak: Dügâh (La)
     * - Güçlü: Nevâ (Re)
     * - Yeden: Râst (Sol, 9 koma / Tanini pest)
     * - Seyir: İnici-Çıkıcı
     * - Dizi: Dügâh'ta Hicaz dörtlüsüne Nevâ'da Râst beşlisinin eklenmesi
     * - Aralıklar: S-A12-S-T-K-S-T (5, 12, 5, 9, 8, 5, 9 koma = 53 koma)
     * - Ayırt edici ses: Eviç (Fa bakiye diyez)
     */
    val HICAZ = MakamProfile(
        id = "hicaz",
        name = "Hicaz",
        durakPerde = "Dügâh",
        gucluPerde = "Neva",
        yedenPerde = "Rast",
        seyir = SeyirType.INICI_CIKICI,
        scaleIntervals = listOf(5, 12, 5, 9, 8, 5, 9),
        scalePerdeler = listOf("Dügâh", "Dik Kürdî", "Kürdî", "Hicâz", "Nîm Hicâz", "Dik Hicâz", "Neva", "Hüseynî", "Eviç", "Gerdâniye", "Muhayyer"),
        asmaKararPerdeleri = listOf("Eviç", "Hüseynî")
    )

    /**
     * UZZAL Makamı (Özkan s. 169-173):
     * - Durak: Dügâh (La)
     * - Güçlü: Nevâ (Re, 1. mertebe), Hüseynî (Mi, 2. mertebe)
     * - Yeden: Râst (Sol, 9 koma / Tanini pest)
     * - Seyir: İnici-Çıkıcı
     * - Dizi: Dügâh'ta Hicaz beşlisine Hüseynî'de Uşşâk dörtlüsünün eklenmesi
     * - Aralıklar: S-A12-S-T-K-S-T (5, 12, 5, 9, 8, 5, 9 koma = 53 koma)
     */
    val UZZAL = MakamProfile(
        id = "uzzal",
        name = "Uzzal",
        durakPerde = "Dügâh",
        gucluPerde = "Neva",
        yedenPerde = "Rast",
        seyir = SeyirType.INICI_CIKICI,
        scaleIntervals = listOf(5, 12, 5, 9, 8, 5, 9),
        scalePerdeler = listOf("Dügâh", "Dik Kürdî", "Kürdî", "Hicâz", "Nîm Hicâz", "Dik Hicâz", "Neva", "Hüseynî", "Eviç", "Gerdâniye", "Muhayyer"),
        asmaKararPerdeleri = listOf("Hüseynî")
    )

    /**
     * ZÎRGÛLELİ HİCAZ Makamı (Özkan s. 174-178):
     * - Durak: Dügâh (La)
     * - Güçlü: Nevâ (Re)
     * - Yeden: Zîrgûle (Sol bakiye diyez / Nîm Zîrgüle veya Rast)
     * - Seyir: İnici-Çıkıcı
     * - Dizi: Dügâh'ta Hicaz dörtlüsüne Nevâ'da Hicaz beşlisinin eklenmesi
     * - Aralıklar: S-A12-S-T-S-A12-S (5, 12, 5, 9, 5, 12, 5 koma = 53 koma)
     * - Ayırt edici sesler: Hisâr / Dik Hisâr ve Şehnâz
     */
    val ZIRGULELI_HICAZ = MakamProfile(
        id = "zirguleli_hicaz",
        name = "Zîrgûleli Hicaz",
        durakPerde = "Dügâh",
        gucluPerde = "Neva",
        yedenPerde = "Zîrgûle",
        seyir = SeyirType.INICI_CIKICI,
        scaleIntervals = listOf(5, 12, 5, 9, 5, 12, 5),
        scalePerdeler = listOf("Dügâh", "Dik Kürdî", "Kürdî", "Hicâz", "Nîm Hicâz", "Dik Hicâz", "Neva", "Hisâr", "Dik Hisâr", "Nîm Hisâr", "Şehnâz", "Nîm Şehnâz", "Dik Şehnâz", "Muhayyer", "Zîrgûle", "Nîm Zîrgüle"),
        asmaKararPerdeleri = listOf("Hicâz", "Hisâr")
    )

    /**
     * KARCIĞAR Makamı (Özkan s. 199-202):
     * - Durak: Dügâh (La)
     * - Güçlü: Nevâ (Re)
     * - Yeden: Râst (Sol, 9 koma / Tanini pest)
     * - Seyir: İnici-Çıkıcı
     * - Dizi: Dügâh'ta Uşşâk dörtlüsüne Nevâ'da Hicaz beşlisinin eklenmesi
     * - Aralıklar: K-S-T-T-S-A12-S (8, 5, 9, 9, 5, 12, 5 koma = 53 koma)
     * - Segâh toleransı: [-2.0, +0.5] koma
     * - Ayırt edici sesler: Segâh + Nevâ'da Hicaz (Hisâr / Şehnâz)
     */
    val KARCIGAR = MakamProfile(
        id = "karcigar",
        name = "Karcığar",
        durakPerde = "Dügâh",
        gucluPerde = "Neva",
        yedenPerde = "Rast",
        seyir = SeyirType.INICI_CIKICI,
        scaleIntervals = listOf(8, 5, 9, 9, 5, 12, 5),
        scalePerdeler = listOf("Dügâh", "Segâh", "Çârgâh", "Neva", "Hisâr", "Dik Hisâr", "Nîm Hisâr", "Şehnâz", "Nîm Şehnâz", "Dik Şehnâz", "Muhayyer"),
        asmaKararPerdeleri = listOf("Segâh", "Hisâr"),
        segahToleranceKoma = -2.0..0.5
    )

    /**
     * BASİT SÛZ'NÂK Makamı (Özkan s. 203-206):
     * - Durak: Râst (Sol)
     * - Güçlü: Nevâ (Re)
     * - Yeden: Irak (Fa bakiye diyez, 5 koma / Küçük Mücenneb pest)
     * - Seyir: İnici-Çıkıcı
     * - Dizi: Rast perdesinde Rast beşlisine Nevâ'da Hicaz dörtlüsünün eklenmesi
     * - Aralıklar: T-K-S-T-S-A12-S (9, 8, 5, 9, 5, 12, 5 koma = 53 koma)
     * - Segâh toleransı: [-2.0, +0.5] koma
     */
    val BASIT_SUZNAK = MakamProfile(
        id = "basit_suznak",
        name = "Basit Sûz'nâk",
        durakPerde = "Rast",
        gucluPerde = "Neva",
        yedenPerde = "Irak",
        seyir = SeyirType.INICI_CIKICI,
        scaleIntervals = listOf(9, 8, 5, 9, 5, 12, 5),
        scalePerdeler = listOf("Rast", "Dügâh", "Segâh", "Çârgâh", "Neva", "Hisâr", "Dik Hisâr", "Nîm Hisâr", "Şehnâz", "Gerdâniye"),
        asmaKararPerdeleri = listOf("Segâh", "Dügâh"),
        segahToleranceKoma = -2.0..0.5
    )

    /**
     * NEVÂ Makamı (Özkan s. 191-198):
     * - Durak: Dügâh (La)
     * - Güçlü: Nevâ (Re)
     * - Yeden: Râst (Sol, 9 koma / Tanini pest)
     * - Seyir: İnici-Çıkıcı
     * - Dizi: Dügâh'ta Uşşâk dörtlüsüne Nevâ'da Râst beşlisinin eklenmesi
     * - Aralıklar: K-S-T-T-K-S-T (8, 5, 9, 9, 8, 5, 9 koma = 53 koma)
     * - Segâh toleransı: [-2.0, +0.5] koma
     */
    val NEVA = MakamProfile(
        id = "neva",
        name = "Nevâ",
        durakPerde = "Dügâh",
        gucluPerde = "Neva",
        yedenPerde = "Rast",
        seyir = SeyirType.INICI_CIKICI,
        scaleIntervals = listOf(8, 5, 9, 9, 8, 5, 9),
        scalePerdeler = listOf("Dügâh", "Segâh", "Çârgâh", "Neva", "Hüseynî", "Eviç", "Gerdâniye", "Muhayyer"),
        asmaKararPerdeleri = listOf("Segâh", "Hüseynî"),
        segahToleranceKoma = -2.0..0.5
    )

    /**
     * İsmail Hakkı Özkan'ın kitabında yer alan temel basit makamlar listesi.
     */
    val CORE_MAKAMS: List<MakamProfile> = listOf(
        CARGAH,
        BUSELIK,
        KURDI,
        RAST,
        USSAK,
        HUSEYNI,
        HUMAYUN,
        HICAZ,
        UZZAL,
        ZIRGULELI_HICAZ,
        KARCIGAR,
        BASIT_SUZNAK,
        NEVA
    )

    /**
     * Makam kimliğine (id) göre makam profilini bulur.
     */
    fun getById(id: String): MakamProfile? {
        return CORE_MAKAMS.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }

    /**
     * Makam adına göre makam profilini bulur (Türkçe karakter ve şapka toleranslı).
     */
    fun getByName(name: String): MakamProfile? {
        val normalized = normalizeText(name)
        return CORE_MAKAMS.firstOrNull { normalizeText(it.name) == normalized || normalizeText(it.id) == normalized }
    }

    /**
     * Perde isimlerini karşılaştırma için normalize eder (Türkçe karakter, inceltme şapkası toleransı).
     */
    fun normalizePerdeName(name: String): String {
        return normalizeText(name)
    }

    private fun normalizeText(input: String): String {
        return input.trim().lowercase()
            .replace("â", "a")
            .replace("î", "i")
            .replace("û", "u")
            .replace("ç", "c")
            .replace("ğ", "g")
            .replace("ı", "i")
            .replace("ö", "o")
            .replace("ş", "s")
            .replace("ü", "u")
    }
}
