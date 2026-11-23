package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {

    static final String URL = "jdbc:mysql://localhost:3306/basecommune1";
    static final String USERNAME = "root";
    static final String PASSWORD = "";
    static Connection connection;

    private static MyDatabase instance;

    // Constructeur privé pour empêcher l'instanciation directe
    private MyDatabase() {
        try {
            // Première connexion à la base de données
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connexion établie avec succès.");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode pour obtenir l'instance du Singleton
    public static MyDatabase getInstance() {
        if (instance == null) {
            synchronized (MyDatabase.class) {
                if (instance == null) {
                    instance = new MyDatabase();
                }
            }
        }
        return instance;
    }

    // Méthode pour récupérer la connexion à la base de données
    public static Connection getConnection() {
        try {
            // Vérifier si la connexion est fermée ou inexistante
            if (connection == null || connection.isClosed()) {
                // Si la connexion est fermée, rétablir la connexion
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("🔄 Connexion rétablie.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de la connexion : " + e.getMessage());
        }
        return connection;
    }

    // Méthode pour fermer la connexion à la fin de l'application
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Connexion fermée avec succès.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
        }
    }
}
