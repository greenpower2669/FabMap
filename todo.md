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
