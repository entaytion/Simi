@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package ua.entaytion.simi.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontVariation
import ua.entaytion.simi.R

val OverusedGrotesk = FontFamily(
    Font(
        resId = R.font.overusedgrotesk,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        resId = R.font.overusedgrotesk,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        resId = R.font.overusedgrotesk,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        resId = R.font.overusedgrotesk,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    )
)

private val defaultTypography = Typography()
val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = OverusedGrotesk),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = OverusedGrotesk),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = OverusedGrotesk),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = OverusedGrotesk),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = OverusedGrotesk),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = OverusedGrotesk),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = OverusedGrotesk),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = OverusedGrotesk),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = OverusedGrotesk),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = OverusedGrotesk),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = OverusedGrotesk),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = OverusedGrotesk),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = OverusedGrotesk),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = OverusedGrotesk),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = OverusedGrotesk)
)