package ba.unsa.etf.rpr;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;

public class OracleDAO {
    private static GenerateID lastID;
    private static OracleDAO instance = null;
    private final String URL = "jdbc:oracle:thin:@ora.db.lab.ri.etf.unsa.ba:1521:ETFLAB";
    private Connection connection;
    private PreparedStatement glavniGradQuery, obrisiDrzavuQuery1, obrisiDrzavuQuery2, gradoviQuery, dodajGradQuery, dodajDrzavuQuery, izmijeniGradQuery, nadjiDrzavuQuery, deleteGradQuery, deleteDrzavaQuery;
    private PreparedStatement selektGradovi, selektDrzave;
    private OracleDAO(){
        try {
            connection = DriverManager.getConnection(URL, "NK17825", "xuYoshjV");
            imaLiBaza(); // gleda ima li uopste potrebnih tabela u bazi
            prepareStatements();
            ResultSet idEvi = selektGradovi.executeQuery();
            lastID = new GenerateID(vratiMaxID(idEvi));
            ResultSet gradovi = selektGradovi.executeQuery();
            ResultSet drzave = selektDrzave.executeQuery();
            if(!gradovi.next() || !drzave.next())
                regenerisi();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int vratiMaxID(ResultSet idEvi){
        int maxID = 108;
        try {
            if(idEvi.next()){
                if(idEvi.getInt(1) > idEvi.getInt(4))
                    maxID = idEvi.getInt(1);
                else
                    maxID = idEvi.getInt(4);
            }
            while (idEvi.next()) {
                if(idEvi.getInt(1) > maxID)
                    maxID = idEvi.getInt(1);
                if(idEvi.getInt(4) > maxID)
                    maxID = idEvi.getInt(4);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maxID;
    }

    private void imaLiBaza(){

        // a ovo je za oracle
        String kreirajTabele = "CREATE TABLE grad (\n" +
                "                    id int not null primary key ,\n" +
                "                    naziv VARCHAR2(20) not null ,\n" +
                "                    broj_stanovnika int,\n" +
                "                    drzava int\n" +
                ");\n" +
                "create table drzava (\n" +
                "                      id int not null primary key ,\n" +
                "                      naziv VARCHAR2(20) not null ,\n" +
                "                      glavni_grad int\n" +
                ");\n" +
                "commit ;\n" +
                "alter table grad add foreign key (drzava) references drzava(id);\n" +
                "alter table drzava add foreign key (glavni_grad) references grad(id);\n" +
                "commit ;";
        boolean dropovoJeTabele = false;
        try{
            //ako bilo koji selekt ne uspije jedna ili obije tabele ne postoje
            getConnection().createStatement().executeQuery("SELECT * FROM grad");
            getConnection().createStatement().executeQuery("select * from drzava");
        } catch (SQLException e){
            dropovoJeTabele = true;
            try{ // treba dropati kolone koji su strani kljucevi prvo
                getConnection().createStatement().executeUpdate("alter table grad drop column drzava;");
            } catch (SQLException f) {}
            try{
                getConnection().createStatement().executeUpdate("alter table drzava drop column glavni_grad;");
            } catch (SQLException f) {}
            try{ // pa dropamo obije tabele da bi ih fino kreirali
                getConnection().createStatement().executeUpdate("drop table grad;");
            } catch (SQLException f) {}
            try{
                getConnection().createStatement().executeUpdate("drop table drzava;");
            } catch (SQLException f) {}
        }
        if(dropovoJeTabele){
            try{
                getConnection().createStatement().executeUpdate(kreirajTabele);
            } catch (SQLException e){
                e.printStackTrace();
            }
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
        obrisiDrzavuQuery1 = getConnection().prepareStatement("delete from grad where drzava = (select id from drzava where naziv = ?);\n commit ;");
        obrisiDrzavuQuery2 = getConnection().prepareStatement("delete from drzava where naziv = ?;\n commit ;");
        gradoviQuery = getConnection().prepareStatement("select * from grad order by broj_stanovnika desc");
        dodajGradQuery = getConnection().prepareStatement("insert into grad values (?, ?, ?, ?);\n commit ;");
        dodajDrzavuQuery = getConnection().prepareStatement("insert into drzava values(?, ?, ?);\n commit ;");
        izmijeniGradQuery = getConnection().prepareStatement("update grad set naziv = ?, broj_stanovnika = ? where id = ?;\n commit ;");
        nadjiDrzavuQuery = getConnection().prepareStatement("select * from drzava where naziv = ?");
        deleteGradQuery = getConnection().prepareStatement("delete from grad where id is not null;\n commit ;");
        deleteDrzavaQuery = getConnection().prepareStatement("delete from drzava where id is not null;\n commit ;");
    }

    private static void initialize(){
        instance = new OracleDAO();
    }

    public static OracleDAO getInstance(){
        if(instance == null) initialize();
        return instance;
    }

    public static void removeInstance(){
        if(instance != null) {
            try{
                instance.getConnection().close();
            } catch (SQLException e) { }
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
        Grad g3 = new Grad(104, "Beč", 1899055, null);
        Grad g4 = new Grad(106, "Manchester", 545500, null);
        Grad g5 = new Grad(108, "Graz", 280200, null);
        Drzava d1 = new Drzava(101, "Francuska",  null);
        Drzava d2 = new Drzava(103, "Velika Britanija", null);
        Drzava d3 = new Drzava(105, "Austrija", null);
        g1.setDrzava(d1); g2.setDrzava(d2); g3.setDrzava(d3); g4.setDrzava(d2); g5.setDrzava(d3);
        d1.setGlavniGrad(g1); d2.setGlavniGrad(g2); d3.setGlavniGrad(g3);

        dodajGrad(g1);
        dodajGrad(g2);
        dodajGrad(g3);
        dodajGrad(g4);
        dodajGrad(g5);
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
                Grad g = new Grad(gradoviSet.getInt(1), gradoviSet.getString(2), gradoviSet.getInt(3), null);
                ResultSet drzava = getConnection().createStatement().executeQuery("select naziv from drzava where id = " + gradoviSet.getInt(4));
                Drzava d = null;
                if(drzava.next()){
                    d = nadjiDrzavu(drzava.getString(1));
                }
                g.setDrzava(d);
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