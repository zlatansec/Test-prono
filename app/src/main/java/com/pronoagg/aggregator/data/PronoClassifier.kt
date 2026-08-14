package com.pronoagg.aggregator.data

/**
 * Heuristique légère pour distinguer un vrai prono (cote, confrontation,
 * vocabulaire paris/sport) des posts de bruit que postent aussi les canaux
 * de tipsters : promo VIP, "rejoins le groupe", messages de bienvenue, etc.
 * Ce n'est pas un classifieur ML, juste des règles pragmatiques — le but est
 * de réduire le bruit, pas d'être parfait (d'où le toggle "Tout afficher").
 */
object PronoClassifier {

    private val oddsPattern = Regex("""\b\d{1,2}[.,]\d{2}\b""")

    private val matchupPattern = Regex(
        """\b\p{Lu}[\p{L}'.]+(?:\s\p{Lu}[\p{L}'.]+)*\s(?:-|vs\.?|VS|🆚)\s\p{Lu}[\p{L}'.]+(?:\s\p{Lu}[\p{L}'.]+)*"""
    )

    private val bettingKeywords = listOf(
        "cote", "cotes", "pari ", "paris ", "mise ", "combiné", "combine",
        "double chance", "over ", "under ", "btts", "value bet", "value ",
        "unité", "unites", "unités", "prono", "pronostic", "bankroll", "surebet",
        "1n2", "1x2", "handicap"
    )

    private val sportKeywords = listOf(
        "foot", "football", "tennis", "basket", "nba", "ligue 1", "ligue 2",
        "premier league", "champions league", "europa league", "liga", "serie a",
        "bundesliga", "wta", "atp", "euroleague", "coupe de france"
    )

    private val adKeywords = listOf(
        "rejoins", "rejoignez", "abonne-toi", "abonnez-vous", "canal vip",
        "groupe vip", "code promo", "inscris-toi", "inscrivez-vous",
        "clique ici", "cliquez ici", "bonus de bienvenue", "parrainage"
    )

    fun looksLikeProno(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.length < 15) return false

        val lower = trimmed.lowercase()
        val isAd = adKeywords.any { lower.contains(it) }

        val hasOdds = oddsPattern.containsMatchIn(trimmed)
        val hasMatchup = matchupPattern.containsMatchIn(trimmed)
        val hasBettingVocab = bettingKeywords.any { lower.contains(it) }
        val hasSportVocab = sportKeywords.any { lower.contains(it) }

        if (isAd && !hasOdds && !hasMatchup) return false

        return hasOdds || hasMatchup || hasBettingVocab || hasSportVocab
    }
}
