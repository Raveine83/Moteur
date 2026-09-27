/**
 * Représente un projectile en vol : encapsule une Particule du moteur
 * physique et gère la trajectoire, la sortie d'écran et les collisions.
 * Le rendu visuel est laissé aux sous-classes (Balle, Boulet, Laser, BouleDeFeu).
 */
abstract class Projectile {
  TypeProjectile type;
  Particule particule;
  ArrayList<Vecteur3D> trajectoire;
  boolean actif;

  Projectile(TypeProjectile type, Vecteur3D position, Vecteur3D vitesse, double damping) {
    this.type = type;

    Vecteur3D acceleration = new Vecteur3D(0, gravite, 0);
    this.particule = new Particule(position, vitesse, acceleration, type.masse, damping);

    this.trajectoire = new ArrayList<Vecteur3D>();
    this.actif = true;
  }

  void mettreAJour(double dt) {
    particule.integrer(dt);

    Vecteur3D p = particule.getPosition();
    trajectoire.add(new Vecteur3D(p.getX(), p.getY(), p.getZ()));

    if (p.getX() < -50 || p.getX() > width + 50 || p.getY() > height + 50) {
      actif = false;
    }
  }

  void dessinerTrajectoire() {
    noFill();
    stroke(type.couleur, 120);
    strokeWeight(1);
    beginShape();
    for (Vecteur3D p : trajectoire) {
      vertex((float) p.getX(), (float) p.getY());
    }
    endShape();
  }

  boolean collisionAvec(Cible cible) {
    Vecteur3D p = particule.getPosition();
    float dx = (float) p.getX() - (float) cible.position.getX();
    float dy = (float) p.getY() - (float) cible.position.getY();
    float distance = sqrt(dx * dx + dy * dy);
    return distance <= cible.rayon + (type.taille / 2.0f);
  }

  abstract void dessiner();
}
