package id.or.karangtaruna.kasgo

import android.app.Application
import id.or.karangtaruna.kasgo.data.repositories.BillingRepository
import id.or.karangtaruna.kasgo.data.repositories.FinanceRepository
import id.or.karangtaruna.kasgo.data.repositories.OrganizationRepository
import id.or.karangtaruna.kasgo.data.repositories.UserProfileRepository

class KasGoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        OrganizationRepository.initialize(this)
        UserProfileRepository.initialize(this)
        FinanceRepository.initialize(this)
        BillingRepository.initialize(this)
    }
}
