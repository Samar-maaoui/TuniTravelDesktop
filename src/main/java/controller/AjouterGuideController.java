package controller;

import entities.Guide;
import services.GuideService;
import utils.ImageUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AjouterGuideController {

    @FXML private TextField telephone;
    @FXML private TextField nom;
    @FXML private TextField prenom;
    @FXML private TextField email;
    @FXML private CheckBox francais;
    @FXML private CheckBox anglais;
    @FXML private CheckBox espagnol;
    @FXML private Button btnAjouter;
    @FXML private Button btnChoisirImage;
    @FXML private ImageView imageView;

    private GuideService guideService;
    private String imageUrl = ""; // Stocke le chemin de l'image
    private File selectedImageFile;

    public AjouterGuideController() throws SQLException {
        guideService = new GuideService();
    }

    @FXML
    private void handleAjouter() throws SQLException {
        System.out.println("Le bouton Ajouter a été cliqué");

        String nomValue = nom.getText().trim();
        String prenomValue = prenom.getText().trim();
        String telephoneValue = telephone.getText().trim();
        String emailValue = email.getText().trim();

        if (nomValue.isEmpty() || prenomValue.isEmpty() || telephoneValue.isEmpty() || emailValue.isEmpty()) {
            showAlert("Erreur", "Tous les champs doivent être remplis.");
            return;
        }

        if (!emailValue.matches("^[a-zA-Z0-9._%+-]+@gmail\\.com$")) {
            showAlert("Erreur", "L'email doit être une adresse Gmail valide (ex: exemple@gmail.com).");
            return;
        }

        if (!telephoneValue.matches("\\d{8}")) {
            showAlert("Erreur", "Le téléphone doit être un numéro valide de 8 chiffres.");
            return;
        }

        if (guideService.isEmailExist(emailValue)) {
            showAlert("Erreur", "Cet email existe déjà.");
            return;
        }
        if (guideService.isTelephoneExist(telephoneValue)) {
            showAlert("Erreur", "Ce numéro de téléphone existe déjà.");
            return;
        }

        List<String> langues = new ArrayList<>();
        if (francais.isSelected()) langues.add("Français");
        if (anglais.isSelected()) langues.add("Anglais");
        if (espagnol.isSelected()) langues.add("Espagnol");

        if (langues.isEmpty()) {
            showAlert("Erreur", "Veuillez sélectionner au moins une langue.");
            return;
        }

        if (selectedImageFile != null) {
            imageUrl = selectedImageFile.getName();
            ImageUtils.copierImageGuide(selectedImageFile); // 🔁 copie local + Symfony
        }

        Guide guide = new Guide(nomValue, prenomValue, String.join(", ", langues), telephoneValue, emailValue, imageUrl);
        System.out.println(guide);

        try {
            guideService.ajouter(guide);
            showAlert("Succès", "Guide ajouté avec succès!");
            resetFields();
            Stage stage = (Stage) btnAjouter.getScene().getWindow();
            stage.close();
        } catch (SQLException e) {
            showAlert("Erreur", "Une erreur est survenue lors de l'ajout du guide.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleChoisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(btnChoisirImage.getScene().getWindow());

        if (file != null) {
            selectedImageFile = file;
            imageUrl = file.getName();
            imageView.setImage(new Image(file.toURI().toString()));
        }
    }

    private void resetFields() {
        nom.clear();
        prenom.clear();
        telephone.clear();
        email.clear();
        francais.setSelected(false);
        anglais.setSelected(false);
        espagnol.setSelected(false);
        imageView.setImage(null);
        imageUrl = "";
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
