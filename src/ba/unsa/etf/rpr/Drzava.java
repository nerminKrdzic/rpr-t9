package ba.unsa.etf.rpr;

public class Drzava {
    private int id;
    private String naziv;
    private int broj_stanovnika;
    private int drzava;

    public Drzava(int id, String naziv, int broj_stanovnika, int drzava) {
        setId(id);
        setNaziv(naziv);
        setBroj_stanovnika(broj_stanovnika);
        setDrzava(drzava);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public int getBroj_stanovnika() {
        return broj_stanovnika;
    }

    public void setBroj_stanovnika(int broj_stanovnika) {
        this.broj_stanovnika = broj_stanovnika;
    }

    public int getDrzava() {
        return drzava;
    }

    public void setDrzava(int drzava) {
        this.drzava = drzava;
    }
}
