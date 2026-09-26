package id.or.karangtaruna.kasgo.ui.screens.admin

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.or.karangtaruna.kasgo.core.constants.AppColors
import id.or.karangtaruna.kasgo.core.utils.AppToast
import id.or.karangtaruna.kasgo.data.models.PaymentMethodItem
import id.or.karangtaruna.kasgo.data.repositories.OrganizationRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSettingsScreen(onBack: () -> Unit) {
    val orgRepo = remember { OrganizationRepository.get() }
    val orgConfig by orgRepo.config.collectAsState()
    val methods = orgConfig.paymentMethods

    var methodToEdit by remember { mutableStateOf<PaymentMethodItem?>(null) }
    var showAddCustomModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.backgroundLight)
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.borderSubtle),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.ArrowBack, null, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "Metode Pembayaran Kas", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimaryLight)
                Text(text = "Kelola rekening bank dan saklar aktif/nonaktif", fontSize = 11.sp, color = AppColors.textSecondaryLight)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info Banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AppColors.surfaceLavender,
            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.borderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Info, null, tint = AppColors.primaryRoyal, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Aktifkan atau nonaktifkan rekening sesuai kebutuhan. Warga hanya dapat melihat metode pembayaran yang aktif.",
                    fontSize = 12.sp,
                    color = AppColors.textPrimaryLight,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(methods, key = { it.id }) { m ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (m.isActive) AppColors.primaryRoyal.copy(alpha = 0.3f) else AppColors.borderSubtle
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (m.isActive) AppColors.surfaceLavender else Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (m.type == "qris") Icons.Outlined.QrCode2 else Icons.Outlined.AccountBalance,
                                    contentDescription = null,
                                    tint = if (m.isActive) AppColors.primaryRoyal else AppColors.textMutedLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = m.title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = m.accountNumber.ifBlank { "(Nomor rekening belum diatur)" },
                                    fontSize = 12.sp,
                                    color = if (m.accountNumber.isNotBlank()) AppColors.textSecondaryLight else Color(0xFFD97706)
                                )
                            }
                            Switch(
                                checked = m.isActive,
                                onCheckedChange = { orgRepo.togglePaymentMethod(m.id, it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = AppColors.primaryRoyal)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Atas Nama: ${m.accountName.ifBlank { "-" }}", fontSize = 11.sp, color = AppColors.textSecondaryLight)
                            Row {
                                TextButton(onClick = { methodToEdit = m }) {
                                    Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ubah", fontSize = 12.sp)
                                }
                                if (m.type == "custom" || m.id.startsWith("pm_")) {
                                    IconButton(onClick = { orgRepo.deletePaymentMethod(m.id) }) {
                                        Icon(Icons.Outlined.DeleteOutline, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { showAddCustomModal = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Outlined.AddCircleOutline, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tambah Rekening / E-Wallet Baru", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Modal Edit Rekening
    if (methodToEdit != null) {
        val target = methodToEdit!!
        var title by remember { mutableStateOf(target.title) }
        var accNo by remember { mutableStateOf(target.accountNumber) }
        var accName by remember { mutableStateOf(target.accountName) }
        var instructions by remember { mutableStateOf(target.instructions ?: "") }

        ModalBottomSheet(
            onDismissRequest = { methodToEdit = null },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(text = "Edit ${target.title}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Layanan / Bank") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = accNo,
                    onValueChange = { accNo = it },
                    label = { Text(if (target.type == "qris") "Kode NMID QRIS" else "Nomor Rekening") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = accName,
                    onValueChange = { accName = it },
                    label = { Text("Atas Nama") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Petunjuk Transfer") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (title.isBlank() || accNo.isBlank()) {
                            AppToast.error("Nama dan nomor rekening wajib diisi")
                            return@Button
                        }
                        target.title = title.trim()
                        target.accountNumber = accNo.trim()
                        target.accountName = accName.trim()
                        target.instructions = instructions.trim().ifBlank { null }
                        orgRepo.updatePaymentMethod(target)
                        methodToEdit = null
                        AppToast.success("Metode pembayaran disimpan")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primaryRoyal)
                ) {
                    Text("Simpan Pengaturan", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal Tambah Custom
    if (showAddCustomModal) {
        var title by remember { mutableStateOf("") }
        var accNo by remember { mutableStateOf("") }
        var accName by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("bank") }

        ModalBottomSheet(
            onDismissRequest = { showAddCustomModal = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(text = "Tambah Rekening / E-Wallet Baru", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Bank / Layanan (Cth: Bank BSI / GoPay)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = accNo,
                    onValueChange = { accNo = it },
                    label = { Text("Nomor Rekening / No HP") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = accName,
                    onValueChange = { accName = it },
                    label = { Text("Atas Nama") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (title.isBlank() || accNo.isBlank()) {
                            AppToast.error("Nama dan nomor rekening wajib diisi")
                            return@Button
                        }
                        val newItem = PaymentMethodItem(
                            id = "pm_${System.currentTimeMillis()}",
                            type = type,
                            title = title.trim(),
                            accountNumber = accNo.trim(),
                            accountName = accName.trim(),
                            isActive = true
                        )
                        orgRepo.updatePaymentMethod(newItem)
                        showAddCustomModal = false
                        AppToast.success("Metode pembayaran baru ditambahkan")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primaryRoyal)
                ) {
                    Text("Tambahkan", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
