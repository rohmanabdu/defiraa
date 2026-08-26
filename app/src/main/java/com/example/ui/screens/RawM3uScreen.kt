package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SaveM3uDialog
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.M3uExtractorViewModel

private const val MAX_EDITABLE_CHARS = 50_000

@Composable
fun RawM3uScreen(
    viewModel: M3uExtractorViewModel,
    modifier: Modifier = Modifier
) {
    val rawM3uText by viewModel.rawM3uText.collectAsState()
    val channels by viewModel.channels.collectAsState()

    var showDownloadDialog by remember { mutableStateOf(false) }

    val totalLines = if (rawM3uText.isBlank()) 0 else rawM3uText.lines().count()
    val isLargeFile = rawM3uText.length > MAX_EDITABLE_CHARS
    val fileSizeKb = rawM3uText.length / 1024

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Actions & Controls Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            color = DarkSurface
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Format Playlist (.M3U)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricViolet.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$totalLines Baris / ${channels.size} Channel",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricViolet
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons Row (Download, Copy, Share)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Primary Download Button for edited file
                    Button(
                        onClick = { showDownloadDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = DarkSurface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("download_edited_m3u_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Unduh .M3U", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.copyM3uToClipboard() },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHighlight, contentColor = TextPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_m3u_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salin", fontWeight = FontWeight.Medium)
                    }

                    OutlinedButton(
                        onClick = { viewModel.exportAndShareM3u("playlist_siaran_edit.m3u") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("share_m3u_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Bagikan",
                            tint = CyanGlow,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Raw Editor / Virtualized Viewer Box
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(16.dp)),
            color = DarkBackground
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isLargeFile) "PRATINJAU DOKUMEN .M3U" else "EDITOR TEKS #EXTM3U",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = CyanNeon
                        )
                        Text(
                            text = if (isLargeFile) "Mode Pratinjau Cepat ($fileSizeKb KB) • Siap diunduh" else "Dapat diedit langsung sebelum diunduh",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextMuted
                        )
                    }

                    if (!isLargeFile) {
                        Button(
                            onClick = { viewModel.parseAndApplyRawM3u(rawM3uText) },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet.copy(alpha = 0.3f), contentColor = ElectricViolet),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("apply_raw_m3u_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = ElectricViolet,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sinkron ke Daftar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (isLargeFile) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceHighlight)
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Playlist berukuran besar ($fileSizeKb KB). Gunakan tab 'Channel' untuk edit per-siaran, atau klik 'Unduh .M3U' untuk menyimpan file lengkap.",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isLargeFile) {
                    // Safe virtualized LazyColumn preview to avoid Android IME IPC TransactionTooLargeException
                    val lines = remember(rawM3uText) { rawM3uText.lines() }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        itemsIndexed(lines) { index, line ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp)
                            ) {
                                Text(
                                    text = "${index + 1}".padStart(4, ' '),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    ),
                                    modifier = Modifier.width(36.dp)
                                )
                                Text(
                                    text = line,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = when {
                                            line.startsWith("#EXTM3U") -> CyanNeon
                                            line.startsWith("#EXTINF") -> ElectricViolet
                                            line.startsWith("#EXTVLCOPT") -> CyanGlow
                                            line.startsWith("http") -> TextPrimary
                                            else -> TextSecondary
                                        }
                                    )
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = rawM3uText,
                        onValueChange = { viewModel.setRawM3uText(it) },
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = TextPrimary
                        ),
                        placeholder = { Text("#EXTM3U\n#EXTINF:-1,Nama Siaran\nhttp://...", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("raw_m3u_text_editor")
                    )
                }
            }
        }
    }

    // Save & Download Dialog
    if (showDownloadDialog) {
        SaveM3uDialog(
            initialFileName = "playlist_siaran_edit.m3u",
            channelCount = channels.size,
            contentToSave = rawM3uText,
            onDismiss = { showDownloadDialog = false },
            onSaveToDownloads = { fileName ->
                viewModel.saveM3uToPublicDownloads(fileName, rawM3uText)
            },
            onSaveToUri = { uri ->
                viewModel.writeM3uToUri(uri, rawM3uText)
            },
            onShare = { fileName ->
                viewModel.exportAndShareM3u(fileName, rawM3uText)
            }
        )
    }
}
