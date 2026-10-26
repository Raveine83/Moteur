import com.team.game.Vecteur3D;
import com.team.game.Particule;


double gravite;
final double GRAVITE_MIN = 0.0;
final double GRAVITE_MAX = 1200.0;
final double GRAVITE_PAS = 20.0;

TypeProjectile TYPE_BALLE;
TypeProjectile TYPE_BOULET;
TypeProjectile TYPE_LASER;
TypeProjectile TYPE_BOULE_FEU;

TypeProjectile typeSelectionne;

ArrayList<Projectile> projectiles;
ArrayList<Cible> cibles;

float canonX, canonY;

int score;
long dernierTemps;
float dt;
boolean partieGagnee;


double dampingActuel;
final double DAMPING_MIN = 0.80;
final double DAMPING_MAX = 1.0;
final double DAMPING_PAS = 0.01;

void setup() {
  size(900, 600);

  // Vitesse initiale et masse propres à chaque type de projectile,
  TYPE_BALLE     = new TypeProjectile("Balle",        1.0, 500, color(230, 200, 40), 8);
  TYPE_BOULET    = new TypeProjectile("Boulet",        8.0, 350, color(70, 70, 70),   16);
  TYPE_LASER     = new TypeProjectile("Laser",         0.05, 900, color(255, 30, 30),  4);
  TYPE_BOULE_FEU = new TypeProjectile("Boule de feu",  3.0, 420, color(255, 120, 0),  12);

  typeSelectionne = TYPE_BALLE;

  projectiles = new ArrayList<Projectile>();
  cibles = new ArrayList<Cible>();

  canonX = 60;
  canonY = height - 60;

  genererCibles();

  score = 0;
  partieGagnee = false;
  dernierTemps = millis();

  dampingActuel = 0.999;
  gravite = 400.0;
}

void genererCibles() {
  cibles.add(new Cible(420, height - 80, 25));
  cibles.add(new Cible(580, height - 220, 22));
  cibles.add(new Cible(730, height - 360, 20));
  cibles.add(new Cible(850, height - 130, 18));
}

void draw() {
  long maintenant = millis();
  dt = (maintenant - dernierTemps) / 1000.0;
  dernierTemps = maintenant;

  background(135, 206, 235);

  dessinerSol();
  dessinerCanon();

  for (Cible cible : cibles) {
    cible.dessiner();
  }

  for (int i = projectiles.size() - 1; i >= 0; i--) {
    Projectile projectile = projectiles.get(i);
    projectile.mettreAJour(dt);

    for (Cible cible : cibles) {
      if (!cible.touchee && projectile.collisionAvec(cible)) {
        cible.touchee = true;
        projectile.actif = false;
        score += 100;
      }
    }

    projectile.dessinerTrajectoire();
    projectile.dessiner();

    if (!projectile.actif) {
      projectiles.remove(i);
    }
  }

  dessinerHUD();

  if (!partieGagnee && toutesCiblesTouchees()) {
    partieGagnee = true;
  }

  if (partieGagnee) {
    dessinerVictoire();
  }
}

boolean toutesCiblesTouchees() {
  for (Cible cible : cibles) {
    if (!cible.touchee) {
      return false;
    }
  }
  return true;
}

void dessinerSol() {
  noStroke();
  fill(90, 160, 90);
  rect(0, height - 30, width, 30);
}

void dessinerCanon() {
  float dirX = mouseX - canonX;
  float dirY = mouseY - canonY;
  float norme = sqrt(dirX * dirX + dirY * dirY);
  if (norme == 0) {
    norme = 1;
  }

  float longueurCanon = 30;

  stroke(50);
  strokeWeight(6);
  line(canonX, canonY, canonX + dirX / norme * longueurCanon, canonY + dirY / norme * longueurCanon);

  noStroke();
  fill(50);
  ellipse(canonX, canonY, 24, 24);
}

void dessinerHUD() {
  fill(0);
  textSize(14);
  textAlign(LEFT, TOP);
  text("dt: " + nf(dt, 1, 4) + " s", 10, 10);
  text("Score: " + score, 10, 30);
  text("Projectile: " + typeSelectionne.nom, 10, 50);
  text("Frottement (damping): " + nf((float) dampingActuel, 1, 3) + "  [Fleches haut/bas pour ajuster]", 10, 70);
  text("Gravite: " + nf((float) gravite, 1, 0) + " px/s^2  [Fleches gauche/droite pour ajuster]", 10, 90);
  text("[1] Balle  [2] Boulet  [3] Laser  [4] Boule de feu  -  Clic pour tirer", 10, 110);
}

void dessinerVictoire() {
  fill(0, 130, 0);
  textSize(32);
  textAlign(CENTER, CENTER);
  text("VICTOIRE !", width / 2.0, height / 2.0);
}

void keyPressed() {
  if (key == '1') {
    typeSelectionne = TYPE_BALLE;
  } else if (key == '2') {
    typeSelectionne = TYPE_BOULET;
  } else if (key == '3') {
    typeSelectionne = TYPE_LASER;
  } else if (key == '4') {
    typeSelectionne = TYPE_BOULE_FEU;
  } else if (key == CODED && keyCode == UP) {
    dampingActuel = Math.min(DAMPING_MAX, dampingActuel + DAMPING_PAS);
  } else if (key == CODED && keyCode == DOWN) {
    dampingActuel = Math.max(DAMPING_MIN, dampingActuel - DAMPING_PAS);
  } else if (key == CODED && keyCode == RIGHT) {
    gravite = Math.min(GRAVITE_MAX, gravite + GRAVITE_PAS);
  } else if (key == CODED && keyCode == LEFT) {
    gravite = Math.max(GRAVITE_MIN, gravite - GRAVITE_PAS);
  }
}

void mousePressed() {
  if (partieGagnee) {
    return;
  }

  float dirX = mouseX - canonX;
  float dirY = mouseY - canonY;
  float norme = sqrt(dirX * dirX + dirY * dirY);
  if (norme == 0) {
    return;
  }

  double vx = (dirX / norme) * typeSelectionne.vitesseInitiale;
  double vy = (dirY / norme) * typeSelectionne.vitesseInitiale;

  Vecteur3D position = new Vecteur3D(canonX, canonY, 0);
  Vecteur3D vitesse = new Vecteur3D(vx, vy, 0);

  Projectile projectile;
  if (typeSelectionne == TYPE_BALLE) {
    projectile = new Balle(position, vitesse, dampingActuel);
  } else if (typeSelectionne == TYPE_BOULET) {
    projectile = new Boulet(position, vitesse, dampingActuel);
  } else if (typeSelectionne == TYPE_LASER) {
    projectile = new Laser(position, vitesse, dampingActuel);
  } else {
    projectile = new BouleDeFeu(position, vitesse, dampingActuel);
  }

  projectiles.add(projectile);
}
