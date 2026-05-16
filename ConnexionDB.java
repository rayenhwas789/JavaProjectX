import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnexionDB {

    private static final String URL = "jdbc:mysql://localhost:3306/coworking";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // optionnel
        } catch (ClassNotFoundException e) {
            System.out.println("Driver MySQL introuvable");
        }

        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("Nouvelle connexion ouverte");

        return conn;
    }
}