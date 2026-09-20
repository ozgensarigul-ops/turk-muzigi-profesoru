package com.example.tmtuner.core.musicology.atlas

import com.example.tmtuner.core.musicology.math.Edo53Calculator
import com.example.tmtuner.core.musicology.model.Ahenk
import com.example.tmtuner.core.musicology.model.PerdeNote
import kotlin.math.pow

/**
 * Arel-Ezgi-Uzdilek (AEU) ve İsmail Hakkı Özkan Tablo I (s. 74-77)
 * saf Pisagor kesirleri ile 3 Oktavlık (72 Perde) Perde Atlası.
 */
object AeuScaleAtlas {

    /**
     * Özkan Tablo I 24 Sesli Pisagorik Aralık Oranları (Tam Beşliler Zinciri):
     * T: 9/8, K: 65536/59049, S: 2187/2048, B: 256/243
     */
    val AEU_RATIOS = Edo53Calculator.OZKAN_TABLO_I_RATIOS

    /**
     * Pes / Kaba (Oktav 0) Perde İsimleri (24 perde).
     */
    val NAMES_OCTAVE_0 = arrayOf(
        "Kaba Çârgâh", "Kaba Nîm Hicâz", "Kaba Hicâz", "Kaba Dik Hicâz",
        "Yegâh", "Kaba Nîm Hisâr", "Kaba Hisâr", "Kaba Dik Hisâr",
        "Hüseynî Aşîrân", "Acem Aşîrân", "Dik Acem Aşîrân", "Irak",
        "Geveşt", "Dik Geveşt", "Rast", "Nîm Zîrgûle",
        "Zîrgûle", "Dik Zîrgûle", "Dügâh", "Kürdî",
        "Dik Kürdî", "Segâh", "Bûselik", "Dik Bûselik"
    )

    /**
     * Orta / Ana (Oktav 1) Perde İsimleri (24 perde).
     */
    val NAMES_OCTAVE_1 = arrayOf(
        "Çârgâh", "Nîm Hicâz", "Hicâz", "Dik Hicâz",
        "Neva", "Nîm Hisâr", "Hisâr", "Dik Hisâr",
        "Hüseynî", "Acem", "Dik Acem", "Eviç",
        "Mahur", "Dik Mahur", "Gerdâniye", "Nîm Şehnâz",
        "Şehnâz", "Dik Şehnâz", "Muhayyer", "Sünbüle",
        "Dik Sünbüle", "Tîz Segâh", "Tîz Bûselik", "Tîz Dik Bûselik"
    )

    /**
     * Tîz / En Tîz (Oktav 2) Perde İsimleri (24 perde).
     */
    val NAMES_OCTAVE_2 = arrayOf(
        "Tîz Çârgâh", "Tîz Nîm Hicâz", "Tîz Hicâz", "Tîz Dik Hicâz",
        "Tîz Neva", "Tîz Nîm Hisâr", "Tîz Hisâr", "Tîz Dik Hisâr",
        "Tîz Hüseynî", "Tîz Acem", "Tîz Dik Acem", "Tîz Eviç",
        "Tîz Mahur", "Tîz Dik Mahur", "Tîz Gerdâniye", "Tîz Nîm Şehnâz",
        "Tîz Şehnâz", "Tîz Dik Şehnâz", "Tîz Muhayyer", "Tîz Sünbüle",
        "Tîz Dik Sünbüle", "En Tîz Segâh", "En Tîz Bûselik", "En Tîz Dik Bûselik"
    )

    /**
     * Türk Müziği gamının omurgasını oluşturan 8 Temel Perde Adı.
     */
    val FUNDAMENTAL_PERDE_NAMES = setOf(
        "Rast", "Dügâh", "Segâh", "Çârgâh", "Neva", "Hüseynî", "Eviç", "Gerdâniye"
    )

