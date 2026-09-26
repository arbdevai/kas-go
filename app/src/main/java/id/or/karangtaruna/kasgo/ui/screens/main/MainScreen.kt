package id.or.karangtaruna.kasgo.ui.screens.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

enum class SubScreen {
    NONE, ADD_INCOME, ADD_EXPENSE, PAYMENT_SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    var currentSubScreen by rememberSaveable { mutableStateOf(SubScreen.NONE) }
    var updateDialogInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }

    LaunchedEffect(Unit) {
        FirebaseSyncService.syncDashboardSummary()
        val update = AppUpdateService.checkUpdate()
        if (update != null && update.hasUpdate) {
            updateDialogInfo = update
        }
    }

    BackHandler(currentSubScreen != SubScreen.NONE) { currentSubScreen = SubScreen.NONE }

    Crossfade(targetState = currentSubScreen, animationSpec = tween(300), label = "Form navigation") { screen ->
        when (screen) {
            SubScreen.ADD_INCOME -> AddIncomeScreen(onBack = { currentSubScreen = SubScreen.NONE })
            SubScreen.ADD_EXPENSE -> AddExpenseScreen(onBack = { currentSubScreen = SubScreen.NONE })
            SubScreen.PAYMENT_SETTINGS -> PaymentSettingsScreen(onBack = { currentSubScreen = SubScreen.NONE })
            SubScreen.NONE -> {
                Scaffold(
                    containerColor = AppColors.backgroundLight,
                    bottomBar = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.borderSubtle),
                                shadowElevation = 6.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    NavDockItem(
                                        selected = currentTab == 0,
                                        icon = Icons.Outlined.Dashboard,
                                        activeIcon = Icons.Rounded.Dashboard,
                                        label = "Beranda",
                                        onClick = { currentTab = 0 }
                                    )
                                    NavDockItem(
                                        selected = currentTab == 1,
                                        icon = Icons.Outlined.ReceiptLong,
                                        activeIcon = Icons.Rounded.ReceiptLong,
                                        label = "Buku Kas",
                                        onClick = { currentTab = 1 }
                                    )
                                    NavDockItem(
                                        selected = currentTab == 2,
                                        icon = Icons.Outlined.Payments,
                                        activeIcon = Icons.Rounded.Payments,
                                        label = "Bayar Kas",
                                        onClick = { currentTab = 2 }
                                    )
                                    NavDockItem(
                                        selected = currentTab == 3,
                                        icon = Icons.Outlined.Person,
                                        activeIcon = Icons.Rounded.Person,
                                        label = "Akun",
                                        onClick = { currentTab = 3 }
                                    )
                                }
                            }
                        }
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        Crossfade(targetState = currentTab, animationSpec = tween(300), label = "Navigation") { tab ->
                            when (tab) {
                                0 -> DashboardScreen(
                                    onNavigateTab = { currentTab = it },
                                    onAddIncome = { currentSubScreen = SubScreen.ADD_INCOME },
                                    onAddExpense = { currentSubScreen = SubScreen.ADD_EXPENSE }
                                )
                                1 -> LedgerScreen()
                                2 -> PaymentHubScreen(
                                    onOpenSettings = { currentSubScreen = SubScreen.PAYMENT_SETTINGS }
                                )
                                3 -> ProfileScreen(
                                    onNavigatePaymentSettings = { currentSubScreen = SubScreen.PAYMENT_SETTINGS }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (updateDialogInfo != null) {
        UpdateDialog(
            info = updateDialogInfo!!,
            onDismiss = { updateDialogInfo = null }
        )
    }
}

@Composable
fun NavDockItem(
    selected: Boolean,
    icon: ImageVector,
    activeIcon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) AppColors.surfaceLavender else Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.height(48.dp).widthIn(min = 48.dp).padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (selected) activeIcon else icon,
                contentDescription = label,
                tint = if (selected) AppColors.primaryRoyal else AppColors.textSecondaryLight,
                modifier = Modifier.size(20.dp)
            )
            AnimatedVisibility(visible = selected) {
                Row {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.primaryRoyal
                    )
                }
            }
        }
    }
}
