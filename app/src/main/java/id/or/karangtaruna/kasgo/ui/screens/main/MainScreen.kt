package id.or.karangtaruna.kasgo.ui.screens.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.widthIn
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import id.or.karangtaruna.kasgo.core.constants.AppColors
import id.or.karangtaruna.kasgo.data.models.AppUpdateInfo
import id.or.karangtaruna.kasgo.services.AppUpdateService
import id.or.karangtaruna.kasgo.services.FirebaseSyncService
import id.or.karangtaruna.kasgo.ui.components.UpdateDialog
import id.or.karangtaruna.kasgo.ui.screens.admin.AddExpenseScreen
import id.or.karangtaruna.kasgo.ui.screens.admin.AddIncomeScreen
import id.or.karangtaruna.kasgo.ui.screens.admin.PaymentSettingsScreen
import id.or.karangtaruna.kasgo.ui.screens.dashboard.DashboardScreen
import id.or.karangtaruna.kasgo.ui.screens.ledger.LedgerScreen
import id.or.karangtaruna.kasgo.ui.screens.payments.PaymentHubScreen
import id.or.karangtaruna.kasgo.ui.screens.profile.ProfileScreen

enum class SubScreen { NONE, ADD_INCOME, ADD_EXPENSE, PAYMENT_SETTINGS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    var currentSubScreen by rememberSaveable { mutableStateOf(SubScreen.NONE) }
    var updateDialogInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }
    val hazeState = remember { HazeState() }

    LaunchedEffect(Unit) {
        FirebaseSyncService.syncDashboardSummary()
        val update = AppUpdateService.checkUpdate()
        if (update != null && update.hasUpdate) updateDialogInfo = update
    }

    BackHandler(currentSubScreen != SubScreen.NONE) { currentSubScreen = SubScreen.NONE }
    Crossfade(targetState = currentSubScreen, animationSpec = tween(250), label = "Form navigation") { screen ->
        when (screen) {
            SubScreen.ADD_INCOME -> AddIncomeScreen(onBack = { currentSubScreen = SubScreen.NONE })
            SubScreen.ADD_EXPENSE -> AddExpenseScreen(onBack = { currentSubScreen = SubScreen.NONE })
            SubScreen.PAYMENT_SETTINGS -> PaymentSettingsScreen(onBack = { currentSubScreen = SubScreen.NONE })
            SubScreen.NONE -> {
                Scaffold(containerColor = AppColors.backgroundLight) { contentPadding ->
                    Box(Modifier.fillMaxSize()) {
                        Crossfade(
                            targetState = currentTab,
                            animationSpec = tween(250),
                            label = "Navigation",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = contentPadding.calculateTopPadding())
                                .haze(hazeState, HazeStyle(tint = Color.Transparent, blurRadius = 24.dp, noiseFactor = .08f))
                        ) { tab ->
                            when (tab) {
                                0 -> DashboardScreen(
                                    onNavigateTab = { currentTab = it },
                                    onAddIncome = { currentSubScreen = SubScreen.ADD_INCOME },
                                    onAddExpense = { currentSubScreen = SubScreen.ADD_EXPENSE }
                                )
                                1 -> LedgerScreen()
                                2 -> PaymentHubScreen(onOpenSettings = { currentSubScreen = SubScreen.PAYMENT_SETTINGS })
                                else -> ProfileScreen(onNavigatePaymentSettings = { currentSubScreen = SubScreen.PAYMENT_SETTINGS })
                            }
                        }

                        val glassShape = RoundedCornerShape(24.dp)
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .fillMaxWidth()
                                .widthIn(max = 460.dp)
                                .shadow(
                                    elevation = 12.dp,
                                    shape = glassShape,
                                    ambientColor = Color(0x1A211544),
                                    spotColor = Color(0x26211544)
                                )
                                .clip(glassShape)
                                .hazeChild(
                                    hazeState,
                                    shape = glassShape,
                                    style = HazeStyle(tint = Color.White.copy(alpha = .86f), blurRadius = 20.dp, noiseFactor = .02f)
                                )
                                .border(1.dp, AppColors.borderSubtle, glassShape),
                            shape = glassShape,
                            color = Color.White.copy(alpha = .90f),
                            contentColor = AppColors.textPrimaryLight
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                AppNavigationItem(currentTab == 0, Icons.Outlined.Dashboard, Icons.Rounded.Dashboard, "Beranda") { currentTab = 0 }
                                AppNavigationItem(currentTab == 1, Icons.Outlined.ReceiptLong, Icons.Rounded.ReceiptLong, "Buku Kas") { currentTab = 1 }
                                AppNavigationItem(currentTab == 2, Icons.Outlined.Payments, Icons.Rounded.Payments, "Bayar Kas") { currentTab = 2 }
                                AppNavigationItem(currentTab == 3, Icons.Outlined.Person, Icons.Rounded.Person, "Akun") { currentTab = 3 }
                            }
                        }
                    }
                }
            }
        }
    }

    updateDialogInfo?.let { info ->
        UpdateDialog(info = info, onDismiss = { updateDialogInfo = null })
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RowScope.AppNavigationItem(
    selected: Boolean,
    icon: ImageVector,
    activeIcon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .weight(if (selected) 1.45f else 1f)
            .height(54.dp)
            .padding(horizontal = 3.dp),
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) AppColors.surfaceLavender else Color.Transparent,
        contentColor = if (selected) AppColors.primaryRoyal else AppColors.textMutedLight
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (selected) activeIcon else icon,
                contentDescription = if (selected) null else "$label tab",
                modifier = Modifier.size(21.dp)
            )
            if (selected) {
                androidx.compose.foundation.layout.Spacer(Modifier.width(7.dp))
                Text(
                    label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
