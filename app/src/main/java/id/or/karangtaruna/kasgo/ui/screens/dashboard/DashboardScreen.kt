package id.or.karangtaruna.kasgo.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(6.dp), color = AppColors.primaryRoyal) {
                    Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = "Kas Go",
                        tint = Color.White, modifier = Modifier.padding(11.dp).size(22.dp))
                }
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text("Kas Go", fontSize = 12.sp, color = AppColors.textSecondaryLight)
                    Text(orgConfig.fullTitle, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
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
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun HeroCard(balance: Long, income: Long, expense: Long) {
    var visible by rememberSaveable { mutableStateOf(true) }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp))
        .background(AppColors.heroPurpleStart).padding(22.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Total Saldo Kas", Modifier.weight(1f), color = AppColors.textOnPurpleMuted, fontSize = 12.sp)
            IconButton(onClick = { visible = !visible }, modifier = Modifier.size(48.dp)) {
                Icon(if (visible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    if (visible) "Sembunyikan saldo" else "Tampilkan saldo", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
        Text(if (visible) Formatters.formatRupiah(balance) else "••••••", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(18.dp))
        Divider(color = Color.White.copy(alpha = .2f), thickness = .5.dp)
        Spacer(Modifier.height(14.dp))
        Text("Bulan ini", color = AppColors.textOnPurpleMuted, fontSize = 11.sp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("Kas masuk" to income, "Kas keluar" to expense).forEach { (label, amount) ->
                Column(Modifier.weight(1f)) {
                    Text(label, color = AppColors.textOnPurpleMuted, fontSize = 11.sp)
                    Text(if (visible) Formatters.formatRupiah(amount) else "••••••", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun QuickMenuGrid(onAddIncome: () -> Unit, onAddExpense: () -> Unit, onPay: () -> Unit, onRecap: () -> Unit) {
    val icons = listOf(Icons.Outlined.ArrowDownward, Icons.Outlined.ArrowUpward, Icons.Outlined.Payments, Icons.Outlined.ReceiptLong)
    val actions = listOf(onAddIncome, onAddExpense, onPay, onRecap)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Masuk", "Keluar", "Bayar", "Rekap").forEachIndexed { index, label ->
            Column(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).clickable { actions[index]() }
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(icons[index], null, tint = AppColors.primaryRoyal, modifier = Modifier.size(23.dp))
                Spacer(Modifier.height(7.dp))
                Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimaryLight)
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
