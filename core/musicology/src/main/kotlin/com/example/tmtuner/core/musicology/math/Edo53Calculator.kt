package com.example.tmtuner.core.musicology.math

import com.example.tmtuner.core.musicology.model.Ahenk
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * 53 Eşit Bölümlü (53-EDO / Holderian / Turel) Mikrotonal Ses Sistemi ve Matematiksel Motoru.
 * İsmail Hakkı Özkan "Türk Mûsikîsi Nazariyatı ve Usûlleri" (s. 74-77, Tablo I)
 * saf Pisagor kesirleri (T: 9/8, K: 65536/59049, S: 2187/2048, B: 256/243, F: 531441/524288)
 * temel alınarak analitik olarak tasarlanmıştır.
 */
object Edo53Calculator {

    /** Bir oktavdaki eşit koma sayısı */
    const val KOMA_PER_OCTAVE = 53

    /** Bir Holder komasının tam cent değeri: 1200 / 53 ≈ 22.64150943 Cent */
    const val HOLDER_KOMA_CENT = 1200.0 / 53.0

    // =========================================================================
    // Özkan Tablo I Saf Pisagor Aralık Oranları
    // =========================================================================
    /** Tanini (T = 9/8) */
    const val RATIO_T = 9.0 / 8.0

    /** Büyük Mücennep (K = 65536/59049) */
    const val RATIO_K = 65536.0 / 59049.0

    /** Küçük Mücennep (S = 2187/2048) */
    const val RATIO_S = 2187.0 / 2048.0

    /** Bakiye (B = 256/243) */
    const val RATIO_B = 256.0 / 243.0

    /** Fazla / Pisagor Koması (F = T / K = 531441/524288 ≈ 1.01364) */
    const val RATIO_F = 531441.0 / 524288.0

    /** Bolâhenk Düzeni Nevâ Referansı = 440.0 Hz */
    const val BOLAHENK_NEVA_REFERENCE = 440.0

    /** Bolâhenk Düzeninde Analitik Kaba Çârgâh Referansı = 440.0 * (4/9) ≈ 195.5556 Hz */
    const val BOLAHENK_KABA_CARGAH_BASE = (440.0 * 4.0) / 9.0

    /** Mansur Fizik Düzeni Kaba Çârgâh Referansı = 256.0 Hz */
    const val MANSUR_KABA_CARGAH_BASE = 256.0

    /**
     * Özkan Tablo I'deki 24 perdenin Kaba Çârgâh'a (1/1) göre saf Pisagorik oranları (0..23).
     */
    val OZKAN_TABLO_I_RATIOS = doubleArrayOf(
        1.0,                                // 0: Çârgâh (1/1)
        256.0 / 243.0,                      // 1: Nîm Hicâz (B = 256/243)
        2187.0 / 2048.0,                    // 2: Hicâz (S = 2187/2048)
        65536.0 / 59049.0,                  // 3: Dik Hicâz (K = 65536/59049)
        9.0 / 8.0,                          // 4: Yegâh / Nevâ (T = 9/8)
        32.0 / 27.0,                        // 5: Nîm Hisâr (T * B = 32/27)
        19683.0 / 16384.0,                  // 6: Hisâr (T * S = 19683/16384)
        8192.0 / 6561.0,                    // 7: Dik Hisâr (T * K = 8192/6561)
        81.0 / 64.0,                        // 8: Hüseynî (T^2 = 81/64)
        4.0 / 3.0,                          // 9: Acem (T^2 * B = 4/3)
        177147.0 / 131072.0,                // 10: Dik Acem (T^2 * S = 177147/131072)
        1024.0 / 729.0,                     // 11: Eviç / Irak (T^2 * K = 1024/729)
        729.0 / 512.0,                      // 12: Mahur / Geveşt (T^3 = 729/512)
        262144.0 / 177147.0,                // 13: Dik Mahur / Dik Geveşt (T^3 * B = 262144/177147)
        3.0 / 2.0,                          // 14: Gerdâniye / Rast (Tam Beşli = 3/2)
        128.0 / 81.0,                       // 15: Nîm Şehnâz / Nîm Zîrgûle (3/2 * B = 128/81)
        6561.0 / 4096.0,                    // 16: Şehnâz / Zîrgûle (3/2 * S = 6561/4096)
        32768.0 / 19683.0,                  // 17: Dik Şehnâz / Dik Zîrgûle (3/2 * K = 32768/19683)
        27.0 / 16.0,                        // 18: Muhayyer / Dügâh (3/2 * T = 27/16)
        16.0 / 9.0,                         // 19: Sünbüle / Kürdî (27/16 * B = 16/9)
        59049.0 / 32768.0,                  // 20: Dik Sünbüle / Dik Kürdî (27/16 * S = 59049/32768)
        4096.0 / 2187.0,                    // 21: Tîz Segâh / Segâh (27/16 * K = 4096/2187)
        243.0 / 128.0,                      // 22: Tîz Bûselik / Bûselik (27/16 * T = 243/128)
        1048576.0 / 531441.0                // 23: Tîz Dik Bûselik / Dik Bûselik (243/128 * B = 1048576/531441)
    )

