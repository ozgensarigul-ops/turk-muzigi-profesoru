package com.example.tmtuner

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tmtuner.data.models.AhenkType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.sin

class TMTunerViewModel : ViewModel() {
    private val _frequency = MutableStateFlow(0.0f)
    val frequency: StateFlow<Float> = _frequency.asStateFlow()

    private val _detectedNote = MutableStateFlow("Dinleniyor...")
    val detectedNote: StateFlow<String> = _detectedNote.asStateFlow()

    private val _detectedOctave = MutableStateFlow(1)
    val detectedOctave: StateFlow<Int> = _detectedOctave.asStateFlow()

    private val _centsDifference = MutableStateFlow(0f)
    val centsDifference: StateFlow<Float> = _centsDifference.asStateFlow()

    private val _selectedAhenk = MutableStateFlow(AhenkType.MANSUR)
    val selectedAhenk: StateFlow<AhenkType> = _selectedAhenk.asStateFlow()

    private val _isEbAltoSax = MutableStateFlow(false)
    val isEbAltoSax: StateFlow<Boolean> = _isEbAltoSax.asStateFlow()

    private val _applySegahNuance = MutableStateFlow(false)
    val applySegahNuance: StateFlow<Boolean> = _applySegahNuance.asStateFlow()

    private val _selectedMakam = MutableStateFlow("Rast")
    val selectedMakam: StateFlow<String> = _selectedMakam.asStateFlow()

    private val _isTransposed = MutableStateFlow(false)
    val isTransposed: StateFlow<Boolean> = _isTransposed.asStateFlow()

    private val _selectedTranspose = MutableStateFlow("Konsert (Do)")
    val selectedTranspose: StateFlow<String> = _selectedTranspose.asStateFlow()

    private val _isDronePlaying = MutableStateFlow(false)
    val isDronePlaying: StateFlow<Boolean> = _isDronePlaying.asStateFlow()

    private val _droneVolume = MutableStateFlow(0.5f)
    val droneVolume: StateFlow<Float> = _droneVolume.asStateFlow()

    private var audioJob: Job? = null
    private var audioTrack: AudioTrack? = null
    private var recordingJob: Job? = null

    private val aeuRatios = doubleArrayOf(
        1.0, 256.0/243.0, 2187.0/2048.0, 65536.0/59049.0, 9.0/8.0, 32.0/27.0,
        19683.0/16384.0, 8192.0/6561.0, 81.0/64.0, 4.0/3.0, 177147.0/131072.0,
        1024.0/729.0, 729.0/512.0, 262144.0/177147.0, 3.0/2.0, 128.0/81.0,
        6561.0/4096.0, 32768.0/19683.0, 27.0/16.0, 16.0/9.0, 59049.0/32768.0,
        4096.0/2187.0, 243.0/128.0, 1048576.0/531441.0
    )

    private val namesOctave0 = arrayOf("Kaba Çârgâh", "Kaba Nîm Hicâz", "Kaba Hicâz", "Kaba Dik Hicâz", "Yegâh", "Kaba Nîm Hisâr", "Kaba Hisâr", "Kaba Dik Hisâr", "Hüseynî Aşîrân", "Acem Aşîrân", "Dik Acem Aşîrân", "Irak", "Geveşt", "Dik Geveşt", "Rast", "Nîm Zîrgûle", "Zîrgûle", "Dik Zîrgûle", "Dügâh", "Kürdî", "Dik Kürdî", "Segâh", "Bûselik", "Dik Bûselik")
    private val namesOctave1 = arrayOf("Çârgâh", "Nîm Hicâz", "Hicâz", "Dik Hicâz", "Neva", "Nîm Hisâr", "Hisâr", "Dik Hisâr", "Hüseynî", "Acem", "Dik Acem", "Eviç", "Mahur", "Dik Mahur", "Gerdâniye", "Nîm Şehnâz", "Şehnâz", "Dik Şehnâz", "Muhayyer", "Sünbüle", "Dik Sünbüle", "Tîz Segâh", "Tîz Bûselik", "Tîz Dik Bûselik")
    private val namesOctave2 = arrayOf("Tîz Çârgâh", "Tîz Nîm Hicâz", "Tîz Hicâz", "Tîz Dik Hicâz", "Tîz Neva", "Tîz Nîm Hisâr", "Tîz Hisâr", "Tîz Dik Hisâr", "Tîz Hüseynî", "Tîz Acem", "Tîz Dik Acem", "Tîz Eviç", "Tîz Mahur", "Tîz Dik Mahur", "Tîz Gerdâniye", "Tîz Nîm Şehnâz", "Tîz Şehnâz", "Tîz Dik Şehnâz", "Tîz Muhayyer", "Tîz Sünbüle", "Tîz Dik Sünbüle", "En Tîz Segâh", "En Tîz Bûselik", "En Tîz Dik Bûselik")

