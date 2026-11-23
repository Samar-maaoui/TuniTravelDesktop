package controller;

import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.UtilisateurService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class InterfaceAdminController {

    @FXML
    private AnchorPane contentArea;

    @FXML
    private TextField searchField;

    private final UtilisateurService utilisateurService = new UtilisateurService();

    @FXML
    public void initialize() {
        System.out.println("✅ InterfaceAdminController initialisé !");
    }

    @FXML
    private void gotoMenuEvenement() {
        loadContent("/menuEvnement.fxml");
    }

    @FXML
    private void afficherVols() {
        loadContent("/AfficherVol.fxml");
    }

    @FXML
    private void gotoGestionPackVoyage() {
        loadContent("/AfficherPackVoyage.fxml");
    }

    @FXML
    private void gotoAfficherHotel() {
        loadContent("/afficher_hotel.fxml");
    }

    @FXML
    private void gotofeedbackadmin() {
        loadContent("/FeedbackAdmin.fxml");
    }

    @FXML
    private void gotoGestionPosts() {
        loadContent("/AfficherPost.fxml");
    }
    public void gotoafficherGuide() {
        loadContent("/AffGuide.fxml");
    }
    public void afficherUtilisateurs() {
        loadContent("/MenuAdmin.fxml");
    }

    @FXML
    private void gotoMenuExcursion() {
        loadContent("/MenuExcursion.fxml");
    }




    private void loadContent(String fxmlFile) {
        try {
            System.out.println("🔄 Chargement : " + fxmlFile);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            if (loader.getLocation() == null) {
                throw new IllegalStateException("⚠️ Fichier FXML introuvable : " + fxmlFile);
            }

            Parent content = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);

        } catch (IOException e) {
            System.out.println("❌ Erreur de chargement : " + fxmlFile);
            e.printStackTrace();
        } catch (IllegalStateException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        }
    }



    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }


    public void search() {
    }

    public void openLoginScreen(MouseEvent mouseEvent) {
        try {
            // Charge le fichier FXML de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent loginScreen = loader.load();

            // Crée une nouvelle scène avec le contenu chargé
            Scene scene = new Scene(loginScreen);

            // Obtenez la fenêtre actuelle (Stage) pour remplacer le contenu
            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace(); // Gère l'erreur si le fichier FXML ne peut pas être chargé
        }
    }

    public void logout(MouseEvent mouseEvent) {
        SessionManager.logout();
        openLoginScreen(mouseEvent); // Ne passe pas le mouseEvent ici
        System.out.println(SessionManager.isUserLoggedIn());
    }
}
