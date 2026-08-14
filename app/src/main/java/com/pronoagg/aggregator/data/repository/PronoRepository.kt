package com.pronoagg.aggregator.data.repository

import com.pronoagg.aggregator.data.local.AppDatabase
import com.pronoagg.aggregator.data.local.ChannelEntity
import com.pronoagg.aggregator.data.local.ChannelSource
import com.pronoagg.aggregator.data.local.ChannelStatsRow
import com.pronoagg.aggregator.data.local.PronoEntity
import com.pronoagg.aggregator.data.local.PronoOutcome
import com.pronoagg.aggregator.data.remote.PronosoftScraper
import com.pronoagg.aggregator.data.remote.ScrapedPost
import com.pronoagg.aggregator.data.remote.TelegramPreviewScraper
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow

class PronoRepository(
    private val db: AppDatabase,
    private val telegramScraper: TelegramPreviewScraper = TelegramPreviewScraper(),
    private val pronosoftScraper: PronosoftScraper = PronosoftScraper()
) {

    val feed: Flow<List<PronoEntity>> = db.pronoDao().getAllOrderedByDate()
    val channels: Flow<List<ChannelEntity>> = db.channelDao().getAll()
    val channelStats: Flow<List<ChannelStatsRow>> = db.pronoDao().getStatsByChannel()

    suspend fun setOutcome(pronoId: Long, outcome: PronoOutcome) {
        db.pronoDao().updateOutcome(pronoId, outcome.name)
    }

    suspend fun addChannel(rawUsername: String, source: ChannelSource = ChannelSource.TELEGRAM) {
        val username = if (source == ChannelSource.TELEGRAM) normalize(rawUsername) else rawUsername.trim()
        if (username.isBlank()) return
        val channel = ChannelEntity(username = username, source = source.name)
        db.channelDao().insert(channel)
        refreshChannel(channel)
    }

    suspend fun removeChannel(channel: ChannelEntity) {
        db.channelDao().delete(channel)
        db.pronoDao().deleteForChannel(channel.username)
    }

    suspend fun refreshAll() = coroutineScope {
        val channels = db.channelDao().getAllOnce()
        channels.map { channel -> async { refreshChannel(channel) } }.forEach { it.await() }
        db.pronoDao().trimTo(MAX_STORED_PRONOS)
    }

    private suspend fun refreshChannel(channel: ChannelEntity) {
        val posts = try {
            when (ChannelSource.fromStorage(channel.source)) {
                ChannelSource.TELEGRAM -> telegramScraper.fetchChannel(channel.username)
                ChannelSource.PRONOSOFT -> pronosoftScraper.fetchPredictions()
            }
        } catch (e: Exception) {
            emptyList<ScrapedPost>()
        }
        if (posts.isEmpty()) return

        val entities = posts.map { post ->
            PronoEntity(
                channelUsername = post.sourceUsername,
                channelDisplayName = post.sourceDisplayName,
                messageId = post.postId,
                link = post.link,
                text = post.text,
                timestampMillis = post.timestampMillis
            )
        }
        db.pronoDao().insertAll(entities)
    }

    private fun normalize(rawUsername: String): String {
        return rawUsername
            .trim()
            .removePrefix("https://t.me/")
            .removePrefix("http://t.me/")
            .removePrefix("t.me/")
            .removePrefix("@")
            .trim()
    }

    companion object {
        private const val MAX_STORED_PRONOS = 500
    }
}
