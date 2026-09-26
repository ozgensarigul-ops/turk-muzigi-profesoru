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

## [VAKA-007] Kız Neyi Ahengi Canlı İcrada Uşşâk Peşrevi'nin Zîrgûleli Hicaz Olarak Yanlış Tanınması

- **Tarih:** 2026-09-23
- **Test Edilen Eser:** Uşşak Peşrev - Tatyos Efendi (Ahenk: Kız Neyi - B)
- **Hata Belirtisi:** 
  - Tespit Edilen Makam: Zîrgûleli Hicaz (Beklenen: Uşşâk)
  - Seyir: Çıkıcı (Beklenen: İnici-Çıkıcı)
  - Durak: Kaba Hicaz
  - Güçlü: Kaba Çârgâh (Durağın 1 koma altında imkânsız güçlü ataması)
- **Kök Neden:**
  1. *Transpozisyon Yön Hatası:* Kız Neyi (+1 Tanini / +9 koma tiz) icrası sisteme girerken yerindeki perdeye dönüştürülmek için 9 koma eksiltilmesi gerekirken, motorda ters işaretle (+9 koma) ötelenmiş; bu durum Uşşak'ın K-S-T aralığı koma cetvelinde 53-EDO haritasında Zîrgûleli Hicaz'ın S-A12-S çeşnisi bölgesine kaydırmıştır.
  2. *Oktav Katlanması (Armonik Hatası):* Düşük frekans armoniklerinin filtrelenememesi sebebiyle sesler kaba sekizliye fırlamıştır.
  3. *Hiyerarşik Güçlü Filtresi Yokluğu:* Algoritma salt yoğunluk sayımı yaptığı için durağın altında kalan Kaba Çârgâh'ı güçlü tayin etmiştir (Özkan s. 88 ihlali).
  4. *Segâh Pest İcra Yanılgısı:* Uşşâk makamında geleneksel olarak 1-2 koma pest basılan Segâh perdesinin en yakın koma eşleşmesinde Dik Kürdî'ye kayması ve yanlış Hicaz çeşnisi puanı tetiklemesi.
  5. *Erken Seyir Kararı:* Eserin başlangıçtaki birkaç saniyesine bakılarak aceleyle "Çıkıcı" tayin edilmesi.
- **Uygulanan Çözüm:**
  - Kız Neyi ve diğer ney ahenklerinin dönüşüm matrisinde ofset yönleri düzeltildi (Kız Neyi icrası için Mansur'a 9 koma eksiltilerek indirgeme sağlandı).
  - Güçlü perdesi seçim mantığına `Özkan s. 88-89` hiyerarşi kuralı entegre edildi: Güçlü daima durağın tiz tarafında (4. veya 5. derece, minimum +20 koma) yer almak zorundadır.
  - Bas armonik filtreleme (`FrequencyEstimator.disambiguatePeriodLag` ve `disambiguateOctave`) ile oktav sıçramaları önlendi.
  - Segâh perdesi için `[-2.0, +0.5]` koma icra toleransı penceresi ve Uşşâk için pest Segâh güven bonusu eklendi.
  - Seyir analizi 3 aşamalı ağırlıklı pencereye (Giriş, Gelişme, Karar yürüyüşü) bölünerek Nevâ'da dolaşıp Dügâh'a inen eserler için "İnici-Çıkıcı" tespiti sağlandı.

## [VAKA-008] Canlı İcrada Uşşâk Makâmının Çârgâh veya Sûz'nâk Olarak Sapması ve Rafinasyonu

- **Tarih:** 2026-09-24
- **Test Edilen Eser:** Tatyos Efendi Uşşâk Peşrevi (Kız Neyi - B)
- **Hata Belirtisi:**
  - Segâh perdeleri algılandığı halde sistem Sûz'nâk veya Çârgâh makamına sapmakta, ekranda Uşşâk yerine Çârgâh/Sûz'nâk belirmekteydi.
- **Kök Neden:**
  1. *Durak Tayininde Yeden Sapması:* Cümle sonundaki kadansta Râst perdesinin yeden (leading tone) olarak geçişinde durağa gereğinden fazla ağırlık vermesi ve Dügâh yerine Râst veya Çârgâh'ın durak seçilmesi.
  2. *Segâh Nüansı Toleransı:* Uşşâk dörtlüsünün teorik aralık şablonu (K-S-T: 8-5-9 koma) icradaki pest Segâh tavrıyla (Dügâh-Segâh = 6-7 koma, Segâh-Çârgâh = 6-7 koma) tam örtüşmediğinde çeşni puanının düşmesi.
  3. *Makam Eşleme Matrisinde Negatif Kısıtların Eksikliği:* Durak Dügâh iken ve Segâh mevcutken Çârgâh ve Sûz'nâk makamlarına yeterli ceza/eleme kuralı işletilmemesi.
- **Uygulanan Çözüm:**
  - `TonicDetector.kt` modülü inşa edildi: Son 3-5 saniyelik kadans penceresinde Râst'ın yalnızca yeden olarak geçtiği durumlarda durak puanı alması engellendi (Özkan s. 91). Kadansın en pest ve kararlı sesi Dügâh ise Durak Dügâh puanına +40 bonus verildi.
  - `ChesniPatterns.kt` modülünde `USSAK_QUARTET` şablonu güncellendi: Dügâh->Segâh için [6.0..8.5] koma, Segâh->Çârgâh için [4.5..7.5] koma tolerans koridoru tanımlandı ve Segâh tespiti halinde +25 güven puanı eklendi.
  - `MakamScorer.kt` matrisi oluşturuldu: Durak Dügâh olduğunda ve Segâh perdesi tespit edildiğinde Çârgâh makamı puanı sıfırlandı; Durak Dügâh iken Sûz'nâk kesin olarak elendi (-100 puan). Uşşâk için Durak Dügâh ve Güçlü Nevâ zorunlu kılındı.
  - `UssakDetectionRefinementTest.kt` ile %100 doğrulandı.

