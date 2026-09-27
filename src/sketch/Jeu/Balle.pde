class Balle extends Projectile {
  Balle(Vecteur3D position, Vecteur3D vitesse, double damping) {
    super(TYPE_BALLE, position, vitesse, damping);
  }

  void dessiner() {
    Vecteur3D p = particule.getPosition();
    noStroke();
    fill(type.couleur);
    ellipse((float) p.getX(), (float) p.getY(), type.taille, type.taille);
  }
}
