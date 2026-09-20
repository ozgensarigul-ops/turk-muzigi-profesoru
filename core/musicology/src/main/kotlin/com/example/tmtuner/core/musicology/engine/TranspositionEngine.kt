package com.example.tmtuner.core.musicology.engine

import com.example.tmtuner.core.musicology.math.Edo53Calculator
import com.example.tmtuner.core.musicology.model.Ahenk
import com.example.tmtuner.core.musicology.model.NeyType
import com.example.tmtuner.core.musicology.model.TransposingInstrument
import kotlin.math.pow

/**
 * Türk Makam Müziği kuramı (İsmail Hakkı Özkan & AEU 53-EDO) standartlarında
 * dinamik ahenk, ney ve batı transpoze enstrüman servis motoru.
 */
object TranspositionEngine {

    // =========================================================================
    // 1. Mansur, Bolâhenk ve Kız Standart Referans Sabitleri (İsmail Hakkı Özkan s. 87)
    // =========================================================================

    /** Mansur Fizik Düzeninde Kaba Çârgâh Referansı = 256.0 Hz */
    const val MANSUR_KABA_CARGAH_HZ: Double = 256.0

    /** Mansur Diyapazon Düzeninde Dügâh (Konser La4) Referansı = 440.0 Hz */
    const val MANSUR_DUGAH_HZ: Double = 440.0

    /** Mansur Fizik Düzeninde Dügâh Referansı: 256 * (27/16) = 432.0 Hz */
    const val MANSUR_DUGAH_PHYSICAL_HZ: Double = 256.0 * (27.0 / 16.0)

    /** Mansur Referans Koma ve Oranı */
    const val MANSUR_KOMA_SHIFT: Int = 0
    const val MANSUR_RATIO: Double = 1.0

    /** Bolâhenk Düzeninde Nevâ (Re5) Referansı = 440.0 Hz */
    const val BOLAHENK_NEVA_HZ: Double = 440.0

    /** Bolâhenk Düzeninde Analitik Pisagor Dügâh Referansı = 440.0 * (3/4) = 330.0 Hz */
    const val BOLAHENK_DUGAH_PYTHAGOREAN_HZ: Double = 330.0

    /** Bolâhenk Düzeninde 12-TET Tampere Dügâh Referansı ≈ 329.6275 Hz (440 * 2^(-5/12)) */
    const val BOLAHENK_DUGAH_12TET_HZ: Double = 329.6275569

    /** Bolâhenk Düzeninde Kaba Çârgâh Referansı = 440.0 * (4/9) ≈ 195.5556 Hz */
    const val BOLAHENK_KABA_CARGAH_HZ: Double = (440.0 * 4.0) / 9.0

    /** Bolâhenk Âhengi Koma Kayması (Mansur'a göre Tam Dörtlü pes) = -22 koma */
    const val BOLAHENK_KOMA_FROM_MANSUR: Int = -22

    /** Bolâhenk Âhengi Frekans Oranı (Mansur'a göre Tam Dörtlü pes) = 3/4 */
    const val BOLAHENK_RATIO_FROM_MANSUR: Double = 3.0 / 4.0

    /** Kız Âhengi Dügâh Referansı = 440.0 * (4/3) ≈ 586.6667 Hz */
    const val KIZ_DUGAH_HZ: Double = 440.0 * (4.0 / 3.0)

    /** Kız Âhengi Koma Kayması (Mansur'a göre Tam Dörtlü tiz) = +22 koma */
    const val KIZ_KOMA_FROM_MANSUR: Int = 22

    /** Kız Âhengi Frekans Oranı (Mansur'a göre Tam Dörtlü tiz) = 4/3 */
    const val KIZ_RATIO_FROM_MANSUR: Double = 4.0 / 3.0

    /** Eb Alto Saksafon Yazılı -> Konsert Çarpanı (Büyük 6'lı pes: 16/27) */
    const val EB_ALTO_WRITTEN_TO_CONCERT_RATIO: Double = 16.0 / 27.0

    /** Eb Alto Saksafon Konsert -> Yazılı Çarpanı (Büyük 6'lı tiz: 27/16) */
    const val EB_ALTO_CONCERT_TO_WRITTEN_RATIO: Double = 27.0 / 16.0

    // =========================================================================
    // 2. Ahenk Referans Frekansları & Dönüşümleri
    // =========================================================================

    /**
     * Verilen ahenk için temel Kaba Çârgâh frekansını döndürür.
     */
    fun getKabaCargahFrequency(ahenk: Ahenk): Double {
        return when (ahenk) {
            Ahenk.MANSUR -> MANSUR_KABA_CARGAH_HZ
            Ahenk.BOLAHENK -> BOLAHENK_KABA_CARGAH_HZ
            Ahenk.KIZ -> KIZ_DUGAH_HZ * (16.0 / 27.0)
            else -> ahenk.getKabaCargahBaseFrequency()
        }
    }

    /**
     * Verilen ahenk için Dügâh (La) referans frekansını döndürür.
     */
    fun getDugahFrequency(ahenk: Ahenk, use12TetForBolahenk: Boolean = false): Double {
        return when (ahenk) {
            Ahenk.MANSUR -> MANSUR_DUGAH_HZ
            Ahenk.BOLAHENK -> if (use12TetForBolahenk) BOLAHENK_DUGAH_12TET_HZ else BOLAHENK_DUGAH_PYTHAGOREAN_HZ
            Ahenk.KIZ -> KIZ_DUGAH_HZ
            else -> ahenk.referenceLa
        }
    }

