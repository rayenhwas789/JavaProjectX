import java.sql.Date;

/**
 * Modèle Facture
 * Liée à une réservation et un membre.
 * Montant calculé : (dateFin - dateDebut) * tarif journalier de la salle/bureau.
 */
public class Facture {

    private int    id;
    private int    idReservation;
    private int    idMembre;
    private Date   dateFacture;
    private double montant;
    private String statut;   // "PAYÉE" | "EN_ATTENTE" | "ANNULÉE"

    // ===================== CONSTRUCTORS =====================

    public Facture() {}

    public Facture(int id, int idReservation, int idMembre,
                   Date dateFacture, double montant, String statut) {
        this.id            = id;
        this.idReservation = idReservation;
        this.idMembre      = idMembre;
        this.dateFacture   = dateFacture;
        this.montant       = montant;
        this.statut        = statut;
    }

    // ===================== GETTERS / SETTERS =====================

    public int getId()                      { return id; }
    public void setId(int id)              { this.id = id; }

    public int getIdReservation()                       { return idReservation; }
    public void setIdReservation(int idReservation)    { this.idReservation = idReservation; }

    public int getIdMembre()                   { return idMembre; }
    public void setIdMembre(int idMembre)     { this.idMembre = idMembre; }

    public Date getDateFacture()                      { return dateFacture; }
    public void setDateFacture(Date dateFacture)     { this.dateFacture = dateFacture; }

    public double getMontant()                  { return montant; }
    public void setMontant(double montant)     { this.montant = montant; }

    public String getStatut()                   { return statut; }
    public void setStatut(String statut)       { this.statut = statut; }

    @Override
    public String toString() {
        return "Facture{id=" + id + ", reservation=" + idReservation
                + ", membre=" + idMembre + ", montant=" + montant
                + ", statut=" + statut + "}";
    }
}
