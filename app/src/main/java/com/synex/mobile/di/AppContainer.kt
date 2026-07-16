package com.synex.mobile.di

import com.synex.core.data.NetworkSynexRepository
import com.synex.core.data.SynexRepository
import com.synex.core.network.SynexApiClient
import com.synex.feature.auth.Auth0Session

class AppContainer(
    context: android.content.Context,
    apiBaseUrl: String,
    auth0ClientId: String,
    auth0Domain: String,
    auth0Audience: String,
) {
    val authSession = Auth0Session(context, auth0ClientId, auth0Domain, auth0Audience)
    val repository: SynexRepository = NetworkSynexRepository(
        SynexApiClient(apiBaseUrl, authSession),
    )
}
