package com.example.xyzen.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.xyzen.firebase.FirebaseServiceClass
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlaylistScreen(navController: NavController) {
	val firebaseService = remember { FirebaseServiceClass() }
	val coroutineScope = rememberCoroutineScope()

	// State variables
	var playlistName by remember { mutableStateOf("") }
	var playlistDescription by remember { mutableStateOf("") }
	var isPublic by remember { mutableStateOf(true) }
	var isLoading by remember { mutableStateOf(false) }
	var errorMessage by remember { mutableStateOf<String?>(null) }

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Create Playlist") },
				navigationIcon = {
					IconButton(onClick = { navController.popBackStack() }) {
						Icon(
							imageVector = Icons.Default.ArrowBack,
							contentDescription = "Back"
						)
					}
				},
				actions = {
					// Save button
					IconButton(
						onClick = {
							if (playlistName.isBlank()) {
								errorMessage = "Please enter a playlist name"
								return@IconButton
							}

							isLoading = true
							coroutineScope.launch {
								firebaseService.createPlaylist(
									name = playlistName,
									description = playlistDescription,
									isPublic = isPublic
								).fold(
									onSuccess = {
										// Navigate back to profile
										navController.popBackStack()
									},
									onFailure = { error ->
										errorMessage = "Failed to create playlist: ${error.message}"
										isLoading = false
									}
								)
							}
						},
						enabled = !isLoading && playlistName.isNotBlank()
					) {
						Icon(
							imageVector = Icons.Default.Check,
							contentDescription = "Save"
						)
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
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(16.dp)
					.verticalScroll(rememberScrollState())
			) {
				// Playlist name
				OutlinedTextField(
					value = playlistName,
					onValueChange = { playlistName = it },
					label = { Text("Playlist Name") },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					isError = errorMessage != null && playlistName.isBlank()
				)

				Spacer(modifier = Modifier.height(16.dp))

				// Playlist description
				OutlinedTextField(
					value = playlistDescription,
					onValueChange = { playlistDescription = it },
					label = { Text("Description (Optional)") },
					modifier = Modifier.fillMaxWidth(),
					minLines = 3,
					maxLines = 5
				)

				Spacer(modifier = Modifier.height(16.dp))

				// Visibility toggle
				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						text = "Visibility",
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.Medium
					)

					Spacer(modifier = Modifier.weight(1f))

					Switch(
						checked = isPublic,
						onCheckedChange = { isPublic = it }
					)

					Spacer(modifier = Modifier.width(8.dp))

					Icon(
						imageVector = if (isPublic) Icons.Default.Public else Icons.Default.Lock,
						contentDescription = if (isPublic) "Public" else "Private"
					)

					Spacer(modifier = Modifier.width(4.dp))

					Text(
						text = if (isPublic) "Public" else "Private",
						style = MaterialTheme.typography.bodyMedium
					)
				}

				// Error message
				if (errorMessage != null) {
					Spacer(modifier = Modifier.height(16.dp))
					Text(
						text = errorMessage!!,
						color = MaterialTheme.colorScheme.error,
						style = MaterialTheme.typography.bodyMedium
					)
				}
			}

			// Loading indicator
			if (isLoading) {
				CircularProgressIndicator(
					modifier = Modifier.align(Alignment.Center)
				)
			}
		}
	}
}