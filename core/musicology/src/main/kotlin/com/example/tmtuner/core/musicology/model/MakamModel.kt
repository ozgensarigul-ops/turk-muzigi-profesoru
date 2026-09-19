package com.example.tmtuner.core.musicology.model

/**
 * Geleneksel Türk Makam Müziği makam tanımı modeli.
 */
data class MakamModel(
    val id: String,
    val name: String,
    val tonicNoteName: String,         // Karar / Durak perdesi
    val dominantNoteName: String,      // Güçlü perdesi
    val leadingNoteName: String = "",  // Yeden perdesi
    val suspendedTonics: List<String> = emptyList(), // Asma kararlar
    val seyirType: String = "İnici-Çıkıcı",
    val primaryCesni: String = "",
    val scaleKomaIntervals: List<Int> = emptyList(),
    val scaleFormulaLetters: List<String> = emptyList(),
    val scaleNotes: List<String> = emptyList(),
    val description: String = ""
) {
    companion object {
        val RAST = MakamModel(
            id = "rast",
            name = "Rast",
            tonicNoteName = "Rast",
            dominantNoteName = "Neva",
            leadingNoteName = "Irak",
            suspendedTonics = listOf("Segâh", "Dügâh"),
            seyirType = "Çıkıcı",
            primaryCesni = "Râst Beşlisi + Râst Dörtlüsü",
            scaleKomaIntervals = listOf(9, 8, 5, 9, 9, 8, 5),
            scaleFormulaLetters = listOf("T", "K", "S", "T", "T", "K", "S"),
            scaleNotes = listOf("Rast", "Dügâh", "Segâh", "Çârgâh", "Neva", "Hüseynî", "Eviç", "Gerdâniye"),
            description = "Rast makamı; neşeli, dingin ve asil seyre sahiptir. Kararı Rast perdesidir."
        )

        val USSAK = MakamModel(
            id = "ussak",
            name = "Uşşak",
            tonicNoteName = "Dügâh",
            dominantNoteName = "Neva",
            leadingNoteName = "Rast",
            suspendedTonics = listOf("Segâh"),
            seyirType = "Çıkıcı",
            primaryCesni = "Uşşâk Dörtlüsü + Bûselik Beşlisi",
            scaleKomaIntervals = listOf(8, 5, 9, 9, 4, 9, 9),
            scaleFormulaLetters = listOf("K", "S", "T", "T", "B", "T", "T"),
            scaleNotes = listOf("Dügâh", "Segâh", "Çârgâh", "Neva", "Hüseynî", "Acem", "Gerdâniye", "Muhayyer"),
            description = "Uşşak makamı; içli, hüzünlü ve mistik bir karaktere sahiptir. Kararı Dügâh perdesidir."
        )

        val BUSELIK = MakamModel(
            id = "buselik",
            name = "Buselik",
            tonicNoteName = "Dügâh",
            dominantNoteName = "Hüseynî",
            leadingNoteName = "Nîm Zîrgûle",
            suspendedTonics = listOf("Çârgâh", "Neva"),
            seyirType = "İnici-Çıkıcı",
            primaryCesni = "Bûselik Beşlisi + Kürdî / Hicaz Dörtlüsü",
            scaleKomaIntervals = listOf(9, 4, 9, 9, 4, 9, 9),
            scaleFormulaLetters = listOf("T", "B", "T", "T", "B", "T", "T"),
            scaleNotes = listOf("Dügâh", "Bûselik", "Çârgâh", "Neva", "Hüseynî", "Acem", "Gerdâniye", "Muhayyer"),
            description = "Bûselik makamı; Batı müziğindeki La minör dizisine benzer. Kararı Dügâh perdesidir."
        )

        val HICAZ = MakamModel(
            id = "hicaz",
            name = "Hicaz",
            tonicNoteName = "Dügâh",
            dominantNoteName = "Neva",
            leadingNoteName = "Rast",
            suspendedTonics = listOf("Dik Zîrgûle"),
            seyirType = "İnici-Çıkıcı",
            primaryCesni = "Hicaz Dörtlüsü + Rast Beşlisi",
            scaleKomaIntervals = listOf(5, 12, 5, 9, 8, 5, 9),
            scaleFormulaLetters = listOf("S", "A12", "S", "T", "K", "S", "T"),
            scaleNotes = listOf("Dügâh", "Dik Zîrgûle", "Neva", "Hüseynî", "Eviç", "Gerdâniye"),
            description = "Hicaz makamı; derin duygusal etki ve yanıklık taşır. Kararı Dügâh perdesidir."
        )

        val HUSEYNI = MakamModel(
            id = "huseyni",
            name = "Hüseyni",
            tonicNoteName = "Dügâh",
            dominantNoteName = "Hüseynî",
            leadingNoteName = "Rast",
            suspendedTonics = listOf("Neva", "Segâh"),
            seyirType = "İnici-Çıkıcı",
            primaryCesni = "Hüseynî Beşlisi + Uşşâk Dörtlüsü",
            scaleKomaIntervals = listOf(8, 5, 9, 9, 8, 5, 9),
            scaleFormulaLetters = listOf("K", "S", "T", "T", "K", "S", "T"),
            scaleNotes = listOf("Dügâh", "Segâh", "Çârgâh", "Neva", "Hüseynî", "Eviç", "Gerdâniye", "Muhayyer"),
            description = "Hüseyni makamı; yiğit, vakur ve duygulu bir yapıya sahiptir."
        )

        val NIHAVEND = MakamModel(
            id = "nihavend",
            name = "Nihavend",
            tonicNoteName = "Rast",
            dominantNoteName = "Neva",
            leadingNoteName = "Irak",
            suspendedTonics = listOf("Bûselik", "Çârgâh"),
            seyirType = "İnici-Çıkıcı",
            primaryCesni = "Bûselik Beşlisi + Hicaz / Kürdî Dörtlüsü",
            scaleKomaIntervals = listOf(9, 4, 9, 9, 4, 9, 9),
            scaleFormulaLetters = listOf("T", "B", "T", "T", "B", "T", "T"),
            scaleNotes = listOf("Rast", "Dügâh", "Kürdî", "Çârgâh", "Neva", "Hisâr", "Gerdâniye"),
            description = "Nihavend makamı; Râst perdesinde Bûselik dizisi ile icra edilir."
        )

        val SEGAH = MakamModel(
            id = "segah",
            name = "Segâh",
            tonicNoteName = "Segâh",
            dominantNoteName = "Neva",
            leadingNoteName = "Dügâh",
            suspendedTonics = listOf("Rast"),
            seyirType = "Çıkıcı",
            primaryCesni = "Segâh Beşlisi",
            scaleKomaIntervals = listOf(5, 9, 9, 8, 5, 9, 8),
            scaleFormulaLetters = listOf("S", "T", "T", "K", "S", "T", "K"),
            scaleNotes = listOf("Segâh", "Çârgâh", "Neva", "Hüseynî", "Eviç", "Gerdâniye", "Tîz Segâh"),
            description = "Segâh makamı; mistik, uhrevi ve derin bir tesire sahiptir. Kararı Segâh perdesidir."
        )

        val CARGAH = MakamModel(
            id = "cargah",
            name = "Çârgâh",
            tonicNoteName = "Çârgâh",
            dominantNoteName = "Gerdâniye",
            leadingNoteName = "Bûselik",
            suspendedTonics = listOf("Neva"),
            seyirType = "Çıkıcı",
            primaryCesni = "Çârgâh Beşlisi + Çârgâh Dörtlüsü",
            scaleKomaIntervals = listOf(9, 9, 4, 9, 9, 9, 4),
            scaleFormulaLetters = listOf("T", "T", "B", "T", "T", "T", "B"),
            scaleNotes = listOf("Çârgâh", "Neva", "Hüseynî", "Acem", "Gerdâniye", "Muhayyer", "Tîz Bûselik", "Tîz Çârgâh"),
            description = "Çârgâh makamı; Türk müziğinin ana dizisi kabul edilir (T-T-B-T-T-T-B = 53 koma)."
        )

        val CORE_MAKAMS = listOf(RAST, USSAK, BUSELIK, HICAZ, HUSEYNI, NIHAVEND, SEGAH, CARGAH)
    }
}
