package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.example.model.ChannelItem
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PlayerDialog(
    channel: ChannelItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isBuffering by remember { mutableStateOf(true) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var retryTrigger by remember { mutableStateOf(0) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                videoViewRef?.stopPlayback()
            } catch (ignored: Exception) {}
        }
    }

    Dialog(onDismissRequest = {
        try {
            videoViewRef?.stopPlayback()
        } catch (ignored: Exception) {}
        onDismiss()
    }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = DarkSurface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = channel.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            maxLines = 1
                        )
                    }

                    IconButton(onClick = {
                        try {
                            videoViewRef?.stopPlayback()
                        } catch (ignored: Exception) {}
                        onDismiss()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Video Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                videoViewRef = this
                                val mediaController = MediaController(ctx)
                                mediaController.setAnchorView(this)
                                setMediaController(mediaController)

                                setOnPreparedListener { mp ->
                                    isBuffering = false
                                    playbackError = null
                                    mp.isLooping = true
                                    mp.start()
                                }

                                setOnErrorListener { _, _, _ ->
                                    isBuffering = false
                                    playbackError = "Stream memerlukan pemutar eksternal (VLC/IPTV) atau token live streaming."
                                    true
                                }

                                try {
                                    val headers = mutableMapOf<String, String>()
                                    if (channel.userAgent.isNotBlank()) {
                                        headers["User-Agent"] = channel.userAgent
                                    }
                                    if (channel.httpReferrer.isNotBlank()) {
                                        headers["Referer"] = channel.httpReferrer
                                    }

                                    val uri = Uri.parse(channel.streamUrl)
                                    if (headers.isNotEmpty()) {
                                        setVideoURI(uri, headers)
                                    } else {
                                        setVideoURI(uri)
                                    }
                                    requestFocus()
                                    start()
                                } catch (e: Exception) {
                                    isBuffering = false
                                    playbackError = e.localizedMessage ?: "Gagal memuat URL stream"
                                }
                            }
                        },
                        update = { view ->
                            if (retryTrigger > 0) {
                                try {
                                    isBuffering = true
                                    playbackError = null
                                    view.setVideoURI(Uri.parse(channel.streamUrl))
                                    view.start()
                                } catch (e: Exception) {
                                    isBuffering = false
                                    playbackError = e.localizedMessage
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isBuffering && playbackError == null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = CyanNeon,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Menghubungkan ke siaran...",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }

                    if (playbackError != null) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = playbackError ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = ErrorRed,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Klik tombol di bawah untuk memutar via VLC / aplikasi lain.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons: Open in external player (VLC/MX Player) & Retry
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(Uri.parse(channel.streamUrl), "video/*")
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                val chooser = Intent.createChooser(intent, "Buka Siaran TV di Pemutar Eksternal")
                                chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(chooser)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Tidak ada pemutar video terpasang: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanNeon,
                            contentColor = DarkSurface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_external_player_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Putar di VLC / Eksternal", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { retryTrigger++ },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Coba Lagi",
                            tint = CyanNeon,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stream metadata
                Text(
                    text = "Kategori: ${channel.group}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyanNeon
                )
                Text(
                    text = "URL: ${channel.streamUrl}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    maxLines = 2
                )
            }
        }
    }
}
