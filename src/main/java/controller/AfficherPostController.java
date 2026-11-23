package controller;

import entities.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.PostService;
import services.ReservationPackVoyageService;
import utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AfficherPostController {

    @FXML
    private FlowPane flowPanePosts;
    @FXML
    private Button btn_ajouter, btn_modifier, btn_supprimer;
    @FXML
    private Button btn_refraichir;
    @FXML
    private TextField tf_recherche;
    @FXML
    private ComboBox<String> cb_tri;

    private final PostService postService = PostService.getInstance();
    private final ReservationPackVoyageService reservationService = new ReservationPackVoyageService();

    @FXML
    void initialize() {
        Utilisateur utilisateur = SessionManager.getInstance().getUtilisateur();
        boolean isAdmin = utilisateur != null && utilisateur.getTypeUtilisateur() == TypeUtilisateur.ADMIN;

        btn_ajouter.setVisible(isAdmin);
        btn_modifier.setVisible(isAdmin);
        btn_supprimer.setVisible(isAdmin);

        btn_ajouter.setOnAction(event -> handleAjouterPost());
        btn_modifier.setOnAction(event -> handleModifierPost());
        btn_supprimer.setOnAction(event -> handleSupprimerPost());
        btn_refraichir.setOnAction(event -> handleRafraichir());

        tf_recherche.textProperty().addListener((observable, oldValue, newValue) -> chargerPosts());
        cb_tri.valueProperty().addListener((observable, oldValue, newValue) -> chargerPosts());
        cb_tri.setItems(FXCollections.observableArrayList("Titre", "Date Expiration", "Statut"));
        cb_tri.getSelectionModel().selectFirst();

        chargerPosts();
    }

    public void chargerPosts() {
        flowPanePosts.getChildren().clear();

        List<Post> posts = postService.listerPosts();

        String keyword = tf_recherche.getText().toLowerCase();
        posts = posts.stream()
                .filter(post -> post.getTitre().toLowerCase().contains(keyword))
                .collect(Collectors.toList());

        String criteria = cb_tri.getValue();
        Comparator<Post> comparator = switch (criteria) {
            case "Titre" -> Comparator.comparing(Post::getTitre);
            case "Date Expiration" -> Comparator.comparing(Post::getDateExpiration);
            case "Statut" -> Comparator.comparing(Post::getStatut);
            default -> null;
        };
        if (comparator != null) {
            posts = posts.stream().sorted(comparator).collect(Collectors.toList());
        }

        for (Post post : posts) {
            flowPanePosts.getChildren().add(creerCartePost(post));
        }
    }

    private VBox creerCartePost(Post post) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setPrefWidth(270);
        card.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6,0.1,0,2);");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(270);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);

        PackVoyage pack = post.getPackVoyage();
        String imageUrl = (pack != null && pack.getImageUrl() != null && !pack.getImageUrl().isEmpty())
                ? pack.getImageUrl() : post.getImageUrl();
        imageView.setImage(chargerImage(imageUrl));

        Label titre = new Label(post.getTitre());
        titre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1E3A8A;");

        String detailsText = (pack != null && pack.getDetails() != null && !pack.getDetails().isEmpty())
                ? pack.getDetails() : "Aucune description disponible.";
        Label description = new Label(detailsText);
        description.setWrapText(true);
        description.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");

        Label prix = new Label("💰 " + (pack != null ? pack.getPrixTotal() : 0.0) + " €");
        prix.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13px;");

        Label expire = new Label("📅 Expire: " + post.getDateExpiration());
        expire.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");

        Label hotel = new Label("🏨 Hôtel: " + (pack != null && pack.getHotel() != null ? pack.getHotel().getNom() : "-"));
        hotel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");

        Label vol = new Label("✈ Vol: " + (pack != null && pack.getVol() != null
                ? pack.getVol().getaero_depart() + " → " + pack.getVol().getaero_arrivee()
                : "-"));
        vol.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");

        Label excursion = new Label("🌍 Excursion: " + (pack != null && pack.getExcursion() != null
                ? pack.getExcursion().getNom() + " (" + pack.getExcursion().getLieu() + ")"
                : "-"));
        excursion.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");

        Button btnReserver = new Button("Réserver");
        btnReserver.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold;");

        Utilisateur utilisateur = SessionManager.getInstance().getUtilisateur();
        boolean isVIPorNormal = utilisateur != null &&
                (utilisateur.getTypeUtilisateur() == TypeUtilisateur.NORMAL || utilisateur.getTypeUtilisateur() == TypeUtilisateur.VIP);

        btnReserver.setVisible(utilisateur != null && utilisateur.getTypeUtilisateur() != TypeUtilisateur.ADMIN);
        btnReserver.setDisable(!isVIPorNormal || pack == null || pack.getNombrePlacesRestants() <= 0);
        btnReserver.setText((pack != null && pack.getNombrePlacesRestants() > 0) ? "Réserver" : "Complet");
        btnReserver.setOnAction(e -> ouvrirFenetreReservation(post));

        card.getChildren().addAll(
                imageView, titre, description, prix, expire, hotel, vol, excursion, btnReserver
        );
        return card;
    }

    @FXML
    private void handleAjouterPost() {
        ouvrirFenetre("AjouterPost.fxml", "Ajouter un Post");
    }

    @FXML
    private void handleModifierPost() {
        afficherAlerte("Veuillez sélectionner un Post à modifier dans la nouvelle interface.");
    }

    @FXML
    private void handleSupprimerPost() {
        afficherAlerte("Veuillez sélectionner un Post à supprimer dans la nouvelle interface.");
    }

    @FXML
    private void handleRafraichir() {
        chargerPosts();
        afficherAlerte("La liste des posts a été mise à jour !");
    }

    private void ouvrirFenetreReservation(Post post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReservationPackVoyage.fxml"));
            Parent root = loader.load();
            ReservationPackVoyageController controller = loader.getController();
            controller.setPost(post);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Réserver un Pack de Voyage");
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> chargerPosts());
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ouvrirFenetre(String fxml, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(titre);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Image chargerImage(String imageUrl) {
        try {
            if (imageUrl.startsWith("http")) {
                return new Image(imageUrl, true);
            }
            URL url = getClass().getResource("/images/" + imageUrl);
            return (url != null) ? new Image(url.toExternalForm()) : new Image(getClass().getResource("/images/default.jpg").toExternalForm());
        } catch (Exception e) {
            return new Image(getClass().getResource("/images/default.jpg").toExternalForm());
        }
    }

    private void afficherAlerte(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.showAndWait();
    }
}
