package com.example.xyzen.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.xyzen.components.BottomNavigationBar
import com.example.xyzen.screens.FeedScreen
import com.example.xyzen.screens.NotificationsScreen
import com.example.xyzen.screens.ProfileScreen
import com.example.xyzen.screens.UploadScreen
import com.example.xyzen.navigation.BottomNavItem

@Composable
fun MainScreen() {
	val navController = rememberNavController()

	Scaffold(
		bottomBar = { BottomNavigationBar(navController = navController) }
	) { innerPadding ->
		NavHost(
			navController = navController,
			startDestination = BottomNavItem.Feed.route,
			modifier = Modifier.padding(innerPadding)
		) {
			composable(BottomNavItem.Feed.route) { FeedScreen() }
			composable(BottomNavItem.Upload.route) { UploadScreen() }
			composable(BottomNavItem.Profile.route) { ProfileScreen() }
			composable(BottomNavItem.Notifications.route) { NotificationsScreen() }
		}
	}
}