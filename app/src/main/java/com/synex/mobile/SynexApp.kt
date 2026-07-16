package com.synex.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.synex.core.data.SynexRepository
import com.synex.core.ui.SynexPaper
import com.synex.core.ui.SynexWordmark
import com.synex.feature.account.AccountRoute
import com.synex.feature.markets.MarketsRoute
import com.synex.feature.legal.LegalRoute
import com.synex.feature.overview.OverviewRoute
import com.synex.feature.portfolio.PortfolioRoute
import com.synex.mobile.navigation.SynexBottomBar
import com.synex.mobile.navigation.AppRoutes
import com.synex.mobile.navigation.SynexDestination

@Composable
fun SynexApp(repository: SynexRepository, onSignOut: () -> Unit) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SynexPaper,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { SynexTopBar() },
        bottomBar = {
            if (currentRoute != AppRoutes.LEGAL) {
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
            NavHost(navController, startDestination = SynexDestination.OVERVIEW.route) {
                composable(SynexDestination.OVERVIEW.route) {
                    OverviewRoute(
                        repository,
                        onMarkets = { navController.navigate(SynexDestination.MARKETS.route) },
                        onPortfolio = { navController.navigate(SynexDestination.PORTFOLIO.route) },
                        onAccount = { navController.navigate(SynexDestination.ACCOUNT.route) },
                    )
                }
                composable(SynexDestination.MARKETS.route) { MarketsRoute(repository) }
                composable(SynexDestination.PORTFOLIO.route) { PortfolioRoute(repository) }
                composable(SynexDestination.ACCOUNT.route) {
                    AccountRoute(
                        repository = repository,
                        onLegalClick = { navController.navigate(AppRoutes.LEGAL) },
                        onAuthenticationAction = onSignOut,
                    )
                }
                composable(AppRoutes.LEGAL) {
                    LegalRoute(onBack = { navController.popBackStack() })
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
