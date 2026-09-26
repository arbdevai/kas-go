package id.or.karangtaruna.kasgo.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat
import id.or.karangtaruna.kasgo.core.constants.AppColors

private val Inter = FontFamily(
    Font(R.font.inter_variable, weight = FontWeight.Normal),
    Font(R.font.inter_variable, weight = FontWeight.Medium),
    Font(R.font.inter_variable, weight = FontWeight.SemiBold),
    Font(R.font.inter_variable, weight = FontWeight.Bold),
    Font(R.font.inter_variable, weight = FontWeight.ExtraBold)
)

private val LightColorScheme = lightColorScheme(
    primary = AppColors.primaryRoyal,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = AppColors.surfaceLavender,
    onPrimaryContainer = AppColors.primaryRoyal,
    secondaryContainer = AppColors.surfaceLavender,
    onSecondaryContainer = AppColors.primaryRoyal,
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
        typography = androidx.compose.material3.Typography(defaultFontFamily = Inter),
        shapes = androidx.compose.material3.Shapes(
            small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            large = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
        ),
        content = content
    )
}
