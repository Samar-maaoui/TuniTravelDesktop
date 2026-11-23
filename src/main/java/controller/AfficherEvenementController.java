package controller;

import entities.Evenement;
import entities.TypeUtilisateur;
import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.ServiceEvenement;
import utils.SessionManager;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class AfficherEvenementController {
    @FXML
    private ComboBox<String> categoryFilter;
    @FXML
    private TextField searchTextField;
    @FXML
    private VBox eventCardsContainer;

    @FXML
    private Button btnAjouter;

    @FXML
    private ScrollPane scrollPane;

    private EventCategory selectedCategory = EventCategory.ALL;



    public enum EventCategory {
        ALL("Tous"),
        CONFÉRENCE("Conférence"),
        SÉMINAIRE("Séminaire"),
        CONCERT("Concert"),
        SPORT("Sport");

        private final String label;

        EventCategory(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public static EventCategory fromLabel(String label) {
            for (EventCategory category : values()) {
                if (category.getLabel().equalsIgnoreCase(label)) {
                    return category;
                }
            }
            return ALL;
        }
    }

    private int getCurrentUserId() {
        return SessionManager.getUtilisateur().getIdUtilisateur();
    }

    @FXML
    private void initialize() {
        loadEvents();
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        categoryFilter.getItems().addAll(
                EventCategory.ALL.getLabel(),
                EventCategory.CONFÉRENCE.getLabel(),
                EventCategory.SÉMINAIRE.getLabel(),
                EventCategory.CONCERT.getLabel(),
                EventCategory.SPORT.getLabel()
        );
        categoryFilter.setValue(EventCategory.ALL.getLabel());

        categoryFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            selectedCategory = EventCategory.fromLabel(newValue);
            filterEvents();
        });

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterEvents();
        });

        if (!isAdminUser()) {
            btnAjouter.setDisable(true);
            btnAjouter.setVisible(false);
        } else {
            btnAjouter.setDisable(false);
            btnAjouter.setVisible(true);
        }
    }

    private VBox createEventCard(Evenement evenement) {
        VBox card = new VBox(3);
        card.getStyleClass().add("event-card");

        Label nameLabel = new Label(evenement.getNom());
        nameLabel.getStyleClass().add("event-card-name");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 30px; -fx-text-fill: #2C3E50;");

        VBox content = new VBox(5);
        content.getStyleClass().add("event-card-content");
        content.setStyle("-fx-background-color: #ECF0F1; -fx-padding: 50px; -fx-border-radius: 10px;");

        Label dateLabel = new Label("📅 Date          :" + evenement.getDateEvenement());
        dateLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #2C3E50;");

        Label descriptionLabel = new Label("📝 Description:" + evenement.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(600);
        descriptionLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #2C3E50;");

        Label typeLabel = new Label("📌 Type          :" + evenement.getType());
        typeLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #2C3E50;");

        Label lieuLabel = new Label("📍 Lieu           :" + evenement.getLieu());
        lieuLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #2C3E50;");

        Label capaciteLabel = new Label("👥 Capacité    :" + evenement.getCapacite());
        capaciteLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #2C3E50;");

        Label prixLabel = new Label("💰 Prix            :" + evenement.getPrix() + " TND");
        prixLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #2C3E50;");

        content.getChildren().addAll(dateLabel, descriptionLabel, typeLabel, lieuLabel, capaciteLabel, prixLabel);

        HBox buttonBox = new HBox(5);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.setStyle("-fx-padding: 20px;");

        if (isAdminUser()) {
            Button modifierButton = new Button("✏");
            modifierButton.getStyleClass().add("icon-button modifier-button");
            modifierButton.setStyle("-fx-background-color: rgba(6, 8, 90, 0.6); -fx-text-fill: white; -fx-font-size: 16px;");

            modifierButton.setOnAction(event -> {
                try {
                    goToModifier(evenement.getIdEvenement());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            Button deleteButton = new Button("🗑");
            deleteButton.getStyleClass().add("icon-button delete-button");
            deleteButton.setStyle("-fx-background-color: rgba(6, 8, 90, 0.6); -fx-text-fill: white; -fx-font-size: 16px;");
            deleteButton.setOnAction(event -> deleteEvent(evenement.getIdEvenement()));

            buttonBox.getChildren().addAll(modifierButton, deleteButton);
        }

        Button weatherButton = null;
        weatherButton = new Button("🌤 Voir la Météo");
        weatherButton.setStyle("-fx-background-color: #007BFF; -fx-text-fill: white; -fx-font-size: 16px;");
        weatherButton.setOnAction(event -> fetchWeatherForEvent(evenement.getLieu()));

        if (isVipUser()) {

            Button reserverButton = new Button("Réserver");
            reserverButton.getStyleClass().add("icon-button reserver-button");
            reserverButton.setStyle("-fx-background-color: #007BFF; -fx-text-fill: white; -fx-font-size: 16px;");

            reserverButton.setOnAction(event -> {
                try {
                    int idEvenement = evenement.getIdEvenement();
                    int capacite = evenement.getCapacite();
                    int idUtilisateur = getCurrentUserId();

                    goToCreerReservation(idEvenement, capacite, idUtilisateur);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            buttonBox.getChildren().add(reserverButton);
        }

        card.getChildren().addAll(nameLabel, content, buttonBox, weatherButton);
        return card;
    }

    public void goToCreerReservation(int idEvenement, int capacite, int idUtilisateur) throws IOException {
        if (!isVipUser()) {
            showAlert("Accès refusé", "Seuls les utilisateurs VIP peuvent réserver un événement.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/CreerReservation.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = new Stage();
        stage.setTitle("Créer une Réservation");
        stage.setScene(scene);

        controller.CreerReservationController controller = loader.getController();
        controller.setIdEvenement(idEvenement, capacite, idUtilisateur);

        stage.setOnHidden(event -> refreshEvents());
        stage.show();
    }

    private void loadEvents() {
        ServiceEvenement serviceEvenement = new ServiceEvenement();
        List<Evenement> evenements = serviceEvenement.getEvenements();

        eventCardsContainer.getChildren().clear();

        if (evenements != null && !evenements.isEmpty()) {
            for (Evenement evenement : evenements) {
                VBox eventCard = createEventCard(evenement);
                eventCardsContainer.getChildren().add(eventCard);
            }
        } else {
            Label noEventsLabel = new Label("Aucun événement disponible.");
            eventCardsContainer.getChildren().add(noEventsLabel);
        }
    }

    @FXML
    public void goToAjouter() throws IOException {
        if (!isAdminUser()) {
            showAlert("Accès refusé", "Seuls les administrateurs peuvent ajouter un événement.");
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterEvenement.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = new Stage();
        stage.setTitle("Ajouter un Événement");
        stage.setScene(scene);
        stage.setOnHidden(event -> refreshEvents());
        stage.show();
    }

    public void goToModifier(int idEvenement) throws IOException {
        if (!isAdminUser()) {
            showAlert("Accès refusé", "Seuls les administrateurs peuvent modifier un événement.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierEvenement.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = new Stage();
        stage.setTitle("Modifier un Événement");
        stage.setScene(scene);

        ModifierEvenementController controller = loader.getController();
        controller.loadEvenement(idEvenement);

        stage.setOnHidden(event -> refreshEvents());
        stage.show();
    }

    @FXML
    private void deleteEvent(int idEvenement) {
        if (!isAdminUser()) {
            showAlert("Accès refusé", "Seuls les administrateurs peuvent supprimer un événement.");
            return;
        }

        boolean confirmation = confirmDeletion();
        if (!confirmation) return;

        ServiceEvenement serviceEvenement = new ServiceEvenement();
        serviceEvenement.supprimerEvenement(idEvenement);

        refreshEvents();
    }

    private boolean confirmDeletion() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Voulez-vous vraiment supprimer cet événement ?");
        alert.setContentText("Cette action est irréversible.");

        ButtonType yesButton = new ButtonType("Oui");
        ButtonType noButton = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton);

        return alert.showAndWait().orElse(noButton) == yesButton;
    }

    @FXML
    private void refreshEvents() {
        eventCardsContainer.getChildren().clear();
        loadEvents();
    }

    @FXML
    private void filterEvents() {
        ServiceEvenement serviceEvenement = new ServiceEvenement();
        List<Evenement> allEvenements = serviceEvenement.getEvenements();

        String searchQuery = searchTextField.getText().toLowerCase();

        List<Evenement> filteredEvenements = allEvenements.stream()
                .filter(e -> (selectedCategory == EventCategory.ALL || e.getType().equalsIgnoreCase(String.valueOf(selectedCategory))) &&
                        (e.getNom().toLowerCase().contains(searchQuery) ||
                                e.getDescription().toLowerCase().contains(searchQuery)))
                .collect(Collectors.toList());

        eventCardsContainer.getChildren().clear();
        if (filteredEvenements.isEmpty()) {
            Label noEventsLabel = new Label("Aucun événement trouvé.");
            eventCardsContainer.getChildren().add(noEventsLabel);
        } else {
            for (Evenement evenement : filteredEvenements) {
                VBox eventCard = createEventCard(evenement);
                eventCardsContainer.getChildren().add(eventCard);
            }
        }
    }

    private boolean isAdminUser() {
        Utilisateur utilisateur = SessionManager.getUtilisateur();
        return utilisateur != null && utilisateur.getTypeUtilisateur() == TypeUtilisateur.ADMIN;
    }

    private boolean isVipUser() {
        Utilisateur utilisateur = SessionManager.getUtilisateur();
        return utilisateur != null && utilisateur.getTypeUtilisateur() == TypeUtilisateur.VIP;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void goToCalendar(MouseEvent mouseEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Calendar.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = new Stage();
        stage.setTitle("Calendrier");
        stage.setScene(scene);
        stage.setOnHidden(event -> refreshEvents());
        stage.show();
    }
    private void fetchWeatherForEvent(String lieu) {
        try {
            // Extract the part after the comma in the lieu string
            String location = lieu.contains(",") ? lieu.split(",")[1].trim() : lieu.trim();

            // Load the weatherController from its FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/weather.fxml"));
            Parent root = loader.load(); // Load the FXML file
            weatherController weatherController = loader.getController(); // Get the controller instance

            // Set the location in the cityField
            weatherController.cityField.setText(location);

            // Fetch the weather data
            weatherController.fetchWeather();

            // Display the weather data in a new window using the loaded FXML
            Stage weatherStage = new Stage();
            Scene weatherScene = new Scene(root); // Use the loaded FXML root
            weatherStage.setScene(weatherScene);
            weatherStage.setTitle("Météo pour " + location);
            weatherStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de récupérer les données météo pour " + lieu);
        }
    }
}
