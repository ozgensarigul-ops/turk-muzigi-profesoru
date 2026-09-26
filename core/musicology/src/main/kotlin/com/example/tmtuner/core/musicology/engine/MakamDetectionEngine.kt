package com.example.tmtuner.core.musicology.engine

import com.example.tmtuner.core.musicology.analysis.FrequencyEstimator
import com.example.tmtuner.core.musicology.analysis.MakamScorer
import com.example.tmtuner.core.musicology.analysis.MakamSeyirAnalyzer
import com.example.tmtuner.core.musicology.analysis.TonicDetector
import com.example.tmtuner.core.musicology.atlas.AeuScaleAtlas
import com.example.tmtuner.core.musicology.atlas.PitchMatcher
import com.example.tmtuner.core.musicology.model.Ahenk
import com.example.tmtuner.core.musicology.model.MakamDetectionResult
import com.example.tmtuner.core.musicology.model.MakamProfile
import com.example.tmtuner.core.musicology.model.SeyirType
import com.example.tmtuner.core.musicology.registry.MakamRegistry
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * İcrada tespit edilen perde olayı veri yapısı.
 *
 * @param perdeName Algılanan perde adı (örn: "Dügâh", "Segâh", "Neva")
 * @param komaOffset Nominal 53-EDO perdesine göre koma sapması (örn: Segâh için -1.5 koma)
 * @param durationMs Perdenin kesintisiz tınlama süresi (milisaniye)
 * @param rmsEnergy Sinyal RMS enerji genliği (0.0f - 1.0f)
 * @param timestamp Olayın kaydedildiği zaman damgası (milisaniye)
 * @param frequency İcracının ürettiği / mikrofondan algılanan ham frekans (Hz)
 */
data class PitchEvent(
    val perdeName: String = "",
    val komaOffset: Double = 0.0,
    val durationMs: Long = 100L,
    val rmsEnergy: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis(),
    val frequency: Double = 0.0
)

/**
 * 53-EDO Makam Tanıma ve Seyir Analiz Motoru.
 *
 * İsmail Hakkı Özkan'ın "Türk Mûsikîsi Nazariyatı ve Usûlleri" kitabı ve Arel-Ezgi-Uzdilek (AEU)
 * sistemine tam uyumlu olarak:
 * 1. Canlı perdeleri son 20-30 saniyelik kayan zaman penceresinde (sliding window) biriktirir.
 * 2. Zaman ve RMS enerji ağırlıklı perde süre histogramı oluşturur.
 * 3. Ezgisel seyir yönünü (Çıkıcı, İnici-Çıkıcı, İnici) melodinin ilk çeyreğindeki ağırlık merkezi
 *    ve akış yönünden tespit eder.
 * 4. Durak (karar) ve Güçlü (yarım karar) perdelerini tespit eder.
 * 5. Segâh perdesinin Uşşâk ve Hüseynî makamlarındaki geleneksel -1 ila -2 komalık pest icra
 *    toleransını (`segahToleranceKoma`) uygular ve doğru icraya güven bonusu verir.
 * 6. Hüseynî ile Uşşâk arasındaki güçlü (Hüseynî vs Nevâ) ve 6. derece (Eviç vs Acem) farklarını
 *    analitik olarak ayrıştırır.
 */
