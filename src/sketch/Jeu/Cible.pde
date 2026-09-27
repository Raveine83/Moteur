class Cible {
  Vecteur3D position;
  float rayon;
  boolean touchee;

  Cible(float x, float y, float rayon) {
    this.position = new Vecteur3D(x, y, 0);
    this.rayon = rayon;
    this.touchee = false;
  }

  void dessiner() {
    if (touchee) {
      fill(120);
      stroke(70);
    } else {
      fill(200, 40, 40);
      stroke(120, 0, 0);
    }

    strokeWeight(2);
    ellipse((float) position.getX(), (float) position.getY(), rayon * 2, rayon * 2);

    noStroke();
    fill(255);
    ellipse((float) position.getX(), (float) position.getY(), rayon * 0.6f, rayon * 0.6f);
  }
}
