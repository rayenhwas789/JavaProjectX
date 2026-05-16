import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Bureau {

    private int id;
    private String nom;
    private boolean disponible;
    private double tarif;

    public Bureau(int id,String nom, boolean disponible, double tarif) {
        this.id=id;
        this.nom = nom;
        this.disponible = disponible;
        this.tarif = tarif;
    }
public int getId(){return id;}
    public String getNom() {
        return nom;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public double getTarif() {
        return tarif;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public void setTarif(double tarif) {
        this.tarif = tarif;
    }


    }
