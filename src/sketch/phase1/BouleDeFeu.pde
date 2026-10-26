class BouleDeFeu extends Projectile {
  BouleDeFeu(Vecteur3D position, Vecteur3D vitesse, double damping) {
    super(TYPE_BOULE_FEU, position, vitesse, damping);
  }

  void dessiner() {
    Vecteur3D p = particule.getPosition();
    float x = (float) p.getX();
    float y = (float) p.getY();

    noStroke();
    fill(255, 160, 0, 110);
    ellipse(x, y, type.taille * 1.8f, type.taille * 1.8f);

    fill(type.couleur);
    ellipse(x, y, type.taille, type.taille);
  }
}
