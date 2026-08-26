package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM saved_playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<SavedPlaylistEntity>>

    @Query("SELECT * FROM saved_playlists WHERE id = :id")
    suspend fun getPlaylistById(id: Long): SavedPlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: SavedPlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: SavedPlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: SavedPlaylistEntity)

    @Query("DELETE FROM saved_playlists WHERE id = :id")
    suspend fun deleteById(id: Long)
}
