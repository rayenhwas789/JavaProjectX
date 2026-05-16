import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDAO {

    // ── Modifiez ces 3 constantes selon votre configuration ──
    private static final String URL      = "jdbc:mysql://localhost:3306/coworking";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    /**
     * Ouvre et retourne une connexion à la base de données.
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Vérifie les identifiants et retourne l'Admin correspondant,
     * ou null si email/mot de passe incorrects.
     *
     * Structure attendue de la table :
     *   CREATE TABLE admin (
     *       id         INT PRIMARY KEY AUTO_INCREMENT,
     *       nom        VARCHAR(100),
     *       prenom     VARCHAR(100),
     *       email      VARCHAR(150) UNIQUE,
     *       mot_de_passe VARCHAR(255)
     *   );
     */
    public Admin authentifier(String email, String motDePasse) {
        String sql = "SELECT id, nom, prenom, email, mot_de_passe "
                   + "FROM admin "
                   + "WHERE email = ? AND mot_de_passe = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, motDePasse); // Remplacez par un hash (BCrypt) en production

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Admin(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur BD lors de l'authentification : " + e.getMessage());
        }

        return null; // identifiants incorrects ou erreur
    }
}
