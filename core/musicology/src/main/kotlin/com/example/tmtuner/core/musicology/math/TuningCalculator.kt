package com.example.tmtuner.core.musicology.math

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Akort göstergesi (Tuning Gauge) ve koma sapması hesaplayıcı.
 * 1 Tanini penceresi: -4.5 koma .. +4.5 koma (toplam 9 koma).
 */
object TuningCalculator {

    /** İğnenin yeşil (Tam İsabet) sayılacağı tolerans eşiği (±0.5 koma) */
    const val IN_TUNE_TOLERANCE_KOMA = 0.5f

    /**
     * Cent farkını koma değerine dönüştürür:
     * 1 koma ≈ 22.6415 Cent
     */
    fun centsToKoma(cents: Float): Float {
        return cents / Edo53Calculator.HOLDER_KOMA_CENT.toFloat()
    }

    /**
     * Koma değerini cent değerine dönüştürür.
     */
    fun komaToCents(koma: Float): Float {
        return koma * Edo53Calculator.HOLDER_KOMA_CENT.toFloat()
    }

    /**
     * Koma sapmasını Tanini kadranı için normalize eder (-4.5f .. +4.5f aralığına kırpar).
     */
    fun clampToTaniniGauge(komaDifference: Float): Float {
        return komaDifference.coerceIn(-4.5f, 4.5f)
    }

    /**
     * Perdenin kabul edilebilir akort hassasiyetinde (merkeze ±0.5 komadan yakın) olup olmadığını belirtir.
     */
    fun isInTune(komaDifference: Float): Boolean {
        return abs(komaDifference) <= IN_TUNE_TOLERANCE_KOMA
    }

    /**
     * İcracı için sade, anlaşılır metinsel geri bildirim üretir:
     * "Tam İsabet", "+2 Koma (Dik)", "-1 Koma (Pes)" vb.
     */
    fun formatKomaFeedback(komaDifference: Float): String {
        if (isInTune(komaDifference)) {
            return "Tam İsabet"
        }
        val rounded = komaDifference.roundToInt()
        return if (rounded > 0) {
            "+$rounded Koma (Dik)"
        } else if (rounded < 0) {
            "$rounded Koma (Pes)"
        } else {
            "Tam İsabet"
        }
    }
}
