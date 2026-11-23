package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;

import javafx.stage.Stage;
import services.c_vol; // Assurez-vous que c'est bien le service contenant la méthode supprimer()

public class supprimerVolController {
    @FXML
    private Button gotoafficher;
    @FXML
    private TextField numeroVolField;

    private final c_vol volService = new c_vol(); // Instance du service de gestion des vols

    @FXML
    public void supprimervol(ActionEvent actionEvent) {
        String numeroVol = numeroVolField.getText().trim();

        // Vérification si le champ est vide
        if (numeroVol.isEmpty()) {
            showAlert("Erreur", "Veuillez entrer un numéro de vol.", Alert.AlertType.ERROR);
            return;
        }

        try {
            // Appeler la méthode de suppression
            volService.supprimer(numeroVol);
            showAlert("Succès", "Le vol a été supprimé avec succès.", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de supprimer le vol : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    @FXML
    public void gotoafficher(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/afficherVol.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) gotoafficher.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Méthode utilitaire pour afficher une alerte
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
