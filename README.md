# Moteur physique — Phase 2

Projet de moteur physique en Java réalisé par l’équipe **Sonic**, dans le cadre du cours **8INF935** à l’UQAC.

Cette phase ajoute les générateurs de forces, les ressorts, les contraintes et la résolution des collisions par impulsions. Le moteur est utilisé dans un jeu de blob : un ensemble de particules reliées qui peut se déformer, se séparer et fusionner.

## Technologies

- **Java 17 ou supérieur** pour compiler et exécuter le projet.
- **Maven** pour les dépendances, la compilation et les tests.
- **Processing 4** pour l’affichage et l’export d’une application Windows.
- **JUnit 5** pour les tests automatisés.

Le projet utilise Processing Core **4.5.3**, téléchargé automatiquement par Maven. Une connexion Internet est nécessaire lors de la première compilation pour récupérer les dépendances.

## Installation et lancement

Exécuter les commandes depuis le dossier contenant `pom.xml`.

### Avec Maven et Java

```powershell
mvn package
java -jar target/game-engine-1.0.0.jar
```

La commande `mvn package` compile le projet, exécute les tests et produit le JAR exécutable avec ses dépendances.

### Avec Processing

1. Exécuter `mvn package` à la racine du projet.
2. Ouvrir `src/sketch/Blob/Blob.pde` dans Processing 4.
3. Lancer le sketch.

Le build Maven copie automatiquement le moteur dans le dossier `code` des sketches Processing.

### Avec l’application Windows exportée

Dans le dossier de remise, lancer `Application/Blob.exe`. Conserver l’ensemble du dossier `Application`, notamment ses sous-dossiers `java` et `lib`. L’export contient son propre environnement Java.

Les applications exportées se trouvent dans `release/` en local. Ce dossier est ignoré par Git et n’est pas fourni lors d’un clonage du dépôt.

## Objectif du jeu

La scène 1 commence avec **8 particules reliées**. Quatre particules dorées sont réparties dans le niveau.

Pour gagner :

1. Recruter les quatre particules dorées en les touchant ou en fusionnant à portée.
2. Franchir le mur de **120 pixels** situé entre les deux plaques.
3. Séparer le blob et placer **deux morceaux distincts** sur les plaques.
4. Maintenir les deux plaques occupées pendant **0,75 seconde** pour ouvrir la porte.
5. Réunir les **12 particules** en un seul morceau et les amener entièrement dans la sortie verte.

La porte reste ouverte après son activation. Un seul morceau étiré sur les deux plaques ne suffit pas à l’ouvrir. Les particules dorées deviennent contrôlables après leur recrutement.

## Commandes

| Touche ou action | Effet |
|---|---|
| Flèches gauche / droite | Déplacer le morceau sélectionné |
| Espace / flèche haut | Sauter dans le niveau 1, depuis le sol ou le dessus du mur |
| Flèches | Appliquer des forces dans les scènes de démonstration ; la scène 8 reste horizontale |
| S | Séparer le morceau sélectionné, dans les scènes 1 et 9 |
| F | Fusionner avec un morceau proche, dans les scènes 1 et 9 |
| Tab / clic sur une particule | Sélectionner un autre morceau contrôlable |
| P | Mettre en pause ou reprendre |
| R | Recommencer la scène actuelle |
| 1 à 9 | Choisir une scène |
| A / D | Appliquer une petite force horizontale dans la scène 8 |

Le saut nécessite un appui. Maintenir sa touche ne provoque ni vol ni sauts répétés. La fusion avec `F` nécessite une distance inférieure à **70 pixels** entre deux particules des morceaux concernés.

### Indications visuelles

- **Vert** : morceau sélectionné.
- **Bleu** : autres morceaux recrutés.
- **Doré** : particules à récupérer.
- **Liens du bleu à l’orange** : étirement croissant vers la longueur maximale.
- **Anneaux et textes temporaires** : recrutement ou fusion.

Le compteur du HUD indique le nombre de particules dans le morceau sélectionné. Chaque changement de valeur déclenche une oscillation amortie.

## Scènes de démonstration

| Scène | Contenu |
|---|---|
| 1 | Niveau complet : collecte, mur, saut, plaques, porte et sortie |
| 2 | Ressort classique entre deux particules |
| 3 | Ressort ancré à un point fixe |
| 4 | Ressort bungee, actif uniquement en étirement |
| 5 | Ressort rigide factice amorti |
| 6 | Tige imposant une distance fixe |
| 7 | Câble imposant une distance maximale |
| 8 | Friction statique et cinétique |
| 9 | Blob libre de 12 particules, pour tester la séparation et la fusion |

Dans la scène 8, `A` et `D` appliquent une force de 130, inférieure au seuil statique de 216. Les flèches appliquent une force de 650 pour observer le glissement.

