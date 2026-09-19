package com.example.tmtuner

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tmtuner.core.musicology.analysis.MakamClassifierEngine
import com.example.tmtuner.core.musicology.analysis.PitchHistogram
import com.example.tmtuner.core.musicology.atlas.AeuScaleAtlas
import com.example.tmtuner.core.musicology.atlas.PitchMatcher
import com.example.tmtuner.core.musicology.model.Ahenk
import com.example.tmtuner.core.musicology.model.PerdeNote
import com.example.tmtuner.core.musicology.model.TranspositionMode
import com.example.tmtuner.data.ml.MakamRecognitionResult
import com.example.tmtuner.data.models.AhenkType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

data class LivePitchData(
    val frequency: Float,
    val noteName: String,
    val centsOffset: Int
)

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
    val isPlaying: StateFlow<Boolean> = _isDronePlaying.asStateFlow()

    private val _droneVolume = MutableStateFlow(0.5f)
    val droneVolume: StateFlow<Float> = _droneVolume.asStateFlow()

    private val _currentPitch = MutableStateFlow<LivePitchData?>(null)
    val currentPitch: StateFlow<LivePitchData?> = _currentPitch.asStateFlow()

    private val _recognitionResult = MutableStateFlow<MakamRecognitionResult?>(null)
    val recognitionResult: StateFlow<MakamRecognitionResult?> = _recognitionResult.asStateFlow()

    private val pitchHistogramEngine = PitchHistogram()
    private var pitchDetectionCount = 0
    private var currentPitchAtlas: List<PerdeNote> = emptyList()

    private val referenceTrainingVectors by lazy {
        MakamClassifierEngine.createDefaultReferenceVectors()
    }

    private var audioJob: Job? = null
    private var audioTrack: AudioTrack? = null
    private var recordingJob: Job? = null

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
        val coreAhenk = Ahenk.fromString(_selectedAhenk.value.name)
        val atlas = AeuScaleAtlas.buildPitchAtlas(coreAhenk)
        currentPitchAtlas = atlas
        currentTMNotes = atlas.map { Pair(it.frequency, it.name) }
    }

    fun getDroneNoteName(): String {
        val coreAhenk = Ahenk.fromString(_selectedAhenk.value.name)
        val (name, _) = AeuScaleAtlas.calculateDroneFrequency(_selectedMakam.value, coreAhenk)
        return name
    }

    private fun calculateTargetFrequency(): Double {
        val coreAhenk = Ahenk.fromString(_selectedAhenk.value.name)
        val (_, freq) = AeuScaleAtlas.calculateDroneFrequency(_selectedMakam.value, coreAhenk)
        return freq
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

    fun startMicrophoneAnalysis() {
        startListening()
    }

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
                                "Tenor / Soprano Saksafon (Bb)" -> TranspositionMode.BB_INSTRUMENTS.toTargetPitch(pitch.toDouble())
                                "Alto Saksafon (Eb)" -> TranspositionMode.EB_ALTO_SAX.toTargetPitch(pitch.toDouble())
                                else -> if (_isTransposed.value) TranspositionMode.BB_INSTRUMENTS.toTargetPitch(pitch.toDouble()) else pitch.toDouble()
                            }
                            _frequency.value = pitch

                            val match = PitchMatcher.matchPitch(
                                frequency = adjustedPitch,
                                atlas = currentPitchAtlas,
                                segahNuanceOffset = if (_applySegahNuance.value) -1 else 0
                            )
                            if (match != null) {
                                _detectedNote.value = match.matchedNote.name
                                _centsDifference.value = match.centsOffset
                                _detectedOctave.value = match.octaveIndex

                                _currentPitch.value = LivePitchData(
                                    frequency = pitch,
                                    noteName = match.matchedNote.name,
                                    centsOffset = match.centsOffset.roundToInt()
                                )

                                val coreAhenk = Ahenk.fromString(_selectedAhenk.value.name)
                                pitchHistogramEngine.addPitch(adjustedPitch, coreAhenk.getKabaCargahBaseFrequency())
                                pitchDetectionCount++
                                if (pitchDetectionCount % 3 == 0) {
                                    val result = MakamClassifierEngine.classify(
                                        pitchHistogramEngine.getNormalizedHistogram(),
                                        referenceTrainingVectors
                                    )
                                    _recognitionResult.value = MakamRecognitionResult(
                                        makamAdi = result.makamName,
                                        confidence = result.confidence,
                                        tespitEdilenCesni = result.detectedCesni
                                    )
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
