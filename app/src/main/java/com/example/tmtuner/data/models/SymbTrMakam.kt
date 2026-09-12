package com.example.tmtuner.data.models

/**
 * Açık kaynak SymbTr veri tabanına dayalı Makam tanım modeli.
 */
data class SymbTrMakam(
    val id: String,
    val adi: String,
    val kararPerdesi: String,
    val kararPerdeId: String,
    val gucluPerdesi: String,
    val gucluPerdeId: String,
    val asmaKararlar: List<String> = emptyList(),
    val yedenPerdesi: String = "",
    val seyirTipi: String = "İnici-Çıkıcı",
    val diziCesnileri: String = "",
    val aralikFormuluKoma: List<Int> = emptyList(),
    val aralikFormuluHarf: List<String> = emptyList(),
    val perdeDizisi: List<String> = emptyList(),
    val symbtrKodlari: List<String> = emptyList(),
    val seyirOzeti: String = "",
    val perdeler: List<SymbTrPerde> = emptyList(),
    val durakPerde: SymbTrPerde = SymbTrPerde.DEFAULT,
    val gucluPerde: SymbTrPerde = SymbTrPerde.DEFAULT,
    val makamAdi: String = adi
)
