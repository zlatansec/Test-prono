package com.pronoagg.aggregator.data

/**
 * Canaux Telegram publics de tipsters indépendants, utilisés pour peupler
 * l'app au premier lancement. Ce sont des sources tierces non vérifiées
 * quant à la qualité de leurs pronostics — remplaçables à tout moment
 * depuis l'écran "Canaux".
 */
object DefaultChannels {
    val SEED_USERNAMES = listOf(
        "louise_prono",
        "pronosticsfootball365",
        "neopronostics",
        "parieursfoot"
    )
}
