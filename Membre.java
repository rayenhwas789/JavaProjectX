import java.sql.Connection;
import java.sql.PreparedStatement;

public class Membre extends Utilisateur{
    private String typeAbonnement;

    public Membre(int id,String nom,String prenom,String email,String mot_de_passe,String typeAbonnement){
        super(id,nom,prenom,email,mot_de_passe);
        this.typeAbonnement=typeAbonnement;
    }

    public String getTypeAbonnement() {
        return typeAbonnement;
    }

}
