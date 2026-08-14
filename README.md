# Pronos Agrégés

Application Android native (Kotlin + Jetpack Compose) qui agrège dans un flux
unique les messages postés sur des canaux Telegram **publics** de pronostics
sportifs, avec un suivi manuel des résultats par canal.

## Fonctionnement

- Au premier lancement, l'app est pré-remplie avec 4 canaux Telegram publics
  de tipsters indépendants (`louise_prono`, `pronosticsfootball365`,
  `neopronostics`, `parieursfoot`) — ce sont des sources tierces non vérifiées
  quant à la qualité de leurs pronostics, à remplacer ou compléter librement
  depuis l'écran "Canaux".
- L'app va chercher les messages via la page de prévisualisation publique de
  Telegram (`https://t.me/s/<canal>`), la même page légère que Telegram sert
  pour les aperçus de liens/embeds — aucun compte, token de bot ni login requis.
  Ça ne fonctionne que pour les canaux marqués **publics** par leur propriétaire.
- Les messages sont dédupliqués (par lien Telegram) et stockés en local
  (Room/SQLite), puis affichés dans un flux chronologique.
- Un `WorkManager` rafraîchit automatiquement toutes les 30 minutes ; un bouton
  de rafraîchissement manuel est aussi disponible dans la barre du haut.
- Chaque prono peut être marqué manuellement **Gagné / Perdu / Annulé** via des
  chips sur sa carte (Telegram ne donne pas le résultat de façon structurée, la
  détection automatique du gagné/perdu à partir du texte n'est pas fiable).
- L'écran **Stats** agrège ces marquages par canal : nombre de pronos, W/L/void
  et taux de réussite (gagnés / (gagnés + perdus)), triés du meilleur au moins bon.
- Les canaux Telegram mélangent souvent pronos et bruit (promo VIP, messages de
  bienvenue, liens de parrainage...). Le toggle **"Pronos uniquement"** filtre
  ces posts via une heuristique locale (`PronoClassifier`) qui cherche des
  cotes, un format "Équipe A - Équipe B" ou du vocabulaire paris/sport — pas un
  vrai classifieur, donc imparfait, d'où le toggle pour tout revoir au besoin.

## Structure du projet

```
app/src/main/java/com/pronoagg/aggregator/
├── PronoAggApplication.kt        # planifie le rafraîchissement périodique + seed des canaux par défaut
├── MainActivity.kt
├── data/
│   ├── DefaultChannels.kt        # canaux Telegram pré-remplis au premier lancement
│   ├── PronoClassifier.kt        # heuristique "ça ressemble à un prono ?" pour filtrer le bruit
│   ├── local/                    # Room : ChannelEntity, PronoEntity, PronoOutcome, DAOs, AppDatabase
│   ├── remote/                   # TelegramPreviewScraper (OkHttp + Jsoup)
│   └── repository/               # PronoRepository (orchestration + dédup + stats)
├── worker/                       # RefreshPronosWorker (WorkManager)
└── ui/
    ├── feed/                     # Écran flux de pronos + ViewModel
    ├── channels/                 # Écran gestion des canaux + ViewModel
    ├── stats/                    # Écran statistiques par canal + ViewModel
    └── theme/                    # Thème Material3
```

## Build

Ce projet est un module Gradle standard. Pour compiler :

```bash
./gradlew assembleDebug
```

L'APK debug est généré dans `app/build/outputs/apk/debug/`.

Tu peux aussi simplement ouvrir le dossier dans Android Studio (Giraffe ou
plus récent) — il détectera le projet Gradle automatiquement.

Un workflow GitHub Actions (`.github/workflows/android-build.yml`) compile
automatiquement l'APK debug à chaque push et le publie en artifact du run,
donc tu peux le télécharger sans installer de SDK Android en local.

**Lien de téléchargement stable** : chaque push met aussi à jour une release
GitHub `apk-latest` avec le dernier APK compilé — un seul lien à garder en
favori, pas besoin de refouiller les runs Actions à chaque fois :

https://github.com/zlatansec/Test-prono/releases/tag/apk-latest

## Limites connues / pistes d'amélioration

- Seuls les canaux Telegram **publics** fonctionnent (pas les canaux privés,
  ni les groupes payants nécessitant une invitation).
- Le marquage gagné/perdu/annulé est manuel — pas de calcul de ROI (nécessite
  la cote et la mise, que les canaux ne fournissent pas toujours de façon
  structurée).
- Le scraping HTML de `t.me/s/` peut casser si Telegram change la structure
  de cette page ; à surveiller.
- Respecte les CGU des canaux que tu ajoutes et évite un rafraîchissement
  trop agressif (l'intervalle par défaut est de 30 minutes).
- Les 4 canaux par défaut sont des exemples pour démarrer rapidement, pas une
  recommandation de qualité — vérifie toi-même leur fiabilité avant de suivre
  leurs pronostics.
- Le filtre "Pronos uniquement" est heuristique (mots-clés/regex), pas un vrai
  classifieur : il peut laisser passer du bruit ou cacher un vrai prono mal
  formulé. Prochaine piste : marquage manuel "Ce n'est pas un prono" pour
  affiner, ou passage à un modèle plus fin.
