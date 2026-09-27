package id.or.karangtaruna.kasgo.ui.screens.auth

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import id.or.karangtaruna.kasgo.core.constants.AppColors
import id.or.karangtaruna.kasgo.core.utils.AppToast
import id.or.karangtaruna.kasgo.data.repositories.OrganizationRepository
import id.or.karangtaruna.kasgo.data.repositories.UserProfileRepository
import id.or.karangtaruna.kasgo.services.AppUpdateService
import id.or.karangtaruna.kasgo.services.FirebaseSyncService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen() {
    val userRepo = remember { UserProfileRepository.get() }
    val orgRepo = remember { OrganizationRepository.get() }
    val orgConfig by orgRepo.config.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showNewUserModal by remember { mutableStateOf(false) }
    var newUserEmail by remember { mutableStateOf("") }
    var newUserName by remember { mutableStateOf("") }
    var newUserUid by remember { mutableStateOf("") }
    var signingIn by remember { mutableStateOf(false) }
    var registering by remember { mutableStateOf(false) }

    val credentialManager = remember(context) { CredentialManager.create(context) }

    fun launchGoogleAccountChooser() {
        scope.launch {
            signingIn = true
            try {
                val googleOption = GetGoogleIdOption.Builder()
                    .setServerClientId(context.getString(id.or.karangtaruna.kasgo.R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .setAutoSelectEnabled(false)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleOption)
                    .build()
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential !is CustomCredential ||
                    credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) error("Pilih akun Google untuk masuk")

                val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val firebaseUser = FirebaseAuth.getInstance()
                    .signInWithCredential(firebaseCredential).await().user
                    ?: error("Akun Firebase tidak ditemukan")

                when (val resolution = FirebaseSyncService.resolveSignedInMember(firebaseUser, orgConfig.orgId)) {
                    is FirebaseSyncService.MemberResolution.Active -> {
                        userRepo.loginWithFirebase(resolution.profile)
                        FirebaseSyncService.startLiveSync(orgConfig.orgId, resolution.profile)
                        AppToast.success("Selamat datang kembali, ${resolution.profile.name}")
                    }
                    FirebaseSyncService.MemberResolution.Pending -> {
                        FirebaseAuth.getInstance().signOut()
                        runCatching { credentialManager.clearCredentialState(ClearCredentialStateRequest()) }
                        AppToast.info("Pendaftaran akun ini masih menunggu persetujuan pengurus")
                    }
                    FirebaseSyncService.MemberResolution.NeedsRegistration -> {
                        newUserUid = firebaseUser.uid
                        newUserEmail = firebaseUser.email.orEmpty().trim().lowercase()
                        val cleanEmail = newUserEmail
                        val defaultName = (firebaseUser.displayName ?: cleanEmail.substringBefore("@"))
                            .replace(".", " ")
                            .replace("_", " ")
                            .trim()
                        newUserName = defaultName.ifBlank { "Warga Baru" }
                        showNewUserModal = true
                    }
                }
            } catch (error: GetCredentialException) {
                AppToast.info("Masuk Google dibatalkan atau tidak tersedia")
            } catch (error: Exception) {
                FirebaseAuth.getInstance().signOut()
                AppToast.error("Gagal masuk dengan Google: ${error.localizedMessage ?: "coba lagi"}")
            } finally {
                signingIn = false
            }
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
                .imePadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logo Monogram
            Box(
                modifier = Modifier
                    .size(80.dp)

                    .clip(CircleShape)
                    .background(
                        AppColors.surfaceLavender
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint = AppColors.primaryRoyal,
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

            Spacer(modifier = Modifier.height(44.dp))

            // Card Masuk
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(.75.dp, AppColors.borderSubtle, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
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

                    // Tombol Masuk dengan Google
                    OutlinedButton(
                        onClick = { launchGoogleAccountChooser() },
                        enabled = !signingIn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
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
                                text = if (signingIn) "Menghubungkan akun..." else "Masuk dengan Google",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.primaryRoyal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Kas Go • Versi ${AppUpdateService.currentVersion}",
                fontSize = 11.sp,
                color = AppColors.textMutedLight
            )
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
                        if (registering) return@Button
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

                        registering = true
                        scope.launch {
                            try {
                                FirebaseSyncService.registerPendingMember(
                                    uid = newUserUid,
                                    email = newUserEmail,
                                    name = name,
                                    phone = phone,
                                    address = address,
                                    orgId = orgConfig.orgId
                                )
                                FirebaseAuth.getInstance().signOut()
                                runCatching { credentialManager.clearCredentialState(ClearCredentialStateRequest()) }
                                showNewUserModal = false
                                AppToast.success("Pendaftaran terkirim. Pengurus perlu menyetujui akun warga.")
                            } catch (error: Exception) {
                                AppToast.error("Pendaftaran gagal: ${error.localizedMessage ?: "coba lagi"}")
                            } finally {
                                registering = false
                            }
                        }
                    },
                    enabled = !registering,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primaryRoyal)
                ) {
                    Text(if (registering) "Mengirim pendaftaran..." else "Daftar & Masuk", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
