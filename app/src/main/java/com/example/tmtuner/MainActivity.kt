package com.example.tmtuner

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
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
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.cos
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

class TMTunerViewModel : ViewModel() {
    private val _frequency = MutableStateFlow(0.0f)
    val frequency: StateFlow<Float> = _frequency.asStateFlow()

    private val _detectedNote = MutableStateFlow("Dinleniyor...")
    val detectedNote: StateFlow<String> = _detectedNote.asStateFlow()

    private val _detectedOctave = MutableStateFlow(1)
    val detectedOctave: StateFlow<Int> = _detectedOctave.asStateFlow()

    private val _centsDifference = MutableStateFlow(0f)
    val centsDifference: StateFlow<Float> = _centsDifference.asStateFlow()

    private val _selectedAhenk = MutableStateFlow("Mansur (La=440)")
    val selectedAhenk: StateFlow<String> = _selectedAhenk.asStateFlow()

    private val _selectedMakam = MutableStateFlow("Rast")
    val selectedMakam: StateFlow<String> = _selectedMakam.asStateFlow()

    private val _isTransposed = MutableStateFlow(false)
    val isTransposed: StateFlow<Boolean> = _isTransposed.asStateFlow()

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

    fun updateAhenk(newAhenk: String) { _selectedAhenk.value = newAhenk; updateTMNotesMap(); restartAudioIfPlaying() }
    fun updateMakam(newMakam: String) { _selectedMakam.value = newMakam; restartAudioIfPlaying() }
    fun toggleTranspose() { _isTransposed.value = !_isTransposed.value }
    fun setDroneVolume(vol: Float) { _droneVolume.value = vol }
    fun toggleDrone() { _isDronePlaying.value = !_isDronePlaying.value; if (_isDronePlaying.value) startAudio() else stopAudio() }
    private fun restartAudioIfPlaying() { if (_isDronePlaying.value) { stopAudio(); startAudio() } }

    private fun updateTMNotesMap() {
        val baseLa = when (_selectedAhenk.value) {
            "Mansur (La=440)" -> 440.0
            "Kız (La=415)" -> 415.0
            "Bolahenk (La=586)" -> 586.0
            "Süpürde (La=523)" -> 523.0
            else -> 440.0
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
            "Rast", "Nihavend" -> "Rast Perdesi (Sol)"
            "Uşşak", "Hüseyni", "Hicaz" -> "Dügâh Perdesi (La)"
            else -> "Karar Sesi"
        }
    }

    private fun calculateTargetFrequency(): Double {
        val baseLa = when (_selectedAhenk.value) { "Mansur (La=440)" -> 440.0; "Kız (La=415)" -> 415.0; "Bolahenk (La=586)" -> 586.0; "Süpürde (La=523)" -> 523.0; else -> 440.0 }
        return when (_selectedMakam.value) { "Rast", "Nihavend" -> baseLa * 0.89089; else -> baseLa }
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
                            val adjustedPitch = if (_isTransposed.value) pitch * (9.0/8.0) else pitch.toDouble()
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

@Composable
fun AppNavigation(viewModel: TMTunerViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf("Tuner") }
    val context = LocalContext.current
    var hasMicPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) }

    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(), onResult = { isGranted ->
        hasMicPermission = isGranted; if (isGranted) viewModel.startListening()
    })

    LaunchedEffect(Unit) { if (!hasMicPermission) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) else viewModel.startListening() }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(icon = { Icon(Icons.Default.Home, contentDescription = "Akort") }, label = { Text("Akort") }, selected = currentScreen == "Tuner", onClick = { currentScreen = "Tuner" })
                NavigationBarItem(icon = { Icon(Icons.Default.Build, contentDescription = "Ahenk") }, label = { Text("Ahenk") }, selected = currentScreen == "Makam", onClick = { currentScreen = "Makam" })
                NavigationBarItem(icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Dem Ses") }, label = { Text("Dem Sesi") }, selected = currentScreen == "Drone", onClick = { currentScreen = "Drone" })
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (!hasMicPermission && currentScreen == "Tuner") {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("Akort için mikrofon izni gereklidir.", color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }) { Text("İzin Ver") }
                }
            } else {
                when (currentScreen) { "Tuner" -> TunerScreen(viewModel); "Makam" -> MakamScreen(viewModel); "Drone" -> DroneScreen(viewModel) }
            }
        }
    }
}

