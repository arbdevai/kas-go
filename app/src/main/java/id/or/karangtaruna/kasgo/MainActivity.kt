package id.or.karangtaruna.kasgo

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import id.or.karangtaruna.kasgo.data.repositories.UserProfileRepository
import id.or.karangtaruna.kasgo.ui.components.TopToastHost
import id.or.karangtaruna.kasgo.ui.screens.auth.AuthScreen
import id.or.karangtaruna.kasgo.ui.screens.main.MainScreen
import id.or.karangtaruna.kasgo.ui.screens.splash.SplashScreen
import id.or.karangtaruna.kasgo.ui.theme.KasGoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KasGoTheme {
                val userRepo = remember { UserProfileRepository.get() }
                val isAuthenticated by userRepo.isAuthenticated.collectAsState()
                var showSplash by remember { mutableStateOf(true) }
                val notificationPermission = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { }
                val notificationPrefs = remember {
                    getSharedPreferences("kas_go_notification_prefs", MODE_PRIVATE)
                }
                androidx.compose.runtime.LaunchedEffect(isAuthenticated, showSplash) {
                    if (!showSplash && isAuthenticated && Build.VERSION.SDK_INT >= 33 &&
                        !notificationPrefs.getBoolean("permission_requested", false) &&
                        ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPrefs.edit().putBoolean("permission_requested", true).apply()
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Crossfade(
                        targetState = showSplash,
                        animationSpec = tween(320),
                        label = "AppCrossfade"
                    ) { isSplash ->
                        if (isSplash) {
                            SplashScreen(onFinish = { showSplash = false })
                        } else {
                            if (!isAuthenticated) {
                                AuthScreen()
                            } else {
                                MainScreen()
                            }
                        }
                    }

                    // Global Top Floating Toast Host (Melayang di atas status bar / AppBar)
                    TopToastHost()
                }
            }
        }
    }
}
