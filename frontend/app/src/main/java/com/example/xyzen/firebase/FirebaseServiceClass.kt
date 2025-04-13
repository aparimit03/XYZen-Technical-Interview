package com.example.xyzen.firebase

import android.net.Uri
import com.example.xyzen.model.User
import com.example.xyzen.model.Video
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

class FirebaseServiceClass() {
	private val firebaseAuth = Firebase.auth
	private val firestore = Firebase.firestore
	private val firebaseStorage = Firebase.storage

	companion object {
		private const val TAG = "FirebaseAuthService"
	}

	fun isUserLoggedIn(): Boolean {
		return firebaseAuth.currentUser != null
	}

	fun getCurrentUser(): FirebaseUser? {
		return firebaseAuth.currentUser
	}

	suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
		return try {
			val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
			Result.success(result.user!!)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	suspend fun register(email: String, password: String, username: String): Result<FirebaseUser> {
		return try {
			val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
			val user = result.user!!

			val userModel = User(
				id = user.uid,
				username = username,
				email = email
			)

			firestore.collection("users").document(user.uid)
				.set(userModel)
				.await()

			Result.success(user)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	fun signOut() {
		firebaseAuth.signOut()
	}

	suspend fun uploadVideo(
		videoUri: Uri,
		videoId: String,
		userId: String,
		caption: String
	): Result<Video> {
		return try {
			// Uploading video to Firebase Storage
			val storageRef = firebaseStorage.reference
				.child("videos/$userId/$videoId.mp4")

			val uploadTask = storageRef.putFile(videoUri).await()
			val videoUrl = storageRef.downloadUrl.await().toString()

			// Generate a thumbnail URL (using the video URL for now)
			// In a production app, you would generate an actual thumbnail
			val thumbnailUrl = videoUrl

			// Link Video URL in Firestore
			val video = Video(
				id = videoId,
				userId = userId,
				caption = caption,
				videoUrl = videoUrl,
				thumbnailUrl = thumbnailUrl,
				timestamp = Timestamp.now()
			)

			// Save video to videos collection
			firestore.collection("videos")
				.document(videoId)
				.set(video)
				.await()

			// Update user's videos list
			firestore.collection("users")
				.document(userId)
				.update("videos", com.google.firebase.firestore.FieldValue.arrayUnion(videoId))
				.await()

			Result.success(video)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	suspend fun getVideosForFeed(limit: Long = 10): Result<List<Video>> {
		return try {
			val videosSnapshot = firestore.collection("videos")
				.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
				.limit(limit)
				.get()
				.await()

			val videos = videosSnapshot.documents.mapNotNull { doc ->
				doc.toObject(Video::class.java)
			}

			Result.success(videos)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	suspend fun getUserData(userId: String): Result<User> {
		return try {
			val userDoc = firestore.collection("users").document(userId).get().await()
			val user = userDoc.toObject(User::class.java)

			if (user != null) {
				Result.success(user)
			} else {
				Result.failure(Exception("User not found"))
			}
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	suspend fun getUserVideos(userId: String): Result<List<Video>> {
		return try {
			val videosSnapshot = firestore.collection("videos")
				.whereEqualTo("userId", userId)
				.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
				.get()
				.await()

			val videos = videosSnapshot.documents.mapNotNull { doc ->
				doc.toObject(Video::class.java)
			}

			// Log the number of videos found for debugging
			println("Found ${videos.size} videos for user $userId")

			Result.success(videos)
		} catch (e: Exception) {
			println("Error fetching user videos: ${e.message}")
			Result.failure(e)
		}
	}

	suspend fun getVideoById(videoId: String): Result<Video> {
		return try {
			val videoDoc = firestore.collection("videos").document(videoId).get().await()
			val video = videoDoc.toObject(Video::class.java)

			if (video != null) {
				Result.success(video)
			} else {
				Result.failure(Exception("Video not found"))
			}
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	suspend fun likeVideo(videoId: String): Result<Boolean> {
		return try {
			val currentUser = getCurrentUser() ?: throw Exception("User not logged in")

			// Check if user already liked the video
			val likeDoc = firestore.collection("likes")
				.whereEqualTo("videoId", videoId)
				.whereEqualTo("userId", currentUser.uid)
				.get()
				.await()

			val videoRef = firestore.collection("videos").document(videoId)
			val isLiked = likeDoc.isEmpty

			if (isLiked) {
				// User hasn't liked the video yet, add like
				val likeData = hashMapOf(
					"videoId" to videoId,
					"userId" to currentUser.uid,
					"timestamp" to Timestamp.now()
				)

				// Add to likes collection
				firestore.collection("likes").add(likeData).await()

				// Update video like count
				videoRef.update("likes", FieldValue.increment(1)).await()
			} else {
				// User already liked the video, remove like
				val likeDocId = likeDoc.documents[0].id

				// Remove from likes collection
				firestore.collection("likes").document(likeDocId).delete().await()

				// Update video like count
				videoRef.update("likes", FieldValue.increment(-1)).await()
			}

			Result.success(isLiked)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	suspend fun checkIfUserLikedVideo(videoId: String): Result<Boolean> {
		return try {
			val currentUser = getCurrentUser() ?: return Result.success(false)

			val likeDoc = firestore.collection("likes")
				.whereEqualTo("videoId", videoId)
				.whereEqualTo("userId", currentUser.uid)
				.get()
				.await()

			Result.success(!likeDoc.isEmpty)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}
}