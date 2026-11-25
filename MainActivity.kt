package com.enos.music

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private val executor = Executors.newSingleThreadExecutor()

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        loadControllerAndUi()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= 33) permissions += Manifest.permission.READ_MEDIA_AUDIO
        else permissions += Manifest.permission.READ_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= 31) permissions += Manifest.permission.BLUETOOTH_CONNECT
        if (Build.VERSION.SDK_INT >= 33) permissions += Manifest.permission.POST_NOTIFICATIONS
        val missing = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) permissionLauncher.launch(missing.toTypedArray()) else loadControllerAndUi()
    }

    private fun loadControllerAndUi() {
        val token = SessionToken(this, ComponentName(this, MusicPlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, token).buildAsync()
        controllerFuture?.addListener({
            try { mediaController = controllerFuture?.get() } catch (_: Exception) { }
            runOnUiThread { showUi() }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun showUi() {
        setContent {
            val controller = mediaController
            var tracks by remember { mutableStateOf(AudioLibrary.load(this)) }
            var tab by remember { mutableStateOf("Músicas") }
            var showSongList by remember { mutableStateOf(false) }
            var currentTitle by remember { mutableStateOf(controller?.currentMediaItem?.mediaMetadata?.title?.toString() ?: "Escolhe uma música") }
            var currentArtist by remember { mutableStateOf(controller?.currentMediaItem?.mediaMetadata?.artist?.toString() ?: "Enos Music") }
            var currentArtwork by remember { mutableStateOf(controller?.currentMediaItem?.mediaMetadata?.artworkUri) }
            var playing by remember { mutableStateOf(controller?.isPlaying == true) }
            var position by remember { mutableLongStateOf(controller?.currentPosition ?: 0L) }
            var duration by remember { mutableLongStateOf(controller?.duration?.coerceAtLeast(0L) ?: 0L) }

            DisposableEffect(controller) {
                val listener = object : Player.Listener {
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        currentTitle = mediaItem?.mediaMetadata?.title?.toString() ?: "Escolhe uma música"
                        currentArtist = mediaItem?.mediaMetadata?.artist?.toString() ?: "Enos Music"
                        currentArtwork = mediaItem?.mediaMetadata?.artworkUri
                    }
                    override fun onIsPlayingChanged(isPlaying: Boolean) { playing = isPlaying }
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        duration = controller?.duration?.coerceAtLeast(0L) ?: 0L
                    }
                }
                controller?.addListener(listener)
                onDispose { controller?.removeListener(listener) }
            }
            LaunchedEffect(controller) {
                while (true) {
                    position = controller?.currentPosition ?: 0L
                    duration = controller?.duration?.coerceAtLeast(0L) ?: 0L
                    delay(500)
                }
            }

            MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF7B6A9C))) {
                Box(modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color.White, Color.White, Color(0xFFE6D8EF), Color(0xFF9E8AB7), Color(0xFFB5D6D2)))
                )) {
                    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(34.dp))
                        Box(
                            modifier = Modifier.size(218.dp).background(Color(0xFFE2E2E2), RoundedCornerShape(30.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = "Capa da música", tint = Color.White, modifier = Modifier.size(112.dp))
                            if (currentArtwork != null) AsyncImage(model = currentArtwork, contentDescription = "Capa do álbum", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }
                        Spacer(Modifier.height(24.dp))
                        Text(currentTitle, fontSize = 22.sp, color = Color(0xFF242424), textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(5.dp))
                        Text(currentArtist, fontSize = 16.sp, color = Color(0xFF555555), textAlign = TextAlign.Center, maxLines = 1)
                        Spacer(Modifier.weight(1f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showSongList = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Abrir lista de músicas", modifier = Modifier.size(34.dp), tint = Color.White)
                            }
                        }
                        Slider(
                            value = if (duration > 0) position.toFloat().coerceIn(0f, duration.toFloat()) else 0f,
                            onValueChange = { value -> controller?.seekTo(value.toLong()) },
                            valueRange = 0f..(duration.toFloat().coerceAtLeast(1f)),
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Color(0x88FFFFFF))
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(formatTime(position), color = Color.White, fontSize = 12.sp)
                            Text(formatTime(duration), color = Color.White, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { controller?.shuffleModeEnabled = !(controller?.shuffleModeEnabled ?: false) }) { Icon(Icons.Default.Shuffle, "Aleatório", tint = Color.White, modifier = Modifier.size(25.dp)) }
                            IconButton(onClick = { controller?.seekToPreviousMediaItem() }) { Icon(Icons.Default.SkipPrevious, "Anterior", tint = Color.White, modifier = Modifier.size(34.dp)) }
                            IconButton(onClick = {
                                if (controller != null) {
                                    if (controller.isPlaying) controller.pause()
                                    else {
                                        if (controller.mediaItemCount == 0 && tracks.isNotEmpty()) {
                                            controller.setMediaItems(tracks); controller.prepare()
                                        }
                                        controller.play()
                                    }
                                }
                            }, modifier = Modifier.size(66.dp)) { Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, if (playing) "Pausa" else "Reproduzir", tint = Color.White, modifier = Modifier.size(54.dp)) }
                            IconButton(onClick = { controller?.seekToNextMediaItem() }) { Icon(Icons.Default.SkipNext, "Seguinte", tint = Color.White, modifier = Modifier.size(34.dp)) }
                            IconButton(onClick = { tab = "Cantores" }) { Icon(Icons.Default.LibraryMusic, "Biblioteca", tint = Color.White, modifier = Modifier.size(25.dp)) }
                        }
                        Spacer(Modifier.height(20.dp))
                    }

                    
                }
                if (showSongList) {
                    AlertDialog(
                        onDismissRequest = { showSongList = false },
                        title = { Text("Músicas") },
                        text = {
                            LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                                items(tracks) { item ->
                                    Column(modifier = Modifier.fillMaxWidth().clickable {
                                        val index = tracks.indexOf(item).coerceAtLeast(0)
                                        if (controller != null) { controller.setMediaItems(tracks, index, 0L); controller.prepare(); controller.play() }
                                        showSongList = false
                                    }.padding(vertical = 10.dp)) {
                                        Text(item.mediaMetadata.title?.toString() ?: "Sem título", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(item.mediaMetadata.artist?.toString() ?: "Artista desconhecido", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                            }
                        },
                        confirmButton = { TextButton(onClick = { showSongList = false }) { Text("Fechar") } }
                    )
                }
                if (tab != "Músicas") {
                    AlertDialog(
                        onDismissRequest = { tab = "Músicas" },
                        title = { Text("Cantores") },
                        text = {
                            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                                items(tracks.map { it.mediaMetadata.artist?.toString() ?: "Artista desconhecido" }.distinct().sorted()) { artist ->
                                    Text(artist, modifier = Modifier.fillMaxWidth().clickable {
                                        val filtered = tracks.filter { it.mediaMetadata.artist?.toString() == artist }
                                        if (filtered.isNotEmpty()) { controller?.setMediaItems(filtered); controller?.prepare(); controller?.play() }
                                        tab = "Músicas"
                                    }.padding(vertical = 12.dp))
                                }
                            }
                        },
                        confirmButton = { TextButton(onClick = { tab = "Músicas" }) { Text("Fechar") } }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        executor.shutdown()
        super.onDestroy()
    }

    private fun formatTime(ms: Long): String {
        val seconds = (ms / 1000).coerceAtLeast(0)
        return String.format("%d:%02d", seconds / 60, seconds % 60)
    }
}
