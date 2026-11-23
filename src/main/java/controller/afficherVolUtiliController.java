package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import entities.vol;
import services.c_vol;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class afficherVolUtiliController {
    @FXML
    private Button btnmesreservation;
    @FXML
    private ListView<vol> listViewVols; // ListView typée en vol
    @FXML
    private Parent root;  // Ajouter une référence à la racine de la scène
    @FXML
    private DatePicker dateDepartField, dateArriveeField;
    private final c_vol serviceVol = new c_vol();
    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            // Appliquer les styles CSS à la scène
            if (root != null && root.getScene() != null) {
                root.getScene().getStylesheets().clear(); // Supprimer les styles existants
                root.getScene().getStylesheets().add(getClass().getResource("/vol_styles.css").toExternalForm()); // Ajouter le nouveau style
                System.out.println("Styles appliqués: " + root.getScene().getStylesheets());
            }
        });
    }


    @FXML
    public void afficherVols() {
        try {
            // Charger les vols depuis le service
            List<vol> vols = serviceVol.getAllvols();

            // Vérifier si la liste est vide ou nulle
            if (vols == null || vols.isEmpty()) {
                System.out.println("Aucun vol à afficher.");
            }

            // Effacer les éléments existants de la ListView
            listViewVols.getItems().clear();

            // Ajouter tous les vols à la ListView
            listViewVols.getItems().addAll(vols);

            // Définir la cellule personnalisée
            listViewVols.setCellFactory(param -> new ListCell<vol>() {
                @Override
                protected void updateItem(vol v, boolean empty) {
                    super.updateItem(v, empty);
                    if (empty || v == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        // Créer la carte de vol
                        VBox card = createFlightCard(v);
                        setGraphic(card);
                    }
                }
            });

        } catch (SQLException e) {
            showError("Erreur de chargement", "Impossible d'afficher les vols", e);
        }
    }
    private VBox createFlightCard(vol v) {
        // 📌 Icône d'avion
        ImageView iconAvion = new ImageView(new Image(getClass().getResource("/images/plane2.png").toExternalForm()));
        iconAvion.setFitWidth(20);
        iconAvion.setFitHeight(20);

        // 📍 Icône de la compagnie
        ImageView iconCompagnie = new ImageView(new Image(getClass().getResource("/images/compagnie.png").toExternalForm()));
        iconCompagnie.setFitWidth(20);
        iconCompagnie.setFitHeight(20);

        // 🛫 Icône de départ
        ImageView iconDepart = new ImageView(new Image(getClass().getResource("/images/depart.png").toExternalForm()));
        iconDepart.setFitWidth(20);
        iconDepart.setFitHeight(20);

        // 🛬 Icône d'arrivée
        ImageView iconArrivee = new ImageView(new Image(getClass().getResource("/images/arrivee.png").toExternalForm()));
        iconArrivee.setFitWidth(20);
        iconArrivee.setFitHeight(20);

        // 💰 Icône de prix
        ImageView iconPrix = new ImageView(new Image(getClass().getResource("/images/money.png").toExternalForm()));
        iconPrix.setFitWidth(20);
        iconPrix.setFitHeight(20);

        // ✅ Icône de disponibilité
        ImageView iconDisponible = new ImageView(new Image(getClass().getResource("/images/yes.png").toExternalForm()));
        iconDisponible.setFitWidth(20);
        iconDisponible.setFitHeight(20);

        // 📅 Icône de date de départ et arrivée
        ImageView iconDateDepart = new ImageView(new Image(getClass().getResource("/images/d_depart.png").toExternalForm()));
        iconDateDepart.setFitWidth(20);
        iconDateDepart.setFitHeight(20);

        ImageView iconDateArrivee = new ImageView(new Image(getClass().getResource("/images/d_arrivee.png").toExternalForm()));
        iconDateArrivee.setFitWidth(20);
        iconDateArrivee.setFitHeight(20);

        // 🎨 Infos du vol
        Label volLabel = new Label(String.format("Vol: %s", v.getnumero_vol()));
        volLabel.setStyle("-fx-font-size: 18px; ");

        Label compagnieLabel = new Label(String.format("Compagnie: %s", v.getCompagnie()));
        compagnieLabel.setStyle("-fx-font-size: 18px; ");

        Label departLabel = new Label(String.format("Départ: %s", v.getaero_depart()));
        departLabel.setStyle("-fx-font-size: 18px;");

        Label arriveeLabel = new Label(String.format("Arrivée: %s", v.getaero_arrivee()));
        arriveeLabel.setStyle("-fx-font-size: 18px;");

        Label prixLabel = new Label(String.format("Prix: %.2f€", v.getPrix()));
        prixLabel.setStyle("-fx-font-size: 18px;");

        Label dispoLabel = new Label(String.format("Disponible: %s", v.getDisponible()));
        dispoLabel.setStyle("-fx-font-size: 18px;");

        Label dateDepartLabel = new Label(String.format("Date_Départ: %s", v.getDate_depart()));
        dateDepartLabel.setStyle("-fx-font-size: 18px;");

        Label dateArriveeLabel = new Label(String.format("Date_Arrivée: %s", v.getDate_arrivee()));
        dateArriveeLabel.setStyle("-fx-font-size: 18px;");

        // Organiser chaque attribut avec son icône dans un HBox
        HBox volHBox = new HBox(5, iconAvion, volLabel);
        HBox compagnieHBox = new HBox(5, iconCompagnie, compagnieLabel);
        HBox departHBox = new HBox(5, iconDepart, departLabel);
        HBox arriveeHBox = new HBox(5, iconArrivee, arriveeLabel);
        HBox prixHBox = new HBox(5, iconPrix, prixLabel);
        HBox dispoHBox = new HBox(5, iconDisponible, dispoLabel);
        HBox dateDepartHBox = new HBox(5, iconDateDepart, dateDepartLabel);
        HBox dateArriveeHBox = new HBox(5, iconDateArrivee, dateArriveeLabel);

        // Ajuster l'alignement des HBox (non modifié)
        volHBox.setAlignment(Pos.CENTER_LEFT);
        compagnieHBox.setAlignment(Pos.CENTER_LEFT);
        departHBox.setAlignment(Pos.CENTER_LEFT);
        arriveeHBox.setAlignment(Pos.CENTER_LEFT);
        prixHBox.setAlignment(Pos.CENTER_LEFT);
        dispoHBox.setAlignment(Pos.CENTER_LEFT);
        dateDepartHBox.setAlignment(Pos.CENTER_LEFT);
        dateArriveeHBox.setAlignment(Pos.CENTER_LEFT);

        // Création des boutons
        Button btnModifier = new Button("✏️ Réserver");
        btnModifier.getStyleClass().add("modifier-button");
        btnModifier.setOnAction(event -> goToreserverVols(v));

        Button btnVolInfo = new Button("ℹ️ Airport Info");
        btnVolInfo.getStyleClass().add("vol-info-button");
        btnVolInfo.setOnAction(event -> goToVolInfo(v));

        Button btnAirportInfo = new Button("🏢 Vol Info");
        btnAirportInfo.getStyleClass().add("airport-info-button");
        btnAirportInfo.setOnAction(event -> goToAirportInfo(v));

        Button btnRadarInfo = new Button("📡 Radar Info");
        btnRadarInfo.getStyleClass().add("radar-info-button");
        btnRadarInfo.setOnAction(event -> goToRadarInfo(v));

        // Organiser tous les boutons dans un HBox sur la même ligne
        HBox buttonsHBox = new HBox(10, btnVolInfo, btnAirportInfo, btnRadarInfo);
        buttonsHBox.setAlignment(Pos.CENTER); // Centrer les boutons horizontalement
        buttonsHBox.setPadding(new Insets(10)); // Ajouter de l'espacement autour des boutons

        // Organiser toutes les HBox et les boutons dans une VBox
        VBox card = new VBox(10, volHBox, compagnieHBox, departHBox, arriveeHBox, prixHBox, dispoHBox, dateDepartHBox, dateArriveeHBox, btnModifier, buttonsHBox);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-padding: 15px; -fx-border-color: #ccc;");

        // Effet au survol
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #e9ecef; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-padding: 15px; -fx-border-color: #bbb;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-padding: 15px; -fx-border-color: #ccc;"));

        return card;
    }





    @FXML
    public void chercherVol(ActionEvent actionEvent) {
        LocalDate dateDepart = dateDepartField.getValue();
        LocalDate dateArrivee = dateArriveeField.getValue();

        // Vérifier les dates saisies
        System.out.println("Date départ: " + dateDepart);
        System.out.println("Date arrivée: " + dateArrivee);

        try {
            List<vol> vols = serviceVol.getAllvols();
            if (vols == null || vols.isEmpty()) {
                System.out.println("Aucun vol trouvé.");
            }

            // Filtrage par dates
            List<vol> filteredVols = vols.stream()
                    .filter(v -> {
                        LocalDate volDateDepart = convertToLocalDate(v.getDate_depart());
                        LocalDate volDateArrivee = convertToLocalDate(v.getDate_arrivee());

                        // Vérifier les dates de chaque vol
                        System.out.println("Vol: " + v + ", Date départ: " + volDateDepart + ", Date arrivée: " + volDateArrivee);

                        return (volDateDepart.isAfter(dateDepart) || volDateDepart.isEqual(dateDepart)) &&
                                (volDateArrivee.isBefore(dateArrivee) || volDateArrivee.isEqual(dateArrivee));
                    })
                    .collect(Collectors.toList());

            System.out.println("Nombre de vols après filtrage : " + filteredVols.size());

            // Mettre à jour la ListView
            listViewVols.getItems().setAll(filteredVols);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    @FXML
    public void trierVols(ActionEvent actionEvent) {
        try {
            List<vol> vols = serviceVol.getAllvols();
            vols.sort((v1, v2) -> Integer.compare(v2.getDisponible(), v1.getDisponible()));
            // Trie en ordre décroissant : vols avec le plus de places en premier

            listViewVols.getItems().setAll(vols);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void goToVolInfo(vol v) {
        try {
            // Charger le fichier FXML pour VolApi
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/VolApi.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur VolApiController
            VolApiController volApiController = loader.getController();

            // Passer la valeur de aero_depart à VolApiController
            volApiController.setSearchQuery(v.getaero_depart());

            // Créer une nouvelle scène avec le fichier FXML chargé
            Scene scene = new Scene(root);

            // Créer un nouveau stage pour la nouvelle fenêtre
            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.setTitle("Vol Info"); // Vous pouvez définir un titre personnalisé pour la nouvelle fenêtre
            newStage.show(); // Afficher la nouvelle fenêtre

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void goToreserverVols(vol v) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterReservationVol.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur de la fenêtre de modification
            ajouterReservationVolController ajouterReservationVolController = loader.getController();

            // Passer l'objet vol au contrôleur
            ajouterReservationVolController.setVol(v);

            Scene scene = new Scene(root);
            Stage stage = new Stage(); // Ouvrir une nouvelle fenêtre
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void gotoreservation(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mesReservation.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnmesreservation.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void goToAirportInfo(vol v) {
        try {
            // Charger le fichier FXML pour VolApi2
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/VolApi2.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur VolApi2Controller
            VolApi2Controller volApiController = loader.getController();

            // Passer la valeur de numero_vol à VolApi2Controller
            volApiController.setSearchQuery(v.getnumero_vol());

            // Créer une nouvelle scène avec le fichier FXML chargé
            Scene scene = new Scene(root);

            // Créer un nouveau stage pour la nouvelle fenêtre
            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.setTitle("Airport Info"); // Vous pouvez définir un titre personnalisé pour la nouvelle fenêtre
            newStage.show(); // Afficher la nouvelle fenêtre

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void goToRadarInfo(vol v) {
        try {
            // Charger le fichier FXML pour Radar
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/radar.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène pour la nouvelle fenêtre
            Scene scene = new Scene(root);

            // Créer un nouveau stage pour ouvrir une nouvelle fenêtre
            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.setTitle("Radar Info"); // Vous pouvez changer le titre de la fenêtre ici
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showError(String title, String message, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    public void gotohelp(ActionEvent actionEvent) {
        try {
            // Charger le fichier FXML pour la météo
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/help.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène avec le fichier FXML chargé
            Scene scene = new Scene(root);

            // Créer un nouveau stage pour la nouvelle fenêtre
            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.setTitle("helpbot"); // Vous pouvez donner un titre à la nouvelle fenêtre
            newStage.show(); // Afficher la nouvelle fenêtre

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public LocalDate convertToLocalDate(java.util.Date date) {
        // Si la date est une instance de java.sql.Date
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        }
        // Si la date est une instance de java.util.Date
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }


}



