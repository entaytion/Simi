package ua.entaytion.simi.utils

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

data class DenominationCount(
    val label: String,
    val count: Int
)

object MoneyUtils {
    private val denominations = listOf(
        100_000 to "1000 грн",
        50_000 to "500 грн",
        20_000 to "200 грн",
        10_000 to "100 грн",
        5_000 to "50 грн",
        2_000 to "20 грн",
        1_000 to "10 грн",
        500 to "5 грн",
        200 to "2 грн",
        100 to "1 грн",
        50 to "50 коп"
    )

    fun breakdown(amount: Double): List<DenominationCount> {
        if (amount == 0.0) return emptyList()
        val totalKopecks = (normalizeToHryvnia(amount) * 100).toInt()
        var remaining = totalKopecks
        val result = mutableListOf<DenominationCount>()
        for ((value, label) in denominations) {
            val count = remaining / value
            if (count > 0) {
                result.add(DenominationCount(label = label, count = count))
                remaining %= value
            }
        }
        return result
    }

    fun roundedDifference(delta: Double): Double {
        if (delta == 0.0) return 0.0
        val sign = if (delta < 0) -1 else 1
        val normalized = normalizeToHryvnia(abs(delta))
        return normalized * sign
    }

    private fun normalizeToHryvnia(value: Double): Double {
        val base = floor(value)
        val fractional = value - base
        val rounded = if (fractional < 0.5) base else ceil(value)
        return rounded
    }
}
