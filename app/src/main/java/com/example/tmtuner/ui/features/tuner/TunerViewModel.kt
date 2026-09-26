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
import com.example.tmtuner.core.musicology.analysis.FrequencyEstimator
import com.example.tmtuner.core.musicology.atlas.AeuScaleAtlas
import com.example.tmtuner.core.musicology.engine.MakamDetectionEngine
import com.example.tmtuner.core.musicology.engine.TranspositionEngine
import com.example.tmtuner.core.musicology.model.Ahenk
import com.example.tmtuner.core.musicology.model.NeyType
import com.example.tmtuner.core.musicology.model.TransposingInstrument
import kotlin.math.abs
import kotlin.math.log2
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
 * - 53-EDO Makam Tanıma ve Seyir Analiz Motoru (MakamDetectionEngine)
 */
class TunerViewModel @JvmOverloads constructor(
    val audioRecorder: IAudioRecorder = AudioRecorderManager(),
    val tunerEngine: MicrotonalTunerEngine = MicrotonalTunerEngine(),
    val dronePlayer: IDroneAudioPlayer = DroneAudioPlayer(),
    val makamEngine: MakamDetectionEngine = MakamDetectionEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TunerUiState())
    val uiState: StateFlow<TunerUiState> = _uiState.asStateFlow()

    private var recordingJob: Job? = null

    // Canlı akış histerezis ve kararlılık takibi
    private var lastObservedConcertFreq: Double = 0.0
    private var consecutiveStableFrames: Int = 0

    init {
        updateDroneTuning()
        viewModelScope.launch {
            makamEngine.detectionResult.collect { result ->
                _uiState.update { it.copy(makamDetectionState = result) }
            }
        }
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
            val state = _uiState.value
            val minMelodic = if (state.selectedAhenk == Ahenk.KIZ) 350.0 else FrequencyEstimator.DEFAULT_MIN_MELODIC_HZ
            val stabilizedFreq = FrequencyEstimator.disambiguateOctave(
                detectedFrequency = detection.frequency,
                minMelodicFreq = minMelodic,
                maxMelodicFreq = FrequencyEstimator.DEFAULT_MAX_MELODIC_HZ
            )

            // Histerezis ve Anlık Dip/Sıçrama Filtresi:
            // Ney icrasında nefes kaçması sonucu 1 pencerelik 270 Hz veya 1600+ Hz sıçramalarını filtrele.
            // Önceki frekans ile 3 koma (~%3.9) içinde tutarlı ise ardışık sayaç artar.
            val komaDiff = if (lastObservedConcertFreq > 20.0) {
                abs(53.0 * log2(stabilizedFreq / lastObservedConcertFreq))
            } else {
                0.0
            }

            if (komaDiff <= 3.0) {
                consecutiveStableFrames++
            } else {
                consecutiveStableFrames = 1
            }
            lastObservedConcertFreq = stabilizedFreq

            // UI kadranını hemen güncelle (akıcı ibre), ancak makam motoruna yalnızca kararlı tınlayan sesleri ilet
            val isStableForMakam = (consecutiveStableFrames >= 2 || detection.clarity > 0.88)
            processFrequency(
                concertFrequency = stabilizedFreq,
                clarity = detection.clarity,
                rmsEnergy = detection.rmsEnergy.toFloat(),
                feedToMakamEngine = isStableForMakam
            )
        } else {
            consecutiveStableFrames = 0
            lastObservedConcertFreq = 0.0
        }
    }

    /**
     * Frekans değerini doğrudan işler (Birim testler ve canlı akış için ortak giriş).
     *
     * @param concertFrequency Ortamda tınlayan / mikrofondan yakalanan akustik konsert frekansı (Hz)
     * @param clarity Algılama berraklığı (0.0 .. 1.0)
     * @param rmsEnergy Sinyal RMS enerji seviyesi (0.0f .. 1.0f)
     * @param feedToMakamEngine Makam tanıma motoru histogramına aktarılıp aktarılmayacağı
     */
    fun processFrequency(
        concertFrequency: Double,
        clarity: Double = 1.0,
        rmsEnergy: Float = 1.0f,
        feedToMakamEngine: Boolean = true
    ) {
        if (concertFrequency <= 0.0) return

        val state = _uiState.value
        val minMelodic = if (state.selectedAhenk == Ahenk.KIZ) 350.0 else FrequencyEstimator.DEFAULT_MIN_MELODIC_HZ
        val stabilizedConcertFreq = FrequencyEstimator.disambiguateOctave(
            detectedFrequency = concertFrequency,
            minMelodicFreq = minMelodic,
            maxMelodicFreq = FrequencyEstimator.DEFAULT_MAX_MELODIC_HZ
        )

        // Enstrüman transpozisyonu: Konsert tınısından icracının bastığı yazılı perdeye dönüşüm
        val writtenPitch = TranspositionEngine.toInstrumentPitch(
            concertPitch = stabilizedConcertFreq,
            instrument = state.selectedInstrument
        )

        // 53-EDO perde atlasında analiz (İcracının bastığı enstrüman/yazılı perdesi)
        val result = tunerEngine.analyzePitch(
            detectedFrequency = writtenPitch,
            clarity = clarity,
            ahenk = state.selectedAhenk,
            usePhysicalMansur = state.usePhysicalMansur,
            segahMode = state.segahMode
        )

        // Makam Analiz Motoru için Konsert / Asıl Perde analizi
        val concertResult = if (state.selectedInstrument == TransposingInstrument.CONCERT_C) {
            result
        } else {
            tunerEngine.analyzePitch(
                detectedFrequency = stabilizedConcertFreq,
                clarity = clarity,
                ahenk = state.selectedAhenk,
                usePhysicalMansur = state.usePhysicalMansur,
                segahMode = state.segahMode
            )
        }

        if (concertResult != null && feedToMakamEngine) {
            // Saksafon/Ney transpozisyonunda MakamDetectionEngine'e daima asıl Konser Perdesi gönderilir.
            makamEngine.feedPitch(
                perdeName = concertResult.perdeName,
                komaOffset = concertResult.komaDifference,
                durationMs = 100L,
                rmsEnergy = rmsEnergy
            )
        }

        if (result != null) {
            val isSegah = isSegahPerde(result.perdeName)
            val isSegahNuanceTriggered = isSegah && state.segahMode != SegahNuanceMode.NONE

            _uiState.update {
                it.copy(
                    detectedFrequency = writtenPitch,
                    concertFrequency = stabilizedConcertFreq,
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
        makamEngine.ahenk = ahenk
        updateDroneTuning()
        reAnalyzeCurrentPitch()
    }

    fun setInstrument(instrument: TransposingInstrument) {
        _uiState.update { it.copy(selectedInstrument = instrument) }
        reAnalyzeCurrentPitch()
    }

    fun setNeyType(neyType: NeyType, syncAhenk: Boolean = true) {
        val newAhenk = if (syncAhenk) neyType.ahenk else _uiState.value.selectedAhenk
        makamEngine.ahenk = newAhenk
        _uiState.update {
            it.copy(
                selectedNeyType = neyType,
                selectedAhenk = newAhenk
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

    /**
     * Makam tanıma motorunu ve UI durumunu sıfırlar.
     */
    fun resetMakamDetection() {
        makamEngine.reset()
        _uiState.update { it.copy(makamDetectionState = null) }
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
        dronePlayer.release()
    }
}

/**
 * Görev ve mimari uyumluluğu için tür takma adı (typealias).
 */
typealias MainTunerViewModel = TunerViewModel
