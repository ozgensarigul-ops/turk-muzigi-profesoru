package com.example.tmtuner.core.musicology.model

/**
 * Arel-Ezgi-Uzdilek (AEU) ve İsmail Hakkı Özkan nazariyatına tam uyumlu Makam Profili.
 *
 * @param id Benzersiz makam tanıtıcısı (örn: "ussak", "rast")
 * @param name Makamın tam adı (örn: "Uşşâk", "Râst")
 * @param durakPerde Karar perdesi adı (örn: "Dügâh", "Rast")
 * @param gucluPerde Güçlü perdesi adı (örn: "Neva", "Hüseynî")
 * @param yedenPerde Yeden perdesi adı (örn: "Rast", "Irak")
 * @param seyir Makamın karakteristik seyir tipi (Çıkıcı, İnici-Çıkıcı, İnici)
 * @param scaleIntervals 53-EDO koma aralıkları dizisi (örn: [8, 5, 9, 9, 4, 9, 9])
 * @param scalePerdeler Diziyi oluşturan perde isimleri (örn: ["Dügâh", "Segâh", "Çârgâh", "Neva", "Hüseynî", "Acem", "Gerdâniye", "Muhayyer"])
 * @param asmaKararPerdeleri Muvakkat/Asma karar perdeleri
 * @param segahToleranceKoma Segâh perdesi için koma icra toleransı (varsayılan: -2.0..0.5 koma)
 */
data class MakamProfile(
    val id: String,
    val name: String,
    val durakPerde: String,
    val gucluPerde: String,
    val yedenPerde: String,
    val seyir: SeyirType,
    val scaleIntervals: List<Int>, // 53-EDO koma aralıkları dizisi
    val scalePerdeler: List<String>,
    val asmaKararPerdeleri: List<String> = emptyList(),
    val segahToleranceKoma: ClosedFloatingPointRange<Double> = -2.0..0.5
)
