package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoMetadataDao {
    @Query("SELECT * FROM video_metadata")
    fun getAllMetadata(): Flow<List<CustomVideoMetadata>>

    @Query("SELECT * FROM video_metadata WHERE videoUri = :uri LIMIT 1")
    suspend fun getMetadataByUri(uri: String): CustomVideoMetadata?

    @Query("SELECT * FROM video_metadata WHERE isSubscribed = 1")
    fun getSubscribedVideos(): Flow<List<CustomVideoMetadata>>

    @Query("SELECT * FROM video_metadata WHERE isSavedToLibrary = 1")
    fun getSavedVideos(): Flow<List<CustomVideoMetadata>>

    @Query("SELECT * FROM video_metadata WHERE lastWatchedTimestamp > 0 ORDER BY lastWatchedTimestamp DESC")
    fun getWatchHistory(): Flow<List<CustomVideoMetadata>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(metadata: CustomVideoMetadata)

    @Update
    suspend fun update(metadata: CustomVideoMetadata)

    @Query("DELETE FROM video_metadata WHERE videoUri = :uri")
    suspend fun deleteByUri(uri: String)

    @Query("DELETE FROM video_metadata")
    suspend fun clearAll()
}
