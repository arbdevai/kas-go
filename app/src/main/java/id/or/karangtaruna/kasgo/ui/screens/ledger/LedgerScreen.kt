package id.or.karangtaruna.kasgo.ui.screens.ledger

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.or.karangtaruna.kasgo.core.constants.AppColors
import id.or.karangtaruna.kasgo.core.utils.AppToast
import id.or.karangtaruna.kasgo.core.utils.Formatters
import id.or.karangtaruna.kasgo.data.models.LedgerType
import id.or.karangtaruna.kasgo.data.models.TransactionItem
import id.or.karangtaruna.kasgo.data.repositories.BillingRepository
import id.or.karangtaruna.kasgo.data.repositories.FinanceRepository
import id.or.karangtaruna.kasgo.data.repositories.UserProfileRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen() {
    val financeRepo = remember { FinanceRepository.get() }
    val billingRepo = remember { BillingRepository.get() }
    val userRepo = remember { UserProfileRepository.get() }

    val transactions by financeRepo.transactions.collectAsState()
    val bills by billingRepo.allBills.collectAsState()
    val currentUser by userRepo.currentUser.collectAsState()

    var mainTab by remember { mutableIntStateOf(0) } // 0: Mutasi Kas, 1: Rekapitulasi
    var filterType by remember { mutableStateOf<LedgerType?>(null) } // null: Semua, INCOME, EXPENSE

    var selectedTx by remember { mutableStateOf<TransactionItem?>(null) }
    var txToEdit by remember { mutableStateOf<TransactionItem?>(null) }

    val filteredList = remember(transactions, filterType) {
        if (filterType == null) transactions else transactions.filter { it.entryType == filterType }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Buku Kas",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppColors.textPrimaryLight,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Riwayat mutasi kas dan rekapitulasi iuran",
                    fontSize = 12.sp,
                    color = AppColors.textSecondaryLight
                )
            }
            Surface(
                color = AppColors.surfaceLavender,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "${transactions.size} Transaksi",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.primaryRoyal,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Tab Switcher: Mutasi Kas vs Rekapitulasi
        Surface(
            color = AppColors.surfaceLavender,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(3.dp)) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { mainTab = 0 },
                    shape = RoundedCornerShape(11.dp),
                    color = if (mainTab == 0) Color.White else Color.Transparent,
                    shadowElevation = if (mainTab == 0) 3.dp else 0.dp
                ) {
                    Text(
                        text = "Mutasi Kas",
                        fontSize = 12.sp,
                        fontWeight = if (mainTab == 0) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (mainTab == 0) AppColors.primaryRoyal else AppColors.textSecondaryLight,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { mainTab = 1 },
                    shape = RoundedCornerShape(11.dp),
                    color = if (mainTab == 1) Color.White else Color.Transparent,
                    shadowElevation = if (mainTab == 1) 3.dp else 0.dp
                ) {
                    Text(
                        text = "Rekapitulasi",
                        fontSize = 12.sp,
                        fontWeight = if (mainTab == 1) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (mainTab == 1) AppColors.primaryRoyal else AppColors.textSecondaryLight,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (mainTab == 0) {
            // TAB 1: MUTASI KAS
            // Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChipItem(
                        label = "Semua Transaksi",
                        selected = filterType == null,
                        color = AppColors.primaryRoyal,
                        onClick = { filterType = null }
                    )
                }
                item {
                    FilterChipItem(
                        label = "Kas Masuk",
                        selected = filterType == LedgerType.INCOME,
                        color = AppColors.incomeGreen,
                        onClick = { filterType = LedgerType.INCOME }
                    )
                }
                item {
                    FilterChipItem(
                        label = "Pengeluaran",
                        selected = filterType == LedgerType.EXPENSE,
                        color = AppColors.expenseRed,
                        onClick = { filterType = LedgerType.EXPENSE }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(AppColors.surfaceLavender),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.outlined.ReceiptLong,
                                contentDescription = null,
                                tint = AppColors.primaryRoyal,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum ada transaksi kas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AppColors.textPrimaryLight
                        )
                        Text(
                            text = "Catatan mutasi kas akan muncul di sini",
                            fontSize = 12.sp,
                            color = AppColors.textSecondaryLight
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList, key = { it.id }) { tx ->
                        TransactionRowCard(
                            tx = tx,
                            onClick = { selectedTx = tx }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(96.dp))
                    }
                }
            }
        } else {
            // TAB 2: REKAPITULASI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Rekap Saldo Global
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(20.dp)),
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
                            .padding(18.dp)
                    ) {
                        Column {
                            Text(
                                text = "REKAP TOTAL SALDO KAS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.textOnPurpleMuted,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Formatters.formatRupiah(financeRepo.totalBalance),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Total Kas Masuk", fontSize = 11.sp, color = AppColors.textOnPurpleMuted)
                                    Text(
                                        text = Formatters.formatRupiah(financeRepo.totalIncome),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6EE7B7)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Total Pengeluaran", fontSize = 11.sp, color = AppColors.textOnPurpleMuted)
                                    Text(
                                        text = Formatters.formatRupiah(financeRepo.totalExpense),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFCA5A5)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Rekapitulasi Tagihan Iuran Warga
                Text(
                    text = "Rekapitulasi Tagihan Iuran",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimaryLight
                )
                Spacer(modifier = Modifier.height(10.dp))

                val recaps = billingRepo.getAllRecaps()
                if (recaps.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.borderSubtle)
                    ) {
                        Text(
                            text = "Belum ada tagihan iuran yang diterbitkan.",
                            fontSize = 12.sp,
                            color = AppColors.textSecondaryLight,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    recaps.forEach { r ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.borderSubtle)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = r.bill.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.textPrimaryLight
                                    )
                                    Text(
                                        text = "${Formatters.formatRupiah(r.bill.amount)} / warga",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.primaryRoyal
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = (r.collectionPercentage.toFloat() / 100f).coerceIn(0f, 1f),
                                    color = AppColors.incomeGreen,
                                    trackColor = Color(0xFFF1F5F9),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Lunas: ${r.paidCount}/${r.totalMembers} (${r.collectionPercentage.toInt()}%)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.incomeGreen
                                    )
                                    Text(
                                        text = "Terkumpul: ${Formatters.formatRupiah(r.totalCollected)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.textPrimaryLight
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Rekap Pengeluaran per Kategori
                Text(
                    text = "Rekap Pengeluaran per Kategori",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimaryLight
                )
                Spacer(modifier = Modifier.height(10.dp))

                val expenseCats = financeRepo.expenseByCategory
                if (expenseCats.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.borderSubtle)
                    ) {
                        Text(
                            text = "Belum ada catatan pengeluaran kas.",
                            fontSize = 12.sp,
                            color = AppColors.textSecondaryLight,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.borderSubtle)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val totalOut = financeRepo.totalExpense.coerceAtLeast(1L)
                            expenseCats.forEach { (cat, amount) ->
                                val pct = (amount.toDouble() / totalOut.toDouble()) * 100.0
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = cat, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = "${Formatters.formatRupiah(amount)} (${pct.toInt()}%)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.expenseRed
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = (amount.toFloat() / totalOut.toFloat()).coerceIn(0f, 1f),
                                        color = AppColors.expenseRed,
                                        trackColor = AppColors.expenseRedBg,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }

    // Modal Sheet Detail Transaksi
    if (selectedTx != null) {
        val tx = selectedTx!!
        ModalBottomSheet(
            onDismissRequest = { selectedTx = null },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detail Transaksi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimaryLight
                    )
                    IconButton(onClick = { selectedTx = null }) {
                        Icon(Icons.rounded.Close, null, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                // Card Nominal
                Surface(
                    color = if (tx.isIncome) AppColors.incomeGreenBg else AppColors.expenseRedBg,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (tx.isIncome) "KAS MASUK" else "PENGELUARAN KAS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (tx.isIncome) AppColors.incomeGreen else AppColors.expenseRed,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${if (tx.isIncome) "+" else "-"}${Formatters.formatRupiah(tx.amount)}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = if (tx.isIncome) AppColors.incomeGreen else AppColors.expenseRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AuditRowItem("Keperluan / Uraian", tx.summary)
                AuditRowItem("Waktu Transaksi", Formatters.formatTanggalDanJam(tx.occurredAtMillis))
                AuditRowItem("Dicatat Oleh", tx.recordedByName)
                AuditRowItem("Metode Pembayaran", tx.paymentMethod)
                if (!tx.category.isNullOrBlank()) AuditRowItem("Kategori", tx.category!!)
                if (!tx.period.isNullOrBlank()) AuditRowItem("Periode", tx.period!!)

                if (tx.wasEdited) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = Color(0xFFFFFBEB),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Catatan Koreksi",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Diedit oleh: ${tx.editedByName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF92400E)
                            )
                            if (tx.editedAtMillis != null) {
                                Text(
                                    text = "Waktu: ${Formatters.formatTanggalDanJam(tx.editedAtMillis!!)}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF92400E)
                                )
                            }
                            if (!tx.editReason.isNullOrBlank()) {
                                Text(
                                    text = "Alasan: ${tx.editReason}",
                                    fontSize = 11.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = Color(0xFF78350F)
                                )
                            }
                        }
                    }
                }

                if (currentUser.isAdmin) {
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedButton(
                        onClick = {
                            txToEdit = tx
                            selectedTx = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.outlined.Edit, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Koreksi Transaksi", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Dialog Koreksi Transaksi Admin
    if (txToEdit != null) {
        val target = txToEdit!!
        var newAmountText by remember { mutableStateOf(target.amount.toString()) }
        var newSummaryText by remember { mutableStateOf(target.summary) }
        var editReasonText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { txToEdit = null },
            title = { Text("Koreksi Transaksi Kas", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newAmountText,
                        onValueChange = { newAmountText = it },
                        label = { Text("Nominal (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newSummaryText,
                        onValueChange = { newSummaryText = it },
                        label = { Text("Uraian Transaksi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editReasonText,
                        onValueChange = { editReasonText = it },
                        label = { Text("Alasan Koreksi") },
                        placeholder = { Text("Cth: Penyesuaian nota belanja") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = newAmountText.trim().toLongOrNull() ?: 0L
                        val summary = newSummaryText.trim()
                        val reason = editReasonText.trim()
                        if (amount <= 0 || summary.isBlank() || reason.isBlank()) {
                            AppToast.error("Semua kolom koreksi wajib diisi")
                            return@Button
                        }
                        financeRepo.editTransaction(
                            id = target.id,
                            newAmount = amount,
                            newSummary = summary,
                            newCategory = target.category,
                            editorName = currentUser.name,
                            editReason = reason
                        )
                        txToEdit = null
                        AppToast.success("Koreksi transaksi berhasil disimpan")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primaryRoyal)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { txToEdit = null }) {
                    Text("Batal")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun FilterChipItem(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) color else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) color else AppColors.borderSubtle
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else AppColors.textSecondaryLight,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun TransactionRowCard(
    tx: TransactionItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.borderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (tx.isIncome) AppColors.incomeGreenBg else AppColors.expenseRedBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (tx.isIncome) Icons.outlined.ArrowDownward else Icons.outlined.ArrowUpward,
                        contentDescription = null,
                        tint = if (tx.isIncome) AppColors.incomeGreen else AppColors.expenseRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tx.summary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimaryLight,
                        maxLines = 1,
                        overflow = TextOverflow.ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = Formatters.formatTanggalDanJam(tx.occurredAtMillis),
                        fontSize = 11.sp,
                        color = AppColors.textSecondaryLight
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${if (tx.isIncome) "+" else "-"}${Formatters.formatRupiah(tx.amount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (tx.isIncome) AppColors.incomeGreen else AppColors.expenseRed
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.outlined.Person,
                        contentDescription = null,
                        tint = AppColors.textSecondaryLight,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Oleh: ${tx.recordedByName}",
                        fontSize = 11.sp,
                        color = AppColors.textSecondaryLight,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row {
                    Surface(
                        color = AppColors.surfaceLavender,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = tx.category ?: tx.period ?: "Umum",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.primaryRoyal,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (tx.wasEdited) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Diedit",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuditRowItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = AppColors.textSecondaryLight,
            modifier = Modifier.width(130.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.textPrimaryLight,
            modifier = Modifier.weight(1f)
        )
    }
}
