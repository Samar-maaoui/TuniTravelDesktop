package controller;

import entities.Excursion;
import entities.ExcursionStat;
import entities.Guide;
import javafx.animation.FadeTransition;
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
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.ExcursionService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class AfficherExcursionUtilisateurController {
    private final ExcursionService excursionService = new ExcursionService();

    @FXML
    private VBox excursionVBox;  // Conteneur pour afficher les excursions
    @FXML
    private Button idliste;
    @FXML
    private Label prixLabel1;
    @FXML
    private Slider filtragerecherche;
    @FXML
    private ChoiceBox destinationrecherche;
    @FXML
    private ChoiceBox villedepartrecherche;
    @FXML
    private BarChart satexcursion;
    @FXML
    private AnchorPane filtrerrechreche;
    @FXML
    private Button afficherDansLeMap;

    public AfficherExcursionUtilisateurController() throws SQLException {
    }


    @FXML
    public void initialize() {
        afficherGraphiqueStatistiquesExcursions();
        afficherDansLeMap.setOnAction(event -> ouvrirMap());


        villedepartrecherche.getItems().addAll(
                "Aucun", "Ariana", "Ain Drahem" , "Béja", "Ben Arous", "Bizerte", "Gabès", "Gafsa",
                "Jendouba", "Kairouan", "Kasserine", "Kébili", "Kef", "Mahdia", "Manouba",
                "Médenine", "Monastir", "Nabeul", "Sfax", "Sidi Bouzid", "Siliana", "Sousse",
                "Tataouine", "Tozeur", "Tunis", "Zaghouan"
        );

        destinationrecherche.getItems().addAll(
                "Aucun", "Ariana", "Ain Drahem" ,"Béja", "Ben Arous", "Bizerte", "Gabès", "Gafsa",
                "Jendouba", "Kairouan", "Kasserine", "Kébili", "Kef", "Mahdia", "Manouba",
                "Médenine", "Monastir", "Nabeul", "Sfax", "Sidi Bouzid", "Siliana", "Sousse",
                "Tataouine", "Tozeur", "Tunis", "Zaghouan"
        );
        // Remplacez ces valeurs par vos destinations
        villedepartrecherche.valueProperty().addListener((observable, oldValue, newValue) -> filtrerExcursions());
        destinationrecherche.valueProperty().addListener((observable, oldValue, newValue) -> filtrerExcursions());
        rafraichirAffichage();
        filtragerecherche.valueProperty().addListener((observable, oldValue, newValue) -> filtrerExcursions(newValue.doubleValue()));

        // Créer un Timeline qui exécute la mise à jour toutes les 2 secondes
     /*   Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(2), e -> rafraichirAffichage()) // Rafraîchissement toutes les 2 secondes
        );
        timeline.setCycleCount(Timeline.INDEFINITE); // Pour que le rafraîchissement soit infini
        timeline.play(); // Démarrer le timeline */
    }

    @FXML
    private void ouvrirMap() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherMap.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Map des excursions");



            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Erreur lors du chargement du formulaire d'ajout.");
        }
    }

    private void filtrerExcursions() {
        try {
            String villeRecherche = (String) villedepartrecherche.getValue();
            String destinationRecherche = (String) destinationrecherche.getValue();

            // Filtrer les excursions selon la ville et la destination
            List<Excursion> excursions = excursionService.lire();

            List<Excursion> filteredExcursions = excursions.stream()
                    .filter(excursion -> {
                        boolean matchVille = (villeRecherche == null || villeRecherche.equals("Aucun") || excursion.getVilleDepart().equalsIgnoreCase(villeRecherche));
                        boolean matchDestination = (destinationRecherche == null || destinationRecherche.equals("Aucun") || excursion.getLieu().equalsIgnoreCase(destinationRecherche));
                        return matchVille && matchDestination;
                    })
                    .collect(Collectors.toList());

            // Rafraîchir l'affichage avec les excursions filtrées
            excursionVBox.getChildren().clear();
            filteredExcursions.forEach(excursion -> excursionVBox.getChildren().add(createExcursionCard(excursion)));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private Image chargerImage(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageUrl = "default.jpg";
        }

        // ⚠️ Corriger le chemin local vers le vrai système de fichiers
        File imageFile = new File("src/main/resources/images/Excursions/" + imageUrl);

        if (imageFile.exists()) {
            return new Image(imageFile.toURI().toString());
        } else {
            System.out.println("❌ Erreur : Image " + imageUrl + " introuvable, chargement de default.jpg");

            File defaultImage = new File("src/main/resources/images/Excursions/default.jpg");
            if (defaultImage.exists()) {
                return new Image(defaultImage.toURI().toString());
            } else {
                System.out.println("❌ Erreur critique : default.jpg introuvable également !");
                return new Image("https://via.placeholder.com/300x300.png?text=Image+manquante");
            }
        }
    }

    private void afficherExcursions() {
        try {
            // Récupérer la liste des excursions
            List<Excursion> excursions = excursionService.lire();

            excursionVBox.getChildren().clear();

            // Ajouter chaque excursion dans un VBox
            for (Excursion excursion : excursions) {
                VBox excursionCard = createExcursionCard(excursion);
                excursionVBox.getChildren().add(excursionCard);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createExcursionCard(Excursion excursion) {
        VBox excursionCard = new VBox(10);
        excursionCard.setStyle("-fx-padding: 10px; -fx-background-color: #f0f0f0; -fx-border-radius: 10px; -fx-border-color: #dcdcdc;");

        // Chargement de l'image correctement
        ImageView imageView = new ImageView(chargerImage(excursion.getImage()));
        imageView.setFitWidth(400);
        imageView.setFitHeight(400);
        imageView.setPreserveRatio(true);

        VBox textVBox = new VBox(5); // Espacement vertical entre les labels

        // Définition des labels
        Label nomLabel = new Label("🗺️ " + excursion.getNom());
        Label villedepart = new Label("📍 Ville de depart: " + excursion.getVilleDepart());
        Label lieuLabel = new Label("🎯 Destination: " + excursion.getLieu());
        Label prixLabel = new Label("\u200B\uD83D\uDCB2 Prix: " + excursion.getPrix() + " TND");
        Label dateAllerLabel = new Label("\uD83D\uDCC5\u200B   Date Aller: " + excursion.getDateDepart());
        Label dateRetourLabel = new Label("\uD83D\uDCC5\u200B   Date Retour: " + excursion.getDateRetour());

        // Affichage des places disponibles avec gestion du statut "Complet"
        Label placesDispoLabel = new Label(
                excursion.getNbPlace() > 0 ? "Places Disponibles: " + excursion.getNbPlace() : "🔴 Complet"
        );
        placesDispoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " +
                (excursion.getNbPlace() > 0 ? "#555" : "#d90000") + "; -fx-font-weight: bold;");

        Hyperlink guideLink = new Hyperlink("\uD83D\uDE4D\u200D♂\uFE0F\u200B  Guide: " + excursion.getGuide().getNom() + " " + excursion.getGuide().getPrenom());
        guideLink.setStyle("-fx-font-size: 14px; -fx-text-fill: #0077cc; -fx-underline: true;");

        // Ajouter un événement au clic sur le guide
        guideLink.setOnAction(e -> afficherInfosGuide(excursion.getGuide()));

        Hyperlink detailLink = new Hyperlink("📝 Détail");
        detailLink.setStyle("-fx-font-size: 14px; -fx-text-fill: #0077cc; -fx-underline: true;");

        // Action pour afficher les détails
        detailLink.setOnAction(e -> {
            String description = excursionService.Detail(excursion.getIdExcursion());
            showExcursionDetails(description);
        });

        // Appliquer des styles aux labels
        nomLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #d2691e;");
        villedepart.setStyle("-fx-font-size: 16px; -fx-text-fill: #555;");
        lieuLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #555;");
        prixLabel.setStyle("-fx-font-size: 19px; -fx-text-fill: #016c03;");
        dateAllerLabel.setStyle("-fx-font-size: 14px; -fx-font-style: italic; -fx-text-fill: #555555;");
        dateRetourLabel.setStyle("-fx-font-size: 14px; -fx-font-style: italic; -fx-text-fill: #555;");

        // Le bouton de réservation
        Button reserverButton = new Button(" ✔\uFE0F   Réserver");
        reserverButton.setMinWidth(100);
        reserverButton.setStyle(
                "-fx-background-color: #12989e; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 10px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-border-color: transparent; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, #888888, 5, 0, 2, 2);"
        );

        // Désactiver le bouton si l'excursion est complète
        if (excursion.getNbPlace() == 0) {
            reserverButton.setDisable(true);
            reserverButton.setStyle(
                    "-fx-background-color: #999999; " +  // Bouton grisé
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 14px; " +
                            "-fx-padding: 10px; " +
                            "-fx-background-radius: 5px; " +
                            "-fx-border-color: transparent;"
            );
        } else {
            reserverButton.setOnAction(e -> reserverExcursion(excursion));
        }

        // Ajouter les éléments dans le VBox
        excursionCard.getChildren().addAll(
                imageView, nomLabel, lieuLabel, villedepart, prixLabel, detailLink,
                dateAllerLabel, dateRetourLabel, placesDispoLabel, guideLink, reserverButton
        );

        return excursionCard;
    }

    private Image chargerImageGuide(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageUrl = "default.png";
        }

        String imagePath = "/resources/images/"+imageUrl;
        File imageFile = new File(imagePath);

        if (imageFile.exists()) {
            return new Image(imageFile.toURI().toString());
        } else {
            System.out.println("❌ Erreur : Image " + imageUrl + " introuvable, chargement de default.jpg");
            return new Image(getClass().getClassLoader().getResource("images/default.jpg").toExternalForm());
        }
    }
    private void afficherInfosGuide(Guide guide) {
        Stage stage = new Stage();
        stage.setTitle("Informations du Guide");

        VBox root = new VBox(10);
        root.setPadding(new Insets(15, 10, 5, 5));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #FFFAF0; -fx-border-radius: 30; -fx-padding: 15;");

        // Chargement de l'image du guide
        ImageView imageView = new ImageView(chargerImageGuide(guide.getImage()));
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 10, 0.5, 0, 0);");

        Label nomLabel = new Label("\uD835\uDC0D\uD835\uDC28\uD835\uDC26 : " + guide.getNom());
        Label prenomLabel = new Label("\uD835\uDC0F\uD835\uDC2Bé\uD835\uDC27\uD835\uDC28\uD835\uDC26  : " + guide.getPrenom());
        Label emailLabel = new Label(" \u200B\uD83D\uDCE7 \uD835\uDC04\uD835\uDC26\uD835\uDC1A\uD835\uDC22\uD835\uDC25  : " + guide.getEmail());
        Label telLabel = new Label(" \u200B\uD83D\uDCDE\u200B \uD835\uDC13\uD835\uDC1E\uD835\uDC25\uD835\uDC1E\uD835\uDC29\uD835\uDC21\uD835\uDC28\uD835\uDC27\uD835\uDC1E : " + guide.getTelephone());
        Label Langueparlé = new Label(" \uD835\uDC0B\uD835\uDC1A\uD835\uDC27\uD835\uDC20\uD835\uDC2E\uD835\uDC1E \uD835\uDC29\uD835\uDC1A\uD835\uDC2B\uD835\uDC25é : " + guide.getLangue());

        // Appliquer les styles aux labels
        String labelStyle = "-fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;";
        nomLabel.setStyle(labelStyle);
        prenomLabel.setStyle(labelStyle);
        emailLabel.setStyle(labelStyle);
        telLabel.setStyle(labelStyle);
        Langueparlé.setStyle(labelStyle);
        Button fermerButton = new Button("Fermer");
        fermerButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5 10; -fx-background-radius: 5;");
        fermerButton.setOnAction(e -> stage.close());

        root.getChildren().addAll(imageView, nomLabel, prenomLabel, emailLabel, telLabel, Langueparlé,  fermerButton);

        Scene scene = new Scene(root, 300, 350);
        stage.setScene(scene);
        stage.show();
    }

    public void showExcursionDetails(String description) {
        TextArea detailArea = new TextArea();
        detailArea.setEditable(false);
        detailArea.setWrapText(true);
        detailArea.setText(description);

        // 🎨 Ajout de styles en ligne
        detailArea.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-background-color: #f4f4f4; -fx-border-color: #ccc; -fx-border-radius: 5px;");

        ScrollPane scrollPane = new ScrollPane(detailArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: #ffffff; -fx-border-color: #ddd; -fx-padding: 5px;");

        Label titleLabel = new Label("\uD835\uDC03é\uD835\uDC2D\uD835\uDC1A\uD835\uDC22\uD835\uDC25\uD835\uDC2C \uD835\uDC1D\uD835\uDC1E \uD835\uDC25'\uD835\uDC04\uD835\uDC31\uD835\uDC1C\uD835\uDC2E\uD835\uDC2B\uD835\uDC2C\uD835\uDC22\uD835\uDC28\uD835\uDC27\n");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #12989e;-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 10px;");

        VBox vbox = new VBox(10, titleLabel, scrollPane);
        vbox.setStyle("-fx-padding: 20px; -fx-background-color: #FFFAF0; -fx-border-radius: 10px; -fx-border-color: #bdc3c7;");

        Stage detailsStage = new Stage();
        Scene scene = new Scene(vbox, 400, 300);

        detailsStage.setScene(scene);
        detailsStage.setTitle("Détails de l'Excursion");
        detailsStage.show();
    }
    // Méthode de réservation (à adapter selon ta logique)
    private void reserverExcursion(Excursion excursion) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReservationExcursion.fxml"));
            Parent root = loader.load();

            // Passer les données de l'excursion au contrôleur de réservation
            ReservationExcursionController controller = loader.getController();
            controller.setExcursion(excursion);

            Stage stage = new Stage();
            stage.setTitle("Réserver une Excursion");
            stage.setScene(new Scene(root));
            stage.show();
            rafraichirAffichage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void afficherGraphiqueStatistiquesExcursions() {
        // Récupérer les statistiques des excursions (par exemple, via ExcursionService)
        List<ExcursionStat> stats = excursionService.getReservationsByExcursion();

        // Création des axes pour le graphique
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Excursions");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Nombre de Réservations");

        // Créer un graphique à barres
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Statistiques des Réservations par Excursion");

        // Ajouter les séries de données au graphique
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Réservations");

        // Remplir la série avec les données de réservation
        for (ExcursionStat stat : stats) {
            series.getData().add(new XYChart.Data<>(stat.getName(), stat.getReservationsCount()));
        }

        // Ajouter la série au graphique
        barChart.getData().add(series);

        // Ajouter le graphique à l'interface
        satexcursion.getData().clear(); // Vider les données existantes
        satexcursion.getData().addAll(barChart.getData());
    }

    public void rafraichirAffichage() {
        afficherExcursions();
    }

    private void filtrerExcursions(double valeurSlider) {
        try {
            List<Excursion> excursions = excursionService.filtrerParPrix(valeurSlider);
            System.out.println(valeurSlider);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), excursionVBox);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> {
                excursionVBox.getChildren().clear();
                excursions.forEach(excursion -> excursionVBox.getChildren().add(createExcursionCard(excursion)));

                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), excursionVBox);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            fadeOut.play();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void gotoListeReservation(ActionEvent actionEvent) {
        try {
            // Charger le fichier FXML de la liste des réservations
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeReservation.fxml"));
            Parent root = loader.load();  // Charger la racine de la scène

            // Créer une nouvelle scène avec la racine chargée
            Stage stage = new Stage();
            stage.setTitle("Liste des Réservations");
            stage.setScene(new Scene(root));  // Créer la scène avec la racine
            stage.show();  // Afficher la nouvelle fenêtre

        } catch (IOException e) {
            e.printStackTrace();
            // Vous pouvez également ajouter une alerte pour informer l'utilisateur en cas d'erreur
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Problème de chargement");
            alert.setContentText("Une erreur est survenue lors du chargement de la liste des réservations.");
            alert.showAndWait();
        }

    }
    }