    /**
     * 53-EDO koma modunda (0..52) en yakın Özkan Tablo I saf Pisagor oranını eşler.
     */
    fun getPythagoreanRatioForKoma(komaMod53: Int): Double {
        return when (komaMod53) {
            0 -> 1.0
            4 -> 256.0 / 243.0
            5 -> 2187.0 / 2048.0
            8 -> 65536.0 / 59049.0
            9 -> 9.0 / 8.0
            13 -> 32.0 / 27.0
            14 -> 19683.0 / 16384.0
            17 -> 8192.0 / 6561.0
            18 -> 81.0 / 64.0
            22 -> 4.0 / 3.0
            23 -> 177147.0 / 131072.0
            26 -> 1024.0 / 729.0
            27 -> 729.0 / 512.0
            30 -> 262144.0 / 177147.0
            31 -> 3.0 / 2.0
            35 -> 128.0 / 81.0
            36 -> 6561.0 / 4096.0
            39 -> 32768.0 / 19683.0
            40 -> 27.0 / 16.0
            44 -> 16.0 / 9.0
            45 -> 59049.0 / 32768.0
            48, 49 -> 4096.0 / 2187.0 // Segâh perdesi
            52 -> 1048576.0 / 531441.0
            else -> 2.0.pow(komaMod53 / 53.0)
        }
    }

    /**
     * Temel frekanstan (f0) n koma uzaklıktaki frekansı hesaplar:
     * fn = f0 * 2^(n / 53)
     */
    fun frequencyAtKoma(baseFrequency: Double, komaOffset: Double): Double {
        return baseFrequency * 2.0.pow(komaOffset / KOMA_PER_OCTAVE.toDouble())
    }

    /**
     * İki frekans arasındaki Cent farkını hesaplar:
     * cents = 1200 * log2(f1 / f2)
     */
    fun centsBetween(f1: Double, f2: Double): Double {
        if (f1 <= 0.0 || f2 <= 0.0) return 0.0
        return 1200.0 * log2(f1 / f2)
    }

    /**
     * İki frekans arasındaki Koma farkını hesaplar:
     * koma = 53 * log2(f1 / f2)
     */
    fun komasBetween(f1: Double, f2: Double): Double {
        if (f1 <= 0.0 || f2 <= 0.0) return 0.0
        return 53.0 * log2(f1 / f2)
    }

    /**
     * Verilen frekansın temel referans frekansına göre 53-EDO koma gridindeki indeksini döner (0..52).
     */
    fun komaIndex(frequency: Double, baseFrequency: Double): Int {
        if (frequency <= 0.0 || baseFrequency <= 0.0) return 0
        val raw = (53.0 * log2(frequency / baseFrequency)).roundToInt()
        return (raw % 53 + 53) % 53
    }

    /**
     * Bolâhenk düzeninde (Nevâ = 440.0 Hz referansı) mutlak koma değerinden analitik frekans üretir:
     * - Dügâh (40 mutlak koma) = (440.0 * 4/9) * (27/16) = 330.0 Hz (Tam eşitlik)
     * - Nevâ (49 veya 62 mutlak koma) = 440.0 Hz (Tam eşitlik)
     * - Râst (31 mutlak koma) = (440.0 * 4/9) * (3/2) = 880/3 ≈ 293.333 Hz
     * - Çârgâh (22 mutlak koma) = (440.0 * 4/9) * 2 = 3520/9 ≈ 391.111 Hz
     */
    fun toBolahenkPitch(mutlakKoma: Int): Double {
        return when (mutlakKoma) {
            40 -> 330.0 // Dügâh perdesi (440 * 3/4 = 330.0)
            49, 62 -> 440.0 // Nevâ perdesi
            31 -> (440.0 * 4.0 / 9.0) * 1.5 // Râst perdesi (880.0 / 3 ≈ 293.333 Hz)
            22 -> (440.0 * 4.0 / 9.0) * (4.0 / 3.0) // Acem Aşîrân / Çârgâh
            48 -> (440.0 * 4.0 / 9.0) * (4096.0 / 2187.0) // Segâh perdesi
            else -> {
                // Diğer komalar için Dügâh 330 Hz ekseninde analitik Pisagor / 53-EDO dönüşümü
                330.0 * 2.0.pow((mutlakKoma - 40.0) / 53.0)
            }
        }
    }

