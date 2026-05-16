import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Génère les notifications pour un membre :
 *  1. 💰 Facture(s) EN_ATTENTE
 *  2. 📅 Réservation dans moins de 24h
 *  3. ✅ Réservation(s) confirmée(s) (créées aujourd'hui)
 */
public class NotificationManager {

    private final ReservationDAO resDAO = new ReservationDAO();
    private final FactureDAO     facDAO = new FactureDAO();

    public List<Notification> getNotifications(Membre membre) {

        List<Notification> list = new ArrayList<>();

        List<Reservation> reservations = resDAO.getReservationsParMembre(membre.getId());
        List<Facture>     factures     = facDAO.getToutesFactures();


        // IDs des réservations du membre
        Set<Integer> resIds = reservations.stream()
                .map(Reservation::getId)
                .collect(Collectors.toSet());

        Instant now = Instant.now();

        // ── 1. Factures EN_ATTENTE ──────────────────────────────
        for (Facture f : factures) {
            if (resIds.contains(f.getIdReservation())
                    && "EN_ATTENTE".equals(f.getStatut())) {

                list.add(new Notification(
                        "\uD83D\uDCB0 Facture #" + f.getId() + " non payée : " + f.getMontant() + " DT",
                        Notification.Type.FACTURE_IMPAYEE
                ));
            }
        }

        // ── 2. Réservation dans moins de 24h ───────────────────
        for (Reservation r : reservations) {
            Instant debut = r.getDateDebut().toInstant();
            long heures   = ChronoUnit.HOURS.between(now, debut);

            if (heures >= 0 && heures < 24) {
                list.add(new Notification(
                        "\uD83D\uDCC5 Rappel : réservation #" + r.getId()
                                + " commence le " + r.getDateDebut().toString().substring(0, 16),
                        Notification.Type.RESERVATION_BIENTOT
                ));
            }
        }

        // ── 3. Réservations créées aujourd'hui (confirmées) ────
        for (Reservation r : reservations) {
            Instant debut = r.getDateDebut().toInstant();
            long joursDepuis = ChronoUnit.DAYS.between(debut, now);

            // Si la réservation a débuté dans les dernières 24h → "confirmée"
            if (joursDepuis == 0 && debut.isBefore(now)) {
                list.add(new Notification(
                        "\u2705 Réservation #" + r.getId() + " confirmée !",
                        Notification.Type.RESERVATION_CONFIRMEE
                ));
            }
        }

        return list;
    }
}
