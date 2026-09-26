package id.or.karangtaruna.kasgo

import android.os.Bundle
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
