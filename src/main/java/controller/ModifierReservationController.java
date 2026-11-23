package controller;

import entities.ReservationHotel;
import services.ServiceReservationHotel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Date;
import java.time.LocalDate;

public class ModifierReservationController {

    @FXML
    private DatePicker dpCheckIn, dpCheckOut;
    @FXML
    private ComboBox<String> cbTypeChambre;
    @FXML
    private Label lblPrix; // Label for displaying the price
    @FXML
    private Button btnModifier, btnAnnuler;

    private final ServiceReservationHotel serviceReservationHotel = new ServiceReservationHotel();
    private ReservationHotel currentReservation;
    private Runnable refreshCallback; // Callback for refreshing reservations list

    // Method to load reservation details into the fields
    public void loadReservationInfo(ReservationHotel reservation) {
        this.currentReservation = reservation;
        dpCheckIn.setValue(reservation.getCheck_in_date().toLocalDate());
        dpCheckOut.setValue(reservation.getCheck_out_date().toLocalDate());
        cbTypeChambre.setValue(reservation.getType_chambre());
        lblPrix.setText("Prix : " + calculerPrix(reservation) + " DT/nuit");

        cbTypeChambre.getItems().addAll("Simple", "Double", "Suite");
    }

    // Method to calculate the price according to the room type
    private double calculerPrix(ReservationHotel reservation) {
        switch (reservation.getType_chambre()) {
            case "Simple":
                return 120; // Example price for a single room
            case "Double":
                return 120 * 1.5; // Price for a double room
            case "Suite":
                return 120 * 2; // Price for a suite
            default:
                return 0; // Returns 0 if no valid type
        }
    }

    // Set the callback for refreshing after modification
    public void setRefreshCallback(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
    }

    @FXML
    private void modifierReservation() {
        try {
            // Validate input data
            LocalDate checkIn = dpCheckIn.getValue();
            LocalDate checkOut = dpCheckOut.getValue();
            String typeChambre = cbTypeChambre.getValue();

            if (checkIn == null || checkOut == null || typeChambre == null) {
                showAlert(Alert.AlertType.WARNING, "Erreur", "Tous les champs doivent être remplis.");
                return;
            }

            // Update the reservation details
            currentReservation.setCheck_in_date(Date.valueOf(checkIn));
            currentReservation.setCheck_out_date(Date.valueOf(checkOut));
            currentReservation.setType_chambre(typeChambre);

            // Update reservation in database or data source
            serviceReservationHotel.modifierReservation(currentReservation);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Réservation modifiée avec succès !");
            fermerFenetre();

            // Trigger refresh of reservations list
            if (refreshCallback != null) {
                refreshCallback.run();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification de la réservation : " + e.getMessage());
        }
    }

    @FXML
    private void fermerFenetre() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}