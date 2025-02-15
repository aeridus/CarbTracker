package com.aerobush.carbtracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aerobush.carbtracker.AppViewModelProvider
import com.aerobush.carbtracker.data.ThemeMode
import com.aerobush.carbtracker.ui.item.CarbTimeItemViewModel

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    secondary = Blue80,
    tertiary = Yellow80,
    error = Red80,
    background = OffBlack,
    onBackground = OffWhite,
    onPrimary = OffBlack,
    onSecondary = OffWhite
)

private val LightColorScheme = lightColorScheme(
    primary = Green80,
    secondary = Blue80,
    tertiary = Yellow80,
    error = Red80,
    background = OffWhite,
    onBackground = OffBlack,
    onPrimary = OffBlack,
    onSecondary = OffWhite

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun CarbTrackerTheme(
    isSystemInDarkTheme: Boolean = isSystemInDarkTheme(),
    viewModel: CarbTimeItemViewModel = viewModel(factory = AppViewModelProvider.Factory),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val themeUiState = viewModel.themeUiState.collectAsState()
    val darkTheme = when (themeUiState.value.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        else -> isSystemInDarkTheme
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}