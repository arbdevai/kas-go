package id.or.karangtaruna.kasgo.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import id.or.karangtaruna.kasgo.data.models.LedgerType
import id.or.karangtaruna.kasgo.data.models.MemberBillEntry
import id.or.karangtaruna.kasgo.data.models.MonthlyBill
import id.or.karangtaruna.kasgo.data.models.OrganizationConfig
import id.or.karangtaruna.kasgo.data.models.PaymentMethodItem
import id.or.karangtaruna.kasgo.data.models.PickupItem
import id.or.karangtaruna.kasgo.data.models.TransactionItem
import id.or.karangtaruna.kasgo.data.models.UserProfile
import id.or.karangtaruna.kasgo.data.models.UserRole
import id.or.karangtaruna.kasgo.data.repositories.FinanceRepository
import id.or.karangtaruna.kasgo.data.repositories.BillingRepository
import id.or.karangtaruna.kasgo.data.repositories.OrganizationRepository
import id.or.karangtaruna.kasgo.data.repositories.UserProfileRepository
import kotlinx.coroutines.tasks.await

object FirebaseSyncService {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private var registrations = mutableListOf<ListenerRegistration>()

    sealed interface MemberResolution {
        data class Active(val profile: UserProfile) : MemberResolution
        data object Pending : MemberResolution
        data object NeedsRegistration : MemberResolution
    }

    private fun organization(orgId: String = OrganizationRepository.get().orgId) =
        db.collection("organizations").document(orgId)

    suspend fun resolveSignedInMember(user: FirebaseUser, orgId: String): MemberResolution {
        val memberRef = organization(orgId).collection("members").document(user.uid)
        val member = memberRef.get().await()
        if (member.exists()) {
            if (member.getString("status") != "active") return MemberResolution.Pending
            return MemberResolution.Active(memberProfile(member.id, member.data.orEmpty(), user))
        }

        val adminSlots = organization(orgId).collection("config").document("admin_slots")
            .get().await()
        val adminEmails = adminSlots.get("emails") as? List<*> ?: emptyList<Any>()
        val adminUids = adminSlots.get("uids") as? List<*> ?: emptyList<Any>()
        val isBootstrapAdmin = user.email?.lowercase()?.let { it in adminEmails } == true || user.uid in adminUids
        if (isBootstrapAdmin) {
            val profile = UserProfile(
                uid = user.uid,
                name = user.displayName?.takeIf(String::isNotBlank) ?: user.email.orEmpty(),
                email = user.email.orEmpty(),
                phone = user.phoneNumber.orEmpty(),
                address = "",
                role = UserRole.ADMIN1,
                isLoggedIn = true
            )
            memberRef.set(profile.toCloudMap(status = "active")).await()
            return MemberResolution.Active(profile)
        }
        return MemberResolution.NeedsRegistration
    }

    suspend fun registerPendingMember(
        uid: String,
        email: String,
        name: String,
        phone: String,
        address: String,
        orgId: String
    ) {
        organization(orgId).collection("members").document(uid).set(
            mapOf(
                "uid" to uid,
                "name" to name.trim(),
                "email" to email.trim().lowercase(),
                "phone" to phone.trim(),
                "address" to address.trim(),
                "role" to UserRole.WARGA.code,
                "status" to "pending",
                "created_at_millis" to System.currentTimeMillis()
            )
        ).await()
    }

    suspend fun loadMembers(orgId: String): List<UserProfile> {
        return organization(orgId).collection("members").get().await().documents
            .map { memberProfile(it.id, it.data.orEmpty(), null) }
    }

    suspend fun syncDashboardSummary(): Boolean = runCatching {
        organization().collection("aggregates").document("dashboard").get().await()
    }.isSuccess

