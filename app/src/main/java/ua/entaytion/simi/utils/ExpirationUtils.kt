package ua.entaytion.simi.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

enum class ProductType {
    FAST_PERISHABLE,
    SLOW_PERISHABLE,
    CHIPS
}

object ExpirationUtils {
    private val freshRules = listOf(0L to 75, 1L to 50, 2L to 25, 3L to 10)
    private val otherRules = listOf(0L to 75, 10L to 50, 20L to 25, 30L to 10)
    private val chipsRules = listOf(0L to 75, 5L to 50, 10L to 25, 15L to 10)

    fun daysBetween(today: LocalDate, targetMillis: Long?): Long? {
        targetMillis ?: return null
        val targetDate = Instant.ofEpochMilli(targetMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return ChronoUnit.DAYS.between(today, targetDate)
    }

    fun discountFor(type: ProductType, daysLeft: Long?): Int? {
        daysLeft ?: return null
        val rules = when (type) {
            ProductType.FAST_PERISHABLE -> freshRules
            ProductType.SLOW_PERISHABLE -> otherRules
            ProductType.CHIPS -> chipsRules
        }
        val entry = rules.firstOrNull { daysLeft <= it.first }
        return entry?.second
    }
}
