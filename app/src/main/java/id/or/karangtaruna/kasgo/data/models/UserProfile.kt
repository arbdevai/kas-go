package id.or.karangtaruna.kasgo.data.models

enum class UserRole(val code: String, val label: String) {
    ADMIN1("admin1", "Admin 1 (Bendahara)"),
    ADMIN2("admin2", "Admin 2 (Sekretaris)"),
    ADMIN3("admin3", "Admin 3 (Koordinator Lapangan)"),
    WARGA("warga", "Warga");

    companion object {
        fun fromCode(code: String): UserRole {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: WARGA
        }
    }
}

data class UserProfile(
    val uid: String,
    var name: String,
    var email: String,
    var phone: String,
    var address: String,
    var password: String = "",
    var role: UserRole = UserRole.WARGA,
    var isLoggedIn: Boolean = false
) {
    val roleTitle: String get() = role.label
    val isAdmin: Boolean get() = role != UserRole.WARGA
}
