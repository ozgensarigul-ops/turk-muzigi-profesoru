package com.example.tmtuner.core.audio.model

import com.example.tmtuner.core.musicology.model.Ahenk

/**
 * 53-EDO Türk Müziği mikrotonal tuner analiz sonucu.
 *
 * @param detectedFrequency Mikrofondan veya ses tamponundan algılanan frekans (Hz).
 * @param targetFrequency Türk Müziği 53-EDO perde atlasındaki hedef referans frekans (Hz).
 * @param perdeName Eşleşen makam perdesinin adı (Örn: "Dügâh", "Kaba Çârgâh", "Segâh", "Nevâ").
 * @param mutlakKoma Perdenin 3 oktavlık geniş düzendeki mutlak koma numarası (0..158).
 * @param oktavKomaMod Perdenin oktav içi 53-EDO koma indeksi (0..52).
 * @param komaDifference Hedef frekanstan koma sapması: ΔKoma = 53 * log2(f_det / f_target).
 * @param centsDifference Cent cinsinden sapma: ΔCents = ΔKoma * (1200 / 53).
 * @param isTuned Belirlenen tolerans dahilinde perdenin tam akortlu olup olmadığı.
 * @param clarity MPM algoritmasından gelen perde berraklığı / güven skoru (0.0 .. 1.0).
 * @param ahenk Kullanılan icra ahengi (MANSUR, BOLAHENK, KIZ vb.).
 * @param isSegahToleranceApplied Özkan s. 51 gereği Segâh perdesinde -1.0/-2.0 koma icra toleransının uygulanıp uygulanmadığı.
 * @param direction Akort yönü (IN_TUNE, TOO_FLAT / PES, TOO_SHARP / TIZ).
 * @param octaveIndex Perdenin oktavı (0: Kaba/Pes, 1: Ana/Orta, 2: Tîz).
 * @param octaveName Oktav adı ("Kaba / Pes", "Ana / Orta", "Tîz / En Tîz").
 */
data class TunerPitchResult(
    val detectedFrequency: Double,
    val targetFrequency: Double,
    val perdeName: String,
    val mutlakKoma: Int,
    val oktavKomaMod: Int,
    val komaDifference: Double,
    val centsDifference: Double,
    val isTuned: Boolean,
    val clarity: Double,
    val ahenk: Ahenk,
    val isSegahToleranceApplied: Boolean = false,
    val direction: TuningDirection = TuningDirection.fromKomaDifference(komaDifference),
    val octaveIndex: Int = 1,
    val octaveName: String = "Ana / Orta"
) {
    /**
     * Koma farkını insan tarafından okunabilir Türk Müziği formatında döndürür.
     * Örn: "+0.3 Koma Tiz", "-1.2 Koma Pes", "Tam Akortlu"
     */
    fun formatKomaStatus(): String {
        return when {
            isTuned -> "Tam Akortlu (✔)"
            komaDifference > 0 -> String.format("+%.2f Koma Tîz (▲)", komaDifference)
            else -> String.format("%.2f Koma Pes (▼)", komaDifference)
        }
    }
}
