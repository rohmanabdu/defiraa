package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.CategorySortMode
import com.example.model.ChannelItem
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagerDialog(
    categories: List<String>,
    channels: List<ChannelItem>,
    onDismiss: () -> Unit,
    onRenameCategory: (oldName: String, newName: String) -> Unit,
    onDeleteCategory: (categoryName: String, deleteChannels: Boolean, targetCategory: String) -> Unit,
    onMoveCategory: (fromIndex: Int, toIndex: Int) -> Unit,
    onSortCategories: (mode: CategorySortMode) -> Unit,
    onApplyToPlaylist: () -> Unit
) {
    var categoryToRename by remember { mutableStateOf<String?>(null) }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp)),
            color = DarkSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyanNeon.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Kelola Kategori Siaran",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "${categories.size} Kategori • ${channels.size} Siaran",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricViolet
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_category_manager_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Sort Chips Carousel
                Text(
                    text = "Urutkan Cepat Kategori:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { onSortCategories(CategorySortMode.ALPHABETICAL_ASC) },
                            label = { Text("A → Z", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.SortByAlpha,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = DarkSurfaceHighlight,
                                labelColor = CyanNeon,
                                iconColor = CyanNeon
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = DarkSurfaceVariant
                            )
                        )
                    }

                    item {
                        FilterChip(
                            selected = false,
                            onClick = { onSortCategories(CategorySortMode.ALPHABETICAL_DESC) },
                            label = { Text("Z → A", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.SortByAlpha,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = DarkSurfaceHighlight,
                                labelColor = CyanNeon,
                                iconColor = CyanNeon
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = DarkSurfaceVariant
                            )
                        )
                    }

                    item {
                        FilterChip(
                            selected = false,
                            onClick = { onSortCategories(CategorySortMode.CHANNEL_COUNT_DESC) },
                            label = { Text("Banyak Siaran", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = DarkSurfaceHighlight,
                                labelColor = ElectricViolet,
                                iconColor = ElectricViolet
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = DarkSurfaceVariant
                            )
                        )
                    }

                    item {
                        FilterChip(
                            selected = false,
                            onClick = { onSortCategories(CategorySortMode.CHANNEL_COUNT_ASC) },
                            label = { Text("Sedikit Siaran", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = DarkSurfaceHighlight,
                                labelColor = TextSecondary,
                                iconColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = DarkSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Categories List (Reorderable / Editable)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkBackground)
                        .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(16.dp))
                        .padding(8.dp)
                ) {
                    if (categories.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tidak ada kategori dalam playlist.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(categories, key = { _, cat -> cat }) { index, categoryName ->
                                val channelCount = channels.count { it.group.trim() == categoryName.trim() }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("category_item_${index}"),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceHighlight),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Position & Reorder Buttons
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = "${index + 1}.",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = TextMuted,
                                                modifier = Modifier.width(22.dp)
                                            )

                                            Column {
                                                // Move up
                                                IconButton(
                                                    onClick = { onMoveCategory(index, index - 1) },
                                                    enabled = index > 0,
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowUpward,
                                                        contentDescription = "Pindah Ke Atas",
                                                        tint = if (index > 0) CyanNeon else TextMuted.copy(alpha = 0.3f),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }

                                                // Move down
                                                IconButton(
                                                    onClick = { onMoveCategory(index, index + 1) },
                                                    enabled = index < categories.size - 1,
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowDownward,
                                                        contentDescription = "Pindah Ke Bawah",
                                                        tint = if (index < categories.size - 1) CyanNeon else TextMuted.copy(alpha = 0.3f),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        // Category Name & Count
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = categoryName,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = TextPrimary,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "$channelCount siaran",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ElectricViolet
                                            )
                                        }

                                        // Action Icons: Rename & Delete
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            IconButton(
                                                onClick = { categoryToRename = categoryName },
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(DarkSurfaceVariant)
                                                    .testTag("rename_category_${index}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Ubah Nama Kategori",
                                                    tint = CyanNeon,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = { categoryToDelete = categoryName },
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(DarkSurfaceVariant)
                                                    .testTag("delete_category_${index}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Hapus Kategori",
                                                    tint = ErrorRed,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onApplyToPlaylist()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanNeon,
                            contentColor = DarkSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("apply_category_order_to_playlist_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatListNumbered,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Terapkan Urutan Kategori ke File .M3U",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Text("Selesai / Tutup", color = TextSecondary)
                    }
                }
            }
        }
    }

    // Rename Dialog
    categoryToRename?.let { oldName ->
        RenameCategoryDialog(
            currentName = oldName,
            onDismiss = { categoryToRename = null },
            onConfirm = { newName ->
                onRenameCategory(oldName, newName)
                categoryToRename = null
            }
        )
    }

    // Delete Dialog
    categoryToDelete?.let { targetCategory ->
        val otherCategories = categories.filterNot { it == targetCategory }
        val count = channels.count { it.group.trim() == targetCategory.trim() }

        DeleteCategoryDialog(
            categoryName = targetCategory,
            channelCount = count,
            availableMoveCategories = if (otherCategories.isEmpty()) listOf("Umum") else otherCategories,
            onDismiss = { categoryToDelete = null },
            onConfirm = { deleteChannels, targetCategoryForMove ->
                onDeleteCategory(targetCategory, deleteChannels, targetCategoryForMove)
                categoryToDelete = null
            }
        )
    }
}

@Composable
fun RenameCategoryDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (newName: String) -> Unit
) {
    var nameInput by remember { mutableStateOf(currentName) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = DarkSurface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyanNeon.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "Ubah Nama Kategori",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Nama Kategori Baru:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    placeholder = { Text("Contoh: Olahraga, Berita, Hiburan", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedContainerColor = DarkSurfaceHighlight,
                        unfocusedContainerColor = DarkSurfaceHighlight
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_category_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            if (nameInput.isNotBlank()) {
                                onConfirm(nameInput.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanNeon,
                            contentColor = DarkSurface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        enabled = nameInput.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_rename_category_button")
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteCategoryDialog(
    categoryName: String,
    channelCount: Int,
    availableMoveCategories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (deleteChannels: Boolean, targetCategoryForMove: String) -> Unit
) {
    var deleteChannelsOption by remember { mutableStateOf(false) } // Default: move channels
    var targetMoveCategory by remember { mutableStateOf(availableMoveCategories.firstOrNull() ?: "Umum") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = DarkSurface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ErrorRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Hapus Kategori '$categoryName'?",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "$channelCount siaran terdaftar di kategori ini",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Pilih tindakan untuk $channelCount siaran dalam kategori ini:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Option 1: Move channels to another category
                Surface(
                    onClick = { deleteChannelsOption = false },
                    shape = RoundedCornerShape(12.dp),
                    color = if (!deleteChannelsOption) DarkSurfaceHighlight else DarkBackground,
                    border = if (!deleteChannelsOption) androidx.compose.foundation.BorderStroke(1.dp, CyanNeon) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !deleteChannelsOption,
                            onClick = { deleteChannelsOption = false },
                            colors = RadioButtonDefaults.colors(selectedColor = CyanNeon)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Pindahkan siaran ke kategori lain",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Hanya label kategori yang dihapus, siaran tetap tersimpan.",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = TextMuted
                            )
                        }
                    }
                }

                if (!deleteChannelsOption) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pindahkan ke kategori:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = targetMoveCategory,
                        onValueChange = { targetMoveCategory = it },
                        placeholder = { Text("Contoh: Umum", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = DarkSurfaceVariant,
                            focusedContainerColor = DarkSurfaceHighlight,
                            unfocusedContainerColor = DarkSurfaceHighlight
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: Delete channels completely
                Surface(
                    onClick = { deleteChannelsOption = true },
                    shape = RoundedCornerShape(12.dp),
                    color = if (deleteChannelsOption) DarkSurfaceHighlight else DarkBackground,
                    border = if (deleteChannelsOption) androidx.compose.foundation.BorderStroke(1.dp, ErrorRed) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = deleteChannelsOption,
                            onClick = { deleteChannelsOption = true },
                            colors = RadioButtonDefaults.colors(selectedColor = ErrorRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Hapus SEMUA $channelCount siaran",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = ErrorRed
                            )
                            Text(
                                text = "Seluruh siaran dalam kategori ini akan dihapus dari playlist.",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            onConfirm(deleteChannelsOption, targetMoveCategory.ifBlank { "Umum" })
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (deleteChannelsOption) ErrorRed else CyanNeon,
                            contentColor = if (deleteChannelsOption) Color.White else DarkSurface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_delete_category_button")
                    ) {
                        Text(
                            text = if (deleteChannelsOption) "Hapus Siaran" else "Hapus Kategori",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
