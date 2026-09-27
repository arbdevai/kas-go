package id.or.karangtaruna.kasgo.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import id.or.karangtaruna.kasgo.data.repositories.UserProfileRepository

class KasGoMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        val profile = runCatching { UserProfileRepository.get().current }.getOrNull()
        if (profile?.isLoggedIn == true) FirebaseSyncService.registerNotificationToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: "Kas Go"
        val body = message.notification?.body ?: message.data["message"] ?: return
        KasGoNotifications.show(this, title, body, message.data)
    }
}
