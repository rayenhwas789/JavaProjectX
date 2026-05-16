/**
 * Représente une notification pour le membre.
 */
public class Notification {

    // Les 3 types de notifications
    public enum Type {
        FACTURE_IMPAYEE,      // 💰 Facture en attente
        RESERVATION_BIENTOT,  // 📅 Réservation dans moins de 24h
        RESERVATION_CONFIRMEE // ✅ Réservation confirmée
    }

    private String message;
    private Type type;

    public Notification(String message, Type type) {
        this.message = message;
        this.type    = type;
    }

    public String getMessage() { return message; }
    public Type   getType()    { return type; }
}
