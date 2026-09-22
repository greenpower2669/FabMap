# 🐞 FabMap — historique des risques et vérifications

## 2026-09 — lancement
- Fab rapporte un quota GitHub gênant sur la livraison des APK par artefacts Actions. Cause/quota précis non vérifiés.
- Contournement intégré : compilation puis GitHub Release directe par `gh release create` ; aucun `actions/upload-artifact`.
- Limite : ce mécanisme n'évite pas la consommation des minutes Actions et ne fonctionne pas lorsque Actions est entièrement bloqué.
- Risque : keystore debug nouvelle à chaque runner → installation par-dessus la version précédente possiblement impossible. Sauvegarder avant désinstallation. Signature de production stable à préparer plus tard.
- Tests non faits : Android build, installation téléphone, cycles, retour depuis recherche, appareil photo, TTS, archive corrompue et restauration.
- Aucune panne de compilation ni Release réussie affirmée avant consultation du run.

## Run #1 — échec reproduit
- Run 35661973983 : échec dans android-actions/setup-android@v3, AVANT Gradle ; journal : 'Warning: Failed to find package tools'.
- Cause observée : l'action invoque sdkmanager tools alors que le catalogue ne fournit plus ce paquet. Ne pas attribuer cet échec au quota, au code Java ou au Base64.
- Correctif dans ce commit : supprimer l'action setup-android@v3 ; le journal prouve que sdkmanager existe déjà sur le runner Ubuntu, installer directement plateformes/build-tools. Nouveau run et APK à confirmer.

## Run #2 — échec distinct
- Run 35662052714 : 'sdkmanager: command not found' ; setup-android supprimé, donc l'outil n'est plus dans PATH. Gradle et Java n'ont pas été exécutés.
- Correction : localisation explicite du SDK préinstallé depuis son emplacement vérifié au run #1, variables SDK définies ; téléchargement uniquement si composants manquants. À revérifier sur run #3.

## Run #3 — correctif SDK validé sur CI
- Run 35662113704, commit b6e849c5aba3bd20322a538203deb6ff91632363 : succès confirmé de l'installation SDK, Gradle, compilation et publication.
- Release v0.1.0-b3 créée ; binaire FabMap-v0.1.0-b3-debug.apk, 27059 octets.
- Cette validation CI ne démontre PAS la validité du parcours sur un téléphone. Tests humains à faire.

## V0.2.0 — risques identifiés pré-build
- La v0.1.0 exporte/restaure la mémoire **complète** ; restaurer écrase la carte courante. Confondre import et fusion causerait une perte de données. Ajout de deux opérations distinctes de paquet de bulles, import additif sous un parent sélectionné.
- Graphe à parents multiples et cycles : export avec ensemble visité ; import recrée les références internes avec UUID afin de ne pas collisionner avec l'existant.
- Paquets ZIP : interdire chemins arbitraires/entrées doublées, borner nombres et volumes, vérifier photos avant adoption ; à tester avec fichier corrompu.
- L'interprétation de « probables » comme « procédures / bulles » reste une hypothèse explicite, pas une citation littérale de Fab.
- Build CI, publication et vérifications Android 0.2.0 **non réalisés** au moment de ce commit ; ne pas annoncer d'APK prématurément.

## Run #4 — livraison vérifiée
- Run 35665143325 du commit b43bf546ffecdfd9e991ba7673af0fde4cb9b7c6 : SDK, Gradle, compilation et publication directe réussis.
- Pré-release v0.2.0-b4, APK 33011 octets publiée. Aucune preuve de test réel de fichiers .fabmap ou du comportement Android, à effectuer sur téléphone.

## Itération 0.3.0 — comportement non destructif des appuis longs
- Risque : un appui long sur « C'EST FAIT ! » pourrait valider une étape par erreur. Le listener de long click renvoie true, la confirmation reste uniquement dans OnClick.
- Risque : prévisualisation modifiant la pile de navigation ; showPreview est une fenêtre modale, sans zoom ni changement de path ou step.
- Une fenêtre de lecture reste affichée après le relâchement, et le bouton « Réécouter » ne la ferme pas ; la fermeture arrête la synthèse.
- CI et vrai test téléphone à vérifier après commit ; ne pas annoncer l'APK avant preuve de Release.

## Run #5 — livraison 0.3.0 confirmée
- Workflow 35694822501 : JDK, SDK, Gradle, assembleDebug et gh release create réussis ; APK direct de test v0.3.0-b5 (34723 octets).
- Cela prouve la compilation et la publication, non le ressenti tactile, l'ergonomie ou la lecture effective sur téléphone.

