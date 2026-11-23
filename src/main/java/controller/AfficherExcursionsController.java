package controller;

import entities.Excursion;
import entities.ExcursionStat;
import entities.Guide;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;
import services.ExcursionService;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class AfficherExcursionsController {
    private static final String LOCATIONIQ_API_KEY = "pk.d23d607e10a7e14a3ec0d35a174b66e4"; // Remplace par ta clé API

    private final ExcursionService excursionService = new ExcursionService();

    @FXML
    private Button Ajouter; // Button for adding excursions
    @FXML
    private VBox excursionVBox; // VBox to display all excursions
    @FXML
    private Slider filtrage;
    @FXML
    private Label prixLabel;
    @FXML
    private AnchorPane affiner;
    @FXML
    private BarChart satexcursion;
    @FXML
    private Button afficherDansLeMap;
    @FXML
    private ChoiceBox villearrive;
    @FXML
    private ChoiceBox Villedepart;


    public AfficherExcursionsController() throws SQLException {
    }


    @FXML
    public void initialize() {
        // 🔁 Synchroniser les images Symfony -> JavaFX locales (ancienne data)
        utils.ImageUtils.synchroniserDepuisSymfony();

        // Afficher les excursions au démarrage
        afficherExcursions();

        // Initialiser le bouton d’ajout
        Ajouter.setOnAction(e -> ouvrirFormulaireAjout());
        afficherGraphiqueStatistiquesExcursions();

        // Slider de filtrage
        filtrage.valueProperty().addListener((observable, oldValue, newValue) -> filtrerExcursions(newValue.doubleValue()));
        afficherDansLeMap.setOnAction(event -> ouvrirMap());

        Villedepart.getItems().addAll(
                "Aucun", "Ariana", "Ain Drahem" , "Béja", "Ben Arous", "Bizerte", "Gabès", "Gafsa",
                "Jendouba", "Kairouan", "Kasserine", "Kébili", "Kef", "Mahdia", "Manouba",
                "Médenine", "Monastir", "Nabeul", "Sfax", "Sidi Bouzid", "Siliana", "Sousse",
                "Tataouine", "Tozeur", "Tunis", "Zaghouan"
        );

        villearrive.getItems().addAll(
                "Aucun", "Ariana", "Ain Drahem" ,"Béja", "Ben Arous", "Bizerte", "Gabès", "Gafsa",
                "Jendouba", "Kairouan", "Kasserine", "Kébili", "Kef", "Mahdia", "Manouba",
                "Médenine", "Monastir", "Nabeul", "Sfax", "Sidi Bouzid", "Siliana", "Sousse",
                "Tataouine", "Tozeur", "Tunis", "Zaghouan"
        );

        Villedepart.valueProperty().addListener((observable, oldValue, newValue) -> filtrerExcursions());
        villearrive.valueProperty().addListener((observable, oldValue, newValue) -> filtrerExcursions());
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
            String villeRecherche = (String) Villedepart.getValue();
            String destinationRecherche = (String) villearrive.getValue();

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

    private void afficherGraphiqueStatistiquesExcursions() {
        // Récupérer les statistiques des excursions (par exemple, via ExcursionService)
        List<ExcursionStat> stats = excursionService.getReservationsByExcursion();

        // Calculer la somme totale des réservations
        int totalReservations = 0;
        for (ExcursionStat stat : stats) {
            totalReservations += stat.getReservationsCount();
        }

        // Création des axes pour le graphique
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Excursions");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Pourcentage de Réservations");

        // Créer un graphique à barres
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Statistiques des Réservations par Excursion");

        // Ajouter les séries de données au graphique
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Pourcentage des Réservations");

        // Remplir la série avec les données de réservation en pourcentage
        for (ExcursionStat stat : stats) {
            double percentage = (double) stat.getReservationsCount() / totalReservations * 100;
            series.getData().add(new XYChart.Data<>(stat.getName(), percentage));
        }

        // Ajouter la série au graphique
        barChart.getData().add(series);

        // Ajouter le graphique à l'interface
        satexcursion.getData().clear(); // Vider les données existantes
        satexcursion.getData().addAll(barChart.getData());
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



    private Image chargerImage(String imageFileName) {
        if (imageFileName == null || imageFileName.trim().isEmpty()) {
            return new Image(new File("src/main/resources/images/Excursions/default.jpg").toURI().toString());
        }

        File localFile = new File("src/main/resources/images/Excursions/" + imageFileName);
        if (localFile.exists()) {
            return new Image(localFile.toURI().toString());
        } else {
            System.out.println("❌ Image non trouvée localement : " + imageFileName);
            return new Image(new File("src/main/resources/images/Excursions/default.jpg").toURI().toString());
        }
    }



    // Method to display all excursions
    public void afficherExcursions() {
        try {
            List<Excursion> excursions = excursionService.lire();
            System.out.println(excursions);
        excursionVBox.getChildren().clear(); // Clear the VBox before adding new items

            for (Excursion excursion : excursions) {
                HBox excursionCard = createExcursionCard(excursion); // Create a card for each excursion
                excursionVBox.getChildren().add(excursionCard); // Add the card to the VBox
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception by printing the error
        }
    }

    // Create a HBox card for each excursion
    // Create a HBox card for each excursion
    private HBox createExcursionCard(Excursion excursion) {
        HBox excursionCard = new HBox(20); // Espace de 20px entre les éléments
        excursionCard.setStyle("-fx-padding: 10px; -fx-background-color: #eaeaea; -fx-border-radius: 10px; -fx-border-color: #dcdcdc;");

        // Image
        ImageView imageView = new ImageView(chargerImage(excursion.getImage()));
        imageView.setFitWidth(400);
        imageView.setFitHeight(400);
        imageView.setPreserveRatio(true);

        // Textes descriptifs
        VBox textVBox = new VBox(5); // Espacement vertical entre les labels
        // Définition des labels
        Label nomLabel = new Label("🗺️     " + excursion.getNom());
        Label VilleDepartLabel = new Label("📍 Ville de depart: " + excursion.getVilleDepart());
        Label lieuLabel = new Label("🎯 Destination: " + excursion.getLieu());

        //   Label DescLabel= new Label("Description: " + excursion.getDescription());
        Hyperlink detailLink = new Hyperlink("📝 Détail");
        detailLink.setStyle("-fx-font-size: 14px; -fx-text-fill: #0077cc; -fx-underline: true;");

// Assuming 'excursion' is an object containing the details
        detailLink.setOnAction(e -> {
            // Call your method to fetch the description using the excursion's id
            String description = excursionService.Detail(excursion.getIdExcursion());
            showExcursionDetails(description); // Pass the description to the method
        });

        Label prixLabel = new Label("\u200B\uD83D\uDCB2 Prix: " + excursion.getPrix() + " TND");

        Label dateAllerLabel = new Label("\uD83D\uDCC5\u200B   Date Aller: " + excursion.getDateDepart());
        Label dateRetourLabel = new Label("\uD83D\uDCC5\u200B   Date Retour: " + excursion.getDateRetour());

        // Label descLabel = new Label("Description: " + excursion.getDescription());
        Label placesDispoLabel = new Label("👥 Places restante: " + excursion.getNbPlace());

       // Label guideLabel = new Label("\uD83D\uDE4D\u200D♂\uFE0F\u200B  Guide: " + excursion.getGuide().getNom() + " " + excursion.getGuide().getPrenom());
        Hyperlink guideLink = new Hyperlink("\uD83D\uDE4D\u200D♂\uFE0F\u200B  Guide: " + excursion.getGuide().getNom() + " " + excursion.getGuide().getPrenom());
        guideLink.setStyle("-fx-font-size: 14px; -fx-text-fill: #0077cc; -fx-underline: true;");

// Ajouter un événement au clic
        guideLink.setOnAction(e -> afficherInfosGuide(excursion.getGuide()));
// Appliquer des styles en ligne à chaque Label
        nomLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #d2691e;");
        VilleDepartLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");


        lieuLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
        prixLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #016c03;");  // couleur pour le prix
        dateAllerLabel.setStyle("-fx-font-size: 18px; -fx-font-style: italic; -fx-text-fill: #555555;");
        dateRetourLabel.setStyle("-fx-font-size: 18px; -fx-font-style: italic; -fx-text-fill: #555;");

      //  desc.setStyle("-fx-font-size: 10px; -fx-font-style: italic; -fx-text-fill: #666;");
        placesDispoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
        guideLink.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");


        // Groupement des boutons
        VBox buttonVBox = new VBox(5); // Espacement vertical entre les boutons

        // Création des boutons
        Button supprimerButton = createButton("❌ Supprimer", e -> supprimerExcursion(excursion, excursionCard));
        Button modifierButton = createButton("\uD83D\uDD8A  Modifier", e -> ouvrirFormulaireModification(excursion));

// Appliquer le style à "Supprimer"
        supprimerButton.setStyle(
                "-fx-font-family: 'Segoe UI', sans-serif; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-color: #e63946; " + // Rouge pour supprimer
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-padding: 10px 20px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, #000000, 5, 0, 2, 2);"
        );

        supprimerButton.setOnMouseEntered(e -> supprimerButton.setStyle(
                "-fx-font-family: 'Segoe UI', sans-serif; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-color: #DC143C; " + // Changer la couleur au survol
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-padding: 10px 20px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, #d62828, 5, 0, 3, 3);"
        ));

        supprimerButton.setOnMouseExited(e -> supprimerButton.setStyle(
                "-fx-font-family: 'Segoe UI', sans-serif; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-color: #e63946; " + // Retour à la couleur d'origine
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-padding: 10px 20px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, #000000, 5, 0, 2, 2);"
        ));

// Appliquer le style à "Modifier"
        modifierButton.setStyle(
                "-fx-font-family: 'Segoe UI', sans-serif; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-color: #12989e; " + // Vert pour modifier
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-padding: 10px 20px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, #000000, 5, 0, 2, 2);"
        );

        modifierButton.setOnMouseEntered(e -> modifierButton.setStyle(
                "-fx-font-family: 'Segoe UI', sans-serif; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-color: #12989e; " + // Changer la couleur au survol
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-padding: 10px 20px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, #264653, 5, 0, 3, 3);"
        ));

        modifierButton.setOnMouseExited(e -> modifierButton.setStyle(
                "-fx-font-family: 'Segoe UI', sans-serif; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-color: #2a9d8f; " + // Retour à la couleur d'origine
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-padding: 10px 20px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, #000000, 5, 0, 2, 2);"
        ));
        excursionCard.setOnMouseEntered(e -> excursionCard.setStyle("-fx-background-color: #dfe6e9; -fx-border-radius: 10px; -fx-border-color: #b2bec3;"));
        excursionCard.setOnMouseExited(e -> excursionCard.setStyle("-fx-background-color: #eaeaea; -fx-border-radius: 10px; -fx-border-color: #dcdcdc;"));

        buttonVBox.getChildren().addAll(supprimerButton, modifierButton );

        // Ajout des éléments dans la carte (HBox)
        textVBox.getChildren().addAll(nomLabel, VilleDepartLabel, lieuLabel, prixLabel, dateAllerLabel, dateRetourLabel,detailLink, placesDispoLabel, guideLink, buttonVBox);
        excursionCard.getChildren().addAll(imageView, textVBox);

        return excursionCard;
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
    private Image chargerImageGuide(String imageFileName) {
        if (imageFileName == null || imageFileName.trim().isEmpty()) {
            return new Image("http://127.0.0.1:8000/uploads/guide/default.jpg");
        }

        try {
            String imageUrl = "http://127.0.0.1:8000/uploads/guide/" + imageFileName;
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.connect();

            if (connection.getResponseCode() == 200) {
                return new Image(connection.getInputStream());
            } else {
                System.out.println("❌ HTTP " + connection.getResponseCode() + " pour guide : " + imageFileName);
            }
        } catch (Exception e) {
            System.out.println("❌ Exception image guide : " + e.getMessage());
        }

        return new Image("http://127.0.0.1:8000/uploads/guide/default.jpg");
    }

    private void afficherCoordonnees() {
        List<String> adresses = excursionService.getAdressesExcursions();
        if (adresses.isEmpty()) {
System.out.println("pas d'adresses");
return;
        }

        StringBuilder result = new StringBuilder();
        HttpClient client = HttpClient.newHttpClient();

        for (String adresse : adresses) {
            try {
                String encodedAdresse = URLEncoder.encode(adresse, StandardCharsets.UTF_8);
                String url = "https://us1.locationiq.com/v1/search.php?key=" + LOCATIONIQ_API_KEY +
                        "&q=" + encodedAdresse + "&format=json";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JSONArray jsonArray = new JSONArray(response.body());
                    if (!jsonArray.isEmpty()) {
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        String lat = jsonObject.getString("lat");
                        String lon = jsonObject.getString("lon");
                        result.append(adresse).append(" -> Latitude: ").append(lat).append(", Longitude: ").append(lon).append("\n");
                   System.out.println(result.toString());
                    } else {
                        result.append("Aucune coordonnée trouvée pour ").append(adresse).append("\n");
                    }
                } else {
                    result.append("Erreur API pour ").append(adresse).append(" (Code: ").append(response.statusCode()).append(")\n");
                }
            } catch (Exception e) {
                result.append("Erreur pour ").append(adresse).append(": ").append(e.getMessage()).append("\n");
            }
        }

    }

    // Create a button with action
    private Button createButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button(text);
        button.setMinWidth(100);
        button.setOnAction(handler);
        return button;
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


    // Delete an excursion
    private void supprimerExcursion(Excursion excursion, HBox excursionCard) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Suppression d'une excursion");
        alert.setContentText("Voulez-vous vraiment supprimer l'excursion : " + excursion.getNom() + " ?");

        ButtonType buttonTypeYes = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType buttonTypeNo = new ButtonType("Non", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == buttonTypeYes) {
                try {
                    excursionService.supprimer(excursion.getIdExcursion());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                excursionVBox.getChildren().remove(excursionCard); // Supprimer la carte de l'affichage
                System.out.println("Excursion supprimée: " + excursion.getNom());
            }
        });
    }

    // Refresh the list of excursions after modification/deletion
    public void refreshExcursions() {
        afficherExcursions(); // Reload and display the excursions
    }
    public void rafraichirAffichage() {
        afficherExcursions();
    }

    // Open the form to add a new excursion
    @FXML
    private void ouvrirFormulaireAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterExcursion.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Ajouter Excursion");

            AjouterExcursionController ajouterController = loader.getController();
            ajouterController.setAfficherExcursionsController(this); // Pass the reference to refresh after addition

            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Erreur lors du chargement du formulaire d'ajout.");
        }
    }

    // Open the form to modify an excursion
    private void ouvrirFormulaireModification(Excursion excursion) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierExcursion.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Modifier Excursion");

            ModifierExcursionController modifierController = loader.getController();
            modifierController.setExcursionToModif(excursion);
            modifierController.setAfficherExcursionsController(this);

            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Erreur lors du chargement du formulaire de modification.");
        }
    }
}
