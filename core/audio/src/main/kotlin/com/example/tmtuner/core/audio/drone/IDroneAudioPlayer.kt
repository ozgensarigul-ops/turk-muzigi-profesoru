package com.example.tmtuner.core.audio.drone

import kotlinx.coroutines.flow.StateFlow

/**
 * Akustik Referans Sentezleyici (Drone / Dem Sesi) yürütme motoru arayüzü.
 * UI ve ViewModel katmanını AudioTrack donanımından soyutlar.
 */
interface IDroneAudioPlayer {
    val isPlaying: StateFlow<Boolean>
    val volume: StateFlow<Float>
    val profile: StateFlow<AcousticDroneProfile>
    val isDualDrone: StateFlow<Boolean>
    val currentTonicFrequency: StateFlow<Double>
    val currentDominantFrequency: StateFlow<Double>

    /**
     * Belirtilen tonik (karar) ve opsiyonel güçlü (dominant) frekansıyla dem sesini başlatır.
     */
    fun start(tonicFrequency: Double, dominantFrequency: Double? = null)

    /**
     * Dem sesini durdurur.
     */
    fun stop()

    /**
     * Çalmakta olan dem sesinin frekansını kesintisiz olarak günceller.
     */
    fun updateFrequencies(tonicFrequency: Double, dominantFrequency: Double? = null)

    /**
     * Ses seviyesini (0.0f .. 1.0f) ayarlar.
     */
    fun setVolume(volume: Float)

    /**
     * Akustik tını profilini (Tanbûra, Ney, Saf Sinüs) değiştirir.
     */
    fun setProfile(profile: AcousticDroneProfile)

    /**
     * Çift dem (Karar + Güçlü 5'li) modunu açar veya kapatır.
     */
    fun setDualDrone(enabled: Boolean)

    /**
     * Donanım kaynaklarını serbest bırakır.
     */
    fun release()
}
