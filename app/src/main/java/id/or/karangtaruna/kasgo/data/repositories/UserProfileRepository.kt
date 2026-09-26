package id.or.karangtaruna.kasgo.data.repositories

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import id.or.karangtaruna.kasgo.data.models.UserProfile
import id.or.karangtaruna.kasgo.data.models.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserProfileRepository private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kas_go_user_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _members = mutableListOf<UserProfile>()
    private val _currentUser = MutableStateFlow(loadInitialSession())
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(_currentUser.value.isLoggedIn)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    val current: UserProfile get() = _currentUser.value
    val allMembers: List<UserProfile> get() = _members.toList()

    init {
        loadMembers()
    }

    private fun loadInitialSession(): UserProfile {
        val json = prefs.getString("session_user", null)
        return if (!json.isNullOrBlank()) {
            try {
                gson.fromJson(json, UserProfile::class.java)
            } catch (_: Exception) {
                guestUser()
            }
        } else {
            guestUser()
        }
    }

    private fun guestUser() = UserProfile(
        uid = "guest",
        name = "Tamu",
        email = "",
        phone = "",
        address = "",
        role = UserRole.WARGA,
        isLoggedIn = false
    )

    private fun loadMembers() {
        val json = prefs.getString("members_list", null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<UserProfile>>() {}.type
                val list: List<UserProfile> = gson.fromJson(json, type)
                _members.clear()
                _members.addAll(list)
            } catch (_: Exception) {}
        }
    }

    private fun persistSession() {
        val userJson = gson.toJson(_currentUser.value)
        val membersJson = gson.toJson(_members)
        prefs.edit()
            .putString("session_user", userJson)
            .putString("members_list", membersJson)
            .apply()
        _isAuthenticated.value = _currentUser.value.isLoggedIn
    }

    fun checkGoogleAccount(email: String): UserProfile? {
        val cleanEmail = email.trim().lowercase()
        return _members.firstOrNull { it.email.lowercase() == cleanEmail }
    }

    fun loginWithExistingGoogle(profile: UserProfile) {
        profile.isLoggedIn = true
        _currentUser.value = profile.copy(isLoggedIn = true)
        persistSession()
    }

    fun registerWithGoogle(
        email: String,
        name: String,
        phone: String,
        address: String
    ): String? {
        val cleanEmail = email.trim().lowercase()
        val cleanPhone = phone.trim()

        val existing = _members.firstOrNull {
            (cleanEmail.isNotEmpty() && it.email.lowercase() == cleanEmail) ||
            (cleanPhone.isNotEmpty() && it.phone == cleanPhone)
        }

        if (existing != null) {
            existing.name = name.trim()
            existing.phone = cleanPhone
            existing.address = address.trim()
            existing.isLoggedIn = true
            _currentUser.value = existing.copy()
            persistSession()
            return null
        }

        val newProfile = UserProfile(
            uid = "u_${System.currentTimeMillis()}",
            name = name.trim(),
            email = cleanEmail,
            phone = cleanPhone,
            address = address.trim(),
            role = UserRole.WARGA,
            isLoggedIn = true
        )

        _members.add(newProfile)
        _currentUser.value = newProfile
        persistSession()
        return null
    }

    fun loginWithAdminPin(pin: String): Boolean {
        if (pin.trim() == "123456" || pin.trim() == "admin123") {
            val existingAdmin = _members.firstOrNull { it.role == UserRole.ADMIN1 }
                ?: UserProfile(
                    uid = "admin_root",
                    name = "Bendahara Kas",
                    email = "admin@kasgo.id",
                    phone = "0812-0000-0000",
                    address = "Kantor Kas",
                    role = UserRole.ADMIN1,
                    isLoggedIn = true
                ).also { _members.add(it) }

            existingAdmin.isLoggedIn = true
            _currentUser.value = existingAdmin.copy(isLoggedIn = true)
            persistSession()
            return true
        }
        return false
    }

    fun logout() {
        val guest = guestUser()
        _currentUser.value = guest
        _members.forEach { it.isLoggedIn = false }
        persistSession()
    }

    fun updateProfile(name: String, phone: String, address: String) {
        val current = _currentUser.value
        current.name = name.trim()
        current.phone = phone.trim()
        current.address = address.trim()
        val idx = _members.indexOfFirst { it.uid == current.uid }
        if (idx >= 0) {
            _members[idx] = current.copy()
        }
        _currentUser.value = current.copy()
        persistSession()
    }

    fun updateRoleForMember(uid: String, role: UserRole) {
        val idx = _members.indexOfFirst { it.uid == uid }
        if (idx >= 0) {
            _members[idx].role = role
            if (_currentUser.value.uid == uid) {
                _currentUser.value = _currentUser.value.copy(role = role)
            }
            persistSession()
        }
    }

    companion object {
        @Volatile
        private var instance: UserProfileRepository? = null

        fun initialize(context: Context): UserProfileRepository {
            return instance ?: synchronized(this) {
                instance ?: UserProfileRepository(context.applicationContext).also { instance = it }
            }
        }

        fun get(): UserProfileRepository {
            return checkNotNull(instance) { "UserProfileRepository must be initialized first" }
        }
    }
}
