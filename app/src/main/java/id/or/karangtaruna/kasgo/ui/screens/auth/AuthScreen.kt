package id.or.karangtaruna.kasgo.ui.screens.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import id.or.karangtaruna.kasgo.core.constants.AppColors
import id.or.karangtaruna.kasgo.core.utils.AppToast
import id.or.karangtaruna.kasgo.data.repositories.OrganizationRepository
import id.or.karangtaruna.kasgo.data.repositories.UserProfileRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen() {
    val context = LocalContext.current
    val userRepo = remember { UserProfileRepository.get() }
    val orgRepo = remember { OrganizationRepository.get() }
    val orgConfig by orgRepo.config.collectAsState()

    var showNewUserModal by remember { mutableStateOf(false) }
    var newUserEmail by remember { mutableStateOf("") }
    var newUserName by remember { mutableStateOf("") }

    var showAdminPinDialog by remember { mutableStateOf(false) }
    var adminPinText by remember { mutableStateOf("") }

    var showManualEmailSheet by remember { mutableStateOf(false) }
    var manualEmailText by remember { mutableStateOf("") }

    // Google Sign-In Client for native Android Account Chooser popup
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    fun processEmailAuth(email: String, displayName: String) {
        val cleanEmail = email.trim().lowercase()
        val existing = userRepo.checkGoogleAccount(cleanEmail)
        if (existing != null) {
            userRepo.loginWithExistingGoogle(existing)
            AppToast.success("Selamat datang kembali, ${existing.name}")
        } else {
            newUserEmail = cleanEmail
            newUserName = displayName.ifBlank { "Warga Baru" }
            showNewUserModal = true
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val email = account?.email
                val name = account?.displayName ?: ""
                if (!email.isNullOrBlank()) {
                    processEmailAuth(email, name)
                } else {
                    AppToast.error("Tidak dapat memperoleh email dari akun Google")
                }
            } catch (e: ApiException) {
                // If Play Services OAuth isn't configured in console, allow manual email input
                showManualEmailSheet = true
            }
        } else {
            // Prompt fallback sheet if user cancelled or error
            showManualEmailSheet = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.backgroundLight),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Logo Monogram
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(16.dp, CircleShape, spotColor = AppColors.primaryRoyal)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(AppColors.heroPurpleStart, AppColors.heroPurpleEnd)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint = AppColors.accentGold,
                    modifier = Modifier.size(38.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Kas Go",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppColors.textPrimaryLight,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = orgConfig.fullTitle,
                fontSize = 13.sp,
                color = AppColors.textSecondaryLight,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Card Masuk
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(14.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Silakan masuk untuk melanjutkan",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.textSecondaryLight
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Tombol Masuk dengan Google NATIVE
                    OutlinedButton(
                        onClick = {
                            try {
                                googleSignInClient.signOut() // Clear cache to always show chooser
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            } catch (_: Exception) {
                                showManualEmailSheet = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            AppColors.primaryRoyal
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, AppColors.borderSubtle, CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "G",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = Color(0xFF4285F4)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Masuk dengan Google",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.primaryRoyal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(modifier = Modifier.weight(1f), color = Color(0xFFF1F5F9))
                        Text(
                            text = "atau",
                            fontSize = 11.sp,
                            color = AppColors.textMutedLight,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                        Divider(modifier = Modifier.weight(1f), color = Color(0xFFF1F5F9))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Masuk sebagai Pengurus
                    TextButton(
                        onClick = { showAdminPinDialog = true }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.outlined.AdminPanelSettings,
                                contentDescription = null,
                                tint = AppColors.primaryRoyal,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Masuk sebagai Pengurus",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.primaryRoyal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Kas Go • Versi ${id.or.karangtaruna.kasgo.services.AppUpdateService.currentVersion}",
                fontSize = 11.sp,
                color = AppColors.textMutedLight
            )
        }
    }

    // Modal Sheet 1: Manual Google Email Fallback (jika perangkat tidak ada Play Services)
    if (showManualEmailSheet) {
        ModalBottomSheet(
            onDismissRequest = { showManualEmailSheet = false },
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
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, AppColors.borderSubtle, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color(0xFF4285F4)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Pilih / Masukkan Akun Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimaryLight
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = manualEmailText,
                    onValueChange = { manualEmailText = it },
                    label = { Text("Email Google") },
                    placeholder = { Text("nama.anda@gmail.com") },
                    leadingIcon = { Icon(Icons.outlined.Email, null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        val email = manualEmailText.trim()
                        if (email.isNotBlank() && email.contains("@")) {
                            showManualEmailSheet = false
                            processEmailAuth(email, email.substringBefore("@"))
                        } else {
                            AppToast.error("Masukkan alamat email yang valid")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primaryRoyal)
                ) {
                    Text("Lanjutkan", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal Sheet 2: Formulir Pendaftaran Warga Baru (Nama Lengkap, Nomor WhatsApp, Alamat)
    if (showNewUserModal) {
        var inputName by remember { mutableStateOf(newUserName) }
        var inputPhone by remember { mutableStateOf("") }
        var inputAddress by remember { mutableStateOf("") }

        ModalBottomSheet(
            onDismissRequest = { /* Must complete registration */ },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "Pendaftaran Warga Baru",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimaryLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Akun Google: $newUserEmail",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.primaryRoyal
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("Nama Lengkap") },
                    placeholder = { Text("Sesuai KTP") },
                    leadingIcon = { Icon(Icons.outlined.Person, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = inputPhone,
                    onValueChange = { inputPhone = it },
                    label = { Text("Nomor WhatsApp") },
                    placeholder = { Text("0812-xxxx-xxxx") },
                    leadingIcon = { Icon(Icons.outlined.Phone, null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = inputAddress,
                    onValueChange = { inputAddress = it },
                    label = { Text("Alamat Rumah & RT/RW") },
                    placeholder = { Text("Contoh: RT 02 / RW 05") },
                    leadingIcon = { Icon(Icons.outlined.LocationOn, null) },
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
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.incomeGreen)
                ) {
                    Text("Daftar & Masuk", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Dialog Akses Pengurus (PIN)
    if (showAdminPinDialog) {
        AlertDialog(
            onDismissRequest = { showAdminPinDialog = false },
            title = {
                Text(
                    text = "Akses Pengurus",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Masukkan PIN akses pengurus untuk mengelola kas:",
                        fontSize = 12.sp,
                        color = AppColors.textSecondaryLight
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = adminPinText,
                        onValueChange = { adminPinText = it },
                        placeholder = { Text("PIN Pengurus (Default: 123456)") },
                        leadingIcon = { Icon(Icons.outlined.Lock, null) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pin = adminPinText.trim()
                        val success = userRepo.loginWithAdminPin(pin)
                        if (success) {
                            showAdminPinDialog = false
                            AppToast.success("Masuk sebagai Bendahara Kas")
                        } else {
                            AppToast.error("PIN pengurus salah")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primaryRoyal)
                ) {
                    Text("Masuk")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminPinDialog = false }) {
                    Text("Batal")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
