package controller;

import entities.reservationEvenement;
import entities.TypeUtilisateur;
import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import services.ServiceReservationEvenement;
import utils.SessionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ReservationEvenementController {

    @FXML
    private VBox eventCardsContainer;

    @FXML
    private ScrollPane scrollPane;

    private int getCurrentUserId() {
        return SessionManager.getInstance().getUtilisateur().getIdUtilisateur();
    }

    @FXML
    public void initialize() {
        loadEvents();
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
    }

    private VBox createEventCard() {
        VBox card = new VBox(3);
        card.getStyleClass().add("event-card");
        VBox content = new VBox(5);

        if (isAdminUser()) {
            ServiceReservationEvenement serviceEvenement = new ServiceReservationEvenement();
            List<String> reservations = serviceEvenement.getAllReservationsForAdmin();
            for (String reservation : reservations) {
                Label reservationLabel = new Label(reservation);
                reservationLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495E;");
                content.getChildren().add(reservationLabel);
            }
        } else if (isVipUser()) {
            int userId = getCurrentUserId();
            ServiceReservationEvenement serviceEvenement = new ServiceReservationEvenement();
            List<String> userReservations = serviceEvenement.getReservationsForUser(userId);
            for (String reservation : userReservations) {
                Label reservationLabel = new Label(reservation);
                reservationLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495E;");
                content.getChildren().add(reservationLabel);
            }
        }

        return card;
    }

    private void loadEvents() {
        ServiceReservationEvenement serviceReservationEvenement = new ServiceReservationEvenement();

        List<String> reservations;
        if (isAdminUser()) {
            reservations = serviceReservationEvenement.getAllReservationsForAdmin();
        } else if (isVipUser()) {
            int userId = getCurrentUserId();
            reservations = serviceReservationEvenement.getReservationsForUser(userId);
        } else {
            reservations = new ArrayList<>();
        }

        eventCardsContainer.getChildren().clear();

        if (reservations != null && !reservations.isEmpty()) {
            for (String reservation : reservations) {
                Label reservationLabel = new Label(reservation);
                reservationLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495E;");
                eventCardsContainer.getChildren().add(reservationLabel);
            }
        } else {
            Label noReservationsLabel = new Label("Aucune réservation disponible.");
            eventCardsContainer.getChildren().add(noReservationsLabel);
        }
    }

    private boolean isAdminUser() {
        Utilisateur utilisateur = SessionManager.getUtilisateur();
        return utilisateur != null && utilisateur.getTypeUtilisateur() == TypeUtilisateur.ADMIN;
    }

    private boolean isVipUser() {
        Utilisateur utilisateur = SessionManager.getUtilisateur();
        return utilisateur != null && utilisateur.getTypeUtilisateur() == TypeUtilisateur.VIP;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
