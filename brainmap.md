# 🗺️ FabMap — architecture vivante

## Cartographie des fichiers
- `app/src/main/java/fr/fabmap/app/MainActivity.java` : graphe JSON local, navigation historique, rendu des bulles, mode modification, étapes, synthèse vocale, appareils photo/sélecteur, import-export d'archive.
- `app/src/main/AndroidManifest.xml` : activité unique, aucune permission réseau ou familiale.
- `app/src/main/res/drawable/fabmap_icon.xml` : icône vectorielle légère.
- `app/build.gradle` : versionCode 100000 + numéro de run, versionName 0.1.0-bN.
- `.github/workflows/android-release.yml` : push Android ou lancement manuel → JDK 17 / SDK35 / Gradle 8.10.2 → assembleDebug → gh release create en pré-release.
- `README.md` : comportement et procédure de test.

## Structure des données et relations
`files/map.json` possède `{schema:1,nodes:{id:{id,title,description,children:[id],steps:[texte],photo:"nom.jpg",url?}}}`.
Le titre est modifiable ; l'identifiant permanent n'est pas le titre. Plusieurs `children` peuvent référencer le même id. Aucune copie du nœud partagé. `path` en MainActivity garde les visites et produit le dézoom exact ; rechercher ajoute aussi une visite et le retour retrouve la précédente. Les cycles sont possibles : l'historique revient toujours à l'étape précédente, pas à un parent arbitraire. Affichage initial quatre filles, bouton « voir autres ».

`media/UUID.jpg` contient les images référencées ; caméra Android ACTION_IMAGE_CAPTURE en miniature ou sélection ACTION_OPEN_DOCUMENT. Archive .fabmap = ZIP contenant map.json et media/ ; transfert binaire sans Base64. Import dans répertoire de staging, vérifie schéma, références, noms et limites de taille avant adoption. Remplacement de données locales ; attention, préserver une sauvegarde antérieure.

## Risques et dépendances
- Synthèse vocale : dépend de la voix installée ; ne pas imposer un serveur.
- Ouverture web par ACTION_VIEW : ne pas supposer sessions Chrome partagées avec WebView.
- Signature debug sur runner non durable : APK de test peut exiger désinstallation avant réinstallation ; signature stable et AAB seront une étape à part.
- Workflow sans upload-artifact : supprime ce stockage pour livraison ; minutes Actions et permissions de publication restent nécessaires.
- Validation sur téléphone et compilation réelles restent à confirmer, ne pas déduire leur réussite de la présence du code.

## Correctif observé du pipeline
Premier run : setup-android@v3 a échoué sur 'Failed to find package tools' avant Gradle. Retirer cette action et utiliser le sdkmanager déjà présent sur le runner, puis vérifier compilation/release.

## Diagnostic run #2
Le runner contient un SDK à /usr/local/lib/android/sdk d'après run #1, mais la commande sdkmanager n'est pas dans PATH sans setup-android ; utiliser find sur cmdline-tools et sdk_root explicite ; vérifier que platforms/android-35 et build-tools/35.0.0 sont présents avant l'installation.

## État de livraison vérifié
Le run #3 du commit b6e849c5aba3bd20322a538203deb6ff91632363 a compilé :app:assembleDebug puis publié la pré-release v0.1.0-b3 (APK de 27059 octets). Il valide le canal sans upload-artifact ; il ne valide pas l'usage sur téléphone ni la signature d'une future mise à jour.