    /**
     * Verilen ahenk düzenine göre 3 oktav boyunca tüm 72 perdenin frekans ve özelliklerini
     * Özkan Tablo I saf Pisagor kesirleri ile analitik olarak üretir.
     */
    fun buildPitchAtlas(ahenk: Ahenk): List<PerdeNote> {
        // Analitik temel Kaba Çârgâh frekansı
        val baseFreq = when (ahenk) {
            Ahenk.BOLAHENK -> Edo53Calculator.BOLAHENK_KABA_CARGAH_BASE // 440 * (4/9) ≈ 195.5556 Hz
            Ahenk.MANSUR -> Edo53Calculator.MANSUR_KABA_CARGAH_BASE     // 256.0 Hz (Fizik Çârgâh)
            Ahenk.KIZ -> Edo53Calculator.BOLAHENK_KABA_CARGAH_BASE * (16.0 / 9.0) // Tam Dörtlü tiz (+22 koma, 4/3 oranı)
            Ahenk.SUPURDE -> Edo53Calculator.BOLAHENK_KABA_CARGAH_BASE * (523.2 / 440.0)
            Ahenk.MUSTAHSEN -> Edo53Calculator.BOLAHENK_KABA_CARGAH_BASE * (495.0 / 440.0)
            Ahenk.YILDIZ -> Edo53Calculator.BOLAHENK_KABA_CARGAH_BASE * (469.33 / 440.0)
            Ahenk.SAH -> Edo53Calculator.BOLAHENK_KABA_CARGAH_BASE * (391.11 / 440.0)
            Ahenk.DAVUD -> Edo53Calculator.BOLAHENK_KABA_CARGAH_BASE * (366.27 / 440.0)
        }

        val list = ArrayList<PerdeNote>(72)

        for (octave in 0..2) {
            val names = when (octave) {
                0 -> NAMES_OCTAVE_0
                1 -> NAMES_OCTAVE_1
                else -> NAMES_OCTAVE_2
            }
            val octaveName = when (octave) {
                0 -> "Kaba / Pes"
                1 -> "Ana / Orta"
                else -> "Tîz / En Tîz"
            }
            val octaveMultiplier = 2.0.pow(octave)

            for (i in 0..23) {
                val name = names[i]
                val ratio = AEU_RATIOS[i]
                val frequency = baseFreq * ratio * octaveMultiplier
                val isFundamental = FUNDAMENTAL_PERDE_NAMES.contains(name)

                list.add(
                    PerdeNote(
                        id = name.lowercase().replace(" ", "_").replace("â", "a").replace("î", "i"),
                        name = name,
                        octaveIndex = octave,
                        octaveName = octaveName,
                        frequency = frequency,
                        pythagoreanRatio = ratio * octaveMultiplier,
                        isFundamental = isFundamental
                    )
                )
            }
        }
        return list
    }

    /**
     * Makamın karar perdesi için analitik drone (dem) frekansını hesaplar.
     */
    fun calculateDroneFrequency(makamName: String, ahenk: Ahenk): Pair<String, Double> {
        val baseFreq = when (ahenk) {
            Ahenk.BOLAHENK -> Edo53Calculator.BOLAHENK_KABA_CARGAH_BASE // 1760/9 Hz
            Ahenk.MANSUR -> Edo53Calculator.BOLAHENK_KABA_CARGAH_BASE   // Mansur diyapazonda Dügâh 330 Hz referansı
            Ahenk.KIZ -> Edo53Calculator.BOLAHENK_KABA_CARGAH_BASE * (16.0 / 9.0) // Tam Dörtlü tiz (+22 koma, 4/3 oranı)
            Ahenk.SUPURDE -> Edo53Calculator.BOLAHENK_KABA_CARGAH_BASE * (523.2 / 440.0)
            Ahenk.MUSTAHSEN -> Edo53Calculator.BOLAHENK_KABA_CARGAH_BASE * (495.0 / 440.0)
            Ahenk.YILDIZ -> Edo53Calculator.BOLAHENK_KABA_CARGAH_BASE * (469.33 / 440.0)
            Ahenk.SAH -> Edo53Calculator.BOLAHENK_KABA_CARGAH_BASE * (391.11 / 440.0)
            Ahenk.DAVUD -> Edo53Calculator.BOLAHENK_KABA_CARGAH_BASE * (366.27 / 440.0)
        }

        return when {
            makamName.contains("Rast", ignoreCase = true) ||
            makamName.contains("Nihavend", ignoreCase = true) ||
            makamName.contains("Mahur", ignoreCase = true) -> {
                // Rast perdesi: Tam Beşli (3/2) = (1760/9) * 1.5 = 880/3 ≈ 293.333 Hz
                Pair("Râst Perdesi (Sol)", baseFreq * 1.5)
            }
            makamName.contains("Segah", ignoreCase = true) ||
            makamName.contains("Segâh", ignoreCase = true) -> {
                // Segâh perdesi: 4096/2187 oranı
                Pair("Segâh Perdesi (Si♭₁)", baseFreq * (4096.0 / 2187.0))
            }
            makamName.contains("Cargah", ignoreCase = true) ||
            makamName.contains("Çârgâh", ignoreCase = true) -> {
                // Çârgâh perdesi: 2.0 oranı (Orta Çârgâh = 3520/9 ≈ 391.111 Hz)
                Pair("Çârgâh Perdesi (Do)", baseFreq * 2.0)
            }
            else -> {
                // Standart Dügâh Kararı (Uşşak, Hicaz, Hüseyni vb.): 27/16 = 330.0 Hz
                Pair("Dügâh Perdesi (La)", baseFreq * (27.0 / 16.0))
            }
        }
    }
}
