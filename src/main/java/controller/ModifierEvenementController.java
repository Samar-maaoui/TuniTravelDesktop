package controller;

import entities.Evenement;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import services.ServiceEvenement;
import javafx.scene.control.Alert;
import java.time.LocalDate;

public class ModifierEvenementController {

    @FXML private TextField txtNom;
    @FXML private TextField txtDescription;
    @FXML private TextField txtLieu;
    @FXML private DatePicker datePicker;
    @FXML private TextField txtCapacite;
    @FXML private ComboBox<String> comboBoxType;
    @FXML private TextField txtOrganisateur;
    @FXML private TextField txtPrix;
    @FXML private Label labelIdEvenement;
    private static final String[] VALID_EVENT_TYPES = {"CONFÉRENCE", "SÉMINAIRE", "CONCERT", "SPORT", "AUTRE"};
    @FXML private VBox formContainer;

    private Evenement evenement;
    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            if (txtNom.getScene() != null) {
                txtNom.getScene().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            }
        });
        comboBoxType.getItems().addAll(VALID_EVENT_TYPES);
    }


    public void loadEvenement(int evenementId) {
        try {
            // Retrieve the existing event data based on the provided ID
            ServiceEvenement serviceEvenement = new ServiceEvenement();
            evenement = serviceEvenement.getEvenementById(evenementId);  // Assuming this method exists

            if (evenement != null) {

                txtNom.setText(evenement.getNom());
                txtLieu.setText(evenement.getLieu());
                datePicker.setValue(LocalDate.parse(evenement.getDateEvenement()));  // Convert string to LocalDate
                txtCapacite.setText(String.valueOf(evenement.getCapacite()));
                comboBoxType.setValue(evenement.getType());  // Set the combo box to the current event type
                txtOrganisateur.setText(evenement.getOrganisateur());
                txtDescription.setText(evenement.getDescription());
                txtPrix.setText(String.valueOf(evenement.getPrix()));

            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger l'événement.");
        }
    }


    @FXML
    private void modifierEvenement() {
        try {

            LocalDate eventDate = datePicker.getValue();

            if (eventDate == null) {
                showAlert("Erreur", "Veuillez sélectionner une date valide.");
                return;
            }


            Evenement updatedEvenement = new Evenement(
                    evenement.getIdEvenement(),  // Keep the same ID to modify the existing event
                    txtNom.getText().isEmpty() ? evenement.getNom() : txtNom.getText(),  // Only update if modified
                    txtLieu.getText().isEmpty() ? evenement.getLieu() : txtLieu.getText(),
                    eventDate.toString(),
                    txtCapacite.getText().isEmpty() ? evenement.getCapacite() : Integer.parseInt(txtCapacite.getText()),
                    comboBoxType.getValue().isEmpty() ? evenement.getType() : comboBoxType.getValue(),
                    txtOrganisateur.getText().isEmpty() ? evenement.getOrganisateur() : txtOrganisateur.getText(),
                    txtDescription.getText().isEmpty() ? evenement.getDescription() : txtDescription.getText(),
                    txtPrix.getText().isEmpty() ? evenement.getPrix() : Double.parseDouble(txtPrix.getText())
            );


            ServiceEvenement serviceEvenement = new ServiceEvenement();
            serviceEvenement.modifierEvenement(updatedEvenement);

            showAlert("Succès", "L'événement a été modifié avec succès.");
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer des valeurs numériques valides pour la capacité et le prix.");
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur s'est produite. Veuillez vérifier vos entrées.");
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
