# Phase 2 - Formules utilisées et préparation de l'oral

## Repères et unités

Les pages indiquées ci-dessous sont les numéros des pages PDF, comptées depuis 1,
des notes de cours fournies. Les manuscrits utilisent parfois une convention de
normale opposée à celle du code : les signes des impulsions changent ensemble.
Les formules restent équivalentes.

Le jeu est en 2D, avec des `Vecteur3D` dont z = 0. x augmente vers la droite et y
vers le bas. Les distances sont en pixels, les temps en secondes, les masses en
unités conventionnelles. La gravité vaut (0, 360, 0) px/s². Ce choix est une
échelle de jeu, pas une valeur de 360 m/s². Le rayon visible est aussi celui des
collisions. L'inverse de masse nul désigne un objet immobile.

## 1. Position, vitesse et intégration

**Sources : chapitre 3, p. 3-5 ; chapitre 4, p. 4-7.**

`v = dp/dt`, `a = dv/dt`.

Euler explicite, dans `Particule.integrer(dt)` :

```text
a_totale = a_constante + (somme F) * inverseMasse
p_suivante = p + v * dt
v_suivante = (v + a_totale * dt) * damping^dt
```

La position utilise la vitesse du début du pas : c'est bien Euler explicite.
Le facteur `damping^dt` vient du chapitre 4 p. 7 et évite un amortissement
directement dépendant du nombre d'images affichées.

`JeuBlob.dessiner()` mesure la durée réelle de la frame précédente avec
`System.nanoTime()`. `Monde.avancer()` accumule cette durée et appelle la physique
par sous-pas de 1/240 s. À 60 images/s, il y a généralement 4 sous-pas par image.
Ce sous-découpage est un choix numérique pour les ressorts et les collisions,
pas une nouvelle loi physique. Une frame de plus de 0,1 s est plafonnée afin
d'éviter une énorme avancée après un arrêt du débogueur ou une fenêtre déplacée.
Cela signifie une perte volontaire de temps simulé lors d'un blocage prolongé.

## 2. Newton, somme des forces et gravité

**Sources : chapitre 5, p. 1-5 ; chapitre 4, p. 2-3.**

```text
F_resultante = somme des forces
a = F_resultante / m
F_gravite = m * g
```

- `GenerateurForce` : interface commune à toutes les forces.
- `RegistreForces.mettreAJourForces()` : appelle les générateurs enregistrés.
- `Particule.ajouterForce()` : addition vectorielle des forces.
- `Particule.integrer()` : transforme cette somme en accélération et vide
  l'accumulateur à la fin du pas, sans supprimer les inscriptions du registre.
- `Gravite.mettreAJourForce()` : ajoute `m*g` ; la division par m redonne g.
- `Monde.avancer()` : commandes du joueur sous forme de forces, jamais une
  modification directe de la position pour déplacer un blob.

Phrase possible : « Chaque générateur ajoute sa contribution. L'intégrateur ne
connaît pas l'origine des forces, seulement leur somme. »

## 3. Friction

**Source : chapitre 5, p. 2-3.**

Traînée, dans `FrictionTrainee.mettreAJourForce()` :

```text
F = -(k1 * norme(v) + k2 * norme(v)^2) * v_unitaire
```

Elle agit sur toutes les particules mobiles du jeu. Le damping de l'intégrateur
reste proche de 1 ; c'est principalement le générateur qui représente ici la
friction du fluide.

Friction solide, dans `FrictionSolide.mettreAJourForce()`, démonstration [8] :

```text
au repos : norme(F_appliquee) <= mu_s * N => la friction annule la force
en mouvement : F_friction = -mu_k * N * v_unitaire
```

La scène [8] impose un mouvement horizontal. N = 360, mu_s = 0,6 et mu_k = 0,4.
Le seuil statique est donc 216. A/D appliquent une force de 130 : rien ne bouge.
Les flèches appliquent une force de 650 : le bloc se met en mouvement.
Le générateur est enregistré après les autres forces pour lire leur somme.
Une limitation numérique empêche la friction de renverser à elle seule la
vitesse en un seul pas. Ce modèle n'est pas un solveur de friction tangentielle
général pour n'importe quel plan incliné.

