@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package ua.entaytion.simi.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val AmoledDarkColorScheme =
        darkColorScheme(
                primary = DiiaPurpleAccent,
                onPrimary = Color.Black,
                primaryContainer = Color(0xFF3C2463),
                onPrimaryContainer = Color(0xFFEDE8F5),
                secondary = DiiaPurpleAccent,
                onSecondary = Color.Black,
                background = DiiaBgDark,
                onBackground = Color(0xFFEDE8F5),
                surface = DiiaSurfaceDark,
                onSurface = Color(0xFFEDE8F5),
                surfaceVariant = DiiaSurfaceVariantDark,
                onSurfaceVariant = Color(0xFFEDE8F5),
                outline = Color(0xFF3C2463),
                outlineVariant = Color(0xFF23123D)
        )

private val LightColorScheme =
        expressiveLightColorScheme().copy(
                primary = DiiaPurple,
                onPrimary = Color.White,
                secondary = DiiaPurple,
                onSecondary = Color.White,
                tertiary = DiiaPurpleLight,
                background = Color.White,
                onBackground = Color(0xFF1B0E30),
                surface = Color.White,
                onSurface = Color(0xFF1B0E30),
                surfaceVariant = Color.White,
                onSurfaceVariant = Color(0xFF1B0E30),
                outline = Color(0xFFEDE8F5),
                outlineVariant = Color(0xFFF3EEF8)
        )

@Composable
fun SimiTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
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

        MaterialExpressiveTheme(
                colorScheme = colorScheme,
                typography = Typography,
                content = content
        )
}
