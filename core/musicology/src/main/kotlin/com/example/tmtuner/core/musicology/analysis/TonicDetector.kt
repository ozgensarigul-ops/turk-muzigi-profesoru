package com.example.tmtuner.core.musicology.analysis

import com.example.tmtuner.core.musicology.engine.PitchEvent
import com.example.tmtuner.core.musicology.registry.MakamRegistry

/**
 * Karar Perdesi (Tonic) Hiyerarşisi ve Tespiti.
 * İsmail Hakkı Özkan'ın "Türk Mûsikîsi Nazariyatı ve Usûlleri" kurallarına (s. 88-91, 143)
 * tam uyumlu Karar Perdesi Analizörü.
 */
object TonicDetector {

    /**
     * Karar (Durak / Tonic) perdesini tespit eder.
     *
     * MÜZİKOLOJİK KURALLAR (Özkan s. 91 & s. 143):
     * 1. Cümlenin son ve en kararlı durak perdesi Dügâh (Mansur ekseninde A4 / 440 Hz
     *    veya ahenk oranına göre bağıl La) olarak ağırlıklandırılır.
     * 2. Karar perdesi puanlamasında (Final Rest / Cadence), son 3-5 saniyedeki en pest ve
     *    kararlı ses Dügâh ise Durak = Dügâh puanına +40 bonus verilir.
     * 3. Râst (Sol) perdesi yalnızca yeden olarak geçtiğinde (Dügâh'a bağlanan geçici
     *    alt basamak) durak puanı almamalıdır (Özkan s. 91, Uşşâk'ın yedeni tam sesli Râst'tır).
     */
    fun detectDurak(
        events: List<PitchEvent>,
        histogram: Map<String, Double>,
        totalDuration: Double
    ): String {
        if (events.isEmpty()) return "dugah"

        val candidateScores = mutableMapOf<String, Double>()

        // 1. Histogram genel süre ve enerji ağırlığı
        for ((perde, weight) in histogram) {
            candidateScores[perde] = (weight / totalDuration) * 40.0
        }

        // 2. Son perdelerin (karar yürüyüşü) etkisi
        val recentCount = (events.size * 0.25).toInt().coerceIn(1, 10)
        val endingEvents = events.takeLast(recentCount)
        var endingWeightSum = 0.0

        for ((idx, ev) in endingEvents.withIndex()) {
            val norm = MakamRegistry.normalizePerdeName(ev.perdeName)
            val recencyMultiplier = (idx + 1).toDouble() / recentCount
            val w = ev.durationMs * recencyMultiplier
            candidateScores[norm] = candidateScores.getOrDefault(norm, 0.0) + (w * 0.08)
            endingWeightSum += w
        }

        // 3. En son tınlayan ses durak ise belirgin öncelik (+25 puan)
        val lastEvent = events.last()
        val lastEventNorm = MakamRegistry.normalizePerdeName(lastEvent.perdeName)
        candidateScores[lastEventNorm] = candidateScores.getOrDefault(lastEventNorm, 0.0) + 25.0

        // 4. Son 3-5 saniyelik kadans penceresi (Final Rest / Cadence)
        var accumulatedCadenceMs = 0L
        val cadenceEvents = mutableListOf<PitchEvent>()
        for (i in events.indices.reversed()) {
            cadenceEvents.add(0, events[i])
            accumulatedCadenceMs += events[i].durationMs
            if (accumulatedCadenceMs >= 3500L) {
                break
            }
        }

        // Râst'ın yalnızca yeden olarak geçip geçmediğini belirle (Özkan s. 91)
        val rastEventsInCadence = cadenceEvents.filter { MakamRegistry.normalizePerdeName(it.perdeName) == "rast" }
        val dugahEventsInCadence = cadenceEvents.filter { MakamRegistry.normalizePerdeName(it.perdeName) == "dugah" }

        val isRastPassingYeden = rastEventsInCadence.isNotEmpty() &&
                lastEventNorm != "rast" &&
                dugahEventsInCadence.isNotEmpty() &&
                (lastEventNorm == "dugah" || dugahEventsInCadence.sumOf { it.durationMs } > rastEventsInCadence.sumOf { it.durationMs })

        if (isRastPassingYeden) {
            // Râst yalnızca yeden olarak geçtiğinde durak puanı almamalıdır
            candidateScores.remove("rast")
            candidateScores["rast"] = 0.0
        }

        // 5. Kadans penceresindeki en pest ve kararlı ses analizi
        // Yeden elendikten sonra kadans penceresindeki tınlayan perdeler
        val cadencePerdesWithoutYeden = cadenceEvents
            .map { MakamRegistry.normalizePerdeName(it.perdeName) }
            .filter { if (isRastPassingYeden) it != "rast" else true }
            .distinct()

        val lowestPerdeInCadence = cadencePerdesWithoutYeden.minByOrNull {
            MakamSeyirAnalyzer.getPerdeKomaHeight(it)
        }

        val totalDugahDurationInCadence = dugahEventsInCadence.sumOf { it.durationMs }
        val isDugahStableInCadence = totalDugahDurationInCadence >= 500L || lastEventNorm == "dugah"

        if (lowestPerdeInCadence == "dugah" && isDugahStableInCadence) {
            // Son 3-5 saniyedeki en pest ve kararlı ses Dügâh ise Durak = Dügâh puanına +40 bonus ver
            candidateScores["dugah"] = candidateScores.getOrDefault("dugah", 0.0) + 40.0
        }

        return candidateScores.maxByOrNull { it.value }?.key ?: "dugah"
    }
}
