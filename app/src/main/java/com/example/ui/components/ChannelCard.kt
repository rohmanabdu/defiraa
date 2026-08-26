package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.SensorsOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.ChannelItem
import com.example.model.StreamStatus
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun ChannelCard(
    channel: ChannelItem,
    onToggleSelect: () -> Unit,
    onPlayClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onTestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (channel.isSelected) CyanNeon.copy(alpha = 0.4f) else DarkSurfaceVariant,
                RoundedCornerShape(16.dp)
            ),
        color = if (channel.isSelected) DarkSurface else DarkSurface.copy(alpha = 0.6f),
        tonalElevation = if (channel.isSelected) 4.dp else 1.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox for M3U inclusion
                Checkbox(
                    checked = channel.isSelected,
                    onCheckedChange = { onToggleSelect() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = CyanNeon,
                        checkmarkColor = DarkSurface
                    ),
                    modifier = Modifier.testTag("channel_checkbox_${channel.id}")
                )

                // Channel Logo or Placeholder Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceHighlight)
                        .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (channel.logoUrl.isNotBlank()) {
                        AsyncImage(
                            model = channel.logoUrl,
                            contentDescription = channel.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(40.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Channel Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = channel.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Category Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ElectricViolet.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = channel.group,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = ElectricViolet
                            )
                        }

                        // Status Badge
                        StreamStatusBadge(
                            status = channel.status,
                            latencyMs = channel.latencyMs,
                            onTestClick = onTestClick
                        )
                    }
                }

                // Action Buttons
                IconButton(
                    onClick = onPlayClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(CyanNeon.copy(alpha = 0.15f), CircleShape)
                        .testTag("play_channel_${channel.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Putar Siaran",
                        tint = CyanNeon,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Channel",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus Channel",
                        tint = ErrorRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Stream URL snippet
            Text(
                text = channel.streamUrl,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                ),
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 48.dp)
            )
        }
    }
}

@Composable
fun StreamStatusBadge(
    status: StreamStatus,
    latencyMs: Long,
    onTestClick: () -> Unit
) {
    val (bgColor, textColor, text, icon) = when (status) {
        StreamStatus.UNKNOWN -> Quadruple(
            DarkSurfaceHighlight,
            TextMuted,
            "Cek",
            Icons.Outlined.Sensors
        )
        StreamStatus.CHECKING -> Quadruple(
            DarkSurfaceHighlight,
            WarningAmber,
            "Memeriksa...",
            null
        )
        StreamStatus.ONLINE -> Quadruple(
            SuccessGreen.copy(alpha = 0.15f),
            SuccessGreen,
            if (latencyMs > 0) "${latencyMs}ms" else "Aktif",
            Icons.Outlined.Sensors
        )
        StreamStatus.OFFLINE -> Quadruple(
            ErrorRed.copy(alpha = 0.15f),
            ErrorRed,
            "Offline",
            Icons.Outlined.SensorsOff
        )
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .clickable { onTestClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (status == StreamStatus.CHECKING) {
            CircularProgressIndicator(
                modifier = Modifier.size(10.dp),
                strokeWidth = 1.5.dp,
                color = WarningAmber
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(10.dp)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = textColor
        )
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
