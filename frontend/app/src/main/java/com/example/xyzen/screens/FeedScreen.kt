package com.example.xyzen.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.xyzen.components.VideoPlayer
import com.example.xyzen.model.SampleVideoData
import com.example.xyzen.model.VideoItem
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.rememberAsyncImagePainter
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState

@Composable
fun FeedScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        VideoFeed()
    }
}

@Composable
fun VideoFeed() {
    val videos = SampleVideoData.videos
    val pagerState = rememberPagerState(pageCount = { videos.size })
    
    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        key = { videos[it].id }
    ) { page ->
        VideoItem(videos[page])
    }
}

@Composable
fun VideoItem(video: VideoItem) {
    var isPlaying by remember { mutableStateOf(true) }
    
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(800.dp)
        .background(Color.Black)
    ) {
        // Video Player
        VideoPlayer(
            videoUrl = video.videoUrl,
            isPlaying = isPlaying,
            onPlayerClick = { isPlaying = !isPlaying },
            modifier = Modifier.fillMaxSize()
        )
        
        // Overlay content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // User interaction buttons (right side)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp, bottom = 80.dp), // Increased padding to prevent clipping
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Profile picture
                Image(
                    painter = rememberAsyncImagePainter(video.userProfilePic),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
                
                // Like button
                IconButton(onClick = { /* Like action */ }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Like",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                        Text(
                            text = formatCount(video.likes),
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
                
                // Comment button
                IconButton(onClick = { /* Comment action */ }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = "Comment",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                        Text(
                            text = formatCount(video.comments),
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
                
                // Share button
                IconButton(onClick = { /* Share action */ }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                        Text(
                            text = "Share",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            // Video info (bottom)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 16.dp)
            ) {
                // Username
                Text(
                    text = "@${video.username}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                
                // Description
                Text(
                    text = video.description,
                    color = Color.White,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Play/Pause button (center)
            IconButton(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}

// Helper function to format counts (e.g., 1.2K, 4.5M)
private fun formatCount(count: Int): String {
    return when {
        count < 1000 -> count.toString()
        count < 1000000 -> String.format("%.1fK", count / 1000.0)
        else -> String.format("%.1fM", count / 1000000.0)
    }
}