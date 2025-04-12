package com.example.xyzen.model

data class VideoItem(
    val id: String,
    val videoUrl: String,
    val username: String,
    val description: String,
    val likes: Int,
    val comments: Int,
    val userProfilePic: String
)

// Sample data for testing
object SampleVideoData {
    val videos = listOf(
        VideoItem(
            id = "1",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            username = "user1",
            description = "Check out this cool video! #trending #viral",
            likes = 1234,
            comments = 321,
            userProfilePic = "https://i.pravatar.cc/150?img=1"
        ),
        VideoItem(
            id = "2",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            username = "user2",
            description = "My latest creation 🎬 #creative #art",
            likes = 4567,
            comments = 543,
            userProfilePic = "https://i.pravatar.cc/150?img=2"
        ),
        VideoItem(
            id = "3",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            username = "user3",
            description = "Having fun with friends! #weekend #fun",
            likes = 8901,
            comments = 765,
            userProfilePic = "https://i.pravatar.cc/150?img=3"
        ),
        VideoItem(
            id = "4",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            username = "user4",
            description = "Travel memories ✈️ #travel #adventure",
            likes = 2345,
            comments = 432,
            userProfilePic = "https://i.pravatar.cc/150?img=4"
        ),
        VideoItem(
            id = "5",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            username = "user5",
            description = "New dance challenge! #dance #challenge",
            likes = 6789,
            comments = 876,
            userProfilePic = "https://i.pravatar.cc/150?img=5"
        )
    )
}