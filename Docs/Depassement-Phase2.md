# Dépassement de la phase 2 : un niveau qui utilise le blob

## Objectif et commandes

La scène 1 commence avec 8 particules reliées et 4 particules dorées libres.
Gauche/droite déplacent uniquement le morceau sélectionné, par des forces.
Espace ou haut déclenche un saut depuis le sol ou le dessus du mur. Maintenir
la touche ne permet pas de voler ni de répéter le saut en l’air.
S sépare ce morceau ; Tab ou un clic choisit un autre morceau déjà recruté.
F réunit deux morceaux proches. R recommence entièrement le niveau, P met
en pause. La scène 9 conserve l'ancien blob libre de 12 particules.

1. Toucher les particules dorées pour créer un lien ressort/câble avec elles.
   F peut aussi recruter une particule à portée. Les quatre recrutements
   augmentent le nombre de membres et déclenchent le compteur amorti.
2. Poser deux morceaux distincts sur les plaques grises. Une particule recrutée
   du morceau doit toucher le sol sur la plaque avec une faible vitesse
   horizontale. Une particule dorée non recrutée ne peut pas l'activer.
3. Maintenir les deux plaques pendant 0,75 s. La jauge de la porte se remplit.
   La porte reste ensuite ouverte : on peut récupérer les morceaux stationnés.
4. Réunir les 12 particules et les amener entièrement dans la sortie verte.

Un seul groupe étiré sur les deux plaques ne suffit pas. Le niveau demande
une vraie séparation du graphe des liens. Quitter une plaque avant la fin
de la charge remet la jauge à zéro. La porte fermée couvre toute la hauteur
du terrain et possède une collision, donc on ne peut pas simplement la survoler.

## Une solution possible

Déplacer le blob vers la droite et recruter les quatre particules. Revenir
près de la plaque 2, puis séparer le blob. Franchir le mur entre les plaques
en avançant et en sautant ; refaire ce saut au retour. Choisir un morceau et le conduire
sur la plaque 1, en laissant l'autre sur la plaque 2. Attendre l'ouverture.
Ramener ensuite les morceaux l'un vers l'autre, utiliser F pour les réunir,
puis conduire tout le groupe dans la sortie. On peut aussi activer les plaques
avant de terminer la collecte.

## Ce qui dépasse les fonctionnalités minimales

- Un niveau avec une progression et une condition de victoire.
- Des particules à recruter qui modifient effectivement le graphe du blob.
- Une énigme qui utilise la séparation, la sélection et la fusion.
- Une porte avec collision cercle/rectangle et ouverture permanente.
- Un mur de 50 × 120 px entre les plaques, franchissable par un saut physique.
- Une couleur continue sur les liens, du bleu au repos à l'orange à la limite.
- Des anneaux de fusion et des textes de recrutement, avec une durée limitée.

Le résultat s'appuie sur les forces, contacts et câbles du moteur. Les seuils
de collecte, les plaques et l'ouverture de la porte sont des règles de jeu,
pas des lois physiques supplémentaires. L'appréciation de dépassement reste
à la discrétion de l'évaluateur.

## Classes et formules à expliquer à l'oral

`NiveauBlob` contient les règles et se met à jour après chaque sous-pas physique
de `Monde`. Ainsi, la charge des plaques ne dépend pas du nombre d'images.
Le rendu et les animations restent dans `JeuBlob`.

`FrictionSol` applique la friction de Coulomb sur l'axe tangent au sol, seulement
quand la sphère le touche. La réaction normale est estimée par la composante
de la force totale dirigée vers le sol. La friction statique compense la force
tangentielle tant que `|Fx| <= mu_s*N`. En glissement, elle s'oppose à la vitesse,
avec `|Ff| = mu_k*N`. Une limitation numérique empêche la friction de renverser
la vitesse à elle seule. Référence : chapitre 5, Dynamique linéaire, p. 2.
La friction est calculée après tous les générateurs, y compris les nouveaux
ressorts créés lors d'une fusion. Cela aide les morceaux non sélectionnés à
rester sur les plaques, sans fixer directement leurs positions.

`ContactRectangle` trouve le point du rectangle le plus proche du centre de
la sphère. Si cette distance est inférieure au rayon, il crée un `Contact`.
Si le centre est dans le rectangle, il utilise la face la plus proche.
La résolution réutilise les impulsions et la correction d'interpénétration
de la phase 2. Cette détection cercle/rectangle est un ajout au projet.

`LienBlob.getEtirement()` calcule `(longueur - repos)/(maximum - repos)`, borné
entre 0 et 1. `JeuBlob` utilise ce rapport pour interpoler la couleur et
l'épaisseur du lien. La couleur ne modifie pas la physique.

Le compteur continue d'utiliser la solution harmonique amortie décrite dans
`Phase2-Formules-et-oral.md`. Les anneaux de fusion sont des animations visuelles
à durée finie, pas des simulations de forces.

## Vérifications

Les tests couvrent le blocage par la porte, ses faces et ses coins, les centres
à l'intérieur, le recrutement, la sélection des membres, les plaques, le délai,
la réinitialisation de la charge et l'ouverture permanente. Un test termine
le niveau par des forces de commande, sans téléporter les particules.
Les tests vérifient aussi les faces du mur, son dessus, le saut avec appui,
l’absence de saut en l’air et de vol en maintenant haut. Les 152 tests passent.
La présentation actualisée dans `Presentation-Phase2-Niveau` intègre le socle,
le niveau et le mur ; ses notes proposent une démonstration de 10 minutes.

## Formule du saut et repères pour l’oral

Chapitre 6, Impulsions et collisions : J = Δp = m Δv, donc Δv = J/m.
`NiveauBlob.sauter()` choisit Δv = (0, −520, 0) px/s, puis transmet
J = m Δv à `Particule.appliquerImpulsion()`. Cette dernière multiplie J par
l’inverse de la masse pour modifier la vitesse. La gravité poursuit ensuite
la trajectoire avec l’intégrateur habituel. Les particules de masse infinie
ne sont pas accélérées. Le seuil d’appui, le délai de 0,2 s et la vitesse
de saut sont des choix de jouabilité ; la formule d’impulsion vient du cours.
