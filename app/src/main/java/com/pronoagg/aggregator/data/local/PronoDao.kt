package com.pronoagg.aggregator.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PronoDao {

    @Query("SELECT * FROM pronos ORDER BY timestampMillis DESC")
    fun getAllOrderedByDate(): Flow<List<PronoEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(pronos: List<PronoEntity>)

    @Query("DELETE FROM pronos WHERE channelUsername = :username")
    suspend fun deleteForChannel(username: String)

    @Query(
        "DELETE FROM pronos WHERE id NOT IN (SELECT id FROM pronos ORDER BY timestampMillis DESC LIMIT :keep)"
    )
    suspend fun trimTo(keep: Int)
}
