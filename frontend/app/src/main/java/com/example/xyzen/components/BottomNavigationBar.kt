package com.example.xyzen.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.xyzen.navigation.BottomNavItem

@Composable
fun BottomNavigationBar(navController: NavHostController) {
	val navBackStackEntry by navController.currentBackStackEntryAsState()
	val currentRoute = navBackStackEntry?.destination?.route

	NavigationBar {
		BottomNavItem.items.forEach { item ->
			NavigationBarItem(
				icon = { Icon(item.icon, contentDescription = item.label) },
				label = { Text(item.label) },
				selected = currentRoute == item.route,
				onClick = {
					if (currentRoute != item.route) {
						navController.navigate(item.route) {
							popUpTo(navController.graph.startDestinationId)
							launchSingleTop = true
						}
					}
				}
			)
		}
	}
}