    private var currentTMNotes = listOf<Pair<Double, String>>()

    init { updateTMNotesMap() }

    fun setAhenk(ahenk: AhenkType) {
        _selectedAhenk.value = ahenk
        updateTMNotesMap()
        restartAudioIfPlaying()
    }
    fun updateAhenk(newAhenk: String) {
        _selectedAhenk.value = AhenkType.fromString(newAhenk)
        updateTMNotesMap()
        restartAudioIfPlaying()
    }
    fun setEbAltoSax(enabled: Boolean) { _isEbAltoSax.value = enabled }
    fun setSegahNuance(enabled: Boolean) { _applySegahNuance.value = enabled }
    fun updateMakam(newMakam: String) { _selectedMakam.value = newMakam; restartAudioIfPlaying() }
    fun toggleTranspose() { _isTransposed.value = !_isTransposed.value }
    fun updateTranspose(newTranspose: String) {
        _selectedTranspose.value = newTranspose
        _isTransposed.value = (newTranspose != "Konsert (Do)")
    }
    fun setDroneVolume(vol: Float) { _droneVolume.value = vol }
    fun toggleDrone() { _isDronePlaying.value = !_isDronePlaying.value; if (_isDronePlaying.value) startAudio() else stopAudio() }
    private fun restartAudioIfPlaying() { if (_isDronePlaying.value) { stopAudio(); startAudio() } }

    private fun updateTMNotesMap() {
        val baseLa = when (_selectedAhenk.value) {
            AhenkType.BOLAHENK -> 586.0
            AhenkType.KIZ -> 415.0
            AhenkType.SUPURDE -> 523.0
            AhenkType.MANSUR -> 440.0
        }
        val kabaCargahFreq = baseLa / 2.25
        val newNotes = mutableListOf<Pair<Double, String>>()
        for (o in 0..2) {
            val names = when(o) { 0 -> namesOctave0; 1 -> namesOctave1; else -> namesOctave2 }
            val multiplier = 2.0.pow(o)
            for (i in 0..23) {
                newNotes.add(Pair(kabaCargahFreq * aeuRatios[i] * multiplier, names[i]))
            }
        }
        currentTMNotes = newNotes
    }

    fun getDroneNoteName(): String {
        return when (_selectedMakam.value) {
            "Rast", "Nihavend", "Mahur" -> "Râst Perdesi (Sol)"
            "Segâh" -> "Segâh Perdesi (Si♭₁)"
            "Çârgâh" -> "Çârgâh Perdesi (Do)"
            else -> "Dügâh Perdesi (La)"
        }
    }

    private fun calculateTargetFrequency(): Double {
        val baseLa = when (_selectedAhenk.value) {
            AhenkType.BOLAHENK -> 586.0
            AhenkType.KIZ -> 415.0
            AhenkType.SUPURDE -> 523.0
            AhenkType.MANSUR -> 440.0
        }
        val kabaCargahFreq = baseLa / 2.25
        return when (_selectedMakam.value) {
            "Rast", "Nihavend", "Mahur" -> kabaCargahFreq * 1.5 // Rast perdesi (293.33 Hz)
            "Segâh" -> kabaCargahFreq * (4096.0 / 2187.0) // Segâh perdesi (366.27 Hz)
            "Çârgâh" -> kabaCargahFreq * 2.0 // Çârgâh perdesi (391.11 Hz)
            else -> kabaCargahFreq * (27.0 / 16.0) // Dügâh perdesi (330.00 Hz)
        }
    }

