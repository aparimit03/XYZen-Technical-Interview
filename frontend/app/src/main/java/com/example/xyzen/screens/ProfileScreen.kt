package com.example.xyzen.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.example.xyzen.MainActivity
import com.example.xyzen.R
import com.example.xyzen.firebase.FirebaseServiceClass
import com.example.xyzen.model.User
import com.example.xyzen.model.Video
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, userId: String? = null) {
	val context = LocalContext.current
	val activity = context as? MainActivity
	val firebaseService = remember { FirebaseServiceClass() }
	val coroutineScope = rememberCoroutineScope()

	// State variables
	var user by remember { mutableStateOf<User?>(null) }
	var userVideos by remember { mutableStateOf<List<Video>>(emptyList()) }
	var isLoading by remember { mutableStateOf(true) }
	var errorMessage by remember { mutableStateOf<String?>(null) }
	var isCurrentUser by remember { mutableStateOf(false) }

	// Load user data when the screen is first displayed
	LaunchedEffect(userId) {
		val currentUser = firebaseService.getCurrentUser()
		// Determine which user ID to use
		val targetUserId = userId ?: currentUser?.uid

		if (targetUserId != null) {
			// Check if viewing own profile
			isCurrentUser = currentUser != null && targetUserId == currentUser.uid

			coroutineScope.launch {
				try {
					// Fetch user data from Firestore
					val userResult = firebaseService.getUserData(targetUserId)
					userResult.fold(
						onSuccess = { userData ->
							user = userData
							// Fetch user videos
							val videosResult = firebaseService.getUserVideos(targetUserId)
							videosResult.fold(
								onSuccess = { videos ->
									userVideos = videos
									isLoading = false
								},
								onFailure = { error ->
									errorMessage = "Failed to load videos: ${error.message}"
									isLoading = false
								}
							)
						},
						onFailure = { error ->
							errorMessage = "Failed to load user data: ${error.message}"
							isLoading = false
						}
					)
				} catch (e: Exception) {
					errorMessage = "An error occurred: ${e.message}"
					isLoading = false
				}
			}
		} else {
			// User not logged in and no userId provided
			errorMessage = "User not found"
			isLoading = false
		}
	}

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(if (isCurrentUser) "My Profile" else "${user?.username}'s Profile") },
				actions = {
					// Only show sign out button if viewing own profile
					if (isCurrentUser) {
						IconButton(onClick = {
							firebaseService.signOut()
							activity?.signOutFromApp()
						}) {
							Icon(
								imageVector = Icons.Default.ExitToApp,
								contentDescription = "Sign Out"
							)
						}
					}
				},
				navigationIcon = {
					// Add back button if viewing someone else's profile
					if (!isCurrentUser) {
						IconButton(onClick = { navController.popBackStack() }) {
							Icon(
								imageVector = Icons.Default.ArrowBack,
								contentDescription = "Back"
							)
						}
					}
				}
			)
		}
	) { paddingValues ->
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(paddingValues)
		) {
			when {
				isLoading -> {
					CircularProgressIndicator(
						modifier = Modifier.align(Alignment.Center)
					)
				}

				errorMessage != null -> {
					Text(
						text = errorMessage ?: "Unknown error",
						color = MaterialTheme.colorScheme.error,
						modifier = Modifier
							.align(Alignment.Center)
							.padding(16.dp)
					)
				}

				user == null -> {
					Text(
						text = "User not found",
						modifier = Modifier.align(Alignment.Center)
					)
				}

				else -> {
					// User profile content
					Column(
						modifier = Modifier
							.fillMaxSize()
							.padding(16.dp),
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						// Profile header with user info
						ProfileHeader(
							user = user!!,
							onEditClick = {
								// Only allow editing own profile
								if (isCurrentUser) {
									// Navigate to edit profile screen
								}
							},
							showEditButton = isCurrentUser
						)

					Spacer(modifier = Modifier.height(24.dp))

					// Bio section
					if (user?.bio?.isNotEmpty() == true) {
						Text(
							text = user?.bio ?: "",
							style = MaterialTheme.typography.bodyMedium,
							textAlign = TextAlign.Center,
							modifier = Modifier.padding(horizontal = 32.dp)
						)
						Spacer(modifier = Modifier.height(24.dp))
					}

					// Videos section
					Text(
						text = "Videos",
						style = MaterialTheme.typography.titleLarge,
						fontWeight = FontWeight.Bold
					)

					Spacer(modifier = Modifier.height(8.dp))

					if (userVideos.isEmpty()) {
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.height(200.dp),
							contentAlignment = Alignment.Center
						) {
							Text("No videos uploaded yet")
						}
					} else {
						// Grid of videos
						LazyVerticalGrid(
							columns = GridCells.Fixed(3),
							horizontalArrangement = Arrangement.spacedBy(4.dp),
							verticalArrangement = Arrangement.spacedBy(4.dp),
							modifier = Modifier.fillMaxWidth()
						) {
							items(userVideos) { video ->
								VideoThumbnail(video = video) {
									// Handle video click - navigate to video detail
									navController.navigate("video_detail/${video.id}")
								}
							}
						}
					}
				}
			}
		}
	}
}
}

