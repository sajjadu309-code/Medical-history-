package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ClinicalLightColorScheme = lightColorScheme(
    primary = DeepTealPrimary,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainerTeal,
    onPrimaryContainer = OnPrimaryContainerTeal,
    inversePrimary = PrimaryFixedDim,
    secondary = OceanicBlueSecondary,
    onSecondary = Color.White,
    secondaryContainer = SecondaryContainerBlue,
    onSecondaryContainer = OnSecondaryContainerBlue,
    tertiary = EmeraldGreenTertiary,
    onTertiary = Color.White,
    tertiaryContainer = TertiaryContainerGreen,
    onTertiaryContainer = Color(0xFFF5FFF6),
    error = CriticalEmergencyRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = CanvasBase,
    onBackground = OnSurfaceDarkNavy,
    surface = SurfaceContainerLowest,
    onSurface = OnSurfaceDarkNavy,
    surfaceVariant = SurfaceContainerHighest,
    onSurfaceVariant = OnSurfaceVariantSlate,
    surfaceTint = DeepTealPrimary,
    inverseSurface = Color(0xFF213145),
    inverseOnSurface = Color(0xFFEAF1FF),
    outline = OutlineSlate,
    outlineVariant = BorderDividerSlate,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest
)

val ClinicalShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),     // Base components (8px)
    medium = RoundedCornerShape(12.dp),   // Buttons / sub-cards (12px)
    large = RoundedCornerShape(16.dp),    // Standard Record Cards (16px)
    extraLarge = RoundedCornerShape(24.dp)// Sheets & Modal Surfaces (24px)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ClinicalLightColorScheme,
        typography = Typography,
        shapes = ClinicalShapes,
        content = content
    )
}
