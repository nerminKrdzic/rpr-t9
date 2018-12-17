package ba.unsa.etf.rpr;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static String ispisiGradove(){
        String ispis = new String("");
        ArrayList<Grad> gradovi = GeografijaDAO.getInstance().gradovi();
        for(Grad g: gradovi){
            if(g.getDrzava() != null)
                ispis += g.getNaziv() + " (" + g.getDrzava().getNaziv() + ") " + g.getBrojStanovnika() + "\n";
            else ispis += g.getNaziv() + " () " + g.getBrojStanovnika() + "\n";
        }
        return ispis;
    }
    public static void main(String[] args) {
        System.out.println("Gradovi su:\n" + ispisiGradove());
        //glavniGrad();
        GeografijaDAO.getInstance().glavniGrad("Francuska");
    }
}
