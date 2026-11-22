package br.edu.iftm.expensetracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import java.util.Locale

val Indigo700 = Color(0xFF4338CA)
val Indigo500 = Color(0xFF6366F1)
val Indigo100 = Color(0xFFE0E7FF)
val Ink = Color(0xFF1A1C2B)
val Paper = Color(0xFFF6F7FB)

val SafeColor = Color(0xFF22A06B)
val WarningColor = Color(0xFFEAB308)
val WarningTextColor = Color(0xFFA16207)
val DangerColor = Color(0xFFE5484D)

val SupermarketColor = Color(0xFF3B82F6)
val FuelColor = Color(0xFFF97316)
val PharmacyColor = Color(0xFF14B8A6)
val OtherColor = Color(0xFF8B5CF6)

val PtBr: Locale = Locale.forLanguageTag("pt-BR")

private val LightColors = lightColorScheme(
    primary = Indigo700,
    onPrimary = Color.White,
    primaryContainer = Indigo100,
    onPrimaryContainer = Ink,
    secondary = Indigo500,
    background = Paper,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    error = DangerColor
)

private val DarkColors = darkColorScheme(
    primary = Indigo500,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2B2A5E),
    onPrimaryContainer = Indigo100,
    secondary = Indigo500,
    background = Color(0xFF111220),
    surface = Color(0xFF1B1C2E),
    error = DangerColor
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
    bodyMedium = TextStyle(fontSize = 14.sp)
)

/**
 * Tema do app em Material 3, com paleta índigo clara ou escura conforme o sistema.
 *
 * @param content telas desenhadas com o tema.
 */
@Composable
fun ExpenseTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
