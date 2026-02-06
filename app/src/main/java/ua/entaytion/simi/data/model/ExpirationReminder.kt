package ua.entaytion.simi.data.model

data class ExpirationReminder(
        val id: String = "",
        val name: String = "",
        val category: String = ProductFreshnessCategory.FRESH.name, // "FRESH" or "NON_FRESH"
        val initialDate: Long? = null, // Epoch Millis. Optional
        val finalDate: Long = 0L, // Epoch Millis. Required
        val discount10Date: Long? = null,
        val discount25Date: Long? = null,
        val discount50Date: Long? = null,
        val isDiscount10Applied: Boolean = false,
        val isDiscount25Applied: Boolean = false,
        val isDiscount50Applied: Boolean = false,
        val isWrittenOff: Boolean = false
)

enum class ProductFreshnessCategory {
    FRESH, // Default if not matched
    NON_FRESH // "напої, пиво, снеки..."
}
