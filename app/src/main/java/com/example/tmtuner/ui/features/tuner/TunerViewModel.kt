package com.example.tmtuner.ui.features.tuner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tmtuner.core.audio.drone.AcousticDroneProfile
import com.example.tmtuner.core.audio.drone.DroneAudioPlayer
import com.example.tmtuner.core.audio.drone.IDroneAudioPlayer
import com.example.tmtuner.core.audio.engine.MicrotonalTunerEngine
import com.example.tmtuner.core.audio.model.SegahNuanceMode
import com.example.tmtuner.core.audio.recorder.AudioRecorderManager
import com.example.tmtuner.core.audio.recorder.IAudioRecorder
import com.example.tmtuner.core.musicology.atlas.AeuScaleAtlas
import com.example.tmtuner.core.musicology.engine.TranspositionEngine
import com.example.tmtuner.core.musicology.model.Ahenk
import com.example.tmtuner.core.musicology.model.NeyType
import com.example.tmtuner.core.musicology.model.TransposingInstrument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Türk Müziği 53-EDO Mikrotonal Akort Cihazı ViewModel katmanı.
 *
 * İsmail Hakkı Özkan nazariyatına tam uyumlu:
 * - Mansur (Kaba Çârgâh 256 Hz / Dügâh 440 Hz)
 * - Bolâhenk (Nevâ 440 Hz / Dügâh ≈ 330 Hz, -22 koma)
 * - Kız Âhengi (+22 koma, 4/3 oranı)
 * - Ney Çeşitleri & Transpoze Batı Enstrümanları (Eb Alto Sax 16/27, Bb Tenor Sax 8/9)
 * - Özkan s. 51 gereği Segâh İcra Toleransı (-1.0 / -2.0 koma)
 * - Akustik Referans Sentezleyici (Tanbûra, Ney, Saf Sinüs Karar & Güçlü Dem Sesi)
 */
