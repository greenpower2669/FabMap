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
