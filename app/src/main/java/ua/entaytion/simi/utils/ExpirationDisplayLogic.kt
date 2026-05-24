package ua.entaytion.simi.utils

import java.util.concurrent.TimeUnit
import ua.entaytion.simi.data.model.ProductFreshnessCategory

object ExpirationDisplayLogic {

    private val NON_FRESH_KEYWORDS =
            listOf(
                    "напої",
                    "пиво",
                    "снек",
                    "бакалія",
                    "маргарин",
                    "заморожен",
                    "м'ясо заморожене",
                    "горіх",
                    "сухофрукт",
                    "вафл",
                    "тарталетк",
                    "грісіні",
                    "крутон",
                    "сухар",
                    "панірован",
                    "грінки",
                    "хлібці",
                    "сушки",
                    "соломка",
                    "кекс",
                    "панетонне",
                    "тварин"
            )

    fun determineCategory(name: String): ProductFreshnessCategory {
        val lowerName = name.lowercase()
        return if (NON_FRESH_KEYWORDS.any { lowerName.contains(it) }) {
            ProductFreshnessCategory.NON_FRESH
        } else {
            ProductFreshnessCategory.FRESH
        }
    }

    fun calculateDiscountDates(category: ProductFreshnessCategory, initialDate: Long?, finalDate: Long): Triple<Long?, Long?, Long?> {
        val date10: Long? = null // Знижка 10% скасована
        val date25: Long
        val date50: Long

        if (category == ProductFreshnessCategory.FRESH) {
            // Для ФРЕШ: 25% за 3 дні, 50% за 1 день
            date25 = finalDate - TimeUnit.DAYS.toMillis(3)
            date50 = finalDate - TimeUnit.DAYS.toMillis(1)
        } else {
            // Для НЕ ФРЕШ: залежно від загального терміну придатності
            val start = initialDate ?: (finalDate - TimeUnit.DAYS.toMillis(59))
            val totalDurationMillis = finalDate - start
            val totalDays = TimeUnit.MILLISECONDS.toDays(totalDurationMillis)

            val days25: Int
            val days50: Int

            if (totalDays <= 59) {
                // до 59 днів: 25% за 5 днів, 50% за 2 дні
                days25 = 5
                days50 = 2
            } else if (totalDays <= 179) {
                // від 60 до 179 днів: 25% за 15 днів, 50% за 5 днів
                days25 = 15
                days50 = 5
            } else {
                // від 180 днів: 25% за 30 днів, 50% за 10 днів
                days25 = 30
                days50 = 10
            }

            date25 = finalDate - TimeUnit.DAYS.toMillis(days25.toLong())
            date50 = finalDate - TimeUnit.DAYS.toMillis(days50.toLong())
        }

        return Triple(date10, date25, date50)
    }
}
