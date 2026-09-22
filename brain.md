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

## Préparation suivante — planches d'icônes et procédure TV réelle (décision de Fab, 2026-09-22)
**Mode de travail demandé : documentation uniquement maintenant.** Ne pas coder, ne pas modifier les données initiales, ne pas déplacer ni téléverser les planches à la place de Fab. Fab préfère téléverser lui-même les PNG binaires sur la branche `main` ; attendre son signal et contrôler les chemins du dépôt avant toute intégration.

**Inventaire à préserver : sept planches conceptuelles produites dans la conversation, chacune avec huit pictogrammes légendés (56 emplacements visuels, notions récurrentes possibles).** Dans l'ordre provisoire de création : 01 Thèmes (Maison, Grande pièce, Télévision, Télécommande, Cuisine, Santé, Famille, Musique) ; 02 Actions (Écouter, Rechercher, Sauvegarder, Télécharger, Importer, Modifier, Photo, Recentrer) ; 03 États (Validé, Favori, Important, À écouter, Partagée, Sous-bulles, En cours, Terminé) ; 04 Navigation (Accueil, Retour, Ouvrir, Fermer, Zoomer, Dézoomer, Aide, Réécouter) ; 05 Procédure (Étape, Consigne, Liste, Ordre, Minuteur, Refaire, Cocher, Terminer) ; 06 Bulles (Bulle, Bulle mère, Sous-bulle, Bulle liée, Bulle favorite, Bulle photo, Bulle audio, Bulle procédure) ; 07 Repérage (Favori, Important, À voir, À faire, À écouter, En cours, Urgent, Souvenir). **L'ordre définitif reste à organiser avec Fab** ; ne pas confondre l'inventaire de planches avec des ressources d'APK déjà intégrées.

**Bulles de démonstration à créer plus tard, pas dans ce commit :** un catalogue « Icônes FabMap » avec une bulle simple pour chacun des sept thèmes de planche et au moins un lien vers une bulle réelle par chacun des 56 emplacements d'icône, sans recopier une même notion en plusieurs nœuds lorsqu'elle est réutilisable. Les bulles peuvent porter titre + icône + lecture vocale/aperçu B ; elles doivent rester consultables dans le mode guidé comme dans la Vallée. Ce sont des bulles réelles créées volontairement dans la mémoire, jamais des bulles inventées pour combler l'espace vide de la carte.

**Procédure réelle « Allumer la télévision » donnée par Fab (à implémenter après son feu vert ; supplante la démonstration SOURCE/chaînes fictive actuelle) :**
1. Allumer la TV avec la télécommande, au moyen du bouton ON/OFF indiqué dessus.
2. Si l'écran arrive sur la vue des chaînes payantes et autres, chercher/sélectionner **HDMI3**.
3. Si l'écran est noir, reprendre la télécommande TV, celle dont le bouton ON/OFF porte « TV » à côté, puis appuyer sur ce bouton.
4. Attendre l'affichage : il peut parfois être long.
Ces chemins sont **conditionnels** ; ne pas présenter HDMI3 et le second appui ON/OFF comme deux gestes obligatoires l'un après l'autre. Fab n'a pas précisé le détail des touches pour accéder à HDMI3 ; ne pas inventer de touche SOURCE, de branche supplémentaire ou de délai chiffré. Conserver strictement ses termes avant essai sur sa télévision.

