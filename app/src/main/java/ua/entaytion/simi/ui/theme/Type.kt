package ua.entaytion.simi.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ua.entaytion.simi.R

private val NataSans = FontFamily(
    Font(R.font.nata_sans, weight = FontWeight.W100),
    Font(R.font.nata_sans, weight = FontWeight.W200),
    Font(R.font.nata_sans, weight = FontWeight.W300),
    Font(R.font.nata_sans, weight = FontWeight.W400),
    Font(R.font.nata_sans, weight = FontWeight.W500),
    Font(R.font.nata_sans, weight = FontWeight.W600),
    Font(R.font.nata_sans, weight = FontWeight.W700),
    Font(R.font.nata_sans, weight = FontWeight.W800),
    Font(R.font.nata_sans, weight = FontWeight.W900)
)

val Typography = Typography().run {
    Typography(
        displayLarge = displayLarge.copy(fontFamily = NataSans),
        displayMedium = displayMedium.copy(fontFamily = NataSans),
        displaySmall = displaySmall.copy(fontFamily = NataSans),
        headlineLarge = headlineLarge.copy(fontFamily = NataSans),
        headlineMedium = headlineMedium.copy(fontFamily = NataSans),
        headlineSmall = headlineSmall.copy(fontFamily = NataSans),
        titleLarge = titleLarge.copy(fontFamily = NataSans),
        titleMedium = titleMedium.copy(fontFamily = NataSans),
        titleSmall = titleSmall.copy(fontFamily = NataSans),
        bodyLarge = bodyLarge.copy(fontFamily = NataSans, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
        bodyMedium = bodyMedium.copy(fontFamily = NataSans),
        bodySmall = bodySmall.copy(fontFamily = NataSans),
        labelLarge = labelLarge.copy(fontFamily = NataSans),
        labelMedium = labelMedium.copy(fontFamily = NataSans),
        labelSmall = labelSmall.copy(fontFamily = NataSans)
    )
}