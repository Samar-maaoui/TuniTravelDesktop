package services;

import entities.TypeUtilisateur;
import entities.Utilisateur;
import org.mindrot.jbcrypt.BCrypt;
import utils.EmailService;
import utils.MyDatabase;
import utils.PasswordUtils;
import utils.SessionManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


public class UtilisateurService implements IUtilisateur<Utilisateur> {
    private Connection connection;
    public static List<Utilisateur> utilisateurs = new ArrayList<>();
    private int cin;


    public UtilisateurService() {
        connection = MyDatabase.getInstance().getConnection();
        try {
            chargerTousLesUtilisateurs();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void checkConnection() throws SQLException {
        if (connection == null || connection.isClosed()){
            connection = MyDatabase.getInstance().getConnection();
            if(connection == null){
                throw new RuntimeException("Impossible d'établir la connexion à la base de données.");
            }
        }
    }
    public String recupererMotDePasseParEmail(String email) throws SQLException {
        String query = "SELECT mot_de_passe FROM utilisateur WHERE email = ?";
        try (Connection conn = MyDatabase.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("mot_de_passe");
            }
        }
        return null;
    }

    public  List<Utilisateur> chargerTousLesUtilisateurs() throws SQLException {
        utilisateurs.clear();
        List<Utilisateur> utilisateursTemp = new ArrayList<>();
        String req = "SELECT id_utilisateur, nom, prenom, cin, email, mot_de_passe, adresse, telephone, date_naissance, type_Utilisateur, is_verified FROM Utilisateur";

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(req)) {

            while (rs.next()) {
                int idUtilisateur = rs.getInt("id_utilisateur");
                LocalDate dateNaissance = (rs.getDate("date_naissance") != null) ? rs.getDate("date_naissance").toLocalDate() : null;
                TypeUtilisateur typeUtilisateur = TypeUtilisateur.fromString(rs.getString("type_Utilisateur").trim());
                boolean isVerified = rs.getBoolean("is_verified");

                Utilisateur utilisateur = new Utilisateur(
                        idUtilisateur,
                        rs.getString("nom").trim(),
                        rs.getString("prenom").trim(),
                        rs.getInt("cin"),
                        rs.getString("email").trim(),
                        rs.getString("mot_de_passe").trim(),
                        rs.getString("adresse").trim(),
                        String.valueOf(rs.getInt("telephone")),
                        dateNaissance,
                        typeUtilisateur,
                        isVerified
                );

                System.out.println("Utilisateur chargé : " + utilisateur); // 🔍 Vérification

                utilisateursTemp.add(utilisateur);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des utilisateurs : " + e.getMessage());
            throw e;
        }

        utilisateurs.addAll(utilisateursTemp);
        System.out.println(utilisateursTemp); // 🔍 Vérifie que l'ID apparaît bien ici
        return utilisateursTemp;
    }

    public List<Utilisateur> chargerTousLesUtilisateurs_mahdi() throws SQLException {
        utilisateurs.clear();
        List<Utilisateur> utilisateursTemp = new ArrayList<>();
        String req = "SELECT id_utilisateur, nom, prenom, cin, email, mot_de_passe, adresse, telephone, date_naissance, type_Utilisateur, is_verified FROM Utilisateur";

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(req)) {

            while (rs.next()) {
                LocalDate dateNaissance = (rs.getDate("date_naissance") != null) ? rs.getDate("date_naissance").toLocalDate() : null;
                TypeUtilisateur typeUtilisateur = TypeUtilisateur.fromString(rs.getString("type_Utilisateur").trim());
                boolean isVerified = rs.getBoolean("is_verified");

                // ✅ Ajout de la récupération de l'ID utilisateur sans toucher au reste
                Utilisateur utilisateur = new Utilisateur(
                        rs.getInt("id_utilisateur"), // 🔥 Correction ici : Récupération de l'ID utilisateur
                        rs.getString("nom").trim(),
                        rs.getString("prenom").trim(),
                        rs.getInt("cin"),
                        rs.getString("email").trim(),
                        rs.getString("mot_de_passe").trim(),
                        rs.getString("adresse").trim(),
                        String.valueOf(rs.getInt("telephone")),
                        dateNaissance,
                        typeUtilisateur,
                        isVerified
                );

                utilisateursTemp.add(utilisateur);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des utilisateurs : " + e.getMessage());
            throw e;
        }

        utilisateurs.addAll(utilisateursTemp);
        return utilisateursTemp;
    }
    public String recupererNumeroParEmail(String email) throws SQLException {
        String query = "SELECT telephone FROM Utilisateur WHERE email = ?";

        try (Connection conn = MyDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("telephone");
            }
        }
        return null; // 📌 Retourne `null` si l'email n'est pas trouvé
    }

