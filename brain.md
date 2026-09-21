# 🧠 FabMap — contrat vivant

## Décisions confirmées par Fab
- Mémoire personnelle en bulles : zoom dans une fille, dézoom vers le parent emprunté ; fille dans fille sans profondeur imposée.
- Une même bulle peut avoir plusieurs chemins d'accès, sans duplication ; retour par historique réel.
- Beaucoup de données en arrière-plan, peu de choix visibles ; renommer les lieux selon les mots de la personne.
- Contenus évolutifs : photos, explications, procédures, voix, recherche et sauvegarde ; première utilité : télévision.
- Pas de connexions entre personnes au démarrage : pas de compte familial, messagerie, SOS réseau, synchronisation ou accès distant.
- Suivre FAB Copilot et quatre mémoires synchronisées avec le code.
- Compiler les APK et les publier directement en Releases ; éviter les artefacts Actions bloquant le quota rapporté.

## Périmètre prototype 0.1.0, non encore validé humainement
- Android natif local, démonstration Maison → Grande pièce → Télévision.
- Bulles ajoutables et renommables, liens de bulles déjà créées, retour, accueil et recherche par mots.
- TextToSpeech, étapes, choix/prendre photo, sauvegarde/restauration ZIP contenant JSON et JPG.
- Un lien web pour ouvrir ChatGPT, sans dépendance de l'app au compte ou à l'IA.
- Édition séparée de l'usage ordinaire par bouton « Modifier / créer ».
- Non inclus : annotation fine de photo, moteur IA embarqué, édition de relations existantes, partage ou prise en main.
- Build debug de test uniquement, APK renommée/versionnée ; AAB différé tant que signature stable absente.

## Règle de vérité
Ne pas annoncer d'APK avant compilation et Release vérifiées. La sauvegarde est indispensable avant désinstallation d'une APK signée debug par un autre runner.

## CI — correction après premier run
Le runner Ubuntu dispose déjà de sdkmanager ; éviter l'action setup-android v3 qui recherche le paquet obsolète 'tools'. Installer directement platform 35 et build-tools 35.0.0.

## CI — correction #2
Sans setup-android, sdkmanager n'est pas dans le PATH. Trouver le binaire via le répertoire SDK préinstallé et définir ANDROID_HOME/ANDROID_SDK_ROOT explicitement.

## Itération 0.2.0 — export/import de bulles (demande vocale à confirmer)
Interprétation de « sauvegarde ou DL de probables » : sauvegarder/télécharger des bulles **et procédures** individuellement, en complément du ZIP de mémoire complète. Chaque paquet englobe tous ses descendants accessibles, étapes et photos ; import sous la bulle choisie **sans remplacer** la mémoire. Pas de réseau ni d'accès familial. Confirmation de la formulation à demander à Fab sans empêcher la réalisation de ce besoin compatible avec le concept.
