package id.or.karangtaruna.kasgo.services

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.JsonObject
import id.or.karangtaruna.kasgo.core.utils.AppToast
import id.or.karangtaruna.kasgo.data.models.AppUpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object AppUpdateService {
    const val currentVersion = "2.0.1"
    private const val repoOwner = "arbdevai"
    private const val repoName = "kas-go"

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    suspend fun checkUpdate(): AppUpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.github.com/repos/$repoOwner/$repoName/releases/latest"
            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: return@withContext null
                val json = gson.fromJson(bodyStr, JsonObject::class.java)

                val tagName = json.get("tag_name")?.asString?.replace("v", "") ?: ""
                val title = json.get("name")?.asString ?: "Pembaruan Tersedia"
                val changelog = json.get("body")?.asString ?: "Pembaruan stabilitas dan performa."
                val htmlUrl = json.get("html_url")?.asString ?: "https://github.com/$repoOwner/$repoName/releases"

                var apkUrl = htmlUrl
                val assets = json.getAsJsonArray("assets")
                if (assets != null) {
                    for (element in assets) {
                        val obj = element.asJsonObject
                        val name = obj.get("name")?.asString ?: ""
                        if (name.endsWith(".apk")) {
                            apkUrl = obj.get("browser_download_url")?.asString ?: apkUrl
                            break
                        }
                    }
                }

                val hasUpdate = isNewer(tagName, currentVersion)
                return@withContext AppUpdateInfo(
                    remoteVersion = tagName,
                    currentVersion = currentVersion,
                    hasUpdate = hasUpdate,
                    title = title,
                    changelog = changelog,
                    apkUrl = apkUrl,
                    htmlUrl = htmlUrl
                )
            }
        } catch (_: Exception) {}
        null
    }

    private fun isNewer(remote: String, current: String): Boolean {
        return try {
            val rParts = remote.split(".").map { it.toInt() }
            val cParts = current.split(".").map { it.toInt() }
            for (i in 0 until minOf(rParts.size, cParts.size)) {
                if (rParts[i] > cParts[i]) return true
                if (rParts[i] < cParts[i]) return false
            }
            rParts.size > cParts.size
        } catch (_: Exception) {
            remote.isNotBlank() && remote != current
        }
    }

    fun launchDownload(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            AppToast.success("Membuka unduhan APK di peramban...")
        } catch (_: Exception) {
            copyToClipboard(context, url)
            AppToast.info("Tautan unduh APK disalin ke papan klip.")
        }
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Kas Go APK", text)
        clipboard.setPrimaryClip(clip)
    }
}
