package id.or.karangtaruna.kasgo.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.or.karangtaruna.kasgo.core.constants.AppColors
import id.or.karangtaruna.kasgo.data.models.AppUpdateInfo
import id.or.karangtaruna.kasgo.services.AppUpdateService

@Composable
fun UpdateDialog(
    info: AppUpdateInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Pembaruan Versi Tersedia",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimaryLight
                )
                Text(
                    text = "Versi v${info.remoteVersion} (Saat ini: v${info.currentVersion})",
                    fontSize = 11.sp,
                    color = AppColors.textSecondaryLight
                )
            }
        },
        text = {
            Column {
                Text(
                    text = info.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.primaryRoyal
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = AppColors.surfaceLavender,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = info.changelog.ifBlank { "Pembaruan aplikasi resmi." },
                        fontSize = 12.sp,
                        color = AppColors.textPrimaryLight,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    AppUpdateService.launchDownload(context, info.apkUrl)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primaryRoyal)
            ) {
                Text("Unduh APK", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Nanti")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}
