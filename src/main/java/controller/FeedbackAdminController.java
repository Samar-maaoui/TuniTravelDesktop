package controller;

import entities.Feedback;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.geometry.Insets;
import javafx.scene.effect.DropShadow;
import services.FeedbackAdminService;
import utils.NotificationEmailService;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class FeedbackAdminController {

    @FXML
    private ListView<Feedback> listViewFeedbacks;

    private final FeedbackAdminService feedbackAdminService = new FeedbackAdminService();


    @FXML
    public void initialize() {
        afficherFeedbacks();

        // Set this controller as the scene's user data when the scene is available
        listViewFeedbacks.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setUserData(this);
            }
        });
    }

    @FXML
    private void trierFeedbacksCroissant(ActionEvent actionEvent) {
        try {
            List<Feedback> feedbackList = feedbackAdminService.getAllFeedbacks();
            feedbackList.sort((f1, f2) -> Integer.compare(f1.getNote(), f2.getNote()));
            listViewFeedbacks.setItems(FXCollections.observableArrayList(feedbackList));
        } catch (SQLException e) {
            afficherAlerte("Erreur", "Impossible de trier les feedbacks.");
            e.printStackTrace();
        }
    }

    /**
     * 🔹 Trie les feedbacks par note décroissante.
     */
    @FXML
    private void trierFeedbacksDecroissant(ActionEvent actionEvent) {
        try {
            List<Feedback> feedbackList = feedbackAdminService.getAllFeedbacks();
            feedbackList.sort((f1, f2) -> Integer.compare(f2.getNote(), f1.getNote()));
            listViewFeedbacks.setItems(FXCollections.observableArrayList(feedbackList));
        } catch (SQLException e) {
            afficherAlerte("Erreur", "Impossible de trier les feedbacks.");
            e.printStackTrace();
        }
    }

    /**
     * 🔹 Affiche uniquement les feedbacks dont la note est entre 0 et 5.
     */
    private void afficherFeedbacks() {
        try {
            List<Feedback> feedbackList = feedbackAdminService.getAllFeedbacks();
            System.out.println(feedbackList);
            feedbackList.removeIf(f -> f.getNote() > 5); // Filtrage des notes > 5

            ObservableList<Feedback> observableList = FXCollections.observableArrayList(feedbackList);
            listViewFeedbacks.setCellFactory(param -> new FeedbackListCell2());
            listViewFeedbacks.setItems(observableList);
        } catch (SQLException e) {
            afficherAlerte("Erreur", "Impossible de récupérer les feedbacks.");
            e.printStackTrace();
        }
    }

    /**
     * 🔹 Affiche une boîte de dialogue d'alerte stylisée.
     */
    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-font-size: 14px; -fx-background-color: #f8f9fa;");

        alert.showAndWait();
    }

    /**
     * 🔹 Affiche une boîte de dialogue texte stylisée.
     */
    private Optional<String> afficherBoiteTexte(String titre, String header, String valeurParDefaut) {
        TextInputDialog dialog = new TextInputDialog(valeurParDefaut);
        dialog.setTitle(titre);
        dialog.setHeaderText(header);
        dialog.setContentText("➜ Entrez la nouvelle valeur :");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");

        return dialog.showAndWait();
    }

    /**
     * 🔹 Confirme une action avec un design amélioré.
     */
    private boolean afficherConfirmation(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType boutonOui = new ButtonType("✅ Oui", ButtonBar.ButtonData.YES);
        ButtonType boutonNon = new ButtonType("❌ Non", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(boutonOui, boutonNon);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-font-size: 14px; -fx-background-color: #f8f9fa;");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == boutonOui;
    }

    /**
     * 🔹 Modifie un feedback existant après vérification.
     */


    /**
     * 🔹 Supprime un feedback après confirmation.
     */
    @FXML
    public void supprimerFeedback(ActionEvent actionEvent) {
        Feedback feedbackToDelete = listViewFeedbacks.getSelectionModel().getSelectedItem();

        if (feedbackToDelete == null) {
            afficherAlerte("Erreur", "Veuillez sélectionner un feedback à supprimer.");
            return;
        }

        if (afficherConfirmation("Suppression", "Voulez-vous vraiment supprimer ce feedback ?")) {
            try {
                // Suppression du feedback
                feedbackAdminService.supprimerFeedback(feedbackToDelete);
                afficherFeedbacks();
                afficherAlerte("Succès", "Feedback supprimé avec succès.");

                // Envoi de l'e-mail de notification
                String emailClient = feedbackToDelete.getUtilisateur().getEmail();
                String subject = "Notification de suppression de votre feedback";

                String message = "<html><body style='font-family:Arial, sans-serif; color:#333;'>"
                        + "<div style='text-align:center;'>"
                        + "<img src='cid:logo' alt='Itinereves' style='width:150px; margin-bottom:20px;'/>"
                        + "</div>"
                        + "<p>Bonjour <strong>" + feedbackToDelete.getUtilisateur().getNom() + "</strong>,</p>"
                        + "<p>Nous souhaitons vous informer que votre feedback a été supprimé par notre équipe d’administration.</p>"
                        + "<p>Voici les détails du feedback concerné :</p>"
                        + "<ul style='background:#f9f9f9; padding:15px; border-radius:5px; list-style:none;'>"
                        + "<li><strong>Note :</strong> " + feedbackToDelete.getNote() + "/5</li>"
                        + "<li><strong>Commentaire :</strong> " + feedbackToDelete.getCommentaire() + "</li>"
                        + "<li><strong>Date de soumission :</strong> " + feedbackToDelete.getDateFeedback() + "</li>"
                        + "<li><strong>Date de suppression :</strong> " + LocalDate.now() + "</li>"
                        + "</ul>"
                        + "<p>Si vous avez des questions ou souhaitez en savoir plus sur cette décision, n’hésitez pas à nous contacter.</p>"
                        + "<p>Nous vous remercions de votre compréhension et restons à votre disposition.</p>"
                        + "<p>Cordialement,</p>"
                        + "<p><strong>L’équipe Itinereves</strong></p>"
                        + "<p>📧 contact@itinereves.com | 📞 +216 123 456 789</p>"
                        + "<hr style='border:0; height:1px; background:#ddd;'/>"
                        + "<p style='text-align:center; font-size:12px; color:#777;'>"
                        + "© 2025 Itinereves. Tous droits réservés."
                        + "</p>"
                        + "</body></html>";


                // Vérifier si le logo existe
                String logoPath = "src/main/resources/images/logopng.png";
                File logoFile = new File(logoPath);
                if (!logoFile.exists()) {
                    System.err.println("Erreur : Le fichier logo n'existe pas à l'emplacement : " + logoPath);
                    return;
                }

                // Envoi de l'e-mail
                NotificationEmailService.sendNotificationEmailWithAttachment(emailClient, subject, message, logoPath, "logo");
                System.out.println("E-mail de suppression envoyé à : " + emailClient);

            } catch (SQLException e) {
                afficherAlerte("Erreur", "Impossible de supprimer ce feedback.");
                e.printStackTrace();
            }
        }
    }



}




