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
            ProductFreshnessCategory
                    .FRESH // Or default logic? User implies specific list is NON-FRESH.
            // Actually user implies the special logic applies to NON-FRESH.
            // What about FRESH? Assuming standard or no logic specified?
            // "Якщо категорія нефреш... там є такий прикол."
            // Implicitly, fresh might have different rules, but none were provided.
            // I will assume for now we only support this "Non-Fresh" logic or apply it generally if
            // user didn't specify otherwise.
            // But wait, user said "If category non-fresh ... there is a trick."
            // So if it IS fresh, maybe standard logic applies?
            // I'll stick to determining it, but since I have no rules for "Fresh", I'll assume
            // users only care about "Non-Fresh" logic mainly or I'll use it as default if name
            // matches nothing?
            // "Триграммы или ИИ который будет определять".
            // Let's stick to: if matches keywords -> Non-Fresh. Else -> Fresh (No specific rules
            // given, warning user?)
            // Actually, maybe the USER meant ALL items they add are likely these.
            // I will just apply the logic provided if it matches, or fall back to a safe default
            // (maybe same rules? or no discount reminders?).
            // Let's default to applying the rules as they are detailed.
            ProductFreshnessCategory.NON_FRESH
        }
    }

    fun calculateDiscountDates(initialDate: Long?, finalDate: Long): Triple<Long?, Long?, Long?> {
        // If no initial date, we might not be able to calculate duration.
        // But user said initial date is OPTIONAL.
        // "НЕОБОВ'ЯЗЗКОВО але можемо уточнити... ПОЧАТКОВИЙ ТЕРМІН..."
        // If not provided, we can't know total shelf life.
        // What to do?
        // Maybe default to "lowest tier" (<= 59 days)? Safer to assume short shelf life?
        // Or assume from creation date?
        // Let's assume if initial date is missing, we use the "< 2 months" rule as a fallback or
        // simply ask user.
        // User said "Initial date (datepicker), but it is not mandatory".
        // It's safer to treat missing initial date as "Short Shelf Life" (<59 days).

        val start = initialDate ?: (finalDate - TimeUnit.DAYS.toMillis(59)) // Fallback if missing?
        // Actually if missing, we just don't know the "Total Life".
        // But logic DEPENDS on "Total Life" (<59, <179, >=180).
        // I will assume < 59 days logic if unknown, or maybe the user implies we just look at
        // remaining time?
        // "ПОЧАТКОВИЙ ... КОЛИ ЙОГО ВИГОТУВАЛИ А ПОТІМ ЗНИЖКА ЦЕ СКІЛЬКИ ЛИШИЛОСЬ"
        // The *logic tier* depends on *total lifespan*.

        val totalDurationMillis = finalDate - start
        val totalDays = TimeUnit.MILLISECONDS.toDays(totalDurationMillis)

        val days10: Int
        val days25: Int
        val days50: Int

        // 75% NEVER SET according to prompt.

        if (totalDays <= 59) {
            days10 = 5
            days25 = 3
            days50 = 2
        } else if (totalDays <= 179) {
            days10 = 15
            days25 = 10
            days50 = 5
        } else { // >= 180
            days10 = 30
            days25 = 20
            days50 = 10
        }

        val date10 = finalDate - TimeUnit.DAYS.toMillis(days10.toLong())
        val date25 = finalDate - TimeUnit.DAYS.toMillis(days25.toLong())
        val date50 = finalDate - TimeUnit.DAYS.toMillis(days50.toLong())

        return Triple(date10, date25, date50)
    }
}
