package id.or.karangtaruna.kasgo.data.repositories

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import id.or.karangtaruna.kasgo.data.models.BillPaymentStatus
import id.or.karangtaruna.kasgo.data.models.BillingRecap
import id.or.karangtaruna.kasgo.data.models.MemberBillEntry
import id.or.karangtaruna.kasgo.data.models.MonthlyBill
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingRepository private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kas_go_billing_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _bills = MutableStateFlow<List<MonthlyBill>>(loadBills())
    val allBills: StateFlow<List<MonthlyBill>> = _bills.asStateFlow()

    private val _entries = MutableStateFlow<List<MemberBillEntry>>(loadEntries())
    val allEntries: StateFlow<List<MemberBillEntry>> = _entries.asStateFlow()

    private fun loadBills(): List<MonthlyBill> {
        val json = prefs.getString("bills_list", null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<MonthlyBill>>() {}.type
                return gson.fromJson(json, type) ?: emptyList()
            } catch (_: Exception) {}
        }
        return emptyList()
    }

    private fun loadEntries(): List<MemberBillEntry> {
        val json = prefs.getString("entries_list", null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<MemberBillEntry>>() {}.type
                return gson.fromJson(json, type) ?: emptyList()
            } catch (_: Exception) {}
        }
        return emptyList()
    }

    private fun persist() {
        prefs.edit()
            .putString("bills_list", gson.toJson(_bills.value))
            .putString("entries_list", gson.toJson(_entries.value))
            .apply()
    }

    fun getBillsForMember(nameOrId: String): List<MemberBillEntry> {
        val clean = nameOrId.trim().lowercase()
        return _entries.value.filter {
            it.memberId == nameOrId || it.memberName.lowercase().contains(clean)
        }
    }

    fun getEntriesForBill(billId: String): List<MemberBillEntry> {
        return _entries.value.filter { it.billId == billId }
    }

    fun getRecapForBill(bill: MonthlyBill): BillingRecap {
        val list = getEntriesForBill(bill.id)
        val totalMembers = list.size
        val paidList = list.filter { it.isPaid }
        val paidCount = paidList.size
        val unpaidCount = totalMembers - paidCount
        val totalCollected = paidList.sumOf { it.amount }
        val totalTarget = totalMembers.toLong() * bill.amount

        return BillingRecap(
            bill = bill,
            totalMembers = totalMembers,
            paidCount = paidCount,
            unpaidCount = unpaidCount,
            totalCollected = totalCollected,
            totalTarget = totalTarget
        )
    }

    fun getAllRecaps(): List<BillingRecap> {
        return _bills.value.map { getRecapForBill(it) }
    }

    fun publishNewBill(
        title: String,
        period: String,
        amount: Long,
        dueDateMillis: Long,
        createdByName: String,
        description: String? = null,
        memberNames: List<String>
    ): MonthlyBill {
        val billId = "bill_${System.currentTimeMillis()}"
        val bill = MonthlyBill(
            id = billId,
            title = title,
            period = period,
            amount = amount,
            dueDateMillis = dueDateMillis,
            createdByName = createdByName,
            createdAtMillis = System.currentTimeMillis(),
            description = description
        )

        val updatedBills = _bills.value.toMutableList()
        updatedBills.add(0, bill)
        _bills.value = updatedBills

        val updatedEntries = _entries.value.toMutableList()
        for (name in memberNames) {
            if (name.isBlank()) continue
            val entry = MemberBillEntry(
                id = "mb_${System.currentTimeMillis()}_${name.hashCode()}",
                billId = billId,
                memberId = "mem_${name.hashCode()}",
                memberName = name.trim(),
                amount = amount,
                period = period,
                status = BillPaymentStatus.BELUM_BAYAR
            )
            updatedEntries.add(entry)
        }
        _entries.value = updatedEntries
        persist()
        return bill
    }

    fun confirmBillPayment(
        entryId: String,
        paymentMethod: String,
        verifiedByName: String
    ) {
        val updated = _entries.value.toMutableList()
        val index = updated.indexOfFirst { it.id == entryId }
        if (index >= 0) {
            val old = updated[index]
            if (old.isPaid) return

            val item = old.copy(
                status = BillPaymentStatus.LUNAS,
                paidAtMillis = System.currentTimeMillis(),
                paymentMethod = paymentMethod,
                recordedByName = verifiedByName
            )
            updated[index] = item
            _entries.value = updated

            // Otomatis catat kas masuk ke Buku Kas
            FinanceRepository.get().recordIncome(
                memberName = item.memberName,
                period = item.period,
                amount = item.amount,
                paymentMethod = paymentMethod,
                recorderName = verifiedByName,
                note = "Iuran ${item.period} (Lunas)",
                billingId = item.billId
            )
            persist()
        }
    }

    fun requestPaymentVerification(entryId: String, paymentMethod: String) {
        val updated = _entries.value.toMutableList()
        val index = updated.indexOfFirst { it.id == entryId }
        if (index >= 0) {
            val item = updated[index].copy(
                status = BillPaymentStatus.MENUNGGU_VERIFIKASI,
                paymentMethod = paymentMethod
            )
            updated[index] = item
            _entries.value = updated
            persist()
        }
    }

    companion object {
        @Volatile
        private var instance: BillingRepository? = null

        fun initialize(context: Context): BillingRepository {
            return instance ?: synchronized(this) {
                instance ?: BillingRepository(context.applicationContext).also { instance = it }
            }
        }

        fun get(): BillingRepository {
            return checkNotNull(instance) { "BillingRepository must be initialized first" }
        }
    }
}
