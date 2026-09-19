# Proje Genel Bakış & Anayasa
Türk Müziği Profesörü - Native Android Application

## Teknoloji Yığını
- Dil: Kotlin (Strict Mode)
- UI: Jetpack Compose (Material3)
- Mimari: MVVM + Clean Architecture (Unidirectional Data Flow)
- Veritabanı & Ağ: Room Database + Retrofit

## Derleme & Test Komutları
- Derleme: `./gradlew assembleDebug`
- Test Çalıştırma: `./gradlew testDebugUnitTest`
- Lint Kontrolü: `./gradlew lint`

## Proje Mimarisi & Dizin Yapısı
- Ekranlar: `/app/src/main/java/com/example/tmtuner/ui/features/`
- Bileşenler: `/app/src/main/java/com/example/tmtuner/ui/components/`
- İş Mantığı: `/app/src/main/java/com/example/tmtuner/domain/`
- Veri Katmanı: `/app/src/main/java/com/example/tmtuner/data/`

## Kritik Kurallar
- Yeni ekranlarda SADECE Jetpack Compose kullan. XML yazma.
- ViewModels durum yönetimi için `StateFlow` kullanmalıdır.
- API anahtarlarını koda gömme; `local.properties` kullan.
- Veritabanı ve ağ çağrıları `Result<T>` veya `Flow<T>` döndürmelidir.
- İş mantığı ViewModel içinde değil, Repository / UseCase katmanında olmalıdır.

## Dokunulmaz Bölgeler (Plan sunmadan dokunma)
- `/app/src/main/java/com/example/tmtuner/data/local/db/` (DB Şemaları ve Migrasyonlar)
- `build.gradle.kts` (Bağımlılık güncellemeleri)

## Hata Geçmişi ve Öğrenilen Dersler
@docs/troubleshooting.md
