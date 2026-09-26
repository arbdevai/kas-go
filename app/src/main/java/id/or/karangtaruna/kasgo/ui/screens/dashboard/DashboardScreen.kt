package id.or.karangtaruna.kasgo.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.or.karangtaruna.kasgo.core.constants.AppColors
import id.or.karangtaruna.kasgo.core.utils.Formatters
import id.or.karangtaruna.kasgo.data.models.TransactionItem
import id.or.karangtaruna.kasgo.data.repositories.FinanceRepository
import id.or.karangtaruna.kasgo.data.repositories.OrganizationRepository
import id.or.karangtaruna.kasgo.ui.components.CashFlowTrendChart
import id.or.karangtaruna.kasgo.ui.components.TransactionRow
import java.util.Calendar

@Composable
fun DashboardScreen(onNavigateTab: (Int) -> Unit, onAddIncome: () -> Unit, onAddExpense: () -> Unit) {
    val financeRepo = remember { FinanceRepository.get() }
    val orgRepo = remember { OrganizationRepository.get() }
    val transactions by financeRepo.transactions.collectAsState()
    val orgConfig by orgRepo.config.collectAsState()
    val monthStart = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val monthTransactions = transactions.filter { it.occurredAtMillis in monthStart..System.currentTimeMillis() }
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    AnimatedVisibility(entered, enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 24 }) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 20.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = AppColors.primaryRoyal) {
                    Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = "Kas Go",
                        tint = Color.White, modifier = Modifier.padding(11.dp).size(22.dp))
                }
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text("KAS GO", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = AppColors.textSecondaryLight)
                    Text(orgConfig.fullTitle, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Surface(shape = RoundedCornerShape(6.dp), color = Color.White, border = BorderStroke(1.dp, AppColors.borderSubtle)) {
                    IconButton(onClick = { onNavigateTab(2) }) {
                        Icon(Icons.Outlined.ReceiptLong, "Lihat tagihan", tint = AppColors.primaryRoyal)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            HeroCard(
                balance = transactions.sumOf { if (it.isIncome) it.amount else -it.amount },
                income = monthTransactions.filter { it.isIncome }.sumOf { it.amount },
                expense = monthTransactions.filterNot { it.isIncome }.sumOf { it.amount }
            )
            Spacer(Modifier.height(20.dp))
            QuickMenuGrid(onAddIncome, onAddExpense, { onNavigateTab(2) }, { onNavigateTab(1) })
            Spacer(Modifier.height(24.dp))
            CashFlowTrendChart(transactions = transactions)
            Spacer(Modifier.height(24.dp))
            GroupedTransactionList(transactions.sortedByDescending { it.occurredAtMillis }.take(6), { onNavigateTab(1) })
            Spacer(Modifier.height(112.dp))
        }
    }
}

@Composable
fun HeroCard(balance: Long, income: Long, expense: Long) {
    var visible by rememberSaveable { mutableStateOf(true) }
    Column(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(AppColors.primaryRoyal))
            Spacer(Modifier.width(9.dp))
            Text("SALDO ORGANISASI", Modifier.weight(1f), color = AppColors.textSecondaryLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.1.sp)
            IconButton(onClick = { visible = !visible }, modifier = Modifier.size(40.dp)) {
                Icon(if (visible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    if (visible) "Sembunyikan saldo" else "Tampilkan saldo", tint = AppColors.textSecondaryLight, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(3.dp))
        Text(
            if (visible) Formatters.formatRupiah(balance) else "••••••",
            color = AppColors.textPrimaryLight,
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-1).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(22.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            BalanceMetric("Masuk bulan ini", income, visible, true, Modifier.weight(1f))
            Box(Modifier.width(1.dp).height(42.dp).background(AppColors.borderSubtle))
            BalanceMetric("Keluar bulan ini", expense, visible, false, Modifier.weight(1f))
        }
    }
}

@Composable
private fun BalanceMetric(label: String, amount: Long, visible: Boolean, isIncome: Boolean, modifier: Modifier = Modifier) {
    val valueColor = if (isIncome) AppColors.incomeGreen else AppColors.textPrimaryLight
    Column(modifier) {
        Text(label, color = AppColors.textSecondaryLight, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(5.dp))
        Text(
            if (visible) Formatters.formatRupiah(amount) else "••••••",
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun QuickMenuGrid(onAddIncome: () -> Unit, onAddExpense: () -> Unit, onPay: () -> Unit, onRecap: () -> Unit) {
    val icons = listOf(Icons.Outlined.ArrowDownward, Icons.Outlined.ArrowUpward, Icons.Outlined.Payments, Icons.Outlined.ReceiptLong)
    val actions = listOf(onAddIncome, onAddExpense, onPay, onRecap)
    val colors = listOf(AppColors.incomeGreen, AppColors.expenseRed, AppColors.primaryRoyal, AppColors.balanceBlue)
    val backgrounds = listOf(AppColors.incomeGreenBg, AppColors.expenseRedBg, AppColors.surfaceLavender, AppColors.balanceBlueBg)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Masuk", "Keluar", "Bayar", "Rekap").forEachIndexed { index, label ->
            Column(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable { actions[index]() }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(color = backgrounds[index], shape = RoundedCornerShape(12.dp)) {
                    Icon(icons[index], null, tint = colors[index], modifier = Modifier.padding(10.dp).size(21.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimaryLight)
            }
        }
    }
}

@Composable
fun GroupedTransactionList(transactions: List<TransactionItem>, onViewAll: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Transaksi terakhir", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        TextButton(onClick = onViewAll) { Text("Lihat semua", fontSize = 12.sp) }
    }
    Surface(shape = RoundedCornerShape(6.dp), color = Color.White, border = BorderStroke(.75.dp, AppColors.borderSubtle)) {
        Column(Modifier.fillMaxWidth()) {
            if (transactions.isEmpty()) {
                Column(Modifier.padding(24.dp)) {
                    Text("Belum ada transaksi", fontWeight = FontWeight.SemiBold)
                    Text("Catatan kas akan muncul di sini.", color = AppColors.textSecondaryLight, fontSize = 12.sp)
                }
            }
            transactions.forEachIndexed { index, tx ->
                if (index > 0) Divider(color = AppColors.dividerLight, thickness = .5.dp)
                TransactionRow(tx, onViewAll)
            }
        }
    }
}
