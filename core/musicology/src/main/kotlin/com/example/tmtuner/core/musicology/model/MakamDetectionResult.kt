package com.example.tmtuner.core.musicology.model

/**
 * Makam tespit ve seyir analiz motorunun analitik çıktısı.
 *
 * @param matchedMakam Eşleşen makam profili
 * @param confidence Güven katsayısı (0.0f - 1.0f)
 * @param detectedDurak İcrada tespit edilen durak (karar) perdesi
 * @param detectedGuclu İcrada tespit edilen güçlü (yarım karar) perdesi
 * @param detectedSeyir Ezgiden çıkarılan seyir karakteri
 * @param pitchHistogram Perde isimlerine göre zaman ve enerji ağırlıklı süre histogramı
 */
data class MakamDetectionResult(
    val matchedMakam: MakamProfile,
    val confidence: Float, // 0.0f - 1.0f
    val detectedDurak: String,
    val detectedGuclu: String,
    val detectedSeyir: SeyirType,
    val pitchHistogram: Map<String, Double>
)
