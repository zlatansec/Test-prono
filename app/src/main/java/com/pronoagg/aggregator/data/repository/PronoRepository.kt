package com.pronoagg.aggregator.data.repository

import com.pronoagg.aggregator.data.local.AppDatabase
import com.pronoagg.aggregator.data.local.ChannelEntity
import com.pronoagg.aggregator.data.local.PronoEntity
import com.pronoagg.aggregator.data.remote.TelegramPreviewScraper
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow

class PronoRepository(
    private val db: AppDatabase,
    private val scraper: TelegramPreviewScraper = TelegramPreviewScraper()
) {

    val feed: Flow<List<PronoEntity>> = db.pronoDao().getAllOrderedByDate()
    val channels: Flow<List<ChannelEntity>> = db.channelDao().getAll()

    suspend fun addChannel(rawUsername: String) {
        val username = normalize(rawUsername)
        if (username.isBlank()) return
        db.channelDao().insert(ChannelEntity(username = username))
        refreshChannel(username)
    }

    suspend fun removeChannel(channel: ChannelEntity) {
        db.channelDao().delete(channel)
        db.pronoDao().deleteForChannel(channel.username)
    }

    suspend fun refreshAll() = coroutineScope {
        val channels = db.channelDao().getAllOnce()
        channels.map { channel -> async { refreshChannel(channel.username) } }.forEach { it.await() }
        db.pronoDao().trimTo(MAX_STORED_PRONOS)
    }

    private suspend fun refreshChannel(username: String) {
        val messages = try {
            scraper.fetchChannel(username)
        } catch (e: Exception) {
            emptyList()
        }
        if (messages.isEmpty()) return

        val entities = messages.map { message ->
            PronoEntity(
                channelUsername = message.channelUsername,
                channelDisplayName = message.channelDisplayName,
                messageId = message.messageId,
                link = message.link,
                text = message.text,
                timestampMillis = message.timestampMillis
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
