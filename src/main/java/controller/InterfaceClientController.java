package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import services.UtilisateurService;
import utils.SessionManager;

import java.io.IOException;

public class InterfaceClientController {

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
    private void afficherVols() { loadContent("/MenuVols.fxml");
    }

    @FXML
    private void gotoGestionPackVoyage() {
        loadContent("/PaiementPackVoyage.fxml");
    }

    @FXML
    private void gotoAfficherHotel() {
        loadContent("/afficher_hotels_client.fxml");
    }

    @FXML
    private void gotofeedbackadmin() {
        loadContent("/Feedback.fxml");
    }
    @FXML
    private void gotomenuclient() {
        loadContent("/MenuClient.fxml");
    }

    @FXML
    private void gotoGestionPosts() {
        loadContent("/AfficherPost.fxml");
    }



    @FXML
    private void gotoMenuExcursion() {
        loadContent("/AfficherExcursionUtilisateur.fxml");
    }


    public void gotoGuide() {loadContent("/AfficherGuideUtilisateur.fxml");
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