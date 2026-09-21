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
