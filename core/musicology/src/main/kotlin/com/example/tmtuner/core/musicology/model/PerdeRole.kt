package com.example.tmtuner.core.musicology.model

/**
 * Türk Müziği makam yapısındaki perdelerin fonksiyonel rolleri (İsmail Hakkı Özkan s. 95-97).
 */
enum class PerdeRole {
    DURAK,       // Karar perdesi (Tonic)
    GUCLU,       // Yarım karar / egemen perde (Dominant)
    ASMA_KARAR,  // Muvakkat karar perdesi (Suspended tonic)
    YEDEN,       // Karar perdesinin altındaki çekim perdesi (Leading note)
    DERECE       // Dizinin diğer basamak perdeleri (Scale degree)
}
