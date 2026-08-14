package com.pronoagg.aggregator.data.remote

import com.pronoagg.aggregator.data.PronoClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

/**
 * Pronosoft n'a pas d'API et sa page n'a pas pu être inspectée pendant le
 * développement (domaine bloqué par la politique réseau de l'environnement
 * de dev). Plutôt que de parier sur des sélecteurs CSS précis probablement
 * faux, ce scraper ratisse large (article/li/tr/blocs "prono*") et réutilise
 * PronoClassifier pour ne garder que ce qui ressemble à un vrai pronostic.
 * À affiner une fois testé contre le vrai site.
 */
class PronosoftScraper(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {

    suspend fun fetchPredictions(): List<ScrapedPost> = withContext(Dispatchers.IO) {
        PAGES.flatMap { pageUrl ->
            try {
                fetchPage(pageUrl)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private fun fetchPage(pageUrl: String): List<ScrapedPost> {
        val request = Request.Builder()
            .url(pageUrl)
            .header("User-Agent", "Mozilla/5.0 (Android 14; Mobile) PronoAggregator")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return emptyList()
            val html = response.body?.string() ?: return emptyList()
            return parse(pageUrl, html)
        }
    }

    private fun parse(pageUrl: String, html: String): List<ScrapedPost> {
        val doc = Jsoup.parse(html, pageUrl)
        val candidates = doc.select("article, li, tr, [class*=prono], [class*=pronostic]")

        val seenText = HashSet<String>()
        val posts = mutableListOf<ScrapedPost>()

        for (candidate in candidates) {
            val text = candidate.text().trim()
            if (text.length < 20 || text.length > 600) continue
            if (!seenText.add(text)) continue
            if (!PronoClassifier.looksLikeProno(text)) continue

            val link = candidate.selectFirst("a[href]")?.absUrl("href")?.takeIf { it.isNotBlank() }
                ?: "$pageUrl#${text.hashCode()}"

            posts += ScrapedPost(
                sourceUsername = SOURCE_ID,
                sourceDisplayName = "Pronosoft",
                postId = text.hashCode().toLong(),
                link = link,
                text = text,
                timestampMillis = System.currentTimeMillis()
            )
        }

        return posts
    }

    companion object {
        const val SOURCE_ID = "pronosoft"
        private val PAGES = listOf(
            "https://www.pronosoft.com/fr/bookmakers/pronostics/"
        )
    }
}
