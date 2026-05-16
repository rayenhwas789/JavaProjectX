import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
public class Reservation {

    private int id;
    private Timestamp dateDebut;
    private Timestamp dateFin;
    private String type;

    private int idMembre;
    private Integer idSalleReunion;
    private Integer idBureau;

    public Reservation(int id,Timestamp dateDebut, Timestamp dateFin, String type,
                       int idMembre, Integer idSalleReunion, Integer idBureau) {
        this.id=id;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.type = type;
        this.idMembre = idMembre;
        this.idSalleReunion = idSalleReunion;
        this.idBureau = idBureau;
    }

    public int getId() {
        return id;
    }

    public Timestamp getDateDebut() {
        return dateDebut;
    }

    public Timestamp getDateFin() {
        return dateFin;
    }

    public String getType() {
        return type;
    }

    public int getIdMembre() {
        return idMembre;
    }

    public Integer getIdSalleReunion() {
        return idSalleReunion;
    }

    public Integer getIdBureau() {
        return idBureau;
    }

    public void setDateDebut(Timestamp dateDebut) {
        this.dateDebut = dateDebut;
    }

    public void setDateFin(Timestamp dateFin) {
        this.dateFin = dateFin;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setIdMembre(int idMembre) {
        this.idMembre = idMembre;
    }

    public void setIdSalleReunion(Integer idSalleReunion) {
        this.idSalleReunion = idSalleReunion;
    }

    public void setIdBureau(Integer idBureau) {
        this.idBureau = idBureau;
    }

}