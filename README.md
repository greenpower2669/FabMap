# FabMap 🫧

Mémoire vivante et aide-mémoire Android, avec zoom dans les bulles filles et dézoom sur le chemin d'origine. Première démonstration : télévision et télécommande.

**Prototype 0.1.0 à tester sur téléphone** : bulles et liens réutilisables, création, recherche, lecture vocale, photo, étapes et sauvegarde locale. Pas de serveur, compte familial, messages ni accès distant. ChatGPT n'est qu'un raccourci web facultatif.

## Livrer directement l'APK

Un push sur main modifiant Android déclenche le build ; il est aussi possible de lancer **Actions → FabMap APK direct Release → Run workflow**. Si la compilation et la publication réussissent, un APK `FabMap-v0.1.0-bN-debug.apk` est dans **Releases**. Pas de `upload-artifact`, pas de ZIP Actions.

Ce contournement concerne uniquement le quota de stockage des artefacts Actions ; il ne contourne pas les minutes CI, les restrictions de compte ou la facturation. Signature debug temporaire : une mise à jour peut exiger désinstallation et entraîner la perte de données si aucune sauvegarde n'a été exportée.

Outils : JDK 17, Gradle 8.10.2, Android SDK 35. Construction locale : `gradle :app:assembleDebug -PbuildNumber=1`.

Documents FAB Copilot : [brain.md](brain.md), [brainmap.md](brainmap.md), [debughistorical.md](debughistorical.md), [todo.md](todo.md).

## Exporter / importer une procédure
Une sauvegarde complète existe toujours : **Sauvegarder ma mémoire** puis **Restaurer une sauvegarde** (attention, cette restauration remplace la mémoire actuelle).
En plus, **Télécharger cette bulle et ses filles** crée un paquet local .fabmap depuis la bulle affichée, y compris descendants, étapes et photos référencées. **Importer des bulles ici** rattache le paquet à la bulle courante sans écraser les existantes. Identifiants et médias sont remappés à l'import, une sous-bulle partagée est exportée une seule fois. Hors ligne ; aucun compte ni connexion entre personnes.
Le sélecteur Android permet de choisir Téléchargements, stockage local ou un fournisseur de documents éventuellement configuré sur le téléphone. Il n'y a pas de téléchargement automatique depuis un serveur.

## FabMap 0.3.0 : lecture par appui long
Toucher brièvement une bulle pour zoomer, maintenir pour ouvrir un aperçu centré en gros caractères avec lecture vocale automatique ; relâcher ne ferme pas la fenêtre. « Réécouter » et « Fermer » sont explicites. Les boutons ont le même comportement : maintenir ne déclenche jamais leur action. « C'EST FAIT ! » est vert et agrandi. Sur téléphone, tester le comportement avec TalkBack et les tailles de police Android.

## 0.4.0 — Vallée des bulles
« Explorer la Vallée des bulles (2D) » ouvre une carte calculée sur les bulles réellement enregistrées dans le sous-graphe courant : couleurs de branches, liens réels, aucun concept ajouté pour remplir le vide. Glisser voyage à l'intérieur de la carte bornée ; pincer ou boutons −/+ zoome ; « Recentrer » retrouve les bulles. Toucher une bulle ouvre sa fiche guidée ; maintien donne l'aperçu vocal B. Le mode guidé reste accessible et illimité ; seule la carte limite son affichage aux 160 premières bulles pour éviter les ralentissements. Placement par relations, pas par modèle vectoriel IA. Aucune dépendance ou permission réseau supplémentaire.

## 0.5.0 — Icônes de FabMap
Les planches maîtresses PNG restent dans `assets/icons/boards/` sur main ; seules les petites icônes découpées par `tools/ExtractIconBoards.java` sont incluses dans l'APK. Trois planches sources ont été téléversées par Fab : thèmes, actions, états ; les quatre autres restent à déposer. En mode « Modifier / créer », **Choisir une icône** affiche celles disponibles et permet d'importer une image personnelle (JPEG normalisé 256 px), ou de supprimer l'association. Sur Accueil, **Installer les bulles des icônes** crée un catalogue de sept familles/56 notions, avec texte même si la planche correspondante n'est pas encore disponible. **Installer la procédure TV réelle** crée deux chemins conditionnels sous la bulle TV de démonstration sans écraser l'ancienne mémoire. Les icônes personnelles sont sauvegardées dans media/ en JPEG UUID, reprises dans la sauvegarde complète et les paquets de branche, sans Base64. Les icônes par défaut sont identifiées par built:NN-NN et n'exigent pas de copie dans les exports.

## 0.5.1 — télévision et titres contrastés
La procédure TV n'est plus un module « TV réelle » à installer : FabMap corrige le parcours de démonstration intégré. Allumer avec ON/OFF, puis choisir **uniquement** l'état constaté : vue des chaînes payantes/autres → HDMI3 ; écran noir → bouton ON/OFF marqué TV sur la télécommande TV, puis attendre un affichage parfois long. L'ancienne touche SOURCE générique ne figure plus dans un parcours neuf. Une mémoire déjà créée est ajustée seulement lorsque le texte et les étapes correspondent exactement à l'ancienne démonstration ; les bulles personnelles et textes édités ne sont pas écrasés. Une branche auparavant ajoutée reste accessible sans doublon.
La Vallée dessine le titre sous l'image de chaque bulle, dans un cartouche bleu nuit aux caractères blancs et ombre portée. Les tuiles du mode guidé séparent aussi pictogramme et libellé dans un bandeau contrasté, avec relief.
