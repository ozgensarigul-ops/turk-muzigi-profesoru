package com.example.tmtuner.core.musicology.analysis

import com.example.tmtuner.core.musicology.model.ChesniPatterns
import com.example.tmtuner.core.musicology.model.MakamProfile
import com.example.tmtuner.core.musicology.model.SeyirType
import com.example.tmtuner.core.musicology.registry.MakamRegistry

/**
 * Makam Eşleme Matrisi ve Puanlama Motoru (MakamScorer).
 *
 * İsmail Hakkı Özkan'ın "Türk Mûsikîsi Nazariyatı ve Usûlleri" kurallarına (s. 51, 118-206)
 * tam uyumlu makam puanlama algoritması:
 * 1. Durak ve Güçlü Hiyerarşisi
 * 2. Seyir Uyumu (Çıkıcı, İnici-Çıkıcı, İnici)
 * 3. Dizi Perde Örtüşmesi
 * 4. Çeşni ve Mikrotonal İcra Toleransı (Segâh nüansı vb.)
 * 5. Makam Aileleri ve Çeşni Ayrıştırmaları
 */
object MakamScorer {

    /**
     * Aday makam profilini tespit edilen özelliklere göre puanlar.
     */
    fun scoreMakam(
        makam: MakamProfile,
        detectedDurak: String,
        detectedGuclu: String,
        detectedSeyir: SeyirType,
        rawHistogram: Map<String, Double>,
        totalWeightedDuration: Double,
        hasSegah: Boolean,
        avgSegahOffset: Double
    ): Double {
        val durakNorm = MakamRegistry.normalizePerdeName(detectedDurak)
        val gucluNorm = MakamRegistry.normalizePerdeName(detectedGuclu)
        val makamDurakNorm = MakamRegistry.normalizePerdeName(makam.durakPerde)
        val makamGucluNorm = MakamRegistry.normalizePerdeName(makam.gucluPerde)

        // --- ÖZEL CEZA VE ELEME KURALLARI (Özkan Nazariyatı) ---

        // 1. Çârgâh makamına ceza kuralı (Özkan s. 118):
        // Çârgâh makamında Bûselik (Si bekar) vardır; Segâh perdesi kesinlikle bulunamaz.
        // Eğer durak Dügâh ise ve Segâh perdesi mevcutsa, Çârgâh makamı puanını sıfırla.
        if (makam.id == "cargah" && durakNorm == "dugah" && hasSegah) {
            return 0.0
        }

        // 2. Sûz'nâk makamına ceza kuralı (Özkan s. 203):
        // Sûz'nâk makamı Râst perdesinde karar eder.
        // Eğer durak Dügâh ise Sûz'nâk kesinlikle seçilemez.
        if (makam.id == "basit_suznak" && durakNorm == "dugah") {
            return -100.0
        }

        // 3. Uşşâk Makamı Zorunlu Kuralı (Özkan s. 143):
        // Durak: DÜGÂH (Zorunlu)
        // Eğer durak Dügâh değilse Uşşâk seçilemez.
        if (makam.id == "ussak" && durakNorm != "dugah") {
            return -100.0
        }

        var score = 0.0

        // 1. Durak Uyumu (En kritik kriter: 40 puan)
        if (durakNorm == makamDurakNorm) {
            score += 40.0
        } else {
            score -= 35.0
        }

        // 2. Güçlü Uyumu (25 puan)
        if (gucluNorm == makamGucluNorm) {
            score += 25.0
        } else if (makam.asmaKararPerdeleri.any { MakamRegistry.normalizePerdeName(it) == gucluNorm }) {
            score += 15.0 // Asma karar veya 2. mertebe güçlü uyumu
        } else if (makam.id == "ussak") {
            // Uşşâk'ta Güçlü NEVÂ zorunludur
            score -= 30.0
        }

        // 3. Seyir Uyumu (15 puan)
        if (detectedSeyir == makam.seyir) {
            score += 15.0
        } else if (detectedSeyir == SeyirType.INICI_CIKICI && makam.seyir == SeyirType.CIKICI) {
            score += 5.0 // Kısmi seyir benzerliği
        }

        // 4. Dizi Perde Örtüşmesi (15 puan)
        val makamScaleNorms = makam.scalePerdeler.map { MakamRegistry.normalizePerdeName(it) }.toSet()
        var inScaleWeight = 0.0
        var foreignWeight = 0.0

        for ((perde, w) in rawHistogram) {
            if (makamScaleNorms.contains(perde)) {
                inScaleWeight += w
            } else {
                foreignWeight += w
            }
        }

        val scaleRatio = if (totalWeightedDuration > 0.0) inScaleWeight / totalWeightedDuration else 0.0
        val foreignRatio = if (totalWeightedDuration > 0.0) foreignWeight / totalWeightedDuration else 0.0
        score += (scaleRatio * 15.0) - (foreignRatio * 20.0)

        // 5. Segâh İcra Toleransı ve Pest Segâh Bonusu (Özkan s. 51 & 143)
        if (hasSegah) {
            val toleranceRange = if (makam.id == "ussak") -2.0..0.8 else makam.segahToleranceKoma
            if (avgSegahOffset in toleranceRange) {
                // Tolerans penceresinde ceza uygulanmaz
                score += 5.0
                // Uşşâk ve Hüseynî makamlarında Segâh 1-2 koma pest basıldığında (-0.5 .. -2.0) ek bonus
                if ((makam.id == "ussak" || makam.id == "huseyni") && avgSegahOffset <= -0.5) {
                    score += 15.0
                }
            } else {
                // Tolerans dışı aşırı sapma varsa ceza puanı
                score -= 10.0
            }

            // Uşşâk Makamına Özel Segâh Güven Bonusu (ChesniPatterns.USSAK_QUARTET)
            if (makam.id == "ussak") {
                score += ChesniPatterns.USSAK_QUARTET.segahBonusScore // +25.0 güven puanı
            }
        }

        // 6. Makam Aileleri ve Çeşni Ayrıştırmaları (Özkan s. 143-206)
        val hasHisarOrSehnaz = rawHistogram.containsKey("hisar") || rawHistogram.containsKey("dik hisar") ||
                rawHistogram.containsKey("nim hisar") || rawHistogram.containsKey("sehnaz") ||
                rawHistogram.containsKey("dik sehnaz") || rawHistogram.containsKey("nim sehnaz")

        // A. Hüseynî vs Uşşâk Ayrımı (Özkan s. 143 & 179)
        if (makam.id == "huseyni") {
            val hasEvic = rawHistogram.containsKey("evic")
            val hasAcem = rawHistogram.containsKey("acem")
            if (hasEvic) score += 18.0
            if (hasAcem && !hasEvic) score -= 12.0
            if (gucluNorm == "huseyni") score += 15.0
            if (hasHisarOrSehnaz) score -= 25.0
        } else if (makam.id == "ussak") {
            val hasAcem = rawHistogram.containsKey("acem")
            val hasEvic = rawHistogram.containsKey("evic")
            if (hasAcem) score += 15.0
            if (hasEvic) score -= 15.0
            if (gucluNorm == "neva") score += 12.0
            if (hasHisarOrSehnaz) score -= 25.0
        }

        // B. Hicaz Ailesi İnce Ayrımı (Hümâyûn, Hicaz, Uzzal, Zîrgûleli Hicaz - Özkan s. 157-178)
        val hasHicazCesnisi = rawHistogram.containsKey("hicaz") || rawHistogram.containsKey("dik hicaz") ||
                rawHistogram.containsKey("nim hicaz") || rawHistogram.containsKey("dik kurdi")

        if (makam.id in listOf("humayun", "hicaz", "uzzal", "zirguleli_hicaz")) {
            if (hasHicazCesnisi) score += 20.0 else score -= 15.0
            if (rawHistogram.containsKey("segah")) score -= 25.0
        }

        when (makam.id) {
            "humayun" -> {
                val hasAcem = rawHistogram.containsKey("acem")
                val hasEvic = rawHistogram.containsKey("evic")
                if (hasAcem) score += 25.0
                if (hasEvic) score -= 30.0
                if (hasHisarOrSehnaz) score -= 20.0
            }
            "hicaz" -> {
                val hasEvic = rawHistogram.containsKey("evic")
                val hasAcem = rawHistogram.containsKey("acem")
                if (hasEvic) score += 30.0
                if (hasAcem && !hasEvic) score -= 30.0
                if (gucluNorm == "neva") score += 15.0
                if (hasHisarOrSehnaz) score -= 20.0
            }
            "uzzal" -> {
                val hasEvic = rawHistogram.containsKey("evic")
                if (hasEvic) score += 20.0
                if (gucluNorm == "huseyni") score += 25.0
                if (hasHisarOrSehnaz) score -= 20.0
            }
            "zirguleli_hicaz" -> {
                if (hasHisarOrSehnaz) score += 30.0 else score -= 20.0
                if (rawHistogram.containsKey("zirgule") || rawHistogram.containsKey("nim zirgule")) score += 15.0
                if (rawHistogram.containsKey("evic") || rawHistogram.containsKey("acem")) score -= 15.0
            }
        }

        // C. Karcığar Makamı (Özkan s. 199)
        if (makam.id == "karcigar") {
            if (hasSegah && hasHisarOrSehnaz) {
                score += 35.0
            } else if (!hasHisarOrSehnaz) {
                score -= 20.0
            }
            if (rawHistogram.containsKey("evic") || rawHistogram.containsKey("acem")) score -= 15.0
        }

        // D. Basit Sûz'nâk Makamı (Özkan s. 203)
        if (makam.id == "basit_suznak") {
            if (durakNorm == "rast" && hasHisarOrSehnaz) {
                score += 35.0
            }
            if (rawHistogram.containsKey("evic")) score -= 20.0
        } else if (makam.id == "rast") {
            if (hasHisarOrSehnaz) score -= 30.0
        }

        // E. Nevâ Makamı (Özkan s. 191)
        if (makam.id == "neva") {
            val hasEvic = rawHistogram.containsKey("evic")
            if (gucluNorm == "neva" && hasEvic && hasSegah) score += 15.0
            if (hasHisarOrSehnaz) score -= 25.0
        }

        return score
    }
}
