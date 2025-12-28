package ua.entaytion.simi.data.model

data class ExpirationReminder(
        val id: String,
        val name: String,
        val category: String, // "FRESH" or "NON_FRESH"
        val initialDate: Long?, // Epoch Millis. Optional
        val finalDate: Long, // Epoch Millis. Required
        val discount10Date: Long?,
        val discount25Date: Long?,
        val discount50Date: Long?,
        val isDiscount10Applied: Boolean = false,
        val isDiscount25Applied: Boolean = false,
        val isDiscount50Applied: Boolean = false,
        val isWrittenOff: Boolean = false
)

enum class ProductFreshnessCategory {
    FRESH, // Default if not matched
    NON_FRESH // "напої, пиво, снеки..."
}
