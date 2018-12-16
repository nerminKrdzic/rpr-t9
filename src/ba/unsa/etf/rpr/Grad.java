package ba.unsa.etf.rpr;

public class Grad {
    private int id;
    private String naziv;
    private int glavni_grad;

    public Grad(int id, String naziv, int glavni_grad) {
        setId(id);
        setNaziv(naziv);
        setGlavni_grad(glavni_grad);
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

    public int getGlavni_grad() {
        return glavni_grad;
    }

    public void setGlavni_grad(int glavni_grad) {
        this.glavni_grad = glavni_grad;
    }
}
