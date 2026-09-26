package id.or.karangtaruna.kasgo.data.repositories

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import id.or.karangtaruna.kasgo.data.models.OrganizationConfig
import id.or.karangtaruna.kasgo.data.models.PaymentMethodItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OrganizationRepository private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kas_go_org_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<OrganizationConfig> = _config.asStateFlow()

    val current: OrganizationConfig get() = _config.value
    val orgId: String get() = _config.value.orgId
    val organizationTitle: String get() = _config.value.fullTitle
    val activePaymentMethods: List<PaymentMethodItem>
        get() = _config.value.paymentMethods.filter { it.isActive }

    private fun loadConfig(): OrganizationConfig {
        val json = prefs.getString("config_key", null)
        return if (!json.isNullOrBlank()) {
            try {
                gson.fromJson(json, OrganizationConfig::class.java)
            } catch (_: Exception) {
                OrganizationConfig.initialDefault()
            }
        } else {
            OrganizationConfig.initialDefault()
        }
    }

    private fun persist() {
        val json = gson.toJson(_config.value)
        prefs.edit().putString("config_key", json).apply()
    }

    fun saveConfig(
        orgId: String,
        name: String,
        scopeArea: String,
        description: String? = null,
        contactPhone: String? = null
    ) {
        val currentConfig = _config.value
        currentConfig.orgId = orgId.trim()
        currentConfig.name = name.trim()
        currentConfig.scopeArea = scopeArea.trim()
        if (description != null) currentConfig.description = description.trim()
        if (contactPhone != null) currentConfig.contactPhone = contactPhone.trim()
        _config.value = currentConfig.copy()
        persist()
    }

    fun togglePaymentMethod(id: String, isActive: Boolean) {
        val list = _config.value.paymentMethods.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index >= 0) {
            list[index].isActive = isActive
            _config.value = _config.value.copy(paymentMethods = list)
            persist()
        }
    }

    fun updatePaymentMethod(item: PaymentMethodItem) {
        val list = _config.value.paymentMethods.toMutableList()
        val index = list.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            list[index] = item
        } else {
            list.add(item)
        }
        _config.value = _config.value.copy(paymentMethods = list)
        persist()
    }

    fun deletePaymentMethod(id: String) {
        val list = _config.value.paymentMethods.toMutableList()
        list.removeAll { it.id == id }
        _config.value = _config.value.copy(paymentMethods = list)
        persist()
    }

    companion object {
        @Volatile
        private var instance: OrganizationRepository? = null

        fun initialize(context: Context): OrganizationRepository {
            return instance ?: synchronized(this) {
                instance ?: OrganizationRepository(context.applicationContext).also { instance = it }
            }
        }

        fun get(): OrganizationRepository {
            return checkNotNull(instance) { "OrganizationRepository must be initialized first" }
        }
    }
}
