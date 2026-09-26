package com.example.tmtuner.core.musicology.analysis

import com.example.tmtuner.core.musicology.engine.PitchEvent
import com.example.tmtuner.core.musicology.model.SeyirType
import com.example.tmtuner.core.musicology.registry.MakamRegistry
import kotlin.math.abs

/**
 * İsmail Hakkı Özkan'ın "Türk Mûsikîsi Nazariyatı ve Usûlleri" kurallarına (s. 46-56, 88-89, 143)
 * tam uyumlu Hiyerarşik Güçlü ve Ezgisel Seyir Analizörü.
 */
object MakamSeyirAnalyzer {

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

    fun getPerdeKomaHeight(perdeName: String): Int {
        val norm = MakamRegistry.normalizePerdeName(perdeName)
        return PERDE_KOMA_POSITIONS[norm] ?: 40
    }

    /**
     * Karar (Durak / Tonic) perdesini tespit eder.
     * İsmail Hakkı Özkan'ın "Türk Mûsikîsi Nazariyatı ve Usûlleri" kurallarına (s. 88-91, 143)
     * uygun olarak TonicDetector üzerinden hiyerarşik durak tespiti yapar.
     */
    fun detectDurak(
        events: List<PitchEvent>,
        histogram: Map<String, Double>,
        totalDuration: Double
    ): String {
        return TonicDetector.detectDurak(events, histogram, totalDuration)
    }

    /**
     * Güçlü (Dominant) perdesini tespit eder.
     *
     * MÜZİKOLOJİK KURAL (Özkan s. 88-89 - Dizi Seslerinin İsim ve Görevleri):
     * 1. Güçlü perdesi daima durağın TİZ tarafında (üzerinde) yer alır; 4. derece (altında dörtlü varsa)
     *    veya 5. derece (altında beşli varsa) olabilir.
     * 2. Karar perdesi Dügâh (La) iken veya Do# iken durağın altında kalan (örneğin Kaba Çârgâh)
     *    bir perdenin güçlü seçilmesi kesin bir kural ihlalidir.
     * 3. Aday güçlü perdesinin koma yüksekliği durağın üzerinde olmak zorundadır (pHeight > durakHeight).
     */
    fun detectGuclu(
        events: List<PitchEvent>,
        histogram: Map<String, Double>,
        totalDuration: Double,
        durakNorm: String
    ): String {
        val durakHeight = getPerdeKomaHeight(durakNorm)

        // 1. Müzikolojik Filtre: Durağa eşit veya durağın altında kalan perdeler kesinlikle elenir!
        val eligibleCandidates = histogram.filter { (perde, _) ->
            val pHeight = getPerdeKomaHeight(perde)
            pHeight > durakHeight // Sadece durağın tiz tarafındaki perdeler aday olabilir
        }

        if (eligibleCandidates.isEmpty()) {
            // Uygun tiz aday yoksa durağın 4. derecesini (Tam Dörtlü / 22 koma tiz) varsayılan olarak ata
            return when (durakNorm) {
                "dugah" -> "neva"
                "rast" -> "neva"
                "cargah" -> "gerdaniye"
                "buselik" -> "huseyni"
                "segah" -> "neva"
                else -> "neva"
            }
        }

        val candidateScores = mutableMapOf<String, Double>()

        // 2. Histogram süre ve enerji ağırlığı
        for ((perde, weight) in eligibleCandidates) {
            candidateScores[perde] = (weight / totalDuration) * 50.0
        }

        // 3. Orta bölümdeki ısrar ve uzun kalışlar (Half Cadence / Muvakkat Karar)
        val middleEvents = events.filter {
            val norm = MakamRegistry.normalizePerdeName(it.perdeName)
            val pHeight = getPerdeKomaHeight(norm)
            pHeight > durakHeight && it.durationMs >= 200L
        }

        for (ev in middleEvents) {
            val norm = MakamRegistry.normalizePerdeName(ev.perdeName)
            candidateScores[norm] = candidateScores.getOrDefault(norm, 0.0) + (ev.durationMs * 0.04)
        }

        // 4. Müzikolojik Aralık Rezonans Bonusu:
        // Durağın 4. derecesi (Tam Dörtlü: ~22 koma) veya 5. derecesi (Tam Beşli: ~31 koma)
        for ((perde, score) in candidateScores) {
            val pHeight = getPerdeKomaHeight(perde)
            val interval = pHeight - durakHeight // Daima pozitif
            if (interval in 20..24) {
                // 4. Derece (Tam Dörtlü: örneğin Dügâh -> Nevâ)
                candidateScores[perde] = score + 25.0
            } else if (interval in 29..33) {
                // 5. Derece (Tam Beşli: örneğin Dügâh -> Hüseynî, Râst -> Gerdâniye)
                candidateScores[perde] = score + 25.0
            } else if (interval < 18) {
                // Durağa çok yakın (2. veya 3. derece) güçlü olmaz; ceza puanı
                candidateScores[perde] = score - 15.0
            }
        }

        val bestGuclu = candidateScores.maxByOrNull { it.value }?.key
        return bestGuclu ?: when (durakNorm) {
            "dugah" -> "neva"
            "rast" -> "neva"
            else -> "neva"
        }
    }

