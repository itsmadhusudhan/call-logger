package com.shreekaram.calllogger.navigation

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    data object Home : BottomNavItem(
        Route.Home.id,
        Route.Home.title,
        Icons.Default.Home,
        Icons.Filled.Home)
    data object Insights : BottomNavItem(
        Route.Insights.id,
        Route.Insights.title,
        Icons.Default.Insights,
        Icons.Filled.Insights
    )
}

val items = listOf(
    BottomNavItem.Home,
    BottomNavItem.Insights,
)

@Composable
fun BottomNavigationBar(navHostController: NavHostController) {
    val borderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5F)

    BottomNavigation(
        backgroundColor = MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.onBackground,
        elevation = 0.dp,
        modifier = Modifier
            .drawBehind {
                drawLine(
                    borderColor,
                    Offset(0f, 0F),
                    Offset(size.width, 0F),
                    1F
                )
            }
    ) {
        val stackEntry by navHostController.currentBackStackEntryAsState()
        val currentRoute = stackEntry?.destination?.route

        items.forEach { item ->
            val selected = currentRoute == item.route

            BottomNavigationItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(text = item.title) },
                selected = selected,
                unselectedContentColor = MaterialTheme.colors.onBackground.copy(alpha = 0.5F),
                selectedContentColor = MaterialTheme.colors.primary,
                onClick = {
                    navHostController.navigate(item.route) {
                        popUpTo(navHostController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
