# Moteur Physique — Jeu de tir balistique

Projet de l'équipe **Groupe 2 - Sonic** — Phase #1 : moteur physique (particules et intégrateur).

## Contexte

Cette phase du cours consiste à créer un moteur physique simple de gestion de particules, qui servira de fondation aux phases suivantes. Le projet contient :

- une classe `Vecteur3D` (norme, normalisation, addition, soustraction, produits scalaire/vectoriel, etc.) ;
- une classe `Particule` avec un intégrateur d'Euler (`integrer(temps)`) qui met à jour la position et la vitesse à chaque frame, en tenant compte du frottement (damping) ;
- un petit jeu de tir balistique en Processing qui exploite ce moteur : on choisit un projectile (balle, boulet, laser ou boule de feu), on tire sur des cibles, et on peut modifier la gravité et le frottement de l'air en direct.

Le moteur est écrit en Java (Maven, tests JUnit). Le jeu est un sketch Processing qui importe le moteur sous forme de jar.

## Prérequis

- JDK 17
- Maven
- [Processing 4](https://processing.org/download)

## Lancer le jeu

1. Compiler le moteur et générer le jar (les tests sont exécutés au passage) :

   ```bash
   mvn package
   ```

   Le jar est copié automatiquement dans `src/sketch/Jeu/code/game-engine.jar`. Ce fichier n'est pas versionné : il faut donc refaire cette étape après un `git clone` ou après une modification du moteur.

2. Ouvrir `src/sketch/Jeu/Jeu.pde` dans Processing, puis cliquer sur **Run**.

Pour lancer uniquement les tests unitaires : `mvn test`.

## Contrôles

| Action | Commande |
|---|---|
| Choisir le projectile | `1` Balle, `2` Boulet, `3` Laser, `4` Boule de feu |
| Viser | Déplacer la souris (le canon suit le curseur) |
| Tirer | Clic gauche |
| Augmenter / diminuer le damping | Flèche haut / bas |
| Augmenter / diminuer la gravité | Flèche droite / gauche |

Le but est de toucher les 4 cibles (100 points chacune). Le message « VICTOIRE ! » s'affiche quand toutes sont touchées. Le HUD affiche en temps réel la durée de la dernière frame (`dt`), le score, le projectile sélectionné, le damping et la gravité.

Les réglages de damping et de gravité s'appliquent aux **nouveaux** tirs uniquement : les projectiles déjà en vol gardent leurs valeurs.

## Comment ça fonctionne

### Structure du projet

```
src/main/java/com/team/game/   Moteur (Java) : Vecteur3D, Particule
src/test/java/com/team/game/   Tests unitaires JUnit
src/sketch/Jeu/                Jeu (Processing) : Jeu.pde + une classe par onglet
pom.xml                        Build Maven (génère le jar du moteur)
.github/workflows/ci.yml       CI : compilation, tests et livrable
Docs/, Files/                  Diapositives et vidéo de démonstration
```

### Le moteur

Chaque frame, `Particule.integrer(dt)` applique :

```
position += vitesse * dt
vitesse  += acceleration * dt
vitesse  *= damping ^ dt
```

- `dt` est la durée réelle de la frame précédente, mesurée avec `millis()` dans `draw()`. Elle est appliquée à l'intégration de la frame courante.
- Le damping est élevé à la puissance `dt` pour que le frottement ne dépende pas du nombre d'images par seconde. Une valeur proche de 1 donne un frottement négligeable.
- La particule stocke l'inverse de la masse (`inverseMasse`), avec ses accesseurs `getInverseMasse` / `setInverseMasse`.

### Le jeu

Les classes du jeu sont des onglets Processing dans `src/sketch/Jeu/` :

| Fichier | Rôle |
|---|---|
| `Jeu.pde` | Boucle principale, calcul de `dt`, entrées clavier/souris, HUD, score |
| `TypeProjectile.pde` | Paramètres d'un type de projectile : masse, vitesse initiale, couleur, taille |
| `Projectile.pde` | Classe abstraite : encapsule une `Particule`, trajectoire, sortie d'écran, collision |
| `Balle.pde`, `Boulet.pde`, `Laser.pde`, `BouleDeFeu.pde` | Sous-classes de `Projectile`, qui ne redéfinissent que le rendu |
| `Cible.pde` | Cible à toucher |

Au clic, la direction de tir est le vecteur du canon vers la souris, normalisé puis multiplié par la vitesse initiale du projectile choisi. Une `Particule` est créée avec la gravité comme accélération constante et le damping courant. Les unités sont des pixels (px, px/s, px/s²) et non des unités SI.

### Tests

`Vecteur3DTest` et `ParticuleTest` couvrent le moteur. Le code du sketch (`.pde`) n'est pas testé automatiquement : Processing le compile en une classe dérivée de `PApplet`, ce qui empêche de le tester avec JUnit directement.

### Intégration continue

Le workflow GitHub Actions compile et exécute les tests à chaque push. Sur `master` et `dev`, il assemble aussi le livrable selon la structure demandée (`Application`, `Sources`, `Docs`, `Files`).
