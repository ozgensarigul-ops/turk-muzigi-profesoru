# Hata Geçmişi ve Öğrenilen Dersler (Troubleshooting)

### 1. `bundleDebugAar` ve `AarMetadata` Görevi İlişkisi
- **Sorun:** Android Kütüphane (`com.android.library`) modüllerinde (`:core:audio` vb.) `tasks.matching { it.name.contains("AarMetadata") }.configureEach { enabled = false }` kullanıldığında, `writeDebugAarMetadata` veya `checkDebugAarMetadata` çıktısı üretilmediğinden `bundleDebugAar` görevi `intermediates/aar_metadata_check/...` dizinini bulamayarak derleme hatası verir.
- **Çözüm:** `AarMetadata` engellemesi yalnızca üst seviye `com.android.application` modülünde (`:app`) uygulanmalı; `com.android.library` modüllerinde devre dışı bırakılmamalıdır.

### 2. `setContent` İçi ViewModel İlklendirmesi ve Erken İzin Tetiklemesi (`UninitializedPropertyAccessException`)
- **Sorun:** `MainActivity` içinde `tunerViewModel` alanı `lateinit` tanımlanıp `setContent { tunerViewModel = viewModel() }` bloğunda ilklendirildiğinde; `setContent` asenkron composition planlaması yaptığından, `onCreate` içerisindeki izin kontrolü (`RECORD_AUDIO`) senkron çalıştığında `tunerViewModel.startListening()` çağrısı `UninitializedPropertyAccessException` ile uygulamanın çökmesine yol açar.
- **Çözüm:** `ViewModel` ilklendirmesi `setContent` içine bırakılmamalı; Activity seviyesinde `by viewModels<TunerViewModel>()` property delegate ile yapılmalıdır. İzin kontrolü ve başlatma döngüsü `checkPermissionAndStartListening()` fonksiyonunda güvenli `ContextCompat.checkSelfPermission` teyidi ile kapsüllenmelidir.
### 3. Android 16 (API 37) Grafik Pencere Yöneticisi (`starting_reveal leash` / `alpha = 0.0`) Siyah Ekran Hatası
- **Sorun:** Android 16 / API 37 sanal cihazında veya Android Studio Layout Inspector ortamında, `compose.ui:1.10.4` sürümü kullanıldığında sistem pencere yöneticisi (WindowManager / SurfaceFlinger) yeni açılış penceresi animasyonunda (`starting_reveal` leash) kilitlenir. Bu kilitlenme nedeniyle Activity'nin çizim durumu `mDrawState=HAS_DRAWN` olsa bile pencere görünürlüğü `mShownAlpha = 0.0` (tamamen siyah / görünmez) seviyesinde kalır.
- **Kök Neden:** Compose 1.10.4 sürümünün Android 16 (API 37) SurfaceFlinger / Shell Transition commit protokolü ile yaşadığı uyumsuzluk, başlangıç penceresi (splash preview) teslimat gecikmesi ve `onCreate` anında senkron mikrofon başlatmanın main thread'i kilitlemesi.
- **Çözüm:**
  1. `gradle/libs.versions.toml` dosyasında Jetpack Compose UI sürümü `1.11.0` olarak tanımlanıp uygulanmalıdır (`composeUi = "1.11.0"`).
  2. `themes.xml` içerisine `<item name="android:windowDisablePreview">true</item>` eklenerek geçiş animasyonu kilitlenmeleri bertaraf edilmelidir.
  3. `MainActivity.kt` içinde `onCreate` seviyesinde `window.statusBarColor`, `window.navigationBarColor` ve `window.decorView.setBackgroundColor(Color.parseColor("#121212"))` tanımlanarak pencere katmanı zorunlu opak yapılmalıdır.
  4. `enableEdgeToEdge()` çağrısı `SystemBarStyle.dark(Color.parseColor("#121212"))` ile güncellenmelidir.
  5. `Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background)` ile ana Compose kapsayıcısı net arka plan rengiyle mühürlenmelidir.
  6. Mikrofon akışı `onCreate` içinde doğrudan çağrılmayıp `setContent` içerisindeki `LaunchedEffect(Unit)` ile asenkron olarak başlatılmalıdır.

### 4. Dem Sesinin (Drone Synth) Bozulması ve Cızırtı Yapması (Buffer Underrun & Phase Accumulator Normalization)
- **Hata:** Dem sesinin (drone) başladıktan kısa süre sonra bozulması, dalga formunda yırtılmalar ve cızırtı yapması.
- **Kök Neden:** `AcousticDroneSynthesizer` içindeki faz biriktiricinin (phase accumulator) $2\pi$ modülüne alınmaması sonucu oluşan floating-point hassasiyet kaybı ve `AudioTrack` akışındaki buffer underrun gecikmeleri.
- **Çözüm:** Fazların her örnekte `phase %= 2 * PI` ile normalize edilmesi, `AudioTrack` tampon boyutunun `maxOf(minBufferSize * 2, 4096 * 2)` çift tampon standardına getirilmesi ve `AudioTrack.WRITE_BLOCKING` moduna geçirilmesi.
