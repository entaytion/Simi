package ua.entaytion.simi.data.model

import ua.entaytion.simi.utils.ProductMatrix

data class ExpirationThreat(
    val id: String = "",
    val name: String = "",
    val matrix: ProductMatrix = ProductMatrix.FRESH,
    val expirationDate: Long = 0L,
    val proofImageUrls: List<String> = emptyList(), // Store multiple images
    val isResolved: Boolean = false,
    val resolvedAt: Long? = null,
    val addedAt: Long = System.currentTimeMillis(),
    
    // Tracking applied actions (milestones)
    val isDiscount10Applied: Boolean = false,
    val isDiscount25Applied: Boolean = false,
    val isDiscount50Applied: Boolean = false
)
