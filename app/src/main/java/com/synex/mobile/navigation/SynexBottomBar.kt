package com.synex.mobile.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.synex.core.ui.SynexInk

@Composable
fun SynexBottomBar(
    currentRoute: String?,
    onSelect: (SynexDestination) -> Unit,
) {
    NavigationBar(containerColor = Color.White.copy(alpha = 0.96f), tonalElevation = 0.dp) {
        SynexDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onSelect(destination) },
                icon = { Icon(destination.icon, destination.label) },
                label = { Text(destination.label, style = MaterialTheme.typography.labelMedium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = SynexInk,
                    indicatorColor = SynexInk,
                    unselectedIconColor = SynexInk.copy(alpha = 0.48f),
                    unselectedTextColor = SynexInk.copy(alpha = 0.48f),
                ),
            )
        }
    }
}