    suspend fun setMemberRole(uid: String, role: UserRole, orgId: String) {
        val ref = organization(orgId).collection("members").document(uid)
        val slotsRef = organization(orgId).collection("config").document("admin_slots")
        db.runTransaction { transaction ->
            transaction.get(ref)
            val slots = transaction.get(slotsRef)
            val uids = (slots.get("uids") as? List<*>)
                .orEmpty().filterIsInstance<String>().toMutableList()
            val emails = (slots.get("emails") as? List<*>)
                .orEmpty().filterIsInstance<String>()
            if (role == UserRole.WARGA) {
                uids.remove(uid)
            } else if (uid !in uids) {
                require(uids.size + emails.size < 3) { "Slot pengurus sudah penuh" }
                uids.add(uid)
            }
            transaction.update(ref, mapOf("role" to role.code, "status" to "active"))
            transaction.update(slotsRef, "uids", uids)
            true
        }.await()
    }

    fun updatePickupStatus(id: String, status: String) {
        organization().collection("pickup_requests").document(id).update(
            mapOf("status" to status, "updated_at_millis" to System.currentTimeMillis())
        )
    }

    fun updateMemberProfile(uid: String, name: String, phone: String, address: String) {
        organization().collection("members").document(uid).update(
            mapOf("name" to name.take(100), "phone" to phone.take(32), "address" to address.take(240))
        )
    }

    fun pushOrganizationConfig(config: OrganizationConfig) {
        if (auth.currentUser == null) return
        val methods = config.paymentMethods.map { method ->
            mapOf(
                "id" to method.id.take(80),
                "type" to method.type.take(32),
                "title" to method.title.take(100),
                "account_number" to method.accountNumber.take(100),
                "account_name" to method.accountName.take(100),
                "is_active" to method.isActive,
                "qr_image_url" to method.qrImageUrl?.take(500),
                "instructions" to method.instructions?.take(500)
            )
        }
        organization(config.orgId).collection("config").document("payment_methods").set(
            mapOf(
                "name" to config.name.take(100),
                "scope_area" to config.scopeArea.take(100),
                "description" to config.description?.take(500),
                "contact_phone" to config.contactPhone?.take(32),
                "payment_methods" to methods
            )
        )
    }

