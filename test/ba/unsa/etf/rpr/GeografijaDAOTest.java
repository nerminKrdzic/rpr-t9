package ba.unsa.etf.rpr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class GeografijaDAOTest {

    @Test
    void regenerateFile() {
        GeografijaDAO.removeInstance();
        File dbfile = new File("resources/baza.db");
        dbfile.delete();
        GeografijaDAO dao = GeografijaDAO.getInstance();
        ArrayList<Grad> gradovi = dao.gradovi();
        assertEquals("London", gradovi.get(0).getNaziv());
        assertEquals("Francuska", gradovi.get(1).getDrzava().getNaziv());
        dao.resetBazu();
    }

    @Test
    void glavniGrad() {
        GeografijaDAO dao = GeografijaDAO.getInstance();
        dao.resetBazu(); // zasto se u testovima smatra da ce baza biti u pocetnom obliku svaki put
        // kad se testovi izvrsavaju u random reoslijdu????? i u pitanju je baza podataka
        // baza je tu da bi se sacuvali podaci
        Grad nepoznat = dao.glavniGrad("Bosna i Hercegovina");
        assertNull(nepoznat);
        Grad bech = dao.glavniGrad("Austrija");
        assertEquals("Beč", bech.getNaziv());
    }

    @Test
    void obrisiDrzavu() {
        GeografijaDAO dao = GeografijaDAO.getInstance();
        dao.resetBazu();
        // Nepostojeća država, neće se desiti ništa
        dao.obrisiDrzavu("Kina");
        ArrayList<Grad> gradovi = dao.gradovi();
        assertEquals("Pariz", gradovi.get(1).getNaziv());
        assertEquals("Austrija", gradovi.get(2).getDrzava().getNaziv());
    }

    @Test
    void obrisiDrzavu2() {
        GeografijaDAO dao = GeografijaDAO.getInstance();
        dao.resetBazu();

        // Nema gradova Beč i Graz koji su iz austrije
        dao.obrisiDrzavu("Austrija");

        ArrayList<Grad> gradovi = dao.gradovi();
        //jer je dodano sarajevo
        assertEquals(3, gradovi.size());
        assertEquals("London", gradovi.get(0).getNaziv());
        assertEquals("Pariz", gradovi.get(1).getNaziv());
        assertEquals("Manchester", gradovi.get(2).getNaziv());
    }

    @Test
    void dodajGrad() {
        GeografijaDAO dao = GeografijaDAO.getInstance();
        dao.resetBazu();
        Drzava francuska = dao.nadjiDrzavu("Francuska");
        Grad grad = new Grad();
        grad.setNaziv("Marseille");
        grad.setBrojStanovnika(869815);
        grad.setDrzava(francuska);
        dao.dodajGrad(grad);

        // Marsej je veći od Manchestera i Graza, ali manji od Pariza, Londona i Beča
        ArrayList<Grad> gradovi = dao.gradovi();
        assertEquals("Marseille", gradovi.get(3).getNaziv());
    }

    @Test
    void dodajDrzavu() {
        Grad sarajevo = new Grad();
        sarajevo.setNaziv("Sarajevo");
        sarajevo.setBrojStanovnika(500000);
        Drzava bih = new Drzava();
        bih.setNaziv("Bosna i Hercegovina");
        bih.setGlavniGrad(sarajevo);
        sarajevo.setDrzava(bih);

        GeografijaDAO dao = GeografijaDAO.getInstance();
        dao.resetBazu();
        dao.dodajDrzavu(bih);
        dao.dodajGrad(sarajevo);

        // Provjera
        Grad proba = dao.glavniGrad("Bosna i Hercegovina");
        assertEquals("Sarajevo", proba.getNaziv());
        assertEquals(500000, proba.getBrojStanovnika());
        assertEquals("Bosna i Hercegovina", proba.getDrzava().getNaziv());
    }

    @Test
    void izmijeniGrad() {
        GeografijaDAO dao = GeografijaDAO.getInstance();
        dao.resetBazu();
        Grad bech = dao.glavniGrad("Austrija");
        bech.setNaziv("Vienna");
        dao.izmijeniGrad(bech);

        ArrayList<Grad> gradovi = dao.gradovi();
        assertEquals("Vienna", gradovi.get(2).getNaziv());
    }
}
