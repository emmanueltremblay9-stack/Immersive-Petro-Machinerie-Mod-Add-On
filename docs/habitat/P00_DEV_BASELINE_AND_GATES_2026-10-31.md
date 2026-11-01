# IPM Habitat Cart — P00 audit du candidat DEV et gate de reprise

**Project :** Immersive Petro-Machinery / `Immersive_Petro-Machinerie_GPT`  
**Internal Project ID :** `DS-IPM` (profil/DECISIONS v0.2.1)  
**Dépôt de destination :** `emmanueltremblay9-stack/Immersive-Petro-Machinerie-Mod-Add-On`  
**Cible produit :** Minecraft Java 1.21.1 / NeoForge / Java 21  
**Date de l'observation :** 2026-10-31 — America/Toronto  
**Mode :** `P00 / READ_ONLY_AUDIT + DOCUMENTARY_DRAFT_PR`  
**Verdict :** `P00_PARTIAL / BLOCKED_FOR_DEPENDENT_CODE`  
**État DEV :** `NO_GAMEPLAY_IMPLEMENTATION / BUILD_NOT_RUN / GAMETEST_NOT_RUN`

## 1. Objet et frontière d'autorisation

Après la réconciliation du **Volet 2 GOV** dans la PR GOV #1, créer une trace de préflight **dans DEV**, sans prétendre avoir découvert le candidat local le plus récent. Cette PR DEV est volontairement **documentaire et en brouillon**, distincte du lot de code d'identité et clé. Elle ne réalise pas P03/P07, n'adopte pas les modules proposés et n'ouvre aucun gate protégé.

**Pourquoi ce périmètre minimal :** des travaux locaux plus récents ont été signalés, mais la connexion à l'ordinateur autorisé est **hors ligne** au moment de cet audit. Ni HEAD local, ni branche, ni fichiers modifiés, ni versions actives, ni JAR de ces travaux ne peuvent être confirmés. Partir de l'ancien `main` GitHub pour écrire du Java et annoncer une implantation serait trompeur. Aucune opération de reset ou de synchronisation destructive n'est permise pour contourner cette incertitude.

## 2. Observations DIRECTES — dépôt GitHub distant

| Critère | Observation | Portée |
|---|---|---|
| Dépôt DEV | Public, push autorisé au compte connecté, non archivé | Permission de connexion GitHub, pas autorisation de publication mod |
| Branche distante par défaut | `main` | Branche contrôlée pour cette PR documentaire |
| HEAD distant `main` | `fee532df16975a1ce36d75c5315eff659f597bba` | Commit du 17 juin 2026, non preuve de l'état local actuel |
| Autres branches | Recherche de branche : `main` trouvée ; pas d'autre branche dans la réponse courante | Vérification au moment de la lecture |
| PR ouvertes au dépôt | Aucune renvoyée par la recherche GitHub ciblée | Ne prouve pas absence d'un travail local |
| `AGENTS.md` | Relu au commit distant | Java 21, build après code/config/ressources, restrictions IPM, compétences locales |
| `gradle.properties` | `mod_version=0.1.0`, `minecraft_version=1.21.1`, `neoforge_version=21.1.164` | Valeurs du snapshot, **pas** valeurs du candidat local plus récent |
| Dépendances distantes | IM `0.2.0+1.21.1+neoforge`, IE `1.21.1-12.4.2-194`, IP `4.4.1-37`, IA `1.2.2+1.21.1+neoforge` | Versions déclarées, résolution et runtime non testés |
| Java/Gradle | `build.gradle` impose toolchain et `--release 21`; `settings.gradle` nomme le projet | Fichiers lus, aucune compilation |
| Code distant | Classe `ImmersivePetroMachinery` et modules Garage ; aucun dossier `habitat` dans l'arbre Git distant lu | N'affirme rien sur les fichiers locaux non publiés |
| GameTests | `src/main/java/com/emman/immersivepetromachinery/gametest/IPMGarageSafetyGameTests.java` existe | Aucun GameTest exécuté dans cette tranche |
| Compétences locales | `.agents/skills/neoforge-gametest-diagnostics/SKILL.md` et `.agents/skills/ie-formed-multiblock-dev/SKILL.md` relus sur le distant | Guides conditionnels ; ni exécutants, ni droits de modifications |
| Contact avec le candidat local | Remote Desktop Commander : appareil inscrit mais **offline** | Workspace/HEAD/dirty state/tracking/artefact actuels inconnus |

**Historique attribué et non certifié aujourd'hui :** le contexte IPM mentionne des builds/runtime `0.1.19` et un candidat `0.1.23`, mais leurs révisions réelles n'ont pas été relues. Un état de `main` GitHub antérieur ne révoque pas les travaux locaux.

