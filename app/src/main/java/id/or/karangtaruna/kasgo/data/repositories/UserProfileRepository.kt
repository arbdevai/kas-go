package id.or.karangtaruna.kasgo.data.repositories

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth
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
    private val _membersFlow = MutableStateFlow<List<UserProfile>>(emptyList())
    val membersFlow: StateFlow<List<UserProfile>> = _membersFlow.asStateFlow()
    private val _currentUser: MutableStateFlow<UserProfile>
    val currentUser: StateFlow<UserProfile>

    private val _isAuthenticated: MutableStateFlow<Boolean>
    val isAuthenticated: StateFlow<Boolean>

    val current: UserProfile get() = _currentUser.value
    val allMembers: List<UserProfile> get() = _members.toList()

    init {
        loadMembers()
        _membersFlow.value = _members.toList()
        val sessionUser = loadInitialSession()
        _currentUser = MutableStateFlow(sessionUser)
        currentUser = _currentUser.asStateFlow()
        _isAuthenticated = MutableStateFlow(sessionUser.isLoggedIn)
        isAuthenticated = _isAuthenticated.asStateFlow()
    }

    private fun loadInitialSession(): UserProfile {
        val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid ?: return guestUser()
        val json = prefs.getString("session_user", null)
        val user = if (!json.isNullOrBlank()) {
            try {
                gson.fromJson(json, UserProfile::class.java)
            } catch (_: Exception) {
                guestUser()
            }
        } else {
            guestUser()
        }
        if (user.isLoggedIn && user.uid == firebaseUid) {
            val matching = _members.firstOrNull { it.uid == firebaseUid }
                ?: return guestUser()
            return matching.copy(isLoggedIn = true)
        }
        return user
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
        _membersFlow.value = _members.toList()
        _isAuthenticated.value = _currentUser.value.isLoggedIn
    }

    fun loginWithFirebase(profile: UserProfile) {
        profile.isLoggedIn = true
        profile.membershipStatus = "active"
        val index = _members.indexOfFirst { it.uid == profile.uid }
        if (index >= 0) _members[index] = profile.copy() else _members.add(profile.copy())
        _currentUser.value = profile.copy()
        persistSession()
    }

    fun replaceMembersFromCloud(members: List<UserProfile>) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        val currentProfile = _currentUser.value.takeIf { it.isLoggedIn && it.uid == currentUid }
        if (_members.isNotEmpty() && prefs.getString("legacy_members_backup", null) == null) {
            prefs.edit().putString("legacy_members_backup", gson.toJson(_members)).apply()
        }
        _members.clear()
        _members.addAll(members)
        if (currentProfile != null) {
            val refreshed = _members.firstOrNull { it.uid == currentUid } ?: currentProfile
            _currentUser.value = refreshed.copy(isLoggedIn = true)
            persistSession()
        } else {
            prefs.edit().putString("members_list", gson.toJson(_members)).apply()
            _membersFlow.value = _members.toList()
        }
    }

    suspend fun logout() {
        runCatching { id.or.karangtaruna.kasgo.services.FirebaseSyncService.unregisterCurrentDeviceToken() }
        id.or.karangtaruna.kasgo.services.FirebaseSyncService.stopLiveSync()
        FirebaseAuth.getInstance().signOut()
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
        id.or.karangtaruna.kasgo.services.FirebaseSyncService.updateMemberProfile(
            current.uid,
            current.name,
            current.phone,
            current.address
        )
    }

    suspend fun updateRoleForMember(uid: String, role: UserRole) {
        check(_currentUser.value.isAdmin) { "Hanya pengurus yang dapat mengubah status anggota" }
        val orgId = OrganizationRepository.get().orgId
        id.or.karangtaruna.kasgo.services.FirebaseSyncService.setMemberRole(uid, role, orgId)
        replaceMembersFromCloud(id.or.karangtaruna.kasgo.services.FirebaseSyncService.loadMembers(orgId))
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
