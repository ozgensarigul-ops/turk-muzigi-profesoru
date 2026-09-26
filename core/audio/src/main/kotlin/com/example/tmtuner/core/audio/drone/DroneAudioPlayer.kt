package com.example.tmtuner.core.audio.drone

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * Android AudioTrack PCM akışı tabanlı kesintisiz Akustik Referans Sentezleyici yürütücüsü.
 *
 * @param sampleRate Örnekleme hızı (varsayılan 48000 Hz, desteklenmezse 44100 Hz fallback).
 * @param frameSize Döngü başına işlenecek örnek sayısı (varsayılan 2048 short örnek).
 */
class DroneAudioPlayer(
    val sampleRate: Int = 48000,
    val frameSize: Int = 2048,
    val synthesizer: AcousticDroneSynthesizer = AcousticDroneSynthesizer(sampleRate)
) : IDroneAudioPlayer {

    private val audioExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "DroneAudioTrackThread").apply {
            priority = Thread.MAX_PRIORITY
        }
    }
    private val audioDispatcher = audioExecutor.asCoroutineDispatcher()

    private val playerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var playbackJob: Job? = null
    private var stopJob: Job? = null
    private var audioTrack: AudioTrack? = null
    private val lock = Any()

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

        stopJob?.cancel()
        stopJob = null

        _isPlaying.value = true
        synthesizer.targetVolume = _volume.value

        playbackJob?.cancel()
        playbackJob = playerScope.launch(audioDispatcher) {
            try {
                initAndStartAudioTrack()
                val pcmBuffer = ShortArray(frameSize)

                while (isActive && _isPlaying.value) {
                    synthesizer.renderPcm16(pcmBuffer, 0, frameSize)

                    val track = synchronized(lock) { audioTrack } ?: break
                    if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                        break
                    }

                    val written = track.write(pcmBuffer, 0, pcmBuffer.size, AudioTrack.WRITE_BLOCKING)
                    if (written < 0) {
                        break
                    }
                }
            } catch (e: Exception) {
                // Log or ignore unexpected playback error
            } finally {
                cleanupAudioTrack()
                _isPlaying.value = false
            }
        }
    }

    override fun stop() {
        if (!_isPlaying.value && stopJob == null) return
        stopJob?.cancel()
        stopJob = playerScope.launch {
            // Anti-pop: Durdurmadan önce kazancı sıfırlayıp yumuşak iniş bekle (~50 ms)
            synthesizer.targetVolume = 0f
            delay(50)
            _isPlaying.value = false
            playbackJob?.cancelAndJoin()
            playbackJob = null
            cleanupAudioTrack()
            synthesizer.resetPhase()
            stopJob = null
        }
    }

    override fun updateFrequencies(tonicFrequency: Double, dominantFrequency: Double?) {
        if (tonicFrequency > 20.0) {
            _currentTonicFrequency.value = tonicFrequency
            val dom = dominantFrequency ?: (tonicFrequency * 1.5)
            _currentDominantFrequency.value = dom
            synthesizer.setFrequencies(tonicFrequency, dom)

            if (_isPlaying.value) {
                flushBuffer()
            }
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
        stopJob?.cancel()
        _isPlaying.value = false
        playbackJob?.cancel()
        cleanupAudioTrack()
        playerScope.cancel()
        audioDispatcher.close()
        audioExecutor.shutdown()
    }

    private fun flushBuffer() {
        synchronized(lock) {
            try {
                audioTrack?.let { track ->
                    if (track.state == AudioTrack.STATE_INITIALIZED &&
                        track.playState == AudioTrack.PLAYSTATE_PLAYING
                    ) {
                        track.pause()
                        track.flush()
                        track.play()
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun initAndStartAudioTrack() {
        cleanupAudioTrack()

        val channelConfig = AudioFormat.CHANNEL_OUT_MONO
        val audioEncoding = AudioFormat.ENCODING_PCM_16BIT

        // Sample Rate: 48000 Hz (cihaz desteklemiyorsa 44100 Hz fallback)
        val preferredSampleRate = 48000
        val fallbackSampleRate = 44100
        val minBuf48k = AudioTrack.getMinBufferSize(preferredSampleRate, channelConfig, audioEncoding)

        val (actualSampleRate, minBufferSize) = if (minBuf48k > 0) {
            preferredSampleRate to minBuf48k
        } else {
            fallbackSampleRate to AudioTrack.getMinBufferSize(fallbackSampleRate, channelConfig, audioEncoding)
        }

        synthesizer.sampleRate = actualSampleRate

        // Buffer boyutu: minBufferSize * 2 veya en az 4096 short örnek (8192 bayt)
        val minShortsBytes = 4096 * 2
        val bufferSizeBytes = maxOf(minBufferSize * 2, minShortsBytes)

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(actualSampleRate)
            .setChannelMask(channelConfig)
            .setEncoding(audioEncoding)
            .build()

        synchronized(lock) {
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
    }

    private fun cleanupAudioTrack() {
        synchronized(lock) {
            try {
                audioTrack?.let { track ->
                    if (track.state == AudioTrack.STATE_INITIALIZED) {
                        if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                            track.stop()
                        }
                        track.flush()
                        track.release()
                    }
                }
            } catch (_: Exception) {
            } finally {
                audioTrack = null
            }
        }
    }
}
