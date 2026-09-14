package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppThemeOption(val titleUrdu: String, val subtitle: String) {
    CLASSIC_NAVY("کلاسک نیوی و سفید", "Classic Academic Navy"),
    INSTITUTIONAL_GREEN("قومی و ادارتی سبز", "Institutional Forest Green"),
    ROYAL_DARK_GOLD("شاہی گہرا سلیٹی و سنہری", "Royal Dark Gold")
}

// 1. تھیم اول: Classic Academic Navy & White
private val ClassicNavyColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = EmeraldPresent,
    onSecondary = Color.White,
    secondaryContainer = EmeraldLight,
    onSecondaryContainer = EmeraldDark,
    tertiary = AmberLeave,
    onTertiary = Color.White,
    tertiaryContainer = AmberLight,
    error = CrimsonAbsent,
    onError = Color.White,
    errorContainer = CrimsonLight,
    onErrorContainer = CrimsonDark,
    background = SlateBackground,
    onBackground = SlateTextPrimary,
    surface = SlateSurface,
    onSurface = SlateTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = SlateTextSecondary,
    outline = SlateBorder
)

// 2. تھیم دوم: Institutional Forest Green (قومی ادارتی سبز)
private val InstitutionalGreenColorScheme = lightColorScheme(
    primary = InstitutionalGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = InstitutionalGreenContainer,
    onPrimaryContainer = Color(0xFF042F2C),
    secondary = EmeraldPresent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF064E3B),
    tertiary = AmberLeave,
    onTertiary = Color.White,
    tertiaryContainer = AmberLight,
    error = CrimsonAbsent,
    onError = Color.White,
    errorContainer = CrimsonLight,
    onErrorContainer = CrimsonDark,
    background = InstitutionalGreenBackground,
    onBackground = Color(0xFF0F172A),
    surface = InstitutionalGreenSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE6F4EA),
    onSurfaceVariant = Color(0xFF2D3748),
    outline = Color(0xFFCBD5E1)
)

// 3. تھیم سوم: Royal Dark & Gold (شاہی گہرا سلیٹی و سنہری)
private val RoyalDarkGoldColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = Color(0xFF451A03),
    primaryContainer = Color(0xFF78350F),
    onPrimaryContainer = Color(0xFFFEF3C7),
    secondary = Color(0xFF34D399),
    onSecondary = Color(0xFF064E3B),
    secondaryContainer = Color(0xFF065F46),
    onSecondaryContainer = Color(0xFFD1FAE5),
    tertiary = Color(0xFF60A5FA),
    onTertiary = Color(0xFF1E3A8A),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    background = DarkBackground,
    onBackground = Color(0xFFF8FAFC),
    surface = DarkSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = DarkBorder
)

// Theme State Holder
object AppThemeState {
    var currentTheme by mutableStateOf(AppThemeOption.INSTITUTIONAL_GREEN)
}

val LocalAppThemeOption = compositionLocalOf { AppThemeOption.INSTITUTIONAL_GREEN }

@Composable
fun CollegeAttendanceTheme(
    selectedTheme: AppThemeOption = AppThemeState.currentTheme,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = when (selectedTheme) {
        AppThemeOption.CLASSIC_NAVY -> ClassicNavyColorScheme
        AppThemeOption.INSTITUTIONAL_GREEN -> InstitutionalGreenColorScheme
        AppThemeOption.ROYAL_DARK_GOLD -> RoyalDarkGoldColorScheme
    }

    CompositionLocalProvider(
        LocalAppThemeOption provides selectedTheme,
        androidx.compose.material3.LocalTextStyle provides androidx.compose.ui.text.TextStyle(
            fontFamily = JameelNooriFamily
        )
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    selectedTheme: AppThemeOption = AppThemeState.currentTheme,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = CollegeAttendanceTheme(selectedTheme, darkTheme, dynamicColor, content)

