package com.synex.mobile.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CandlestickChart
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.ui.graphics.vector.ImageVector

enum class SynexDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    OVERVIEW("overview", "Overview", Icons.Rounded.GridView),
    MARKETS("markets", "Markets", Icons.Rounded.CandlestickChart),
    TRADE("trade", "Trade", Icons.Rounded.SwapHoriz),
    PORTFOLIO("portfolio", "Portfolio", Icons.Rounded.PieChart),
    ACTIVITY("activity", "Activity", Icons.Rounded.ReceiptLong),
    ACCOUNT("account", "Account", Icons.Rounded.AccountCircle),
}
