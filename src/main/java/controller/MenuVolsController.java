package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class MenuVolsController {

    @FXML
    private AnchorPane contentArea;

    @FXML
    private void gotoAfficherEvenement() {
        loadContent("afficherVolUtili.fxml");
    }



    @FXML
    private void goToDisplayReservations() {
        loadContent("mesReservation.fxml");
    }

    private void loadContent(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlFile)); // Fixed path
            Node content = loader.load();

            // Clear old content and add new content
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);

            // Anchor the new content to fit the parent
            AnchorPane.setTopAnchor(content, 0.0);
            AnchorPane.setBottomAnchor(content, 0.0);
            AnchorPane.setLeftAnchor(content, 0.0);
            AnchorPane.setRightAnchor(content, 0.0);
        } catch (IOException e) {
            System.out.println("Error loading FXML: " + fxmlFile);
            e.printStackTrace();
        }
    }
}
