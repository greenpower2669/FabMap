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

## 0.3.0 — décision de Fab après maquette
- Appui court = action habituelle (bulle : zoom ; bouton : fonction).
- Appui long **B** = aperçu texte grand, centré, lecture vocale automatique, qui reste après relâchement ; fermeture explicite, bouton Réécouter ; aucune action normale déclenchée par appui long.
- Toutes les bulles et les boutons interactifs, y compris le bouton de validation, sont concernés.
- Bouton « C'EST FAIT ! » vert, plus grand, texte clair et grande cible tactile ; sa lecture explique la validation mais ne l'effectue pas.
- Aucun code de connexion entre personnes ; ne pas modifier la sauvegarde/import au cours de ce changement.

## 0.4.0 — décision de Fab : la Vallée des bulles
- La carte 2D existe uniquement là où il y a des bulles et liens enregistrés, à la manière d'une trame d'espace-temps créée par son contenu. Pas de grille navigable infinie, pas de bulle inventée ou créée sur le vide.
- Exploration spatiale bornée, glisser, pincer, zoom +/- et recentrer, groupes colorés par branche ; mode guidé intégralement conservé. Appui long B (agrandissement + voix) disponible sur les bulles cartographiées.
- Prototype sans moteur 3D, modèle d'IA ou corpus vectoriel embarqué : proximité fondée sur les relations existantes et non sur de véritables embeddings sémantiques. Objectif central : APK petit et pas de lag.
- Limite volontaire de la carte à 160 bulles affichées, sans suppression de mémoire ; le mode guidé ne subit pas cette limite.

### État de compilation 0.4.0
Premier build échoué dans Java (initialisation d'un Listener dans un Runnable de champ), correctif technique sans changement du contrat fonctionnel ; la livraison dépend de la validation du prochain run.

### Livraison 0.4.0
La compilation Android et la Release du prototype Vallée des bulles v0.4.0-b7 ont été vérifiées sur CI ; les gestes et les performances sur téléphone restent non testés.
