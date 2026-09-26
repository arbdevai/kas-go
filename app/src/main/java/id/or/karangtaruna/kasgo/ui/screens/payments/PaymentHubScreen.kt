package id.or.karangtaruna.kasgo.ui.screens.payments

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChecklistRtl
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.or.karangtaruna.kasgo.core.constants.AppColors
import id.or.karangtaruna.kasgo.core.utils.AppToast
import id.or.karangtaruna.kasgo.core.utils.Formatters
import id.or.karangtaruna.kasgo.data.models.BillPaymentStatus
import id.or.karangtaruna.kasgo.data.models.MemberBillEntry
import id.or.karangtaruna.kasgo.data.models.PaymentMethodItem
import id.or.karangtaruna.kasgo.data.repositories.BillingRepository
import id.or.karangtaruna.kasgo.data.repositories.FinanceRepository
import id.or.karangtaruna.kasgo.data.repositories.OrganizationRepository
import id.or.karangtaruna.kasgo.data.repositories.UserProfileRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHubScreen(
    onOpenSettings: () -> Unit
) {
    val orgRepo = remember { OrganizationRepository.get() }
    val userRepo = remember { UserProfileRepository.get() }
    val billingRepo = remember { BillingRepository.get() }
    val clipboard = LocalClipboardManager.current

    val orgConfig by orgRepo.config.collectAsState()
    val currentUser by userRepo.currentUser.collectAsState()
    val allBills by billingRepo.allBills.collectAsState()
    val myEntries = remember(allBills, currentUser) {
        billingRepo.getBillsForMember(currentUser.name)
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Bayar Online, 1: Tagihan Saya, 2: Jemput Tunai
    var entryToConfirm by remember { mutableStateOf<MemberBillEntry?>(null) }

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
                    text = "Pembayaran Kas",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppColors.textPrimaryLight,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = orgConfig.fullTitle,
                    fontSize = 12.sp,
                    color = AppColors.textSecondaryLight
                )
            }
            if (currentUser.isAdmin) {
                IconButton(onClick = onOpenSettings) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.surfaceLavender)
                            .border(1.dp, AppColors.borderSubtle, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = "Pengaturan Rekening",
                            tint = AppColors.primaryRoyal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Segmented Tabs Pill
        Surface(
            color = AppColors.surfaceLavender,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(3.dp)) {
                listOf("Bayar Online", "Tagihan Saya", "Jemput Tunai").forEachIndexed { idx, title ->
                    val isSelected = selectedTab == idx
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = idx },
                        shape = RoundedCornerShape(11.dp),
                        color = if (isSelected) Color.White else Color.Transparent,
                        shadowElevation = if (isSelected) 3.dp else 0.dp
                    ) {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) AppColors.primaryRoyal else AppColors.textSecondaryLight,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            0 -> {
                // TAB 1: BAYAR ONLINE
                val activeMethods = orgRepo.activePaymentMethods
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (activeMethods.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Belum ada metode pembayaran yang diaktifkan oleh pengurus.",
                                fontSize = 12.sp,
                                color = AppColors.textSecondaryLight,
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                    } else {
                        activeMethods.forEach { method ->
                            if (method.type == "qris") {
                                QrisCard(method = method, onCopy = {
                                    clipboard.setText(AnnotatedString(method.accountNumber))
                                    AppToast.info("Kode QRIS disalin")
                                })
                            } else {
                                BankCard(method = method, onCopy = {
                                    clipboard.setText(AnnotatedString(method.accountNumber))
                                    AppToast.info("Nomor ${method.title} disalin")
                                })
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Petunjuk Transfer
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.ChecklistRtl, null, tint = AppColors.primaryRoyal, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Petunjuk Pembayaran", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                StepItem("1", "Transfer atau scan QRIS sesuai nominal iuran.")
                                StepItem("2", "Beri berita transfer: Iuran Kas - [Nama Anda].")
                                StepItem("3", "Buka tab Tagihan Saya lalu klik konfirmasi bayar.")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(96.dp))
                }
            }
            1 -> {
                // TAB 2: TAGIHAN SAYA
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Ringkasan Akun
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.borderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.surfaceLavender),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Person, null, tint = AppColors.primaryRoyal, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = currentUser.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${myEntries.size} tagihan terdaftar atas nama Anda", fontSize = 11.sp, color = AppColors.textSecondaryLight)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (allBills.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Tidak ada tagihan iuran aktif dari pengurus.",
                                fontSize = 12.sp,
                                color = AppColors.textSecondaryLight,
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                    } else {
                        allBills.forEach { bill ->
                            val entry = myEntries.firstOrNull { it.billId == bill.id } ?: MemberBillEntry(
                                id = "virtual_${bill.id}",
                                billId = bill.id,
                                memberId = currentUser.uid,
                                memberName = currentUser.name,
                                amount = bill.amount,
                                period = bill.period,
                                status = BillPaymentStatus.BELUM_BAYAR
                            )

                            val isPaid = entry.isPaid
                            val isWaiting = entry.status == BillPaymentStatus.MENUNGGU_VERIFIKASI

                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isPaid) AppColors.incomeGreen.copy(alpha = 0.3f) else AppColors.borderSubtle
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = bill.title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Surface(
                                            color = when {
                                                isPaid -> AppColors.incomeGreenBg
                                                isWaiting -> Color(0xFFFEF3C7)
                                                else -> AppColors.expenseRedBg
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = entry.status.label,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when {
                                                    isPaid -> AppColors.incomeGreen
                                                    isWaiting -> Color(0xFFB45309)
                                                    else -> AppColors.expenseRed
                                                },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Periode ${bill.period} • Jatuh Tempo ${Formatters.formatTanggal(bill.dueDateMillis)}",
                                        fontSize = 11.sp,
                                        color = AppColors.textSecondaryLight
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = Formatters.formatRupiah(bill.amount),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            color = AppColors.primaryRoyal
                                        )
                                        if (!isPaid && !isWaiting) {
                                            FilledTonalButton(onClick = { entryToConfirm = entry }) {
                                                Text("Konfirmasi Bayar")
                                            }
                                        } else if (isWaiting) {
                                            Text(
                                                text = "Menunggu verifikasi admin",
                                                fontSize = 11.sp,
                                                color = Color(0xFFB45309),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(96.dp))
                }
            }
            2 -> {
                // TAB 3: JEMPUT SETORAN TUNAI
                PickupFormTab()
            }
        }
    }

    // Modal Konfirmasi Pembayaran
    if (entryToConfirm != null) {
        val entry = entryToConfirm!!
        var method by remember { mutableStateOf("Transfer Bank") }

        ModalBottomSheet(
            onDismissRequest = { entryToConfirm = null },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = "Konfirmasi Pembayaran Iuran", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Periode ${entry.period} • Nominal ${Formatters.formatRupiah(entry.amount)}",
                    fontSize = 12.sp,
                    color = AppColors.textSecondaryLight
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Metode Pembayaran", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                listOf("Transfer Bank", "Scan QRIS", "Tunai").forEach { m ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { method = m }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (method == m) AppColors.primaryRoyal else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (method == m) AppColors.primaryRoyal else AppColors.borderSubtle
                            ),
                            modifier = Modifier.size(16.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = m, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        billingRepo.requestPaymentVerification(entry.id, method)
                        entryToConfirm = null
                        AppToast.success("Konfirmasi pembayaran diajukan ke pengurus")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primaryRoyal)
                ) {
                    Text("Kirim Konfirmasi", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun QrisCard(method: PaymentMethodItem, onCopy: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.borderSubtle),
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Verified, null, tint = AppColors.primaryRoyal, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = method.accountName.uppercase(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimaryLight
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${method.accountNumber} • Semua E-Wallet & Bank",
                fontSize = 11.sp,
                color = AppColors.textSecondaryLight
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Barcode Visual
            Surface(
                modifier = Modifier.size(180.dp),
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(2.dp, AppColors.borderSubtle)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.QrCode2,
                        contentDescription = null,
                        tint = AppColors.primaryRoyal.copy(alpha = 0.85f),
                        modifier = Modifier.size(150.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = method.instructions ?: "Mendukung seluruh m-Banking dan e-Wallet berlogo QRIS.",
                fontSize = 12.sp,
                color = AppColors.textSecondaryLight,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = onCopy,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.CopyAll, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Salin Kode QRIS", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BankCard(method: PaymentMethodItem, onCopy: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(22.dp), spotColor = AppColors.primaryRoyal)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(AppColors.heroPurpleStart, AppColors.heroPurpleEnd))
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = method.title.uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.2.sp
                    )
                    Surface(
                        color = Color.White.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (method.type == "ewallet") "E-Wallet" else "Rekening Kas",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Nomor Rekening", fontSize = 11.sp, color = AppColors.textOnPurpleMuted)
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = method.accountNumber,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.5.sp
                    )
                    IconButton(onClick = onCopy) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CopyAll,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Atas Nama", fontSize = 11.sp, color = AppColors.textOnPurpleMuted)
                Text(text = method.accountName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun PickupFormTab() {
    val financeRepo = remember { FinanceRepository.get() }
    val userRepo = remember { UserProfileRepository.get() }
    val user = userRepo.current

    var name by remember { mutableStateOf(user.name) }
    var phone by remember { mutableStateOf(user.phone) }
    var address by remember { mutableStateOf(user.address) }
    var amountText by remember { mutableStateOf("25000") }
    var timeSlot by remember { mutableStateOf("Sore (16:00 - 18:00)") }

    val slots = listOf(
        "Pagi (08:00 - 11:00)",
        "Siang (13:00 - 15:00)",
        "Sore (16:00 - 18:00)",
        "Malam (19:00 - 21:00)"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.borderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = "Jemput Setoran Tunai", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Petugas akan menjemput setoran iuran tunai langsung ke alamat Anda sesuai jadwal.",
                fontSize = 12.sp,
                color = AppColors.textSecondaryLight
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Lengkap") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Nomor WhatsApp") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Alamat Rumah & RT/RW") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Nominal Iuran (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text("Pilihan Waktu", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            slots.forEach { slot ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { timeSlot = slot }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (timeSlot == slot) AppColors.primaryRoyal else Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (timeSlot == slot) AppColors.primaryRoyal else AppColors.borderSubtle
                        ),
                        modifier = Modifier.size(16.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = slot, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val amount = amountText.trim().toLongOrNull() ?: 0L
                    if (name.isBlank() || phone.isBlank() || address.isBlank() || amount <= 0) {
                        AppToast.error("Lengkapi seluruh kolom formulir")
                        return@Button
                    }
                    financeRepo.addPickupRequest(
                        name = name.trim(),
                        address = address.trim(),
                        phone = phone.trim(),
                        amount = amount,
                        timeSlot = timeSlot
                    )
                    AppToast.success("Permintaan jemput setoran diajukan")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primaryRoyal)
            ) {
                Text("Ajukan Penjemputan", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Composable
fun StepItem(number: String, text: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Surface(
            shape = CircleShape,
            color = AppColors.surfaceLavender,
            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.borderSubtle),
            modifier = Modifier.size(18.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = number, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AppColors.primaryRoyal)
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 12.sp, color = AppColors.textSecondaryLight)
    }
}
