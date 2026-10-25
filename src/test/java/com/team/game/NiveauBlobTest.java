package com.team.game;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class NiveauBlobTest {
    private void attendre(NiveauBlob n, double secondes) {
        n.getMonde().commander(0,0);
        for (int i=0;i<(int)Math.ceil(secondes*60);i++) { n.getMonde().avancer(1.0/60); }
    }
    private void poser(List<ParticuleSphere> groupe, double x) {
        double cx=groupe.stream().mapToDouble(p->p.getPosition().getX()).average().orElseThrow();
        double bas=groupe.stream().mapToDouble(p->p.getPosition().getY()+p.getRayon()).max().orElseThrow();
        for(ParticuleSphere p:groupe) {
            p.getPosition().additionner(new Vecteur3D(x-cx,Monde.SOL-bas,0)); p.setVitesse(new Vecteur3D());
        }
    }
    private NiveauBlob ouvrir() {
        NiveauBlob n=new NiveauBlob();n.getMonde().separer();
        List<List<ParticuleSphere>> g=n.groupesRecrutes();poser(g.get(0),215);poser(g.get(1),630);
        attendre(n,1.5);assertTrue(n.isPorteOuverte(), "Les deux morceaux posés doivent ouvrir la porte");
        return n;
    }
    @Test void departHuitMembresQuatreCollectiblesEtSelectionProtegee() {
        NiveauBlob n=new NiveauBlob();
        assertEquals(8,n.getMonde().getGroupeSelectionne().size());assertEquals(0,n.getNombreRecuperees());
        assertEquals(12,n.getMonde().getParticules().size());assertEquals(1,n.groupesRecrutes().size());
        ParticuleSphere libre=n.getMonde().getParticules().get(8);
        n.getMonde().selectionner(libre.getPosition().getX(),libre.getPosition().getY());
        assertEquals(8,n.getMonde().getGroupeSelectionne().size());
        n.getMonde().selectionSuivante();assertEquals(8,n.getMonde().getGroupeSelectionne().size());
    }
    @Test void collecteAuContactEtEvenementConsommeUneSeuleFois() {
        NiveauBlob n=new NiveauBlob();ParticuleSphere membre=n.getMonde().getParticules().get(0);
        ParticuleSphere libre=n.getMonde().getParticules().get(8);
        libre.setPosition(membre.getPosition().copie().additionner(new Vecteur3D(-28,0,0)));
        n.getMonde().avancer(Monde.PAS);
        assertEquals(9,n.getMonde().getGroupeSelectionne().size());assertEquals(1,n.getNombreRecuperees());
        assertEquals(1,n.consommerFusions().size());assertTrue(n.consommerFusions().isEmpty());
    }
    @Test void porteBloqueLeBlobEtNePeutEtreContourneeParLeHaut() {
        NiveauBlob n=new NiveauBlob();poser(n.getMonde().getGroupeSelectionne(),630);
        n.getMonde().commander(1,0);
        for(int i=0;i<800;i++) {n.getMonde().avancer(1.0/60);}
        assertFalse(n.isPorteOuverte());assertFalse(n.isGagne());
        for(ParticuleSphere p:n.getMonde().getParticules()) {
            assertTrue(p.getPosition().getX()+p.getRayon()<=NiveauBlob.PORTE_X+0.2);
        }
        n.getMonde().commander(1,-1);
        for(int i=0;i<300;i++){n.getMonde().avancer(1.0/60);}
        for(ParticuleSphere p:n.getMonde().getParticules()) {
            assertTrue(p.getPosition().getX()+p.getRayon()<=NiveauBlob.PORTE_X+0.2);
        }
    }
    @Test void deuxMorceauxDistinctsEtMaintienNecessairesPuisPorteVerrouilleeOuverte() {
        NiveauBlob n=new NiveauBlob();attendre(n,2);assertFalse(n.isPorteOuverte());
        n.getMonde().separer();List<List<ParticuleSphere>> g=n.groupesRecrutes();
        poser(g.get(0),215);poser(g.get(1),630);
        attendre(n,0.25);assertFalse(n.isPorteOuverte());assertTrue(n.getCharge()>0);
        attendre(n,1);assertTrue(n.isPorteOuverte());
        poser(g.get(0),380);poser(g.get(1),730);attendre(n,1);
        assertTrue(n.isPorteOuverte());assertFalse(n.isGagne());
    }
    @Test void lesParticulesDoreesNePeuventPasActiverLesPlaques() {
        NiveauBlob n=new NiveauBlob();poser(n.getMonde().getGroupeSelectionne(),215);
        ParticuleSphere libre=n.getMonde().getParticules().get(8);
        libre.setPosition(new Vecteur3D(630,Monde.SOL-13,0));attendre(n,2);
        assertTrue(n.isPlaqueOccupee(0));assertFalse(n.isPlaqueOccupee(1));assertFalse(n.isPorteOuverte());
    }
    @Test void unSeulGroupeSurLesDeuxPlaquesNeSuffitPas() {
        NiveauBlob n=new NiveauBlob();n.getMonde().separer();
        List<List<ParticuleSphere>> g=n.groupesRecrutes();poser(g.get(0),215);poser(g.get(1),630);
        ParticuleSphere a=g.get(0).get(0),b=g.get(1).get(0);
        n.getMonde().relier(a,b,a.getPosition().moins(b.getPosition()).norme());
        attendre(n,2);
        assertEquals(1,n.groupesRecrutes().size());
        assertTrue(n.isPlaqueOccupee(0));assertTrue(n.isPlaqueOccupee(1));
        assertFalse(n.isPorteOuverte());assertEquals(0,n.getCharge(),1e-9);
    }
    @Test void quitterUnePlaqueInterromptLaCharge() {
        NiveauBlob n=new NiveauBlob();n.getMonde().separer();
        // Terminer la poussée de séparation avant de poser les morceaux au repos.
        attendre(n,.4);
        List<List<ParticuleSphere>> g=n.groupesRecrutes();poser(g.get(0),215);poser(g.get(1),630);
        attendre(n,.3);assertTrue(n.getCharge()>0);poser(g.get(1),750);attendre(n,.1);
        assertEquals(0,n.getCharge(),1e-9);assertFalse(n.isPorteOuverte());
    }
    @Test void laSortieRefuseUnBlobIncomplet() {
        NiveauBlob n=ouvrir();
        for(List<ParticuleSphere> g:n.groupesRecrutes()){poser(g,980);}
        assertTrue(n.fusionner());
        attendre(n,0.1);assertEquals(1,n.groupesRecrutes().size());assertFalse(n.isGagne());
    }
    private void allerVers(NiveauBlob n,double cible,int maximum) {
        int stable=0;
        for(int i=0;i<maximum;i++){
            List<ParticuleSphere> groupe=n.getMonde().getGroupeSelectionne();
            double x=groupe.stream().mapToDouble(p->p.getPosition().getX()).average().orElseThrow();
            double vx=groupe.stream().mapToDouble(p->p.getVitesse().getX()).average().orElseThrow();
            boolean traverser = (cible > NiveauBlob.MUR_X + NiveauBlob.MUR_LARGEUR && x < NiveauBlob.MUR_X + 25)
                || (cible < NiveauBlob.MUR_X && x > NiveauBlob.MUR_X + 25);
            if(traverser && Math.abs(x - (NiveauBlob.MUR_X + 25)) < 145) { n.sauter(); }
            n.getMonde().commander(Math.max(-1,Math.min(1,(cible-x)*0.018-vx*0.06)),0);
            n.getMonde().avancer(1.0/60);
            stable=Math.abs(cible-x)<15&&Math.abs(vx)<15?stable+1:0;
            if(stable>30){return;}
        }
    }
    @Test void niveauTermineAvecLesCommandesSansTeleporterLesParticules() {
        NiveauBlob n=new NiveauBlob();
        allerVers(n,750,1200);
        assertEquals(4,n.getNombreRecuperees(),"Le parcours doit permettre de récupérer les quatre particules");
        allerVers(n,630,900);
        assertTrue(n.getMonde().separer());attendre(n,0.4);
        allerVers(n,215,1500);attendre(n,2);
        assertTrue(n.isPorteOuverte(),"Deux morceaux séparés doivent pouvoir rester sur les plaques");
        allerVers(n,630,1500);
        for(int i=0;i<12&&n.groupesRecrutes().size()>1;i++) {n.fusionner();attendre(n,0.2);}
        assertEquals(1,n.groupesRecrutes().size(),"Les morceaux doivent pouvoir se réunir après ouverture");
        n.getMonde().commander(1,0);
        for(int i=0;i<1500&&!n.isGagne();i++){n.getMonde().avancer(1.0/60);}
        assertTrue(n.isGagne(),"Le blob complet doit pouvoir atteindre la sortie");
    }
    @Test void frictionSolSeulementAuContactEtSansInversion() {
        ParticuleSphere p=new ParticuleSphere(13);p.setPosition(new Vecteur3D(400,627,0));
        p.setVitesse(new Vecteur3D(.01,0,0));p.ajouterForce(new Vecteur3D(0,360,0));
        new FrictionSol(640,.65,.55).mettreAJourForce(p,Monde.PAS);p.integrer(Monde.PAS);
        assertEquals(0,p.getVitesse().getX(),1e-9);
        p.setPosition(new Vecteur3D(400,400,0));p.setVitesse(new Vecteur3D(10,0,0));
        p.ajouterForce(new Vecteur3D(0,360,0));new FrictionSol(640,.65,.55).mettreAJourForce(p,Monde.PAS);
        assertEquals(0,p.getForceAccumulee().getX(),1e-9);
    }
    @Test void sautDisponibleAuSolMaisPasDansLesAirs() {
        NiveauBlob n=new NiveauBlob();assertFalse(n.sauter());attendre(n,2);
        assertTrue(n.sauter());assertFalse(n.sauter());
        for(ParticuleSphere p:n.getMonde().getGroupeSelectionne()) {assertTrue(p.getVitesse().getY() < -450);}
        n.getMonde().avancer(.1);n.getMonde().avancer(.1);n.getMonde().avancer(.1);
        assertFalse(n.sauter());
    }
    @Test void leMurBloqueUnCercleAuSolEtPermetDeSePoserDessus() {
        ParticuleSphere p=new ParticuleSphere(13);p.setPosition(new Vecteur3D(395,600,0));
        ContactRectangle mur=new ContactRectangle(List.of(p),NiveauBlob.MUR_X,
            Monde.SOL-NiveauBlob.MUR_HAUTEUR,NiveauBlob.MUR_LARGEUR,NiveauBlob.MUR_HAUTEUR);
        java.util.ArrayList<Contact> contacts=new java.util.ArrayList<>();mur.genererContacts(contacts);
        contacts.get(0).resoudreInterpenetration();assertEquals(387,p.getPosition().getX(),1e-9);
        p.setPosition(new Vecteur3D(425,515,0));contacts.clear();mur.genererContacts(contacts);
        contacts.get(0).resoudreInterpenetration();assertEquals(507,p.getPosition().getY(),1e-9);
    }
    @Test void impulsionTientCompteDeLaMasseEtDesObjetsFixes() {
        Particule p=new Particule();p.setMasse(2);p.appliquerImpulsion(new Vecteur3D(0,-20,0));
        assertEquals(-10,p.getVitesse().getY(),1e-9);
        p.setInverseMasse(0);p.appliquerImpulsion(new Vecteur3D(0,-20,0));
        assertEquals(-10,p.getVitesse().getY(),1e-9);
    }
}
