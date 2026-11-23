package controller;

import entities.*;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.PackVoyageService;
import services.PostService;
import tests.MainFX;
import utils.SessionManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class AjouterPostController {

    @FXML
    private TextField titreField, descriptionField;

    @FXML
    private DatePicker dateExpirationPicker;

    @FXML
    private ComboBox<PackVoyage> comboPackVoyage;

    @FXML
    private Button btnAjouter, btnRetour;

    private final PostService postService = PostService.getInstance();
    private final PackVoyageService packVoyageService = new PackVoyageService();

    @FXML
    void initialize()
    {
        // Charger les packs en arrière-plan pour éviter le blocage de l'interface utilisateur
        chargerPacksAsync();

        // Événements pour les boutons
        btnAjouter.setOnAction(event -> handleAjouter());
        btnRetour.setOnAction(event -> handleRetour());

        // Adapter l'affichage des packs dans la ComboBox
        comboPackVoyage.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(PackVoyage pack, boolean empty) {
                super.updateItem(pack, empty);
                setText((empty || pack == null) ? null : pack.getNomPack());
            }
        });

        comboPackVoyage.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(PackVoyage pack, boolean empty) {
                super.updateItem(pack, empty);
                setText((empty || pack == null) ? "Sélectionner un PackVoyage" : pack.getNomPack());
            }
        });
    }

    /**
     * Charger la liste des `PackVoyage` en arrière-plan pour éviter de bloquer l'UI
     */
    private void chargerPacksAsync() {
        Task<List<PackVoyage>> task = new Task<>() {
            @Override
            protected List<PackVoyage> call() throws SQLException {
                return packVoyageService.listerPacks();
            }
        };

        task.setOnSucceeded(event -> {
            comboPackVoyage.getItems().addAll(task.getValue());
        });

        new Thread(task).start();
    }

    /**
     * Ajouter un `Post` après validation des données
     */


    @FXML
    private void handleAjouter() {
        String titre = titreField.getText().trim();
        String description = descriptionField.getText().trim();
        LocalDate dateExpiration = dateExpirationPicker.getValue();
        PackVoyage pack = comboPackVoyage.getValue();

        // Vérifier si l'utilisateur est un admin
        Utilisateur admin = SessionManager.getInstance().getUtilisateur();
        if (admin == null || !admin.getTypeUtilisateur().equals(TypeUtilisateur.ADMIN)) {
            afficherAlerte("Accès refusé", "Seuls les administrateurs peuvent ajouter un Post.");
            return;
        }

        // Vérifier les champs obligatoires
        if (titre.isEmpty() || description.isEmpty() || dateExpiration == null || pack == null) {
            afficherAlerte("Champs obligatoires", "Veuillez remplir tous les champs et sélectionner un PackVoyage !");
            return;
        }

        Date dateExp = Date.from(dateExpiration.atStartOfDay(ZoneId.systemDefault()).toInstant());
        String imageUrl = (pack.getImageUrl() != null) ? pack.getImageUrl() : "default.jpg";

        // Créer le Post
        Post post = new Post(titre, description, dateExp, pack, admin, imageUrl);
        post.mettreAJourStatut();

        try {
            postService.ajouterPost(post); // Ajouter le post via le service

            // Message de succès
            afficherAlerte("Succès", "Le Post a été ajouté avec succès !");

            // Fermer la fenêtre modale après l'ajout
            ((Stage) titreField.getScene().getWindow()).close();

            // Rafraîchir la liste des posts dans AfficherPostController
            // Nous allons récupérer l'instance de AfficherPostController et appeler la méthode chargerPosts
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherPost.fxml"));
            Parent root = loader.load();
            AfficherPostController afficherPostController = loader.getController();
            afficherPostController.chargerPosts(); // Rafraîchir la liste des posts

        } catch (Exception e) {
            afficherAlerte("Erreur", "Une erreur est survenue lors de l'ajout du Post.");
            e.printStackTrace(); // Affichez l'exception dans la console pour le débogage
        }
    }


    /**
     * Retour à la liste des posts
     */
    @FXML
    private void handleRetour() {
        Stage stage = (Stage) btnRetour.getScene().getWindow();  // Récupérer le stage de la scène actuelle
        stage.close();  // Fermer la fenêtre modale
    }


    /**
     * Affiche une alerte avec un message personnalisé
     */
    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
