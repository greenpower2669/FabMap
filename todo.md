# ✅ FabMap — état vivant

## Implémenté dans ce premier commit, vérification à faire
- Squelette Android et icône vectorielle FabMap.
- Navigation zoom/dézoom, bulles partagées, création, liens, recherche, lecture vocale, photos, procédures simples, sauvegarde ZIP.
- FAB Copilot : brain, brainmap, debughistorical et todo cohérents avec ce prototype.
- GitHub Actions sur push + lancement manuel, publication de l'APK directement en pré-release GitHub sans upload-artifact.

## À confirmer (ne pas déclarer prêt avant les preuves)
- Commit sur main, déclenchement et réussite du run Actions, Release et lien APK.
- Essai sur téléphone : affichage, voix, sauvegarde/restauration et photos. Signatures debug : exporter avant réinstallation.
- Améliorer erreurs de restauration et gestion des médias volumineux selon tests.
- Ajout favoris, annotations photo cliquables, sous-routines composables, suppression/modification de liens, zoom animé, recherche améliorée.
- Signature stable APK/AAB de distribution ; absence de connexions entre personnes maintenue.

## Prochain geste
Examiner le premier run Actions et corriger ce qui bloque avec les quatre mémoires dans le même commit. Si Actions est bloqué par minutes/autorisations, une Release n'y remédie pas.
