package com.synex.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import com.synex.core.data.SynexRepository
import com.synex.core.ui.SynexPaper
import com.synex.core.ui.SynexWordmark
import com.synex.core.ui.SynexRed
import com.synex.feature.account.AccountRoute
import com.synex.feature.markets.MarketsRoute
import com.synex.feature.legal.LegalRoute
import com.synex.feature.legal.LegalDocumentRoute
import com.synex.feature.overview.OverviewRoute
import com.synex.feature.portfolio.PortfolioRoute
import com.synex.feature.trade.TradeRoute
import com.synex.feature.trade.PositionDetailRoute
import com.synex.feature.activity.ActivityRoute
import com.synex.feature.funding.FundingRoute
import com.synex.feature.automation.AutomationRoute
import com.synex.feature.legacy.LegacyRoute
import com.synex.mobile.navigation.SynexBottomBar
import com.synex.mobile.navigation.AppRoutes
import com.synex.mobile.navigation.SynexDestination
import kotlinx.coroutines.delay

@Composable
fun SynexApp(repository: SynexRepository, onSignOut: () -> Unit) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    var derivUnavailable by remember { mutableStateOf(false) }
    LaunchedEffect(repository) {
        while (true) {
            derivUnavailable = runCatching { repository.derivSystemStatus() == "unavailable" }.getOrDefault(false)
            delay(60_000)
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SynexPaper,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { Column { SynexTopBar(); if (derivUnavailable) Surface(color = androidx.compose.ui.graphics.Color(0xFFFFF3E0)) { Text("Deriv is temporarily unavailable. Market data and account actions may be delayed.", Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp), color = SynexRed) } } },
        bottomBar = {
            if (currentRoute?.startsWith("legal") != true) {
                SynexBottomBar(currentRoute) { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).background(SynexPaper)) {
            NavHost(
                navController,
                startDestination = SynexDestination.OVERVIEW.route,
                enterTransition = { fadeIn(tween(320)) + slideInHorizontally(tween(360)) { it / 8 } },
                exitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(260)) { -it / 10 } },
                popEnterTransition = { fadeIn(tween(320)) + slideInHorizontally(tween(360)) { -it / 8 } },
                popExitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(260)) { it / 10 } },
            ) {
                composable(SynexDestination.OVERVIEW.route) {
                    OverviewRoute(
                        repository,
                        onMarkets = { navController.navigate(SynexDestination.MARKETS.route) },
                        onPortfolio = { navController.navigate(SynexDestination.PORTFOLIO.route) },
                        onAccount = { navController.navigate(SynexDestination.ACCOUNT.route) },
                    )
                }
                composable(SynexDestination.MARKETS.route) { MarketsRoute(repository) }
                composable(SynexDestination.TRADE.route) {
                    TradeRoute(repository, onAccount = { navController.navigate(SynexDestination.ACCOUNT.route) })
                }
                composable(SynexDestination.PORTFOLIO.route) {
                    PortfolioRoute(repository, onAccount = {
                        navController.navigate(SynexDestination.ACCOUNT.route)
                    }, onPosition = { navController.navigate(AppRoutes.position(it)) })
                }
                composable(SynexDestination.ACTIVITY.route) {
                    ActivityRoute(repository, onAccount = { navController.navigate(SynexDestination.ACCOUNT.route) })
                }
                composable(SynexDestination.ACCOUNT.route) {
                    AccountRoute(
                        repository = repository,
                        onLegalClick = { navController.navigate(AppRoutes.LEGAL) },
                        onAuthenticationAction = onSignOut,
                        onFundingClick = { navController.navigate(AppRoutes.FUNDING) },
                        onAutomationClick = { navController.navigate(AppRoutes.AUTOMATION) },
                        onLegacyHistoryClick = { navController.navigate(AppRoutes.LEGACY_HISTORY) },
                    )
                }
                composable(AppRoutes.FUNDING) {
                    FundingRoute(
                        repository = repository,
                        onAccount = { navController.navigate(SynexDestination.ACCOUNT.route) },
                    )
                }
                composable(AppRoutes.AUTOMATION) {
                    AutomationRoute(
                        repository = repository,
                        onAccount = { navController.navigate(SynexDestination.ACCOUNT.route) },
                    )
                }
                composable(AppRoutes.LEGACY_HISTORY) { LegacyRoute(repository) }
                composable(AppRoutes.LEGAL) {
                    LegalRoute(
                        onBack = { navController.popBackStack() },
                        onDocument = { navController.navigate(AppRoutes.legalDocument(it)) },
                    )
                }
                composable(
                    AppRoutes.LEGAL_DOCUMENT,
                    arguments = listOf(navArgument("documentType") { type = NavType.StringType }),
                ) { entry ->
                    LegalDocumentRoute(
                        documentType = entry.arguments?.getString("documentType").orEmpty(),
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(
                    AppRoutes.POSITION,
                    arguments = listOf(navArgument("contractId") { type = NavType.LongType }),
                ) { entry ->
                    PositionDetailRoute(
                        repository = repository,
                        contractId = entry.arguments?.getLong("contractId") ?: 0L,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

@Composable
private fun SynexTopBar() {
    Surface(color = SynexPaper, modifier = Modifier.statusBarsPadding()) {
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SynexWordmark()
        }
    }
}
