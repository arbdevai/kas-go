package id.or.karangtaruna.kasgo

import android.app.Application
import id.or.karangtaruna.kasgo.data.repositories.BillingRepository
import id.or.karangtaruna.kasgo.data.repositories.FinanceRepository
import id.or.karangtaruna.kasgo.data.repositories.OrganizationRepository
import id.or.karangtaruna.kasgo.data.repositories.UserProfileRepository
import id.or.karangtaruna.kasgo.services.FirebaseSyncService
import id.or.karangtaruna.kasgo.services.KasGoNotifications

class KasGoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        OrganizationRepository.initialize(this)
        UserProfileRepository.initialize(this)
        FinanceRepository.initialize(this)
        BillingRepository.initialize(this)
        KasGoNotifications.createChannel(this)
        val signedInProfile = UserProfileRepository.get().current
        if (signedInProfile.isLoggedIn) {
            FirebaseSyncService.startLiveSync(OrganizationRepository.get().orgId, signedInProfile)
        }
    }
}
