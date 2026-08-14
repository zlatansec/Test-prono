package com.pronoagg.aggregator.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "channels",
    indices = [Index(value = ["username"], unique = true)]
)
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val displayName: String? = null,
    val addedAt: Long = System.currentTimeMillis(),
    val source: String = ChannelSource.TELEGRAM.name
)
