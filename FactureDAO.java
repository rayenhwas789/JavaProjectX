import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FactureDAO {

    // ===================== GÉNÉRER (calcul automatique) =====================

    public Facture genererFacture(int idReservation) {

        String sqlRes = "SELECT r.*, m.typeAbonnement, "
                + "s.tarif AS tarifSalle, b.tarif AS tarifBureau "
                + "FROM reservation r "
                + "JOIN membre m ON r.idMembre = m.id "
                + "LEFT JOIN salle_de_reunion s ON r.idSalleReunion = s.id "
                + "LEFT JOIN bureau b ON r.idBureau = b.id "
                + "WHERE r.id = ?";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlRes)) {

            ps.setInt(1, idReservation);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return null;

            // ===== Données =====
            Timestamp dateDebut = rs.getTimestamp("dateDebut");
            Timestamp dateFin   = rs.getTimestamp("dateFin");
            int idMembre        = rs.getInt("idMembre");
            String typeAbonnement = rs.getString("typeAbonnement");

            // ===== Sécurité =====
            if (dateFin.before(dateDebut)) {
                throw new IllegalArgumentException("dateFin < dateDebut");
            }

            // ===== Calcul jours (ARRONDI SUPÉRIEUR) =====
            long diffMillis = dateFin.getTime() - dateDebut.getTime();

            double jours = diffMillis / (1000.0 * 60 * 60 * 24);
            jours = Math.ceil(jours); // 🔥 très important
            jours = Math.max(1, jours);

            // ===== Tarif =====
            double tarifSalle  = rs.getDouble("tarifSalle");
            boolean salleNull  = rs.wasNull();

            double tarifBureau = rs.getDouble("tarifBureau");
            boolean bureauNull = rs.wasNull();

            double tarif = 0;

            if (!salleNull) {
                tarif = tarifSalle;
            } else if (!bureauNull) {
                tarif = tarifBureau;
            }

            // ===== Réduction abonnement =====
            double reduction = 0;

            if (typeAbonnement != null) {
                if (typeAbonnement.equalsIgnoreCase("mensuel")) {
                    reduction = 0.3;
                } else if (typeAbonnement.equalsIgnoreCase("annuel")) {
                    reduction = 0.5;
                }
            }

            // ===== Montant =====
            double montant = jours * tarif * (1 - reduction);

            // Arrondi
            montant = Math.round(montant * 100.0) / 100.0;

            // ===== Facture =====
            Facture f = new Facture(
                    0,
                    idReservation,
                    idMembre,
                    new Date(System.currentTimeMillis()),
                    montant,
                    "EN_ATTENTE"
            );

            ajouterFacture(f);

            return f;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ===================== AJOUTER =====================
    public void ajouterFacture(Facture f) {

        String sql = "INSERT INTO facture (idReservation, idMembre, dateFacture, montant, statut) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, f.getIdReservation());
            ps.setInt(2, f.getIdMembre());
            ps.setDate(3, f.getDateFacture());
            ps.setDouble(4, f.getMontant());
            ps.setString(5, f.getStatut());

            ps.executeUpdate();

            // Récupérer l'id auto-généré
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) f.setId(keys.getInt(1));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===================== MODIFIER STATUT =====================
    public void modifierStatut(int id, String statut) {

        String sql = "UPDATE facture SET statut=? WHERE id=?";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, statut);
            ps.setInt(2, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===================== MODIFIER (complet) =====================
    public void modifierFacture(int id, Facture f) {

        String sql = "UPDATE facture SET idReservation=?, idMembre=?, "
                   + "dateFacture=?, montant=?, statut=? WHERE id=?";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, f.getIdReservation());
            ps.setInt(2, f.getIdMembre());
            ps.setDate(3, f.getDateFacture());
            ps.setDouble(4, f.getMontant());
            ps.setString(5, f.getStatut());
            ps.setInt(6, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===================== SUPPRIMER =====================
    public void supprimerFacture(int id) {

        String sql = "DELETE FROM facture WHERE id=?";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===================== GET ALL =====================
    public List<Facture> getToutesFactures() {

        List<Facture> list = new ArrayList<>();
        String sql = "SELECT * FROM facture ORDER BY dateFacture DESC";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Facture(
                        rs.getInt("id"),
                        rs.getInt("idReservation"),
                        rs.getInt("idMembre"),
                        rs.getDate("dateFacture"),
                        rs.getDouble("montant"),
                        rs.getString("statut")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ===================== GET BY MEMBRE =====================
    public List<Facture> getFacturesParMembre(int idMembre) {

        List<Facture> list = new ArrayList<>();
        String sql = "SELECT * FROM facture WHERE idMembre=? ORDER BY dateFacture DESC";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idMembre);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Facture(
                        rs.getInt("id"),
                        rs.getInt("idReservation"),
                        rs.getInt("idMembre"),
                        rs.getDate("dateFacture"),
                        rs.getDouble("montant"),
                        rs.getString("statut")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ===================== TOTAL REVENUS =====================
    public double getTotalRevenus() {

        String sql = "SELECT COALESCE(SUM(montant), 0) FROM facture WHERE statut='PAYÉE'";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getDouble(1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}
