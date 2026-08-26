package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_playlists")
data class SavedPlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val sourceUrl: String,
    val channelCount: Int,
    val rawM3u: String,
    val createdAt: Long = System.currentTimeMillis()
)
