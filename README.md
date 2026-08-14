# Pronos Agrégés

Application Android native (Kotlin + Jetpack Compose) qui agrège dans un flux
unique les messages postés sur des canaux Telegram **publics** de pronostics
sportifs.

## Fonctionnement

- Tu ajoutes le `@username` d'un ou plusieurs canaux Telegram publics dans
  l'écran "Canaux".
- L'app va chercher les messages via la page de prévisualisation publique de
  Telegram (`https://t.me/s/<canal>`), la même page légère que Telegram sert
  pour les aperçus de liens/embeds — aucun compte, token de bot ni login requis.
  Ça ne fonctionne que pour les canaux marqués **publics** par leur propriétaire.
- Les messages sont dédupliqués (par lien Telegram) et stockés en local
  (Room/SQLite), puis affichés dans un flux chronologique.
- Un `WorkManager` rafraîchit automatiquement toutes les 30 minutes ; un bouton
  de rafraîchissement manuel est aussi disponible dans la barre du haut.

## Structure du projet

```
app/src/main/java/com/pronoagg/aggregator/
├── PronoAggApplication.kt        # planifie le rafraîchissement périodique
├── MainActivity.kt
├── data/
│   ├── local/                    # Room : ChannelEntity, PronoEntity, DAOs, AppDatabase
│   ├── remote/                   # TelegramPreviewScraper (OkHttp + Jsoup)
│   └── repository/               # PronoRepository (orchestration + dédup)
├── worker/                       # RefreshPronosWorker (WorkManager)
└── ui/
    ├── feed/                     # Écran flux de pronos + ViewModel
    ├── channels/                 # Écran gestion des canaux + ViewModel
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

## Limites connues / pistes d'amélioration

- Seuls les canaux Telegram **publics** fonctionnent (pas les canaux privés,
  ni les groupes payants nécessitant une invitation).
- Pas de détection automatique du statut gagné/perdu d'un prono, ni de stats
  (ROI, taux de réussite) — la V1 est un flux simple.
- Le scraping HTML de `t.me/s/` peut casser si Telegram change la structure
  de cette page ; à surveiller.
- Respecte les CGU des canaux que tu ajoutes et évite un rafraîchissement
  trop agressif (l'intervalle par défaut est de 30 minutes).
