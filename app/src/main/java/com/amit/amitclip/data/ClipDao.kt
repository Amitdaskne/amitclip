package com.amit.amitclip.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipDao {

    @Query("SELECT * FROM clips ORDER BY isPinned DESC, timestamp DESC")
    fun getAllClips(): Flow<List<Clip>>

    @Query("SELECT * FROM clips WHERE text LIKE '%' || :query || '%' ORDER BY isPinned DESC, timestamp DESC")
    fun searchClips(query: String): Flow<List<Clip>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(clip: Clip): Long

    @Delete
    suspend fun delete(clip: Clip)

    @Query("UPDATE clips SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: Long, isPinned: Boolean)

    @Query("DELETE FROM clips WHERE isPinned = 0 AND id NOT IN (SELECT id FROM clips WHERE isPinned = 0 ORDER BY timestamp DESC LIMIT :keep)")
    suspend fun pruneOldClips(keep: Int)
}
