package id.or.karangtaruna.kasgo.data.models

data class PaymentMethodItem(
    val id: String,
    val type: String, // "qris", "bank", "ewallet", "custom"
    var title: String,
    var accountNumber: String,
    var accountName: String,
    var isActive: Boolean = true,
    var qrImageUrl: String? = null,
    var instructions: String? = null
)

data class OrganizationConfig(
    var orgId: String = "kt-pemuda",
    var name: String = "Karang Taruna",
    var scopeArea: String = "Unit Pengurus Kas",
    var description: String = "Sistem Pencatatan dan Transparansi Kas Terbuka",
    var contactPhone: String? = null,
    var paymentMethods: MutableList<PaymentMethodItem> = mutableListOf()
) {
    val fullTitle: String
        get() = "$name $scopeArea".trim()

    companion object {
        fun initialDefault(): OrganizationConfig {
            return OrganizationConfig(
                orgId = "kt-pemuda",
                name = "Karang Taruna",
                scopeArea = "Unit Pengurus Kas",
                description = "Sistem Pencatatan dan Transparansi Kas Terbuka",
                contactPhone = null,
                paymentMethods = mutableListOf()
            )
        }
    }
}
