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
            id = "tx_${System.currentTimeMillis()}",
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
            id = "tx_${System.currentTimeMillis()}",
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
        editReason: String
    ) {
        val updated = _items.value.toMutableList()
        val index = updated.indexOfFirst { it.id == id }
        if (index >= 0) {
            val old = updated[index]
            val item = old.copy(
                amount = newAmount,
                summary = newSummary,
                category = newCategory ?: old.category,
                editedByName = editorName,
                editedAtMillis = System.currentTimeMillis(),
                editReason = editReason
            )
            updated[index] = item
            _items.value = updated
            persist()
        }
    }

    fun addPickupRequest(
        name: String,
        address: String,
        phone: String,
        amount: Long,
        timeSlot: String
    ) {
        val item = PickupItem(
            id = "pk_${System.currentTimeMillis()}",
            name = name,
            address = address,
            phone = phone,
            amount = amount,
            timeSlot = timeSlot,
            createdAtMillis = System.currentTimeMillis()
        )
        val updated = _pickups.value.toMutableList()
        updated.add(0, item)
        _pickups.value = updated
        persist()
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
