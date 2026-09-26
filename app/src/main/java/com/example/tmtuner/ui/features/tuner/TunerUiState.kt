package com.example.tmtuner.ui.features.tuner

import com.example.tmtuner.core.audio.drone.AcousticDroneProfile
import com.example.tmtuner.core.audio.model.SegahNuanceMode
import com.example.tmtuner.core.musicology.model.Ahenk
import com.example.tmtuner.core.musicology.model.MakamDetectionResult
import com.example.tmtuner.core.musicology.model.NeyType
import com.example.tmtuner.core.musicology.model.TransposingInstrument

/**
 * 53-EDO Mikrotonal Tuner UI Durumu (Unidirectional Data Flow / MVVM).
 */
data class TunerUiState(
    val isRecording: Boolean = false,
    val detectedFrequency: Double = 0.0,       // Çalınan / analiz edilen perde frekansı (Hz)
    val concertFrequency: Double = 0.0,        // Mikrofondan algılanan mutlak konsert frekansı (Hz)
    val targetFrequency: Double = 0.0,         // 53-EDO perde hedef frekansı (Hz)
    val perdeName: String = "—",               // Eşleşen Türk Müziği perdesi
    val octaveName: String = "",               // "Kaba / Pes", "Ana / Orta", "Tîz"
    val mutlakKoma: Int = 0,                   // 3 oktavlık dizideki mutlak koma (0..158)
    val komaDifference: Double = 0.0,          // ΔKoma = 53 * log2(f_det / f_target)
    val centsDifference: Double = 0.0,         // ΔCents = 1200 * log2(f_det / f_target)
    val isTuned: Boolean = false,              // Tolerans dahilinde akortlu mu
    val clarity: Double = 0.0,                 // MPM berraklık skoru (0.0 .. 1.0)
    val rmsLevel: Float = 0f,                  // Mikrofon ses giriş seviyesi (0.0 .. 1.0)
    val selectedAhenk: Ahenk = Ahenk.MANSUR,
    val selectedInstrument: TransposingInstrument = TransposingInstrument.CONCERT_C,
    val selectedNeyType: NeyType = NeyType.MANSUR,
    val segahMode: SegahNuanceMode = SegahNuanceMode.TOLERANT_RANGE,
    val usePhysicalMansur: Boolean = false,    // Fizik Mansur (256 Hz Kaba Çârgâh) vs Diyapazon (Dügâh 440 Hz)
    val isSegahNuanceActive: Boolean = false,  // Segâh perdesi tespit edildi ve nüans modu aktif mi
    val statusMessage: String = "Mikrofon bekleniyor...",

    // Akustik Referans Sentezleyici (Drone / Dem Sesi) Durumları
    val isDronePlaying: Boolean = false,
    val droneVolume: Float = 0.5f,
    val droneProfile: AcousticDroneProfile = AcousticDroneProfile.TANBURA,
    val isDualDrone: Boolean = false,
    val selectedMakam: String = "Rast",
    val droneTonicNoteName: String = "Râst (Sol)",
    val droneTonicFrequency: Double = 293.33,
    val droneDominantNoteName: String = "Nevâ (Re)",
    val droneDominantFrequency: Double = 440.0,

    // Canlı 53-EDO Makam Tanıma ve Seyir Analiz Durumu
    val makamDetectionState: MakamDetectionResult? = null
)