## 0.4.0 — risques, distinctions, vérifications requises
- « La bulle engendre l'espace » signifie carte bornée par la présence d'éléments réels, pas physique 3D ni génération de mots. Le vide ne crée aucune bulle, pan contraint à la boîte des bulles.
- La proximité visuelle vient des liens parent/enfant existants et de placements déterministes ; NE PAS la présenter comme une distance de tokens IA/embedding. Aucun modèle lourd ni appel réseau.
- Cartes >160 : seule la représentation Vallée est tronquée, pas les données/fiche guidée ; vérifier les arcs et collisions sur graphes cycliques/partagés.
- Tests non faits au moment de ce commit : build 0.4.0, drag et pinçage sur appareil, appui long B, précision des touchers et TalkBack. Ne pas annoncer Release avant contrôle du run.

### Run #6 — échec de compilation prouvé
- https://github.com/greenpower2669/FabMap/actions/runs/35748705120 : SDK et Gradle réussis, échec compileDebugJavaWithJavac : BubbleValleyView.java:209 « variable listener might not have been initialized » ; gh release ignorée. Cause : Runnable de champ capturant un blank final assigné plus tard dans le constructeur. Correctif : rendre listener non-final ; aucune autre modification de logique.

### Run #7 — correctif Java et Release vérifiés
- https://github.com/greenpower2669/FabMap/actions/runs/35748911214 : compilation, étapes SDK et publication réussies après correction du Listener ; v0.4.0-b7, APK 43775 octets. Aucun test instrumental Android ou utilisateur n'a été exécuté.

## Demande Fab 2026-09-22 — clarifications avant code / upload humain
- **Portée exacte** : mettre à jour les quatre `.md` seulement. Ni APK, ni modification de `MainActivity`, ni commit de PNG, ni déplacement d'images dans ce cycle. Ne pas présenter les 7 planches comme déjà disponibles dans le dépôt : l'inventaire Git actuel ne contient que l'icône Android `fabmap_icon.xml`, pas les boards PNG.
- **Risque de perte de planches / noms approximatifs** : sept planches en conversation : thèmes, actions, états, navigation, procédure, bulles, repérage ; 8 images conceptuelles légendées par planche. Établir noms et ordre stables lorsque Fab les déposera en binaire sur `main` ; vérifier les sept chemins et ne jamais supposer que les chemins proposés sont les chemins effectifs.
- **Risque d'APK lourd/lag** : ne pas mettre sept posters complets et tous leurs libellés dans chaque nœud ni convertir en Base64 ; découper/optimiser après réception, charger les ressources nécessaires. Les PNG master ne sont pas des vecteurs SVG et n'entrent pas automatiquement dans la build Android.
- **Risque de faux sens de « bulles simples liées à chacune au moins une fois »** : documenter une bulle par notion et un lien pour chaque usage visuel, partager les nœuds homonymes si pertinent ; ne pas générer de bulles de remplissage dans les zones vides.
- **Correction du scénario TV historique** : le texte de démonstration `chaines.steps` parle d'appuyer sur SOURCE puis d'entrer dans la box ; Fab donne maintenant son parcours réel : ON/OFF de la TV, puis HDMI3 **si** vue de chaînes payantes/autres, ou ON/OFF télécommande portant « TV » **si** écran noir, puis attendre l'affichage parfois long. L'ancien texte est obsolète pour ce cas, mais **n'a pas encore été modifié** ; ne pas écraser une carte existante ni inventer le bouton qui mène à HDMI3. À vérifier en situation réelle.

## 0.5.0 — risques distingués et actions correctives
- Constat Git au début de ce cycle : les trois PNG de Fab sont à la racine avec des identifiants techniques, sans assets/. Leur classement est un MOVE via SHAs de blobs Git, sans transfert JSON/Base64. Aucun PNG 04–07 reçu ; les placeholders de catalogue ne doivent PAS être présentés comme des pictos présents.
- Les tableaux préparatoires 04–07 des mémoires étaient une projection de concept, différents des planches complémentaires réellement générées ensuite. Le pense-bête 0.5.0 de brain.md devient la référence, par index, pour prévenir les confusions (ex. navigation/procédure/bulles/repérage).
- JPEG custom séparé des masters, limite mémoire par décodage échantillonné et 256px ; sauvegarde dans media/. ZIP de branche : vérifier fichier utilisé et remapper refs à l'import ; pas de crash si source built-in absente. Les références custom malformées devront être testées manuellement.
- Procédure TV démonstration SOURCE déjà existante dans certaines mémoires : ne pas l'écraser automatiquement. Ajouter distinctement la procédure réelle à la demande, deux chemins conditionnels, sans inventer de touche source ni durée.
- Build et tests Android **à vérifier après push**, signature debug temporaire : export complet avant toute désinstallation. Test à effectuer : import d'une image PNG avec transparence, conversion JPEG, deux exports/imports branche, vieux ZIP, upload futur 04–07, rendu carte à 160 bulles.

### Run #8 — succès de build, limites conservées
https://github.com/greenpower2669/FabMap/actions/runs/35764510346 : extraction 24 sprites, compilation et publication directe réussies ; APK v0.5.0-b8, 742367 octets. Ce test ne prouve ni le rendu ni la restauration des icônes sur téléphone. La signature debug reste éphémère ; export avant désinstallation.