## 3. Sources de design et statut des décisions

- GOV IPM : `emmanueltremblay9-stack/Immersive_Petro-Machinerie_GPT`.
- PR GOV **#1**, `docs(IPM): handoffs flotte + Habitat Cart v0.3 réconcilié`, **fusionnée** au commit `7362a2aa25b557d4badf6c2fbd20a9995dcf56fc` (observation 2026-10-31 locale).
- Référence d'étude : `proposals/IPM-FLEET-20261031/03_HABITAT_CART_SPEC_CONSOLIDEE_v0.3.md` dans GOV. Elle est **DESIGN_PROPOSED**, pas du code déjà présent.
- Plan P00–P15 : `proposals/IPM-FLEET-20261031/02_GOV_IMPLEMENTATION_HANDOFF.md`. Les numéros P00..P15 sont des **lots**, pas des PR DEV déjà créées.
- `GOVERNANCE.md`, `DECISIONS.md`, `PROJECT_PROFILE.md` et `IMMERSIVE_PETRO_MACHINERY_PROJECT_SOURCE_v2.1.0.md` servent à réconcilier permissions, décisions et variantes techniques.
- Les profils MK1/MK2/MK3 5×5/7×7/9×9 possèdent un accord daté du 18 juin pour **cette tranche précise** ; le `AGENTS.md` distant contient un ancien « approval pending ». Cette divergence est résolue pour le profil uniquement, **pas pour de nouvelles opérations de minage ou du code Habitat**.

## 4. Ce qui peut être développé après P00

**Premier outcome de code proposé, à exécuter seulement sur le candidat sélectionné et relu :** modèles d'identité/ACL/credential `Habitat`, enregistrements persistants testables et validation serveur pure qui refuse une clé obsolète ou étrangère.

Ce premier outcome **n'implique pas** l'entité visuelle, la création de dimension, la téléportation, le vrai cadre-portail, l'éjection, les dégâts, le port de transfert, le sommeil ni la GUI de Garage. Ils appartiennent à des lots et tests ultérieurs, selon les contrats et décisions adoptés.

Référence d'acceptation préparatoire : `docs/habitat/P03_IDENTITY_KEY_CART_CONTRACT_PROPOSED.md` sur la même branche.

## 5. Condition de déblocage P00 avant une PR de Java

1. Identifier l'espace de travail réellement retenu **et vérifier** son dépôt racine, branche, HEAD, tree, `git status --porcelain`, suivi et ancêtres de `origin/main`. Faire l'inventaire des fichiers non suivis et du candidat local plus récent **sans reset**.
2. Lire `AGENTS.md` de cet espace, ses skills accessibles, `gradle.properties`, `build.gradle`, `settings.gradle`, le mod TOML et les registres/menus/entities réels.
3. Identifier toute différence entre sources locales et `fee532df...` ; préserver les changements du Digger, du Garage, des tests et des ressources existantes.
4. Examiner les PR/branches ouvertes et chevauchements **à ce moment** ; choisir une base qui ne remplace pas un travail local non poussé. Pas de branche d'implémentation tant que la base dépendante reste inconnue.
5. Vérifier Java 21, l'exécutable Gradle wrapper et les dépendances effectives du candidat. Exécuter un **build de référence** si cet environnement l'autorise ; consigner commande, exit code et artefacts, pas de PASS inventé.
6. Reconfirmer la portée d'une **première tranche code** et ses éventuels impacts d'autorité ; conserver les restrictions de publication, mixins, GUI/renderer/fluides natifs du Tunnel Digger, minage hors contrat, Survey Console, Core Sampling et rendu Garage.
7. Une fois le candidat et la portée vérifiés, préparer la plus petite PR de code/QA sur **la branche candidate correcte**. Construire et tester avec Java 21 après les changements ; distinguer tests unitaires, GameTests, dédié, persistance et comportement en jeu.

**Stop boundary :** aucune édition des sources Java, resources, dépendances ou configuration de build dans cette PR. Aucune fusion/Release/upload. Ne pas afficher `P00 PASS` tant que les points 1–5 sont inconnus.

## 6. Résultat documentaire et preuve

La présente PR DEV prouve uniquement qu'une **base distante figée** a été inspectée et qu'un contrat de première implémentation est disponible dans DEV. La connexion GitHub et l'écriture sur la branche documentaire seront vérifiées par lecture après commit. Aucun `.jar`, test de build, GameTest, lancement client/serveur, changement de mod ou validation visuelle n'est revendiqué.

**VERDICT P00 :** `PARTIAL` — audit distant conclu ; baseline locale/version candidate `UNKNOWN`, code dépendant `BLOCKED` jusqu'à réconciliation.
