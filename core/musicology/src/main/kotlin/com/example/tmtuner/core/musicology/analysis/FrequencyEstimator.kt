package com.example.tmtuner.core.musicology.analysis

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Türk Makam Müziği icralarında ses perdesi takibi ve frekans kestirim filtresi.
 *
 * İsmail Hakkı Özkan nazariyatı ve akustik prensipler uyarınca:
 * 1. Ney, insan sesi ve yaylı sazlarda bas armoniklere kapılarak Kaba sekizliye (sub-harmonic)
 *    düşüşleri engeller.
 * 2. Orta oktav (La3-Re5 / ~200-650 Hz) ana icra bölgesini muhafaza eden oktav arıtımı
 *    (octave disambiguation / harmonic peak validation) uygular.
 */
object FrequencyEstimator {

    /** Orta oktav Türk Müziği icrası için makul minimum temel frekans eşiği (Hz) */
    const val DEFAULT_MIN_MELODIC_HZ: Double = 180.0

    /** Orta oktav Türk Müziği icrası için makul maksimum temel frekans eşiği (Hz) */
    const val DEFAULT_MAX_MELODIC_HZ: Double = 1200.0

    /**
     * Düşük frekans bas armoniklerine ve alt oktava kapılmayı engelleyen
     * oktav arıtım (disambiguation) filtresi.
     *
     * @param detectedFrequency Akustik algılayıcıdan gelen ham temel frekans (Hz)
     * @param minMelodicFreq Beklenen minimum ezgisel frekans (varsayılan 180.0 Hz)
     * @param maxMelodicFreq Beklenen maksimum ezgisel frekans (varsayılan 1200.0 Hz)
     * @return Oktav sıçraması ve sub-armoniklerden arındırılmış frekans (Hz)
     */
    fun disambiguateOctave(
        detectedFrequency: Double,
        minMelodicFreq: Double = DEFAULT_MIN_MELODIC_HZ,
        maxMelodicFreq: Double = DEFAULT_MAX_MELODIC_HZ
    ): Double {
        if (detectedFrequency <= 20.0) return 0.0

        var freq = detectedFrequency

        // 1. Kaba sekizliye / sub-harmoniklere (örneğin f0/2 veya f0/3) düşüş kontrolü
        while (freq < minMelodicFreq && freq > 20.0) {
            val doubled = freq * 2.0
            if (doubled <= maxMelodicFreq) {
                freq = doubled
            } else {
                break
            }
        }

        // 2. Aşırı tiz oktav taşması kontrolü (örneğin 3*f0 veya 2*f0 armonik kilidi)
        if (freq > maxMelodicFreq) {
            val third = freq / 3.0
            if (third in minMelodicFreq..maxMelodicFreq) {
                freq = third
            } else {
                while (freq > maxMelodicFreq) {
                    val halved = freq / 2.0
                    if (halved >= minMelodicFreq) {
                        freq = halved
                    } else {
                        break
                    }
                }
            }
        }

        return freq
    }

    /**
     * MPM/NSDF lag tepe noktaları arasından hem üst harmonik (3x, 2x overtone)
     * hem de alt harmonik (sub-harmonic) katlanmalarını bertaraf eden tepe doğrulama filtresi.
     *
     * @param selectedTau İlk seçilen lag periyodu
     * @param peakLags Tespit edilen tüm tepe lag indeksleri
     * @param nsdf NSDF korelasyon dizisi
     * @param sampleRate Ses örnekleme frekansı (varsayılan 44100 Hz)
     * @param harmonicToleranceRatio Kabul edilebilir korelasyon oranı
     * @return Doğrulanmış temel periyot (tau)
     */
    fun disambiguatePeriodLag(
        selectedTau: Int,
        peakLags: List<Int>,
        nsdf: DoubleArray,
        sampleRate: Int = 44100,
        harmonicToleranceRatio: Double = 0.70
    ): Int {
        if (selectedTau <= 0 || peakLags.isEmpty() || nsdf.isEmpty()) return selectedTau

        val selectedVal = nsdf.getOrElse(selectedTau) { 0.0 }
        val threshold = selectedVal * harmonicToleranceRatio

        // 1. Üst Harmonik Kontrolü (Tiz Harmoniğe Sıçramayı Önleme):
        // Ney ve nefesli sazlarda 2. veya 3. harmonik (özellikle 550 Hz yerine 1100 veya 1650 Hz)
        // çok güçlü çıkıp NSDF'de erken bir lag tepesi oluşturabilir.
        // Eğer seçilen frekans > ~650 Hz ise (tau < sampleRate / 650),
        // selectedTau * 2 ve selectedTau * 3 katlarında (ana temel frekansta) güçlü bir tepe var mı kontrol et.
        val maxFundamentalLag = (sampleRate / 650.0).toInt().coerceAtLeast(30)
        if (selectedTau < maxFundamentalLag) {
            for (multiplier in listOf(3, 2)) {
                val targetMulTau = selectedTau * multiplier
                if (targetMulTau >= nsdf.size - 2) continue

                val matchingPeak = peakLags.find { peak ->
                    abs(peak - targetMulTau) <= (multiplier + 1) && peak > selectedTau
                }

                if (matchingPeak != null && matchingPeak < nsdf.size) {
                    val mulVal = nsdf[matchingPeak]
                    // Çarpan tepesi geçerli bir periyodik korelasyona sahipse (temel frekans buradadır)
                    if (mulVal >= 0.55 && mulVal >= (selectedVal * 0.70)) {
                        return matchingPeak
                    }
                }
            }
        }

        // 2. Alt Harmonik Kontrolü (Kaba Sekizliye Düşüşü Önleme):
        // Eğer seçilen periyot çok büyükse (örneğin f < ~360 Hz -> tau > sampleRate / 360),
        // sub-armonik nedeniyle temel periyodun 2x veya 3x katına düşülmüş olabilir.
        val minSubharmonicLag = (sampleRate / 360.0).toInt()
        if (selectedTau > minSubharmonicLag) {
            for (divisor in listOf(2, 3)) {
                val targetSubTau = selectedTau / divisor
                if (targetSubTau < 1) continue

                val matchingPeak = peakLags.find { peak ->
                    abs(peak - targetSubTau) <= 2 && peak < selectedTau
                }

                if (matchingPeak != null && matchingPeak < nsdf.size) {
                    val subVal = nsdf[matchingPeak]
                    if (subVal >= threshold && subVal > 0.50) {
                        return matchingPeak
                    }
                }
            }
        }

        return selectedTau
    }
}
