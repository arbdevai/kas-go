package id.or.karangtaruna.kasgo.data.models

data class AppUpdateInfo(
    val remoteVersion: String,
    val currentVersion: String,
    val hasUpdate: Boolean,
    val title: String,
    val changelog: String,
    val apkUrl: String,
    val htmlUrl: String
)
