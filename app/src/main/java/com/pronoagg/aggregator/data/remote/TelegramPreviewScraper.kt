package com.pronoagg.aggregator.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Fetches public channel posts through Telegram's own SSR preview endpoint
 * (https://t.me/s/<username>), the same lightweight page Telegram serves for
 * link previews and embeds. No login or Bot API token is required, but it
 * only works for channels the owner has marked "public".
 */
class TelegramPreviewScraper(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {

    suspend fun fetchChannel(username: String): List<ScrapedPost> = withContext(Dispatchers.IO) {
        val cleanUsername = username.removePrefix("@").trim()
        val request = Request.Builder()
            .url("https://t.me/s/$cleanUsername")
            .header("User-Agent", "Mozilla/5.0 (Android 14; Mobile) PronoAggregator")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext emptyList()
            val html = response.body?.string() ?: return@withContext emptyList()
            parse(cleanUsername, html)
        }
    }

    private fun parse(username: String, html: String): List<ScrapedPost> {
        val doc = Jsoup.parse(html, "https://t.me/s/$username")
        val channelTitle = doc.selectFirst(".tgme_channel_info_header_title")?.text()

        return doc.select("div.tgme_widget_message[data-post]").mapNotNull { messageDiv ->
            val dataPost = messageDiv.attr("data-post")
            val messageId = dataPost.substringAfterLast("/").toLongOrNull() ?: return@mapNotNull null

            val textEl = messageDiv.selectFirst(".tgme_widget_message_text") ?: return@mapNotNull null
            val rawHtml = textEl.html().replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            val text = Jsoup.parseBodyFragment(rawHtml).body().wholeText().trim()
            if (text.isBlank()) return@mapNotNull null

            val datetimeAttr = messageDiv.selectFirst("time[datetime]")?.attr("datetime")
            val timestamp = datetimeAttr?.let { parseIso8601(it) } ?: return@mapNotNull null

            ScrapedPost(
                sourceUsername = username,
                sourceDisplayName = channelTitle,
                postId = messageId,
                link = "https://t.me/$username/$messageId",
                text = text,
                timestampMillis = timestamp
            )
        }
    }

    private fun parseIso8601(value: String): Long? {
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).parse(value)?.time
        } catch (e: Exception) {
            null
        }
    }
}
