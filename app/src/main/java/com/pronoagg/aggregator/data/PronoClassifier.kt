package com.pronoagg.aggregator.data

/**
 * Heuristique légère pour distinguer un vrai prono (cote, confrontation,
 * vocabulaire paris/sport) des posts de bruit que postent aussi les canaux
 * de tipsters : promo VIP, "rejoins le groupe", messages de bienvenue, etc.
 * Ce n'est pas un classifieur ML, juste des règles pragmatiques, réglées pour
 * privilégier le rappel (mieux vaut laisser passer un peu de bruit que
 * cacher un vrai prono) — d'où le toggle "Tout afficher" en complément.
 */
object PronoClassifier {

    private val oddsPattern = Regex("""\b\d{1,2}[.,]\d{2}\b""")

    // Un seul mot en majuscule de chaque côté suffit (pas besoin que "Saint-Germain"
    // ou "des Bleus" soit entièrement capitalisé) pour capter "Real - Betis",
    // "PSG vs Lyon", "OM 🆚 Marseille", etc.
    private val matchupPattern = Regex(
        """\b\p{Lu}[\p{L}'.]*(?:\s[\p{L}'.]+)*\s(?:-|vs\.?|VS|🆚)\s\p{Lu}[\p{L}'.]*(?:\s[\p{L}'.]+)*"""
    )

    private val bettingKeywords = listOf(
        "cote", "cotes", "mise", "combiné", "combine", "double chance",
        "over", "under", "btts", "value bet", "unité", "unites", "unités",
        "prono", "pronostic", "bankroll", "surebet", "1n2", "1x2", "handicap"
    )

    private val sportKeywords = listOf(
        "foot", "football", "tennis", "basket", "nba", "ligue 1", "ligue 2",
        "premier league", "champions league", "europa league", "liga", "serie a",
        "bundesliga", "wta", "atp", "euroleague", "coupe de france"
    )

    fun looksLikeProno(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.length < 8) return false

        val lower = trimmed.lowercase()

        val hasOdds = oddsPattern.containsMatchIn(trimmed)
        val hasMatchup = matchupPattern.containsMatchIn(trimmed)
        val hasBettingVocab = bettingKeywords.any { lower.contains(it) }
        val hasSportVocab = sportKeywords.any { lower.contains(it) }

        return hasOdds || hasMatchup || hasBettingVocab || hasSportVocab
    }
}
