package com.example.tmtuner.data.repository

import android.content.Context
import com.example.tmtuner.data.models.AhenkFrekanslar
import com.example.tmtuner.data.models.AhenkType
import com.example.tmtuner.data.models.SymbTrMakam
import com.example.tmtuner.data.models.SymbTrPerde
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow

/**
 * İsmail Hakkı Özkan Nazariyatı (s. 74-77, Tablo I) ve 53-EDO / SymbTr standartlarında
 * perde ve makam verilerini yöneten repository katmanı.
 */
object SymbTrRepository {

    private var cachedPerdeler: List<SymbTrPerde>? = null
    private var cachedMakamlar: List<SymbTrMakam>? = null

    /**
     * Tüm 53-EDO ve AEU SymbTr perdelerini assets/data/symbtr_perde_tablosu.json dosyasından okur.
     */
    fun getPerdeler(context: Context): List<SymbTrPerde> {
        cachedPerdeler?.let { return it }

        val list = mutableListOf<SymbTrPerde>()
        try {
            val jsonString = readAssetFile(context, "data/symbtr_perde_tablosu.json")
            val root = JSONObject(jsonString)
            val jsonArray = root.getJSONArray("perdeler")

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val frekObj = obj.getJSONObject("frekanslar")

                val mansur256 = frekObj.optDouble("mansur_fizik_cargah_256", frekObj.optDouble("mansur_la_440", 256.0))
                val bolahenk440 = frekObj.optDouble(
                    "bolahenk_neva_440",
                    frekObj.optDouble("botahenk_neva_440", frekObj.optDouble("bolanhenk_la_586", 330.0))
                )
                val kizBaskayitsiz = frekObj.optDouble("kiz_nevi_baskayitsiz", frekObj.optDouble("kiz_la_415", 345.50))

                val frekanslar = AhenkFrekanslar(
                    mansurFizikCargah256 = mansur256,
                    bolahenkNeva440 = bolahenk440,
                    kizNeviBaskayitsiz = kizBaskayitsiz,
                    botahenkNeva440 = bolahenk440,
                    mansurLa440 = mansur256,
                    kizLa415 = kizBaskayitsiz,
                    bolahenkLa586 = bolahenk440,
                    supurdeLa523 = bolahenk440 * (523.0 / 440.0)
                )

                val komaCargah = obj.optInt("koma_53_cargah", 0)
                val oktavKomaMod = obj.optInt("oktav_koma_mod", komaCargah)
                val mutlakKoma = obj.optInt("mutlak_koma", komaCargah)

                list.add(
                    SymbTrPerde(
                        id = obj.getString("id"),
                        perdeAdi = obj.getString("perde_adi"),
                        symbtrKodu = obj.getString("symbtr_kodu"),
                        batiKarsiligi = obj.getString("bati_karsiligi"),
                        aeuSembolu = obj.getString("aeu_sembolu"),
                        symbtrNotaNo = obj.optInt("symbtr_nota_no", 0),
                        oktavKomaMod = oktavKomaMod,
                        mutlakKoma = mutlakKoma,
                        koma53Cargah = komaCargah,
                        koma53Rast = obj.optInt("koma_53_rast", 0),
                        centDegeri = obj.optDouble("cent_degeri", 0.0),
                        pisagorOrani = obj.optString("pisagor_orani", "1/1"),
                        oranOndalik = obj.optDouble("oran_ondalik", 1.0),
                        temelPerdeMi = obj.optBoolean("temel_perde_mi", false),
                        makamFonksiyonu = obj.optString("makam_fonksiyonu", ""),
                        oktav = obj.optString("oktav", "Ana / Orta"),
                        frekanslar = frekanslar
                    )
                )
            }
            cachedPerdeler = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cachedPerdeler ?: emptyList()
    }

    /**
     * Yalnızca Türk Müziğinin 8 temel perdesini döndürür:
     * Râst, Dügâh, Segâh, Çârgâh, Nevâ, Hüseynî, Eviç, Gerdâniye
     */
    fun getFundamentalPerdeler(context: Context): List<SymbTrPerde> {
        return getPerdeler(context).filter { it.temelPerdeMi }
    }

