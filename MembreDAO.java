import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MembreDAO {

    // ===================== AJOUT =====================
    public void add(Membre m) {

        String sql = "INSERT INTO membre (nom, prenom, email, motDePasse, typeAbonnement) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, m.getNom());
            ps.setString(2, m.getPrenom());
            ps.setString(3, m.getEmail());
            ps.setString(4, m.getMotDePasse());
            ps.setString(5, m.getTypeAbonnement());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===================== MODIFIER =====================
    public void update(int id, Membre m) {

        String sql = "UPDATE membre SET nom=?, prenom=?, email=?, motDePasse=?, typeAbonnement=? WHERE id=?";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, m.getNom());
            ps.setString(2, m.getPrenom());
            ps.setString(3, m.getEmail());
            ps.setString(4, m.getMotDePasse());
            ps.setString(5, m.getTypeAbonnement());
            ps.setInt(6, id);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===================== SUPPRIMER =====================
    public void delete(int id) {

        String sql = "DELETE FROM membre WHERE id=?";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===================== LISTE =====================
    public List<Membre> getAll() {

        List<Membre> list = new ArrayList<>();

        String sql = "SELECT * FROM membre";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Membre(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("motDePasse"),
                        rs.getString("typeAbonnement")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // ===================== GET BY ID =====================
    public Membre getMembreById(int id) {

        String sql = "SELECT * FROM membre WHERE id=?";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Membre(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("motDePasse"),
                        rs.getString("typeAbonnement")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }



    public Membre authentifier(String email, String motDePasse) {
        String sql = "SELECT * FROM membre WHERE email = ? AND motDePasse = ?";
        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, motDePasse);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Membre(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("motDePasse"),
                        rs.getString("typeAbonnement")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}