class MakamDetectionEngine(
    val windowDurationMs: Long = 30_000L,
    val registeredMakams: List<MakamProfile> = MakamRegistry.CORE_MAKAMS,
    var ahenk: Ahenk = Ahenk.MANSUR
) {
    private val lock = Any()
    private val events = mutableListOf<PitchEvent>()

    private val _detectionResult = MutableStateFlow<MakamDetectionResult?>(null)
    val detectionResult: StateFlow<MakamDetectionResult?> = _detectionResult.asStateFlow()

    private val _detectionEvents = MutableSharedFlow<MakamDetectionResult>(replay = 1)
    val detectionEvents: SharedFlow<MakamDetectionResult> = _detectionEvents.asSharedFlow()

    companion object {
        /**
         * 53-EDO perde bağıl koma yükseklik tablosu (Kaba Çârgâh = 0 referansı).
         */
        val PERDE_KOMA_POSITIONS: Map<String, Int> = mapOf(
            "kaba cargah" to 0,
            "kaba nim hicaz" to 4,
            "kaba hicaz" to 5,
            "kaba dik hicaz" to 8,
            "yegah" to 9,
            "kaba nim hisar" to 13,
            "kaba hisar" to 14,
            "kaba dik hisar" to 17,
            "huseyni asiran" to 18,
            "acem asiran" to 22,
            "dik acem asiran" to 23,
            "irak" to 26,
            "gevest" to 27,
            "dik gevest" to 30,
            "rast" to 31,
            "nim zirgule" to 35,
            "zirgule" to 36,
            "dik zirgule" to 39,
            "dugah" to 40,
            "kurdi" to 44,
            "dik kurdi" to 45,
            "segah" to 48,
            "buselik" to 49,
            "dik buselik" to 52,
            "cargah" to 53,
            "nim hicaz" to 57,
            "hicaz" to 58,
            "dik hicaz" to 61,
            "neva" to 62,
            "nim hisar" to 66,
            "hisar" to 67,
            "dik hisar" to 70,
            "huseyni" to 71,
            "acem" to 75,
            "dik acem" to 76,
            "evic" to 79,
            "mahur" to 80,
            "dik mahur" to 83,
            "gerdaniye" to 84,
            "nim sehnaz" to 88,
            "sehnaz" to 89,
            "dik sehnaz" to 92,
            "muhayyer" to 93,
            "sunbule" to 97,
            "dik sunbule" to 98,
            "tiz segah" to 101,
            "tiz buselik" to 102,
            "tiz dik buselik" to 105,
            "tiz cargah" to 106
        )

        /**
         * Verilen perde adının yaklaşık koma yüksekliğini döner.
         */
        fun getPerdeKomaHeight(perdeName: String): Int {
            val norm = MakamRegistry.normalizePerdeName(perdeName)
            return PERDE_KOMA_POSITIONS[norm] ?: 40
        }
    }

    /**
     * Canlı perde olayını motora iletir.
     */
    fun feedPitch(event: PitchEvent): MakamDetectionResult? {
        if (event.frequency > 0.0 && (event.perdeName.isEmpty() || event.perdeName == "—")) {
            return feedFrequency(event.frequency, event.durationMs, event.rmsEnergy, event.timestamp, this.ahenk)
        }
        synchronized(lock) {
            events.add(event)
            pruneOldEvents(event.timestamp)
        }
        return analyze()
    }

    /**
     * Canlı perdeyi parametrelerle pratik olarak iletir.
     */
    fun feedPitch(
        perdeName: String,
        komaOffset: Double = 0.0,
        durationMs: Long = 100L,
        rmsEnergy: Float = 1.0f,
        timestamp: Long = System.currentTimeMillis()
    ): MakamDetectionResult? {
        return feedPitch(PitchEvent(perdeName, komaOffset, durationMs, rmsEnergy, timestamp))
    }

    /**
     * Canlı icra frekansını (Hz) seçili ahenk bilgisine göre transpoze ederek
     * ve oktav arıtımı uygulayarak motora iletir.
     *
     * Özkan s. 87 Transpozisyon Yönü Kuralı:
     * Kız Neyi (Dügâh = Si / B ≈ 495 Hz, +9 koma tiz) icrası sisteme girerken
     * yerindeki Mansur dizisine indirgenmek üzere 9 KOMA PESTLEŞTİRİLİR (frekans * 8/9).
     */
    fun feedFrequency(
        frequency: Double,
        durationMs: Long = 100L,
        rmsEnergy: Float = 1.0f,
        timestamp: Long = System.currentTimeMillis(),
        inputAhenk: Ahenk = this.ahenk
    ): MakamDetectionResult? {
        if (frequency <= 20.0 || rmsEnergy < 0.005f) return null

        // 1. Oktav arıtımı (Kaba sekizliye / sub-armoniğe düşüşleri ve 3. harmonik sıçramalarını önle)
        val minMelodic = if (inputAhenk == Ahenk.KIZ) 350.0 else FrequencyEstimator.DEFAULT_MIN_MELODIC_HZ
        val disambiguatedFreq = FrequencyEstimator.disambiguateOctave(
            detectedFrequency = frequency,
            minMelodicFreq = minMelodic,
            maxMelodicFreq = FrequencyEstimator.DEFAULT_MAX_MELODIC_HZ
        )

        // 2. Ahenk Transpozisyonu: İcrâ frekansından konsert (Mansur) frekansına dönüşüm
        val concertFreq = TranspositionEngine.toConcertPitch(disambiguatedFreq, inputAhenk)

        // 3. 53-EDO Mansur perde atlasında eşleme
        val atlas = AeuScaleAtlas.buildPitchAtlas(Ahenk.MANSUR)
        val match = PitchMatcher.matchPitch(concertFreq, atlas) ?: return null

        // 4. Geleneksel Türk Müziği icrasında Segâh toleransı (Özkan s. 51, 143):
        // Uşşâk ve Hüseynî makamlarında Segâh perdesi 1-2 koma pest icra edilir [-2.0, +0.5].
        // Saf Pisagor veya 53-EDO eşlemesinde 1.5-2 koma pest Segâh (475-480 Hz), matematiksel olarak
        // Dik Kürdî'ye (470.92 Hz) nominal Segâh'tan (488.38 Hz) daha yakın düşebilir. Bu yanılgıyı önlemek için
        // nominal Segâh çevresindeki [-2.2, +0.8] koma penceresi Segâh olarak kabul edilir.
        val nominalSegahFreq = (440.0 * 16.0 / 27.0) * (4096.0 / 2187.0) // ≈ 488.385 Hz
        var matchedPerdeName = match.matchedNote.name
        var finalKomaOffset = match.komaOffset.toDouble()

        for (octaveMul in listOf(1.0, 2.0, 4.0)) {
            val segahRef = nominalSegahFreq * octaveMul
            val deltaKoma = 53.0 * kotlin.math.log2(concertFreq / segahRef)
            if (deltaKoma in -2.2..0.8) {
                matchedPerdeName = when (octaveMul) {
                    1.0 -> "Segâh"
                    2.0 -> "Tîz Segâh"
                    else -> "En Tîz Segâh"
                }
                finalKomaOffset = deltaKoma
                break
            }
        }

        return feedPitch(
            PitchEvent(
                perdeName = matchedPerdeName,
                komaOffset = finalKomaOffset,
                durationMs = durationMs,
                rmsEnergy = rmsEnergy,
                timestamp = timestamp,
                frequency = frequency
            )
        )
    }

    /**
     * Kayıtlı olayları sıfırlar.
     */
    fun reset() {
        synchronized(lock) {
            events.clear()
            _detectionResult.value = null
        }
    }

    /**
     * Mevcut tampon verisi üzerinden tam makam ve seyir analizi gerçekleştirir.
     */
    fun analyze(): MakamDetectionResult? {
        val currentEvents: List<PitchEvent> = synchronized(lock) {
            if (events.isEmpty()) return null
            ArrayList(events)
        }

        if (currentEvents.isEmpty()) return null

        // 1. Histogram oluşturma: Zaman ve RMS ağırlıklı perde süreleri
        val rawHistogram = mutableMapOf<String, Double>()
        var totalWeightedDuration = 0.0

        for (event in currentEvents) {
            val normPerde = MakamRegistry.normalizePerdeName(event.perdeName)
            val weight = event.durationMs * (0.3 + 0.7 * event.rmsEnergy.coerceIn(0.0f, 1.0f))
            rawHistogram[normPerde] = rawHistogram.getOrDefault(normPerde, 0.0) + weight
            totalWeightedDuration += weight
        }

        if (totalWeightedDuration <= 0.0) return null

        // Görüntüleme için normalize edilmiş perde histogramı
        val displayHistogram = mutableMapOf<String, Double>()
        for ((perde, weight) in rawHistogram) {
            val displayPerde = canonicalPerdeName(perde)
            displayHistogram[displayPerde] = (weight / totalWeightedDuration)
        }

        // 2. Durak (Karar) ve Güçlü (Yarım Karar) Tespiti
        val detectedDurak = detectDurak(currentEvents, rawHistogram, totalWeightedDuration)
        val detectedGuclu = detectGuclu(currentEvents, rawHistogram, totalWeightedDuration, detectedDurak)

        // 3. Seyir Tespiti: Melodinin ilk %25'lik penceresi ve akış yönü
        val detectedSeyir = detectSeyir(currentEvents, detectedDurak, detectedGuclu)

        // 4. Şablon Eşleme & Puanlama (Makam Matching)
        var bestMakam = registeredMakams.first()
        var highestScore = -100.0

        val segahEvents = currentEvents.filter { MakamRegistry.normalizePerdeName(it.perdeName) == "segah" }
        val avgSegahOffset = if (segahEvents.isNotEmpty()) segahEvents.map { it.komaOffset }.average() else 0.0

        for (makam in registeredMakams) {
            val score = scoreMakam(
                makam = makam,
                detectedDurak = detectedDurak,
                detectedGuclu = detectedGuclu,
                detectedSeyir = detectedSeyir,
                rawHistogram = rawHistogram,
                totalWeightedDuration = totalWeightedDuration,
                hasSegah = segahEvents.isNotEmpty(),
                avgSegahOffset = avgSegahOffset
            )

            if (score > highestScore) {
                highestScore = score
                bestMakam = makam
            }
        }

        // Güven katsayısı: 0.0f - 1.0f aralığında normalize
        val confidence = ((highestScore) / 100.0).coerceIn(0.0, 1.0).toFloat()

        val result = MakamDetectionResult(
            matchedMakam = bestMakam,
            confidence = confidence,
            detectedDurak = canonicalPerdeName(detectedDurak),
            detectedGuclu = canonicalPerdeName(detectedGuclu),
            detectedSeyir = detectedSeyir,
            pitchHistogram = displayHistogram
        )

        _detectionResult.value = result
        _detectionEvents.tryEmit(result)

        return result
    }

    /**
     * Karar (Durak) perdesini tespit eder.
     * Özkan s. 88-91 ve s. 143 hiyerarşik durak kuralları (TonicDetector) kullanılır.
     */
    private fun detectDurak(
        events: List<PitchEvent>,
        histogram: Map<String, Double>,
        totalDuration: Double
    ): String {
        return TonicDetector.detectDurak(events, histogram, totalDuration)
    }

    /**
     * Güçlü (Dominant) perdesini tespit eder.
     * MakamSeyirAnalyzer ve Özkan s. 88-89 hiyerarşik güçlü kuralları kullanılır.
     * Güçlü daima durağın tiz tarafında (üzerinde, minimum 4. derece) yer almak zorundadır.
     */
    private fun detectGuclu(
        events: List<PitchEvent>,
        histogram: Map<String, Double>,
        totalDuration: Double,
        durakNorm: String
    ): String {
        return MakamSeyirAnalyzer.detectGuclu(events, histogram, totalDuration, durakNorm)
    }

    /**
     * Ezgisel seyir tipini (Çıkıcı, İnici-Çıkıcı, İnici) tespit eder.
     * MakamSeyirAnalyzer ve Özkan s. 51 & 143 3 aşamalı ağırlıklı seyir penceresi kullanılır.
     */
    private fun detectSeyir(
        events: List<PitchEvent>,
        durakNorm: String,
        gucluNorm: String
    ): SeyirType {
        return MakamSeyirAnalyzer.detectSeyir(events, durakNorm, gucluNorm)
    }

    /**
     * Aday makam profilini tespit edilen özelliklere göre puanlar.
     * MakamScorer ve Özkan Nazariyatı matrisi kullanılır.
     */
    private fun scoreMakam(
        makam: MakamProfile,
        detectedDurak: String,
        detectedGuclu: String,
        detectedSeyir: SeyirType,
        rawHistogram: Map<String, Double>,
        totalWeightedDuration: Double,
        hasSegah: Boolean,
        avgSegahOffset: Double
    ): Double {
        return MakamScorer.scoreMakam(
            makam = makam,
            detectedDurak = detectedDurak,
            detectedGuclu = detectedGuclu,
            detectedSeyir = detectedSeyir,
            rawHistogram = rawHistogram,
            totalWeightedDuration = totalWeightedDuration,
            hasSegah = hasSegah,
            avgSegahOffset = avgSegahOffset
        )
    }

    private fun pruneOldEvents(currentTime: Long) {
        val cutoff = currentTime - windowDurationMs
        while (events.isNotEmpty() && events.first().timestamp < cutoff) {
            events.removeAt(0)
        }
    }

    private fun canonicalPerdeName(norm: String): String {
        return when (norm) {
            "cargah" -> "Çârgâh"
            "neva" -> "Nevâ"
            "huseyni" -> "Hüseynî"
            "acem" -> "Acem"
            "gerdaniye" -> "Gerdâniye"
            "muhayyer" -> "Muhayyer"
            "buselik" -> "Bûselik"
            "dugah" -> "Dügâh"
            "kurdi" -> "Kürdî"
            "dik kurdi" -> "Dik Kürdî"
            "rast" -> "Râst"
            "segah" -> "Segâh"
            "evic" -> "Eviç"
            "irak" -> "Irak"
            "nim zirgule" -> "Nîm Zîrgüle"
            "zirgule" -> "Zîrgüle"
            "dik zirgule" -> "Dik Zîrgüle"
            "hicaz" -> "Hicâz"
            "dik hicaz" -> "Dik Hicâz"
            "nim hicaz" -> "Nîm Hicâz"
            "hisar" -> "Hisâr"
            "dik hisar" -> "Dik Hisâr"
            "nim hisar" -> "Nîm Hisâr"
            "sehnaz" -> "Şehnâz"
            "nim sehnaz" -> "Nîm Şehnâz"
            "dik sehnaz" -> "Dik Şehnâz"
            "kaba dik hisar" -> "Kaba Dik Hisâr"
            "kaba hisar" -> "Kaba Hisâr"
            "kaba nim hisar" -> "Kaba Nîm Hisâr"
            "kaba cargah" -> "Kaba Çârgâh"
            "kaba hicaz" -> "Kaba Hicâz"
            "kaba dik hicaz" -> "Kaba Dik Hicâz"
            "kaba nim hicaz" -> "Kaba Nîm Hicâz"
            "humayun" -> "Hümâyûn"
            "uzzal" -> "Uzzal"
            "zirguleli hicaz" -> "Zîrgûleli Hicaz"
            "karcigar" -> "Karcığar"
            "basit suznak" -> "Basit Sûz'nâk"
            else -> norm.replaceFirstChar { it.uppercase() }
        }
    }
}
