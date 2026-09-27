/**
 * Regroupe les paramètres propres à un type de projectile
 * (masse, vitesse initiale, rendu). Le damping n'est pas fixé ici : il est
 * réglable en jeu (voir dampingActuel dans Jeu.pde) pour pouvoir jouer avec
 * le frottement de l'air.
 */
class TypeProjectile {
  final String nom;
  final double masse;
  final double vitesseInitiale;
  final int couleur;
  final float taille;

  TypeProjectile(String nom, double masse, double vitesseInitiale, int couleur, float taille) {
    this.nom = nom;
    this.masse = masse;
    this.vitesseInitiale = vitesseInitiale;
    this.couleur = couleur;
    this.taille = taille;
  }
}
