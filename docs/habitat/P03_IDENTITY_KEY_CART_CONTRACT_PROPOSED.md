# IPM Habitat Cart — Contrat candidat P03 : identité, clé et ancre

**État :** `DESIGN_PROPOSED / CODE_NOT_STARTED / QA_NOT_RUN`.  
**Owner de design :** Immersive Petro-Machinery GOV. **Owner d'exécution :** DEV IPM, après P00 complet.  
**Cible :** Minecraft 1.21.1 / NeoForge / Java 21, avec versions du workspace candidat à découvrir.  
**Dépendance :** [P00_DEV_BASELINE_AND_GATES_2026-10-31.md](P00_DEV_BASELINE_AND_GATES_2026-10-31.md).  
**Source de conception :** GOV IPM `proposals/IPM-FLEET-20261031/03_HABITAT_CART_SPEC_CONSOLIDEE_v0.3.md`, issue du Volet 2 réconcilié.

## 1. Résultat unique proposé pour la première PR de code

**TASK proposé :** créer et tester, sur une base réelle validée, un sous-système **serveur** qui préserve l'identité d'un habitat et évalue les credentials de manière pure, sans créer encore l'entité, la dimension ou la téléportation.

**Critère de fin :** le registre sérialise/désérialise un habitat et ses permissions sans inventer d'identifiants ; une évaluation d'entrée accepte exactement le joueur + la clé + l'ancre + l'ACL + les révisions valides, rejette les autres et ne modifie aucune donnée de jeu en cas de refus. Les tests sont exécutés avec la version et le HEAD candidats, pas uniquement décrits ici.

Les futures étapes **P04/P07** raccordent l'évaluation à la vraie interaction physique du cart, à l'item Habitat Key et à la dimension privée. Ce document n'en fait pas une fonctionnalité déjà utilisable.

## 2. Invariants de modèle

| Concept (nom de travail) | Champ / type métier | Règle |
|---|---|---|
| Identité monde | `worldId: UUID` | Évite une réutilisation ordinaire entre saves ; pas garantie universelle contre clones de monde |
| Identité habitat | `habitatId: UUID` | Stable sur déplacement, re-key et remplacement de cart |
| Dimension | `dimensionKey` (valeur serveur dérivée) | Un identifiant stable par habitat ; allocation réelle réservée au backend P02/P07 |
| Propriété | `ownerUuid: UUID` | Le détenteur temporaire de la clé n'obtient jamais la propriété |
| Ancre canonique | `anchorUuid: UUID` + `anchorEpoch: long` | Unique ; remplacement incrémente la génération |
| Révision des permissions | `aclRevision: long` | Toute mutation des permissions invalide les évaluations anciennes |
| Clé personnelle | `keyId: UUID` + `boundHolderUuid: UUID` | Non transférable par simple échange d'item |
| Révision de clé | `credentialRevision: long` + `revoked: boolean` | Réémission/révocation vérifiées par le serveur |
| Portée | `keyScope: enum` | Ne donne jamais plus de droits que l'ACL actuelle |
| Session | `sessionId` + status | Distinguer nouvelle entrée et reprise d'un occupant déjà validé |
| Sauvegarde | `schemaVersion` / `migrationVersion` | Rejeter/quarantainer cas inconnus sans détruire les données |
| Concurrence | `expectedRevision` + `requestId` | Pas de double allocation, émission ni mutation sur retry |

Tous ces noms sont **des champs proposés**, non des registry IDs ni des classes réellement présentes. Les noms/classes/packages et codecs sont à choisir en lisant le candidat vivant au gate P00.

## 3. Prédicat d'entrée normale

Le service serveur évaluera les conditions suivantes **toutes ensemble** :

1. Un joueur authentifié au serveur correspond au `boundHolderUuid` de la clé.
2. Le credential `keyId` existe dans le registre **du même `worldId`**, n'est ni révoqué ni obsolète.
3. Le credential désigne le même `habitatId` et la **génération actuelle** `anchorEpoch`.
4. Le cart observé désigne l'unique `anchorUuid` canonique (l'ID est corroboré par l'entité serveur).
5. L'ACL actuelle autorise `ENTER` pour ce joueur et cette portée de clé.
6. Le contexte de porte représente **une interaction locale vérifiée sur ce cart réel**, sans destination fournie librement par le client.
7. L'état réel n'interdit pas l'accès (travaux, rupture, mobilité, destination invalide).

**Séparer calcul et effet :** P03 peut tester la vérification des identités/révisions sans simuler un clic physique. La présence de l'entité et la portée de l'interaction sont des preuves que **P07 doit fournir**, pas des booléens fournis à la confiance du client.

**Jamais :** clé seule = téléportation ; UUID devinable = permission ; item NBT = autorité ; une clé Crew = transfert de propriété. Une porte déjà ouverte n'ouvre pas l'accès aux non-autorisés.

**Toujours :** la sortie normale et la sortie de secours restent disponibles sans clé ni ACL d'entrée. Un occupant déjà présent peut reprendre une session valide après reconnexion ; ce n'est pas une nouvelle entrée distante.

## 4. Émission, révocation et remplacement de cart (contrats)