## V0.2.0 — paquets locaux de bulles/procédures
- `BubblePacks.java` : export depuis identifiant racine, parcours itératif avec ensemble visité (cycles/partages), paquet ZIP `bubble.json` + `media/*.jpg` ; limites 400 nœuds, 500 entrées, 5 Mo descriptif, 16 Mo/photo, 100 Mo total. Export et import hors ligne via ContentResolver/SAF, aucun Base64.
- `MainActivity.java` : requêtes 105/106, saisie des identifiants de contexte avant sélecteur Android et conservation dans instance state. Boutons « Télécharger cette bulle et ses filles » / « Importer des bulles ici ». Import sans remplacement : clone des JSON existants, nouveaux UUID pour tous les nœuds, liens internes remappés, médias copiés sous noms distincts, racine ajoutée dans les enfants du parent cible. Ancienne restauration complète inchangée.
- Vérification de schéma/kind, références fermées, chemins whitelistés, duplications et tailles avant mutation de graphe. Échec d'archive ne remplace pas le JSON existant ; les données locales restent privées.
- Limites connues : copie/ZIP effectués sur thread UI (gros paquet peut figer l'écran), sauvegarde globale préexistante distincte et remplaçante, robustesse d'erreur d'écriture après validation à renforcer, interface de restauration non transactionnelle pour panne de stockage ; tests téléphone indispensables.

## CI 0.2.0
Le run 35665143325 compile le nouveau `BubblePacks.java` avec MainActivity et publie la pré-release v0.2.0-b4 ; la compilation ne constitue pas un test d'import/export réel sur appareil.

## 0.3.0 — affordances voix et grossissement
- MainActivity.button(...) assure désormais une cible >=64dp, ou >=110dp et verte pour « C'EST FAIT ! ». Tap exécute la callback d'origine ; long press consumé par OnLongClickListener.
- MainActivity.previewOnLongPress(...) déclenche retour haptique Android puis showPreview(...). Celui-ci crée un AlertDialog à contenu ScrollView, titre 32sp et explication 24sp centrés ; fermeture explicite, écoute auto et bouton Réécouter ; fermer stoppe TTS.
- Les bulles ont leur OnClick zoom d'origine ET leur appui long consultatif, sans mutation de path, step, edit ni données.
- La section des étapes appelle button(..., true) pour la validation verte. Les autres boutons passent tous par button(...) pour harmoniser l'appui long.
- Limites de validation : comportement TalkBack et ergonomie petit écran à essayer en main réelle ; aucun test instrumenté d'accessibilité encore.

## État CI 0.3.0
Le run #5 (35694822501) du commit ff16e57a88608145aeacaa3b5b10ce80084d10fe a compilé le nouveau comportement et publié v0.3.0-b5 en Release directe, sans upload-artifact.

## 0.4.0 — carte spatiale finie
- BubbleValleyView.java : Android Canvas sans dépendance externe ; BFS des nœuds réellement accessibles depuis la bulle actuelle, identifiants uniques et arcs reconstitués après BFS pour liens partagés/cycliques. Plafond 160 pour la carte seulement.
- Position des enfants proche du parent, angle stable dérivé de l'ID, couleurs héritées de la première branche, détection de collision de la nouvelle bulle, jamais de simulation physique animée ni de nœud inventé. Ce positionnement n'est PAS une base vectorielle IA.
- Trame = halos autour des seules bulles existantes et traits entre vrais liens. Bornes issues des positions des bulles ; clamp du pan sur x/y, zoom .35 à 3, reset, culling de dessin hors champ, labels omis à zoom extrême.
- OnTouchEvent + ScaleGestureDetector : pan / pinch, appui long consumé via Handler et preview vocale B, tap sur bulle ouvre la fiche guidée existante ; le vide n'a aucune action créatrice. Boutons zoom et mode guidé accessibles hors Canvas.
- MainActivity.java : flag valley conservé après rotation, entrée depuis chaque fiche ; vue Vallée remplace temporairement le contenu guidé sans changer le JSON. Les paquets et sauvegardes restent identiques.
- Limites connues : positions calculées sur thread UI à ouverture, plafond 160 ; Canvas ne fournit pas un objet TalkBack individuel pour chaque bulle, donc préserver le parcours guidé. Aucun asset volumineux ajouté.

### Dépendance d'initialisation réparée
Le Runnable d'appui long défini comme champ accède à listener assigné dans le constructeur de BubbleValleyView. La déclaration de Listener n'est plus blank final, pour satisfaire l'analyse d'affectation de javac tout en gardant l'attribution avant toute interaction utilisateur.

### Livraison 0.4.0 validée côté compilation
Run #7 35748911214 du commit d47fe34a56efbf833b441484d3cd318936d9b5fb : assembleDebug et publication en Release directe réussis (43 775 octets). L'ergonomie Canvas reste à vérifier sur appareil.

## Préparation d'intégration — planches et nœuds (pas de code ni assets dans ce commit)
- **Source des sept planches dans la conversation** : 01 thèmes, 02 actions, 03 états, 04 navigation, 05 procédure, 06 bulles, 07 repérage ; chacune contient 8 pictogrammes avec libellés français, soit 56 associations icône→notion à inventorier. Les trois premières planches ont précédé les quatre autres ; l'ordre final de présentation demeure ouvert.
- **Destination d'upload proposée sur `main` par Fab** : `assets/icons/boards/` avec sept PNG complets nommés de façon stable : `01-themes.png`, `02-actions.png`, `03-etats.png`, `04-navigation.png`, `05-procedure.png`, `06-bulles.png`, `07-reperage.png`. Proposition de chemin/nom, **pas un fait accompli** : si Fab utilise d'autres noms, les relever depuis Git et mettre les quatre mémoires à jour. Ces planches sources hors `app/` n'entrent pas d'office dans l'APK.
- **Future transformation autorisée après upload et demande d'intégration** : vérifier les sept fichiers binaires réels, dimensions/qualité/ordre, découper proprement les icônes si nécessaire en petits fichiers individuels puis convertir/adapter pour Android avec budget de taille ; pas de Base64 lourd, pas de chargement de sept planches entières par bulle, pas de promesse de vecteur natif à partir d'une image raster. Garder les masters intacts.
- **Modèle de bulles préparatoire** : « Icônes FabMap » → sept bulles catégories nommées d'après les planches → une relation au moins par icône vers une bulle simple réutilisable. Les concepts identiques figurant sur plusieurs planches doivent partager l'ID du nœud (ex. Favori, Important, À écouter, En cours), sans duplication d'information ; chaque usage garde son association propre à la planche. La hiérarchie réelle, pas une carte remplie artificiellement, définit l'espace Vallée.
- **TV — changement futur localisé** : dans `MainActivity.load()`, les nœuds de démonstration `tele`, `telecommande`, `source`, `chaines` et la liste `steps` de `chaines` utilisent actuellement un scénario SOURCE générique. Prévoir nœud/procédure « Allumer la télévision » avec étapes et alternatives conditionnelles « vue chaînes payantes → HDMI3 » / « écran noir → ON/OFF avec TV à côté → attendre », sous réserve de préserver les cartes personnelles déjà stockées : `load()` ne crée le scénario que pour une installation sans `map.json`. Concevoir une migration non destructive ou une procédure importable ; ne pas supposer qu'un changement de démonstration modifie les mémoires existantes.

## 0.5.0 — Pipeline des icônes et persistance
- Trois blobs Git fournis par Fab déplacés sans conversion sous `assets/icons/boards/{01-themes,02-actions,03-etats}.png` sur main ; source des quatre autres planches absente (upload humain ultérieur). `tools/ExtractIconBoards.java` utilise ImageIO du JDK 17 pendant le CI (après checkout, avant Gradle) et génère huit PNG 144px pour chaque board présent vers `app/src/main/assets/icons/default/NN-II.png` : APK n'embarque pas les grandes planches masters, aucune Pillow/NDK/lib externe ni Base64.
- `IconAssets.java` : table déterministe des sept planches / 56 libellés, résolution par `icon: built:NN-II` pour les PNG embarqués ou `icon: custom:UUID.jpg` pour media/ privé. LruCache de mini-bitmap, sélecteur accessible aux PNG effectivement produits. Import personnalisé via SAF, limite 8 Mo source, dimensions <=10000, décodage échantillonné, aplatissement blanc, JPEG normalisé 256px à qualité 86 ; absence d'une icône par défaut → texte seul et aucune erreur.
- `MainActivity.java` : mode Édition, sélection icône préinstallée / ajout d'image / retrait ; rendu en fiche, vignette de bulle et Canvas Vallée ; bouton sur Accueil pour installer explicitement le catalogue « Icônes FabMap » avec sept groupes et 56 bulles, sans écraser les bulles préexistantes, et bouton pour procédure TV réelle distincte à branches sous `tele`. Les groupes et bulles de planches non disponibles existent comme texte sans image jusqu'à l'upload et reconstruction.
- `BubblePacks.java` : export de sous-graphe collecte `icon=custom:*.jpg` comme media (et les photos), valide leurs fichiers, importe par remappage UUID des images et refs ; les built-in demeurent identifiants, non copiés. `MainActivity.exportTo/importFrom` sauvegarde complète : media/*.jpg contient déjà photos et JPEG d'icônes, ajoute validation des références personnalisées à la restauration. Compatibilité rétroactive : anciennes bulles sans propriété icon restent lisibles.

### Preuves CI 0.5.0
Run 35764510346 sur 0927b28c36e2833f253c7bd8b51bdfbf4921504b : extracteur 24 petites icônes, assembleDebug réussi, Release v0.5.0-b8 créée, APK 742367 octets. Le dépôt contient uniquement assets/icons/boards/01-themes.png, 02-actions.png, 03-etats.png comme planches masters, et aucun PNG anonyme à la racine.

## 0.5.1 — correction architecture TV et étiquettes
- Nouveau `TvDemo.java` : génération du parcours conditionnel dès la création d'une mémoire, migration uniquement des nœuds ayant les titres/explications/étapes exacts de l'ancien exemple TV. Préserve JSON utilisateur ; si `fabmap-tv-real` existe (ancien installateur), le réutilise comme branche d'entrée afin d'éviter un doublon visible. Pas de commande d'installation TV. Les anciens identifiants `source` et `chaines` peuvent rester dans le JSON pour protéger d'autres liens, mais leur contenu par défaut reconnu est actualisé et seuls les anciens liens de navigation de démonstration sont retirés.
- `MainActivity.load()` appelle `TvDemo.refresh(nodes)` après lecture et dans le scénario vierge. Le bouton et la méthode `installTvProcedure()` sont supprimés ; les fonctions de sauvegarde et d'icônes ne sont pas modifiées.
- `MainActivity.show()` : une tuile verticale par enfant : icône dédiée au-dessus, texte dans son propre bandeau bleu nuit à lettres blanches avec ombre et élévation ; click/long click sur toute la tuile, chemin préservé.
- `BubbleValleyView.onDraw()` : image dans la partie supérieure, titre dessiné dans un cartouche arrondi sombre dans la partie basse avec ombre, séparé du pictogramme ; pas de nouveau bitmap géant ni nouvelle dépendance.

### Preuve CI 0.5.1
Run 35769861235 du commit a39c9a308b198e75805fe3fa189d1022602e9697 : sprites générés, assembleDebug et publication directe réussis ; pré-release v0.5.1-b9 avec APK 743747 octets. Aucun test tactile ni migration instrumentée démontré par CI.

## 0.5.2 — Chaîne de transparence et stylisation
- `tools/ExtractIconBoards.java` : masque circulaire à alpha progressif appliqué aux *sprites 144px uniquement* après découpe des planches maîtresses opaques ; les quatre coins deviennent transparents. Les masters restent intacts sous assets/icons/boards ; l'alpha est conservé dans les sprites PNG embarqués.
- `IconAssets.importCustom()` : bitmap ARGB_8888 vierge transparent, mise à l'échelle dans 256px et compression PNG ; références `custom:UUID.png`. `custom:UUID.jpg` d'archives et d'installations antérieures reste reconnu/affiché.
- `MainActivity.show()` : iconBadge ronde semi-transparente et titre indépendant à Shader LinearGradient bicolore bleu + ombre légère sans cartouche plein ; titre/interactions de la tuile et lecture B préservés. `BubbleValleyView.onDraw()` : verre rond translucide derrière l'icône, titre clair bi-teinte avec ombre mais sans fond opaque ; paints restaurés après dessin.
- `BubblePacks.java` : paquets de branches acceptent `media/*.png` et `media/*.jpg`, remappent les images référencées en conservant l'extension ; la sauvegarde complète et sa restauration acceptent ces deux formats. JSON anciens et vieux JPEG compatibles.
