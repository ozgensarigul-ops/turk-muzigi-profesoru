package com.example.tmtuner.core.audio.drone

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Android AudioTrack PCM akışı tabanlı kesintisiz Akustik Referans Sentezleyici yürütücüsü.
 *
 * @param sampleRate Örnekleme hızı (varsayılan 44100 Hz).
 * @param frameSize Döngü başına işlenecek örnek sayısı (varsayılan 2048 örnek).
 */
class DroneAudioPlayer(
    val sampleRate: Int = 44100,
    val frameSize: Int = 2048,
    val synthesizer: AcousticDroneSynthesizer = AcousticDroneSynthesizer(sampleRate)
) : IDroneAudioPlayer {

    private val playerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var playbackJob: Job? = null
    private var audioTrack: AudioTrack? = null

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _volume = MutableStateFlow(0.5f)
    override val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _profile = MutableStateFlow(AcousticDroneProfile.TANBURA)
    override val profile: StateFlow<AcousticDroneProfile> = _profile.asStateFlow()

    private val _isDualDrone = MutableStateFlow(false)
    override val isDualDrone: StateFlow<Boolean> = _isDualDrone.asStateFlow()

    private val _currentTonicFrequency = MutableStateFlow(440.0)
    override val currentTonicFrequency: StateFlow<Double> = _currentTonicFrequency.asStateFlow()

    private val _currentDominantFrequency = MutableStateFlow(660.0)
    override val currentDominantFrequency: StateFlow<Double> = _currentDominantFrequency.asStateFlow()

    init {
        synthesizer.targetVolume = _volume.value
        synthesizer.profile = _profile.value
        synthesizer.isDualDroneEnabled = _isDualDrone.value
    }

    override fun start(tonicFrequency: Double, dominantFrequency: Double?) {
        updateFrequencies(tonicFrequency, dominantFrequency)
        if (_isPlaying.value) return

        _isPlaying.value = true
        synthesizer.targetVolume = _volume.value

        playbackJob?.cancel()
        playbackJob = playerScope.launch {
            try {
                initAndStartAudioTrack()
                val buffer = ShortArray(frameSize)

                while (isActive && _isPlaying.value) {
                    synthesizer.renderPcm16(buffer, 0, frameSize)
                    val written = audioTrack?.write(buffer, 0, frameSize) ?: -1
                    if (written < 0) {
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cleanupAudioTrack()
                _isPlaying.value = false
            }
        }
    }

    override fun stop() {
        if (!_isPlaying.value) return
        playerScope.launch {
            // Anti-pop: Durdurmadan önce kazancı sıfırlayıp kısa bir rampa bekle
            synthesizer.targetVolume = 0f
            delay(30)
            _isPlaying.value = false
            playbackJob?.cancel()
            playbackJob = null
            cleanupAudioTrack()
            synthesizer.resetPhase()
        }
    }

    override fun updateFrequencies(tonicFrequency: Double, dominantFrequency: Double?) {
        if (tonicFrequency > 20.0) {
            _currentTonicFrequency.value = tonicFrequency
            val dom = dominantFrequency ?: (tonicFrequency * 1.5)
            _currentDominantFrequency.value = dom
            synthesizer.setFrequencies(tonicFrequency, dom)
        }
    }

    override fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0.0f, 1.0f)
        _volume.value = clamped
        if (_isPlaying.value) {
            synthesizer.targetVolume = clamped
        }
    }

    override fun setProfile(profile: AcousticDroneProfile) {
        _profile.value = profile
        synthesizer.profile = profile
    }

    override fun setDualDrone(enabled: Boolean) {
        _isDualDrone.value = enabled
        synthesizer.isDualDroneEnabled = enabled
    }

    override fun release() {
        stop()
    }

    private fun initAndStartAudioTrack() {
        cleanupAudioTrack()

        val channelConfig = AudioFormat.CHANNEL_OUT_MONO
        val audioEncoding = AudioFormat.ENCODING_PCM_16BIT
        val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioEncoding)
        val bufferSizeBytes = maxOf(minBufferSize * 2, frameSize * 2 * 2)

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setChannelMask(channelConfig)
            .setEncoding(audioEncoding)
            .build()

        val track = AudioTrack(
            attributes,
            format,
            bufferSizeBytes,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )

        track.play()
        audioTrack = track
    }

    private fun cleanupAudioTrack() {
        try {
            audioTrack?.let { track ->
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.stop()
                }
                track.release()
            }
        } catch (_: Exception) {
            // Temizleme hataları yok sayılır
        }
        audioTrack = null
    }
}