    private fun startAudio() {
        audioJob?.cancel()
        audioJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val sampleRate = 44100
                val targetFreq = calculateTargetFrequency()
                var bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
                if (bufferSize <= 0) bufferSize = 4096

                audioTrack = AudioTrack.Builder().setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(sampleRate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()).setBufferSizeInBytes(bufferSize).setTransferMode(AudioTrack.MODE_STREAM).build()
                audioTrack?.play()
                val buffer = ShortArray(bufferSize)
                var angle = 0.0
                val increment = 2.0 * PI * targetFreq / sampleRate

                while (isActive && _isDronePlaying.value) {
                    for (i in buffer.indices) { buffer[i] = (sin(angle) * Short.MAX_VALUE * _droneVolume.value).toInt().toShort(); angle += increment }
                    audioTrack?.write(buffer, 0, buffer.size)
                }
                stopAudioInternally()
            } catch (e: Exception) { e.printStackTrace(); _isDronePlaying.value = false; stopAudioInternally() }
        }
    }

    private fun stopAudioInternally() { try { audioTrack?.stop(); audioTrack?.release() } catch (e: Exception) {} finally { audioTrack = null } }
    private fun stopAudio() { audioJob?.cancel(); stopAudioInternally() }

    fun startListening() {
        if (recordingJob != null) return
        recordingJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val sampleRate = 44100
                val bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
                val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)

                if (audioRecord.state != AudioRecord.STATE_INITIALIZED) return@launch
                audioRecord.startRecording()
                val buffer = ShortArray(bufferSize)

                while (isActive) {
                    val readResult = audioRecord.read(buffer, 0, bufferSize)
                    if (readResult > 0) {
                        val pitch = calculatePitchAutocorrelation(buffer, sampleRate)
                        if (pitch > 50f && pitch < 2000f) {
                            val adjustedPitch = when (_selectedTranspose.value) {
                                "Tenor / Soprano Saksafon (Bb)" -> pitch * (9.0 / 8.0)
                                "Alto Saksafon (Eb)" -> pitch * (27.0 / 16.0)
                                else -> if (_isTransposed.value) pitch * (9.0 / 8.0) else pitch.toDouble()
                            }
                            _frequency.value = pitch

                            val closestNote = currentTMNotes.minByOrNull { abs(it.first - adjustedPitch) }
                            if (closestNote != null) {
                                _detectedNote.value = closestNote.second
                                _centsDifference.value = (1200.0 * log2(adjustedPitch / closestNote.first)).toFloat()

                                _detectedOctave.value = when (closestNote.second) {
                                    in namesOctave0 -> 0 // Kaba
                                    in namesOctave2 -> 2 // Tîz
                                    else -> 1 // Orta
                                }
                            }
                        }
                    }
                }
                audioRecord.stop()
                audioRecord.release()
            } catch (e: SecurityException) { e.printStackTrace() }
        }
    }

    private fun calculatePitchAutocorrelation(audioData: ShortArray, sampleRate: Int): Float {
        var maxCorrelation = 0f
        var bestLag = 0
        val amplitude = audioData.maxOrNull() ?: 0
        if (amplitude < 500) return 0f

        val minLag = sampleRate / 1000
        val maxLag = sampleRate / 70

        for (lag in minLag until maxLag) {
            var correlation = 0f
            for (i in 0 until audioData.size - lag) { correlation += audioData[i] * audioData[i + lag] }
            if (correlation > maxCorrelation) { maxCorrelation = correlation; bestLag = lag }
        }
        return if (bestLag == 0) 0f else sampleRate.toFloat() / bestLag
    }

    override fun onCleared() { super.onCleared(); stopAudio(); recordingJob?.cancel() }
}
