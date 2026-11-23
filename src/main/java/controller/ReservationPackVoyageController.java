package controller;

import entities.*;

import services.ReservationPackVoyageService;
import services.MailerSendService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import utils.SessionManager;
import utils.ReservationUtils;

public class ReservationPackVoyageController {

    @FXML private Label nomPackLabel;
    @FXML private TextField placesDemandeesField;

    private Post post;

    public void setPost(Post post) {
        this.post = post;
        afficherDetailsPack();
    }

    private void afficherDetailsPack() {
        if (post != null && post.getPackVoyage() != null) {
            // Remplir le champ avec le nom du pack
            nomPackLabel.setText(post.getPackVoyage().getNomPack());
        }
    }

    // Méthode pour gérer la réservation
    @FXML
    private void reserverPackVoyage() {
        Utilisateur utilisateurConnecte = SessionManager.getInstance().getUtilisateur();

        try {
            int placesDemandees = Integer.parseInt(placesDemandeesField.getText());

            // Vérification du nombre de places demandées
            if (placesDemandees <= 0) {
                throw new NumberFormatException("Le nombre de places demandées doit être supérieur à 0.");
            }

            // Vérification des places disponibles
            if (placesDemandees > post.getPackVoyage().getNombrePlacesRestants()) {
                throw new NumberFormatException("Le nombre de places demandées dépasse les places restantes.");
            }

            // Générer une clé unique pour chaque réservation
            String reservationKey = ReservationUtils.generateUniqueKey();

            // Créer la réservation
            ReservationPackVoyage reservation = new ReservationPackVoyage(
                    0,
                    utilisateurConnecte.getIdUtilisateur(),
                    new java.sql.Timestamp(System.currentTimeMillis()),
                    StatutReservation.EN_ATTENTE,
                    StatutPaiement.NON_PAYE,
                    post.getPackVoyage(),
                    post,
                    placesDemandees,
                    post.getPackVoyage().getPrixTotal() * placesDemandees,
                    reservationKey
            );

            // Enregistrement dans la base de données
            new ReservationPackVoyageService().ajouterReservation(reservation);

            // Envoi de l'email de confirmation
            String email = utilisateurConnecte.getEmail();
            String subject = "Confirmation de votre réservation - En attente de paiement";
            String body = "Bonjour,\n\nVotre réservation est en attente de paiement. Détails :\n" +
                    "Pack: " + post.getPackVoyage().getNomPack() + "\n" +
                    "Places réservées: " + placesDemandees + "\n" +
                    "Montant: " + reservation.getMontantTotal() + "€\n" +
                    "Date: " + reservation.getDateReservation() + "\nStatut de paiement: En attente.";
            MailerSendService.sendEmail(email, subject, body);

            // Fermeture de la fenêtre de réservation
            Stage stage = (Stage) nomPackLabel.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            // Affichage d'un message d'erreur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de réservation");
            alert.setContentText("Erreur lors de la réservation : " + e.getMessage());
            alert.showAndWait();
        }
    }

    // Méthode pour annuler la réservation et fermer la fenêtre
    @FXML
    private void annulerReservation() {
        Stage stage = (Stage) nomPackLabel.getScene().getWindow();
        stage.close();
    }
}
