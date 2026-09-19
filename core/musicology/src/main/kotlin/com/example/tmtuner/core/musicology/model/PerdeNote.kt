package com.example.tmtuner.core.musicology.model

/**
 * 3-Oktavlık Perde Atlasında yer alan mikrotonal bir Türk Müziği perdesi.
 */
data class PerdeNote(
    val id: String,
    val name: String,
    val octaveIndex: Int, // 0: Kaba/Pes, 1: Ana/Orta, 2: Tîz/En Tîz
    val octaveName: String,
    val frequency: Double,
    val pythagoreanRatio: Double,
    val ratioString: String = "",
    val aeuSymbol: String = "",
    val westernEquivalent: String = "",
    val mutlakKoma: Int = 0,
    val oktavKomaMod: Int = mutlakKoma % 53,
    val isFundamental: Boolean = false,
    val makamRole: String = ""
)
