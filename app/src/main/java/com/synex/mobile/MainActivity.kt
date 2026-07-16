package com.synex.mobile

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.view.animation.DecelerateInterpolator
import com.synex.core.ui.SynexTheme
import com.synex.mobile.di.AppContainer
import com.synex.feature.auth.AuthGate
import com.synex.feature.onboarding.OnboardingGate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setOnExitAnimationListener { provider ->
            provider.iconView.animate()
                .alpha(0f)
                .scaleX(1.08f)
                .scaleY(1.08f)
                .setDuration(320L)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction(provider::remove)
                .start()
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()

        val container = AppContainer(
            context = applicationContext,
            apiBaseUrl = BuildConfig.SYNEX_API_BASE_URL,
            auth0ClientId = BuildConfig.SYNEX_AUTH0_CLIENT_ID,
            auth0Domain = BuildConfig.SYNEX_AUTH0_DOMAIN,
            auth0Audience = BuildConfig.SYNEX_AUTH0_AUDIENCE,
        )

        setContent {
            SynexTheme {
                AuthGate(
                    session = container.authSession,
                    authenticatedContent = { onLogout ->
                        OnboardingGate(container.repository) {
                            SynexApp(container.repository, onLogout)
                        }
                    },
                )
            }
        }
    }
}
