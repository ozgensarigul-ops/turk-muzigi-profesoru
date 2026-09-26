package com.example.tmtuner.core.audio.drone

import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Türk Müziği Akustik Referans Sentezleyici (Drone / Dem Sesi) Sentez Motoru.
 *
 * Özellikler:
 * 1. **Kesintisiz Faz Birikimi (Phase Accumulation)**: Faz değişkenleri Double olarak tutulur
 *    ve her ses örneğinde `phase = (phase + phaseStep) % (2.0 * Math.PI)` formülüyle modülo alınarak sarmalanır.
 *    Uzun süreli çalımlarda kayan nokta hassasiyet kaybı ve faz yırtılmaları kesinlikle engellenir.
 * 2. **50 ms Pürüzsüz Frekans Rampalama (Smoothing)**: Ani frekans/perde ve koma değişimlerinde
 *    anlık frekanstan hedef frekansa 50 ms'lik lineer rampa uygulanarak klik/patlama (click/pop) sesleri engellenir.
 * 3. **Harmonik Katkı Sentezi & Headroom Normalizasyonu**: Tanbûra ve Ney tını profillerinde
 *    harmonik genliklerinin toplamı 0.85 katsayısına çekilerek DAC tavan kırpılmalarını önleyen -1.41 dB tavan boşluğu bırakılır.
 * 4. **Güvenli PCM 16-bit Dönüşümü**: Dalga örnekleri `(sample * 32767.0).roundToInt().coerceIn(-32768, 32767).toShort()`
 *    formülüyle Short dizisine dönüştürülür; yuvarlama hataları ve tavan kırpılmaları önlenir.
 * 5. **Çift Dem / Armonik Beşli (Dual Drone)**: Karar perdesi ile birlikte istenirse güçlü (Dominant 5'li)
 *    perdesi paralel olarak tınlatılabilir.
 * 6. **Yumuşak Kazanç Geçişi (Gain Slew / Anti-Pop)**: Başlatma ve durdurmada yumuşak ses iniş/çıkış rampası uygulanır.
 *
 * @param sampleRate Örnekleme hızı (Hz), varsayılan 48000 Hz.
 */
class AcousticDroneSynthesizer(
    @Volatile var sampleRate: Int = 48000
) {
    // Hedef ve anlık frekanslar (50 ms pürüzsüz rampa için)
    private var currentTonicFrequency: Double = 440.0
    private var targetTonicFrequency: Double = 440.0
    private var tonicFreqStep: Double = 0.0
    private var tonicRampRemainingSamples: Int = 0

    private var currentDominantFrequency: Double = 660.0
    private var targetDominantFrequency: Double = 660.0
    private var dominantFreqStep: Double = 0.0
    private var dominantRampRemainingSamples: Int = 0

    // Geriye dönük uyumluluk property'leri
    var tonicFrequency: Double
        get() = targetTonicFrequency
        set(value) {
            setFrequencies(value, targetDominantFrequency)
        }

    var dominantFrequency: Double
        get() = targetDominantFrequency
        set(value) {
            setFrequencies(targetTonicFrequency, value)
        }

    @Volatile var isDualDroneEnabled: Boolean = false
    @Volatile var profile: AcousticDroneProfile = AcousticDroneProfile.TANBURA

    // Hedef ve anlık kazanç (Anti-pop ve yumuşak ses kontrolü)
    @Volatile var targetVolume: Float = 0.5f
    private var currentVolume: Double = 0.0

    // Kesintisiz faz değişkenleri (0.0 .. 2*PI)
    private var phaseTonic: Double = 0.0
    private var phaseDominant: Double = 0.0

    // Kazanç yumuşatma hızı (Örnek başına adaptasyon: ~25 ms rampa)
    private val gainSlewRate: Double
        get() = 1.0 / (sampleRate * 0.025)

    /**
     * Hedef tonik (karar) ve opsiyonel güçlü (dominant) frekansını günceller.
     * Faz sıfırlanmaz, 50 ms'lik pürüzsüz lineer rampa ile hedef frekansa kayar.
     */
    @Synchronized
    fun setFrequencies(tonicFreq: Double, dominantFreq: Double? = null) {
        val rampSamples = (sampleRate * 0.050).toInt().coerceAtLeast(1)

        if (tonicFreq > 20.0) {
            targetTonicFrequency = tonicFreq
            tonicFreqStep = (targetTonicFrequency - currentTonicFrequency) / rampSamples
            tonicRampRemainingSamples = rampSamples
        }

        val targetDom = dominantFreq ?: (if (tonicFreq > 20.0) tonicFreq * 1.5 else targetTonicFrequency * 1.5)
        if (targetDom > 20.0) {
            targetDominantFrequency = targetDom
            dominantFreqStep = (targetDominantFrequency - currentDominantFrequency) / rampSamples
            dominantRampRemainingSamples = rampSamples
        }
    }

    /**
     * Bir sonraki PCM-16 tamponunu üretir.
     *
     * @param buffer Çıktının yazılacağı ShortArray tamponu.
     * @param offset Yazmaya başlanacak indeks.
     * @param length Üretilecek örnek sayısı.
     * @return Üretilen örnek sayısı.
     */
    @Synchronized
    fun renderPcm16(buffer: ShortArray, offset: Int = 0, length: Int = buffer.size): Int {
        val targetGain = targetVolume.toDouble().coerceIn(0.0, 1.0)
        val profileWeights = profile.harmonicWeights
        val weightSum = profileWeights.sum()

        // Tanbûr, Ney ve tüm profiller için harmonik genlik toplamını 0.85 tavan boşluğuna (headroom) ölçekle
        val headroom = 0.85
        val invWeightSum = if (weightSum > 0.0) headroom / weightSum else headroom

        val dual = isDualDroneEnabled
        val twoPi = 2.0 * Math.PI

        for (i in 0 until length) {
            // 0. Kazanç yumuşatma (Anti-pop)
            val slew = gainSlewRate
            if (currentVolume < targetGain) {
                currentVolume = (currentVolume + slew).coerceAtMost(targetGain)
            } else if (currentVolume > targetGain) {
                currentVolume = (currentVolume - slew).coerceAtLeast(targetGain)
            }

            // 1. 50 ms Frekans Rampalama (Smoothing)
            if (tonicRampRemainingSamples > 0) {
                currentTonicFrequency += tonicFreqStep
                tonicRampRemainingSamples--
                if (tonicRampRemainingSamples == 0) {
                    currentTonicFrequency = targetTonicFrequency
                }
            }

            if (dominantRampRemainingSamples > 0) {
                currentDominantFrequency += dominantFreqStep
                dominantRampRemainingSamples--
                if (dominantRampRemainingSamples == 0) {
                    currentDominantFrequency = targetDominantFrequency
                }
            }

            // 2. Tonik (Karar) dalga formu - Harmonik katkı sentezi
            var tonicSample = 0.0
            for (h in profileWeights.indices) {
                val harmonicIndex = h + 1
                val harmonicAngle = (phaseTonic * harmonicIndex) % twoPi
                tonicSample += profileWeights[h] * sin(harmonicAngle)
            }
            tonicSample *= invWeightSum

            // 3. Güçlü (Dominant) dalga formu (aktifse)
            var finalSample = tonicSample
            if (dual) {
                var domSample = 0.0
                for (h in profileWeights.indices) {
                    val harmonicIndex = h + 1
                    val harmonicAngle = (phaseDominant * harmonicIndex) % twoPi
                    domSample += profileWeights[h] * sin(harmonicAngle)
                }
                domSample *= invWeightSum
                // %70 Karar, %30 Güçlü dengesi
                finalSample = 0.70 * tonicSample + 0.30 * domSample
            }

            // 4. PCM 16-bit Dönüşümü: Taşmaları ve tavan kırpılmalarını önleyen güvenli yuvarlama
            val sample = finalSample * currentVolume
            buffer[offset + i] = (sample * 32767.0).roundToInt().coerceIn(-32768, 32767).toShort()

            // 5. Faz Biriktirici & Sarmalama: phase = (phase + phaseStep) % (2.0 * Math.PI)
            val tonicIncrement = (twoPi * currentTonicFrequency) / sampleRate
            phaseTonic = (phaseTonic + tonicIncrement) % twoPi

            if (dual) {
                val domIncrement = (twoPi * currentDominantFrequency) / sampleRate
                phaseDominant = (phaseDominant + domIncrement) % twoPi
            }
        }

        return length
    }

    /**
     * Sentezleyici fazını, frekans rampasını ve kazancını sıfırlar.
     */
    @Synchronized
    fun resetPhase() {
        phaseTonic = 0.0
        phaseDominant = 0.0
        currentVolume = 0.0
        currentTonicFrequency = targetTonicFrequency
        tonicRampRemainingSamples = 0
        currentDominantFrequency = targetDominantFrequency
        dominantRampRemainingSamples = 0
    }
}
