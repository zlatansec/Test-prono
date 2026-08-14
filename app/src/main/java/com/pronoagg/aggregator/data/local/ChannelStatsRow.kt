package com.pronoagg.aggregator.data.local

data class ChannelStatsRow(
    val channelUsername: String,
    val channelDisplayName: String?,
    val total: Int,
    val won: Int,
    val lost: Int,
    val voidCount: Int
) {
    val decided: Int get() = won + lost
    val winRatePercent: Int? get() = if (decided == 0) null else (won * 100) / decided
}
