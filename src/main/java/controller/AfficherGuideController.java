package controller;

import entities.Guide;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import services.GuideService;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherGuideController {
    private GuideService guideService;

    @FXML
    private TableView<Guide> tableViewGuides;
    @FXML
    private TableColumn<Guide, String> nomPrenomColumn;
    @FXML
    private TableColumn<Guide, String> emailColumn;
    @FXML
    private TableColumn<Guide, String> telColumn;
    @FXML
    private TableColumn<Guide, String> langueColumn;
    @FXML
    private TableColumn<Guide, Void> actionColumn; // Boutons Modifier & Supprimer

    public void initialize() throws SQLException {
        System.out.println("initialize admin OK"); // DEBUG

        utils.ImageUtils.synchroniserDepuisSymfonyGuide();


        guideService = new GuideService();
        try {
            List<Guide> guides = guideService.lire();
            ObservableList<Guide> observableGuides = FXCollections.observableArrayList(guides);
            tableViewGuides.setItems(observableGuides);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Assigner les colonnes aux bonnes propriétés
        nomPrenomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        telColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        langueColumn.setCellValueFactory(new PropertyValueFactory<>("langue"));

        // Ajouter les boutons Modifier et Supprimer à la colonne actionColumn
        actionColumn.setCellFactory(getButtonCellFactory());
    }
    public void handleAjouterGuide() {
        try {
            // Charger la fenêtre AjouterGuide.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterGuide.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène avec le fichier FXML
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter Guide");
            stage.show(); // Afficher la nouvelle fenêtre

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleModifier(Guide guide) {
        try {
            // Charger l'interface de modification
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FormulaireModification.fxml"));

            // Charger l'interface sans définir manuellement le contrôleur
            Parent root = loader.load();

            // Récupérer le contrôleur de la fenêtre de modification
            FormulaireModificationController controller = loader.getController();

            // Passer le guide sélectionné au contrôleur
            controller.setGuide(guide);

            // Créer une nouvelle fenêtre (Stage)
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Guide");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Générer les boutons Modifier et Supprimer dans chaque ligne
    private Callback<TableColumn<Guide, Void>, TableCell<Guide, Void>> getButtonCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Guide, Void> call(TableColumn<Guide, Void> param) {
                return new TableCell<>() {
                    private final Button btnModifier = new Button("Modifier");
                    private final Button btnSupprimer = new Button("Supprimer");
                    private final HBox hbox = new HBox(10, btnModifier, btnSupprimer);
                    {
                        // Assigner les classes CSS aux boutons
                        btnModifier.getStyleClass().add("button-modifier");
                        btnSupprimer.getStyleClass().add("button-supprimer");

                        btnModifier.setOnAction(event -> handleModifier(getTableView().getItems().get(getIndex())));
                        btnSupprimer.setOnAction(event -> handleSupprimer(getTableView().getItems().get(getIndex())));
                    }

                    {
                        btnModifier.setOnAction(event -> handleModifier(getTableView().getItems().get(getIndex())));
                        btnSupprimer.setOnAction(event -> handleSupprimer(getTableView().getItems().get(getIndex())));
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : hbox);
                    }
                };
            }
        };
    }

    private void handleSupprimer(Guide guide) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le guide");
        alert.setContentText("Voulez-vous vraiment supprimer ce guide ? Cette action est irréversible.");

        // Boutons Oui / Non
        ButtonType buttonTypeOui = new ButtonType("Oui", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeNon = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeOui, buttonTypeNon);

        // Afficher l'alerte et attendre la réponse de l'utilisateur
        alert.showAndWait().ifPresent(response -> {
            if (response == buttonTypeOui) {
                try {
                    guideService.supprimer(guide);
                    tableViewGuides.getItems().remove(guide); // Met à jour la table après suppression
                    System.out.println("Guide supprimé avec succès !");
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("Erreur lors de la suppression !");
                }
            }
        });
    }

    public void reloadTableView() throws SQLException {
        // Récupérer la liste mise à jour des guides
        List<Guide> updatedGuides = guideService.lire();

        // Convertir en ObservableList pour lier à la TableView
        ObservableList<Guide> observableGuides = FXCollections.observableArrayList(updatedGuides);

        // Mettre à jour la TableView
        tableViewGuides.setItems(observableGuides);
    }
    private Image chargerImageGuide(String imageName) {
        if (imageName == null || imageName.trim().isEmpty()) {
            return new Image("http://127.0.0.1:8000/uploads/guide/default.jpg");
        }

        // Chemin local JavaFX
        File localImage = new File("src/main/resources/images/" + imageName);
        if (localImage.exists()) {
            return new Image(localImage.toURI().toString());
        }

        // Sinon, on tente depuis Symfony
        try {
            return new Image("http://127.0.0.1:8000/uploads/guide/" + imageName, true);
        } catch (Exception e) {
            System.out.println("❌ Erreur chargement image guide " + imageName + " : " + e.getMessage());
            return new Image("http://127.0.0.1:8000/uploads/guide/default.jpg");
        }
    }


}