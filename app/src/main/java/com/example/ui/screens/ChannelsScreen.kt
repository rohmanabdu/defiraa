package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.TvOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChannelItem
import com.example.ui.components.AddEditChannelDialog
import com.example.ui.components.CategoryManagerDialog
import com.example.ui.components.ChannelCard
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.SaveM3uDialog
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.M3uExtractorViewModel

@Composable
fun ChannelsScreen(
    viewModel: M3uExtractorViewModel,
    modifier: Modifier = Modifier
) {
    val channels by viewModel.channels.collectAsState()
    val rawM3uText by viewModel.rawM3uText.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isTestingStreams by viewModel.isTestingStreams.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var channelToEdit by remember { mutableStateOf<ChannelItem?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showSaveDownloadDialog by remember { mutableStateOf(false) }
    var showCategoryManagerDialog by remember { mutableStateOf(false) }

    val customCategoryOrder by viewModel.customCategoryOrder.collectAsState()
    val rawCategories = remember(channels, customCategoryOrder) {
        viewModel.getOrderedCategories()
    }
    val categories = listOf("Semua") + rawCategories

    val filteredChannels = channels.filter { ch ->
        val matchesCategory = (selectedCategory == "Semua" || ch.group.equals(selectedCategory, ignoreCase = true))
        val matchesSearch = searchQuery.isBlank() ||
                ch.name.contains(searchQuery, ignoreCase = true) ||
                ch.group.contains(searchQuery, ignoreCase = true) ||
                ch.streamUrl.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val selectedCount = channels.count { it.isSelected }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search and Controls Bar
            Surface(
                color = DarkSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Cari siaran atau kata kunci...", color = TextMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = CyanNeon
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Hapus",
                                        tint = TextMuted
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = DarkSurfaceVariant,
                            focusedContainerColor = DarkSurfaceHighlight,
                            unfocusedContainerColor = DarkSurfaceHighlight
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_channel_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Chips Carousel with Manage Button
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Manage Categories Chip
                        item {
                            Surface(
                                onClick = { showCategoryManagerDialog = true },
                                shape = RoundedCornerShape(20.dp),
                                color = ElectricViolet.copy(alpha = 0.18f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.6f)),
                                modifier = Modifier.testTag("manage_categories_chip_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = ElectricViolet,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Kelola Kategori (${rawCategories.size})",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        items(categories) { cat ->
                            val isSelected = cat == selectedCategory
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) CyanNeon else DarkSurfaceHighlight)
                                    .clickable { viewModel.setSelectedCategory(cat) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) DarkSurface else TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Batch Actions & Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$selectedCount dari ${channels.size} siaran dipilih",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanNeon
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(
                                onClick = {
                                    val allSelected = channels.isNotEmpty() && channels.all { it.isSelected }
                                    viewModel.selectAllChannels(!allSelected)
                                }
                            ) {
                                Icon(
                                    imageVector = if (channels.isNotEmpty() && channels.all { it.isSelected }) Icons.Default.Deselect else Icons.Default.SelectAll,
                                    contentDescription = null,
                                    tint = CyanNeon,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (channels.isNotEmpty() && channels.all { it.isSelected }) "Lepas Semua" else "Pilih Semua",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanNeon
                                )
                            }

                            TextButton(
                                onClick = { viewModel.testAllChannels() },
                                enabled = !isTestingStreams && channels.isNotEmpty()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = ElectricViolet,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isTestingStreams) "Memeriksa..." else "Cek Ping",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricViolet
                                )
                            }
                        }
                    }

                    // Active Selection Actions Toolbar (Hapus Terpilih & Unduh M3U)
                    AnimatedVisibility(
                        visible = selectedCount > 0,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            color = DarkSurfaceHighlight
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { showDeleteConfirmDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ErrorRed.copy(alpha = 0.9f),
                                        contentColor = androidx.compose.ui.graphics.Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("delete_selected_channels_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Hapus ($selectedCount) Terpilih",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                Button(
                                    onClick = { showSaveDownloadDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CyanNeon,
                                        contentColor = DarkSurface
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("download_current_m3u_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Unduh .M3U",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Channel Items List or Empty State
            if (filteredChannels.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TvOff,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (channels.isEmpty()) "Belum ada channel yang diekstrak" else "Tidak ada channel sesuai pencarian",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (channels.isEmpty()) "Buka tab 'Ekstrak' lalu masukkan link APK siaran TV Anda." else "Coba ubah kata kunci atau kategori filter.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        if (channels.isEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.setSelectedTab(0) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = DarkSurface)
                            ) {
                                Text("Mulai Ekstrak Sekarang", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredChannels, key = { it.id }) { channel ->
                        ChannelCard(
                            channel = channel,
                            onToggleSelect = { viewModel.toggleChannelSelection(channel.id) },
                            onPlayClick = { viewModel.setCurrentlyPlayingChannel(channel) },
                            onEditClick = { channelToEdit = channel },
                            onDeleteClick = { viewModel.deleteChannel(channel.id) },
                            onTestClick = { viewModel.testSingleChannel(channel.id) }
                        )
                    }
                }
            }
        }

        // Bottom Bar Action buttons (Floating Action Button to Add Custom Channel)
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = CyanNeon,
            contentColor = DarkSurface,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_custom_channel_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Tambah Channel Manual",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    // Add Dialog
    if (showAddDialog) {
        AddEditChannelDialog(
            channelToEdit = null,
            availableCategories = rawCategories,
            onDismiss = { showAddDialog = false },
            onSave = { name, url, group, logo ->
                viewModel.addManualChannel(name, url, group, logo)
            }
        )
    }

    // Edit Dialog
    channelToEdit?.let { ch ->
        AddEditChannelDialog(
            channelToEdit = ch,
            availableCategories = rawCategories,
            onDismiss = { channelToEdit = null },
            onSave = { name, url, group, logo ->
                viewModel.updateChannel(
                    ch.copy(name = name, streamUrl = url, group = group, logoUrl = logo)
                )
                channelToEdit = null
            }
        )
    }

    // Delete Selected Confirmation Dialog
    if (showDeleteConfirmDialog) {
        DeleteConfirmDialog(
            count = selectedCount,
            onDismiss = { showDeleteConfirmDialog = false },
            onConfirm = {
                viewModel.deleteSelectedChannels()
            }
        )
    }

    // Save & Download Dialog
    if (showSaveDownloadDialog) {
        SaveM3uDialog(
            initialFileName = "playlist_siaran_edit.m3u",
            channelCount = channels.size,
            contentToSave = rawM3uText,
            onDismiss = { showSaveDownloadDialog = false },
            onSaveToDownloads = { fileName ->
                viewModel.saveM3uToPublicDownloads(fileName)
            },
            onSaveToUri = { uri ->
                viewModel.writeM3uToUri(uri)
            },
            onShare = { fileName ->
                viewModel.exportAndShareM3u(fileName)
            }
        )
    }

    // Category Manager Dialog (Rename, Delete, Reorder, Sort)
    if (showCategoryManagerDialog) {
        CategoryManagerDialog(
            categories = rawCategories,
            channels = channels,
            onDismiss = { showCategoryManagerDialog = false },
            onRenameCategory = { oldName, newName ->
                viewModel.renameCategory(oldName, newName)
            },
            onDeleteCategory = { categoryName, deleteChannels, targetCategory ->
                viewModel.deleteCategory(categoryName, deleteChannels, targetCategory)
            },
            onMoveCategory = { fromIndex, toIndex ->
                viewModel.moveCategory(fromIndex, toIndex, syncToPlaylist = true)
            },
            onSortCategories = { mode ->
                viewModel.sortCategories(mode, applyToChannelsPlaylist = true)
            },
            onApplyToPlaylist = {
                viewModel.applyCategoryOrderToChannels()
            }
        )
    }
}
