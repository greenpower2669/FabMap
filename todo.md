# ✅ FabMap — état vivant

## Implémenté dans ce premier commit, vérification à faire
- Squelette Android et icône vectorielle FabMap.
- Navigation zoom/dézoom, bulles partagées, création, liens, recherche, lecture vocale, photos, procédures simples, sauvegarde ZIP.
- FAB Copilot : brain, brainmap, debughistorical et todo cohérents avec ce prototype.
- GitHub Actions sur push + lancement manuel, publication de l'APK directement en pré-release GitHub sans upload-artifact.

## À confirmer (ne pas déclarer prêt avant les preuves)
- Commit initial confirmé sur main : dd5ced45384997c5f7edc2d667566a2bd7370891.
- Run #1 (35661973983) : échec confirmé dans setup-android@v3 avant Gradle ; correctif SDK préparé dans ce commit. Vérifier la nouvelle Action, Release et lien APK.
- Essai sur téléphone : affichage, voix, sauvegarde/restauration et photos. Signatures debug : exporter avant réinstallation.
- Améliorer erreurs de restauration et gestion des médias volumineux selon tests.
- Ajout favoris, annotations photo cliquables, sous-routines composables, suppression/modification de liens, zoom animé, recherche améliorée.
- Signature stable APK/AAB de distribution ; absence de connexions entre personnes maintenue.

## Prochain geste
Examiner le premier run Actions et corriger ce qui bloque avec les quatre mémoires dans le même commit. Si Actions est bloqué par minutes/autorisations, une Release n'y remédie pas.

## Run #2
Run 35662052714 : échec SDK manager absent du PATH. Localisation explicite du binaire ajoutée ; prochain run et liens de release à contrôler. Aucun APK disponible au moment de ce constat.

## Livraison CI vérifiée
- Run #3 : https://github.com/greenpower2669/FabMap/actions/runs/35662113704 — **success**, compilation APK et création GitHub Release réussies.
- Pré-release : https://github.com/greenpower2669/FabMap/releases/tag/v0.1.0-b3
- APK direct : https://github.com/greenpower2669/FabMap/releases/download/v0.1.0-b3/FabMap-v0.1.0-b3-debug.apk
- Taille GitHub confirmée : 27059 octets ; aucune installation ni essai utilisateur sur téléphone encore effectués.
- Prochaine action humaine : exporter l'ancienne sauvegarde avant changement de build signée debug, installer et tester les gestes principaux.

## Itération 0.2.0 — paquets locaux (en cours)
- Code : export de la bulle active et des descendants ; import sous la bulle active sans effacement ; pièces jointes photos, sous-procédures partagées et UUID remappés. Sauvegarde complète v0.1 conservée.
- À confirmer : sens exact du message vocal « sauvegarde ou dl de probables » (interprété procédures/bulles).
- À vérifier immédiatement : compilation Actions déclenchée par le code, logs, lien APK direct de nouvelle pré-release ; corriger dans le même cycle avec quatre mémoires si échec.
- Tests smartphone : exporter une procédure avec deux niveaux, importer deux fois sous parents distincts, dézoom, photos, import refusé si archive invalide, sauvegarde complète avant désinstallation.

## Livraison 0.2.0 vérifiée
- Run #4 : https://github.com/greenpower2669/FabMap/actions/runs/35665143325 — compilation et publication réussies.
- Pré-release : https://github.com/greenpower2669/FabMap/releases/tag/v0.2.0-b4
- APK direct : https://github.com/greenpower2669/FabMap/releases/download/v0.2.0-b4/FabMap-v0.2.0-b4-debug.apk (33 011 octets d'après GitHub).
- Tests sur appareil non effectués. Vérifier export/import, photo, cycles et espace insuffisant. Avant toute désinstallation de v0.1.0, EXPORTER LA MÉMOIRE ENTIÈRE : signature debug éphémère.

