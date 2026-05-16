public class Admin extends Utilisateur {

    public Admin(int id, String nom, String prenom, String email, String motDePasse) {
        super(id, nom, prenom, email, motDePasse);
    }

    @Override
    public String toString() {
        return "Admin{id=" + getId() + ", nom=" + getNom() + ", prenom=" + getPrenom()
                + ", email=" + getEmail() + "}";
    }
}
