package com.synex.mobile.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CandlestickChart
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.ui.graphics.vector.ImageVector

enum class SynexDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    OVERVIEW("overview", "Overview", Icons.Rounded.GridView),
    MARKETS("markets", "Markets", Icons.Rounded.CandlestickChart),
    PORTFOLIO("portfolio", "Portfolio", Icons.Rounded.PieChart),
    ACCOUNT("account", "Account", Icons.Rounded.AccountCircle),
}
