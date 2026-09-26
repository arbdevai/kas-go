package id.or.karangtaruna.kasgo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.or.karangtaruna.kasgo.core.constants.AppColors

/** Shared field: fixed external label, uninterrupted border, and animated focus. */
@Composable
fun KasInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = false,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val border by animateColorAsState(
        if (focused) AppColors.primaryRoyal else AppColors.borderSubtle,
        tween(180), label = "Input focus"
    )
    Column(modifier) {
        if (label != null) {
            CompositionLocalProvider(LocalContentColor provides AppColors.textSecondaryLight) {
                ProvideTextStyle(MaterialTheme.typography.labelMedium) { label() }
            }
            Spacer(Modifier.height(6.dp))
        }
        TextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().border(if (focused) 1.5.dp else 1.dp, border, RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            placeholder = placeholder, leadingIcon = leadingIcon, trailingIcon = trailingIcon,
            singleLine = singleLine, readOnly = readOnly, keyboardOptions = keyboardOptions,
            minLines = minLines, maxLines = maxLines, interactionSource = interaction,
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = AppColors.textPrimaryLight, unfocusedTextColor = AppColors.textPrimaryLight,
                focusedLeadingIconColor = AppColors.primaryRoyal,
                unfocusedLeadingIconColor = AppColors.textSecondaryLight
            )
        )
    }
}
