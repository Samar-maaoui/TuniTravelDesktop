package controller;

import entities.PackVoyage;
import entities.Post;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.PackVoyageService;
import services.PostService;
import tests.MainFX;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class ModifierPostController {

    @FXML
    private TextField titreField, descriptionField;

    @FXML
    private DatePicker dateExpirationPicker;

    @FXML
    private ComboBox<PackVoyage> comboPackVoyage;

    @FXML
    private Button btnModifier, btnAnnuler;

    private final PostService postService = PostService.getInstance();
    private final PackVoyageService packVoyageService = new PackVoyageService();

    private static Post postAModifier;

    public static void setPostAModifier(Post post) {
        postAModifier = post;
    }

    @FXML
    void initialize() {
        if (postAModifier == null) {
            afficherAlerte("Aucun Post sélectionné !");
            Platform.runLater(() -> ((Stage) btnModifier.getScene().getWindow()).close());
            return;
        }

        // ✅ Remplissage des champs avec les valeurs actuelles du Post
        titreField.setText(postAModifier.getTitre());
        descriptionField.setText(postAModifier.getDescription());

        // ✅ Pré-remplissage du datePicker avec la date d'expiration actuelle
        if (postAModifier.getDateExpiration() != null) {
            java.util.Date utilDate = new java.util.Date(postAModifier.getDateExpiration().getTime());
            dateExpirationPicker.setValue(utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }

        // ✅ Chargement asynchrone des PackVoyage
        chargerPacksAsync();

        btnModifier.setOnAction(event -> handleModifier());
        btnAnnuler.setOnAction(event -> handleAnnuler());
    }

    private void chargerPacksAsync() {
        Task<List<PackVoyage>> task = new Task<>() {
            @Override
            protected List<PackVoyage> call() throws SQLException {
                return packVoyageService.listerPacks(); // Opération lourde
            }
        };

        task.setOnSucceeded(event -> {
            List<PackVoyage> packs = task.getValue();
            comboPackVoyage.getItems().addAll(packs);
            comboPackVoyage.setValue(postAModifier.getPackVoyage()); // Sélectionner le pack actuel
        });

        task.setOnFailed(event -> afficherAlerte("Erreur lors du chargement des packs de voyage"));

        new Thread(task).start(); // Lancer la tâche en arrière-plan
    }

    @FXML
    private void handleModifier() {
        // ✅ Vérification des champs
        String titre = titreField.getText().trim();
        String description = descriptionField.getText().trim();
        LocalDate dateExp = dateExpirationPicker.getValue();
        PackVoyage pack = comboPackVoyage.getValue();

        if (titre.isEmpty() || description.isEmpty() || dateExp == null || pack == null) {
            afficherAlerte("Veuillez remplir tous les champs !");
            return;
        }

        // ✅ Vérifier si l'ID du Post est valide
        if (postAModifier.getIdPost() == 0) {
            afficherAlerte("Erreur : Impossible de modifier un Post sans ID !");
            return;
        }

        // ✅ Confirmation avant la modification
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de modification");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir modifier ce Post ?");
        confirmation.setContentText("Titre : " + titre + "\nDescription : " + description);

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // ✅ Mise à jour du post
            postAModifier.setTitre(titre);
            postAModifier.setDescription(description);
            postAModifier.setDateExpiration(java.sql.Date.valueOf(dateExp));
            postAModifier.setPackVoyage(pack);

            // ✅ Enregistrement dans la base de données
            try {
                postService.modifierPost(postAModifier);
                afficherAlerte("Post modifié avec succès !");
            } catch (Exception e) {
                afficherAlerte("Erreur lors de la modification du Post : " + e.getMessage());
                return;
            }

            // ✅ Recharger les posts après modification
            Platform.runLater(() -> MainFX.changeScene("AfficherPost.fxml", "Gestion des Posts"));
        }
    }

    @FXML
    private void handleAnnuler() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();  // Récupérer le stage de la scène actuelle
        stage.close();  // Fermer la fenêtre modale
    }


    private void afficherAlerte(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
