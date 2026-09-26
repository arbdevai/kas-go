package id.or.karangtaruna.kasgo.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import id.or.karangtaruna.kasgo.core.constants.AppColors

private val LightColorScheme = lightColorScheme(
    primary = AppColors.primaryRoyal,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = AppColors.primarySoft,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    background = AppColors.backgroundLight,
    surface = AppColors.surfaceLight,
    surfaceVariant = AppColors.surfaceMuted,
    onSurfaceVariant = AppColors.textSecondaryLight,
    outline = AppColors.borderSubtle,
    outlineVariant = AppColors.dividerLight,
    onBackground = AppColors.textPrimaryLight,
    onSurface = AppColors.textPrimaryLight,
    error = AppColors.expenseRed
)

@Composable
fun KasGoTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = AppColors.backgroundLight.toArgb()
                window.navigationBarColor = AppColors.backgroundLight.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
