package id.or.karangtaruna.kasgo.data.models

enum class LedgerType {
    INCOME, EXPENSE
}

data class TransactionItem(
    val id: String,
    val entryType: LedgerType,
    var amount: Long,
    var summary: String,
    val recordedByName: String,
    val occurredAtMillis: Long,
    var period: String? = null,
    var category: String? = null,
    var paymentMethod: String = "Tunai",
    var recordedByUid: String? = null,
    var editedByName: String? = null,
    var editedAtMillis: Long? = null,
    var editReason: String? = null,
    var billingId: String? = null
) {
    val isIncome: Boolean get() = entryType == LedgerType.INCOME
    val wasEdited: Boolean get() = !editedByName.isNullOrBlank()
}

data class PickupItem(
    val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val amount: Long,
    val timeSlot: String,
    val createdAtMillis: Long,
    var status: String = "Menunggu"
)
