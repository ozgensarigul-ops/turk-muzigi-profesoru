package com.example.tmtuner.data.ml

import com.example.tmtuner.data.models.AhenkType
import com.example.tmtuner.data.models.SymbTrMakam
import com.example.tmtuner.data.models.SymbTrPerde

data class MakamPitchDistributionVector(
    val makamId: String,
    val makamAdi: String,
    val ahenk: AhenkType,
    val komaHistogram: FloatArray,
    val durakKoma: Int,
    val gucluKoma: Int,
    val isEbAltoSax: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MakamPitchDistributionVector
        return makamId == other.makamId && ahenk == other.ahenk && isEbAltoSax == other.isEbAltoSax && komaHistogram.contentEquals(other.komaHistogram)
    }

    override fun hashCode(): Int {
        var result = makamId.hashCode()
        result = 31 * result + ahenk.hashCode()
        result = 31 * result + komaHistogram.contentHashCode()
        return result
    }
}

object MakamDatasetGenerator {

    fun generateMakamFeatureVector(
        makam: SymbTrMakam,
        ahenk: AhenkType = AhenkType.BOLAHENK,
        isEbAltoSax: Boolean = false,
        nuanceOffset: Int = 0
    ): MakamPitchDistributionVector {
        val histogram = FloatArray(53) { 0.0f }

        makam.perdeler.forEach { perde ->
            var komaMod = perde.oktavKomaMod % 53
            if (perde.id == "segah" && nuanceOffset != 0) {
                komaMod = (komaMod + nuanceOffset + 53) % 53
            }
            histogram[komaMod] += 1.0f
        }

        val durakMod = makam.durakPerde.oktavKomaMod % 53
        histogram[durakMod] += 3.0f

        val gucluMod = makam.gucluPerde.oktavKomaMod % 53
        histogram[gucluMod] += 2.0f

        var sumSquares = 0.0f
        for (v in histogram) {
            sumSquares += v * v
        }
        val norm = kotlin.math.sqrt(sumSquares)
        if (norm > 0.0f) {
            for (i in histogram.indices) {
                histogram[i] /= norm
            }
        }

        return MakamPitchDistributionVector(
            makamId = makam.id,
            makamAdi = makam.makamAdi,
            ahenk = ahenk,
            komaHistogram = histogram,
            durakKoma = durakMod,
            gucluKoma = gucluMod,
            isEbAltoSax = isEbAltoSax
        )
    }

    fun generateEpochTrainingSet(makamlar: List<SymbTrMakam>): List<MakamPitchDistributionVector> {
        val dataset = mutableListOf<MakamPitchDistributionVector>()
        val ahenkler = listOf(AhenkType.BOLAHENK, AhenkType.MANSUR)
        val nuanceValues = listOf(0, -1, -2)

        for (makam in makamlar) {
            for (ahenk in ahenkler) {
                for (nuance in nuanceValues) {
                    dataset.add(generateMakamFeatureVector(makam, ahenk, isEbAltoSax = false, nuanceOffset = nuance))
                    dataset.add(generateMakamFeatureVector(makam, ahenk, isEbAltoSax = true, nuanceOffset = nuance))
                }
            }
        }
        return dataset
    }
}
