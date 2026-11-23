package controller;

import entities.ReservationExcursion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.ReservationExcursionService;
import utils.SessionManager;

import java.sql.SQLException;
import java.util.List;

public class DisplayEventController {

    @FXML
    private ListView<ReservationExcursion> reservationsList;

    private ObservableList<ReservationExcursion> reservationsObservable;

    @FXML
    public void initialize() {
        try {
            int utilisateurId = SessionManager.getUtilisateur().getIdUtilisateur();

            List<ReservationExcursion> reservations = new ReservationExcursionService().lireParUtilisateur(utilisateurId);
            reservationsObservable = FXCollections.observableArrayList(reservations);
            reservationsList.setItems(reservationsObservable);

            reservationsList.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(ReservationExcursion item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText("Excursion : " + item.getExcursion().getNom() +
                                " | Places : " + item.getNbrPlaceReservee() +
                                " | Total : " + item.getMontantTotal() + " DT");
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur SQL", "Impossible de charger les réservations.");
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        ReservationExcursion selected = reservationsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            TextInputDialog dialog = new TextInputDialog(String.valueOf(selected.getNbrPlaceReservee()));
            dialog.setTitle("Modifier la réservation");
            dialog.setHeaderText("Modification des places réservées");
            dialog.setContentText("Nouveau nombre de places :");

            dialog.showAndWait().ifPresent(input -> {
                try {
                    int newNbr = Integer.parseInt(input);
                    if (newNbr <= 0) {
                        showAlert("Erreur", "Le nombre de places doit être supérieur à zéro.");
                        return;
                    }

                    selected.setNbrPlaceReservee(newNbr);
                    boolean success = new ReservationExcursionService().modifier(selected);
                    if (success) {
                        reservationsList.refresh(); // Met à jour l'affichage
                        showAlert("Succès", "La réservation a été mise à jour.");
                    }
                } catch (NumberFormatException e) {
                    showAlert("Erreur", "Veuillez entrer un nombre valide.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Erreur SQL", "Erreur lors de la mise à jour de la réservation.");
                }
            });
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner une réservation à modifier.");
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        ReservationExcursion selected = reservationsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmer la suppression");
            confirmation.setHeaderText("Suppression de la réservation");
            confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette réservation ?");

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        new ReservationExcursionService().supprimer(selected.getIdReservation());
                        reservationsObservable.remove(selected);
                        showAlert("Succès", "Réservation supprimée avec succès.");
                    } catch (SQLException e) {
                        e.printStackTrace();
                        showAlert("Erreur SQL", "Erreur lors de la suppression.");
                    }
                }
            });
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner une réservation à supprimer.");
        }
    }

    // Méthode utilitaire
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