@Composable
fun OctaveSegment(title: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp)) {
        Box(
            modifier = Modifier
                .height(4.dp)
                .fillMaxWidth()
                .background(if (isActive) MaterialTheme.colorScheme.primary else Color.DarkGray)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(title, fontSize = 12.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal, color = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray)
    }
}

@Composable
fun TunerScreen(viewModel: TMTunerViewModel) {
    val currentFreq by viewModel.frequency.collectAsState()
    val detectedNote by viewModel.detectedNote.collectAsState()
    val detectedOctave by viewModel.detectedOctave.collectAsState()
    val centsDiff by viewModel.centsDifference.collectAsState()

    val ahenk by viewModel.selectedAhenk.collectAsState()
    val makam by viewModel.selectedMakam.collectAsState()
    val isTransposed by viewModel.isTransposed.collectAsState()

    var isProMode by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {

        Card(modifier = Modifier.wrapContentWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Klasik (Hızlı)", fontSize = 14.sp, color = if (!isProMode) MaterialTheme.colorScheme.primary else Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(checked = isProMode, onCheckedChange = { isProMode = it })
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pro (Akışkan)", fontSize = 14.sp, color = if (isProMode) MaterialTheme.colorScheme.primary else Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            OctaveSegment("Pes / Kaba", isActive = currentFreq > 0 && detectedOctave == 0)
            OctaveSegment("Orta / Ana", isActive = currentFreq > 0 && detectedOctave == 1)
            OctaveSegment("Tîz / En Tîz", isActive = currentFreq > 0 && detectedOctave == 2)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = detectedNote, fontSize = 42.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        val displayFreq = if (currentFreq > 0) String.format(Locale.US, "%.1f", currentFreq) else "--"
        Text(text = "$displayFreq Hz", fontSize = 24.sp, color = Color.Gray)

        // SADELEŞTİRİLMİŞ TAM SAYI KOMA VE "KOMA DEĞERİ" EKRANI
        if (currentFreq > 0) {
            val rawKomaDiff = centsDiff / (1200f / 53f)
            val roundedKoma = rawKomaDiff.roundToInt() // Küsuratları atıp en yakın tam komaya yuvarlar

            val komaText = when {
                roundedKoma == 0 -> "Tam İsabet"
                roundedKoma > 0 -> "+$roundedKoma Koma (Dik)"
                else -> "${abs(roundedKoma)} Koma (Pes)"
            }
            val komaColor = if (roundedKoma == 0) Color.Green else if (roundedKoma > 0) Color.Yellow else Color(0xFFFFA500)

            Text(text = "Koma Değeri: $komaText", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = komaColor, modifier = Modifier.padding(top = 8.dp))
        }

        if (isTransposed) {
            Text(text = "(Bb Transpoze Açık)", color = Color.Yellow, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(48.dp))

        // GÖRSEL KADRAN: 9 Koma (Tam Ses) üzerinden hesaplanır. -4.5 ile +4.5 arası.
        val rawKomaForNeedle = centsDiff / (1200f / 53f)
        val targetNeedleOffset = rawKomaForNeedle.coerceIn(-4.5f, 4.5f)

        val animatedNeedleOffset by animateFloatAsState(
            targetValue = if (currentFreq > 0) targetNeedleOffset else 0f,
            animationSpec = tween(durationMillis = if (isProMode) 400 else 0, easing = LinearOutSlowInEasing), label = "Needle Animation"
        )

        val activeOffset = if (isProMode) animatedNeedleOffset else targetNeedleOffset
        val needleBaseColor = MaterialTheme.colorScheme.onSurface

        Canvas(modifier = Modifier.size(300.dp).padding(16.dp)) {
            val arcCenter = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f

            drawArc(color = Color.DarkGray, startAngle = 210f, sweepAngle = 120f, useCenter = false, style = Stroke(width = 30f, cap = StrokeCap.Round))
            drawLine(color = Color.LightGray, start = Offset(arcCenter.x, arcCenter.y - radius + 15f), end = Offset(arcCenter.x, arcCenter.y - radius - 20f), strokeWidth = 5f, cap = StrokeCap.Round)

            if (currentFreq > 0) {
                // -4.5 Komada kadranın en soluna (210 derece), +4.5 Komada en sağına (330 derece) gider.
                val needleAngle = 270f + (activeOffset / 4.5f) * 60f
                val needleColor = if (abs(targetNeedleOffset) < 0.5f) Color.Green else Color.Red
                val angleRad = (needleAngle * PI / 180f).toFloat()

                val startX = arcCenter.x
                val startY = arcCenter.y
                val endX = arcCenter.x + (radius * 0.9f) * cos(angleRad)
                val endY = arcCenter.y + (radius * 0.9f) * sin(angleRad)

                drawLine(color = needleColor, start = Offset(startX, startY), end = Offset(endX, endY), strokeWidth = 12f, cap = StrokeCap.Round)
                drawCircle(color = needleBaseColor, radius = 16f, center = arcCenter)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Aktif Ahenk: $ahenk", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Aktif Makam: $makam", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun MakamScreen(viewModel: TMTunerViewModel) {
    val ahenk by viewModel.selectedAhenk.collectAsState()
    val makam by viewModel.selectedMakam.collectAsState()
    val isTransposed by viewModel.isTransposed.collectAsState()

    val ahenkList = listOf("Mansur (La=440)", "Kız (La=415)", "Bolahenk (La=586)", "Süpürde (La=523)")
    val makamList = listOf("Rast", "Uşşak", "Nihavend", "Hicaz", "Hüseyni")

    var ahenkExpanded by remember { mutableStateOf(false) }
    var makamExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Ayarlar ve Presetler", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(48.dp))

        Text("Ahenk Sistemi", color = Color.Gray)
        Box {
            Button(onClick = { ahenkExpanded = true }) { Text(text = ahenk) }
            DropdownMenu(expanded = ahenkExpanded, onDismissRequest = { ahenkExpanded = false }) {
                ahenkList.forEach { item -> DropdownMenuItem(text = { Text(item) }, onClick = { viewModel.updateAhenk(item); ahenkExpanded = false }) }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Makam", color = Color.Gray)
        Box {
            Button(onClick = { makamExpanded = true }) { Text(text = makam) }
            DropdownMenu(expanded = makamExpanded, onDismissRequest = { makamExpanded = false }) {
                makamList.forEach { item -> DropdownMenuItem(text = { Text(item) }, onClick = { viewModel.updateMakam(item); makamExpanded = false }) }
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tenor Saksafon (Bb) Transpoze", fontSize = 16.sp)
                Switch(checked = isTransposed, onCheckedChange = { viewModel.toggleTranspose() })
            }
        }
    }
}

@Composable
fun DroneScreen(viewModel: TMTunerViewModel) {
    val isPlaying by viewModel.isDronePlaying.collectAsState()
    val volume by viewModel.droneVolume.collectAsState()
    val currentAhenk by viewModel.selectedAhenk.collectAsState()
    val kararSesi = viewModel.getDroneNoteName()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Hedef Karar Sesi", color = Color.Gray, fontSize = 18.sp)
        Text(kararSesi, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("($currentAhenk)", fontSize = 16.sp, color = Color.LightGray, modifier = Modifier.padding(top = 8.dp))
        Spacer(modifier = Modifier.height(64.dp))
        LargeFloatingActionButton(onClick = { viewModel.toggleDrone() }, containerColor = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(96.dp)) {
            Icon(imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Dem Sesi", modifier = Modifier.size(48.dp))
        }
        Spacer(modifier = Modifier.height(64.dp))
        Text("Ses Seviyesi: %${(volume * 100).toInt()}", fontSize = 16.sp)
        Slider(value = volume, onValueChange = { viewModel.setDroneVolume(it) }, modifier = Modifier.padding(horizontal = 32.dp))
    }
}