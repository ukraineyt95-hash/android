package com.zaneschepke.wireguardautotunnel.ui.theme

import androidx.compose.ui.graphics.Color

val OffWhite = Color(0xFFF2F2F4)
val CoolGray = Color(0xFF8D9D9F)
val LightGrey = Color(0xFFECEDEF)
val Aqua = Color(0xFF76BEBD)
val Plantation = Color(0xFF2E3538)
val Shark = Color(0xFF21272A)
val BalticSea = Color(0xFF1C1B1F)

// Midnight Blue palette
val MidnightBlueBackground = Color(0xFF07111F)
val MidnightBlueSurface = Color(0xFF12233B)
val MidnightBlueSurfaceVariant = Color(0xFF1C2E4A)
val MidnightBluePrimary = Color(0xFF4DA3FF)
val MidnightBlueSecondary = Color(0xFF213A5E)
val MidnightBlueText = Color(0xFFEAF4FF)
val MidnightBlueMutedText = Color(0xFFB7C3D4)
val MidnightBlueOutline = Color(0xFF35527A)

// amoled
val ElectricTeal = Color(0xFF4DD0E1)

// Status colors
val SilverTree = Color(0xFF6DB58B)
val AlertRed = Color(0xFFCF6679)
val Straw = Color(0xFFD4C483)

val Disabled = CoolGray.copy(alpha = 0.4f)

// Other colors
val ConfigHeaderColor = Color(0xFFBB86FC)
val ConfigKeyColor = Color(0xFF03DAC5)
val Heart = Color(0xFFDB61A2)

sealed class ThemeColors(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val secondary: Color,
    val onSurface: Color,
    val onBackground: Color,
    val outline: Color,
) {

    data object Light :
        ThemeColors(
            background = Color(0xFFF5F8FF),
            surface = Color(0xFFFFFFFF),
            primary = MidnightBluePrimary,
            secondary = Color(0xFFD9E7FF),
            onSurface = Color(0xFF0F172A),
            outline = Color(0xFF94A3B8),
            onBackground = Color(0xFF0F172A),
        )

    data object Dark :
        ThemeColors(
            background = MidnightBlueBackground,
            surface = MidnightBlueSurface,
            primary = MidnightBluePrimary,
            secondary = MidnightBlueSecondary,
            onSurface = MidnightBlueText,
            outline = MidnightBlueOutline,
            onBackground = MidnightBlueText,
        )
}
