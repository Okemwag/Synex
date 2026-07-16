package com.synex.mobile.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CandlestickChart
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.PieChartOutline
import androidx.compose.ui.graphics.vector.ImageVector

enum class SynexDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    OVERVIEW("overview", "Overview", Icons.Outlined.GridView),
    MARKETS("markets", "Markets", Icons.Outlined.CandlestickChart),
    PORTFOLIO("portfolio", "Portfolio", Icons.Outlined.PieChartOutline),
    ACCOUNT("account", "Account", Icons.Outlined.AccountCircle),
}
