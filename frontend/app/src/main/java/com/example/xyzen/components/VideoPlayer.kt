package com.example.xyzen.components

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun VideoPlayer(
    videoUrl: String,
    isPlaying: Boolean,
    onPlayerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Create Media3 ExoPlayer instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUrl)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = isPlaying
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }
    
    // Update player state when isPlaying changes
    LaunchedEffect(isPlaying) {
        exoPlayer.playWhenReady = isPlaying
    }
    
    // Clean up player when leaving composition
    DisposableEffect(key1 = Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                setOnClickListener { onPlayerClick() }
            }
        },
        modifier = modifier
    )
}