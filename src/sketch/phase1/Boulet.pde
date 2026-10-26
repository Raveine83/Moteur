class Boulet extends Projectile {
  Boulet(Vecteur3D position, Vecteur3D vitesse, double damping) {
    super(TYPE_BOULET, position, vitesse, damping);
  }

  void dessiner() {
    Vecteur3D p = particule.getPosition();
    fill(type.couleur);
    stroke(0);
    strokeWeight(1);
    ellipse((float) p.getX(), (float) p.getY(), type.taille, type.taille);
  }
}