    fun startLiveSync(orgId: String, profile: UserProfile) {
        registrations.forEach(ListenerRegistration::remove)
        registrations.clear()
        val isAdmin = profile.isAdmin
        KasGoNotificationWorker.schedule(orgId, profile)
        val finance = FinanceRepository.get()
        val org = organization(orgId)
        if (isAdmin) migrateLocalAdminData(orgId)

        val ledgerQuery = org.collection("ledger")
            .orderBy("occurred_at_millis", Query.Direction.DESCENDING)
        registrations += ledgerQuery.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || (snapshot.metadata.isFromCache && snapshot.isEmpty)) return@addSnapshotListener
            finance.replaceTransactionsFromCloud(snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                TransactionItem(
                    id = doc.id,
                    entryType = if (data["entry_type"] == "income") LedgerType.INCOME else LedgerType.EXPENSE,
                    amount = (data["amount"] as? Number)?.toLong() ?: 0L,
                    summary = data["summary"] as? String ?: "",
                    recordedByName = data["recorded_by"] as? String ?: "",
                    occurredAtMillis = (data["occurred_at_millis"] as? Number)?.toLong() ?: 0L,
                    period = data["period"] as? String,
                    category = data["category"] as? String,
                    paymentMethod = data["payment_method"] as? String ?: "Tunai",
                    recordedByUid = data["recorded_by_uid"] as? String,
                    editedByName = data["edited_by_name"] as? String,
                    editedAtMillis = (data["edited_at_millis"] as? Number)?.toLong(),
                    editReason = data["edit_reason"] as? String,
                    billingId = data["billing_id"] as? String,
                    correctionOfTransactionId = data["correction_of_transaction_id"] as? String
                ).takeIf { it.amount > 0 }
            })
        }

        val pickupQuery = org.collection("pickup_requests").let { collection ->
            if (isAdmin) collection.orderBy("created_at_millis", Query.Direction.DESCENDING)
            else collection.whereEqualTo("member_uid", profile.uid)
                .orderBy("created_at_millis", Query.Direction.DESCENDING)
        }
        registrations += pickupQuery.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || (snapshot.metadata.isFromCache && snapshot.isEmpty)) return@addSnapshotListener
            if (!snapshot.metadata.isFromCache && !snapshot.metadata.hasPendingWrites) {
                snapshot.documentChanges.filter { it.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED }
                    .forEach { change ->
                        if (!isAdmin) {
                            val status = change.document.getString("status") ?: return@forEach
                            KasGoNotifications.show(
                                id.or.karangtaruna.kasgo.KasGoApplication.instance,
                                "Status penjemputan diperbarui",
                                status,
                                mapOf("type" to "pickup_status", "request_id" to change.document.id)
                            )
                        }
                    }
            }
            finance.replacePickupRequestsFromCloud(snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                PickupItem(
                    id = doc.id,
                    name = data["name"] as? String ?: "",
                    address = data["address"] as? String ?: "",
                    phone = data["phone"] as? String ?: "",
                    amount = (data["amount"] as? Number)?.toLong() ?: 0L,
                    timeSlot = data["time_slot"] as? String ?: "",
                    createdAtMillis = (data["created_at_millis"] as? Number)?.toLong() ?: 0L,
                    status = data["status"] as? String ?: "Menunggu",
                    memberUid = data["member_uid"] as? String ?: ""
                )
            })
        }

        if (isAdmin) {
            registrations += org.collection("members").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || (snapshot.metadata.isFromCache && snapshot.isEmpty)) return@addSnapshotListener
                UserProfileRepository.get().replaceMembersFromCloud(
                    snapshot.documents.map { memberProfile(it.id, it.data.orEmpty(), null) }
                )
            }
        }

        registrations += org.collection("config").document("payment_methods")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val methods = (snapshot.get("payment_methods") as? List<*>)
                    .orEmpty().mapNotNull { raw ->
                        val item = raw as? Map<*, *> ?: return@mapNotNull null
                        PaymentMethodItem(
                            id = item["id"] as? String ?: return@mapNotNull null,
                            type = item["type"] as? String ?: "custom",
                            title = item["title"] as? String ?: "",
                            accountNumber = item["account_number"] as? String ?: "",
                            accountName = item["account_name"] as? String ?: "",
                            isActive = item["is_active"] as? Boolean ?: false,
                            qrImageUrl = item["qr_image_url"] as? String,
                            instructions = item["instructions"] as? String
                        )
                    }.toMutableList()
                OrganizationRepository.get().replaceFromCloud(
                    OrganizationConfig(
                        orgId = orgId,
                        name = snapshot.getString("name") ?: "Karang Taruna",
                        scopeArea = snapshot.getString("scope_area") ?: "Unit Pengurus Kas",
                        description = snapshot.getString("description") ?: "",
                        contactPhone = snapshot.getString("contact_phone"),
                        paymentMethods = methods
                    )
                )
            }

        val billQuery = org.collection("bills").orderBy("created_at_millis", Query.Direction.DESCENDING)
        registrations += billQuery.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || (snapshot.metadata.isFromCache && snapshot.isEmpty)) return@addSnapshotListener
            BillingRepository.get().replaceBillsFromCloud(snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                MonthlyBill(
                    id = doc.id,
                    title = data["title"] as? String ?: "",
                    period = data["period"] as? String ?: "",
                    amount = (data["amount"] as? Number)?.toLong() ?: 0L,
                    dueDateMillis = (data["due_date_millis"] as? Number)?.toLong() ?: 0L,
                    createdByName = data["created_by_name"] as? String ?: "",
                    createdAtMillis = (data["created_at_millis"] as? Number)?.toLong() ?: 0L,
                    description = data["description"] as? String,
                    isPublished = data["is_published"] as? Boolean ?: true
                )
            })
        }

        val entriesQuery = org.collection("bill_entries").let { collection ->
            if (isAdmin) collection.orderBy("period", Query.Direction.DESCENDING)
            else collection.whereEqualTo("member_uid", profile.uid)
                .orderBy("period", Query.Direction.DESCENDING)
        }
        registrations += entriesQuery.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || (snapshot.metadata.isFromCache && snapshot.isEmpty)) return@addSnapshotListener
            if (!snapshot.metadata.isFromCache && !snapshot.metadata.hasPendingWrites) {
                snapshot.documentChanges.filter { it.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED }
                    .forEach { change ->
                        val status = change.document.getString("status") ?: return@forEach
                        val (title, body, type) = when {
                            isAdmin && status == "menunggu_verifikasi" -> Triple(
                                "Pembayaran perlu diverifikasi",
                                "${change.document.getString("member_name") ?: "Warga"} mengirim bukti pembayaran.",
                                "payment_verification"
                            )
                            !isAdmin && status == "lunas" -> Triple(
                                "Pembayaran terverifikasi",
                                "Pembayaran kas ${change.document.getString("period") ?: "Anda"} sudah dikonfirmasi.",
                                "payment_confirmed"
                            )
                            !isAdmin && status == "belum_bayar" -> Triple(
                                "Pembayaran belum terverifikasi",
                                "Silakan periksa kembali pembayaran kas ${change.document.getString("period") ?: "Anda"}.",
                                "payment_rejected"
                            )
                            else -> return@forEach
                        }
                        KasGoNotifications.show(
                            id.or.karangtaruna.kasgo.KasGoApplication.instance,
                            title,
                            body,
                            mapOf("type" to type, "entry_id" to change.document.id)
                        )
                    }
            }
            BillingRepository.get().replaceEntriesFromCloud(snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                MemberBillEntry(
                    id = doc.id,
                    billId = data["bill_id"] as? String ?: "",
                    memberId = data["member_uid"] as? String ?: "",
                    memberName = data["member_name"] as? String ?: "",
                    amount = (data["amount"] as? Number)?.toLong() ?: 0L,
                    period = data["period"] as? String ?: "",
                    status = id.or.karangtaruna.kasgo.data.models.BillPaymentStatus.fromCode(
                        data["status"] as? String ?: "belum_bayar"
                    ),
                    paidAtMillis = (data["paid_at_millis"] as? Number)?.toLong(),
                    paymentMethod = data["payment_method"] as? String,
                    recordedByName = data["recorded_by_name"] as? String,
                    transactionId = data["transaction_id"] as? String
                )
            })
        }
    }

    fun stopLiveSync() {
        registrations.forEach(ListenerRegistration::remove)
        registrations.clear()
        KasGoNotificationWorker.cancel()
    }

    private fun migrateLocalAdminData(orgId: String) {
        val org = organization(orgId)
        val localTransactions = FinanceRepository.get().transactions.value.toList()
        if (localTransactions.isNotEmpty()) {
            org.collection("ledger").get().addOnSuccessListener { remote ->
                val remoteIds = remote.documents.mapTo(hashSetOf()) { it.id }
                localTransactions.filterNot { it.id in remoteIds }.forEach(::pushTransaction)
            }
        }

        val billing = BillingRepository.get()
        val localBills = billing.allBills.value.toList()
        val localEntries = billing.allEntries.value.toList()
        if (localBills.isNotEmpty()) {
            org.collection("bills").get().addOnSuccessListener { remote ->
                val remoteIds = remote.documents.mapTo(hashSetOf()) { it.id }
                localBills.filterNot { it.id in remoteIds }.forEach { bill ->
                    pushBill(bill, localEntries.filter { it.billId == bill.id })
                }
            }
        }

        val localConfig = OrganizationRepository.get().current
        org.collection("config").document("payment_methods").get().addOnSuccessListener { remote ->
            if (!remote.exists()) pushOrganizationConfig(localConfig)
        }
    }

    fun pushTransaction(item: TransactionItem) {
        val user = auth.currentUser ?: return
        val fields = hashMapOf<String, Any?>(
            "entry_type" to if (item.isIncome) "income" else "expense",
            "amount" to item.amount,
            "summary" to item.summary.take(240),
            "recorded_by" to item.recordedByName.take(100),
            "occurred_at_millis" to item.occurredAtMillis,
            "period" to item.period,
            "category" to item.category,
            "payment_method" to item.paymentMethod.take(80),
            "recorded_by_uid" to user.uid,
            "edited_by_name" to item.editedByName?.take(100),
            "edited_at_millis" to item.editedAtMillis,
            "edit_reason" to item.editReason?.take(240),
            "billing_id" to item.billingId,
            "correction_of_transaction_id" to item.correctionOfTransactionId
        )
        organization().collection("ledger").document(item.id).set(fields)
    }

    fun pushPickupRequest(item: PickupItem) {
        organization().collection("pickup_requests").document(item.id).set(
            mapOf(
                "name" to item.name.take(100),
                "address" to item.address.take(240),
                "phone" to item.phone.take(32),
                "amount" to item.amount,
                "time_slot" to item.timeSlot.take(80),
                "created_at_millis" to item.createdAtMillis,
                "updated_at_millis" to item.createdAtMillis,
                "status" to "Menunggu",
                "member_uid" to (auth.currentUser?.uid ?: item.memberUid)
            )
        )
    }

    fun pushBill(bill: MonthlyBill, entries: List<MemberBillEntry>) {
        val org = organization()
        val batch = db.batch()
        batch.set(org.collection("bills").document(bill.id),
            mapOf(
                "title" to bill.title.take(100),
                "period" to bill.period,
                "amount" to bill.amount,
                "due_date_millis" to bill.dueDateMillis,
                "created_by_name" to bill.createdByName.take(100),
                "created_at_millis" to bill.createdAtMillis,
                "description" to bill.description?.take(240),
                "is_published" to bill.isPublished
            )
        )
        entries.forEach { entry ->
            batch.set(org.collection("bill_entries").document(entry.id),
                mapOf(
                    "bill_id" to entry.billId,
                    "member_uid" to entry.memberId,
                    "member_name" to entry.memberName.take(100),
                    "amount" to entry.amount,
                    "period" to entry.period,
                    "status" to entry.status.code,
                    "paid_at_millis" to entry.paidAtMillis,
                    "updated_at_millis" to System.currentTimeMillis(),
                    "payment_method" to entry.paymentMethod,
                    "recorded_by_name" to entry.recordedByName,
                    "transaction_id" to entry.transactionId
                )
            )
        }
        batch.commit()
    }

    fun updateBillEntry(entry: MemberBillEntry) {
        val fields = if (UserProfileRepository.get().current.isAdmin) {
            mapOf(
                "status" to entry.status.code,
                "payment_method" to entry.paymentMethod,
                "paid_at_millis" to entry.paidAtMillis,
                "recorded_by_name" to entry.recordedByName,
                "transaction_id" to entry.transactionId,
                "updated_at_millis" to System.currentTimeMillis()
            )
        } else {
            mapOf(
                "status" to entry.status.code,
                "payment_method" to entry.paymentMethod,
                "paid_at_millis" to entry.paidAtMillis,
                "updated_at_millis" to System.currentTimeMillis()
            )
        }
        organization().collection("bill_entries").document(entry.id).update(fields)
    }

    private fun memberProfile(id: String, data: Map<String, Any?>, authUser: FirebaseUser?): UserProfile {
        return UserProfile(
            uid = id,
            name = data["name"] as? String ?: authUser?.displayName.orEmpty(),
            email = data["email"] as? String ?: authUser?.email.orEmpty(),
            phone = data["phone"] as? String ?: authUser?.phoneNumber.orEmpty(),
            address = data["address"] as? String ?: "",
            role = UserRole.fromCode(data["role"] as? String ?: UserRole.WARGA.code),
            isLoggedIn = false,
            membershipStatus = data["status"] as? String ?: "pending"
        )
    }

    private fun UserProfile.toCloudMap(status: String) = mapOf(
        "uid" to uid,
        "name" to name.take(100),
        "email" to email.trim().lowercase(),
        "phone" to phone.take(32),
        "address" to address.take(240),
        "role" to role.code,
        "status" to status,
        "created_at_millis" to System.currentTimeMillis()
    )
}