    /**
     * Ezgisel seyir tipini (Çıkıcı, İnici-Çıkıcı, İnici) tespit eder.
     *
     * MÜZİKOLOJİK KURAL (Özkan s. 51 ve s. 143):
     * 1. Uşşâk Makâmı inici-çıkıcı seyre sahiptir; güçlüsü Nevâ (Re), durağı Dügâh (La)'dır.
     * 2. Eserin yalnızca ilk 5-10 saniyesinde durak perdesine (Dügâh) dokunulması eseri "çıkıcı" yapmaz.
     * 3. 3 Aşamalı Ağırlıklı Seyir Penceresi:
     *    - Giriş Bölümü (%20): Başlangıç perdesi ve register'ı.
     *    - Gelişme Bölümü (%60): Melodinin ağırlık merkezi nerede yoğunlaşıyor? Nevâ güçlüsü ve
     *      tizler (Hüseynî, Acem, Gerdâniye) etrafında gezinme var mı?
     *    - Teslim / Karar Bölümü (%20): Nevâ'dan veya tizlerden inici bir yürüyüşle Dügâh'a inip
     *      karar veriliyor mu?
     */
    fun detectSeyir(
        events: List<PitchEvent>,
        durakNorm: String,
        gucluNorm: String
    ): SeyirType {
        if (events.size < 2) return SeyirType.INICI_CIKICI

        val durakPitch = getPerdeKomaHeight(durakNorm)
        val gucluPitch = getPerdeKomaHeight(gucluNorm)
        val firstPitch = getPerdeKomaHeight(events.first().perdeName)

        val totalEvents = events.size
        val firstPartCount = (totalEvents * 0.20).toInt().coerceIn(1, 5)
        val lastPartCount = (totalEvents * 0.25).toInt().coerceIn(1, 6)
        val middlePartEvents = if (totalEvents > firstPartCount + lastPartCount) {
            events.subList(firstPartCount, totalEvents - lastPartCount)
        } else {
            events
        }

        // 1. Giriş Bölümü Analizi
        val initialEvents = events.take(firstPartCount)
        val initialAvgPitch = calculateWeightedPitch(initialEvents)

        // 2. Gelişme / Orta Bölüm Analizi (Melodik ağırlık merkezi)
        val middleAvgPitch = calculateWeightedPitch(middlePartEvents)
        var middleGucluAndAboveDuration = 0.0
        var middleTotalDuration = 0.0
        for (ev in middlePartEvents) {
            val h = getPerdeKomaHeight(ev.perdeName)
            if (h >= gucluPitch - 4) { // Güçlü perdesi ve üstündeki tınlamalar
                middleGucluAndAboveDuration += ev.durationMs
            }
            middleTotalDuration += ev.durationMs
        }
        val middleGucluRatio = if (middleTotalDuration > 0) middleGucluAndAboveDuration / middleTotalDuration else 0.0

        // 3. Karar / Teslim Bölümü Analizi (Son perdelerin akış yönü)
        val lastEvents = events.takeLast(lastPartCount)
        val lastAvgPitch = calculateWeightedPitch(lastEvents)
        val endsOnDurak = abs(getPerdeKomaHeight(events.last().perdeName) - durakPitch) <= 4

        // --- Karar Kuralları ---

        // Kural A: Çok tiz bölgeden (gerdâniye/muhayyer veya güçlüden çok tiz) başlayıp inen seyir -> İNİCİ
        val tizThreshold = maxOf(gucluPitch + 8, 80)
        if (firstPitch >= tizThreshold && initialAvgPitch >= tizThreshold - 4.0) {
            return SeyirType.INICI
        }

        // Kural B: Saf ÇIKICI Tespiti (Râst, Çârgâh Prensibi - Özkan s. 118, 137):
        // Durak veya yeden perdesinden başlar (firstPitch <= durakPitch + 5),
        // ilk bölümde durağın çevresinde/alt dörtlüde yoğunlaşır ve güçlüye doğru basamak basamak yükselir.
        val lowerRegisterThreshold = durakPitch + (gucluPitch - durakPitch).coerceAtLeast(9) * 0.55
        if (firstPitch <= durakPitch + 5 && initialAvgPitch <= lowerRegisterThreshold) {
            return SeyirType.CIKICI
        }

        // Kural C: İNİCİ-ÇIKICI Tespiti (Özkan s. 51 & 143 - Uşşâk & Hüseynî Prensibi):
        // Eser güçlü civarından başlar veya açılışta güçlüye sıçrar;
        // Orta bölümde Nevâ ve tizler etrafında dolaşır, teslimde Dügâh durağına inip karar kılar.
        return SeyirType.INICI_CIKICI
    }

    private fun calculateWeightedPitch(eventsList: List<PitchEvent>): Double {
        if (eventsList.isEmpty()) return 40.0
        var sum = 0.0
        var totalDur = 0.0
        for (ev in eventsList) {
            val h = getPerdeKomaHeight(ev.perdeName)
            sum += h * ev.durationMs
            totalDur += ev.durationMs
        }
        return if (totalDur > 0) sum / totalDur else 40.0
    }
}
