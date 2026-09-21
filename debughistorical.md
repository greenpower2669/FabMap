# 🐞 FabMap — historique des risques et vérifications

## 2026-09 — lancement
- Fab rapporte un quota GitHub gênant sur la livraison des APK par artefacts Actions. Cause/quota précis non vérifiés.
- Contournement intégré : compilation puis GitHub Release directe par `gh release create` ; aucun `actions/upload-artifact`.
- Limite : ce mécanisme n'évite pas la consommation des minutes Actions et ne fonctionne pas lorsque Actions est entièrement bloqué.
- Risque : keystore debug nouvelle à chaque runner → installation par-dessus la version précédente possiblement impossible. Sauvegarder avant désinstallation. Signature de production stable à préparer plus tard.
- Tests non faits : Android build, installation téléphone, cycles, retour depuis recherche, appareil photo, TTS, archive corrompue et restauration.
- Aucune panne de compilation ni Release réussie affirmée avant consultation du run.
