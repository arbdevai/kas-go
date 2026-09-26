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
    var contactPhone: String? = "0812-3456-7890",
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
                contactPhone = "0812-3456-7890",
                paymentMethods = mutableListOf(
                    PaymentMethodItem(
                        id = "qris",
                        type = "qris",
                        title = "QRIS Kas Resmi",
                        accountNumber = "NMID-ID1020261928340",
                        accountName = "Kas Karang Taruna",
                        isActive = true,
                        instructions = "Mendukung seluruh m-Banking dan e-Wallet berlogo QRIS."
                    ),
                    PaymentMethodItem(
                        id = "bca",
                        type = "bank",
                        title = "Bank BCA",
                        accountNumber = "883019283401",
                        accountName = "Kas Karang Taruna",
                        isActive = true,
                        instructions = "Transfer dan beri berita: Iuran Kas - [Nama Anda]"
                    ),
                    PaymentMethodItem(
                        id = "bri",
                        type = "bank",
                        title = "Bank BRI",
                        accountNumber = "",
                        accountName = "Kas Karang Taruna",
                        isActive = false,
                        instructions = "Transfer ke rekening kas BRI resmi."
                    ),
                    PaymentMethodItem(
                        id = "mandiri",
                        type = "bank",
                        title = "Bank Mandiri",
                        accountNumber = "",
                        accountName = "Kas Karang Taruna",
                        isActive = false,
                        instructions = "Transfer ke rekening kas Mandiri resmi."
                    ),
                    PaymentMethodItem(
                        id = "dana",
                        type = "ewallet",
                        title = "DANA",
                        accountNumber = "",
                        accountName = "Kas Karang Taruna",
                        isActive = false,
                        instructions = "Kirim saldo DANA ke nomor kas resmi."
                    )
                )
            )
        }
    }
}
