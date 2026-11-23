package services;

import entities.Feedback;
import entities.Utilisateur;
import utils.MyDatabase;
import utils.SessionManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FeedbackService implements IFeedback<Feedback> {
    private Connection connection;

    public FeedbackService() {
        connection = MyDatabase.getInstance().getConnection();
    }


    @Override
    public void ajouterFeedback(Feedback f) throws SQLException {
        Utilisateur utilisateurConnecte = SessionManager.getUtilisateur();
        if (utilisateurConnecte == null) {
            System.out.println("❌ Aucun utilisateur connecté !");
        } else {
            System.out.println("✅ Utilisateur connecté : " + utilisateurConnecte.getIdUtilisateur());
        }
        String req = "INSERT INTO feedback (utilisateur_id, commentaire, note, date_feedback) " +
                "VALUES (?, ?, ?, ?)";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, f.getUtilisateur().getIdUtilisateur());
        ps.setString(2, f.getCommentaire().replace("'", "''"));
        ps.setInt(3, f.getNote());
        ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now())); // Ajoute la date et l'heure actuelles



        int rowsAffected = ps.executeUpdate();

        if (rowsAffected > 0) {
            System.out.println("Feedback ajouté avec succès !");

            // Récupérer l'utilisateur connecté via la session

            if (utilisateurConnecte != null) {
                // Ajouter le feedback à la liste de feedbacks de l'utilisateur
                utilisateurConnecte.ajouterFeedback(f);
            }
        } else {
            System.out.println("Erreur lors de l'ajout du feedback.");
        }
    }


    @Override
    public void modifierFeedback(Feedback f) throws SQLException {
        // Vérifier si l'utilisateur connecté est bien le propriétaire du feedback
        if (!estFeedbackDeUtilisateurConnecte(f.getIdFeedback())) {
            System.out.println("Accès refusé : Vous ne pouvez modifier que vos propres feedbacks !");
            return;
        }

        String query = "UPDATE feedback SET commentaire = ?, note = ?, date_feedback = CURRENT_TIMESTAMP WHERE id_feedback = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, f.getCommentaire());
        ps.setInt(2, f.getNote());
        ps.setInt(3, f.getIdFeedback());

        int rowsAffected = ps.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Feedback modifié avec succès !");
        } else {
            System.out.println("Erreur lors de la modification du feedback.");
        }
    }



    @Override
    public void supprimerFeedback(Feedback f) throws SQLException {
        // Vérifier si l'utilisateur est bien le propriétaire du feedback
        if (!estFeedbackDeUtilisateurConnecte(f.getIdFeedback())) {
            System.out.println("Accès refusé : Vous ne pouvez supprimer que vos propres feedbacks !");
            return;
        }

        // Récupérer la date du feedback
        LocalDateTime dateFeedback = f.getDateFeedback();

        // Requête SQL pour supprimer les feedbacks antérieurs ou égaux à la date du feedback

        String query = "DELETE FROM feedback WHERE date_feedback = ?";

        PreparedStatement ps = connection.prepareStatement(query);
        ps.setTimestamp(1, Timestamp.valueOf(dateFeedback));  // Convertir LocalDateTime en Timestamp

        // Exécuter la requête et obtenir le nombre de lignes affectées
        int rowsAffected = ps.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Feedback supprimé avec succès !");
        } else {
            System.out.println("Erreur lors de la suppression du feedback.");
        }
    }


    @Override
    public List<Feedback> trouverFeedbackPar(int utilisateurId) throws SQLException {
        // Vérifier si un utilisateur est connecté
        Utilisateur utilisateurConnecte = SessionManager.getUtilisateur();
        if (utilisateurConnecte == null) {
            System.out.println("Aucun utilisateur connecté !");
            return new ArrayList<>();
        }

        // Vérifier si l'utilisateur connecté cherche ses propres feedbacks
        if (utilisateurConnecte.getIdUtilisateur() != utilisateurId) {
            System.out.println("Accès refusé : Vous ne pouvez voir que vos propres feedbacks !");
            return new ArrayList<>();
        }

        List<Feedback> feedbacks = new ArrayList<>();
        String query = "SELECT * FROM feedback WHERE utilisateur_id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, utilisateurId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            // Récupérer la valeur de date_feedback en tant que Timestamp
            java.sql.Timestamp timestamp = rs.getTimestamp("date_feedback");

            // Convertir en LocalDateTime si nécessaire
            java.time.LocalDateTime dateFeedback = timestamp != null ? timestamp.toLocalDateTime() : null;

            // Créer le feedback
            Feedback feedback = new Feedback(
                    rs.getInt("id_feedback"),
                    rs.getString("commentaire"),
                    rs.getInt("note"),
                    dateFeedback, // Utiliser LocalDateTime si besoin
                    utilisateurConnecte // L'utilisateur connecté est l'auteur du feedback
            );
            feedbacks.add(feedback);
        }

        return feedbacks;
    }

    // Méthode privée pour vérifier si un feedback appartient à l'utilisateur connecté
    private boolean estFeedbackDeUtilisateurConnecte(int feedbackId) throws SQLException {
        Utilisateur utilisateurConnecte = SessionManager.getUtilisateur();
        if (utilisateurConnecte == null) {
            return false; // Aucun utilisateur connecté
        }

        String query = "SELECT utilisateur_id FROM feedback WHERE id_feedback = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, feedbackId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            int feedbackOwnerId = rs.getInt("utilisateur_id");
            return feedbackOwnerId == utilisateurConnecte.getIdUtilisateur();
        }
        return false;
    }
}
