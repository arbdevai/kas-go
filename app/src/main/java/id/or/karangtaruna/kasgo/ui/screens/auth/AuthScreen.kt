package id.or.karangtaruna.kasgo.ui.screens.auth

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import id.or.karangtaruna.kasgo.ui.components.KasInput
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.common.AccountPicker
import id.or.karangtaruna.kasgo.core.constants.AppColors
import id.or.karangtaruna.kasgo.core.utils.AppToast
import id.or.karangtaruna.kasgo.data.repositories.OrganizationRepository
import id.or.karangtaruna.kasgo.data.repositories.UserProfileRepository
import id.or.karangtaruna.kasgo.services.AppUpdateService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen() {
    val userRepo = remember { UserProfileRepository.get() }
    val orgRepo = remember { OrganizationRepository.get() }
    val orgConfig by orgRepo.config.collectAsState()

    var showNewUserModal by remember { mutableStateOf(false) }
    var newUserEmail by remember { mutableStateOf("") }
    var newUserName by remember { mutableStateOf("") }

    fun processSelectedGoogleEmail(email: String) {
        val cleanEmail = email.trim().lowercase()
        val existing = userRepo.checkGoogleAccount(cleanEmail)
        if (existing != null) {
            userRepo.loginWithExistingGoogle(existing)
            AppToast.success("Selamat datang kembali, ${existing.name}")
        } else {
            newUserEmail = cleanEmail
            val defaultName = cleanEmail.substringBefore("@")
                .replace(".", " ")
                .replace("_", " ")
                .split(" ")
                .filter { it.isNotBlank() }
                .joinToString(" ") { part ->
                    part.replaceFirstChar { char -> char.uppercase() }
                }
            newUserName = defaultName.ifBlank { "Warga Baru" }
            showNewUserModal = true
        }
    }

    val googleAccountLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val email = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                ?: result.data?.getStringExtra("authAccount")
            if (!email.isNullOrBlank()) {
                processSelectedGoogleEmail(email)
            } else {
                AppToast.error("Gagal memilih akun Google")
            }
        }
    }

    fun launchGoogleAccountChooser() {
        try {
            val intent = try {
                AccountPicker.newChooseAccountIntent(
                    AccountPicker.AccountChooserOptions.Builder()
                        .setAllowableAccountsTypes(listOf("com.google"))
                        .build()
                )
            } catch (_: Throwable) {
                AccountManager.newChooseAccountIntent(
                    null,
                    null,
                    arrayOf("com.google"),
                    null,
                    null,
                    null,
                    null
                )
            }
            googleAccountLauncher.launch(intent)
        } catch (e: Exception) {
            AppToast.error("Gagal membuka pemilih akun Google: ${e.message}")
        }
    }

    Box(Modifier.fillMaxSize().background(AppColors.backgroundLight)) {
        Column(
            modifier = Modifier.fillMaxSize()
                .imePadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(46.dp).clip(RoundedCornerShape(15.dp))
                            .background(Brush.linearGradient(listOf(AppColors.primaryRoyal, AppColors.primarySoft))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.AccountBalanceWallet, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Kas Go", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold,
                            color = AppColors.textPrimaryLight, letterSpacing = (-.3).sp)
                        Text(orgConfig.fullTitle, fontSize = 11.sp, color = AppColors.textSecondaryLight,
                            maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                    Surface(color = AppColors.surfaceLavender, shape = RoundedCornerShape(50)) {
                        Text("KAS DIGITAL", Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            color = AppColors.primaryRoyal, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = .5.sp)
                    }
                }

                Spacer(Modifier.height(52.dp))
                Surface(color = AppColors.surfaceLavender, shape = RoundedCornerShape(50)) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(AppColors.incomeGreen))
                        Spacer(Modifier.width(8.dp))
                        Text("Transparan untuk seluruh warga", fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold, color = AppColors.primaryRoyal)
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text("Urus kas bersama.\nSemua tercatat.", fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold, color = AppColors.textPrimaryLight,
                    lineHeight = 38.sp, letterSpacing = (-.8).sp)
                Spacer(Modifier.height(12.dp))
                Text("Pantau saldo, iuran, dan setiap transaksi dalam satu tempat.",
                    fontSize = 14.sp, color = AppColors.textSecondaryLight, lineHeight = 21.sp)

                Spacer(Modifier.height(28.dp))
                Surface(shape = RoundedCornerShape(20.dp), color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.borderSubtle)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                        listOf("Saldo dan mutasi tercatat rapi", "Iuran warga mudah dipantau", "Laporan terbuka untuk semua").forEachIndexed { index, label ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(30.dp).clip(CircleShape).background(AppColors.surfaceLavender),
                                    contentAlignment = Alignment.Center) {
                                    Icon(when(index) {
                                        0 -> Icons.Outlined.AccountBalanceWallet
                                        1 -> Icons.Outlined.Groups
                                        else -> Icons.Outlined.Visibility
                                    }, null, tint = AppColors.primaryRoyal, modifier = Modifier.size(16.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.textPrimaryLight)
                            }
                        }
                    }
                }
            }

            Column(Modifier.fillMaxWidth().padding(top = 32.dp)) {
                Button(
                    onClick = { launchGoogleAccountChooser() },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primaryRoyal)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(26.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                            Text("G", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF4285F4))
                        }
                        Spacer(Modifier.width(11.dp))
                        Text("Lanjutkan dengan Google", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Masuk dengan akun Google untuk membuka kas organisasi.",
                    modifier = Modifier.fillMaxWidth(), fontSize = 11.sp, color = AppColors.textSecondaryLight,
                    textAlign = TextAlign.Center)
                Spacer(Modifier.height(18.dp))
                Text("Kas Go  •  Versi ${AppUpdateService.currentVersion}",
                    modifier = Modifier.fillMaxWidth(), fontSize = 10.sp, color = AppColors.textMutedLight,
                    textAlign = TextAlign.Center)
            }
        }
    }

    // Modal Sheet: Formulir Pendaftaran Warga Baru
    if (showNewUserModal) {
        var inputName by remember { mutableStateOf(newUserName) }
        var inputPhone by remember { mutableStateOf("") }
        var inputAddress by remember { mutableStateOf("") }

        ModalBottomSheet(
            onDismissRequest = { showNewUserModal = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding().verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pendaftaran Warga Baru",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.textPrimaryLight
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Akun: $newUserEmail",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.primaryRoyal
                        )
                    }
                    IconButton(onClick = { showNewUserModal = false }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Tutup",
                            tint = AppColors.textSecondaryLight
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                KasInput(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("Nama Lengkap") },
                    placeholder = { Text("Sesuai KTP") },
                    leadingIcon = { Icon(Icons.Outlined.Person, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                KasInput(
                    value = inputPhone,
                    onValueChange = { inputPhone = it },
                    label = { Text("Nomor WhatsApp") },
                    placeholder = { Text("0812-xxxx-xxxx") },
                    leadingIcon = { Icon(Icons.Outlined.Phone, null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                KasInput(
                    value = inputAddress,
                    onValueChange = { inputAddress = it },
                    label = { Text("Alamat Rumah & RT/RW") },
                    placeholder = { Text("Contoh: RT 02 / RW 05") },
                    leadingIcon = { Icon(Icons.Outlined.LocationOn, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val name = inputName.trim()
                        val phone = inputPhone.trim()
                        val address = inputAddress.trim()

                        if (name.isBlank()) {
                            AppToast.error("Nama lengkap wajib diisi")
                            return@Button
                        }
                        if (phone.isBlank()) {
                            AppToast.error("Nomor WhatsApp wajib diisi")
                            return@Button
                        }
                        if (address.isBlank()) {
                            AppToast.error("Alamat rumah wajib diisi")
                            return@Button
                        }

                        val err = userRepo.registerWithGoogle(
                            email = newUserEmail,
                            name = name,
                            phone = phone,
                            address = address
                        )
                        if (err != null) {
                            AppToast.error(err)
                        } else {
                            showNewUserModal = false
                            AppToast.success("Pendaftaran berhasil. Selamat datang, $name!")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primaryRoyal)
                ) {
                    Text("Daftar & Masuk", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
