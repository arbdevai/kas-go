package id.or.karangtaruna.kasgo.data.models

enum class BillPaymentStatus(val code: String, val label: String) {
    BELUM_BAYAR("belum_bayar", "Belum Bayar"),
    MENUNGGU_VERIFIKASI("menunggu_verifikasi", "Menunggu Verifikasi"),
    LUNAS("lunas", "Lunas");

    companion object {
        fun fromCode(code: String): BillPaymentStatus {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: BELUM_BAYAR
        }
    }
}

data class MonthlyBill(
    val id: String,
    val title: String,
    val period: String, // YYYY-MM
    val amount: Long,
    val dueDateMillis: Long,
    val createdByName: String,
    val createdAtMillis: Long,
    val description: String? = null,
    val isPublished: Boolean = true
)

data class MemberBillEntry(
    val id: String,
    val billId: String,
    val memberId: String,
    val memberName: String,
    val amount: Long,
    val period: String,
    var status: BillPaymentStatus = BillPaymentStatus.BELUM_BAYAR,
    var paidAtMillis: Long? = null,
    var paymentMethod: String? = null,
    var recordedByName: String? = null,
    var transactionId: String? = null
) {
    val isPaid: Boolean get() = status == BillPaymentStatus.LUNAS
}

data class BillingRecap(
    val bill: MonthlyBill,
    val totalMembers: Int,
    val paidCount: Int,
    val unpaidCount: Int,
    val totalCollected: Long,
    val totalTarget: Long
) {
    val collectionPercentage: Double
        get() = if (totalTarget > 0) (totalCollected.toDouble() / totalTarget.toDouble()) * 100.0 else 0.0
}
