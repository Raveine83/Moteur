/**
Classe Vecteur3D
Représente un vecteur à 3 composantes (x, y, z)
Fournit les opérations vectorielles courantes
*/
class Vecteur3D {
 
  private float x;
  private float y;
  private float z;
 
 /* ---------- CONSTRUCTEURS ---------- */
 
 public Vecteur3D() {
   this.setVecteur(0, 0, 0);
 }
 
 public Vecteur3D(float x, float y, float z) {
   this.setVecteur(x, y, z);
 }
 
 /* ---------- NORMES ---------- */
 
 public float normeCarre() {
   return (x*x) + (y*y) + (z*z);
 }
 
 public float norme() {
   return sqrt(normeCarre());
 }
 
 /* ---------- NORMALISATION ---------- */
 
 // Normalise le vecteur courant
 public Vecteur3D normaliser() {
   float n = norme();
   if (n != 0) {
      this.x /= n;
      this.y /= n;
      this.z /= n;
   }
   return this;
 }
 
 /* ---------- OPERATIONS ---------- */
 
 // Additionne le vecteur courant avec un autre vecteur
 public Vecteur3D additionner(Vecteur3D v) {
   this.x += v.x;
   this.y += v.y;
   this.z += v.z;
   return this;
 }
 
 // Soustrait le vecteur courant avec un autre vecteur
 public Vecteur3D soustraire(Vecteur3D v) {
   this.x -= v.x;
   this.y -= v.y;
   this.z -= v.z;
   return this;
 }
 
 // Multiplication par un scalaire
 public Vecteur3D multiplierParUnScalaire(float s) {
   this.x *= s;
   this.y *= s;
   this.z *= s;
   return this;
 }
 
 // Produit par composantes
 public Vecteur3D produitParComposantes(Vecteur3D v) {
   this.x *= v.x;
   this.y *= v.y;
   this.z *= v.z;
   return this;
 }
 
 // Produit vectoriel
 public Vecteur3D produitVectoriel(Vecteur3D v) {
   return new Vecteur3D(
     this.y * v.z - this.z * v.y,
     this.z * v.x - this.x * v.z,
     this.x * v.y - this.y * v.x);
 }
 
 // Produit scalaire
 public float produitScalaire(Vecteur3D v) {
   return (this.x * v.x) + (this.y * v.y) + (this.z * v.z);
 }
 
 // Division par un scalaire
 public Vecteur3D diviserParUnScalaire(float s) {
   if (s != 0) {
     x /= s;
     y /= s;
     z /= s;
   }
   return this;
 }
 
 public Vecteur3D negation() {
   this.x = - this.x;
   this.y = - this.y;
   this.z = - this.z;
   return this;
 }
 
 /* ---------- GETTERS ---------- */
 
 public float getX() {
   return this.x;
 }
 
 public float getY() {
   return this.y;
 }
 
 public float getZ() {
   return this.z;
 }
 
 /* ---------- SETTERS ---------- */
 
 private void setX(float x) {
   this.x = x;
 }
 
 private void setY(float y) {
   this.y = y;
 }
 
 private void setZ(float z) {
   this.z = z;
 }
 
 private void setVecteur(float x, float y, float z) {
   this.setX(x);
   this.setY(y);
   this.setZ(z);
 }
 
 /* ---------- UTILITAIRES ---------- */
 
 public boolean estNul() {
   return this.x == 0 && this.y == 0 && this.z == 0;
 }
 
 public boolean equals(Vecteur3D v) {
   return this.x == v.x && this.y == v.y && this.z == v.z;
 }
 
 public String toString() {
   return "(" + this.x + ", " + this.y + ", " + this.z + ")";
 }
}
