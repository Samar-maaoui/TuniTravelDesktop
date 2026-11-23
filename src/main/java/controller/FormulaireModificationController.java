package controller;

import entities.Guide;
import services.GuideService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;

public class FormulaireModificationController {

    private Guide guide;
    private GuideService guideService = new GuideService();
    private AfficherGuideController afficherGuideController;

    // FXML elements mapped from the FXML file

    @FXML
    private TextField telephone; // Correct this to match the fx:id in FXML
    @FXML
    private TextField email; // Correct this to match the fx:id in FXML
    @FXML
    private CheckBox francais; // FXML checkboxes
    @FXML
    private CheckBox anglais;
    @FXML
    private CheckBox espagnol;
    @FXML
    private Button btn; // Modifier button

    public FormulaireModificationController() throws SQLException {
    }

    public void setAfficherGuideController(AfficherGuideController afficherGuideController) {
        this.afficherGuideController = afficherGuideController;
    }

    // Set the guide object and initialize form fields
    public void setGuide(Guide guide) {
        this.guide = guide;
        // Fill in the fields with the current guide data
        email.setText(guide.getEmail());
        telephone.setText(guide.getTelephone());

        // You can also set the language checkboxes based on the guide's data
        francais.setSelected(guide.getLangue().contains("Français"));
        anglais.setSelected(guide.getLangue().contains("Anglais"));
        espagnol.setSelected(guide.getLangue().contains("Espagnol"));
    }

    @FXML
    private void handleSave() throws SQLException {
        // Vérifier l'email
        String emailValue = email.getText();
        if (!emailValue.matches("^[a-zA-Z0-9._%+-]+@gmail\\.com$")) {
            showAlert("Erreur", "L'email doit être une adresse Gmail valide contenant '@gmail.com' !");
            return;
        }

        // Vérifier le numéro de téléphone
        String telephoneValue = telephone.getText();
        if (!telephoneValue.matches("^\\d{8}$")) {
            showAlert("Erreur", "Le numéro de téléphone doit contenir exactement 8 chiffres !");
            return;
        }

        // Vérifier l'unicité de l'email et du téléphone
        if (guideService.isEmailExist(emailValue)) {
            showAlert("Erreur", "Cet email est déjà utilisé !");
            return;
        }

        if (guideService.isTelephoneExist(telephoneValue)) {
            showAlert("Erreur", "Ce numéro de téléphone est déjà utilisé !");
            return;
        }

        // Mettre à jour l'objet guide avec les valeurs du formulaire
        guide.setEmail(emailValue);
        guide.setTelephone(telephoneValue);

        // Ajouter la gestion des langues
        StringBuilder langues = new StringBuilder();
        if (francais.isSelected()) langues.append("Français ");
        if (anglais.isSelected()) langues.append("Anglais ");
        if (espagnol.isSelected()) langues.append("Espagnol ");
        guide.setLangue(langues.toString().trim());

        // Mettre à jour le guide dans la base de données
        try {
            if (guideService.modifier(guide)) {
                showAlert("Succès", "Guide mis à jour avec succès !");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Échec de la mise à jour du guide.");
        }

        // Fermer la fenêtre après modification
        ((Stage) email.getScene().getWindow()).close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleModifier(ActionEvent actionEvent) {
        // This method could be used for additional actions if needed
    }
}