    /**
     * Tüm makam tanımlarını assets/data/symbtr_makamlar.json dosyasından okur.
     */
    fun getMakamlar(context: Context): List<SymbTrMakam> {
        cachedMakamlar?.let { return it }

        val list = mutableListOf<SymbTrMakam>()
        try {
            val jsonString = readAssetFile(context, "data/symbtr_makamlar.json")
            val root = JSONObject(jsonString)
            val jsonArray = root.getJSONArray("makamlar")

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)

                val asmaArr = obj.optJSONArray("asma_kararlar")
                val asmaList = mutableListOf<String>()
                if (asmaArr != null) {
                    for (j in 0 until asmaArr.length()) asmaList.add(asmaArr.getString(j))
                }

                val aralikKomaArr = obj.optJSONArray("aralik_formulu_koma")
                val aralikKomaList = mutableListOf<Int>()
                if (aralikKomaArr != null) {
                    for (j in 0 until aralikKomaArr.length()) aralikKomaList.add(aralikKomaArr.getInt(j))
                }

                val aralikHarfArr = obj.optJSONArray("aralik_formulu_harf")
                val aralikHarfList = mutableListOf<String>()
                if (aralikHarfArr != null) {
                    for (j in 0 until aralikHarfArr.length()) aralikHarfList.add(aralikHarfArr.getString(j))
                }

                val perdeDiziArr = obj.optJSONArray("perde_dizisi")
                val perdeDiziList = mutableListOf<String>()
                if (perdeDiziArr != null) {
                    for (j in 0 until perdeDiziArr.length()) perdeDiziList.add(perdeDiziArr.getString(j))
                }

                val symbtrKodArr = obj.optJSONArray("symbtr_kodlari")
                val symbtrKodList = mutableListOf<String>()
                if (symbtrKodArr != null) {
                    for (j in 0 until symbtrKodArr.length()) symbtrKodList.add(symbtrKodArr.getString(j))
                }

                list.add(
                    SymbTrMakam(
                        id = obj.getString("id"),
                        adi = obj.getString("adi"),
                        kararPerdesi = obj.getString("karar_perdesi"),
                        kararPerdeId = obj.optString("karar_perde_id", ""),
                        gucluPerdesi = obj.getString("guclu_perdesi"),
                        gucluPerdeId = obj.optString("guclu_perde_id", ""),
                        asmaKararlar = asmaList,
                        yedenPerdesi = obj.optString("yeden_perdesi", ""),
                        seyirTipi = obj.optString("seyir_tipi", "İnici-Çıkıcı"),
                        diziCesnileri = obj.optString("dizi_cesnileri", ""),
                        aralikFormuluKoma = aralikKomaList,
                        aralikFormuluHarf = aralikHarfList,
                        perdeDizisi = perdeDiziList,
                        symbtrKodlari = symbtrKodList,
                        seyirOzeti = obj.optString("seyir_ozeti", "")
                    )
                )
            }
            cachedMakamlar = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cachedMakamlar ?: emptyList()
    }

    /**
     * Perdenin kesin frekansını `mutlak_koma` üzerinden hesaplar.
     * - Bolâhenk formülü: 330.0 * 2.0.pow(((perde.mutlak_koma + nuanceOffset) - 40) / 53.0)
     * - Mansur formülü: 256.0 * 2.0.pow((perde.mutlak_koma + nuanceOffset) / 53.0)
     * - Eb Alto Saksafon: Çıktıyı doğrudan `* (27.0 / 16.0)` ile çarpar.
     */
    fun calculatePerdeFrequency(
        perde: SymbTrPerde,
        ahenk: AhenkType,
        nuanceOffset: Int = 0,
        isEbAltoSax: Boolean = false
    ): Double {
        val coreAhenk = com.example.tmtuner.core.musicology.model.Ahenk.fromString(ahenk.name)
        val rawFrequency = com.example.tmtuner.core.musicology.math.Edo53Calculator.calculateAhenkPitch(
            perde.mutlakKoma + nuanceOffset,
            coreAhenk
        )
        return if (isEbAltoSax) com.example.tmtuner.core.musicology.math.Edo53Calculator.toEbAltoPitch(rawFrequency) else rawFrequency
    }

    /**
     * Ahenk ve icra toleransına (nuanceOffset: Uşşak/Segâh için 1-2 koma pes basabilme) göre frekans üretir.
     */
    fun getFrequency(
        perde: SymbTrPerde,
        ahenk: AhenkType,
        nuanceOffset: Int = 0,
        isEbAltoSax: Boolean = false
    ): Double {
        return calculatePerdeFrequency(perde, ahenk, nuanceOffset, isEbAltoSax)
    }

    /**
     * String ahenk adını destekleyen aşırı yükleme (overload).
     */
    fun getFrequency(
        perde: SymbTrPerde,
        ahenk: String,
        nuanceOffset: Int = 0,
        isEbAltoSax: Boolean = false
    ): Double {
        val ahenkType = AhenkType.fromString(ahenk)
        return calculatePerdeFrequency(perde, ahenkType, nuanceOffset, isEbAltoSax)
    }

    /**
     * Verilen frekansa en yakın SymbTr perdesini ve aradaki cent farkını hesaplar.
     */
    fun findClosestPerde(
        frequency: Double,
        ahenk: String,
        context: Context,
        nuanceOffset: Int = 0,
        isEbAltoSax: Boolean = false
    ): Pair<SymbTrPerde, Float>? {
        val perdeler = getPerdeler(context)
        if (perdeler.isEmpty() || frequency <= 0.0) return null

        var minDiff = Double.MAX_VALUE
        var closest: SymbTrPerde? = null

        for (perde in perdeler) {
            val offset = if (perde.id == "segah") nuanceOffset else 0
            val targetFreq = calculatePerdeFrequency(perde, AhenkType.fromString(ahenk), offset, isEbAltoSax)
            val diff = abs(targetFreq - frequency)
            if (diff < minDiff) {
                minDiff = diff
                closest = perde
            }
        }

        return closest?.let {
            val offset = if (it.id == "segah") nuanceOffset else 0
            val targetFreq = calculatePerdeFrequency(it, AhenkType.fromString(ahenk), offset, isEbAltoSax)
            val centsDiff = (1200.0 * log2(frequency / targetFreq)).toFloat()
            Pair(it, centsDiff)
        }
    }

    private fun readAssetFile(context: Context, fileName: String): String {
        val inputStream = context.assets.open(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val sb = StringBuilder()
        var line: String? = reader.readLine()
        while (line != null) {
            sb.append(line)
            line = reader.readLine()
        }
        reader.close()
        inputStream.close()
        return sb.toString()
    }
}
