package com.example.tmtuner.data.models

/**
 * Ahenk türleri: İsmail Hakkı Özkan ve Türk Musikisi icra standardı.
 */
enum class AhenkType(val displayName: String) {
    MANSUR("Mansur (Fizik Çârgâh = 256 Hz)"),
    BOLAHENK("Bolâhenk (Nevâ = 440 Hz / Dügâh = 330 Hz)"),
    KIZ("Kız (Neyi Başkayıtsız / La = 415 Hz)"),
    SUPURDE("Süpürde (La = 523 Hz)");

    companion object {
        fun fromString(name: String): AhenkType {
            return when {
                name.contains("Bolahenk", ignoreCase = true) || name.contains("Bolâhenk", ignoreCase = true) -> BOLAHENK
                name.contains("Kız", ignoreCase = true) || name.contains("Kiz", ignoreCase = true) -> KIZ
                name.contains("Süpürde", ignoreCase = true) || name.contains("Supurde", ignoreCase = true) -> SUPURDE
                else -> MANSUR
            }
        }
    }
}

/**
 * İsmail Hakkı Özkan Nazariyatı (s. 74-77, Tablo I) ve 53-EDO / SymbTr standart perde modeli.
 * [oktavKomaMod] ve [mutlakKoma] alanları non-nullable olarak oktav hiyerarşisini tanımlar.
 */
data class SymbTrPerde(
    val id: String,
    val perdeAdi: String,
    val symbtrKodu: String,
    val batiKarsiligi: String,
    val aeuSembolu: String,
    val symbtrNotaNo: Int,
    val oktavKomaMod: Int,
    val mutlakKoma: Int,
    val koma53Cargah: Int,
    val koma53Rast: Int,
    val centDegeri: Double,
    val pisagorOrani: String,
    val oranOndalik: Double,
    val temelPerdeMi: Boolean,
    val makamFonksiyonu: String,
    val oktav: String,
    val frekanslar: AhenkFrekanslar
) {
    companion object {
        val DEFAULT = SymbTrPerde(
            id = "rast",
            perdeAdi = "Râst",
            symbtrKodu = "rast_4",
            batiKarsiligi = "G4",
            aeuSembolu = "G",
            symbtrNotaNo = 32,
            oktavKomaMod = 31,
            mutlakKoma = 31,
            koma53Cargah = 31,
            koma53Rast = 0,
            centDegeri = 701.96,
            pisagorOrani = "3/2",
            oranOndalik = 1.5,
            temelPerdeMi = true,
            makamFonksiyonu = "Karar",
            oktav = "Ana / Orta",
            frekanslar = AhenkFrekanslar(384.0, 293.33, 347.65)
        )
    }
}

/**
 * Perdenin Özkan Tablo I ve Ahenk sistemlerine göre hesaplanmış frekans değerleri.
 */
data class AhenkFrekanslar(
    val mansurFizikCargah256: Double,
    val bolahenkNeva440: Double,
    val kizNeviBaskayitsiz: Double,
    // Geriye dönük uyumluluk alanları
    val botahenkNeva440: Double = bolahenkNeva440,
    val mansurLa440: Double = mansurFizikCargah256,
    val kizLa415: Double = kizNeviBaskayitsiz,
    val bolahenkLa586: Double = bolahenkNeva440,
    val supurdeLa523: Double = bolahenkNeva440 * (523.0 / 440.0)
)