    public Map<TypeUtilisateur, Integer> getStatistiquesTypeUtilisateur() {
        Map<TypeUtilisateur, Integer> stats = new HashMap<>();

        String req = "SELECT type_Utilisateur, COUNT(*) AS count FROM Utilisateur GROUP BY type_Utilisateur";
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(req);

            while (rs.next()) {
                TypeUtilisateur type = TypeUtilisateur.valueOf(rs.getString("type_Utilisateur").trim().toUpperCase());
                int count = rs.getInt("count");
                stats.put(type, count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }


    public void ajouterUtilisateur(Utilisateur u) throws SQLException {
        String req = "INSERT INTO Utilisateur (nom, prenom, cin, email, mot_de_passe, adresse, telephone, date_naissance, type_Utilisateur, is_verified) " +
                "VALUES ('" + u.getNom() + "', '"
                + u.getPrenom() + "', "
                + u.getCin() + ", '"
                + u.getEmail() + "', '"
                + PasswordUtils.hashPassword(u.getMotDePasse()) + "', '"
                + u.getAdresse() + "', '"
                + u.getTelephone() + "', '"
                + u.getDateNaissance() + "', '"
                + u.getTypeUtilisateur().name() + "', "
                + (u.isVerified() ? 1 : 0) + ")"; // ✅ Ajout de is_verified (1 si vrai, 0 sinon)

        Statement statement = connection.createStatement();
        int rowsAffected = statement.executeUpdate(req);

        if (rowsAffected > 0) {
            System.out.println("Utilisateur ajouté avec succès.");
            chargerTousLesUtilisateurs();
            System.out.println("Liste des utilisateurs après ajout : " + utilisateurs);
        } else {
            System.out.println("Aucun utilisateur ajouté.");
        }
    }

    public void verifierUtilisateur(String email) throws SQLException {
        String req = "UPDATE Utilisateur SET is_verified = 1 WHERE email = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(req);
        preparedStatement.setString(1, email);

        int rowsAffected = preparedStatement.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Utilisateur vérifié avec succès.");
        } else {
            System.out.println("Erreur : Aucun utilisateur mis à jour.");
        }
    }


    @Override
    public Utilisateur trouverUtilisateurParEmail(String email) {
        try {
            chargerTousLesUtilisateurs();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (Utilisateur ur : utilisateurs) {
            if (ur.getEmail().equals(email)) {
                return ur;
            }
        }
        return null;
    }
    @Override
    public Utilisateur trouverUtilisateurParCin(int cin) {
        try {
            chargerTousLesUtilisateurs();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (Utilisateur ur : utilisateurs) {
            if (ur.getCin() == cin) {
                return ur;
            }
        }
        return null;
    }
    public List<Utilisateur> getUtilisateurs() {
        return utilisateurs;
    }
    public Utilisateur getUtilisateurConnecte() {
        return SessionManager.getUtilisateur(); // Récupérer l'utilisateur connecté depuis la session
    }

    @Override
    public int loginUtilisateur(String email, String motDePasse) throws SQLException {
        Utilisateur utilisateur = trouverUtilisateurParEmail(email);

        if (utilisateur == null) {
            System.out.println("Utilisateur non trouvé !");
            return -1; // Erreur
        }

        if (!PasswordUtils.verifyPassword(motDePasse, utilisateur.getMotDePasse())) {
            System.out.println("Mot de passe incorrect !");
            return -1;
        }

        // Vérifier si l'utilisateur est vérifié
        if (!utilisateur.isVerified()) {
            // Récupérer la date de création du compte
            String req = "SELECT date_creation FROM Utilisateur WHERE email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(req);
            preparedStatement.setString(1, email);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                Timestamp dateCreation = rs.getTimestamp("date_creation");
                LocalDateTime creationDateTime = dateCreation.toLocalDateTime();
                LocalDateTime now = LocalDateTime.now();

                long hoursSinceCreation = ChronoUnit.HOURS.between(creationDateTime, now);

                if (hoursSinceCreation >= 24) {
                    // Supprimer le compte
                    supprimerUtilisateur(utilisateur);
                    System.out.println("Votre compte a été supprimé car il n'a pas été vérifié sous 24h.");
                    return -2; // Code spécial pour compte supprimé
                } else {
                    long remainingHours = 24 - hoursSinceCreation;
                    System.out.println("Votre compte n'est pas encore vérifié. Vous avez encore " + remainingHours + " heures pour le valider.");
                    return -3; // Code spécial pour compte non vérifié
                }
            }
        }
        System.out.println("Utilisateur stocké dans la session : " + SessionManager.getUtilisateur());

        // Stocker l'utilisateur dans la session
        SessionManager.setUtilisateur(utilisateur);
        SessionManager.setUtilisateur(utilisateur);
        System.out.println("Utilisateur stocké dans la session après login : " + SessionManager.getUtilisateur());

        // Vérification du rôle de l'utilisateur
        if ("ADMIN".equalsIgnoreCase(utilisateur.getTypeUtilisateur().name())) {
            System.out.println("Connexion réussie en tant qu'ADMIN. ID Admin : " + utilisateur.getIdUtilisateur());
            return 1;
        } else {
            System.out.println("Connexion réussie en tant qu'UTILISATEUR NORMAL ou VIP. ID utilisateur : " + utilisateur.getIdUtilisateur());
            return 2;
        }

    }


    public void logoutUtilisateur() {
        if (SessionManager.isUserLoggedIn()) {
            System.out.println("Déconnexion de l'utilisateur : " + SessionManager.getUtilisateur().getEmail());
            SessionManager.logout();
            System.out.println("Utilisateur déconnecté avec succès.");
        } else {
            System.out.println("Aucun utilisateur connecté.");
        }
    }
    public List<Utilisateur> filtrerParType(TypeUtilisateur typeUtilisateur) {
        return utilisateurs.stream()
                .filter(utilisateur -> utilisateur.getTypeUtilisateur().name().equalsIgnoreCase(typeUtilisateur.name()))
                .collect(Collectors.toList());
    }





    @Override
    public void modifierUtilisateur(Utilisateur utilisateur) throws SQLException {
        // Trouver l'utilisateur par son CIN dans la liste des utilisateurs
        Utilisateur utilisateurExistant = trouverUtilisateurParCin(utilisateur.getCin());

        // Si l'utilisateur existe, on procède à la mise à jour
        if (utilisateurExistant != null) {
            // Préparer la requête SQL pour mettre à jour les informations de l'utilisateur
            String req = "UPDATE Utilisateur SET " +
                    "nom = '" + utilisateur.getNom() + "', " +
                    "prenom = '" + utilisateur.getPrenom() + "', " +
                    "email = '" + utilisateur.getEmail() + "', " +
                    "adresse = '" + utilisateur.getAdresse() + "', " +
                    "telephone = '" + utilisateur.getTelephone() + "', " +
                    "date_naissance = '" + utilisateur.getDateNaissance() + "', " +
                    "type_Utilisateur = '" + utilisateur.getTypeUtilisateur().toString() + "' " +
                    "WHERE cin = " + utilisateur.getCin();

            Statement statement = connection.createStatement();
            int rowsAffected = statement.executeUpdate(req);

            if (rowsAffected > 0) {
                System.out.println("Utilisateur modifié avec succès dans la base de données.");

                // Mettre à jour l'utilisateur dans la liste
                for (int i = 0; i < utilisateurs.size(); i++) {
                    if (utilisateurs.get(i).getCin() == utilisateur.getCin()) {
                        utilisateurs.set(i, utilisateur);
                        break;
                    }
                }

                System.out.println("Liste des utilisateurs mise à jour.");
            } else {
                System.out.println("Aucune modification effectuée.");
            }
        } else {
            System.out.println("Utilisateur avec le CIN " + utilisateur.getCin() + " non trouvé.");
        }
    }


    public Map<TypeUtilisateur, Long> calculerStatistiquesUtilisateurs() {
        Map<TypeUtilisateur, Long> stats = new HashMap<>();
        String req = "SELECT type_Utilisateur, COUNT(*) AS count FROM Utilisateur GROUP BY type_Utilisateur";

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(req)) {

            while (rs.next()) {
                TypeUtilisateur type = TypeUtilisateur.valueOf(rs.getString("type_Utilisateur").trim().toUpperCase());
                stats.put(type, rs.getLong("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    @Override
    public void supprimerUtilisateur(Utilisateur utilisateur) throws SQLException {
        String req = "DELETE FROM Utilisateur WHERE cin = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(req)) {
            preparedStatement.setInt(1, utilisateur.getCin());
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                utilisateurs.removeIf(u -> u.getCin() == utilisateur.getCin());
                System.out.println("Utilisateur supprimé avec succès.");
            } else {
                System.out.println("Aucun utilisateur supprimé.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'utilisateur : " + e.getMessage());
            throw new SQLException("Impossible de supprimer l'utilisateur.");
        }
    }
    private static final Map<String, String> verificationCodes = new HashMap<>();

    public void envoyerEmailVerification(Utilisateur utilisateur) {
        String code = genererCodeVerification();
        verificationCodes.put(utilisateur.getEmail(), code); // Stocker temporairement

        // Envoyer l'email avec le code
        EmailService.sendVerificationEmail(utilisateur.getEmail(), code);
    }

    private String genererCodeVerification() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Génère un code à 6 chiffres
        return String.valueOf(code);
    }

    public boolean verifierCompte(String email, String code) throws SQLException {
        if (verificationCodes.containsKey(email) && verificationCodes.get(email).equals(code)) {
            // Mettre à jour la base de données pour activer le compte
            String req = "UPDATE Utilisateur SET is_verified = TRUE WHERE email = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(req)) {
                preparedStatement.setString(1, email);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    verificationCodes.remove(email); // Supprimer après vérification
                    System.out.println("Compte vérifié avec succès.");
                    return true;
                }
            }
        }
        System.out.println("Code incorrect ou expiré !");
        return false;
    }

    public void validerCompte(String email) {
        String query = "UPDATE utilisateur SET is_verified = 1 WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.executeUpdate(); // ✅ Mets à jour la base de données
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void fermerConnexion() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            // Réinitialiser la connexion
            connection = MyDatabase.getInstance().getConnection();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture/réinitialisation de la connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // Méthode pour mettre à jour le mot de passe
    public void mettreAJourMotDePasse(String email, String nouveauMotDePasse) throws SQLException {
        connection = MyDatabase.getInstance().getConnection();
        String hashedPassword = PasswordUtils.hashPassword(nouveauMotDePasse);
        String req = "UPDATE Utilisateur SET mot_de_passe = ? WHERE email = ?";

        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, hashedPassword);
            ps.setString(2, email);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("La mise à jour du mot de passe a échoué, aucun utilisateur trouvé avec cet email.");
            }

            // Forcer le rafraîchissement de la connexion
            fermerConnexion();
        }
    }

    // Autres méthodes de la classe...
}