@Composable
fun ProfileHeader(user: User, onEditClick: () -> Unit, showEditButton: Boolean = true) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier.fillMaxWidth()
	) {
		// Profile image
		Box(
			modifier = Modifier
				.size(100.dp)
				.clip(CircleShape)
				.background(MaterialTheme.colorScheme.surfaceVariant)
				.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
		) {
			if (user.profileImage != null) {
				Image(
					painter = rememberAsyncImagePainter(user.profileImage),
					contentDescription = "Profile Picture",
					modifier = Modifier.fillMaxSize(),
					contentScale = ContentScale.Crop
				)
			} else {
				// Default profile image
				Icon(
					painter = painterResource(id = R.drawable.ic_person),
					contentDescription = "Default Profile",
					modifier = Modifier
						.size(60.dp)
						.align(Alignment.Center),
					tint = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}

			// Modify the edit button to only show if showEditButton is true
			if (showEditButton) {
				IconButton(
					onClick = onEditClick,
					modifier = Modifier
						.size(36.dp)
						.align(Alignment.BottomEnd)
						.background(MaterialTheme.colorScheme.primary, CircleShape)
				) {
					Icon(
						imageVector = Icons.Default.Edit,
						contentDescription = "Edit Profile",
						tint = MaterialTheme.colorScheme.onPrimary,
						modifier = Modifier.size(20.dp)
					)
				}
			}
		}

		Spacer(modifier = Modifier.height(16.dp))

		// Username
		Text(
			text = "@${user.username}",
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.Bold
		)

		Spacer(modifier = Modifier.height(4.dp))

		// Email
		Text(
			text = user.email,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		Spacer(modifier = Modifier.height(16.dp))

		// Stats row
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceEvenly
		) {
			StatItem(count = user.videos.size, label = "Videos")
			// You can add more stats here like followers, following, etc.
		}
	}
}

@Composable
fun StatItem(count: Int, label: String) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			text = count.toString(),
			style = MaterialTheme.typography.titleLarge,
			fontWeight = FontWeight.Bold
		)
		Text(
			text = label,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}

@Composable
fun VideoThumbnail(video: Video, onClick: () -> Unit) {
	Box(
		modifier = Modifier
			.aspectRatio(9f / 16f)
			.clip(RoundedCornerShape(4.dp))
			.background(MaterialTheme.colorScheme.surfaceVariant)
			.clickable(onClick = onClick)
	) {
		// Try to load the video thumbnail or fallback to the video itself
		val imageUrl = if (video.thumbnailUrl.isNotEmpty()) {
			video.thumbnailUrl
		} else if (video.videoUrl.isNotEmpty()) {
			video.videoUrl
		} else {
			""
		}

		if (imageUrl.isNotEmpty()) {
			AsyncImage(
				model = imageUrl,
				contentDescription = "Video thumbnail",
				modifier = Modifier.fillMaxSize(),
				contentScale = ContentScale.Crop,
				onError = {
					// If loading fails, we'll show the placeholder below
				}
			)
		}

		// Always show a semi-transparent overlay with a play icon
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(Color.Black.copy(alpha = 0.3f)),
			contentAlignment = Alignment.Center
		) {
			Icon(
				imageVector = Icons.Default.PlayArrow,
				contentDescription = "Play video",
				tint = Color.White,
				modifier = Modifier.size(48.dp)
			)
		}
	}
}