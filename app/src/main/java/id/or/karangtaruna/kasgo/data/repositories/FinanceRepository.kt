package id.or.karangtaruna.kasgo.data.repositories

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import id.or.karangtaruna.kasgo.data.models.LedgerType
import id.or.karangtaruna.kasgo.data.models.PickupItem
import id.or.karangtaruna.kasgo.data.models.TransactionItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FinanceRepository private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kas_go_finance_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _items = MutableStateFlow<List<TransactionItem>>(loadTransactions())
    val transactions: StateFlow<List<TransactionItem>> = _items.asStateFlow()

    private val _pickups = MutableStateFlow<List<PickupItem>>(loadPickups())
    val pickups: StateFlow<List<PickupItem>> = _pickups.asStateFlow()

    val totalBalance: Long
        get() {
            var sum = 0L
            for (tx in _items.value) {
                if (tx.isIncome) sum += tx.amount else sum -= tx.amount
            }
            return sum
        }

    val totalIncome: Long
        get() = _items.value.filter { it.isIncome }.sumOf { it.amount }

    val totalExpense: Long
        get() = _items.value.filter { !it.isIncome }.sumOf { it.amount }

    val monthlyIncome: Map<String, Long>
        get() {
            val map = mutableMapOf<String, Long>()
            for (tx in _items.value) {
                if (tx.isIncome && !tx.period.isNullOrBlank()) {
                    map[tx.period!!] = (map[tx.period!!] ?: 0L) + tx.amount
                }
            }
            return map.toSortedMap()
        }

    val expenseByCategory: Map<String, Long>
        get() {
            val map = mutableMapOf<String, Long>()
            for (tx in _items.value) {
                if (!tx.isIncome && !tx.category.isNullOrBlank()) {
                    map[tx.category!!] = (map[tx.category!!] ?: 0L) + tx.amount
                }
            }
            return map.toList().sortedByDescending { it.second }.toMap()
        }

    val cumulativeBalance: List<Pair<String, Long>>
        get() {
            val months = _items.value.mapNotNull { it.period }.distinct().sorted()
            var running = 0L
            return months.map { month ->
                val monthTxs = _items.value.filter { it.period == month }
                for (tx in monthTxs) {
                    if (tx.isIncome) running += tx.amount else running -= tx.amount
                }
                month to running
            }
        }

    private fun loadTransactions(): List<TransactionItem> {
        val json = prefs.getString("tx_list", null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<TransactionItem>>() {}.type
                return gson.fromJson(json, type) ?: emptyList()
            } catch (_: Exception) {}
        }
        return emptyList()
    }

    private fun loadPickups(): List<PickupItem> {
        val json = prefs.getString("pickup_list", null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<PickupItem>>() {}.type
                return gson.fromJson(json, type) ?: emptyList()
            } catch (_: Exception) {}
        }
        return emptyList()
    }

    private fun persist() {
        prefs.edit()
            .putString("tx_list", gson.toJson(_items.value))
            .putString("pickup_list", gson.toJson(_pickups.value))
            .apply()
    }

    fun recordIncome(
        memberName: String,
        period: String,
        amount: Long,
        paymentMethod: String,
        recorderName: String,
        note: String? = null,
        billingId: String? = null
    ) {
        val summaryNote = if (!note.isNullOrBlank()) " ($note)" else ""
        val summary = "Iuran $period - $memberName$summaryNote"
        val item = TransactionItem(
            id = "tx_${UUID.randomUUID()}",
            entryType = LedgerType.INCOME,
            amount = amount,
            summary = summary,
            recordedByName = recorderName,
            occurredAtMillis = System.currentTimeMillis(),
            period = period,
            paymentMethod = paymentMethod,
            category = "Iuran Warga",
            billingId = billingId
        )
        val updated = _items.value.toMutableList()
        updated.add(0, item)
        _items.value = updated
        persist()
    }

    fun recordExpense(
        category: String,
        recipient: String,
        description: String,
        amount: Long,
        recorderName: String,
        dateMillis: Long
    ) {
        val monthStr = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(dateMillis))
        val summary = "$description ($recipient)"
        val item = TransactionItem(
            id = "tx_${UUID.randomUUID()}",
            entryType = LedgerType.EXPENSE,
            amount = amount,
            summary = summary,
            recordedByName = recorderName,
            occurredAtMillis = dateMillis,
            category = category,
            period = monthStr,
            paymentMethod = "Kas Tunai"
        )
        val updated = _items.value.toMutableList()
        updated.add(0, item)
        _items.value = updated
        persist()
    }

    fun editTransaction(
        id: String,
        newAmount: Long,
        newSummary: String,
        newCategory: String?,
        editorName: String,
        editReason: String,
        editorUid: String = ""
    ) {
        val updated = _items.value.toMutableList()
        val index = updated.indexOfFirst { it.id == id }
        if (index < 0 || newAmount <= 0 || editReason.isBlank()) return
        val old = updated[index]
        if (old.correctionOfTransactionId != null || updated.any { it.correctionOfTransactionId == id }) return

        val now = System.currentTimeMillis()
        val accountingPeriod = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(now))
        val reversal = old.copy(
            id = "tx_${UUID.randomUUID()}",
            entryType = if (old.isIncome) LedgerType.EXPENSE else LedgerType.INCOME,
            summary = "Pembalik: ${old.summary}",
            recordedByName = editorName,
            occurredAtMillis = now,
            period = accountingPeriod,
            category = "Koreksi Transaksi",
            paymentMethod = "Koreksi",
            recordedByUid = editorUid,
            editedByName = editorName,
            editedAtMillis = now,
            editReason = editReason,
            billingId = null,
            correctionOfTransactionId = id
        )
        val replacement = old.copy(
            id = "tx_${UUID.randomUUID()}",
            amount = newAmount,
            summary = newSummary,
            recordedByName = editorName,
            occurredAtMillis = now + 1,
            period = accountingPeriod,
            category = newCategory ?: old.category,
            recordedByUid = editorUid,
            editedByName = editorName,
            editedAtMillis = now,
            editReason = editReason,
            billingId = null,
            correctionOfTransactionId = id
        )
        _items.value = listOf(replacement, reversal) + updated
        persist()
    }

    fun addPickupRequest(
        name: String,
        address: String,
        phone: String,
        amount: Long,
        timeSlot: String,
        memberUid: String = ""
    ) {
        val item = PickupItem(
            id = "pk_${UUID.randomUUID()}",
            name = name,
            address = address,
            phone = phone,
            amount = amount,
            timeSlot = timeSlot,
            createdAtMillis = System.currentTimeMillis(),
            memberUid = memberUid
        )
        val updated = _pickups.value.toMutableList()
        updated.add(0, item)
        _pickups.value = updated
        persist()
    }

    fun updatePickupStatus(id: String, nextStatus: String): Boolean {
        val allowedNext = when (_pickups.value.firstOrNull { it.id == id }?.status) {
            "Menunggu" -> setOf("Dijadwalkan", "Dibatalkan")
            "Dijadwalkan" -> setOf("Selesai", "Dibatalkan")
            else -> emptySet()
        }
        if (nextStatus !in allowedNext) return false
        _pickups.value = _pickups.value.map { pickup ->
            if (pickup.id == id) pickup.copy(status = nextStatus) else pickup
        }
        persist()
        return true
    }

    companion object {
        @Volatile
        private var instance: FinanceRepository? = null

        fun initialize(context: Context): FinanceRepository {
            return instance ?: synchronized(this) {
                instance ?: FinanceRepository(context.applicationContext).also { instance = it }
            }
        }

        fun get(): FinanceRepository {
            return checkNotNull(instance) { "FinanceRepository must be initialized first" }
        }
    }
}
