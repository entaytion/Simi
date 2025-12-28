package ua.entaytion.simi.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val AmoledDarkColorScheme =
        darkColorScheme(
                primary = Color(0xFF90CAF9),
                onPrimary = Color.Black,
                secondary = Color(0xFF80CBC4),
                onSecondary = Color.Black,
                tertiary = Color(0xFFFFAB91),
                background = Color.Black,
                onBackground = Color.White,
                surface = Color.Black,
                onSurface = Color.White,
                surfaceVariant = Color(0xFF1A1A1A),
                onSurfaceVariant = Color(0xFFCCCCCC)
        )

private val LightColorScheme =
        lightColorScheme(
                primary = Color(0xFF1976D2),
                onPrimary = Color.White,
                secondary = Color(0xFF00796B),
                onSecondary = Color.White,
                tertiary = Color(0xFFD32F2F),
                surface = Color.White,
                background = Color.White,
                surfaceVariant = Color(0xFFF8F9FA),
                onSurfaceVariant = Color(0xFF444444)
        )

@Composable
fun SimiTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        // Dynamic color is available on Android 12+
        dynamicColor: Boolean = false,
        content: @Composable () -> Unit
) {
        val colorScheme =
                when {
                        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                                val context = LocalContext.current
                                if (darkTheme) dynamicDarkColorScheme(context)
                                else dynamicLightColorScheme(context)
                        }
                        darkTheme -> AmoledDarkColorScheme
                        else -> LightColorScheme
                }

        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
