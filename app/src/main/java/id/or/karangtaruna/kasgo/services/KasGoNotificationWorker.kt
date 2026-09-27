package id.or.karangtaruna.kasgo.services

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import id.or.karangtaruna.kasgo.data.repositories.UserProfileRepository
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

/** Periodically checks only this signed-in member's updates; no server credentials are needed. */
class KasGoNotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = runCatching {
        val uid = inputData.getString(KEY_UID) ?: return Result.success()
        val orgId = inputData.getString(KEY_ORG) ?: return Result.success()
        val authUser = FirebaseAuth.getInstance().currentUser
        val profile = UserProfileRepository.get().current
        if (authUser?.uid != uid || !profile.isLoggedIn || profile.uid != uid) return Result.success()

        val admin = inputData.getBoolean(KEY_ADMIN, false)
        val prefs = applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val cursorKey = "cursor_${orgId}_$uid"
        val cursor = prefs.getLong(cursorKey, System.currentTimeMillis())
        val scanStartedAt = System.currentTimeMillis()
        val org = FirebaseFirestore.getInstance().collection("organizations").document(orgId)

        val entries = org.collection("bill_entries")
            .let { if (admin) it else it.whereEqualTo("member_uid", uid) }
            .whereGreaterThan("updated_at_millis", cursor)
            .get().await()
        val pickups = org.collection("pickup_requests")
            .let { if (admin) it else it.whereEqualTo("member_uid", uid) }
            .whereGreaterThan("updated_at_millis", cursor)
            .get().await()

        entries.documents.forEach { doc ->
            val status = doc.getString("status") ?: return@forEach
            if (admin && status == "menunggu_verifikasi") {
                KasGoNotifications.show(
                    applicationContext, "Pembayaran perlu diverifikasi",
                    "${doc.getString("member_name") ?: "Warga"} mengirim bukti pembayaran.",
                    mapOf("type" to "payment_verification", "entry_id" to doc.id)
                )
            } else if (!admin && status == "lunas") {
                KasGoNotifications.show(
                    applicationContext, "Pembayaran terverifikasi",
                    "Pembayaran kas ${doc.getString("period") ?: "warga"} sudah dikonfirmasi.",
                    mapOf("type" to "payment_confirmed", "entry_id" to doc.id)
                )
            } else if (!admin && status == "belum_bayar" && doc.getLong("paid_at_millis") != null) {
                KasGoNotifications.show(
                    applicationContext, "Pembayaran belum terverifikasi",
                    "Silakan periksa kembali pembayaran kas ${doc.getString("period") ?: "Anda"}.",
                    mapOf("type" to "payment_rejected", "entry_id" to doc.id)
                )
            }
        }

        pickups.documents.forEach { doc ->
            val status = doc.getString("status") ?: return@forEach
            val createdAt = doc.getLong("created_at_millis") ?: 0L
            val updatedAt = doc.getLong("updated_at_millis") ?: createdAt
            if (admin && createdAt > cursor) {
                KasGoNotifications.show(
                    applicationContext, "Permintaan jemput baru",
                    "${doc.getString("name") ?: "Warga"} meminta penjemputan iuran.",
                    mapOf("type" to "pickup_new", "request_id" to doc.id)
                )
            } else if (!admin && updatedAt > cursor && status != "Menunggu") {
                KasGoNotifications.show(
                    applicationContext, "Status penjemputan diperbarui", status,
                    mapOf("type" to "pickup_status", "request_id" to doc.id)
                )
            }
        }

        if (admin) {
            val newMembers = org.collection("members")
                .whereEqualTo("status", "pending")
                .whereGreaterThan("created_at_millis", cursor)
                .get().await()
            newMembers.documents.forEach { doc ->
                KasGoNotifications.show(
                    applicationContext, "Pendaftaran warga baru",
                    "${doc.getString("name") ?: "Warga"} menunggu verifikasi akun.",
                    mapOf("type" to "member_registration", "uid" to doc.id)
                )
            }
        }

        // Advance only to the scan start so updates made while these queries run are picked up next time.
        prefs.edit().putLong(cursorKey, scanStartedAt - 1L).apply()
        Result.success()
    }.getOrElse { Result.retry() }

    companion object {
        private const val KEY_UID = "uid"
        private const val KEY_ORG = "org_id"
        private const val KEY_ADMIN = "is_admin"
        private const val PREFS = "kas_go_notification_sync"
        private const val WORK_PREFIX = "kas_go_notification_poll_"

        fun schedule(orgId: String, profile: id.or.karangtaruna.kasgo.data.models.UserProfile) {
            // Cursor starts when this account is first activated, so old records never fire as alerts.
            val context = id.or.karangtaruna.kasgo.KasGoApplication.instance
            val cursorKey = "cursor_${orgId}_${profile.uid}"
            val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            if (!preferences.contains(cursorKey)) {
                preferences.edit().putLong(cursorKey, System.currentTimeMillis()).apply()
            }
            val request = PeriodicWorkRequestBuilder<KasGoNotificationWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(androidx.work.workDataOf(
                    KEY_UID to profile.uid,
                    KEY_ORG to orgId,
                    KEY_ADMIN to profile.isAdmin
                ))
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_PREFIX + profile.uid,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel() {
            val uid = UserProfileRepository.get().current.uid
            if (uid != "guest") {
                WorkManager.getInstance(id.or.karangtaruna.kasgo.KasGoApplication.instance)
                    .cancelUniqueWork(WORK_PREFIX + uid)
            }
        }
    }
}
