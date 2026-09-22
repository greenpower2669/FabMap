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
