package controller;

import entities.TypeUtilisateur;
import entities.Utilisateur;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import services.UtilisateurService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MenuAdminController {

    @FXML
    private ListView<Utilisateur> listViewUtilisateurs;
    @FXML
    private TextField cinTextField;
    @FXML
    private ComboBox<TypeUtilisateur> typeUtilisateurComboBox;
    @FXML
    private Label totalUsersLabel;
    @FXML
    private VBox graphContainer;
    @FXML
    private Button btnFeedbackAdmin, btnMenuExcursion;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final ObservableList<Utilisateur> utilisateursList = FXCollections.observableArrayList();
    private final ObservableList<Utilisateur> filteredList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configure the ListView with custom cell factory
        listViewUtilisateurs.setCellFactory(param -> new UserListCell(this));

        // Load all users
        rafraichirListeUtilisateurs();

        // Configure type filter
        ObservableList<TypeUtilisateur> typeUtilisateurList = FXCollections.observableArrayList(TypeUtilisateur.values());
        typeUtilisateurComboBox.setItems(typeUtilisateurList);
        typeUtilisateurComboBox.setOnAction(this::filtrerParTypeUtilisateur);

        // Vérification de la présence et de la visibilité des boutons
        if (btnFeedbackAdmin != null) {
            btnFeedbackAdmin.setVisible(true);
            System.out.println("Bouton Feedback Admin initialisé et visible !");
        } else {
            System.out.println("Erreur : btnFeedbackAdmin est NULL !");
        }

        if (btnMenuExcursion != null) {
            btnMenuExcursion.setVisible(true);
            System.out.println("Erreur : btnMenuExcursion est NULL !");
        }

        // Configure selection listener for the ListView
        listViewUtilisateurs.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cinTextField.setText(String.valueOf(newSelection.getCin()));
            }
        });

        // Add listener to cinTextField for instant search
        cinTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrerParCIN(newValue);
        });
    }

    private void filtrerParCIN(String cinText) {
        if (cinText == null || cinText.trim().isEmpty()) {
            // If the search field is empty, show all users
            listViewUtilisateurs.setItems(utilisateursList);
            totalUsersLabel.setText("Total: " + utilisateursList.size() + " utilisateurs");
            return;
        }

        try {
            // Try to parse the CIN as an integer
            int cin = Integer.parseInt(cinText.trim());

            // Filter users by CIN
            List<Utilisateur> utilisateursFiltres = utilisateursList.stream()
                    .filter(u -> String.valueOf(u.getCin()).contains(cinText.trim()))
                    .collect(Collectors.toList());

            // Update the ListView with filtered results
            filteredList.clear();
            filteredList.addAll(utilisateursFiltres);
            listViewUtilisateurs.setItems(filteredList);
            totalUsersLabel.setText("Total: " + utilisateursFiltres.size() + " utilisateurs");

        } catch (NumberFormatException e) {
            // If the input is not a valid number, show no results
            listViewUtilisateurs.setItems(FXCollections.observableArrayList());
            totalUsersLabel.setText("Total: 0 utilisateurs");
        }
    }

    private void rafraichirListeUtilisateurs() {
        try {
            utilisateursList.clear();
            List<Utilisateur> utilisateurs = utilisateurService.chargerTousLesUtilisateurs();
            utilisateursList.addAll(utilisateurs);
            listViewUtilisateurs.setItems(utilisateursList);
            totalUsersLabel.setText("Total: " + utilisateurs.size() + " utilisateurs");
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les utilisateurs.");
        }
    }

    private void filtrerParTypeUtilisateur(ActionEvent actionEvent) {
        TypeUtilisateur typeSelectionne = typeUtilisateurComboBox.getValue();
        if (typeSelectionne != null) {
            List<Utilisateur> utilisateursFiltres = utilisateurService.filtrerParType(typeSelectionne);
            listViewUtilisateurs.setItems(FXCollections.observableArrayList(utilisateursFiltres));
            totalUsersLabel.setText("Total: " + utilisateursFiltres.size() + " utilisateurs");
        }
    }

    @FXML
    public void modifierUtilisateurParCin(ActionEvent event) {
        String cinText = cinTextField.getText().trim();
        if (cinText.isEmpty()) {
            showAlert("Erreur", "Veuillez entrer un CIN.");
            return;
        }

        try {
            int cin = Integer.parseInt(cinText);
            Utilisateur utilisateur = utilisateurService.trouverUtilisateurParCin(cin);
            if (utilisateur != null) {
                ouvrirFenetreModification(utilisateur);
            } else {
                showAlert("Utilisateur non trouvé", "Aucun utilisateur avec ce CIN.");
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le CIN doit être un nombre valide.");
        }
    }

    // Make this method public so it can be called from UserListCell
    public void ouvrirFenetreModification(Utilisateur utilisateur) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier Utilisateur");
        dialog.setHeaderText("Modification de l'utilisateur avec CIN : " + utilisateur.getCin());

        // Charger le fichier CSS
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        // Champs de saisie
        TextField cinField = new TextField(String.valueOf(utilisateur.getCin()));
        cinField.setDisable(true); // Le CIN ne doit pas être modifiable

        TextField nomField = new TextField(utilisateur.getNom());
        TextField prenomField = new TextField(utilisateur.getPrenom());
        TextField emailField = new TextField(utilisateur.getEmail());
        TextField adresseField = new TextField(utilisateur.getAdresse());
        TextField telephoneField = new TextField(utilisateur.getTelephone());

        VBox vbox = new VBox(15,  // Espacement plus grand pour l'aération
                new Label("CIN:"), cinField,
                new Label("Nom:"), nomField,
                new Label("Prénom:"), prenomField,
                new Label("Email:"), emailField,
                new Label("Adresse:"), adresseField,
                new Label("Téléphone:"), telephoneField
        );
        vbox.setPadding(new Insets(20)); // Plus d'espace autour du contenu
        vbox.setAlignment(Pos.CENTER_LEFT); // Alignement plus naturel

        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Donner le focus sur le champ "Nom" pour une meilleure expérience utilisateur
        Platform.runLater(() -> nomField.requestFocus());

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Vérification des champs (exemple : email et téléphone)
                if (!emailField.getText().matches("\\S+@\\S+\\.\\S+")) {
                    showAlert("Erreur", "Adresse e-mail invalide !");
                    return;
                }
                if (!telephoneField.getText().matches("\\d{8,10}")) {
                    showAlert("Erreur", "Numéro de téléphone invalide !");
                    return;
                }

                // Mise à jour des informations de l'utilisateur
                utilisateur.setNom(nomField.getText().trim());
                utilisateur.setPrenom(prenomField.getText().trim());
                utilisateur.setEmail(emailField.getText().trim());
                utilisateur.setAdresse(adresseField.getText().trim());
                utilisateur.setTelephone(telephoneField.getText().trim());

                utilisateurService.modifierUtilisateur(utilisateur);
                rafraichirListeUtilisateurs();
                showAlert("Succès", "Utilisateur modifié avec succès.");
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de modifier l'utilisateur.");
            }
        }
    }

    @FXML
    public void supprimerUtilisateurParCin(ActionEvent event) {
        String cinText = cinTextField.getText().trim();
        if (cinText.isEmpty()) {
            showAlert("Erreur", "Veuillez entrer un CIN.");
            return;
        }

        try {
            int cin = Integer.parseInt(cinText);
            Utilisateur utilisateur = utilisateurService.trouverUtilisateurParCin(cin);
            if (utilisateur != null) {
                confirmerSuppression(utilisateur);
            } else {
                showAlert("Utilisateur non trouvé", "Aucun utilisateur avec ce CIN.");
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le CIN doit être un nombre valide.");
        }
    }

    // Make this method public so it can be called from UserListCell
    public void confirmerSuppression(Utilisateur utilisateur) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous supprimer l'utilisateur " + utilisateur.getNom() + " " + utilisateur.getPrenom() + " ?",
                ButtonType.OK, ButtonType.CANCEL);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Suppression d'utilisateur");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                utilisateurService.supprimerUtilisateur(utilisateur);
                rafraichirListeUtilisateurs();
                showAlert("Succès", "Utilisateur supprimé avec succès.");
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer l'utilisateur: " + e.getMessage());
            }
        }
    }

    @FXML
    public void filtrerutilisateur(ActionEvent event) {
        try {
            Map<TypeUtilisateur, Long> statistiques = utilisateurService.calculerStatistiquesUtilisateurs();

            // Clear previous content
            graphContainer.getChildren().clear();

            // Add title
            Label titleLabel = new Label("Statistiques des utilisateurs par type");
            titleLabel.getStyleClass().add("stats-title");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 0 0 10 0;");

            // Create pie chart
            PieChart pieChart = new PieChart();
            pieChart.setTitle("Répartition par type d'utilisateur");
            pieChart.setLabelsVisible(true);
            pieChart.setLegendVisible(true);

            // Add data to pie chart
            statistiques.forEach((type, count) -> {
                PieChart.Data slice = new PieChart.Data(type.name() + " (" + count + ")", count);
                pieChart.getData().add(slice);
            });

            // Create bar chart
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            barChart.setTitle("Nombre d'utilisateurs par type");
            xAxis.setLabel("Type d'utilisateur");
            yAxis.setLabel("Nombre d'utilisateurs");

            // Create series for bar chart
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Utilisateurs");

            // Add data to bar chart
            statistiques.forEach((type, count) -> {
                series.getData().add(new XYChart.Data<>(type.name(), count));
            });

            barChart.getData().add(series);

            // Create summary text
            VBox summaryBox = new VBox(10);
            summaryBox.setStyle("-fx-padding: 15px; -fx-background-color: #f8f9fa; -fx-background-radius: 5px;");

            Label summaryTitle = new Label("Résumé des statistiques");
            summaryTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

            summaryBox.getChildren().add(summaryTitle);

            // Add summary for each type
            statistiques.forEach((type, count) -> {
                long percentage = Math.round((double) count / utilisateursList.size() * 100);
                Text typeText = new Text(type.name() + ": " + count + " utilisateurs (" + percentage + "%)");
                typeText.setStyle("-fx-font-size: 14px;");
                summaryBox.getChildren().add(typeText);
            });

            // Add total
            Text totalText = new Text("Total: " + utilisateursList.size() + " utilisateurs");
            totalText.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            summaryBox.getChildren().add(totalText);

            // Add all components to graph container
            graphContainer.getChildren().addAll(titleLabel, pieChart, barChart, summaryBox);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'afficher les statistiques: " + e.getMessage());
        }
    }

    @FXML
    public void gotofeedbackadmin(ActionEvent event) {
        ouvrirNouvelleScene("/FeedbackAdmin.fxml", "Gestion des Feedbacks");
    }

    private void ouvrirNouvelleScene(String fxml, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(titre);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'interface.");
        }
    }

    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}