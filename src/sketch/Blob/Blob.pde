import com.team.game.JeuBlob;

JeuBlob jeu;

void settings() { size(1100, 700); }
void setup() {
  surface.setTitle("Phase 2 - Blob");
  frameRate(60);
  jeu = new JeuBlob(this);
}
void draw() {
  if (!focused) jeu.perdreFocus();
  jeu.dessiner();
}
void keyPressed() {
  jeu.touche(key, keyCode, true);
  if (key == ESC) key = 0;
}
void keyReleased() { jeu.touche(key, keyCode, false); }
void mousePressed() { jeu.cliquer(mouseX, mouseY); }
