<div align="center">

# 🎼 Türk Müziği Profesörü
### *Gelenekten Geleceğe: Mikrotonal Türk Makam Müziği Analiz, Akort ve İcra Laboratuvarı*

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://www.android.com/)
[![Language](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![UI Framework](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Theory](https://img.shields.io/badge/Music_Theory-AEU_%26_53--EDO-E50914?style=for-the-badge)](https://en.wikipedia.org/wiki/53_equal_temperament)
[![Audio Engine](https://img.shields.io/badge/Audio-DSP_%26_Autocorrelation-FFA500?style=for-the-badge)](https://developer.android.com/reference/android/media/AudioRecord)
[![License](https://img.shields.io/badge/License-MIT-009688?style=for-the-badge)](LICENSE)

<p align="center">
  <b>Türk Müziği Profesörü</b>, yüzlerce yıllık Türk Makam Müziği teorik mirasını modern dijital sinyal işleme (DSP), gerçek zamanlı ses analitiği ve yapay zekâ mimarileriyle birleştiren açık kaynaklı, akademik ve pratik bir müzik teknolojisi platformudur.
</p>

[Proje Vizyonu](#-proje-vizyonu-ve-manifesto) •
[Teorik Altyapı](#-türk-makam-müziği-ve-mikrotonal-teori) •
[Temel Özellikler](#-çekirdek-modüller-ve-özellikler) •
[Sistem Mimarisi](#-sistem-mimarisi) •
[Geliştirme Yol Haritası](#-yol-haritası-roadmap) •
[Kurulum ve Derleme](#-kurulum-ve-derleme)

---

</div>

## 📖 Proje Vizyonu ve Manifesto

Batı müziğinde kabul gören 12 Eşit Bölümlü Tampere Sistem (12-EDO / 12-TET), oktavı 100'er centlik 12 yarım sese böler. Ancak yüzyıllardır gelişen **Geleneksel Türk Sanat ve Halk Müziği**, doğası gereği mikrotonaldır; ses aralıkları tampere edilmemiş doğal armoni dizilerine, saf beşlilere ve bölgeye/ekole göre nüans kazanan zengin perde perdelerine dayanır.

Standart dijital akort aletleri ve analiz yazılımları, Türk müziğinin **koma**, **bakiye**, **mücennep** ve **tanini** gibi hassas mikrotonal aralıklarını algılayamaz; Segâh, Uşşak, Sabâ veya Rast perdelerini Batı notasyonuna zorlayarak yanlış yönlendirir.

> **Türk Müziği Profesörü'nün Misyonu:**
> İcracıların, araştırmacıların, bestekârların ve öğrencilerin cebinde taşıyabileceği; **53 Sesli Holder/Turel** ve **Arel-Ezgi-Uzdilek (AEU)** perde dizilimlerini milisent ve tam koma hassasiyetiyle analiz eden, karar seslerini canlı sentezleyen, icra esnasında makam ve seyir takibi yapabilen dünyanın en gelişmiş dijital makam asistanını inşa etmektir.

---

## 🏛️ Türk Makam Müziği ve Mikrotonal Teori

Proje, kuramsal temelini Türk müziğinin kabul görmüş iki büyük teorik modelinden alır:

```
                      GELENEKSEL TÜRK MÜZİĞİ TEORİK OMURGASI
                                       │
        ┌──────────────────────────────┴──────────────────────────────┐
        ▼                                                             ▼
┌──────────────────────────────┐              ┌──────────────────────────────┐
│  Arel-Ezgi-Uzdilek (AEU)     │              │  53 Eşit Bölümlü (53-EDO)    │
│  24 Perdeli Pisagor Dizgisi  │              │  YHolder / Turel Sistemi     │
├──────────────────────────────┤              ├──────────────────────────────┤
│ • Saf Tam Beşliler (3:2)     │              │ • 1 Oktav = 53 Eşit Koma     │
│ • 24 Gayrimütesavi Aralık    │              │ • 1 Koma ≈ 22.6415 Cent      │
│ • B, S, K, T, A Aralıkları   │              │ • Milisent Düzeyinde Doğruluk│
└──────────────────────────────┘              └──────────────────────────────┘
```

### 1. Arel-Ezgi-Uzdilek (AEU) Aralık Hiyerarşisi
AEU teorisinde bir oktav, Pisagor dizilimi uyarınca tam beşliler zinciri (3/2 oranı) temel alınarak oluşturulur. İki tam ses (Tanini = 9 koma) arasındaki aralıklar şu standart değerlerle tanımlanır:

| Aralık İsmi | Kısaltma | Koma Değeri | Cent Değeri (Yaklaşık) | Oransal Karşılık (Frekans Oranı) |
|:---|:---:|:---:|:---:|:---:|
| **Fazla (Koma)** | F | 1 Koma | ~22.64 Cent | $\approx 1.0132$ |
| **Bakiye** | B | 4 Koma | ~90.22 Cent | $256 / 243$ |
| **Küçük Mücennep** | S | 5 Koma | ~114.44 Cent | $2187 / 2048$ |
| **Büyük Mücennep** | K | 8 Koma | ~180.45 Cent | $65536 / 59049$ |
| **Tanini (Tam Ses)** | T | 9 Koma | ~203.91 Cent | $9 / 8$ |
| **Artık İkili** | A₁₂ / A₁₃ | 12 - 13 Koma | ~271 - 294 Cent | $19683 / 16384$ vb. |

### 2. 53 Sesli Sistem (William Holder / YHolder / Turel / 53-EDO)
Batı tampere sistemindeki 12 yarım ses yerine oktav $53$ eşit parçaya (Holder koması) bölünür:
$$f_n = f_0 \cdot 2^{\frac{n}{53}} \quad (n \in [0, 52])$$

Bir koma matematiksel olarak tam:
$$\text{Cent}(\text{Koma}) = \frac{1200}{53} \approx 22.641509\text{ Cent}$$
Bu sayede AEU'nun Pisagorik 24 sesli perdesi ve uygulamada icracıların kullandığı ara perdeler (ör. Dik Hisar, Nîm Hicaz, Dik Segâh, vb.) mutlak bir dijital grid üzerinde eşleştirilir.

### 3. 3-Oktavlık Perde Atlası
Uygulama, 3 oktav boyunca (Pes / Kaba, Orta / Ana, Tîz / En Tîz) 72'den fazla mikrotonal perdeyi tanımlar ve anlık olarak frekans haritasında indeksler:
- **Pes / Kaba (Octave 0):** Kaba Çârgâh, Kaba Nîm Hicâz, Kaba Hicâz, Yegâh, Kaba Hisâr, Hüseynî Aşîrân, Acem Aşîrân, Irak, Rast, Dügâh, Segâh, Bûselik...
- **Orta / Ana (Octave 1):** Çârgâh, Nîm Hicâz, Hicâz, Neva, Nîm Hisâr, Hüseynî, Acem, Eviç, Gerdâniye, Şehnâz, Muhayyer, Tîz Segâh, Tîz Bûselik...
- **Tîz / En Tîz (Octave 2):** Tîz Çârgâh, Tîz Neva, Tîz Hüseynî, Tîz Gerdâniye, Tîz Muhayyer, En Tîz Segâh...

### 4. Geleneksel Ahenk ve Transpozisyon Sistemi
Türk müziği enstrümanları (özellikle Ney, Tanbur, Kemençe, Ud, Kanun) icra edilen ahenge göre farklı frekans referanslarında çalınır. Aynı zamanda Türk makamı icra eden modern nefesli ve orkestra sazları için dinamik perde transpozisyonu uygulanır:

#### 🎵 Geleneksel Ahenk Tablosu
| Ahenk Adı | Referans La (Dügâh/Neva) | Açıklama & Tipik Kullanım |
|:---|:---:|:---|
| **Mansur** | **440.0 Hz** | Ana referans ahengi (Standart Diyapazon - Mansur Ney boyu) |
| **Kız** | **415.0 Hz** | Yaklaşık 1 tam ses pes (Kız Ney boyu) |
| **Süpürde** | **523.2 Hz** | 3 ses tiz (Süpürde Ney boyu) |
| **Bolahenk** | **586.6 Hz** | 4 ses tiz / Nısfiye boyu |

#### 🎷 Batı Nefeslileri & Transpoze Enstrüman Desteği
Orkestra ve nefesli saz icracılarının, parmak pozisyonlarını Türk makam müziğinin geleneksel perde isimleriyle (Çârgâh, Dügâh, Segâh, Rast vb.) doğrudan eşleştirebilmesi için akıllı transpozisyon motoru entegre edilmiştir:

| Transpoze Modu | Hedef Enstrümanlar | Frekans Katsayısı | Teorik Aralık (Pisagor / AEU) | Açıklama ve Çalışma Mantığı |
|:---|:---|:---:|:---:|:---|
| **Konsert (Do)** | Ney, Ud, Keman, Kanun, Flüt | $f \times 1.0$ | Doğal | Duyulan ses doğrudan duyulduğu frekansla eşleşir. |
| **B♭ (Si♭) Transpoze** | Tenor Saksafon, Soprano Saksafon, B♭ Klarnet, Trompet | $f \times \frac{9}{8}$ | Tanini ($+9$ Koma / $+203.9\text{ Cent}$) | İcracı "Do" (Çârgâh) üflediğinde akustik olarak konsert Si♭ tınlar; motor bunu 1 tam ses yukarı öteleyerek ekranda hedeflenen Türk müziği perdesini gösterir. |
| **E♭ (Mi♭) Transpoze** | **Alto Saksafon (E♭)**, Bariton Saksafon, E♭ Klarnet | $f \times \frac{27}{16}$ | Büyük Altılı ($+40$ Koma / $\approx +905.9\text{ Cent}$) | **E♭ Alto Saksafon** icracısı yazılı "La" (Dügâh) üflediğinde tınlayan konsert Do (Çârgâh) sesini veya yazılı "Do" bastığında tınlayan konsert Mi♭ sesini, $27/16$ çarpanı ile telafi ederek makam icracısının bastığı perdeyi doğrudan Türk müziği perde atlasında gösterir. |


---

## 🚀 Çekirdek Modüller ve Özellikler

### 🎯 1. Gerçek Zamanlı Mikrotonal Akort Motoru (DSP Tuner Engine)
- **Sinyal Girişi:** 44.1 kHz, 16-Bit PCM mono formatında düşük gecikmeli mikrofon dinlemesi.
- **Otokorelasyon (Autocorrelation F0 Estimator):** Zaman uzayında sinyalin öz-korelasyon piklerini tespit ederek 50 Hz - 2000 Hz aralığında temel frekansı (fundamental frequency) yüksek kararlılıkla yakalar.
- **Dinamik Mod Seçimi:**
  - **Pro Mod (Akışkan Kadran):** Sönümlü yay fiziği ve interpolasyon eğrileri (`LinearOutSlowInEasing`) ile titreşimsiz, profesyonel analog kadran hissi.
  - **Klasik Mod (Hızlı / Sıfır Gecikme):** Stüdyo ve hızlı akort anları için filtrelenmemiş anlık ham reaksiyon.
- **Tanini Kadranı (-4.5 Koma / +4.5 Koma):** 1 tam seslik (9 koma) pencerede iğne salınımı. İbre yeşil olduğunda perde tam oturmuştur (merkeze $\pm 0.5$ komadan yakın).
- **Tam Koma Bildirimi:** İcracının kafa karışıklığını önlemek için küsuratları ayıklar; *"+2 Koma (Dik)"*, *"1 Koma (Pes)"* veya *"Tam İsabet"* şeklinde okunabilir geribildirim sunar.

### 🔊 2. Karar Sesi ve Dem (Tanbura / Drone) Motoru
Makam icrasında en kritik unsur, kulağın sürekli olarak makamın karar sesi (tonik) ile kurduğu psikoakustik ilişkidir.
- **`AudioTrack` PCM Akışı:** Arka planda donanım düzeyinde çalışan gerçek zamanlı ses sentezleyici.
- **Faz Birikimli Sinüs Sentezi:** Faz sürekliliğini (`dynamic phase accumulation`) koruyarak klik, patlama veya kesinti olmadan sonsuz döngüde karar sesi üretimi.
- **Makama Göre Otomatik Karar Ataması:**
  - **Rast, Nihavend:** Rast Perdesi (Sol bandı)
  - **Uşşak, Hüseyni, Hicaz:** Dügâh Perdesi (La bandı)
  - Seçilen ahenge (Mansur, Kız, Bolahenk) göre hedef karar frekansı otomatik transpoze edilir.

### 📱 3. Modern, Reaktif ve Ergonomik Arayüz
- Tamamen **Jetpack Compose** ve **Material 3** ile geliştirilmiş koyu tema (Dark Theme).
- Donanım hızlandırmalı vektör tabanlı `Canvas` göstergeleri.
- Sahne, prova ve loş ışıklı stüdyo ortamlarında gözü yormayan yüksek kontrastlı mikrotonal tipografi.

---

## 🏗️ Sistem Mimarisi

Uygulama, **Clean Architecture** prensipleri, reaktif durum yönetimi (**MVI / Unidirectional Data Flow**) ve modüler DSP katmanları üzerine kurgulanmıştır:

```mermaid
graph TD
    subgraph "Kullanıcı Arayüzü (UI Layer - Jetpack Compose)"
        UI_Tuner["TunerScreen (Mikrotonal Kadran, Koma Analizi)"]
        UI_Makam["MakamScreen (Ahenk, Presetler, Bb Transpoze)"]
        UI_Drone["DroneScreen (Dem Sesi, Karar Kontrolü, Slider)"]
    end

    subgraph "Durum Yönetimi (State & Presentation Layer)"
        VM["TMTunerViewModel (StateFlow & Coroutines)"]
        State_Freq["frequency: StateFlow<Float>"]
        State_Note["detectedNote: StateFlow<String>"]
        State_Koma["centsDifference / Koma Offset"]
    end

    subgraph "Ses İşleme Motoru (DSP & Audio Engine Layer)"
        MicRecord["AudioRecord (44.1kHz, PCM 16-Bit)"]
        PitchDetect["Autocorrelation F0 Estimator (Zaman Uzayı)"]
        DroneTrack["AudioTrack (Mono PCM Stream)"]
        SineGen["Phase Accumulator Tone Generator"]
    end

    subgraph "Türk Müziği Teorik Çekirdeği (Theory Core Engine)"
        AEU_Matrix["AEU 24 Oran Tablosu (aeuRatios)"]
        Ahenk_Calc["Ahenk Frekans Ölçekleyici (Mansur, Kız, vb.)"]
        EDO_Converter["53-EDO / Cent / Koma Dönüştürücü"]
        Octave_Mapper["3 Oktav Perde İsim Eşleştirici (72 Perde)"]
    end

    UI_Tuner -->|Kullanıcı Etkileşimi| VM
    UI_Makam -->|Ahenk / Makam Değişimi| VM
    UI_Drone -->|Dem Aç/Kapa| VM

    VM --> State_Freq
    VM --> State_Note
    VM --> State_Koma

    State_Freq --> UI_Tuner
    State_Note --> UI_Tuner
    State_Koma --> UI_Tuner

    MicRecord -->|Ham Ses Tamponu| PitchDetect
    PitchDetect -->|Frekans (Hz)| VM

    VM -->|Makam / Ahenk Parametreleri| Theory_Core
    AEU_Matrix --> Octave_Mapper
    Ahenk_Calc --> Octave_Mapper
    Octave_Mapper --> VM

    VM -->|Hedef Karar Frekansı| SineGen
    SineGen -->|Sentezlenmiş Dalga| DroneTrack
```

---

## 🗺️ Yol Haritası (Roadmap)

Türk Müziği Profesörü, aşamalı bir inovasyon planıyla sadece bir akort aleti değil, bütüncül bir yapay zekâlı müzikoloji platformu olma yolunda ilerlemektedir:

### 📍 Faz 1: Çekirdek Akort ve Dem Altyapısı `(Mevcut Sürüm - v1.0)`
- [x] Arel-Ezgi-Uzdilek 24 sesli ve 3 oktavlık perde tablosu entegrasyonu.
- [x] Otokorelasyon tabanlı gerçek zamanlı F0 frekans tespiti.
- [x] 53-EDO Holderian koma hesaplayıcısı ve $\pm 4.5$ komalık analog kadran.
- [x] Pro Mod (sönümlü yay animasyonu) / Klasik Mod ayrımı.
- [x] Mansur, Kız, Bolahenk, Süpürde ahenk seçenekleri; B♭ ve E♭ (Alto Saksafon) transpoze desteği.
- [x] Kesintisiz faz akışlı monofonik karar sesi (drone) motoru.

### 📍 Faz 2: Yüksek Performanslı DSP ve C++ / Oboe Katmanı `(v1.5)`
- [ ] **Google Oboe / AAudio C++ Entegrasyonu:** Android ses gecikmesini (audio latency) 10 milisaniyenin altına indirme.
- [ ] **Gelişmiş Zift Tespiti (Pitch Detection):**
  - **YIN / pYIN** ve **McLeod Pitch Method (MPM)** algoritmaları ile gürültülü ortamlarda oktav hatasını sıfırlama.
  - Ney için hava üfleme gürültüsü filtreleme bandı (Band-pass/Comb filter).
  - Tanbur, Ud ve Kanun mızrap vuruşları için Transient / Onset algılama.
- [ ] **Enstrüman Akort Önayarları (Presets):**
  - Bağlama düzenleri (Kara Düzen, Bağlama Düzeni, Misket, Müstezat).
  - Ud akort sistemleri (Klasik 5 telli, 6 telli Cinuçen/Targan sistemleri).
  - Klasik Kemençe, Yaylı Tanbur ve Kanun tel haritaları.

### 📍 Faz 3: Çok-Tınılı Organik Dem Sentezleyici (Acoustic Drone) `(v2.0)`
- [ ] **Akustik Tanbura Örneklemesi:** Yalnızca saf sinüs dalgası değil, gerçek tanbura sazının rezonanslı gövde tınıları ve sempati telleri (sympathetic strings) fiziğini içeren WAV/SFZ tabanlı multi-sample motoru.
- [ ] **Ney Dem Tınısı:** Farklı ney boylarından (Mansur, Kız, Şah, vb.) kaydedilmiş zengin harmonikli üfleme dem sesleri.
- [ ] **Çok Sesli Dem:** Karar sesiyle birlikte güçlü (dominant) perdesinin (ör. Rast için Neva perdesi) armonik olarak eşlik edebilmesi.

### 📍 Faz 4: Yapay Zekâ Destekli Makam Tanıma ve Seyir Analizi `(v2.5 - v3.0)`
- [ ] **Zaman-Frekans Perde Histogramı (Pitch Class Profile):** Çalınan melodinin perdelerde kalış sürelerini ölçerek makamın perde dağılım haritasını çıkarma.
- [ ] **Derin Öğrenme ile Makam Tanıma:**
  - CNN-BiLSTM ve HMM (Hidden Markov Model) hibrit mimarisi.
  - İcracının yaptığı taksimi veya eseri dinleyerek giriş, asma karar, güçlü ve tam karar aşamalarını (Seyir Analizi) gerçek zamanlı tespit etme.
  - Ekranda *"Şu an Rast makamındasınız, Gerdâniye perdesine meylediliyor"* şeklinde rehberlik.
- [ ] **Mikrotonal Dikte & Transkripsiyon:** İcra edilen ezgiyi mikrotonal nota fontları (SMuFL uyumlu AEU arıza işaretleri) ile notaya dökme ve MusicXML/MIDI dışa aktarımı.

### 📍 Faz 5: Mikrotonal İşitme Eğitimi ve Solfej Modülü `(v3.5)`
- [ ] **Koma İşitme Testi:** İki mikrotonal frekans arasındaki koma farkını ayırt etme alıştırmaları (1 koma, 2 koma, 4 koma bakiye farkları).
- [ ] **Makam Kulak Eğitimi:** Dinletilen ezginin makamını bulma ve interaktif sesli solfej okuma değerlendirmesi.

---

## 🛠️ Kurulum ve Derleme

### Gereksinimler
- **Android Studio:** Ladybug (2024.2.1+) veya Koala
- **JDK:** OpenJDK 17 veya 21
- **Android SDK:** Compile SDK 36 (Minimum SDK 24 - Android 7.0 Nougat)
- **Gradle:** 8.9+ (Kotlin 2.0+ Compose Compiler)

### Projeyi Klonlama ve Çalıştırma
```bash
# Depoyu klonlayın
git clone https://github.com/ozgensarigul-ops/turk-muzigi-profesoru.git
cd turk-muzigi-profesoru

# Gradle bağımlılıklarını eşitleyin ve Debug APK oluşturun
./gradlew assembleDebug

# Cihazınıza veya emülatörünüze yükleyin
./gradlew installDebug
```

---

## 📚 Akademik ve Kuramsal Kaynakça

Projenin perde, koma ve oran hesaplamaları aşağıdaki kaynakların ortak birikiminden süzülmüştür:
1. **Arel, Hüseyin Sadettin** — *Türk Musikisi Nazariyatı Dersleri*, Kültür Bakanlığı Yayınları.
2. **Ezgi, Dr. Suphi** — *Nazarî ve Amelî Türk Musikisi*, İstanbul Konservatuvarı Neşriyatı (1933-1953).
3. **Uzdilek, Salih Murat** — *İlim ve Musiki*, Türk Musikisi Dergisi.
4. **Holder, William** — *Treatise on the Natural Grounds, and Principles of Harmony* (1694) — (53-EDO / Holder Koması kuramı).
5. **Özkan, İsmail Hakkı** — *Türk Mûsikîsi Nazariyatı ve Usûlleri: Kudüm Velveleleri*, Ötüken Neşriyat.
6. **Tanrıkorur, Cinuçen** — *Müzik Kimliğimiz Üzerine Düşünceler & Türk Müzik Kimliği*.
7. **Yarman, Ozan** — *79-sesli Türk Müziği Ses Sistemi ve 53-sesli Çözümlemeler*.
8. **Karaosmanoğlu, Kemal** — *A Turkish Makam Music Symbolic Database for Music Information Retrieval: SymbTr*.

---

## 🤝 Katkıda Bulunma

Türk müziği teorisi araştırmacıları, DSP mühendisleri, Android/Kotlin geliştiricileri ve geleneksel saz icracılarının katkıları bu projeyi ileriye taşıyacaktır!

1. Bu depoyu çatallayın (Fork edin).
2. Yeni bir özellik dalı açın (`git checkout -b ozellik/yeni-dsp-algoritmasi`).
3. Değişikliklerinizi commit edin (`git commit -m 'feat: YIN perde algılama algoritması eklendi'`).
4. Dalınızı uzak sunucuya gönderin (`git push origin ozellik/yeni-dsp-algoritmasi`).
5. Bir **Pull Request (PR)** oluşturun.

---

## 📄 Lisans

Bu proje [MIT Lisansı](LICENSE) altında korunmaktadır. Türk musikisinin dijital geleceğine katkıda bulunmak isteyen tüm araştırmacı ve geliştiricilerin kullanımına açıktır.

---

<div align="center">
  <sub>Geleneksel Türk Makam Müziği İlim ve İrfanının Dijital Çağdaki Referansı.</sub><br>
  <b>Türk Müziği Profesörü Projesi</b>
</div>
