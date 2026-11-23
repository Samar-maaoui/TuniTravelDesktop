package controller;

import entities.Feedback;
import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import services.FeedbackService;
import services.UtilisateurService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class MenuClientController {


    @FXML
    private Button btnModifierCompte, btnSupprimerCompte;

    @FXML
    private ListView<String> listView;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private ObservableList<String> utilisateursList = FXCollections.observableArrayList();
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label userTypeLabel;
    @FXML
    private ListView<Feedback> feedbackListView;
    @FXML
    private Button btnAjouterFeedback;
    @FXML
    private Button btnModifierFeedback;
    @FXML
    private Button btnSupprimerFeedback;

    public void initialize() {
        refreshUserInfo();
        refreshListView();
        refreshFeedbackList();

        // Configuration de la cellule personnalisée pour les feedbacks
        feedbackListView.setCellFactory(param -> new FeedbackListCell());

        // Important: Set the controller as userData for the scene
        // This will be done after the scene is set, in a Platform.runLater
        javafx.application.Platform.runLater(() -> {
            if (feedbackListView.getScene() != null) {
                feedbackListView.getScene().setUserData(this);
            }
        });
    }
    private void refreshUserInfo() {
        Utilisateur utilisateurConnecte = utilisateurService.getUtilisateurConnecte();

        if (utilisateurConnecte != null) {
            welcomeLabel.setText("Bienvenue " + utilisateurConnecte.getPrenom() + " " + utilisateurConnecte.getNom());
            userTypeLabel.setText("Type: " + utilisateurConnecte.getTypeUtilisateur());

            // Mettre à jour les labels d'information

        } else {
            showAlert("Aucun utilisateur connecté", "Aucun utilisateur connecté pour afficher les informations.");
        }
    }
    @Deprecated
    public void afficherInfos(ActionEvent event) {
        refreshListView();
    }

    @Deprecated
    public void modifierCompte(ActionEvent event) {
        Utilisateur utilisateurConnecte = utilisateurService.getUtilisateurConnecte();
        if (utilisateurConnecte != null) {
            afficherFenetreModification(utilisateurConnecte);
            refreshListView();

        } else {
            showAlert("Erreur", "Aucun utilisateur connecté.");
        }
    }

    @FXML
    public void supprimerCompte(ActionEvent event) throws SQLException {
        Utilisateur utilisateurConnecte = utilisateurService.getUtilisateurConnecte();

        if (utilisateurConnecte != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation de suppression");
            confirmation.setHeaderText("Voulez-vous vraiment supprimer votre compte ?");
            Optional<ButtonType> result = confirmation.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                utilisateurService.supprimerUtilisateur(utilisateurConnecte);
                showAlert("Suppression réussie", "Votre compte a été supprimé avec succès.");
                utilisateurService.logoutUtilisateur();
                redirectToLoginPage();
            }
        } else {
            showAlert("Aucun utilisateur connecté", "Veuillez vous connecter avant de supprimer votre compte.");
        }
    }

    private void refreshListView() {
        Utilisateur utilisateurConnecte = utilisateurService.getUtilisateurConnecte();
        refreshFeedbackList();
        if (utilisateurConnecte != null) {
            // Vider la ListView
            listView.getItems().clear();

            // Créer un VBox pour contenir toutes les informations
            VBox userInfoContainer = new VBox();
            userInfoContainer.getStyleClass().add("user-info-list");
            userInfoContainer.setSpacing(15);

            // Ajouter chaque ligne d'information
            addInfoRow(userInfoContainer, "Nom:", utilisateurConnecte.getNom());
            addInfoRow(userInfoContainer, "Prénom:", utilisateurConnecte.getPrenom());
            addInfoRow(userInfoContainer, "CIN:", String.valueOf(utilisateurConnecte.getCin()));
            addInfoRow(userInfoContainer, "Email:", utilisateurConnecte.getEmail());
            addInfoRow(userInfoContainer, "Téléphone:", utilisateurConnecte.getTelephone());
            addInfoRow(userInfoContainer, "Adresse:", utilisateurConnecte.getAdresse());
            addInfoRow(userInfoContainer, "Date de naissance:", String.valueOf(utilisateurConnecte.getDateNaissance()));
            addInfoRow(userInfoContainer, "Type utilisateur:", String.valueOf(utilisateurConnecte.getTypeUtilisateur()));

            // Ajouter le VBox à la ListView
            listView.setCellFactory(lv -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(userInfoContainer);
                    }
                }
            });

            // Ajouter un élément fictif pour afficher le contenu
            listView.setItems(FXCollections.observableArrayList("info"));
        } else {
            showAlert("Aucun utilisateur connecté", "Aucun utilisateur connecté pour afficher les informations.");
        }
    }
    FeedbackService feedbackService = new FeedbackService();
    private void addInfoRow(VBox container, String labelText, String valueText) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("user-info-row");
        row.setSpacing(10);

        Label label = new Label(labelText);
        label.getStyleClass().add("user-info-label");

        Label value = new Label(valueText);
        value.getStyleClass().add("user-info-value");
        HBox.setHgrow(value, Priority.ALWAYS);

        row.getChildren().addAll(label, value);
        container.getChildren().add(row);
    }
    private void redirectToLoginPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LOGIN.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setTitle("Connexion");
            loginStage.setScene(new Scene(root));
            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void afficherFenetreModification(Utilisateur utilisateur) {
        if (utilisateur == null) {
            showAlert("Erreur", "Aucun utilisateur sélectionné.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier Utilisateur");
        dialog.setHeaderText("Modification de l'utilisateur avec CIN : " + utilisateur.getCin());

        TextField nomField = new TextField(utilisateur.getNom());
        TextField prenomField = new TextField(utilisateur.getPrenom());
        TextField emailField = new TextField(utilisateur.getEmail());
        TextField adresseField = new TextField(utilisateur.getAdresse());
        TextField telephoneField = new TextField(utilisateur.getTelephone());
        DatePicker dateNaissancePicker = new DatePicker(utilisateur.getDateNaissance());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nom:"), 0, 1);
        grid.add(nomField, 1, 1);
        grid.add(new Label("Prénom:"), 0, 2);
        grid.add(prenomField, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Adresse:"), 0, 4);
        grid.add(adresseField, 1, 4);
        grid.add(new Label("Téléphone:"), 0, 5);
        grid.add(telephoneField, 1, 5);
        grid.add(new Label("Date de naissance:"), 0, 6);
        grid.add(dateNaissancePicker, 1, 6);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Vérification des saisies
            if (!nomField.getText().matches("[A-Za-zÀ-ÖØ-öø-ÿ- ]{2,}")) {
                showAlert("Erreur", "Le nom doit contenir uniquement des lettres et au moins 2 caractères.");
                return;
            }
            if (!prenomField.getText().matches("[A-Za-zÀ-ÖØ-öø-ÿ- ]{2,}")) {
                showAlert("Erreur", "Le prénom doit contenir uniquement des lettres et au moins 2 caractères.");
                return;
            }

            if (!telephoneField.getText().matches("\\d{8}")) {
                showAlert("Erreur", "Le numéro de téléphone doit contenir exactement 8 chiffres.");
                return;
            }
            LocalDate dateNaissance = dateNaissancePicker.getValue();
            if (dateNaissance == null || Period.between(dateNaissance, LocalDate.now()).getYears() < 18) {
                showAlert("Erreur", "L'utilisateur doit avoir au moins 18 ans.");
                return;
            }

            // Mise à jour des données
            try {
                utilisateur.setNom(nomField.getText());
                utilisateur.setPrenom(prenomField.getText());
                utilisateur.setEmail(emailField.getText());
                utilisateur.setAdresse(adresseField.getText());
                utilisateur.setTelephone(telephoneField.getText());
                utilisateur.setDateNaissance(dateNaissance);

                utilisateurService.modifierUtilisateur(utilisateur);
                refreshListView();
                showAlert("Succès", "Utilisateur modifié avec succès.");
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de modifier l'utilisateur.");
            }
        }
    }


    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void modifierUtilisateur(ActionEvent actionEvent) {
        modifierCompte(actionEvent);
    }





    private void refreshFeedbackList() {
        Utilisateur utilisateurConnecte = SessionManager.getUtilisateur();
        if (utilisateurConnecte != null) {
            try {
                List<Feedback> feedbacks = feedbackService.trouverFeedbackPar(utilisateurConnecte.getIdUtilisateur());
                ObservableList<Feedback> feedbackItems = FXCollections.observableArrayList(feedbacks);
                feedbackListView.setItems(feedbackItems);
            } catch (SQLException e) {
                showAlert1("Erreur", "Impossible de récupérer les feedbacks.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void afficherAjouterFeedback(ActionEvent actionEvent) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un Feedback");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStyleClass().add("custom-dialog");

        // Création des composants UI
        Label feedbackLabel = new Label("Votre Feedback :");
        feedbackLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextArea feedbackTextArea = new TextArea();
        feedbackTextArea.setPromptText("Écrivez votre feedback ici...");
        feedbackTextArea.setPrefHeight(100);
        feedbackTextArea.setWrapText(true);

        Label noteLabel = new Label("Note :");
        noteLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Slider noteSlider = new Slider(1, 5, 5);
        noteSlider.setBlockIncrement(1);
        noteSlider.setMajorTickUnit(1);
        noteSlider.setMinorTickCount(0);
        noteSlider.setSnapToTicks(true);
        noteSlider.setShowTickMarks(true);
        noteSlider.setShowTickLabels(true);

        Label currentNoteLabel = new Label("5");
        currentNoteLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0099ff;");

        noteSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                currentNoteLabel.setText(String.valueOf(newValue.intValue()))
        );

        // GridPane pour une disposition soignée
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.add(feedbackLabel, 0, 0);
        gridPane.add(feedbackTextArea, 0, 1, 2, 1);
        gridPane.add(noteLabel, 0, 2);
        gridPane.add(noteSlider, 1, 2);
        gridPane.add(currentNoteLabel, 2, 2);

        dialog.getDialogPane().setContent(gridPane);

        // Boutons OK et Annuler
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Affichage de la boîte de dialogue
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String commentaire = feedbackTextArea.getText();
            int note = (int) noteSlider.getValue();

            if (commentaire.isEmpty()) {
                showAlert1("Erreur", "Le commentaire ne peut pas être vide.");
            } else {
                try {
                    Utilisateur utilisateurConnecte = SessionManager.getUtilisateur();
                    if (utilisateurConnecte != null) {
                        Feedback feedback = new Feedback(utilisateurConnecte, note, commentaire);
                        feedbackService.ajouterFeedback(feedback);
                        showAlert1("Succès", "Feedback ajouté avec succès.");
                        refreshFeedbackList();
                    }
                } catch (SQLException e) {
                    showAlert1("Erreur", "Erreur lors de l'ajout du feedback.");
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    public void modifierFeedback(ActionEvent actionEvent) {
        Feedback selectedFeedback = feedbackListView.getSelectionModel().getSelectedItem();

        if (selectedFeedback != null) {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Modifier un Feedback");
            dialog.setHeaderText(null);
            dialog.getDialogPane().getStyleClass().add("custom-dialog");

            // Champ de texte pour le commentaire
            TextArea feedbackTextArea = new TextArea(selectedFeedback.getCommentaire());
            feedbackTextArea.setPromptText("Modifiez votre feedback ici...");
            feedbackTextArea.setPrefHeight(100);
            feedbackTextArea.setWrapText(true);

            // Slider pour la note (de 1 à 5)
            Slider noteSlider = new Slider(1, 5, selectedFeedback.getNote());
            noteSlider.setBlockIncrement(1);
            noteSlider.setMajorTickUnit(1);
            noteSlider.setMinorTickCount(0);
            noteSlider.setSnapToTicks(true);
            noteSlider.setShowTickMarks(true);
            noteSlider.setShowTickLabels(true);

            Label noteLabel = new Label("Note : " + selectedFeedback.getNote());
            noteLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0099ff;");

            noteSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                noteLabel.setText("Note : " + newValue.intValue());
            });

            // Conteneur vertical
            VBox vbox = new VBox(10);
            vbox.getChildren().addAll(
                    new Label("Commentaire :"),
                    feedbackTextArea,
                    noteLabel,
                    noteSlider
            );

            // Boutons
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Ajout du contenu
            dialog.getDialogPane().setContent(vbox);

            // Affichage de la boîte de dialogue
            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                String nouveauCommentaire = feedbackTextArea.getText().trim();
                int nouvelleNote = (int) noteSlider.getValue();

                // Vérification des saisies
                if (nouveauCommentaire.isEmpty()) {
                    showAlert1("Erreur", "Le commentaire ne peut pas être vide.");
                } else if (nouveauCommentaire.length() < 5) {
                    showAlert1("Attention", "Le commentaire doit contenir au moins 5 caractères.");
                } else {
                    selectedFeedback.setCommentaire(nouveauCommentaire);
                    selectedFeedback.setNote(nouvelleNote);

                    try {
                        feedbackService.modifierFeedback(selectedFeedback);
                        showAlert1("Succès", "Le feedback a été modifié avec succès !");
                        refreshFeedbackList();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        showAlert1("Erreur", "Erreur lors de la modification du feedback.");
                    }
                }
            }
        } else {
            showAlert1("Erreur", "Veuillez sélectionner un feedback à modifier.");
        }
    }

    @FXML
    public void supprimerFeedback(ActionEvent actionEvent) {
        Feedback selectedFeedback = feedbackListView.getSelectionModel().getSelectedItem();
        if (selectedFeedback != null) {
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirmation");
            confirmationAlert.setHeaderText("Voulez-vous vraiment supprimer ce feedback ?");
            Optional<ButtonType> result = confirmationAlert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    feedbackService.supprimerFeedback(selectedFeedback);
                    refreshFeedbackList();
                    showAlert1("Succès", "Le feedback a été supprimé avec succès.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert1("Erreur", "Erreur lors de la suppression du feedback.");
                }
            }
        } else {
            showAlert1("Erreur", "Veuillez sélectionner un feedback à supprimer.");
        }
    }

    private void showAlert1(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}