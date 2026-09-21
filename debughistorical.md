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