## Organisation du moteur

| Classe ou groupe | Responsabilité |
|---|---|
| `Particule`, `ParticuleSphere`, `Vecteur3D` | État physique, calculs vectoriels et intégration |
| `GenerateurForce`, `RegistreForces` | Interface et accumulation des forces |
| `Gravite`, `FrictionTrainee`, `FrictionSolide`, `FrictionSol` | Gravité, traînée et friction |
| `Ressort`, `RessortAncre`, `RessortBungee`, `RessortRigideFactice` | Variantes des ressorts |
| `DetecteurCollisions`, `ContactPlan`, `ContactRectangle` | Détection des contacts entre particules et avec les obstacles |
| `ContactTige`, `ContactCable` | Contraintes de longueur |
| `Contact`, `ResolveurContacts`, `GestionnaireCollisions` | Impulsions et correction des interpénétrations |
| `Monde`, `LienBlob` | Simulation et réseau des liens du blob |
| `NiveauBlob` | Recrutement, plaques, saut, ouverture de la porte et victoire |
| `JeuBlob`, `Main` | Commandes, rendu Processing et lancement |
| `OscillateurAmorti` | Animation du compteur |

La simulation utilise un pas fixe de **1/240 s**, distinct de la fréquence d’affichage. Les règles du niveau sont évaluées après chaque sous-pas physique.

### Formules principales

| Notion | Formule | Utilisation |
|---|---|---|
| Deuxième loi de Newton | `a = ΣF / m` | Accélération des particules |
| Gravité | `F = m g` | Chute des particules |
| Loi de Hooke | `F = −k (ℓ − ℓ₀) n` | Force des ressorts classiques |
| Impulsion | `Δv = J / m` | Résolution des collisions et saut |
| Friction de Coulomb | `Fs ≤ μs N`, `Fk = μk N` | Repos et glissement |
| Oscillateur amorti | `m x'' + c x' + k x = 0` | Animation du HUD |

Le saut applique une impulsion `J = m Δv` avec un changement de vitesse verticale de `−520 px/s`. La gravité et la traînée déterminent ensuite le mouvement. Dans le repère du jeu, l’axe vertical est orienté vers le bas.

Les seuils de recrutement, le délai d’activation des plaques et les conditions de victoire sont des règles de jeu.

## Tests

```powershell
mvn test
```

La suite comprend **152 tests** au dernier lancement vérifié. Elle couvre les forces, les ressorts, les contacts, les contraintes, la séparation/fusion, le saut et un parcours complet du niveau jusqu’à la victoire.

Les tests de rendu produisent les images des neuf scènes dans `target/verification-visuelle/`. Les rapports JUnit sont disponibles dans `target/surefire-reports/`.

## Structure du dépôt

```text
Moteur/
├── pom.xml
├── README.md
├── src/
│   ├── main/java/com/team/game/   # Moteur et logique du jeu
│   ├── test/java/com/team/game/   # Tests JUnit
│   └── sketch/
│       ├── Blob/                 # Jeu de la phase 2
│       └── Jeu/                  # Jeu balistique de la phase 1
├── Docs/                         # Présentation et vidéo
├── scripts/                      # Script d’export Windows
├── target/                       # Résultats Maven, ignorés par Git
└── release/                      # Exports locaux, ignorés par Git
```

## Présentation

Les supports actuels se trouvent dans `Docs/` :

- [Présentation PDF](Docs/Presentation%20Diapositives.pdf)
- [PowerPoint modifiable avec notes pour l’oral](Docs/Presentation%20Diapositives.pptx)
- [Vidéo de démonstration](Docs/VideoDemoJeux.mp4)

La présentation comporte **19 diapositives**, dont trois annexes mathématiques. La démonstration en direct doit montrer les fonctionnalités demandées, en complément de la vidéo.

## Préparer la remise

Après `mvn package`, ouvrir le sketch `Blob` dans Processing et utiliser **Fichier > Exporter l’application**, puis choisir Windows.

Assembler ensuite le dossier de l’équipe :

```text
Sonic/
├── Application/                 # Export Processing complet, avec Blob.exe
├── Sources/                     # src/, pom.xml et fichiers nécessaires au projet
├── Docs/
│   └── Presentation Diapositives.pdf
└── Files/
    └── VideoDemoJeux.mp4
```

La vidéo de remise doit durer **2 à 3 minutes**. Tester l’application exportée sur la machine de présentation, puis compresser le dossier en `Sonic.zip` et vérifier que l’archive fait moins de **500 Mo**.

## Limites connues

L’intégration utilise Euler explicite. La détection est discrète et les tests entre particules ont un coût quadratique. Le résolveur dispose d’un nombre limité d’itérations. La friction au sol est horizontale et le dessus du mur ne possède pas de modèle de friction tangentielle spécifique.