class TunerViewModel @JvmOverloads constructor(
    val audioRecorder: IAudioRecorder = AudioRecorderManager(),
    val tunerEngine: MicrotonalTunerEngine = MicrotonalTunerEngine(),
    val dronePlayer: IDroneAudioPlayer = DroneAudioPlayer()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TunerUiState())
    val uiState: StateFlow<TunerUiState> = _uiState.asStateFlow()

    private var recordingJob: Job? = null

    init {
        updateDroneTuning()
    }

    fun toggleListening() {
        if (_uiState.value.isRecording) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun startListening() {
        if (_uiState.value.isRecording) return

        recordingJob?.cancel()
        recordingJob = viewModelScope.launch(Dispatchers.Default) {
            try {
                _uiState.update { it.copy(isRecording = true, statusMessage = "Mikrofon dinleniyor...") }
                audioRecorder.startRecording().collect { buffer ->
                    processAudioBuffer(buffer, audioRecorder.sampleRate)
                }
            } catch (se: SecurityException) {
                _uiState.update {
                    it.copy(
                        isRecording = false,
                        statusMessage = "Mikrofon izni verilmedi! Lütfen izinleri kontrol edin."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRecording = false,
                        statusMessage = "Ses yakalama hatası: ${e.message}"
                    )
                }
            } finally {
                _uiState.update { it.copy(isRecording = false) }
            }
        }
    }

    fun stopListening() {
        audioRecorder.stopRecording()
        recordingJob?.cancel()
        recordingJob = null
        _uiState.update {
            it.copy(
                isRecording = false,
                statusMessage = "Mikrofon durduruldu"
            )
        }
    }

    /**
     * Ham 16-bit PCM ses tamponunu işler ve perdeyi analiz eder.
     */
    fun processAudioBuffer(buffer: ShortArray, sampleRate: Int) {
        val detection = tunerEngine.pitchDetector.detectPitch(buffer, sampleRate)
        val normalizedRms = (detection.rmsEnergy * 6.0).coerceIn(0.0, 1.0).toFloat()
        
        _uiState.update { it.copy(rmsLevel = normalizedRms) }

        if (detection.isPitched && detection.frequency > 20.0) {
            processFrequency(detection.frequency, detection.clarity)
        }
    }

    /**
     * Frekans değerini doğrudan işler (Birim testler ve canlı akış için ortak giriş).
     *
     * @param concertFrequency Ortamda tınlayan / mikrofondan yakalanan akustik konsert frekansı (Hz)
     * @param clarity Algılama berraklığı (0.0 .. 1.0)
     */
    fun processFrequency(concertFrequency: Double, clarity: Double = 1.0) {
        if (concertFrequency <= 0.0) return

        val state = _uiState.value

        // Enstrüman transpozisyonu: Konsert tınısından icracının bastığı yazılı perdeye dönüşüm
        val writtenPitch = TranspositionEngine.toInstrumentPitch(
            concertPitch = concertFrequency,
            instrument = state.selectedInstrument
        )

        // 53-EDO perde atlasında analiz
        val result = tunerEngine.analyzePitch(
            detectedFrequency = writtenPitch,
            clarity = clarity,
            ahenk = state.selectedAhenk,
            usePhysicalMansur = state.usePhysicalMansur,
            segahMode = state.segahMode
        )

        if (result != null) {
            val isSegah = isSegahPerde(result.perdeName)
            val isSegahNuanceTriggered = isSegah && state.segahMode != SegahNuanceMode.NONE

            _uiState.update {
                it.copy(
                    detectedFrequency = writtenPitch,
                    concertFrequency = concertFrequency,
                    targetFrequency = result.targetFrequency,
                    perdeName = result.perdeName,
                    octaveName = result.octaveName,
                    mutlakKoma = result.mutlakKoma,
                    komaDifference = result.komaDifference,
                    centsDifference = result.centsDifference,
                    isTuned = result.isTuned,
                    clarity = clarity,
                    isSegahNuanceActive = isSegahNuanceTriggered,
                    statusMessage = if (result.isTuned) "Tam Akortlu" else "Akort Bekleniyor"
                )
            }
        }
    }

    fun setAhenk(ahenk: Ahenk) {
        _uiState.update { it.copy(selectedAhenk = ahenk) }
        updateDroneTuning()
        reAnalyzeCurrentPitch()
    }

    fun setInstrument(instrument: TransposingInstrument) {
        _uiState.update { it.copy(selectedInstrument = instrument) }
        reAnalyzeCurrentPitch()
    }

    fun setNeyType(neyType: NeyType, syncAhenk: Boolean = true) {
        _uiState.update {
            it.copy(
                selectedNeyType = neyType,
                selectedAhenk = if (syncAhenk) neyType.ahenk else it.selectedAhenk
            )
        }
        updateDroneTuning()
        reAnalyzeCurrentPitch()
    }

    fun setSegahMode(mode: SegahNuanceMode) {
        _uiState.update { it.copy(segahMode = mode) }
        reAnalyzeCurrentPitch()
    }

    fun setUsePhysicalMansur(usePhysical: Boolean) {
        _uiState.update { it.copy(usePhysicalMansur = usePhysical) }
        reAnalyzeCurrentPitch()
    }

    // --- Akustik Referans Sentezleyici (Drone / Dem Sesi) Yönetimi ---

    fun toggleDrone() {
        if (_uiState.value.isDronePlaying) {
            stopDrone()
        } else {
            startDrone()
        }
    }

    fun startDrone() {
        val state = _uiState.value
        dronePlayer.start(state.droneTonicFrequency, state.droneDominantFrequency)
        _uiState.update { it.copy(isDronePlaying = true) }
    }

    fun stopDrone() {
        dronePlayer.stop()
        _uiState.update { it.copy(isDronePlaying = false) }
    }

    fun setDroneVolume(volume: Float) {
        dronePlayer.setVolume(volume)
        _uiState.update { it.copy(droneVolume = volume) }
    }

    fun setDroneProfile(profile: AcousticDroneProfile) {
        dronePlayer.setProfile(profile)
        _uiState.update { it.copy(droneProfile = profile) }
    }

    fun setDualDrone(enabled: Boolean) {
        dronePlayer.setDualDrone(enabled)
        _uiState.update { it.copy(isDualDrone = enabled) }
    }

    fun setMakam(makamName: String) {
        _uiState.update { it.copy(selectedMakam = makamName) }
        updateDroneTuning()
    }

    private fun updateDroneTuning() {
        val state = _uiState.value
        val tuning = AeuScaleAtlas.calculateMakamDroneTuning(state.selectedMakam, state.selectedAhenk)
        _uiState.update {
            it.copy(
                droneTonicNoteName = tuning.tonicPerdeName,
                droneTonicFrequency = tuning.tonicFrequency,
                droneDominantNoteName = tuning.dominantPerdeName,
                droneDominantFrequency = tuning.dominantFrequency
            )
        }
        dronePlayer.updateFrequencies(tuning.tonicFrequency, tuning.dominantFrequency)
    }

    private fun reAnalyzeCurrentPitch() {
        val currentConcert = _uiState.value.concertFrequency
        if (currentConcert > 20.0) {
            processFrequency(currentConcert, _uiState.value.clarity)
        }
    }

    private fun isSegahPerde(name: String): Boolean {
        return name.contains("Segâh", ignoreCase = true) || name.contains("Segah", ignoreCase = true)
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
        dronePlayer.release()
    }
}
