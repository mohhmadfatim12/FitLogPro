package com.fitlogpro.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlogpro.app.ui.theme.AccentOrange
import com.fitlogpro.app.ui.theme.DarkBorder
import com.fitlogpro.app.ui.theme.DarkSurface
import com.fitlogpro.app.ui.theme.DarkTextPrimary
import com.fitlogpro.app.ui.theme.DarkTextSecondary

data class BottomNavTab(
    val title: String,
    val route: String,
    val icon: ImageVector
)

val BottomTabs = listOf(
    BottomNavTab("Workouts", NavRoute.Routines.route, Icons.Default.FitnessCenter),
    BottomNavTab("History", NavRoute.History.route, Icons.Default.History),
    BottomNavTab("Analytics", NavRoute.Analytics.route, Icons.Default.BarChart),
    BottomNavTab("Body Stats", NavRoute.Measurements.route, Icons.Default.MonitorWeight),
    BottomNavTab("Settings", NavRoute.Settings.route, Icons.Default.Settings)
)

@Composable
fun FitLogBottomBar(
    currentRoute: String?,
    onNavigateToTab: (String) -> Unit
) {
    NavigationBar(
        containerColor = DarkSurface,
        tonalElevation = 8.dp
    ) {
        BottomTabs.forEach { tab ->
            val isSelected = currentRoute == tab.route

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigateToTab(tab.route) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = AccentOrange,
                    indicatorColor = AccentOrange,
                    unselectedIconColor = DarkTextSecondary,
                    unselectedTextColor = DarkTextSecondary
                )
            )
        }
    }
}
