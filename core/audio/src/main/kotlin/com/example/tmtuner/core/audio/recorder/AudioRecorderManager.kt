package com.example.tmtuner.core.audio.recorder

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

/**
 * Interface for audio recording streams.
 * Allows decoupling AudioRecord from testing and ViewModel.
 */
interface IAudioRecorder {
    val isRecording: StateFlow<Boolean>
    val sampleRate: Int
    val frameSize: Int
    fun startRecording(): Flow<ShortArray>
    fun stopRecording()
}

/**
 * Android AudioRecord implementation of [IAudioRecorder].
 * Streams 16-bit Mono PCM audio frames via Kotlin [Flow].
 *
 * @param sampleRate Sampling rate in Hz (typically 44100 or 48000).
 * @param frameSize Number of samples per frame (e.g. 2048 or 4096).
 * @param audioSource Audio source from [MediaRecorder.AudioSource], default is MIC.
 */
class AudioRecorderManager(
    override var sampleRate: Int = 44100,
    override val frameSize: Int = 2048,
    private val audioSource: Int = MediaRecorder.AudioSource.MIC
) : IAudioRecorder {

    private val _isRecording = MutableStateFlow(false)
    override val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    @SuppressLint("MissingPermission")
    override fun startRecording(): Flow<ShortArray> = flow {
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioEncoding = AudioFormat.ENCODING_PCM_16BIT

        // Olası donanım/emülatör farklılıkları için sıralı aday örnekleme hızları
        val candidateRates = intArrayOf(sampleRate, 44100, 48000, 16000).distinct()
        var audioRecord: AudioRecord? = null

        for (rate in candidateRates) {
            val minBufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioEncoding)
            // minBufferSize yetersizliklerine veya hata kodlarına karşı fallback tampon hesabı
            val bufferSizeBytes = if (minBufferSize > 0) {
                maxOf(minBufferSize * 4, frameSize * 4, 4096)
            } else {
                maxOf(frameSize * 4, 4096)
            }

            try {
                val record = AudioRecord(
                    audioSource,
                    rate,
                    channelConfig,
                    audioEncoding,
                    bufferSizeBytes
                )

                if (record.state == AudioRecord.STATE_INITIALIZED) {
                    audioRecord = record
                    sampleRate = rate
                    android.util.Log.i("TMTunerAudio", "AudioRecord başarıyla başlatıldı: sampleRate=$rate, bufferSize=$bufferSizeBytes")
                    break
                } else {
                    record.release()
                }
            } catch (se: SecurityException) {
                // Güvenlik / izin hatası üst katmana iletilmeli
                throw se
            } catch (e: Exception) {
                android.util.Log.w("TMTunerAudio", "SampleRate $rate denenirken hata: ${e.message}")
            }
        }

        if (audioRecord == null || audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            throw IllegalStateException("AudioRecord başlatılamadı. Hiçbir örnekleme hızında STATE_INITIALIZED elde edilemedi.")
        }

        try {
            audioRecord.startRecording()
            if (audioRecord.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                throw IllegalStateException("AudioRecord kayda başlayamadı. State: ${audioRecord.recordingState}")
            }

            _isRecording.value = true
            val buffer = ShortArray(frameSize)
            var logCounter = 0

            while (coroutineContext.isActive && _isRecording.value) {
                val readCount = audioRecord.read(buffer, 0, frameSize)
                if (readCount > 0) {
                    if (logCounter++ % 50 == 0) {
                        var maxSample = 0
                        for (s in buffer) {
                            val absS = kotlin.math.abs(s.toInt())
                            if (absS > maxSample) maxSample = absS
                        }
                        android.util.Log.d("TMTunerAudio", "Ses tamponu okundu ($readCount örnek), Tepe genlik: $maxSample")
                    }
                    emit(buffer.copyOf(readCount))
                } else if (readCount < 0) {
                    // AudioRecord.ERROR_INVALID_OPERATION, ERROR_BAD_VALUE, ERROR_DEAD_OBJECT
                    android.util.Log.e("TMTunerAudio", "AudioRecord okuma hatası: $readCount")
                    break
                }
            }
        } finally {
            try {
                if (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    audioRecord.stop()
                }
            } catch (_: Exception) {
                // Ignore cleanup errors
            }
            try {
                audioRecord.release()
            } catch (_: Exception) {
                // Ignore cleanup errors
            }
            _isRecording.value = false
        }
    }.flowOn(Dispatchers.IO)

    override fun stopRecording() {
        _isRecording.value = false
    }
}