## 4. Ressorts et troisième loi de Newton

**Source : chapitre 5, p. 2 et p. 5-8.**

```text
d = position_A - position_B
l = norme(d)
F_A = -k * (l - l0) * d/l
F_B = -F_A
```

- `Ressort` : étirement et compression entre deux particules, scène [2].
- `RessortAncre` : même loi avec un point fixe, scène [3].
- `RessortBungee` : même force seulement si l > l0, scène [4].
- `RessortRigideFactice` : utilise la solution analytique amortie pour calculer
  une vitesse cible, puis `F = m * (v_cible - v) / dt`, scène [5]. Ce générateur
  préexistant ne traite que le régime sous-amorti ; si son discriminant est
  négatif ou nul, il ne produit pas de force. La scène utilise k=240, c=8, m=1.

`LienBlob` enregistre deux ressorts, un par extrémité, comme demandé dans les
notes p. 6. Il ajoute un `ContactCable` dont la longueur maximale vaut 1,55*l0.
Le ressort fournit la force élastique ; le câble impose la limite. Ce n'est pas
un bungee : le blob conserve son comportement de ressort en compression.
La superposition ressort/câble est le choix d'implémentation de la limite
d'élasticité demandée par l'énoncé.

## 5. Détection et interpénétration

**Source : chapitre 6, p. 5-6.**

`DetecteurCollisions` examine toutes les paires : complexité O(n²).

```text
collision si distance(A,B) < rayon_A + rayon_B
profondeur = rayon_A + rayon_B - distance(A,B)
normale = (position_A - position_B) / distance(A,B)
```

`ContactPlan` utilise la distance signée à un plan, moins le rayon. Une distance
nulle génère aussi un contact, pour une particule déjà posée sur le sol.

`Contact.resoudreInterpenetration()` répartit la correction selon les inverses
de masse wA et wB :

```text
deplacement_A = normale * profondeur * wA/(wA+wB)
deplacement_B = -normale * profondeur * wB/(wA+wB)
```

L'objet le plus léger bouge davantage. Un objet de masse infinie ne bouge pas.
Si deux centres coïncident, le détecteur choisit une normale verticale par défaut.

## 6. Impulsions, restitution et contacts au repos

**Source : chapitre 6, p. 1-4 et p. 7.**

Quantité de mouvement `p = m*v` ; une impulsion change instantanément p.
Dans le code, n pointe de B vers A :

```text
v_sep = (v_A - v_B) . n
v_sep_cible = -e * v_sep
j = (v_sep_cible - v_sep) / (wA+wB)
v_A += j * wA * n
v_B -= j * wB * n
```

`Contact.resoudreVitesse()` effectue ces calculs. Si v_sep > 0, les particules
s'éloignent et aucune impulsion de rapprochement n'est nécessaire.
e=0 correspond à un choc sans restitution ; e=1 à un choc élastique.
Le jeu utilise une faible restitution e=0,15 pour les contacts sphère/sol et
sphère/sphère, et e=0 pour les câbles des blobs.

Pour le repos, on projette l'accélération relative sur la normale :

```text
v_due_acceleration = (a_A - a_B) . n * dt
```

L'accélération utilisée est celle de la dernière intégration, y compris les
générateurs de forces (`getAccelerationDerniereFrame`). Le code annule la
restitution pour les petits impacts jusqu'à trois pas d'accélération : ce facteur
3 est une tolérance pratique du solveur existant, pas une constante du cours.
Cela évite que la gravité produise des rebonds permanents.

`ResolveurContacts` applique des impulsions, puis corrige les positions ; il
limite par défaut chaque boucle à deux fois le nombre de contacts (p. 7).
`GestionnaireCollisions` redétecte jusqu'à quatre fois, car une correction peut
créer un autre contact. L'ordre et le nombre de passes sont des compromis
numériques, sans prétendre à une solution exacte de tous les contacts simultanés.

## 7. Tiges et câbles

