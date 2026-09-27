package id.or.karangtaruna.kasgo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.or.karangtaruna.kasgo.core.constants.AppColors
import id.or.karangtaruna.kasgo.core.utils.Formatters
import id.or.karangtaruna.kasgo.data.models.TransactionItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

internal data class CashFlowPoint(
    val dayMillis: Long,
    val income: Long,
    val expense: Long
)

/** Groups every transaction in the selected calendar period into local-time daily totals. */
internal fun cashFlowPoints(transactions: List<TransactionItem>, monthly: Boolean, now: Long): List<CashFlowPoint> {
    val cursor = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (monthly) set(Calendar.DAY_OF_MONTH, 1)
        else add(Calendar.DAY_OF_MONTH, -((get(Calendar.DAY_OF_WEEK) + 5) % 7))
    }
    val periodStart = cursor.timeInMillis
    val incomeByDay = HashMap<Long, Long>()
    val expenseByDay = HashMap<Long, Long>()
    transactions.asSequence()
        .filter { it.occurredAtMillis >= periodStart && it.occurredAtMillis <= now && it.amount > 0 }
        .forEach { transaction ->
            val day = Calendar.getInstance().apply {
                timeInMillis = transaction.occurredAtMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val target = if (transaction.isIncome) incomeByDay else expenseByDay
            target[day] = (target[day] ?: 0L) + transaction.amount
        }

    val points = mutableListOf<CashFlowPoint>()
    while (cursor.timeInMillis <= now) {
        val day = cursor.timeInMillis
        points += CashFlowPoint(day, incomeByDay[day] ?: 0L, expenseByDay[day] ?: 0L)
        cursor.add(Calendar.DAY_OF_MONTH, 1)
    }
    return points
}

@Composable
fun CashFlowTrendChart(transactions: List<TransactionItem>, modifier: Modifier = Modifier) {
    var monthly by rememberSaveable { mutableStateOf(false) }
    val points = remember(transactions, monthly) {
        cashFlowPoints(transactions, monthly, System.currentTimeMillis())
    }
    var selected by remember(points) { mutableIntStateOf((points.lastIndex).coerceAtLeast(0)) }
    val current = points.getOrNull(selected)
    val incomeColor = AppColors.primaryRoyal
    val expenseColor = Color(0xFF16836D)
    val dateFormat = remember { SimpleDateFormat("EEE, d MMM", Locale("id", "ID")) }
    val accessibilityLabel = current?.let {
        "${dateFormat.format(Date(it.dayMillis))}, kas masuk ${Formatters.formatRupiah(it.income)}, kas keluar ${Formatters.formatRupiah(it.expense)}"
    } ?: "Grafik arus kas kosong"

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(.75.dp, AppColors.borderSubtle)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Arus kas harian", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Pemasukan dan pengeluaran dari catatan kas", fontSize = 12.sp, color = AppColors.textSecondaryLight)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Minggu ini", "Bulan ini").forEachIndexed { index, label ->
                    val active = monthly == (index == 1)
                    Surface(
                        onClick = { monthly = index == 1 },
                        shape = RoundedCornerShape(50),
                        color = if (active) AppColors.surfaceLavender else AppColors.backgroundLight
                    ) {
                        Text(
                            label,
                            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            fontSize = 12.sp,
                            color = if (active) AppColors.primaryRoyal else AppColors.textSecondaryLight,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    LegendLabel("Masuk", incomeColor)
                    Text(Formatters.formatRupiah(points.sumOf { it.income }), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Column(Modifier.weight(1f)) {
                    LegendLabel("Keluar", expenseColor)
                    Text(Formatters.formatRupiah(points.sumOf { it.expense }), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(8.dp))
            if (current != null) {
                Text(
                    "${dateFormat.format(Date(current.dayMillis))}   Masuk ${Formatters.formatRupiah(current.income)}   •   Keluar ${Formatters.formatRupiah(current.expense)}",
                    fontSize = 11.sp,
                    color = AppColors.textSecondaryLight
                )
            }
            Spacer(Modifier.height(8.dp))
            Canvas(
                Modifier.fillMaxWidth().height(150.dp)
                    .semantics {
                        contentDescription = accessibilityLabel
                    }
                    .pointerInput(points) {
                        fun selectAt(x: Float) {
                            if (points.isEmpty()) return
                            val inset = 8.dp.toPx()
                            val fraction = ((x - inset) / (size.width - inset * 2).coerceAtLeast(1f)).coerceIn(0f, 1f)
                            selected = (fraction * points.lastIndex).roundToInt()
                        }
                        detectTapGestures { selectAt(it.x) }
                    }
                    .pointerInput(points) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            if (points.isNotEmpty()) {
                                val inset = 8.dp.toPx()
                                val fraction = ((change.position.x - inset) / (size.width - inset * 2).coerceAtLeast(1f)).coerceIn(0f, 1f)
                                selected = (fraction * points.lastIndex).roundToInt()
                            }
                        }
                    }
            ) {
                if (points.isEmpty()) return@Canvas
                val inset = 10.dp.toPx()
                val chartHeight = size.height - inset * 2
                val chartWidth = size.width - inset * 2
                val maxAmount = points.maxOf { maxOf(it.income, it.expense) }.coerceAtLeast(1L).toDouble()
                repeat(3) { index ->
                    val y = inset + index * chartHeight / 2
                    drawLine(AppColors.dividerLight, Offset(inset, y), Offset(size.width - inset, y), 1.dp.toPx())
                }
                fun drawSeries(value: (CashFlowPoint) -> Long, color: Color) {
                    val coordinates = points.mapIndexed { index, point ->
                        val x = if (points.size == 1) size.width / 2 else inset + index * chartWidth / points.lastIndex
                        val y = inset + ((maxAmount - value(point)) / maxAmount).toFloat() * chartHeight
                        Offset(x, y)
                    }
                    val path = Path().apply {
                        moveTo(coordinates.first().x, coordinates.first().y)
                        coordinates.zipWithNext().forEach { (a, b) ->
                            val midX = (a.x + b.x) / 2
                            cubicTo(midX, a.y, midX, b.y, b.x, b.y)
                        }
                    }
                    drawPath(path, color, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
                    coordinates.forEachIndexed { index, point ->
                        if (index == selected.coerceIn(coordinates.indices)) {
                            drawCircle(Color.White, 5.dp.toPx(), point)
                            drawCircle(color, 3.dp.toPx(), point)
                        }
                    }
                }
                drawSeries({ it.income }, incomeColor)
                drawSeries({ it.expense }, expenseColor)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(points.firstOrNull()?.let { dateFormat.format(Date(it.dayMillis)) }.orEmpty(), fontSize = 10.sp, color = AppColors.textSecondaryLight)
                Text(points.lastOrNull()?.let { dateFormat.format(Date(it.dayMillis)) }.orEmpty(), fontSize = 10.sp, color = AppColors.textSecondaryLight)
            }
            Text(
                if (points.none { it.income > 0 || it.expense > 0 }) "Belum ada transaksi pada periode ini" else "Geser grafik untuk melihat nilai setiap tanggal",
                Modifier.padding(top = 10.dp),
                fontSize = 11.sp,
                color = AppColors.textSecondaryLight
            )
        }
    }
}

@Composable
private fun LegendLabel(label: String, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Surface(Modifier.padding(top = 5.dp).width(14.dp).height(8.dp), color = color, shape = RoundedCornerShape(50)) {}
        Text(label, style = MaterialTheme.typography.labelMedium, color = AppColors.textSecondaryLight)
    }
}
