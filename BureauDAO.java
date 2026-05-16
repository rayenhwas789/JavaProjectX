import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BureauDAO {

    // ===================== GET ALL =====================
    public List<Bureau> getTousLesBureaux() {
        List<Bureau> list = new ArrayList<>();
        String sql = "SELECT * FROM bureau";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Bureau(
                        rs.getInt("id"),
                        rs.getString("nom"  ),
                        rs.getBoolean("disponible"),
                        rs.getDouble("tarif")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // ===================== ADD =====================
    public void ajouterBureau(Bureau b) {
        String sql = "INSERT INTO bureau (nom, disponible, tarif) VALUES (?, ?, ?)";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, b.getNom());
            ps.setBoolean(2, b.isDisponible());
            ps.setDouble(3, b.getTarif());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===================== UPDATE =====================
    public void modifierBureau(int id, Bureau b) {
        String sql = "UPDATE bureau SET nom=?, disponible=?, tarif=? WHERE id=?";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, b.getNom());
            ps.setBoolean(2, b.isDisponible());
            ps.setDouble(3, b.getTarif());
            ps.setInt(4, id);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===================== DELETE =====================
    public void supprimerBureau(int id) {
        String sql = "DELETE FROM bureau WHERE id=?";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Bureau> getBureauxDisponibles() {

        List<Bureau> list = new ArrayList<>();

        String sql = "SELECT * FROM bureau b "
                + "WHERE b.disponible = 1";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Bureau(
                        rs.getInt("id"),
                        rs.getString("nom"),
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