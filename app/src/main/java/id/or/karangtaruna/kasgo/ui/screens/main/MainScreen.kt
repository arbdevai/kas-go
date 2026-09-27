package id.or.karangtaruna.kasgo.ui.screens.main

import android.app.Activity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.Divider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.or.karangtaruna.kasgo.core.constants.AppColors
import id.or.karangtaruna.kasgo.data.models.AppUpdateInfo
import id.or.karangtaruna.kasgo.data.repositories.UserProfileRepository
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
    val context = LocalContext.current
    val userRepo = remember { UserProfileRepository.get() }
    val currentUser by userRepo.currentUser.collectAsState()
    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    var paymentTab by rememberSaveable { mutableIntStateOf(0) }
    var openVerificationQueue by rememberSaveable { mutableStateOf(false) }
    var currentSubScreen by rememberSaveable { mutableStateOf(SubScreen.NONE) }
    var showExitConfirmation by rememberSaveable { mutableStateOf(false) }
    var updateDialogInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }

    LaunchedEffect(Unit) {
        FirebaseSyncService.syncDashboardSummary()
        val update = AppUpdateService.checkUpdate()
        if (update != null && update.hasUpdate) {
            updateDialogInfo = update
        }
    }

    BackHandler {
        when {
            showExitConfirmation -> showExitConfirmation = false
            currentSubScreen != SubScreen.NONE -> currentSubScreen = SubScreen.NONE
            currentTab == 2 && paymentTab != 0 -> paymentTab = 0
            currentTab != 0 -> currentTab = 0
            else -> showExitConfirmation = true
        }
    }

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
                            onClick = {
                                paymentTab = 0
                                currentTab = 2
                            }
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
                    onAddIncome = {
                        if (currentUser.isAdmin) currentSubScreen = SubScreen.ADD_INCOME
                    },
                    onAddExpense = {
                        if (currentUser.isAdmin) currentSubScreen = SubScreen.ADD_EXPENSE
                    },
                    onOpenBills = {
                        paymentTab = 1
                        currentTab = 2
                    },
                    isAdmin = currentUser.isAdmin
                )
                1 -> LedgerScreen()
                2 -> PaymentHubScreen(
                    selectedTab = paymentTab,
                    onTabSelected = { paymentTab = it },
                    openVerificationQueue = openVerificationQueue,
                    onVerificationQueueConsumed = { openVerificationQueue = false }
                )
                3 -> ProfileScreen(
                    onNavigatePaymentSettings = { currentSubScreen = SubScreen.PAYMENT_SETTINGS },
                    onOpenPaymentVerifications = {
                        paymentTab = 0
                        currentTab = 2
                        openVerificationQueue = true
                    }
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

    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            title = { Text("Keluar dari Kas Go?") },
            text = { Text("Kembali sekali lagi untuk menutup aplikasi.") },
            confirmButton = {
                TextButton(onClick = {
                    showExitConfirmation = false
                    (context as? Activity)?.finish()
                }) { Text("Keluar", color = AppColors.expenseRed) }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmation = false }) { Text("Tetap di aplikasi") }
            }
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
            modifier = Modifier.height(48.dp).padding(horizontal = 10.dp),
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
