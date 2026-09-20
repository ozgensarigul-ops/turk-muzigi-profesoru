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
    override val sampleRate: Int = 44100,
    override val frameSize: Int = 2048,
    private val audioSource: Int = MediaRecorder.AudioSource.MIC
) : IAudioRecorder {

    private val _isRecording = MutableStateFlow(false)
    override val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    @SuppressLint("MissingPermission")
    override fun startRecording(): Flow<ShortArray> = flow {
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioEncoding = AudioFormat.ENCODING_PCM_16BIT

        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioEncoding)
        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            throw IllegalStateException("Invalid AudioRecord parameters: sampleRate=$sampleRate")
        }

        // Buffer size in bytes: allocate at least 2x minBufferSize or 2x frameSize bytes
        val bufferSizeBytes = maxOf(minBufferSize * 2, frameSize * 2)

        var audioRecord: AudioRecord? = null
        try {
            audioRecord = AudioRecord(
                audioSource,
                sampleRate,
                channelConfig,
                audioEncoding,
                bufferSizeBytes
            )

            if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                throw IllegalStateException("AudioRecord initialization failed. State: ${audioRecord.state}")
            }

            audioRecord.startRecording()
            _isRecording.value = true

            val buffer = ShortArray(frameSize)

            while (coroutineContext.isActive && _isRecording.value) {
                val readCount = audioRecord.read(buffer, 0, frameSize)
                if (readCount > 0) {
                    emit(buffer.copyOf(readCount))
                } else if (readCount < 0) {
                    // AudioRecord.ERROR_INVALID_OPERATION, ERROR_BAD_VALUE, ERROR_DEAD_OBJECT
                    break
                }
            }
        } finally {
            try {
                if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    audioRecord.stop()
                }
            } catch (_: Exception) {
                // Ignore cleanup errors
            }
            audioRecord?.release()
            _isRecording.value = false
        }
    }.flowOn(Dispatchers.IO)

    override fun stopRecording() {
        _isRecording.value = false
    }
}
