import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    // ===================== AJOUT =====================
    public void ajouterReservation(Reservation r) {

        String sql = "INSERT INTO reservation (dateDebut, dateFin, type, idMembre, idSalleReunion, idBureau) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            Integer salle  = "Salle".equalsIgnoreCase(r.getType())  ? r.getIdSalleReunion() : null;
            Integer bureau = "Bureau".equalsIgnoreCase(r.getType()) ? r.getIdBureau()       : null;

            ps.setTimestamp(1, r.getDateDebut());
            ps.setTimestamp(2, r.getDateFin());
            ps.setString(3, r.getType());
            ps.setInt(4, r.getIdMembre());

            if (salle  != null) ps.setInt(5, salle);  else ps.setNull(5, Types.INTEGER);
            if (bureau != null) ps.setInt(6, bureau); else ps.setNull(6, Types.INTEGER);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===================== UPDATE =====================
    public void modifierReservation(int id, Reservation r) {

        String sql = "UPDATE reservation "
                + "SET dateDebut=?, dateFin=?, type=?, idMembre=?, idSalleReunion=?, idBureau=? "
                + "WHERE id=?";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            Integer salle  = "Salle".equalsIgnoreCase(r.getType())  ? r.getIdSalleReunion() : null;
            Integer bureau = "Bureau".equalsIgnoreCase(r.getType()) ? r.getIdBureau()       : null;

            ps.setTimestamp(1, r.getDateDebut());
            ps.setTimestamp(2, r.getDateFin());
            ps.setString(3, r.getType());
            ps.setInt(4, r.getIdMembre());

            if (salle  != null) ps.setInt(5, salle);  else ps.setNull(5, Types.INTEGER);
            if (bureau != null) ps.setInt(6, bureau); else ps.setNull(6, Types.INTEGER);

            ps.setInt(7, id);

            int rows = ps.executeUpdate();
            if (rows == 0) System.err.println("[ReservationDAO] WARN: aucune ligne modifiée pour id=" + id);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===================== DELETE =====================
    public void supprimerReservation(int id) {

        String sql = "DELETE FROM reservation WHERE id=?";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) System.err.println("[ReservationDAO] WARN: aucune ligne supprimée pour id=" + id);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===================== GET ALL =====================
    public List<Reservation> getToutesReservations() {

        List<Reservation> list = new ArrayList<>();

        // Colonnes explicites + ORDER BY pour un ordre stable
        String sql = "SELECT id, dateDebut, dateFin, type, idMembre, idSalleReunion, idBureau "
                + "FROM reservation ORDER BY id ASC";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ===================== GET BY ID =====================
    /** Récupère une seule réservation par son id — utile pour dialogModifier. */
    public Reservation getById(int id) {

        String sql = "SELECT id, dateDebut, dateFin, type, idMembre, idSalleReunion, idBureau "
                + "FROM reservation WHERE id=?";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ===================== GET BY MEMBRE =====================
    public List<Reservation> getReservationsParMembre(int idMembre) {

        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT id, dateDebut, dateFin, type, idMembre, idSalleReunion, idBureau "
                + "FROM reservation WHERE idMembre=? ORDER BY id ASC";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idMembre);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ===================== MAPPER =====================
    private Reservation map(ResultSet rs) throws SQLException {
        return new Reservation(
                rs.getInt("id"),
                rs.getTimestamp("dateDebut"),
                rs.getTimestamp("dateFin"),
                rs.getString("type"),
                rs.getInt("idMembre"),
                (Integer) rs.getObject("idSalleReunion"),
                (Integer) rs.getObject("idBureau")
        );
    }
}