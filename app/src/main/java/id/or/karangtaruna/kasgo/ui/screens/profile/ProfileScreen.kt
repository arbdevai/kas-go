package id.or.karangtaruna.kasgo.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CorporateFare
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material.icons.outlined.SystemUpdateAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.or.karangtaruna.kasgo.core.constants.AppColors
import id.or.karangtaruna.kasgo.core.utils.AppToast
import id.or.karangtaruna.kasgo.data.models.AppUpdateInfo
import id.or.karangtaruna.kasgo.data.models.UserProfile
import id.or.karangtaruna.kasgo.data.models.UserRole
import id.or.karangtaruna.kasgo.data.repositories.BillingRepository
import id.or.karangtaruna.kasgo.data.repositories.OrganizationRepository
import id.or.karangtaruna.kasgo.data.repositories.UserProfileRepository
import id.or.karangtaruna.kasgo.services.AppUpdateService
import id.or.karangtaruna.kasgo.ui.components.UpdateDialog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigatePaymentSettings: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userRepo = remember { UserProfileRepository.get() }
    val orgRepo = remember { OrganizationRepository.get() }
    val billingRepo = remember { BillingRepository.get() }

    val user by userRepo.currentUser.collectAsState()
    val orgConfig by orgRepo.config.collectAsState()
    val isAdmin = user.isAdmin

    var showEditProfileModal by remember { mutableStateOf(false) }
    var showRoleModal by remember { mutableStateOf(false) }
    var showPublishBillModal by remember { mutableStateOf(false) }
    var showOrgModal by remember { mutableStateOf(false) }
    var updateDialogInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Profil & Akun",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AppColors.textPrimaryLight,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = "Informasi akun dan pengaturan kas",
            fontSize = 12.sp,
            color = AppColors.textSecondaryLight
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Kartu Profil
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(14.dp, RoundedCornerShape(24.dp), spotColor = AppColors.primaryRoyal),
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f))
                                .border(2.dp, AppColors.accentGold.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.name.firstOrNull()?.uppercase() ?: "W",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                color = if (isAdmin) AppColors.accentGold.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = user.roleTitle.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAdmin) AppColors.accentGold else Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = user.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = user.email.ifBlank { user.phone },
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { showEditProfileModal = true }) {
                            Surface(
                                color = Color.White.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Edit Data",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Color.White.copy(alpha = 0.24f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Phone, null, tint = AppColors.textOnPurpleMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = user.phone.ifBlank { "-" }, fontSize = 11.sp, color = Color.White)
                        Spacer(modifier = Modifier.width(14.dp))
                        Icon(Icons.Outlined.LocationOn, null, tint = AppColors.textOnPurpleMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = user.address.ifBlank { "-" },
                            fontSize = 11.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Panel Pengurus khusus Admin
        if (isAdmin) {
            Text(text = "Menu Pengurus", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimaryLight)
            Spacer(modifier = Modifier.height(10.dp))

            ProfileMenuCard(
                icon = Icons.Outlined.ManageAccounts,
                title = "Kelola Peran Anggota",
                subtitle = "Atur hak akses Bendahara, Sekretaris, Koordinator",
                onClick = { showRoleModal = true }
            )
            ProfileMenuCard(
                icon = Icons.Outlined.PostAdd,
                title = "Terbitkan Tagihan Iuran",
                subtitle = "Kirimkan tagihan iuran baru ke warga",
                onClick = { showPublishBillModal = true }
            )
            ProfileMenuCard(
                icon = Icons.Outlined.CorporateFare,
                title = "Profil Organisasi",
                subtitle = "Nama organisasi, lingkup wilayah, dan kode unit",
                onClick = { showOrgModal = true }
            )
            ProfileMenuCard(
                icon = Icons.Outlined.AccountBalance,
                title = "Rekening & QRIS Kas",
                subtitle = "Pengaturan rekening dan saklar aktif/nonaktif",
                onClick = onNavigatePaymentSettings
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Informasi Aplikasi & Periksa Pembaruan
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.borderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = orgConfig.fullTitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimaryLight
                )
                Text(
                    text = "Kas Go • Versi ${AppUpdateService.currentVersion}",
                    fontSize = 11.sp,
                    color = AppColors.textSecondaryLight
                )
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            AppToast.info("Memeriksa rilis terbaru...")
                            scope.launch {
                                val info = AppUpdateService.checkUpdate()
                                if (info != null) {
                                    if (info.hasUpdate) {
                                        updateDialogInfo = info
                                    } else {
                                        AppToast.success("Aplikasi sudah versi terbaru (v${info.currentVersion})")
                                    }
                                } else {
                                    AppToast.error("Tidak dapat terhubung ke server rilis")
                                }
                            }
                        }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColors.surfaceLavender),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.SystemUpdateAlt, null, tint = AppColors.primaryRoyal, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Periksa Pembaruan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.textPrimaryLight
                        )
                        Text(
                            text = "Cek versi baru dan unduh APK",
                            fontSize = 11.sp,
                            color = AppColors.textSecondaryLight
                        )
                    }
                    Icon(Icons.Outlined.ChevronRight, null, tint = AppColors.textMutedLight, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tombol Keluar dari Akun (Logout)
        OutlinedButton(
            onClick = {
                userRepo.logout()
                AppToast.info("Anda telah keluar dari akun")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
        ) {
            Icon(Icons.Outlined.Logout, null, tint = Color.Red, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Keluar dari Akun", color = Color.Red, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(96.dp))
    }

    if (updateDialogInfo != null) {
        UpdateDialog(
            info = updateDialogInfo!!,
            onDismiss = { updateDialogInfo = null }
        )
    }

    // Modal Edit Profil
    if (showEditProfileModal) {
        var nameText by remember { mutableStateOf(user.name) }
        var phoneText by remember { mutableStateOf(user.phone) }
        var addressText by remember { mutableStateOf(user.address) }

        ModalBottomSheet(
            onDismissRequest = { showEditProfileModal = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(text = "Edit Data Diri", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Nama Lengkap") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = phoneText,
                    onValueChange = { phoneText = it },
                    label = { Text("Nomor WhatsApp") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = addressText,
                    onValueChange = { addressText = it },
                    label = { Text("Alamat Rumah & RT/RW") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        userRepo.updateProfile(nameText, phoneText, addressText)
                        showEditProfileModal = false
                        AppToast.success("Data profil diperbarui")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primaryRoyal)
                ) {
                    Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal Kelola Peran
    if (showRoleModal) {
        ModalBottomSheet(
            onDismissRequest = { showRoleModal = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(text = "Kelola Peran Anggota", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Pilih peran pengurus atau kembalikan ke peran warga.", fontSize = 12.sp, color = AppColors.textSecondaryLight)
                Spacer(modifier = Modifier.height(16.dp))

                val members = userRepo.allMembers
                if (members.isEmpty()) {
                    Text(text = "Belum ada anggota terdaftar.", fontSize = 12.sp, color = AppColors.textSecondaryLight)
                } else {
                    members.forEach { m ->
                        var menuExpanded by remember { mutableStateOf(false) }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.borderSubtle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(AppColors.surfaceLavender),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = m.name.firstOrNull()?.uppercase() ?: "W",
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.primaryRoyal
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = m.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = m.roleTitle, fontSize = 11.sp, color = AppColors.primaryRoyal)
                                }
                                Box {
                                    IconButton(onClick = { menuExpanded = true }) {
                                        Icon(Icons.Outlined.MoreVert, null, modifier = Modifier.size(18.dp))
                                    }
                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false }
                                    ) {
                                        UserRole.entries.forEach { roleOption ->
                                            DropdownMenuItem(
                                                text = { Text(roleOption.label) },
                                                onClick = {
                                                    menuExpanded = false
                                                    userRepo.updateRoleForMember(m.uid, roleOption)
                                                    AppToast.success("Peran ${m.name} diubah ke ${roleOption.label}")
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal Terbitkan Tagihan Iuran
    if (showPublishBillModal) {
        val now = remember { SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date()) }
        var billTitle by remember { mutableStateOf("Iuran Kas") }
        var billAmountText by remember { mutableStateOf("25000") }
        var billDesc by remember { mutableStateOf("Iuran rutin") }

        ModalBottomSheet(
            onDismissRequest = { showPublishBillModal = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(text = "Terbitkan Tagihan Iuran", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = billTitle,
                    onValueChange = { billTitle = it },
                    label = { Text("Nama Tagihan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = billAmountText,
                    onValueChange = { billAmountText = it },
                    label = { Text("Nominal per Warga (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = billDesc,
                    onValueChange = { billDesc = it },
                    label = { Text("Keterangan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        val amount = billAmountText.trim().toLongOrNull() ?: 0L
                        if (billTitle.isBlank() || amount <= 0) {
                            AppToast.error("Nama tagihan dan nominal wajib diisi")
                            return@Button
                        }
                        val members = userRepo.allMembers.map { it.name }.ifEmpty { listOf(user.name) }
                        billingRepo.publishNewBill(
                            title = billTitle.trim(),
                            period = now,
                            amount = amount,
                            dueDateMillis = System.currentTimeMillis() + (30L * 24L * 60L * 60L * 1000L),
                            createdByName = user.name,
                            description = billDesc.trim(),
                            memberNames = members
                        )
                        showPublishBillModal = false
                        AppToast.success("Tagihan iuran berhasil diterbitkan")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primaryRoyal)
                ) {
                    Text("Terbitkan Tagihan", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal Profil Organisasi
    if (showOrgModal) {
        var orgName by remember { mutableStateOf(orgConfig.name) }
        var orgScope by remember { mutableStateOf(orgConfig.scopeArea) }
        var orgId by remember { mutableStateOf(orgConfig.orgId) }

        ModalBottomSheet(
            onDismissRequest = { showOrgModal = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(text = "Profil Organisasi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = orgName,
                    onValueChange = { orgName = it },
                    label = { Text("Nama Organisasi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = orgScope,
                    onValueChange = { orgScope = it },
                    label = { Text("Lingkup Wilayah (RT/RW/Desa)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = orgId,
                    onValueChange = { orgId = it },
                    label = { Text("Kode Unik Database") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        if (orgName.isBlank() || orgScope.isBlank() || orgId.isBlank()) {
                            AppToast.error("Semua kolom profil wajib diisi")
                            return@Button
                        }
                        orgRepo.saveConfig(orgId = orgId, name = orgName, scopeArea = orgScope)
                        showOrgModal = false
                        AppToast.success("Profil organisasi disimpan")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primaryRoyal)
                ) {
                    Text("Simpan Profil", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ProfileMenuCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.borderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColors.surfaceLavender),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = AppColors.primaryRoyal, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimaryLight)
                Text(text = subtitle, fontSize = 11.sp, color = AppColors.textSecondaryLight)
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = AppColors.textMutedLight, modifier = Modifier.size(18.dp))
        }
    }
}
