# Hata Geçmişi ve Öğrenilen Dersler (Troubleshooting)

### 1. `bundleDebugAar` ve `AarMetadata` Görevi İlişkisi
- **Sorun:** Android Kütüphane (`com.android.library`) modüllerinde (`:core:audio` vb.) `tasks.matching { it.name.contains("AarMetadata") }.configureEach { enabled = false }` kullanıldığında, `writeDebugAarMetadata` veya `checkDebugAarMetadata` çıktısı üretilmediğinden `bundleDebugAar` görevi `intermediates/aar_metadata_check/...` dizinini bulamayarak derleme hatası verir.
- **Çözüm:** `AarMetadata` engellemesi yalnızca üst seviye `com.android.application` modülünde (`:app`) uygulanmalı; `com.android.library` modüllerinde devre dışı bırakılmamalıdır.
