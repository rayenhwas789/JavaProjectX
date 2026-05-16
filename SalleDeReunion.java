import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalleDeReunion {

    private int id;
    private String nom;
    private int capacite;
    private boolean disponible;
    private double tarif;

    public SalleDeReunion() {}

    public SalleDeReunion(int id, String nom, int capacite, boolean disponible, double tarif) {
        this.id = id;
        this.nom = nom;
        this.capacite = capacite;
        this.disponible = disponible;
        this.tarif = tarif;
    }

    // GETTERS & SETTERS
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public double getTarif() { return tarif; }
    public void setTarif(double tarif) { this.tarif = tarif; }

    }