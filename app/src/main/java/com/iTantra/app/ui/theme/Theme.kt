package com.iTantra.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ITantraColorScheme = lightColorScheme(

    primary = ITantraGreen,
    onPrimary = Color.White,

    primaryContainer = ITantraLightGreen,
    onPrimaryContainer = ITantraDarkGreen,

    background = ITantraBackground,
    onBackground = ITantraText,

    surface = ITantraSurface,
    onSurface = ITantraText,

    surfaceVariant = ITantraLightGreen,
    onSurfaceVariant = ITantraSecondaryText,

    outline = ITantraBorder,

    error = ITantraError
)

@Composable
fun ITantraTheme(
    content: @Composable () -> Unit
) {

    MaterialTheme(
        colorScheme = ITantraColorScheme,
        typography = Typography,
        content = content
    )
}