## Itération 0.3.0 — à vérifier
- Développé : bouton vert « C'EST FAIT ! » de 110dp, long press consultatif sur bulles et boutons, aperçu persistant centré, lecture automatique, Réécouter, Fermer.
- Après commit : vérifier compilation, pré-release et lien direct d'APK sans Artifacts ; consigner commit et run vérifiés.
- Test smartphone : appui long n'exécute pas OnClick et ne change pas l'étape ; fermeture sans zoom ; écran petit/fontScale 200 %, TalkBack et TTS interrompue à fermeture.
- Avant toute désinstallation de l'ancienne build debug, export de la mémoire complète indispensable.

## Livraison 0.3.0 vérifiée
- Run #5 : https://github.com/greenpower2669/FabMap/actions/runs/35694822501 — compilation et publication GitHub Release réussies.
- Pré-release https://github.com/greenpower2669/FabMap/releases/tag/v0.3.0-b5 ; APK direct https://github.com/greenpower2669/FabMap/releases/download/v0.3.0-b5/FabMap-v0.3.0-b5-debug.apk (34 723 octets selon GitHub).
- Aucun test Android réel du geste long effectué ; tests smartphone/TalkBack, sauvegarde avant désinstallation et compatibilité signature restent à vérifier.

## 0.4.0 — Vallée des bulles
- Développé : carte 2D à trame finie de bulles existantes, couleurs de branche, liens réels, drag/pinch, zoom +/- et recentrage, sélection vers fiche guidée, appui long vocal B, plafond 160 de rendu.
- À vérifier après push : CI :app:assembleDebug, publication et URL APK directe, pas d'artefact Actions ; consigner résultats vérifiés dans les mémoires.
- À tester sur téléphone : limite du déplacement sans vide infini, gros graphes, nœuds cycliques, liens partagés, gestes et appui long, retour guidé, grande police, TalkBack ; sauvegarder la mémoire avant toute désinstallation de la build debug.
- Non réalisé : vrais embeddings/proximité sémantique d'IA, icônes spécifiques pour chaque thème, rendu 3D. Aucun code/asset massif dans l'APK.

### CI 0.4.0 après run #6
- Run #6 échec javac sur Listener final dans l'initialiseur de champ ; correctif appliqué avec les quatre mémoires. Nouveau run à vérifier ; aucune APK 0.4.0 publiée à cette étape.

### Livraison 0.4.0 vérifiée
- Run #7 : https://github.com/greenpower2669/FabMap/actions/runs/35748911214 — success, compilation APK et création GitHub Release.
- Page https://github.com/greenpower2669/FabMap/releases/tag/v0.4.0-b7 ; APK direct https://github.com/greenpower2669/FabMap/releases/download/v0.4.0-b7/FabMap-v0.4.0-b7-debug.apk (43 775 octets).
- Reste impératif : test humain sur téléphone, fluidité/pan, lisibilité et geste vocal B, sauvegarde complète avant toute désinstallation de build debug antérieure.

## 2026-09-22 — préparation demandée : planches, bulles, vraie TV (AUCUN CODE)
- [x] Consigner les sept planches et leurs 56 emplacements : thèmes, actions, états, navigation, procédure, bulles, repérage, dans l'ordre provisoire de création ; garder l'ordre définitif à organiser avec Fab.
- [x] Consigner le parcours TV réel avec les deux branches conditionnelles et l'attente sans durée inventée ; noter que le scénario SOURCE de démonstration reste à remplacer prudemment.
- [x] Décrire le futur catalogue de bulles simples : 7 bulles de famille et une association à chacune des 56 icônes au moins une fois ; réutiliser les mêmes bulles pour notions partagées, sans bulles dans le vide.
- [ ] **Relais Fab :** téléverser lui-même les sept planches PNG sur la branche GitHub `main`, emplacement proposé `assets/icons/boards/`, avec `01-themes.png`, `02-actions.png`, `03-etats.png`, `04-navigation.png`, `05-procedure.png`, `06-bulles.png`, `07-reperage.png`. Si noms ou répertoire différents, attendre ses chemins exacts plutôt que déplacer à l'aveugle.
- [ ] Une fois son upload terminé : vérifier les sept PNG et leur ordre effectif dans Git, puis lui demander accord pour découpage/optimisation/intégration des icônes et création effective des bulles. Garder les masters ; aucune grosse image Base64 ; ne pas publier d'APK pour la mise à jour documentaire seule.
- [ ] Prévoir après feu vert une procédure « Allumer la télévision » importable ou migrée sans effacer les mémoires existantes ; vérifier avec Fab comment trouver HDMI3 avant d'inventer toute touche ou interaction.
- [ ] Tests ultérieurs : appui long B, accessibilité des icônes et textes, tailles APK, temps de rendu de la Vallée, liens partagés, procédure TV sur l'appareil réel.

