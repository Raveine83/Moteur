# Moteur physique - Phase 2

Jeu de blob avec gravité, traînée, ressorts limités par câbles, collisions par
impulsions, séparation/fusion et compteur animé par un oscillateur amorti.
Le tir balistique de phase 1 reste dans `src/sketch/Jeu`.

La scène 1 est maintenant un niveau de réflexion : départ avec 8 particules,
4 particules dorées à recruter au contact, deux plaques à activer avec des
morceaux distincts, puis une porte et une sortie exigeant les 12 particules
réunies. Un mur de 120 px entre les plaques impose de sauter. La porte reste ouverte après 0,75 s d'activation simultanée.
La scène 9 conserve le blob libre de 12 particules pour la démonstration de base.

## Lancer

JDK 17 ou plus récent et Maven :

```powershell
mvn package
java -jar target/game-engine-1.0.0.jar
```

Ou ouvrir `src/sketch/Blob/Blob.pde` avec Processing 4 après `mvn package`.
Le build copie le moteur dans les dossiers `code` des deux sketches.
L'export Processing s'effectue depuis Fichier > Exporter l'application ; choisir
Windows pour la remise sur Windows. Le script `scripts/Exporter-Phase2.ps1`
automatise le build et prépare les dossiers Application/Sources/Docs/Files dans
`release`, sans écraser un export existant. Il utilise le Java fourni par Processing.
Le JAR exécutable constitue aussi un moyen
de lancer le jeu sur une machine équipée d'un Java compatible.

## Commandes

| Commande | Action |
|---|---|
| Gauche / droite | Déplacer le morceau sélectionné dans le niveau |
| Espace / haut | Sauter depuis le sol ou le dessus du mur (niveau 1) |
| Flèches | Appliquer une force dans les autres scènes |
| S | Séparer le morceau sélectionné |
| F | Fusionner avec un morceau proche (moins de 70 px entre deux particules) |
| Tab / clic sur une particule | Changer de morceau |
| P | Pause / reprise |
| R | Recommencer la scène |
| 1 | Niveau : récupérer, séparer, activer les plaques, réunir et sortir |
| 2 / 3 / 4 / 5 | Ressort classique / ancré / bungee / rigide factice |
| 6 / 7 | Tige / câble |
| 8 | Friction solide : A/D = petite force ; flèches = grande force |
| 9 | Blob libre de 12 particules, sans porte ni objets à récupérer |

Les liens passent progressivement du bleu à l'orange selon leur étirement.
Vert = morceau sélectionné ; bleu = autres morceaux ; doré = particule à recruter.
Les particules dorées ne peuvent pas être contrôlées avant leur recrutement.
Un anneau et un texte signalent chaque recrutement ou fusion.
Dans les scènes de tige/câble, la boule
mobile est sélectionnée au départ ; le point supérieur est fixe.

## Vérifier et préparer l'oral

```powershell
mvn test
```

La vérification du rendu écrit neuf images dans `target/verification-visuelle`.
Le document `Docs/Phase2-Formules-et-oral.md` relie chaque formule aux notes de
cours, à sa classe Java et à la démonstration correspondante.

Le guide distingue les formules du cours des choix numériques et des règles
de jeu. `Docs/Depassement-Phase2.md` décrit le niveau et une solution possible.
La présentation actualisée dans `Docs/Presentation-Phase2-Niveau` comporte
19 diapositives avec notes orales, dont le niveau, le mur et la formule du saut.
La scène 9 reste disponible pour la démonstration libre.
Avant la remise : vérifier l'export, personnaliser la présentation PDF,
enregistrer la vidéo phase 2 de 2-3 minutes et préparer le ZIP de moins de 500 Mo.
L'ancien PPTX et l'ancienne vidéo dans les dossiers d'origine concernent la phase 1.