**Source : chapitre 6, p. 8.**

- `ContactCable` : pas d'effet si l < l_max ; contact de rappel à la limite ou
  au-delà. La normale inversée transforme l'éloignement en rapprochement à
  résoudre. Démonstration [7].
- `ContactTige` : contacts qui rapprochent si l > l0 et qui écartent si l < l0.
  Restitution nulle. Démonstration [6]. Une coïncidence exacte des deux centres
  reste un cas dégénéré du générateur existant : aucune direction n'y est définie.

## 8. Rebond amorti du compteur

**Source : chapitre 5, p. 6-8, solution sous-amortie de la p. 8.**

Dans `OscillateurAmorti.getDeplacement()` :

```text
m*x'' + c*x' + k*x = 0
omega0 = sqrt(k/m)
zeta = c/(2*m*omega0)
lambda = zeta*omega0 = c/(2*m)
omegaD = sqrt(omega0² - lambda²)
x(t) = exp(-lambda*t) * [K1*cos(omegaD*t) + K2*sin(omegaD*t)]
K1 = x(0)
K2 = (lambda*x(0) + v(0))/omegaD
```

Paramètres : m=1, k=196, c=9, x(0)=-22 px, v(0)=0. Donc omega0=14 rad/s,
lambda=4,5/s et zeta environ 0,321. La valeur traverse sa position d'équilibre
et son amplitude décroît exponentiellement.
`JeuBlob.verifierCompteur()` redéclenche l'oscillation seulement quand le nombre
de particules du morceau sélectionné change, y compris après une sélection.
Le compteur inclut la particule de référence : il affiche 12 pour le blob complet.

## 9. Séparation et fusion : logique de jeu

`Monde.getGroupes()` calcule les composantes connexes du graphe des liens.
La séparation [S] coupe les liens traversant une partition gauche/droite du
morceau sélectionné et applique brièvement des forces opposées. La fusion [F]
crée un pont ressort/câble avec le morceau le plus proche, si deux particules
sont à moins de 70 px. Les particules ne sont pas téléportées. Tab ou un clic
permet de choisir le morceau à déplacer.

Ce sont des règles de jeu, pas des formules du cours. Une collision inélastique
ne crée pas à elle seule des liens permanents entre les blobs.

## Présentation actualisée et saut

La présentation complète est dans `Presentation-Phase2-Niveau` : 16 diapositives
principales pour 10 minutes (dont 125 s de démonstration), puis 3 annexes.
Les notes de chaque diapositive précisent les formules et les passages du cours.
La démonstration commence par le niveau 1 : collecte, saut du mur, séparation,
plaques, fusion et sortie ; elle continue par les ressorts, contraintes et friction.

Le saut utilise J = m Δv, puis Δv = J/m (chapitre 6, impulsions).
`NiveauBlob.sauter()` prépare l’impulsion verticale de chaque membre sélectionné ;
`Particule.appliquerImpulsion()` l’applique à sa vitesse. Le mur est un
`ContactRectangle`. L’appui et l’anti-répétition sont des règles de jeu.
Voir `Depassement-Phase2.md` pour les commandes et une solution du niveau.

Les tests valident notamment un blob simulé pendant 20 s, les limites des câbles,
la fusion/séparation, le repos sous gravité générée, l'indépendance entre 60 et
144 images/s et l'accessibilité de la condition de victoire.

## Remise : éléments à finaliser humainement

La phase 2 vaut 25 % de la session. Son barème est 80 % oral/démo, 10 % respect
des consignes, 10 % dépassement. Le niveau de dépassement est décrit dans
`Depassement-Phase2.md` ; la scène 9 conserve la démonstration libre d'origine.
Personnaliser les diapositives PDF et enregistrer une vidéo de 2-3 minutes
montrant cette version phase 2 ; les fichiers de phase 1 ne les remplacent pas.
L'archive finale doit faire moins de 500 Mo et contenir Application, Sources,
Docs et Files sous le dossier de l'équipe. Tester l'application sur la machine
de présentation. Ce guide aide l'explication, mais n'est pas la présentation PDF.
