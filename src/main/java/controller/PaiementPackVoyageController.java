package controller;

import entities.ReservationPackVoyage;
import entities.StatutPaiement;
import entities.StatutReservation;
import entities.Utilisateur;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import services.PayPalPaymentService;
import services.ReservationPackVoyageService;
import utils.SessionManager;

import java.sql.SQLException;
import java.util.List;

public class PaiementPackVoyageController {

    @FXML
    private ListView<ReservationPackVoyage> listViewReservations;

    @FXML
    private Button btnPayer;

    private ReservationPackVoyageService reservationService;

    public PaiementPackVoyageController() {
        this.reservationService = new ReservationPackVoyageService();
    }

    public void initialize() {
        afficherListeReservations();
    }

    public void afficherListeReservations() {
        try {
            int utilisateurId = SessionManager.getInstance().getUtilisateur().getIdUtilisateur();
            List<ReservationPackVoyage> reservations = new ReservationPackVoyageService().listerReservations(utilisateurId);

            listViewReservations.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(ReservationPackVoyage reservation, boolean empty) {
                    super.updateItem(reservation, empty);
                    if (empty || reservation == null) {
                        setText(null);
                    } else {
                        String displayText = "Pack Voyage: " + reservation.getPackVoyage().getNomPack() +
                                ", Statut: " + reservation.getStatut().name() +
                                ", Statut Paiement: " + reservation.getStatutPaiement().name();
                        setText(displayText);
                    }
                }
            });

            listViewReservations.getItems().setAll(reservations);

            boolean hasNonPayee = reservations.stream().anyMatch(r -> r.getStatutPaiement() == StatutPaiement.NON_PAYE);
            btnPayer.setDisable(!hasNonPayee);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la récupération des réservations.");
        }
    }

    @FXML
    private void payerReservation() {
        ReservationPackVoyage selectedReservation = listViewReservations.getSelectionModel().getSelectedItem();

        if (selectedReservation == null) {
            showAlert("Erreur", "Veuillez sélectionner une réservation à payer.");
            return;
        }

        if (selectedReservation.getStatutPaiement() == StatutPaiement.PAYE) {
            showAlert("Erreur", "Cette réservation a déjà été payée.");
            return;
        }

        try {
            String paypalUrl = PayPalPaymentService.createPayment(
                    selectedReservation.getMontantTotal(),
                    "USD",
                    "Paiement pour " + selectedReservation.getPackVoyage().getNomPack(),
                    "https://cancel.fake",
                    "https://success.fake"
            );

            Stage stage = new Stage();
            WebView web = new WebView();
            WebEngine engine = web.getEngine();
            engine.load(paypalUrl);

            engine.locationProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue.contains("success.fake")) {
                        try {
                            reservationService.mettreAJourStatutPaiementEtConfirmation(selectedReservation.getIdReservation());
                            showAlert("Succès", "Paiement validé avec succès.");
                            afficherListeReservations();
                        } catch (SQLException e) {
                            showAlert("Erreur", "Erreur lors de la mise à jour du statut de la réservation.");
                        }
                        stage.close();
                    } else if (newValue.contains("cancel.fake")) {
                        showAlert("Annulation", "Paiement annulé par l'utilisateur.");
                        stage.close();
                    }
                }
            });

            stage.setScene(new javafx.scene.Scene(web, 900, 700));
            stage.setTitle("Paiement PayPal");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Le paiement PayPal a échoué : " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
