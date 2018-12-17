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
            ispis += g.toString();
        }
        return ispis;
    }

    public static void glavniGrad(){
        System.out.println("Unesite naziv drzave: ");
        Scanner scanner = new Scanner(System.in);
        GeografijaDAO dao = GeografijaDAO.getInstance();
        String naziv = scanner.nextLine().trim();
        Grad glavniGrad = dao.glavniGrad(naziv);
        if(glavniGrad == null) System.out.println("Nepostojeca drzava");
        else System.out.println("Glavni grad drzave " + naziv + " je " + glavniGrad.getNaziv());

    }

    public static void main(String[] args) {
        System.out.println("Gradovi su:\n" + ispisiGradove());
        //glavniGrad();
        GeografijaDAO.getInstance().glavniGrad("Francuska");
    }
}
