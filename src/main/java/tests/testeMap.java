package tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;


public class testeMap extends Application {
    private static Stage primaryStage; // Fenêtre principale


    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        changeScene("LOGIN.fxml", "Connexion"); // Démarrer avec la page de connexion
        primaryStage.show();
    }

    /**
     * Change de scène en chargeant un fichier FXML.
     * @param fxmlFile Nom du fichier FXML (ex: "MenuClient.fxml")
     * @param title Titre de la fenêtre
     */
    public static void changeScene(String fxmlFile, String title) {
        try {
            System.out.println("Chargement du fichier FXML : " + fxmlFile); // Debug
            FXMLLoader loader = new FXMLLoader(MainFX.class.getResource("/" + fxmlFile));
            Parent root = loader.load();
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle(title);
            primaryStage.centerOnScreen(); // Centre la fenêtre sur l'écran
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("⚠️ Erreur lors du chargement de la scène : " + fxmlFile);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}





