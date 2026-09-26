package com.example.tmtuner

import com.example.tmtuner.core.audio.drone.AcousticDroneProfile
import com.example.tmtuner.core.audio.drone.IDroneAudioPlayer
import com.example.tmtuner.core.audio.engine.MicrotonalTunerEngine
import com.example.tmtuner.core.audio.model.SegahNuanceMode
import com.example.tmtuner.core.audio.recorder.IAudioRecorder
import com.example.tmtuner.core.musicology.atlas.AeuScaleAtlas
import com.example.tmtuner.core.musicology.model.Ahenk
import com.example.tmtuner.core.musicology.model.NeyType
import com.example.tmtuner.core.musicology.model.TransposingInstrument
import com.example.tmtuner.ui.features.tuner.TunerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

/**
 * [TunerViewModel] için kapsamlı birim test paketi.
 *
 * Test Edilenler:
 * 1. Mansur Dügâh (440 Hz) akort doğrulaması
 * 2. Bolâhenk Dügâh (330 Hz / 329.63 Hz) akort doğrulaması
 * 3. Mansur Fizik Düzeni Kaba Çârgâh (256 Hz) doğrulaması
 * 4. Eb Alto Saksafon (16/27 ve 27/16 çarpanı) transpozisyon doğrulaması
 * 5. Bb Tenor Saksafon (8/9 ve 9/8 çarpanı) transpozisyon doğrulaması
 * 6. Özkan s. 51 Segâh İcra Nüansı (-1/-2 koma) ve Kehribar (Amber) rozet tetiklemesi
 * 7. Ney çeşitleri seçildiğinde Ahenk senkronizasyonu
 * 8. Akustik Drone / Dem Sesi oynatma, makam ve çift dem yönetimi
 * 9. MakamDetectionEngine entegrasyonu ve UI State reaktif akışı
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TunerViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRecorder: FakeAudioRecorder
    private lateinit var fakeDronePlayer: FakeDroneAudioPlayer
    private lateinit var tunerEngine: MicrotonalTunerEngine
    private lateinit var viewModel: TunerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRecorder = FakeAudioRecorder()
        fakeDronePlayer = FakeDroneAudioPlayer()
        tunerEngine = MicrotonalTunerEngine()
        viewModel = TunerViewModel(
            audioRecorder = fakeRecorder,
            tunerEngine = tunerEngine,
            dronePlayer = fakeDronePlayer
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testMakamDetectionIntegration() {
        viewModel.setAhenk(Ahenk.BOLAHENK)
        viewModel.setInstrument(TransposingInstrument.CONCERT_C)

        // Bolâhenk Dügâh = 330.0 Hz
        viewModel.processFrequency(330.0)
        val stateAfterDugah = viewModel.uiState.value
        assertEquals("Dügâh", stateAfterDugah.perdeName)
        
        val makamState = stateAfterDugah.makamDetectionState
        assertTrue("Makam tespit durumu güncellenmiş olmalıdır", makamState != null)
        assertTrue("Histogram Dügâh perdesini içermelidir", makamState!!.pitchHistogram.containsKey("Dügâh"))
        
        // Sıfırlama testi
        viewModel.resetMakamDetection()
        val stateAfterReset = viewModel.uiState.value
        assertEquals("Sıfırlama sonrası makam tespit durumu null olmalıdır", null, stateAfterReset.makamDetectionState)
    }

    @Test
    fun testTransposedInstrumentMakamAnalysis() {
        viewModel.setAhenk(Ahenk.MANSUR)
        viewModel.setInstrument(TransposingInstrument.EB_ALTO_SAX)

        // Eb Alto Saksafoncu notada 'Do / Çârgâh' (Mansur 521.48 Hz) bastığında:
        // Akustik ortamda duyulan konsert sesi: 521.4815 * (16/27) ≈ 309.026 Hz (Kaba Dik Hisâr / Eb).
        val writtenCargahFreq = 260.7407 * 2.0 // Çârgâh = 521.4814 Hz
        val acousticConcertFreq = writtenCargahFreq * (16.0 / 27.0) // ≈ 309.026 Hz
        viewModel.processFrequency(acousticConcertFreq)

        val state = viewModel.uiState.value
        // 1. İcracının ekranında transpoze enstrüman perdesi (Çârgâh / Do) görünür
        assertEquals("İcracının ekranında transpoze yazılı perde Çârgâh görünmelidir", "Çârgâh", state.perdeName)
        assertEquals(acousticConcertFreq, state.concertFrequency, 0.01)

        // 2. MakamDetectionEngine'e transpozisyon kaynaklı makam kayması olmaması için
        // asıl akustik konser perdesi (Kaba Dik Hisâr) beslenmelidir.
        val makamState = state.makamDetectionState
        assertTrue("Makam tespit durumu oluşturulmalıdır", makamState != null)
        assertTrue(
            "Makam motoru histogramı konsert perdesini (Kaba Dik Hisâr) içermelidir",
            makamState!!.pitchHistogram.keys.any { it.contains("Hisâr", ignoreCase = true) }
        )

        // 3. Ortamda Konser Dügâh (440.0 Hz) tınladığında:
        // Saksafoncu transpoze olarak Mahur (F#) görür, ama makam motoru Dügâh perdesini alır!
        viewModel.processFrequency(440.0)
        val stateAfterConcertDugah = viewModel.uiState.value
        assertEquals("Mahur", stateAfterConcertDugah.perdeName)
        assertTrue(
            "Makam motoru konsert Dügâh perdesini doğru kaydetmelidir",
            stateAfterConcertDugah.makamDetectionState!!.pitchHistogram.containsKey("Dügâh")
        )
    }

    @Test
    fun testMansurDugahTuning() {
        viewModel.setAhenk(Ahenk.MANSUR)
        viewModel.setInstrument(TransposingInstrument.CONCERT_C)

        // 440.0 Hz = Mansur Dügâh (Diyapazon)
        viewModel.processFrequency(440.0)

        val state = viewModel.uiState.value
        assertEquals("Dügâh", state.perdeName)
        assertTrue("Koma farkı 0.1'den küçük olmalıdır", abs(state.komaDifference) < 0.1)
        assertTrue("440 Hz Mansur Dügâh tam akortlu olmalıdır", state.isTuned)
        assertEquals(440.0, state.detectedFrequency, 0.001)
        assertEquals(440.0, state.concertFrequency, 0.001)
    }

    @Test
    fun testBolahenkDugahTuning() {
        viewModel.setAhenk(Ahenk.BOLAHENK)
        viewModel.setInstrument(TransposingInstrument.CONCERT_C)

        // Bolâhenk Dügâh = 330.0 Hz (Nevâ = 440 Hz'e göre Tam Dörtlü pes)
        viewModel.processFrequency(330.0)

        val state = viewModel.uiState.value
        assertEquals("Dügâh", state.perdeName)
        assertTrue("Koma farkı 0.1'den küçük olmalıdır", abs(state.komaDifference) < 0.1)
        assertTrue("330 Hz Bolâhenk Dügâh tam akortlu olmalıdır", state.isTuned)
    }

    @Test
    fun testKabaCargahPhysicalMansur() {
        viewModel.setAhenk(Ahenk.MANSUR)
        viewModel.setUsePhysicalMansur(true)

        // Fizik Mansur: Kaba Çârgâh = 256.0 Hz
        viewModel.processFrequency(256.0)

        val state = viewModel.uiState.value
        assertEquals("Kaba Çârgâh", state.perdeName)
        assertTrue("Fizik Çârgâh koma farkı 0.1'den küçük olmalı", abs(state.komaDifference) < 0.1)
        assertTrue("256 Hz Kaba Çârgâh tam akortlu olmalıdır", state.isTuned)
    }

    @Test
    fun testEbAltoSaxophoneTransposition() {
        viewModel.setAhenk(Ahenk.MANSUR)
        viewModel.setInstrument(TransposingInstrument.EB_ALTO_SAX)

        // Eb Alto Saksafoncu notada 'Dügâh' (440.0 Hz) okuyup bastığında;
        // Akustik ortamda büyük 6'lı pest konsert sesi tınlar: 440.0 * (16/27) ≈ 260.7407 Hz.
        val acousticConcertFreq = 440.0 * (16.0 / 27.0)
        viewModel.processFrequency(acousticConcertFreq)

        val state = viewModel.uiState.value
        assertEquals("İcracının çaldığı nota Dügâh olarak tanımlanmalıdır", "Dügâh", state.perdeName)
        assertEquals("Duyulan konsert frekansı doğru saklanmalıdır", acousticConcertFreq, state.concertFrequency, 0.01)
        assertEquals("Yazılı perde frekansı 440 Hz'e dönüştürülmelidir", 440.0, state.detectedFrequency, 0.05)
        assertTrue("Alto saksafon transpozesi ile tam akortlu olmalıdır", state.isTuned)
    }

    @Test
    fun testBbTenorSaxophoneTransposition() {
        viewModel.setAhenk(Ahenk.MANSUR)
        viewModel.setInstrument(TransposingInstrument.BB_TENOR_SAX)

        // Bb Tenor Saksafoncu notada 'Dügâh' (440.0 Hz) bastığında;
        // Akustik ortamda büyük 2'li (Tanini) pest tınlar: 440.0 * (8/9) ≈ 391.1111 Hz.
        val acousticConcertFreq = 440.0 * (8.0 / 9.0)
        viewModel.processFrequency(acousticConcertFreq)

        val state = viewModel.uiState.value
        assertEquals("İcracının çaldığı nota Dügâh olarak tanımlanmalıdır", "Dügâh", state.perdeName)
        assertEquals("Yazılı perde frekansı 440 Hz olmalıdır", 440.0, state.detectedFrequency, 0.05)
        assertTrue("Tenor saksafon transpozesi ile tam akortlu olmalıdır", state.isTuned)
    }

    @Test
    fun testSegahPerformanceNuanceAndAmberActivation() {
        viewModel.setAhenk(Ahenk.MANSUR)
        viewModel.setInstrument(TransposingInstrument.CONCERT_C)

        // Mansur perde atlasından teorik Segâh frekansını bulalım
        val atlas = AeuScaleAtlas.buildPitchAtlas(Ahenk.MANSUR, usePhysicalMansur = false)
        val segahPerde = atlas.first { it.name.contains("Segâh") || it.name.contains("Segah") }
        val theoreticalSegahFreq = segahPerde.frequency

        // 1. Durum: TOLERANT_RANGE modunda 1.5 koma pest icra (-1.5 koma)
        viewModel.setSegahMode(SegahNuanceMode.TOLERANT_RANGE)
        val nuanceFreq = tunerEngine.shiftFrequencyByKoma(theoreticalSegahFreq, -1.5)

        viewModel.processFrequency(nuanceFreq)
        val stateWithNuance = viewModel.uiState.value

        assertTrue("Perde Segâh olmalıdır", stateWithNuance.perdeName.contains("Segâh"))
        assertTrue("Segâh icra nüansı aktif olmalıdır (Kehribar rozet tetiklenmeli)", stateWithNuance.isSegahNuanceActive)
        assertTrue("Özkan s. 51 esnek tolerans aralığında akortlu kabul edilmelidir", stateWithNuance.isTuned)

        // 2. Durum: Nüans modu kapatıldığında (NONE) aynı -1.5 koma sesi
        viewModel.setSegahMode(SegahNuanceMode.NONE)
        viewModel.processFrequency(nuanceFreq)
        val stateWithoutNuance = viewModel.uiState.value

        assertFalse("Nüans modu NONE iken rozet kapalı olmalıdır", stateWithoutNuance.isSegahNuanceActive)
        assertFalse("-1.5 koma fark teorik tolerans dışı olduğundan akortsus sayılmalıdır", stateWithoutNuance.isTuned)
    }

    @Test
    fun testNeySelectionSyncsAhenk() {
        // Kız Ney seçildiğinde Ahenk KIZ olmalı
        viewModel.setNeyType(NeyType.KIZ)
        assertEquals(Ahenk.KIZ, viewModel.uiState.value.selectedAhenk)
        assertEquals(NeyType.KIZ, viewModel.uiState.value.selectedNeyType)

        // Bolâhenk Ney seçildiğinde Ahenk BOLAHENK olmalı
        viewModel.setNeyType(NeyType.BOLAHENK)
        assertEquals(Ahenk.BOLAHENK, viewModel.uiState.value.selectedAhenk)

        // Mansur Ney seçildiğinde Ahenk MANSUR olmalı
        viewModel.setNeyType(NeyType.MANSUR)
        assertEquals(Ahenk.MANSUR, viewModel.uiState.value.selectedAhenk)
    }

    @Test
    fun testDroneToggleAndPlayback() {
        assertFalse(viewModel.uiState.value.isDronePlaying)

        // Dem başlat
        viewModel.toggleDrone()
        assertTrue(viewModel.uiState.value.isDronePlaying)
        assertTrue(fakeDronePlayer.isPlaying.value)

        // Ses seviyesi ayarla
        viewModel.setDroneVolume(0.75f)
        assertEquals(0.75f, viewModel.uiState.value.droneVolume, 0.001f)
        assertEquals(0.75f, fakeDronePlayer.volume.value, 0.001f)

        // Dem durdur
        viewModel.toggleDrone()
        assertFalse(viewModel.uiState.value.isDronePlaying)
        assertFalse(fakeDronePlayer.isPlaying.value)
    }

    @Test
    fun testDroneMakamChangeUpdatesFrequencies() {
        viewModel.setAhenk(Ahenk.MANSUR)

        // Rast Makamı: Karar Rast perdesi (~293.33 Hz), Güçlü Neva perdesi (~440.0 Hz)
        viewModel.setMakam("Rast")
        assertEquals("Râst (Sol)", viewModel.uiState.value.droneTonicNoteName)
        assertEquals(293.33, viewModel.uiState.value.droneTonicFrequency, 0.5)
        assertEquals(440.0, viewModel.uiState.value.droneDominantFrequency, 0.5)

        // Uşşak Makamı: Karar Dügâh perdesi (330.0 Hz)
        viewModel.setMakam("Uşşak")
        assertEquals("Dügâh (La)", viewModel.uiState.value.droneTonicNoteName)
        assertEquals(330.0, viewModel.uiState.value.droneTonicFrequency, 0.5)

        // Segâh Makamı: Karar Segâh perdesi (~330.0 * 4096/3888...)
        viewModel.setMakam("Segâh")
        assertEquals("Segâh (Si♭₁)", viewModel.uiState.value.droneTonicNoteName)
        assertTrue(viewModel.uiState.value.droneTonicFrequency > 0.0)
    }

    @Test
    fun testDroneProfileAndDualDroneState() {
        // Tını profili değiştir
        viewModel.setDroneProfile(AcousticDroneProfile.NEY)
        assertEquals(AcousticDroneProfile.NEY, viewModel.uiState.value.droneProfile)
        assertEquals(AcousticDroneProfile.NEY, fakeDronePlayer.profile.value)

        viewModel.setDroneProfile(AcousticDroneProfile.PURE_SINE)
        assertEquals(AcousticDroneProfile.PURE_SINE, viewModel.uiState.value.droneProfile)

        // Çift Dem modu aç/kapa
        viewModel.setDualDrone(true)
        assertTrue(viewModel.uiState.value.isDualDrone)
        assertTrue(fakeDronePlayer.isDualDrone.value)

        viewModel.setDualDrone(false)
        assertFalse(viewModel.uiState.value.isDualDrone)
        assertFalse(fakeDronePlayer.isDualDrone.value)
    }

    /**
     * Test amaçlı sahte (Fake) ses kayıt yöneticisi.
     */
    private class FakeAudioRecorder(
        override val sampleRate: Int = 44100,
        override val frameSize: Int = 2048
    ) : IAudioRecorder {
        private val _isRecording = MutableStateFlow(false)
        override val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

        private val audioFlow = MutableSharedFlow<ShortArray>()

        suspend fun emitBuffer(buffer: ShortArray) {
            audioFlow.emit(buffer)
        }

        override fun startRecording(): Flow<ShortArray> {
            _isRecording.value = true
            return audioFlow
        }

        override fun stopRecording() {
            _isRecording.value = false
        }
    }

    /**
     * Test amaçlı sahte (Fake) drone yürütme motoru.
     */
    private class FakeDroneAudioPlayer : IDroneAudioPlayer {
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

        override fun start(tonicFrequency: Double, dominantFrequency: Double?) {
            _isPlaying.value = true
            updateFrequencies(tonicFrequency, dominantFrequency)
        }

        override fun stop() {
            _isPlaying.value = false
        }

        override fun updateFrequencies(tonicFrequency: Double, dominantFrequency: Double?) {
            _currentTonicFrequency.value = tonicFrequency
            _currentDominantFrequency.value = dominantFrequency ?: (tonicFrequency * 1.5)
        }

        override fun setVolume(volume: Float) {
            _volume.value = volume
        }

        override fun setProfile(profile: AcousticDroneProfile) {
            _profile.value = profile
        }

        override fun setDualDrone(enabled: Boolean) {
            _isDualDrone.value = enabled
        }

        override fun release() {
            _isPlaying.value = false
        }
    }
}
