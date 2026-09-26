package id.or.karangtaruna.kasgo

import android.graphics.Bitmap
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import id.or.karangtaruna.kasgo.core.constants.AppColors
import id.or.karangtaruna.kasgo.ui.screens.admin.*
import id.or.karangtaruna.kasgo.ui.screens.auth.AuthScreen
import id.or.karangtaruna.kasgo.ui.screens.dashboard.DashboardScreen
import id.or.karangtaruna.kasgo.ui.screens.ledger.LedgerScreen
import id.or.karangtaruna.kasgo.ui.screens.payments.PaymentHubScreen
import id.or.karangtaruna.kasgo.ui.screens.profile.ProfileScreen
import id.or.karangtaruna.kasgo.ui.screens.splash.SplashScreen
import id.or.karangtaruna.kasgo.ui.theme.KasGoTheme
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Run only on the CI emulator. Screenshots are review artifacts, not golden assertions. */
class ScreenReviewTest {
    @get:Rule val compose = createComposeRule()

    private fun show(content: @Composable () -> Unit) {
        compose.setContent {
            KasGoTheme {
                Surface(Modifier.fillMaxSize().testTag("screen-review-root"), color = AppColors.backgroundLight) { content() }
            }
        }
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.filesDir, "screenshots").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            compose.onNodeWithTag("screen-review-root").captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    @Test fun dashboardBalanceVisibilityAndRange() {
        show { DashboardScreen({}, {}, {}) }
        compose.onNodeWithText("Total Saldo Kas").assertIsDisplayed()
        compose.onNodeWithContentDescription("Sembunyikan saldo").performClick()
        compose.onNodeWithContentDescription("Tampilkan saldo").assertExists().performClick()
        compose.onNodeWithText("Bulan Ini").performScrollTo().performClick()
        capture("dashboard-month")
    }

    @Test fun ledgerPeriodAndRecap() {
        show { LedgerScreen() }
        compose.onNodeWithText("Bulan ini").performClick()
        capture("ledger")
        compose.onNodeWithText("Rekapitulasi").performClick()
        capture("ledger-recap")
    }

    @Test fun payments() {
        show { PaymentHubScreen({}) }
        compose.onNodeWithText("Pembayaran Kas").assertIsDisplayed()
        capture("payments")
    }

    @Test fun profile() {
        show { ProfileScreen({}) }
        compose.onNodeWithText("Profil & Akun").assertIsDisplayed()
        capture("profile")
        compose.onNodeWithContentDescription("Edit Data").performClick()
        compose.onNodeWithText("Nama Lengkap").assertIsDisplayed()
        capture("profile-edit")
    }

    @Test fun incomeDropdown() {
        show { AddIncomeScreen({}) }
        compose.onNodeWithText("Catat Kas Masuk").assertIsDisplayed()
        capture("income")
        compose.onNodeWithText("QRIS Kas Resmi", useUnmergedTree = true)
            .performScrollTo().performClick()
        compose.onNodeWithText("Bank BCA").performClick()
        compose.onNodeWithText("Bank BCA", useUnmergedTree = true).assertExists()
    }

    @Test fun expenseDropdown() {
        show { AddExpenseScreen({}) }
        compose.onNodeWithText("Operasional", useUnmergedTree = true).performScrollTo().performClick()
        compose.onNodeWithText("Sosial").performClick()
        compose.onNodeWithText("Sosial").assertExists()
        capture("expense")
    }

    @Test fun settings() {
        show { PaymentSettingsScreen({}) }
        compose.onNodeWithText("Metode Pembayaran Kas").assertIsDisplayed()
        capture("payment-settings")
    }

    @Test fun auth() {
        show { AuthScreen() }
        compose.onNodeWithText("Lanjutkan dengan Google").assertIsDisplayed()
        capture("auth")
    }

    @Test fun splash() {
        show { SplashScreen({}) }
        compose.onNodeWithText("Kas Go").assertIsDisplayed()
        capture("splash")
    }
}
