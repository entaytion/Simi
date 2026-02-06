package ua.entaytion.simi.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

enum class ProductMatrix {
    FRESH,
    NON_FRESH_SHORT,  // < 2 months (up to 59 days)
    NON_FRESH_MEDIUM, // 2-6 months (60-179 days)
    NON_FRESH_LONG,    // > 6 months (180+ days)
    PROHIBITED        // Strong alcohol, tobacco, etc. NO DISCOUNT
}

object ExpirationUtils {
    // Rules: Pair(DaysLeft, DiscountPercent)
    // "DaysLeft" means: if actual days left <= X, apply Discount Y
    
    // Fresh: 10%(3d), 25%(2d), 50%(1d), 75%(0d or less)
    private val freshRules = listOf(0L to 75, 1L to 50, 2L to 25, 3L to 10)
    
    // Non-Fresh Short (<2mo): 10%(5d), 25%(3d), 50%(2d)
    private val nonFreshShortRules = listOf(2L to 50, 3L to 25, 5L to 10)
    
    // Non-Fresh Medium (2-6mo): 10%(15d), 25%(10d), 50%(5d)
    private val nonFreshMediumRules = listOf(5L to 50, 10L to 25, 15L to 10)
    
    // Non-Fresh Long (>6mo): 10%(30d), 25%(20d), 50%(10d)
    private val nonFreshLongRules = listOf(10L to 50, 20L to 25, 30L to 10)

    fun daysBetween(today: LocalDate, targetMillis: Long?): Long? {
        targetMillis ?: return null
        val targetDate = Instant.ofEpochMilli(targetMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return ChronoUnit.DAYS.between(today, targetDate)
    }

    fun discountFor(matrix: ProductMatrix, daysLeft: Long?): Int? {
        daysLeft ?: return null
        if (matrix == ProductMatrix.PROHIBITED) return null
        
        val rules = when (matrix) {
            ProductMatrix.FRESH -> freshRules
            ProductMatrix.NON_FRESH_SHORT -> nonFreshShortRules
            ProductMatrix.NON_FRESH_MEDIUM -> nonFreshMediumRules
            ProductMatrix.NON_FRESH_LONG -> nonFreshLongRules
            ProductMatrix.PROHIBITED -> return null
        }
        
        val entry = rules.firstOrNull { daysLeft <= it.first }
        return entry?.second
    }
    
    // Helper to help user pick matrix based on Total Shelf Life (TSL) in months/days
    fun determineNonFreshMatrix(totalShelfLifeDays: Long): ProductMatrix {
        return when {
            totalShelfLifeDays < 60 -> ProductMatrix.NON_FRESH_SHORT
            totalShelfLifeDays < 180 -> ProductMatrix.NON_FRESH_MEDIUM
            else -> ProductMatrix.NON_FRESH_LONG
        }
    }
}
