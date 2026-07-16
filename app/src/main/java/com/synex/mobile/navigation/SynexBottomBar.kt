package com.synex.mobile.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.synex.core.ui.SynexInk
import com.synex.core.ui.SynexTeal

@Composable
fun SynexBottomBar(
    currentRoute: String?,
    onSelect: (SynexDestination) -> Unit,
) {
    NavigationBar(containerColor = Color.White.copy(alpha = 0.96f), tonalElevation = 0.dp) {
        SynexDestination.entries.forEach { destination ->
            val selected = currentRoute == destination.route
            val scale by animateFloatAsState(
                if (selected) 1.13f else 1f,
                spring(dampingRatio = 0.68f, stiffness = 420f),
                label = "${destination.label} icon",
            )
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(destination) },
                icon = {
                    Icon(
                        destination.icon,
                        destination.label,
                        Modifier.graphicsLayer { scaleX = scale; scaleY = scale },
                    )
                },
                label = { Text(destination.label, style = MaterialTheme.typography.labelMedium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = SynexInk,
                    indicatorColor = SynexTeal,
                    unselectedIconColor = SynexInk.copy(alpha = 0.48f),
                    unselectedTextColor = SynexInk.copy(alpha = 0.48f),
                ),
            )
        }
    }
}