## 0.5.0 — PENSE-BÊTE CONTRACTUEL DES ICÔNES (relire avant toute modification)
Une icône accompagne toujours un titre et ne le remplace jamais ; l'appui long vocal B et le mode guidé restent disponibles. Une icône est une association à une bulle **existante**, jamais une bulle inventée dans une zone vide. Un même nom conceptuel peut être relié depuis plusieurs planches : ne pas fabriquer des significations contradictoires ; les bulles peuvent être liées sans dupliquer leur contenu utilisateur. Paires import/export : la sauvegarde complète inclut toutes les images personnelles stockées en media ; exporter une branche inclut uniquement les images personnelles utilisées dans ses descendants, sans Base64 et sans recopier les icônes built-in livrées avec l'application.
**Ordre canonique, indexes 0–7 des planches 01–07 :**
01 Thèmes : Maison ; Grande pièce ; Télévision ; Télécommande ; Cuisine ; Santé ; Famille ; Musique.
02 Actions : Écouter ; Rechercher ; Sauvegarder ; Télécharger ; Importer ; Modifier ; Photo ; Recentrer.
03 États : Validé ; Favori ; Important ; À écouter ; Partagée ; Sous-bulles ; En cours ; Terminé.
04 Navigation : Retour ; Accueil ; Zoomer ; Dézoomer ; Explorer ; Déplacer ; Ouvrir ; Fermer.
05 Procédure : Étape ; Choix ; Attendre ; Appuyer ; Allumer ; HDMI3 ; Télécommande TV ; C'est fait.
06 Bulles : Bulle simple ; Bulle parent ; Bulle fille ; Bulle liée ; Bulle photo ; Bulle procédure ; Bulle parlante ; Bulle favorite.
07 Repérage : Ici ; À gauche ; À droite ; En haut ; En bas ; À côté ; Devant ; Derrière.
Cette liste représente **les nouvelles planches complémentaires effectivement montrées** ; la première liste préparatoire pour 04–07 est désormais remplacée par ce contenu observé. Plusieurs concepts sont répétables, la source et l'index sont déterminants. **Fichiers maîtres réellement observés dans Git avant ce cycle :** trois PNG opaques à la racine, identifiés par leur correspondance de génération (01 a778..., 02 9080..., 03 6f648...). Ils sont copiés sous `assets/icons/boards/01-themes.png`, `02-actions.png`, `03-etats.png`; les quatre autres ne sont PAS téléversés dans le dépôt. Fab garde le contrôle des uploads des quatre autres sur main. Les fichiers source ne sont pas redessinés/encodés en Base64.
**Vraie procédure TV** : ON/OFF de la télécommande ; si vue chaînes payantes/autres → HDMI3 ; si écran noir → télécommande TV ON/OFF avec TV à côté, puis attendre, parfois long. Les branches ne sont pas séquentielles, ne pas inventer de touche SOURCE ni de durée.

### Livraison 0.5.0 vérifiée
Run #8 a produit une APK de test contenant les petites icônes des trois planches reçues ; pas de test humain sur téléphone, pas de planches 04–07 dans Git. Les données personnalisées doivent être sauvegardées avant changement de signature debug.

## 0.5.1 — RECTIFICATION EXPLICITE DE FAB (prioritaire)
- La demande « procédure TV réelle » n'était PAS une fonction à installer par l'utilisateur : remplacer **la démonstration TV intégrée**. Retirer le bouton « Installer la procédure TV réelle » et sa méthode. Les anciennes notes parlant d'une installation volontaire TV ne décrivent plus le contrat actuel.
- Ordre : allumer avec ON/OFF de la télécommande ; **observer**, puis choisir uniquement la branche pertinente : si chaînes payantes/autres → rechercher HDMI3 ; si écran noir → reprendre la télécommande TV portant « TV » à côté du ON/OFF, appuyer, attendre l'affichage parfois long. Pas de SOURCE imaginé ni de délai chiffré.
- Protéger les mémoires déjà créées : migration ciblée des libellés/étapes de la démonstration inchangée uniquement, sans écraser les contributions. Si la branche ajoutée précédemment existe, la réutiliser plutôt que créer une seconde « vraie TV ».
- Visuel : le titre doit se **détacher réellement de l'icône** dans la Vallée et en navigation guidée : placement distinct, cartouche bleu nuit, typographie blanche et ombre ; pas de caractères posés directement sur le pictogramme, ni dépendance à la couleur seule. Appui long vocal B conservé.
