package com.synex.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.synex.core.ui.SynexTheme
import com.synex.mobile.di.AppContainer
import com.synex.feature.auth.AuthGate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                        SynexApp(container.repository, onLogout)
                    },
                )
            }
        }
    }
}
