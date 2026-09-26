package com.example.tmtuner.core.musicology.model

/**
 * Türk Müziği makam icrasında ezgisel seyir yönü (İsmail Hakkı Özkan s. 92-94).
 * - CIKICI: Durak perdesi veya çevresinden başlayıp tizlere doğru genişleyen seyir.
 * - INICI_CIKICI: Güçlü perdesi veya çevresinden başlayıp hem pest hem tiz bölgede gezinip durakta karar kılan seyir.
 * - INICI: Tiz bölgeden başlayıp peste doğru inerek durakta karar kılan seyir.
 */
enum class SeyirType {
    CIKICI,
    INICI_CIKICI,
    INICI
}
