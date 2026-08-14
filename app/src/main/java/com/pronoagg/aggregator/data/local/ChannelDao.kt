package com.pronoagg.aggregator.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {

    @Query("SELECT * FROM channels ORDER BY addedAt DESC")
    fun getAll(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels")
    suspend fun getAllOnce(): List<ChannelEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(channel: ChannelEntity): Long

    @Delete
    suspend fun delete(channel: ChannelEntity)
}