    /**
     * İki geleneksel Ahenk arasında dinamik frekans transpozisyonu yapar.
     * Örneğin Mansur'daki bir perdeyi Bolâhenk veya Kız tınısına dönüştürür.
     */
    fun transposeBetweenAhenks(
        frequency: Double,
        sourceAhenk: Ahenk,
        targetAhenk: Ahenk
    ): Double {
        if (sourceAhenk == targetAhenk || frequency <= 0.0) return frequency
        val sourceBase = sourceAhenk.referenceLa
        val targetBase = targetAhenk.referenceLa
        return frequency * (targetBase / sourceBase)
    }

    // =========================================================================
    // 3. Ney Çeşitleri Transpozisyonu
    // =========================================================================

    /**
     * Belirtilen Ney çeşidinin açkı (Râst) perdesinin referans frekansını döner.
     * Mansur Ney için Râst = 256 * 3/2 = 384.0 Hz (Fizik) veya Diyapazonda ~293.33 Hz.
     */
    fun getNeyReferenceFrequency(
        neyType: NeyType,
        baseCargah: Double = MANSUR_KABA_CARGAH_HZ,
        useExactRatio: Boolean = false
    ): Double {
        val mansurRast = baseCargah * 1.5
        return if (useExactRatio) {
            mansurRast * neyType.ratioFromMansur
        } else {
            Edo53Calculator.frequencyAtKoma(mansurRast, neyType.komaShiftFromMansur.toDouble())
        }
    }

    /**
     * İki farklı Ney boyu arasında parmak pozisyonu frekans aktarımı yapar.
     * Örneğin Mansur Ney ile çalınan bir ezgiyi Kız Ney ile çalındığında çıkacak frekansa dönüştürür.
     */
    fun transposeBetweenNeys(
        frequency: Double,
        sourceNey: NeyType,
        targetNey: NeyType,
        useExactRatio: Boolean = false
    ): Double {
        if (sourceNey == targetNey || frequency <= 0.0) return frequency
        return if (useExactRatio) {
            frequency * (targetNey.ratioFromMansur / sourceNey.ratioFromMansur)
        } else {
            val komaDiff = targetNey.komaShiftFromMansur - sourceNey.komaShiftFromMansur
            Edo53Calculator.frequencyAtKoma(frequency, komaDiff.toDouble())
        }
    }

    // =========================================================================
    // 4. Batı Transpoze Enstrüman Servisi (Eb Alto, Bb Tenor vb.)
    // =========================================================================

    /**
     * Enstrümanın yazılı notasından havada tınlayan konsert frekansına dönüştürür.
     *
     * @param instrumentPitch Enstrüman icracısının okuduğu/üflediği frekans
     * @param instrument Transpoze enstrüman sınıfı (Eb Alto Sax, Bb Tenor Sax vb.)
     * @param asMinorThird Eb Alto için büyük 6'lı pest (false - varsayılan) veya aynı oktavda küçük 3'lü tiz (true)
     */
    fun toConcertPitch(
        instrumentPitch: Double,
        instrument: TransposingInstrument,
        asMinorThird: Boolean = false
    ): Double {
        return instrument.toConcertPitch(instrumentPitch, asMinorThird)
    }

    /**
     * Akustik konsert frekansını enstrüman icracısının parmak/yazılı pozisyonuna dönüştürür.
     *
     * @param concertPitch Ortamda duyulan, mikrofonun yakaladığı mutlak frekans
     * @param instrument Transpoze enstrüman sınıfı (Eb Alto Sax, Bb Tenor Sax vb.)
     * @param asMinorThird Eb Alto için büyük 6'lı tiz (false - varsayılan) veya küçük 3'lü pest (true)
     */
    fun toInstrumentPitch(
        concertPitch: Double,
        instrument: TransposingInstrument,
        asMinorThird: Boolean = false
    ): Double {
        return instrument.toInstrumentPitch(concertPitch, asMinorThird)
    }

    // =========================================================================
    // 5. Birleşik ve Koma Bazlı Transpozisyon
    // =========================================================================

    /**
     * Mikrofondan gelen ham ses sinyalini hem seçilen Ahenk hem de Enstrüman sınıfına göre
     * normalize ederek Türk Müziği perde atlasındaki karşılık frekansına dönüştürür.
     */
    fun normalizeDetectedPitch(
        rawFrequency: Double,
        instrument: TransposingInstrument,
        sourceAhenk: Ahenk,
        targetAhenk: Ahenk = Ahenk.MANSUR,
        asMinorThird: Boolean = false
    ): Double {
        if (rawFrequency <= 0.0) return 0.0
        // 1. Adım: Enstrümanın transpozesini telafi ederek yazılı perdeyi bul
        val instrumentCompensated = instrument.toInstrumentPitch(rawFrequency, asMinorThird)
        // 2. Adım: Ahenk farkını Mansur referansına ölçekle
        return transposeBetweenAhenks(instrumentCompensated, sourceAhenk, targetAhenk)
    }

    /**
     * 53-EDO koma indeksini (0..52) enstrüman transpozisyon ofsetine göre kaydırır.
     */
    fun transposeKomaForInstrument(
        komaIndex: Int,
        instrument: TransposingInstrument
    ): Int {
        val shifted = komaIndex + instrument.komaOffsetFromConcert
        return (shifted % 53 + 53) % 53
    }
}
