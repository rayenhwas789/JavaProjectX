import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalleDeReunionDAO {

    public void ajouterSalle(SalleDeReunion s) {
        String sql = "INSERT INTO salle_de_reunion (nom, capacite, disponible, tarif) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, s.getNom());
            ps.setInt(2, s.getCapacite());
            ps.setBoolean(3, s.isDisponible());
            ps.setDouble(4, s.getTarif());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void modifierSalle(SalleDeReunion s) {
        String sql = "UPDATE salle_de_reunion SET nom=?, capacite=?, disponible=?, tarif=? WHERE id=?";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, s.getNom());
            ps.setInt(2, s.getCapacite());
            ps.setBoolean(3, s.isDisponible());
            ps.setDouble(4, s.getTarif());
            ps.setInt(5, s.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void supprimerSalle(int id) {
        String sql = "DELETE FROM salle_de_reunion WHERE id=?";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean verifierDisponibilite(int id) {
        String sql = "SELECT disponible FROM salle_de_reunion WHERE id=?";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("disponible");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<SalleDeReunion> getToutesLesSalles() {
        List<SalleDeReunion> salles = new ArrayList<>();
        String sql = "SELECT * FROM salle_de_reunion";

        try (Connection conn = ConnexionDB.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                salles.add(new SalleDeReunion(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getInt("capacite"),
                        rs.getBoolean("disponible"),
                        rs.getDouble("tarif")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return salles;
    }

    public List<SalleDeReunion> getSallesDisponibles() {

        List<SalleDeReunion> list = new ArrayList<>();

        String sql = "SELECT * FROM salle_de_reunion s "
                + "WHERE s.disponible = 1";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new SalleDeReunion(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getInt("capacite"),
                        rs.getBoolean("disponible"),
                        rs.getDouble("tarif")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}