## 0.5.0 — icônes et autonomie utilisateur
- [x] Classer les trois premiers PNG de Fab en blobs originaux sous assets/icons/boards/ ; ajouter l'extracteur JDK pour ne packager que des PNG 144px.
- [x] Pense-bête indexé des sept planches réellement dessinées dans brain.md, distinction images présentes/à téléverser et concepts éventuellement répétés.
- [x] Ajouter le choix d'icônes des planches présentes, ajout d'icône utilisateur, retrait, stockage JPEG privé, rendu guidé + Vallée.
- [x] Export/import branche avec icônes custom et références remappées ; sauvegarde complète media/ avec validation d'icônes ; anciens ZIP toujours admissibles.
- [x] Catalogue volontaire de sept familles / 56 bulles et ajout distinct de la procédure TV réelle sous la télé de démonstration, sans écraser les cartes personnelles.
- [ ] Vérifier CI Java extracteur et APK Release, réparer tout échec avec les quatre mémoires dans le même commit.
- [ ] Fab upload en binaire les planches 04–07 dans assets/icons/boards/ avec noms conventionnels ; vérifier les dimensions/cadrage réel et ajuster l'extracteur sans perdre les masters.
- [ ] Tests sur vrai téléphone : fluidité Canvas, lisibilité des icônes 44/86 dp, import perso et sauvegarde complète, export/import branche d'une même icône utilisée deux fois, ZIP v0.4, dialogue retour/validation, simulation de stockage insuffisant.

### Livraison 0.5.0 — vérification de production technique
- [x] Run #8 https://github.com/greenpower2669/FabMap/actions/runs/35764510346 : 24 sprites extraits, :app:assembleDebug et publication GitHub Release réussis.
- [x] https://github.com/greenpower2669/FabMap/releases/tag/v0.5.0-b8 ; APK direct https://github.com/greenpower2669/FabMap/releases/download/v0.5.0-b8/FabMap-v0.5.0-b8-debug.apk, 742367 octets.
- [ ] Tests réels de sélection et ajout d'icône, sauvegarde/import d'une branche et récupération complète, rendu Canvas et ancienne mémoire ; 04–07 attendent upload de Fab.

## 0.5.1 — corriger TV + titres des icônes
- [x] Supprimer bouton/méthode « Installer la procédure TV réelle », intégrer branche ON/OFF → observer → HDMI3 **ou** ON/OFF TV selon l'écran dans la démonstration.
- [x] Migration non destructive de la démonstration précédente lorsque contenu source inchangé ; réutiliser l'ancienne branche installée sans créer un doublon ; garder export complet et icônes personnalisées.
- [x] Bandeaux contrastés + ombres séparant titres et images dans la Vallée et les tuiles du mode guidé.
- [ ] Vérifier javac/Gradle, CI, Release et lien APK direct ; consigner preuves puis tester migration ancienne mémoire, présence d'un ancien `fabmap-tv-real`, TV personnalisée, contraste/zoom, appui long et sauvegarde sur téléphone.
