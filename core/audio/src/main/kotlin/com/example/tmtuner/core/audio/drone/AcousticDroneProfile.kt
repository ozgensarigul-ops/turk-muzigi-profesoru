package com.example.tmtuner.core.audio.drone

/**
 * Akustik Referans Sentezleyici (Drone / Dem Sesi) Tını Profilleri.
 *
 * İsmail Hakkı Özkan nazariyatına ve Türk Müziği icra pratiğine uygun olarak:
 * - TANBURA: Klasik mızraplı tanburun dem teli rezonansı ve zengin harmonik spektrumu.
 * - NEY: Ney sazının üfleme / nefes karakterini yansıtan yumuşak ve tek harmonik ağırlıklı tını.
 * - PURE_SINE: Laboratuvar ve hassas mikrotonal frekans eşleme için saf sinüs dalgası.
 */
enum class AcousticDroneProfile(
    val displayName: String,
    val description: String,
    val harmonicWeights: DoubleArray
) {
    /**
     * Tanbûra Dem Teli:
     * Gövde rezonansı ve sempati telleri etkisiyle hem tek hem çift harmonikler zengindir.
     * f0 (1.0), 2f (0.65), 3f (0.42), 4f (0.28), 5f (0.16), 6f (0.10), 7f (0.05)
     */
    TANBURA(
        displayName = "Tanbûra Dem Sesi",
        description = "Geleneksel tanbura dem teli rezonansı ve zengin harmonikler",
        harmonicWeights = doubleArrayOf(1.0, 0.65, 0.42, 0.28, 0.16, 0.10, 0.05)
    ),

    /**
     * Ney Tınısı:
     * Kamış ve nefes borusu fiziği gereği temel frekans güçlü, 3. harmonik belirgin,
     * çift harmonikler zayıftır. Kulak yormayan sıcak akustik tını.
     * f0 (1.0), 2f (0.18), 3f (0.38), 4f (0.08), 5f (0.14), 6f (0.03)
     */
    NEY(
        displayName = "Ney Tınısı",
        description = "Sıcak nefes karakteri ve yumuşak harmonik düşüş",
        harmonicWeights = doubleArrayOf(1.0, 0.18, 0.38, 0.08, 0.14, 0.03)
    ),

    /**
     * Saf Sinüs (Referans Ton):
     * Yalnızca 1. harmonik (saf f0). Sıfır distorsiyon, koma farkını dinleyerek eşleme için ideal.
     */
    PURE_SINE(
        displayName = "Saf Referans (Sinüs)",
        description = "Hassas akort eşleme ve laboratuvar sinüs tonu",
        harmonicWeights = doubleArrayOf(1.0)
    )
}
