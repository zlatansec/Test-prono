package com.pronoagg.aggregator.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pronos",
    indices = [Index(value = ["link"], unique = true)]
)
data class PronoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelUsername: String,
    val channelDisplayName: String?,
    val messageId: Long,
    val link: String,
    val text: String,
    val timestampMillis: Long,
    val fetchedAt: Long = System.currentTimeMillis()
)
