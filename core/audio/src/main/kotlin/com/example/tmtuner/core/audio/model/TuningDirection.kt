package com.example.tmtuner.core.audio.model

/**
 * Türk Müziği mikrotonal akort yönü.
 */
enum class TuningDirection(val displayName: String, val symbol: String) {
    IN_TUNE("Tam Akortlu", "✔"),
    TOO_FLAT("Pes (Tizleştirilmeli)", "▼"),
    TOO_SHARP("Tîz (Pestleştirilmeli)", "▲");

    companion object {
        fun fromKomaDifference(komaDifference: Double, toleranceKoma: Double = 0.5): TuningDirection {
            return when {
                kotlin.math.abs(komaDifference) <= toleranceKoma -> IN_TUNE
                komaDifference < 0 -> TOO_FLAT
                else -> TOO_SHARP
            }
        }
    }
}
