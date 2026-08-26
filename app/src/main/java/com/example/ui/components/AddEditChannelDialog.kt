package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ChannelItem
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AddEditChannelDialog(
    channelToEdit: ChannelItem? = null,
    availableCategories: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (name: String, url: String, group: String, logo: String) -> Unit
) {
    var name by remember { mutableStateOf(channelToEdit?.name ?: "") }
    var streamUrl by remember { mutableStateOf(channelToEdit?.streamUrl ?: "") }
    var group by remember { mutableStateOf(channelToEdit?.group ?: if (availableCategories.isNotEmpty()) availableCategories.first() else "Umum") }
    var logoUrl by remember { mutableStateOf(channelToEdit?.logoUrl ?: "") }

    val isEdit = channelToEdit != null

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = DarkSurface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (isEdit) "Edit Channel Siaran" else "Tambah Channel Baru",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Channel") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        focusedLabelColor = CyanNeon,
                        unfocusedContainerColor = DarkSurfaceHighlight,
                        focusedContainerColor = DarkSurfaceHighlight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = streamUrl,
                    onValueChange = { streamUrl = it },
                    label = { Text("URL Stream (HLS / M3U8 / MP4)") },
                    singleLine = false,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        focusedLabelColor = CyanNeon,
                        unfocusedContainerColor = DarkSurfaceHighlight,
                        focusedContainerColor = DarkSurfaceHighlight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = group,
                    onValueChange = { group = it },
                    label = { Text("Kategori (Group Title)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        focusedLabelColor = CyanNeon,
                        unfocusedContainerColor = DarkSurfaceHighlight,
                        focusedContainerColor = DarkSurfaceHighlight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (availableCategories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(availableCategories) { catName ->
                            val isSelected = group.equals(catName, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { group = catName }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = catName,
                                    fontSize = 11.sp,
                                    color = if (isSelected) CyanNeon else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = logoUrl,
                    onValueChange = { logoUrl = it },
                    label = { Text("URL Logo / Icon (Opsional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        focusedLabelColor = CyanNeon,
                        unfocusedContainerColor = DarkSurfaceHighlight,
                        focusedContainerColor = DarkSurfaceHighlight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Batal", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank() && streamUrl.isNotBlank()) {
                                onSave(name, streamUrl, group, logoUrl)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanNeon,
                            contentColor = DarkSurface
                        ),
                        enabled = name.isNotBlank() && streamUrl.isNotBlank()
                    ) {
                        Text("Simpan Channel", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
