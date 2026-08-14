package com.pronoagg.aggregator.data.remote

/**
 * Représentation neutre d'un post scrapé, quelle que soit la source
 * (canal Telegram, page Pronosoft, ...).
 */
data class ScrapedPost(
    val sourceUsername: String,
    val sourceDisplayName: String?,
    val postId: Long,
    val link: String,
    val text: String,
    val timestampMillis: Long
)
