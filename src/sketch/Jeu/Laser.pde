/**
 * Rendu en faisceau (segment aligné sur le vecteur vitesse) plutôt qu'en
 * cercle, pour bien montrer la trajectoire quasi rectiligne à haute vitesse.
 */
class Laser extends Projectile {
  Laser(Vecteur3D position, Vecteur3D vitesse, double damping) {
    super(TYPE_LASER, position, vitesse, damping);
  }

  void dessiner() {
    Vecteur3D p = particule.getPosition();
    Vecteur3D v = particule.getVitesse();

    float vx = (float) v.getX();
    float vy = (float) v.getY();
    float norme = sqrt(vx * vx + vy * vy);

    float longueur = 18;
    float dx = (norme == 0) ? 0 : vx / norme * longueur;
    float dy = (norme == 0) ? 0 : vy / norme * longueur;

    stroke(type.couleur);
    strokeWeight(type.taille / 2.0f);
    line((float) p.getX() - dx, (float) p.getY() - dy, (float) p.getX(), (float) p.getY());
  }
}
