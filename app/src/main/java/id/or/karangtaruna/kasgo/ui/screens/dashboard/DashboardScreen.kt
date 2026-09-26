package id.or.karangtaruna.kasgo.ui.screens.dashboard

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.or.karangtaruna.kasgo.core.constants.AppColors
import id.or.karangtaruna.kasgo.core.utils.Formatters
import id.or.karangtaruna.kasgo.data.models.TransactionItem
import id.or.karangtaruna.kasgo.data.repositories.FinanceRepository
import id.or.karangtaruna.kasgo.data.repositories.OrganizationRepository

@Composable
fun DashboardScreen(
    onNavigateTab: (Int) -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit
) {
    val financeRepo = remember { FinanceRepository.get() }
    val orgRepo = remember { OrganizationRepository.get() }

    val transactions by financeRepo.transactions.collectAsState()
    val orgConfig by orgRepo.config.collectAsState()

    val totalBalance = financeRepo.totalBalance
    val totalIncome = financeRepo.totalIncome
    val totalExpense = financeRepo.totalExpense

    val monthlyIncome = financeRepo.monthlyIncome
    val expenseByCategory = financeRepo.expenseByCategory
    val cumulativeBalance = financeRepo.cumulativeBalance

    val recentTransactions = transactions.take(6)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 1. HERO CARD
        HeroCard(
            orgTitle = orgConfig.fullTitle,
            balance = totalBalance,
            income = totalIncome,
            expense = totalExpense
        )
        Spacer(modifier = Modifier.height(18.dp))

        // 2. QUICK MENU GRID
        QuickMenuGrid(
            onAddIncome = onAddIncome,
            onAddExpense = onAddExpense,
            onPayIuran = { onNavigateTab(2) },
            onRequestPickup = { onNavigateTab(2) }
        )
        Spacer(modifier = Modifier.height(20.dp))

        // 3. GRAFIK KEUANGAN
        ChartsSection(
            monthlyIncome = monthlyIncome,
            expenseByCategory = expenseByCategory,
            cumulativeBalance = cumulativeBalance
        )
        Spacer(modifier = Modifier.height(20.dp))

        // 4. AKTIVITAS KAS TERBARU
        GroupedTransactionList(
            transactions = recentTransactions,
            onViewAll = { onNavigateTab(1) }
        )
        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
fun HeroCard(
    orgTitle: String,
    balance: Long,
    income: Long,
    expense: Long
) {
    var showBalance by remember { mutableStateOf(true) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = AppColors.primaryRoyal),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(AppColors.heroPurpleStart, AppColors.heroPurpleEnd)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.outlined.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = orgTitle.ifBlank { "Karang Taruna" },
                            fontSize = 12.sp,
                            color = AppColors.textOnPurpleMuted,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.ellipsis
                        )
                        Text(
                            text = "Kas Organisasi",
                            fontSize = 15.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saldo Kas",
                        fontSize = 12.sp,
                        color = AppColors.textOnPurpleMuted,
                        letterSpacing = 0.3.sp
                    )
                    Icon(
                        imageVector = if (showBalance) Icons.rounded.Visibility else Icons.rounded.VisibilityOff,
                        contentDescription = null,
                        tint = AppColors.textOnPurpleMuted,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { showBalance = !showBalance }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (showBalance) Formatters.formatRupiah(balance) else "••••••••",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(18.dp))
                Divider(color = Color.White.copy(alpha = 0.16f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.incomeGreenBg.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.outlined.ArrowDownward,
                                    contentDescription = null,
                                    tint = Color(0xFF6EE7B7),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Kas Masuk",
                                fontSize = 11.sp,
                                color = AppColors.textOnPurpleMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = Formatters.formatRupiah(income),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6EE7B7)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.expenseRedBg.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.outlined.ArrowUpward,
                                    contentDescription = null,
                                    tint = Color(0xFFFCA5A5),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pengeluaran",
                                fontSize = 11.sp,
                                color = AppColors.textOnPurpleMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = Formatters.formatRupiah(expense),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFCA5A5)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickMenuGrid(
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onPayIuran: () -> Unit,
    onRequestPickup: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickMenuItem(
            icon = Icons.outlined.AddCircleOutline,
            title = "Catat Kas",
            subtitle = "Pemasukan",
            badge = "Admin",
            onClick = onAddIncome,
            modifier = Modifier.weight(1f)
        )
        QuickMenuItem(
            icon = Icons.outlined.RemoveCircleOutline,
            title = "Catat",
            subtitle = "Pengeluaran",
            badge = "Admin",
            onClick = onAddExpense,
            modifier = Modifier.weight(1f)
        )
        QuickMenuItem(
            icon = Icons.outlined.QrCode2,
            title = "Bayar",
            subtitle = "Iuran",
            onClick = onPayIuran,
            modifier = Modifier.weight(1f)
        )
        QuickMenuItem(
            icon = Icons.outlined.TwoWheeler,
            title = "Jemput",
            subtitle = "Setoran",
            onClick = onRequestPickup,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Surface(
                modifier = Modifier
                    .size(52.dp)
                    .border(1.dp, AppColors.borderSubtle, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = AppColors.surfaceLavender
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = AppColors.primaryRoyal,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            if (badge != null) {
                Surface(
                    color = AppColors.primarySoft,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(top = 0.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.textPrimaryLight,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = AppColors.textSecondaryLight,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.ellipsis
        )
    }
}

@Composable
fun ChartsSection(
    monthlyIncome: Map<String, Long>,
    expenseByCategory: Map<String, Long>,
    cumulativeBalance: List<Pair<String, Long>>
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(20.dp))
            .border(1.dp, AppColors.borderSubtle, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 4.dp, height = 16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AppColors.primaryRoyal)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Grafik Keuangan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimaryLight
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Segmented Tab
            Surface(
                color = AppColors.surfaceLavender,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp)
                ) {
                    listOf("Pemasukan", "Pengeluaran", "Tren Saldo").forEachIndexed { idx, label ->
                        val isSelected = selectedTab == idx
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = idx },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color.White else Color.Transparent,
                            shadowElevation = if (isSelected) 3.dp else 0.dp
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AppColors.primaryRoyal else AppColors.textSecondaryLight,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    if (monthlyIncome.isEmpty()) {
                        Text(
                            text = "Belum ada data pemasukan kas",
                            fontSize = 12.sp,
                            color = AppColors.textSecondaryLight,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    } else {
                        Column {
                            val maxIncome = (monthlyIncome.values.maxOrNull() ?: 1L).coerceAtLeast(1L)
                            monthlyIncome.forEach { (period, amount) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = period,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.textSecondaryLight,
                                        modifier = Modifier.width(60.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    LinearProgressIndicator(
                                        progress = (amount.toFloat() / maxIncome.toFloat()).coerceIn(0f, 1f),
                                        color = AppColors.incomeGreen,
                                        trackColor = AppColors.incomeGreenBg,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = Formatters.formatRupiah(amount),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.incomeGreen
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    if (expenseByCategory.isEmpty()) {
                        Text(
                            text = "Belum ada data pengeluaran kas",
                            fontSize = 12.sp,
                            color = AppColors.textSecondaryLight,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    } else {
                        val totalExp = expenseByCategory.values.sum().coerceAtLeast(1L)
                        Column {
                            expenseByCategory.forEach { (cat, amount) ->
                                val pct = (amount.toDouble() / totalExp.toDouble()) * 100.0
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = cat,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AppColors.textPrimaryLight
                                        )
                                        Text(
                                            text = "${Formatters.formatRupiah(amount)} (${pct.toInt()}%)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.expenseRed
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = (amount.toFloat() / totalExp.toFloat()).coerceIn(0f, 1f),
                                        color = AppColors.expenseRed,
                                        trackColor = AppColors.expenseRedBg,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    if (cumulativeBalance.isEmpty()) {
                        Text(
                            text = "Belum ada riwayat saldo",
                            fontSize = 12.sp,
                            color = AppColors.textSecondaryLight,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    } else {
                        Column {
                            cumulativeBalance.forEach { (month, running) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = month,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.textSecondaryLight
                                    )
                                    Text(
                                        text = Formatters.formatRupiah(running),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (running >= 0) AppColors.primaryRoyal else AppColors.expenseRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupedTransactionList(
    transactions: List<TransactionItem>,
    onViewAll: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(20.dp))
            .border(1.dp, AppColors.borderSubtle, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(width = 4.dp, height = 16.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(AppColors.primaryRoyal)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Aktivitas Kas Terbaru",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimaryLight
                    )
                }
                TextButton(onClick = onViewAll) {
                    Text(
                        text = "Lihat Semua",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.primaryRoyal
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (transactions.isEmpty()) {
                Text(
                    text = "Belum ada transaksi tercatat",
                    fontSize = 12.sp,
                    color = AppColors.textSecondaryLight,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                transactions.forEachIndexed { idx, tx ->
                    val isIncome = tx.isIncome
                    val color = if (isIncome) AppColors.incomeGreen else AppColors.expenseRed
                    val bgColor = if (isIncome) AppColors.incomeGreenBg else AppColors.expenseRedBg

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(bgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isIncome) Icons.outlined.ArrowDownward else Icons.outlined.ArrowUpward,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.summary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.textPrimaryLight,
                                maxLines = 1,
                                overflow = TextOverflow.ellipsis
                            )
                            Text(
                                text = Formatters.formatTanggal(tx.occurredAtMillis),
                                fontSize = 11.sp,
                                color = AppColors.textSecondaryLight
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${if (isIncome) "+" else "-"}${Formatters.formatRupiah(tx.amount)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                    if (idx < transactions.size - 1) {
                        Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    }
                }
            }
        }
    }
}
