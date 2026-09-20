package com.example.tmtuner.core.audio.drone

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * Türk Müziği Akustik Referans Sentezleyici (Drone / Dem Sesi) Sentez Motoru.
 *
 * Özellikler:
 * 1. **Kesintisiz Faz Birikimi (Phase Accumulation)**: Frekans değiştiğinde veya sürekli çalındığında
 *    asla faz sıfırlanmaz, klik ve patlama sesleri (pop/click) matematiksel olarak önlenir.
 * 2. **Harmonik Katkı Sentezi (Additive Harmonic Synthesis)**: Seçilen [AcousticDroneProfile]
 *    (Tanbûra, Ney veya Saf Sinüs) profiline göre doğal rezonans harmonikleri üretilir.
 * 3. **Çift Dem / Armonik Beşli (Dual Drone)**: Karar perdesi ile birlikte istenirse güçlü (Dominant 5'li)
 *    perdesi paralel olarak tınlatılabilir (Örn: Rast + Nevâ veya Dügâh + Nevâ).
 * 4. **Yumuşak Kazanç Geçişi (Gain Slew / Anti-Pop)**: Başlatma, durdurma ve ses seviyesi değişimlerinde
 *    yumuşak rampa interpolasyonu uygulanarak dijital sıçramalar engellenir.
 * 5. **Saf Matematiksel & Platform Bağımsız**: Android kütüphanelerine bağımlılığı yoktur, doğrudan
 *    birim test edilebilir.
 *
 * @param sampleRate Örnekleme hızı (Hz), varsayılan 44100 Hz.
 */
class AcousticDroneSynthesizer(
    val sampleRate: Int = 44100
) {
    // Akustik ayarlar
    @Volatile var tonicFrequency: Double = 440.0
    @Volatile var dominantFrequency: Double = 660.0
    @Volatile var isDualDroneEnabled: Boolean = false
    @Volatile var profile: AcousticDroneProfile = AcousticDroneProfile.TANBURA

    // Hedef ve anlık kazanç (Anti-pop ve yumuşak ses kontrolü)
    @Volatile var targetVolume: Float = 0.5f
    private var currentVolume: Double = 0.0

    // Kesintisiz faz değişkenleri (0.0 .. 2*PI)
    private var phaseTonic: Double = 0.0
    private var phaseDominant: Double = 0.0

    // Kazanç yumuşatma hızı (Örnek başına adaptasyon: ~20-30 ms rampa)
    private val gainSlewRate: Double = 1.0 / (sampleRate * 0.025)

    /**
     * Hedef tonik (karar) ve opsiyonel güçlü (dominant) frekansını günceller.
     * Faz sıfırlanmaz, ses kesintisiz olarak yeni frekansa kayar.
     */
    fun setFrequencies(tonicFreq: Double, dominantFreq: Double? = null) {
        if (tonicFreq > 20.0) {
            this.tonicFrequency = tonicFreq
        }
        if (dominantFreq != null && dominantFreq > 20.0) {
            this.dominantFrequency = dominantFreq
        } else {
            // Varsayılan olarak 53-EDO / Pisagor Tam Beşli (3/2 = 1.5)
            this.dominantFrequency = this.tonicFrequency * 1.5
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
    fun renderPcm16(buffer: ShortArray, offset: Int = 0, length: Int = buffer.size): Int {
        val targetGain = targetVolume.toDouble().coerceIn(0.0, 1.0)
        val profileWeights = profile.harmonicWeights
        val weightSum = profileWeights.sum()
        val invWeightSum = if (weightSum > 0.0) 1.0 / weightSum else 1.0

        val tonicFreq = tonicFrequency
        val domFreq = dominantFrequency
        val dual = isDualDroneEnabled

        val twoPi = 2.0 * PI
        val tonicIncrement = twoPi * tonicFreq / sampleRate
        val domIncrement = twoPi * domFreq / sampleRate

        for (i in 0 until length) {
            // Kazanç yumuşatma (Anti-pop)
            if (currentVolume < targetGain) {
                currentVolume = (currentVolume + gainSlewRate).coerceAtMost(targetGain)
            } else if (currentVolume > targetGain) {
                currentVolume = (currentVolume - gainSlewRate).coerceAtLeast(targetGain)
            }

            // 1. Tonik (Karar) dalga formu - Harmonik katkı sentezi
            var tonicSample = 0.0
            for (h in profileWeights.indices) {
                val harmonicIndex = h + 1
                val harmonicAngle = phaseTonic * harmonicIndex
                tonicSample += profileWeights[h] * sin(harmonicAngle)
            }
            tonicSample *= invWeightSum

            // 2. Güçlü (Dominant) dalga formu (aktifse)
            var finalSample = tonicSample
            if (dual) {
                var domSample = 0.0
                for (h in profileWeights.indices) {
                    val harmonicIndex = h + 1
                    val harmonicAngle = phaseDominant * harmonicIndex
                    domSample += profileWeights[h] * sin(harmonicAngle)
                }
                domSample *= invWeightSum
                // %70 Karar, %30 Güçlü dengesi
                finalSample = 0.70 * tonicSample + 0.30 * domSample
            }

            // 3. Genel kazanç uygulama ve 16-bit PCM ölçekleme
            val scaledSample = finalSample * currentVolume * Short.MAX_VALUE
            val clampedSample = scaledSample.coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble())
            buffer[offset + i] = clampedSample.toInt().toShort()

            // 4. Faz birikimi (Kesintisiz döngü)
            phaseTonic += tonicIncrement
            if (phaseTonic >= twoPi) {
                phaseTonic -= twoPi
            }

            if (dual) {
                phaseDominant += domIncrement
                if (phaseDominant >= twoPi) {
                    phaseDominant -= twoPi
                }
            }
        }

        return length
    }

    /**
     * Sentezleyici fazını ve kazancını sıfırlar (Durdurulduğunda temiz başlangıç için).
     */
    fun resetPhase() {
        phaseTonic = 0.0
        phaseDominant = 0.0
        currentVolume = 0.0
    }
}
