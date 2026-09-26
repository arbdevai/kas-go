package id.or.karangtaruna.kasgo.services

import com.google.gson.Gson
import com.google.gson.JsonObject
import id.or.karangtaruna.kasgo.data.models.TransactionItem
import id.or.karangtaruna.kasgo.data.repositories.OrganizationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object FirebaseSyncService {
    private const val projectId = "kas-go-app-kt26-e6d6f"
    private const val baseUrl =
        "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents"

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    suspend fun syncDashboardSummary(): Boolean = withContext(Dispatchers.IO) {
        try {
            val orgId = OrganizationRepository.get().orgId
            val url = "$baseUrl/organizations/$orgId/aggregates/dashboard"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            return@withContext response.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    suspend fun pushTransaction(item: TransactionItem): Boolean = withContext(Dispatchers.IO) {
        try {
            val orgId = OrganizationRepository.get().orgId
            val url = "$baseUrl/organizations/$orgId/ledger/${item.id}"

            val fields = JsonObject().apply {
                add("type", JsonObject().apply {
                    addProperty("stringValue", if (item.isIncome) "income" else "expense")
                })
                add("amount", JsonObject().apply {
                    addProperty("integerValue", item.amount.toString())
                })
                add("summary", JsonObject().apply {
                    addProperty("stringValue", item.summary)
                })
                add("recorded_by", JsonObject().apply {
                    addProperty("stringValue", item.recordedByName)
                })
            }
            val root = JsonObject().apply { add("fields", fields) }

            val body = gson.toJson(root).toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).patch(body).build()
            val response = client.newCall(request).execute()
            return@withContext response.isSuccessful
        } catch (_: Exception) {
            false
        }
    }
}