    /**
     * Mansur fizik düzeninde (Kaba Çârgâh = 256.0 Hz referansı) mutlak koma değerinden analitik frekans üretir:
     * - Kaba Çârgâh (0 mutlak koma) = 256.0 * 1/1 = 256.0 Hz (Tam eşitlik)
     * - Râst (31 mutlak koma) = 256.0 * 3/2 = 384.0 Hz (Tam eşitlik)
     * - Dügâh (40 mutlak koma) = 256.0 * 27/16 = 432.0 Hz (Tam eşitlik)
     * - Segâh (48 veya 49 mutlak koma) = 256.0 * 4096/2187 ≈ 479.444 Hz
     * - Orta Çârgâh (22 koma / 1 oktav) = 256.0 * 2 = 512.0 Hz
     */
    fun toMansurPitch(mutlakKoma: Int): Double {
        return when (mutlakKoma) {
            0 -> 256.0 // Kaba Çârgâh (256.0 * 1/1)
            31 -> 256.0 * (3.0 / 2.0) // Râst (256.0 * 1.5 = 384.0 Hz)
            40 -> 256.0 * (27.0 / 16.0) // Dügâh (256.0 * 1.6875 = 432.0 Hz)
            48, 49 -> 256.0 * (4096.0 / 2187.0) // Segâh (≈ 479.444 Hz)
            else -> {
                // Genel mutlak komalar için Özkan Tablo I saf Pisagor oranı
                val komaMod = (mutlakKoma % 53 + 53) % 53
                val octave = mutlakKoma / 53
                256.0 * getPythagoreanRatioForKoma(komaMod) * 2.0.pow(octave)
            }
        }
    }

    /**
     * Kız ahenginde frekans üretir (La = 415 Hz referansı).
     */
    fun toKizPitch(mutlakKoma: Int): Double {
        return toBolahenkPitch(mutlakKoma) * (415.0 / 440.0)
    }

    /**
     * Süpürde ahenginde frekans üretir (La = 523 Hz referansı).
     */
    fun toSupurdePitch(mutlakKoma: Int): Double {
        return toBolahenkPitch(mutlakKoma) * (523.0 / 440.0)
    }

    /**
     * Seçilen Ahenk ve mutlak koma değerine göre analitik frekans hesaplar.
     */
    fun calculateAhenkPitch(mutlakKoma: Int, ahenk: Ahenk): Double {
        return when (ahenk) {
            Ahenk.BOLAHENK -> toBolahenkPitch(mutlakKoma)
            Ahenk.MANSUR -> toMansurPitch(mutlakKoma)
            Ahenk.KIZ -> toKizPitch(mutlakKoma)
            Ahenk.SUPURDE -> toSupurdePitch(mutlakKoma)
        }
    }

    /**
     * Eb Alto Saksafon transpozisyon çarpanı: Büyük Altılı (27/16).
     */
    fun toEbAltoPitch(frequency: Double): Double {
        return frequency * (27.0 / 16.0)
    }

    /**
     * Bb Saksafon / Klarnet transpozisyon çarpanı: Tanini (9/8).
     */
    fun toBbPitch(frequency: Double): Double {
        return frequency * (9.0 / 8.0)
    }

    /**
     * Segâh / Uşşak icra toleransı ve geleneksel pestleşme nüansı (nuanceOffset: -1 veya -2 koma).
     */
    fun applyNuanceOffset(baseFrequency: Double, nuanceOffsetKoma: Int): Double {
        if (nuanceOffsetKoma == 0) return baseFrequency
        return baseFrequency * 2.0.pow(nuanceOffsetKoma / 53.0)
    }
}