- Première association : le Garage émet une Master Key liée au propriétaire/habitat, sur une transaction qui ne crée pas deux maisons si répétée.
- Invitation : chaque équipier obtient, si autorisé, une **Crew Key personnelle** et une portée limitée par l'ACL.
- Perte/vol : la possession de l'item sans bon UUID joueur ne permet pas l'entrée. Réémission = changement de `credentialRevision` / révocation traçable, sans supprimer l'habitat.
- Cart détruit : invalider l'ancre canonique, conserver la maison et ses biens ; l'éjection avec dégâts et `EJECT_ON_LOGIN` sont des opérations **P07/futures**, non implémentées dans P03.
- Remplacement : associer un nouveau `anchorUuid`, incrémenter `anchorEpoch`, revalider/repréparer explicitement les clés. L'ancien véhicule ne peut plus ouvrir la même maison.
- Double ancre ou données contradictoires : état `RECOVERY_REQUIRED` ; ne pas désigner automatiquement le dernier cart chargé comme autorité.

## 5. Persistance, migration et sécurité

Régler les métadonnées dans un registre persistant indépendant du chunk de l'ancre. Le propriétaire du futur stockage et le point de chargement sont à choisir au P00 ; `SavedData` NeoForge est une option architecturale, pas une preuve d'atomicité entre le joueur, le cart et les fichiers de région.

Les données de blocs/objets habitables appartiendront **à la vraie dimension intérieure**, et non au registre. Une entrée de registre sans dimension relue n'est pas une pièce vide à régénérer.

P03 n'implémente ni la dimension par cart, ni sa création dynamique ni le rendu. Ne pas choisir sans preuve une API Hyperbox/Infiniverse ; P02 qualifie le backend. Ne pas interpréter des anciens NBT inconnus comme absence : conserver de quoi diagnostiquer.

Les permissions sont vérifiées et les mutations sérialisées côté serveur. Rejeter ancien `expectedRevision` et doublons `requestId` ; limiter les tailles/collections et la durée de rétention du journal sans oublier une demande encore rejouable.

## 6. Contrats ultérieurs conservés, non fusionnés dans P03

- **25 blocs cubes :** demande propriétaire, interprétation volume total vs positions occupées encore ouverte. Aucune configuration en `7×7×4` ou extension au-delà de 25 implicite.
- **Machines/repas/buffs/debuffs :** gameplay fonctionnel dans la vraie pocket dimension, sous règles de ressources/effets adoptées.
- **Sommeil Overworld :** coordinateur de quorum entre Overworld et résidents des habitats ancrés à l'Overworld ; pas de changement de temps autonome dans chaque pocket.
- **Port mécanique Digger :** seuil cart et raccord validé ; aucun transfert item/fluide/FE ou accès distant automatique.
- **Destruction :** éjection sûre + dégâts lorsque le cart canonique est réellement détruit ; `4 points` reste un exemple de balance, pas une valeur approuvée.
- **Récupération et offline :** sans suppression de dimension ou copie de coffres, incidents et journaux à réconcilier après interruption.
- **Gates IPM :** ne modifier ni GUI/renderer/fluides natifs Tunnel Digger, ni Mixins, ni comportements miniers hors contrat, ni publication. La fondation Garage 11×13×10 reste réelle.

## 7. Tests requis pour qualifier la première PR de code

| ID | Entrée / action | Critère attendu |
|---|---|---|
| P03-T01 | Owner + Master Key correspondante | Validation des identities/ACL positive, **sans téléportation P03** |
| P03-T02 | Clé volée par autre UUID joueur | Rejet serveur |
| P03-T03 | Clé valide pour habitat A sur cart B | Rejet serveur |
| P03-T04 | Ancienne génération d'ancre | Rejet serveur |
| P03-T05 | Révocation de credential entre requête et exécution | Rejet et absence d'effet secondaire |
| P03-T06 | ACL révoquée malgré clé valide | Rejet |
| P03-T07 | Double création/emission avec requestId identique | Un seul habitat/credential, réponse répétée connue |
| P03-T08 | Deux opérations sur même expectedRevision | Une seule acceptée ; autre requête obsolète |
| P03-T09 | Roundtrip sérialisation de propriétaire, ancre, ACL et clés | Identités et révisions conservées |
| P03-T10 | Ancien schema ou champ inconnu | Migration maîtrisée ou mise en quarantaine, pas effacement silencieux |
| P03-T11 | Changement d'ancre/génération après récupération | Ancienne clé/ancienne ancre invalides ; habitatId inchangé |
| P03-T12 | Clé absente sur sortie interne | Sortie non liée à validation credential ; condition testée au niveau du service concerné |

Ces douze cas sont des **critères futurs : `NOT_RUN`**. P03 ne passe à `PASS` qu'avec le code réel, commandes/exit codes, révision et preuves du candidat. Les tests de GameTest/serveur dédié sont nécessaires pour le clic réel, les dimensions et l'interface (P07), pas inventés par cette fiche.

## 8. Livrable, limites et autorité

Cette fiche est l'**entrée de conception du lot**, non sa réalisation. P00 reste partiel tant que le workspace local récent est inaccessible. Ne pas créer du Java sur la base distante du 17 juin, le faire passer pour le plus récent, ni réinitialiser des travaux locaux pour le contourner.

**GIT :** contenu proposé dans une **PR DEV Draft documentaire**, sans merge. **RUNTIME :** `NOT_RUN`. **PUBLICATION :** `NOT_AUTHORIZED`. **OWNER :** DEV IPM seulement après choix de la vraie base et reprise des gates.
