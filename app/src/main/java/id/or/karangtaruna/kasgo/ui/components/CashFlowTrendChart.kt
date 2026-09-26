package id.or.karangtaruna.kasgo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.or.karangtaruna.kasgo.core.constants.AppColors
import id.or.karangtaruna.kasgo.core.utils.Formatters
import id.or.karangtaruna.kasgo.data.models.TransactionItem
import java.util.Calendar
import kotlin.math.roundToInt

data class ChartPoint(val label: String, val value: Long)

/** Daily closing balance, including the opening balance before the selected period. */
internal fun balancePoints(transactions: List<TransactionItem>, monthly: Boolean, now: Long): List<ChartPoint> {
    val start = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        if (monthly) set(Calendar.DAY_OF_MONTH, 1)
        else add(Calendar.DAY_OF_MONTH, -((get(Calendar.DAY_OF_WEEK) + 5) % 7))
    }
    var balance = transactions.filter { it.occurredAtMillis < start.timeInMillis }
        .sumOf { if (it.isIncome) it.amount else -it.amount }
    val points = mutableListOf<ChartPoint>()
    while (start.timeInMillis <= now) {
        val day = start.timeInMillis
        start.add(Calendar.DAY_OF_MONTH, 1)
        balance += transactions.filter { it.occurredAtMillis >= day && it.occurredAtMillis < start.timeInMillis && it.occurredAtMillis <= now }
            .sumOf { if (it.isIncome) it.amount else -it.amount }
        points.add(ChartPoint(Formatters.formatTanggal(day), balance))
    }
    return points
}

@Composable
fun CashFlowTrendChart(transactions: List<TransactionItem>, modifier: Modifier = Modifier) {
    var monthly by rememberSaveable { mutableStateOf(false) }
    val points = balancePoints(transactions, monthly, System.currentTimeMillis())
    var selected by remember(points) { mutableIntStateOf(points.lastIndex) }
    val current = points.getOrNull(selected) ?: points.last()
    val progress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(850, easing = FastOutSlowInEasing))
    }
    val pulse by rememberInfiniteTransition(label = "Latest balance").animateFloat(
        initialValue = .12f, targetValue = .3f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "Pulse"
    )
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp), color = Color.White,
        border = BorderStroke(.75.dp, AppColors.borderSubtle)) {
        Column(Modifier.padding(16.dp)) {
            Text("Arus kas", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Minggu Ini", "Bulan Ini").forEachIndexed { index, label ->
                    val active = monthly == (index == 1)
                    Surface(onClick = { monthly = index == 1 }, shape = RoundedCornerShape(50),
                        color = if (active) AppColors.surfaceLavender else AppColors.backgroundLight) {
                        Text(label, Modifier.padding(horizontal = 14.dp, vertical = 12.dp), fontSize = 12.sp,
                            color = if (active) AppColors.primaryRoyal else AppColors.textSecondaryLight,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(Formatters.formatRupiah(current.value), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Saldo • ${current.label}", fontSize = 11.sp, color = AppColors.textSecondaryLight)
            Spacer(Modifier.height(12.dp))
            Canvas(Modifier.fillMaxWidth().height(150.dp)
                .semantics { contentDescription = "Saldo ${current.label}: ${Formatters.formatRupiah(current.value)}" }
                .pointerInput(points) {
                    detectTapGestures { offset ->
                        val inset = 10.dp.toPx()
                        selected = (((offset.x - inset) / (size.width - inset * 2).coerceAtLeast(1f)) * points.lastIndex).roundToInt().coerceIn(points.indices)
                    }
                }.pointerInput(points) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val inset = 10.dp.toPx()
                        selected = (((change.position.x - inset) / (size.width - inset * 2).coerceAtLeast(1f)) * points.lastIndex).roundToInt().coerceIn(points.indices)
                    }
                }) {
                val inset = 10.dp.toPx()
                val bottom = size.height - inset
                val min = minOf(0L, points.minOf { it.value }).toDouble()
                val max = maxOf(0L, points.maxOf { it.value }).toDouble()
                val range = (max - min).coerceAtLeast(1.0)
                val coordinates = points.mapIndexed { index, point ->
                    Offset(if (points.size == 1) size.width / 2 else inset + index * (size.width - inset * 2) / points.lastIndex,
                        inset + ((max - point.value) / range).toFloat() * (size.height - inset * 2))
                }
                repeat(3) { index ->
                    val y = inset + index * (size.height - inset * 2) / 2
                    drawLine(AppColors.dividerLight, Offset(inset, y), Offset(size.width - inset, y), 1.dp.toPx())
                }
                val line = Path().apply {
                    moveTo(coordinates.first().x, coordinates.first().y)
                    coordinates.zipWithNext().forEach { (a, b) ->
                        val midpoint = (a.x + b.x) / 2
                        cubicTo(midpoint, a.y, midpoint, b.y, b.x, b.y)
                    }
                }
                val fill = Path().apply {
                    addPath(line); lineTo(coordinates.last().x, bottom); lineTo(coordinates.first().x, bottom); close()
                }
                clipRect(right = size.width * progress.value) {
                    drawPath(fill, Brush.verticalGradient(listOf(AppColors.primaryRoyal.copy(alpha = .18f), Color.Transparent)))
                    drawPath(line, AppColors.primaryRoyal, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
                    val active = coordinates[selected.coerceIn(coordinates.indices)]
                    drawLine(AppColors.borderSubtle, Offset(active.x, inset), Offset(active.x, bottom), 1.dp.toPx())
                    drawCircle(AppColors.primaryRoyal.copy(alpha = pulse), 9.dp.toPx(), active)
                    drawCircle(Color.White, 5.dp.toPx(), active)
                    drawCircle(AppColors.primaryRoyal, 3.dp.toPx(), active)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(points.first().label, fontSize = 10.sp, color = AppColors.textSecondaryLight)
                if (points.size > 1) Text(points.last().label, fontSize = 10.sp, color = AppColors.textSecondaryLight)
            }
            Text(if (transactions.isEmpty()) "Belum ada transaksi" else "Geser grafik untuk melihat saldo harian",
                Modifier.padding(top = 12.dp), fontSize = 11.sp, color = AppColors.textSecondaryLight)
        }
    }
}
