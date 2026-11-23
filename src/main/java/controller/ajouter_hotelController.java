package controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import entities.Hotel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.CloudinaryInitializer;
import services.ServiceHotel;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;


public class ajouter_hotelController {

    @FXML
    private TextField txtNom, txtPrix, txtAdresse, txtTel, txtEmail, txtImageUrl;
    @FXML
    private TextArea txtDescription;
    @FXML
    private ToggleButton btnDisponible;
    @FXML
    private Button btnAjouter, btnAnnuler, btnChoisirImage; //btnChoisirImage ajouté
    @FXML
    private Label lblPrix;
    @FXML
    private Label lblMessage;
    private final ServiceHotel hotelService = ServiceHotel.getInstance();
    private final Cloudinary cloudinary = CloudinaryInitializer.getInstance();
    private File selectedImageFile;

    @FXML
    private void initialize() {
        btnDisponible.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        btnDisponible.setText("Indisponible");
        btnDisponible.setOnAction(event -> {
            btnDisponible.setText(btnDisponible.isSelected() ? "Disponible" : "Indisponible");
            btnDisponible.setStyle("-fx-background-color: " + (btnDisponible.isSelected() ? "blue" : "red") + "; -fx-text-fill: white;");
        });

       //Gestion du choix de fichier image
        btnChoisirImage.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner une image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif")
            );
            selectedImageFile = fileChooser.showOpenDialog(null);
            if (selectedImageFile != null) {
                txtImageUrl.setText(selectedImageFile.getAbsolutePath());
            }
        });
    }
    @FXML
    private void ajouterHotel() {
        try {
            // Vérification des champs obligatoires
            if (txtNom.getText().trim().isEmpty() ||
                    txtPrix.getText().trim().isEmpty() ||
                    txtAdresse.getText().trim().isEmpty() ||
                    txtTel.getText().trim().isEmpty() ||
                    txtEmail.getText().trim().isEmpty() ||
                    txtDescription.getText().trim().isEmpty() ||
                    selectedImageFile == null) { // Vérification que l'image est sélectionnée

                showAlert(Alert.AlertType.WARNING, "Champs obligatoires", "Veuillez remplir tous les champs et sélectionner une image.");
                return;
            }

            // Vérification du prix
            double prix;
            try {
                prix = Double.parseDouble(txtPrix.getText().trim());
                if (prix < 0) {
                    showAlert(Alert.AlertType.WARNING, "Prix invalide", "Le prix ne peut pas être négatif.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Prix invalide", "Veuillez entrer un prix valide.");
                return;
            }

            // Vérification du numéro de téléphone (8 à 15 chiffres)
            if (!Pattern.matches("\\d{8,15}", txtTel.getText().trim())) {
                showAlert(Alert.AlertType.WARNING, "Téléphone invalide", "Le numéro doit contenir entre 8 et 15 chiffres.");
                return;
            }

            // Vérification de l'email (doit se terminer par @gmail.com)
            String email = txtEmail.getText().trim();
            if (!email.matches("^[a-zA-Z0-9._%+-]+@gmail\\.com$")) {
                showAlert(Alert.AlertType.WARNING, "Email invalide", "Veuillez entrer une adresse email valide qui se termine par @gmail.com.");
                return;
            }


            // Téléchargement de l'image sur Cloudinary
            String imageUrl = uploadImage(selectedImageFile);
            // Copier l’image localement et côté Symfony
            copierImageLocalEtSymfony(selectedImageFile);


            if (imageUrl == null) {
                lblMessage.setText("Erreur lors du téléchargement de l'image. Veuillez réessayer.");
                return;
            }

            // Création de l'objet Hôtel avec l'URL de Cloudinary
            boolean disponible = btnDisponible.isSelected();
            Hotel h = new Hotel(0, txtNom.getText().trim(), prix, disponible, txtAdresse.getText().trim(),
                    txtDescription.getText().trim(), txtTel.getText().trim(), email, imageUrl);

            // Ajout de l'hôtel dans la base de données
            hotelService.ajouterhotel(h);
            lblMessage.setText("Hôtel ajouté avec succès !");
            fermerFenetre();

        } catch (SQLException e) {
            lblMessage.setText("Erreur SQL : " + e.getMessage());
        } catch (NumberFormatException e) {
            lblMessage.setText("Erreur de format : Le prix doit être un nombre.");
        } catch (Exception e) {
            lblMessage.setText("Erreur imprévue : " + e.getMessage());
            e.printStackTrace(); // Print the stack trace for debugging purposes
        }
    }


    // Méthode uploadImage (à ajouter dans votre classe)
    private String uploadImage(File imageFile) {
        if (imageFile != null) {
            try {
                System.out.println("Uploading image: " + imageFile.getAbsolutePath());
                Map uploadResult = cloudinary.uploader().upload(imageFile, ObjectUtils.emptyMap());
                String secureUrl = (String) uploadResult.get("secure_url");
                System.out.println("Upload successful! Secure URL: " + secureUrl);
                return secureUrl;
            } catch (IOException e) {
                System.err.println("Cloudinary upload error (IOException): " + e.getMessage());
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                System.err.println("Cloudinary upload error (Exception): " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
    @FXML
    private void fermerFenetre() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void copierImageLocalEtSymfony(File imageFile) {
        if (imageFile == null || !imageFile.exists()) return;

        try {
            // Nom du fichier d’origine
            String fileName = imageFile.getName();

            // JavaFX : src/main/resources/images/
            File localDestination = new File("src/main/resources/images/" + fileName);
            java.nio.file.Files.copy(
                    imageFile.toPath(),
                    localDestination.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            // Symfony : public/images/
            File symfonyDestination = new File("C:/Users/abidi/Desktop/PIWEB/PIWEB/public/images/" + fileName);
            java.nio.file.Files.copy(
                    imageFile.toPath(),
                    symfonyDestination.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            System.out.println("✔ Copie locale + Symfony réussie : " + fileName);
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la copie de l'image localement/Symfony : " + e.getMessage());
        }
    }


}
