package ba.unsa.etf.rpr;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;

public class GeografijaDAO {
    private static GenerateID lastID;
    private static GeografijaDAO instance = null;
    private final String URL = "jdbc:sqlite:resources/baza.db";
    private Connection connection;
    private PreparedStatement glavniGradQuery, obrisiDrzavuQuery1, obrisiDrzavuQuery2, gradoviQuery, drzaveQuery, dodajGradQuery, dodajDrzavuQuery, izmijeniGradQuery, nadjiDrzavuQuery, deleteGradQuery, deleteDrzavaQuery;
    private PreparedStatement selektGradovi, selektDrzave;
    private GeografijaDAO(){
        try {
            connection = DriverManager.getConnection(URL);
            prepareStatements();
            ResultSet maxID = getConnection().createStatement().executeQuery("select max(id), max(drzava) from grad");
            if(maxID.next()){
                if(maxID.getInt(1) > maxID.getInt(2)) lastID = new GenerateID(maxID.getInt(1));
                else lastID = new GenerateID(maxID.getInt(2));
            } else lastID = new GenerateID();
            String selektujGradove = "SELECT * FROM grad";
            String selektujDrzave = "select * from drzava";
            ResultSet gradovi = getConnection().createStatement().executeQuery(selektujGradove);
            ResultSet drzave = getConnection().createStatement().executeQuery(selektujDrzave);
            if(!gradovi.next() || !drzave.next())
                regenerisi();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void resetBazu(){
        regenerisi();
        try{
            ResultSet maxID = getConnection().createStatement().executeQuery("select max(id), max(drzava) from grad");
            if(maxID.next()){
                if(maxID.getInt(1) > maxID.getInt(2)) lastID = new GenerateID(maxID.getInt(1));
                else lastID = new GenerateID(maxID.getInt(2));
            } else lastID = new GenerateID();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void prepareStatements() throws SQLException {
        selektGradovi = getConnection().prepareStatement("SELECT * FROM grad");
        selektDrzave = getConnection().prepareStatement("select * from drzava");
        glavniGradQuery = getConnection().prepareStatement("select * from grad where drzava = (select id from drzava where naziv = ?)");
        obrisiDrzavuQuery1 = getConnection().prepareStatement("delete from grad where drzava = (select id from drzava where naziv = ?)");
        obrisiDrzavuQuery2 = getConnection().prepareStatement("delete from drzava where naziv = ?");
        gradoviQuery = getConnection().prepareStatement("select * from grad order by broj_stanovnika desc");
        drzaveQuery = getConnection().prepareStatement("select * from drzava");
        dodajGradQuery = getConnection().prepareStatement("insert into grad values (?, ?, ?, ?)");
        dodajDrzavuQuery = getConnection().prepareStatement("insert into drzava values(?, ?, ?)");
        izmijeniGradQuery = getConnection().prepareStatement("update grad set naziv = ?, broj_stanovnika = ? where id = ?");
        nadjiDrzavuQuery = getConnection().prepareStatement("select * from drzava where naziv = ?");
        deleteGradQuery = getConnection().prepareStatement("delete from grad where id is not null");
        deleteDrzavaQuery = getConnection().prepareStatement("delete from drzava where id is not null");
    }

    private static void initialize(){
        instance = new GeografijaDAO();
    }
    public static GeografijaDAO getInstance(){
        if(instance == null) initialize();
        return instance;
    }
    public static void removeInstance(){
        if(instance != null) {
            try{
                instance.getConnection().close();
            } catch (SQLException e) { }
        }
        if(lastID != null){
            PrintWriter izlaz = null;
            try{
                izlaz = new PrintWriter(new FileWriter("lastID.xml"));
                izlaz.println(lastID.getLastID());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(izlaz != null)
                    izlaz.close();
            }
        }
        instance = null;
    }

    private void regenerisi(){
        try{
            deleteGradQuery.executeUpdate();
            deleteDrzavaQuery.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
        Grad g1 = new Grad(100, "Pariz", 2206488, null);
        Grad g2 = new Grad(102, "London", 8825000, null);
        Grad g3 = new Grad(104, "Bec", 1899055, null);
        Grad g4 = new Grad(106, "Manchester", 545500, null);
        Grad g5 = new Grad(108, "Graz", 280200, null);
        Drzava d1 = new Drzava(101, "Francuska",  null);
        Drzava d2 = new Drzava(103, "Engleska", null);
        Drzava d3 = new Drzava(105, "Austrija", null);
        Drzava d4 = new Drzava(107, "Velika Britanija", null);
        g1.setDrzava(d1); g2.setDrzava(d2); g3.setDrzava(d3); g4.setDrzava(d4); g5.setDrzava(d3);
        d1.setGlavniGrad(g1); d2.setGlavniGrad(g2); d3.setGlavniGrad(g3); d4.setGlavniGrad(g4);
        dodajGrad(g1); dodajGrad(g2); dodajGrad(g3); dodajGrad(g4); dodajGrad(g5);
    }

    public Grad glavniGrad(String drzava){
        Grad grad = null;
        try{
            ArrayList<Grad> gradovi = new ArrayList<>();
            glavniGradQuery.setString(1, drzava);
            ResultSet resultSet = glavniGradQuery.executeQuery();
            while (resultSet.next()){ // trebala bi biti samo jedna iteracija
                int idGrada = resultSet.getInt(1);
                ResultSet drzavaResult = connection.createStatement().executeQuery("select * from drzava where glavni_grad = " + idGrada);
                Drzava d = null;
                while(drzavaResult.next()) { // trebala bi biti samo jedna iteracija
                    d = new Drzava(drzavaResult.getString(2), new Grad());
                    grad = new Grad(resultSet.getString(2), resultSet.getInt(3), d);
                    grad.setId(resultSet.getInt(1));
                    d.setGlavniGrad(grad);
                    d.setId(drzavaResult.getInt(1));
                    gradovi.add(grad);
                }
                if(gradovi.size() >= 1){
                    grad = gradovi.get(0);
                    for(Grad g: gradovi){
                        if(g.getBrojStanovnika() > grad.getBrojStanovnika())
                            grad = g;
                    }
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return grad;
    }
    public void obrisiDrzavu(String drzava){
        try{
            obrisiDrzavuQuery1.setString(1, drzava);
            obrisiDrzavuQuery2.setString(1, drzava);
            obrisiDrzavuQuery1.executeUpdate();
            obrisiDrzavuQuery2.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public ArrayList<Grad> gradovi(){
        ArrayList<Grad> gradovi = new ArrayList<>();
        try{
            ResultSet gradoviSet = gradoviQuery.executeQuery();
            while(gradoviSet.next()){
                Grad g = new Grad(gradoviSet.getString(2), gradoviSet.getInt(3), null);
                g.setId(gradoviSet.getInt(1));
                ResultSet drzaveSet = drzaveQuery.executeQuery();
                while(drzaveSet.next()){
                    if(gradoviSet.getInt(4) == drzaveSet.getInt(1)){
                        Drzava d = new Drzava(drzaveSet.getString(2), g);
                        d.setId(drzaveSet.getInt(1));
                        g.setDrzava(d);
                        break;
                    }
                }
                gradovi.add(g);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return gradovi;
    }
    public void dodajGrad(Grad grad){
        try{
            ArrayList<Grad> gradovi = gradovi();
            for(Grad g: gradovi){
                if(g.getNaziv().equalsIgnoreCase(grad.getNaziv()))
                    return;
            }
            int drzavaID;
            Drzava d = nadjiDrzavu(grad.getDrzava().getNaziv());
            int gradID = lastID.generateID();
            if(d != null){
                drzavaID = d.getId();
            } else drzavaID = lastID.generateID();
            dodajGradQuery.setInt(1, gradID);
            dodajGradQuery.setString(2, grad.getNaziv());
            dodajGradQuery.setInt(3, grad.getBrojStanovnika());
            dodajGradQuery.setInt(4, drzavaID);
            dodajGradQuery.executeUpdate();

            if(d != null) return;
            dodajDrzavuQuery.setInt(1, drzavaID);
            dodajDrzavuQuery.setString(2, grad.getDrzava().getNaziv());
            dodajDrzavuQuery.setInt(3, gradID);
            dodajDrzavuQuery.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void dodajDrzavu(Drzava drzava){
        try{
            Drzava d = nadjiDrzavu(drzava.getNaziv());
            if(d != null) return;

            int gradID = lastID.generateID();
            int drzavaID = lastID.generateID();
            drzava.setId(drzavaID);
            drzava.getGlavniGrad().setId(gradID);
            dodajDrzavuQuery.setInt(1, drzavaID);
            dodajDrzavuQuery.setString(2, drzava.getNaziv());
            dodajDrzavuQuery.setInt(3, gradID);
            dodajDrzavuQuery.executeUpdate();

            dodajGradQuery.setInt(1, gradID);
            dodajGradQuery.setString(2, drzava.getGlavniGrad().getNaziv());
            dodajGradQuery.setInt(3, drzava.getGlavniGrad().getBrojStanovnika());
            dodajGradQuery.setInt(4, drzavaID);
            dodajGradQuery.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public void izmijeniGrad(Grad grad){
        try {
            izmijeniGradQuery.setString(1, grad.getNaziv());
            izmijeniGradQuery.setInt(2, grad.getBrojStanovnika());
            izmijeniGradQuery.setInt(3, grad.getId());
            izmijeniGradQuery.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public Drzava nadjiDrzavu(String drzava){
        Drzava result = null;
        try{
            nadjiDrzavuQuery.setString(1, drzava);
            ResultSet resultSet = nadjiDrzavuQuery.executeQuery();
            while (resultSet.next()){
                result = new Drzava(resultSet.getInt(1), resultSet.getString(2), null);
                ResultSet r = connection.createStatement().executeQuery("select * from grad where drzava = " + resultSet.getInt(1));
                while (r.next()){
                    result.setGlavniGrad(new Grad(r.getInt(1), r.getString(2), r.getInt(3), result));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Connection getConnection() {
        return connection;
    }
}