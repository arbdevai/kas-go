package id.or.karangtaruna.kasgo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.or.karangtaruna.kasgo.core.constants.AppColors
import id.or.karangtaruna.kasgo.core.utils.Formatters
import id.or.karangtaruna.kasgo.data.models.TransactionItem

@Composable
fun TransactionRow(tx: TransactionItem, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).heightIn(min = 60.dp).padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).background(if (tx.isIncome) AppColors.incomeGreenBg else AppColors.surfaceMuted, CircleShape), contentAlignment = Alignment.Center) {
            Icon(if (tx.isIncome) Icons.Outlined.ArrowDownward else Icons.Outlined.ArrowUpward, null,
                tint = if (tx.isIncome) AppColors.incomeGreen else AppColors.textSecondaryLight, modifier = Modifier.size(16.dp))
        }
        Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
            Text(tx.summary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(Formatters.formatTanggal(tx.occurredAtMillis), color = AppColors.textSecondaryLight, fontSize = 11.sp)
        }
        Text("${if (tx.isIncome) "+" else "−"}${Formatters.formatRupiah(tx.amount)}",
            fontSize = 12.sp, fontWeight = FontWeight.Bold,
            color = if (tx.isIncome) AppColors.incomeGreen else AppColors.textPrimaryLight)
    }
}
