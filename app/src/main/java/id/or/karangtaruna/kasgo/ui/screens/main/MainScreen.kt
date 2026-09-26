package id.or.karangtaruna.kasgo.ui.screens.main

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
    var currentTab by remember { mutableIntStateOf(0) }
    var currentSubScreen by remember { mutableStateOf(SubScreen.NONE) }
    var showAddModal by remember { mutableStateOf(false) }
    var updateDialogInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }

    LaunchedEffect(Unit) {
        FirebaseSyncService.syncDashboardSummary()
        val update = AppUpdateService.checkUpdate()
        if (update != null && update.hasUpdate) {
            updateDialogInfo = update
        }
    }

    when (currentSubScreen) {
        SubScreen.ADD_INCOME -> {
            AddIncomeScreen(onBack = { currentSubScreen = SubScreen.NONE })
            return
        }
        SubScreen.ADD_EXPENSE -> {
            AddExpenseScreen(onBack = { currentSubScreen = SubScreen.NONE })
            return
        }
        SubScreen.PAYMENT_SETTINGS -> {
            PaymentSettingsScreen(onBack = { currentSubScreen = SubScreen.NONE })
            return
        }
        SubScreen.NONE -> {}
    }

    Scaffold(
        containerColor = AppColors.backgroundLight,
        floatingActionButton = {
            if (currentTab == 0 || currentTab == 1) {
                FloatingActionButton(
                    onClick = { showAddModal = true },
                    containerColor = AppColors.primaryRoyal,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(6.dp),
                    modifier = Modifier.padding(bottom = 76.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Catat Kas", modifier = Modifier.size(28.dp))
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.borderSubtle),
                    shadowElevation = 14.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NavDockItem(
                            index = 0,
                            selected = currentTab == 0,
                            icon = Icons.Outlined.Dashboard,
                            activeIcon = Icons.Rounded.Dashboard,
                            label = "Beranda",
                            onClick = { currentTab = 0 }
                        )
                        NavDockItem(
                            index = 1,
                            selected = currentTab == 1,
                            icon = Icons.Outlined.ReceiptLong,
                            activeIcon = Icons.Rounded.ReceiptLong,
                            label = "Buku Kas",
                            onClick = { currentTab = 1 }
                        )
                        NavDockItem(
                            index = 2,
                            selected = currentTab == 2,
                            icon = Icons.Outlined.Payments,
                            activeIcon = Icons.Rounded.Payments,
                            label = "Bayar Kas",
                            onClick = { currentTab = 2 }
                        )
                        NavDockItem(
                            index = 3,
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
            when (currentTab) {
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

    // Modal Pilih Catat Kas Masuk / Pengeluaran
    if (showAddModal) {
        ModalBottomSheet(
            onDismissRequest = { showAddModal = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(width = 4.dp, height = 18.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(AppColors.primaryRoyal)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Catat Transaksi Kas",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimaryLight
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))

                // Opsi Kas Masuk
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            showAddModal = false
                            currentSubScreen = SubScreen.ADD_INCOME
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AppColors.incomeGreenBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.ArrowDownward, null, tint = AppColors.incomeGreen, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Catat Kas Masuk", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimaryLight)
                        Text("Iuran warga dan setoran kas", fontSize = 12.sp, color = AppColors.textSecondaryLight)
                    }
                }

                Divider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                // Opsi Pengeluaran
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            showAddModal = false
                            currentSubScreen = SubScreen.ADD_EXPENSE
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AppColors.expenseRedBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.ArrowUpward, null, tint = AppColors.expenseRed, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Catat Pengeluaran", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimaryLight)
                        Text("Belanja perlengkapan dan kegiatan kas", fontSize = 12.sp, color = AppColors.textSecondaryLight)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
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
    index: Int,
    selected: Boolean,
    icon: ImageVector,
    activeIcon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) AppColors.surfaceLavender else Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
