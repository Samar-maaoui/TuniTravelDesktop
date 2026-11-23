package services;

import entities.Feedback;
import entities.Utilisateur;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FeedbackAdminService implements IFeedback<Feedback> {

    private Connection connection;

    public FeedbackAdminService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    public List<Feedback> getAllFeedbacks() throws SQLException {
        List<Feedback> feedbackList = new ArrayList<>();

        // Requête SQL pour récupérer tous les feedbacks avec les informations de l'utilisateur
        String query = "SELECT f.id_feedback, f.commentaire, f.note, f.date_feedback, " +
                "u.id_utilisateur, u.nom, u.prenom, u.cin, u.email " +
                "FROM feedback f " +
                "JOIN utilisateur u ON f.utilisateur_id = u.id_utilisateur";

        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int idFeedback = rs.getInt("id_feedback");
                String commentaire = rs.getString("commentaire");
                int note = rs.getInt("note");
                LocalDateTime dateFeedback = rs.getTimestamp("date_feedback").toLocalDateTime();

                int idUtilisateur = rs.getInt("id_utilisateur");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                int cin = rs.getInt("cin");
                String email = rs.getString("email");

                // Création de l'objet Utilisateur
                Utilisateur utilisateur = new Utilisateur(nom, prenom, cin, email);
                utilisateur.setIdUtilisateur(idUtilisateur); // Ajout de l'ID utilisateur

                // Création de l'objet Feedback
                Feedback feedback = new Feedback(commentaire, note, dateFeedback, utilisateur);
                feedback.setIdFeedback(idFeedback); // Ajout de l'ID feedback

                feedbackList.add(feedback);
            }
        }
        return feedbackList;
    }

    @Override
    public List<Feedback> trouverFeedbackPar(int cin) throws SQLException {
        List<Feedback> feedbackList = new ArrayList<>();
        String query = "SELECT f.id_feedback, f.commentaire, f.note, f.date_feedback, " +
                "u.id_utilisateur, u.nom, u.prenom, u.cin, u.email " +
                "FROM feedback f " +
                "JOIN utilisateur u ON f.utilisateur_id = u.id_utilisateur " +
                "WHERE u.cin = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, cin);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int idFeedback = rs.getInt("id_feedback");
                String commentaire = rs.getString("commentaire");
                int note = rs.getInt("note");
                LocalDateTime dateFeedback = rs.getTimestamp("date_feedback").toLocalDateTime();

                int idUtilisateur = rs.getInt("id_utilisateur");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String email = rs.getString("email");

                Utilisateur utilisateur = new Utilisateur(nom, prenom, cin, email);
                utilisateur.setIdUtilisateur(idUtilisateur);

                Feedback feedback = new Feedback(commentaire, note, dateFeedback, utilisateur);
                feedback.setIdFeedback(idFeedback);

                feedbackList.add(feedback);
            }
        }
        return feedbackList;
    }

    @Override
    public void ajouterFeedback(Feedback f) throws SQLException {
        String query = "INSERT INTO feedback (commentaire, note, date_feedback, utilisateur_id) VALUES (?, ?, CURRENT_TIMESTAMP, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, f.getCommentaire());
            ps.setInt(2, f.getNote());
            ps.setInt(3, f.getUtilisateur().getIdUtilisateur());

            ps.executeUpdate();
            System.out.println("Feedback ajouté avec succès !");
        }
    }

    public Feedback getFeedbackById(int idFeedback) throws SQLException {
        String query = "SELECT f.id_feedback, f.commentaire, f.note, f.date_feedback, " +
                "u.id_utilisateur, u.nom, u.prenom, u.cin, u.email " +
                "FROM feedback f " +
                "JOIN utilisateur u ON f.utilisateur_id = u.id_utilisateur " +
                "WHERE f.id_feedback = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idFeedback);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String commentaire = rs.getString("commentaire");
                int note = rs.getInt("note");
                LocalDateTime dateFeedback = rs.getTimestamp("date_feedback").toLocalDateTime();

                int idUtilisateur = rs.getInt("id_utilisateur");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                int cin = rs.getInt("cin");
                String email = rs.getString("email");

                Utilisateur utilisateur = new Utilisateur(nom, prenom, cin, email);
                utilisateur.setIdUtilisateur(idUtilisateur);

                Feedback feedback = new Feedback(commentaire, note, dateFeedback, utilisateur);
                feedback.setIdFeedback(idFeedback);

                return feedback;
            }
        }
        return null;
    }

    public void modifierFeedback(Feedback feedback) throws SQLException {
        String query = "UPDATE feedback SET commentaire = ?, note = ?, date_feedback = CURRENT_TIMESTAMP WHERE id_feedback = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, feedback.getCommentaire());
            ps.setInt(2, feedback.getNote());
            ps.setInt(3, feedback.getIdFeedback());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Feedback modifié avec succès !");
            } else {
                System.out.println("Erreur lors de la modification du feedback.");
            }
        }
    }

    public void supprimerFeedback(Feedback feedback) throws SQLException {
        String query = "DELETE FROM feedback WHERE id_feedback = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, feedback.getIdFeedback());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Feedback supprimé avec succès !");
            } else {
                System.out.println("Erreur lors de la suppression du feedback.");
            }
        }
    }
}
