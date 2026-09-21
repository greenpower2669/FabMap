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
