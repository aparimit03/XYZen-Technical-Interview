package com.example.xyzen.firebase

import android.net.Uri
import com.example.xyzen.model.User
import com.example.xyzen.model.Video
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
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

			// Link Video URL in Firestore
			val video = Video(
				id = videoId,
				userId = userId,
				caption = caption,
				videoUrl = videoUrl,
				timestamp = Timestamp.now()
			)

			// Save video to videos collection
			firestore.collection("videos")
				.document(videoId)
				.set(video)
				.await()

			// 4. Update user's videos list
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
}