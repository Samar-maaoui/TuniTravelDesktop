package controller;

import entities.TypeUtilisateur;
import entities.Utilisateur;
import entities.reservationvol;
import entities.vol;
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
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.c_reservationvol;
import services.c_vol;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class mesReservationController {
    @FXML
    private Button sup;
    @FXML
    private Button retourto;
    // Nouveau bouton de statistiques si besoin dans le FXML
    @FXML
    private Button statsButton;

    // Ajout du PieChart pour afficher les statistiques
    @FXML
    private PieChart pieChart;

    @FXML
    private Parent root;
    @FXML
    private ListView<reservationvol> listViewVols;

    private final c_reservationvol reservationService = new c_reservationvol();
    private final c_vol serviceVol = new c_vol();
    private boolean isAdminUser;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            // Appliquer les styles CSS à la scène
            if (root != null && root.getScene() != null) {
                root.getScene().getStylesheets().clear();
                root.getScene().getStylesheets().add(getClass().getResource("/vol_styles.css").toExternalForm());
                System.out.println("Styles appliqués: " + root.getScene().getStylesheets());
            }
        });
    }

    @FXML
    private void supprimerreservation(ActionEvent actionEvent) {
        reservationvol selectedReservation = listViewVols.getSelectionModel().getSelectedItem();
        if (selectedReservation != null) {
            try {
                reservationService.supprimer(selectedReservation);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Suppression réussie");
                alert.setHeaderText(null);
                alert.setContentText("La réservation a été supprimée avec succès.");
                alert.showAndWait();
                listViewVols.getItems().remove(selectedReservation);
            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Une erreur s'est produite lors de la suppression.");
                alert.showAndWait();
            }
        }
    }

    public void navigatetovolutili(ActionEvent actionEvent) {
        try {
            FXMLLoader loader;



            if (isAdminUser()) {
                loader = new FXMLLoader(getClass().getResource("/Interfaceadmin.fxml"));
            } else {
                loader = new FXMLLoader(getClass().getResource("/InterfaceClient.fxml"));
            }

            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) retourto.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void afficher(ActionEvent actionEvent) {
        try {
            List<reservationvol> reservations = reservationService.getAllReservations();
            listViewVols.getItems().clear();
            listViewVols.getItems().addAll(reservations);
            listViewVols.setCellFactory(param -> new ListCell<reservationvol>() {
                @Override
                protected void updateItem(reservationvol r, boolean empty) {
                    super.updateItem(r, empty);
                    if (empty || r == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        VBox card = createReservationCard(r);
                        setGraphic(card);
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createReservationCard(reservationvol r) {
        try {
            vol v = serviceVol.getvolbyid(r.getId_vol());
            if (v == null) {
                throw new SQLException("Vol introuvable");
            }
            ImageView iconAvion = new ImageView(new Image(getClass().getResource("/images/plane2.png").toExternalForm()));
            iconAvion.setFitWidth(20);
            iconAvion.setFitHeight(20);

            ImageView iconCompagnie = new ImageView(new Image(getClass().getResource("/images/compagnie.png").toExternalForm()));
            iconCompagnie.setFitWidth(20);
            iconCompagnie.setFitHeight(20);

            ImageView iconDepart = new ImageView(new Image(getClass().getResource("/images/depart.png").toExternalForm()));
            iconDepart.setFitWidth(20);
            iconDepart.setFitHeight(20);

            ImageView iconArrivee = new ImageView(new Image(getClass().getResource("/images/arrivee.png").toExternalForm()));
            iconArrivee.setFitWidth(20);
            iconArrivee.setFitHeight(20);

            ImageView iconPrix = new ImageView(new Image(getClass().getResource("/images/money.png").toExternalForm()));
            iconPrix.setFitWidth(20);
            iconPrix.setFitHeight(20);

            ImageView iconDisponible = new ImageView(new Image(getClass().getResource("/images/yes.png").toExternalForm()));
            iconDisponible.setFitWidth(20);
            iconDisponible.setFitHeight(20);

            ImageView iconDateDepart = new ImageView(new Image(getClass().getResource("/images/d_depart.png").toExternalForm()));
            iconDateDepart.setFitWidth(20);
            iconDateDepart.setFitHeight(20);

            ImageView iconDateArrivee = new ImageView(new Image(getClass().getResource("/images/d_arrivee.png").toExternalForm()));
            iconDateArrivee.setFitWidth(20);
            iconDateArrivee.setFitHeight(20);

            Label volLabel = new Label(String.format("Vol: %s", v.getnumero_vol()));
            volLabel.setStyle("-fx-font-size: 18px;");

            Label compagnieLabel = new Label(String.format("Compagnie: %s", v.getCompagnie()));
            compagnieLabel.setStyle("-fx-font-size: 18px;");

            Label departLabel = new Label(String.format("Départ: %s", v.getaero_depart()));
            departLabel.setStyle("-fx-font-size: 18px;");

            Label arriveeLabel = new Label(String.format("Arrivée: %s", v.getaero_arrivee()));
            arriveeLabel.setStyle("-fx-font-size: 18px;");

            Label prixLabel = new Label(String.format("Prix: %.2f€", v.getPrix()));
            prixLabel.setStyle("-fx-font-size: 18px;");

            Label serviceLabel = new Label(String.format("Service: %s", r.getClasse_service().name()));
            serviceLabel.setStyle("-fx-font-size: 18px;");

            ImageView iconService = new ImageView(new Image(getClass().getResource("/images/service.png").toExternalForm()));
            iconService.setFitWidth(20);
            iconService.setFitHeight(20);

            HBox volHBox = new HBox(5, iconAvion, volLabel);
            HBox compagnieHBox = new HBox(5, iconCompagnie, compagnieLabel);
            HBox departHBox = new HBox(5, iconDepart, departLabel);
            HBox arriveeHBox = new HBox(5, iconArrivee, arriveeLabel);
            HBox prixHBox = new HBox(5, iconPrix, prixLabel);
            HBox serviceHBox = new HBox(5, iconService, serviceLabel);

            volHBox.setAlignment(Pos.CENTER_LEFT);
            compagnieHBox.setAlignment(Pos.CENTER_LEFT);
            departHBox.setAlignment(Pos.CENTER_LEFT);
            arriveeHBox.setAlignment(Pos.CENTER_LEFT);
            prixHBox.setAlignment(Pos.CENTER_LEFT);
            serviceHBox.setAlignment(Pos.CENTER_LEFT);

            VBox card = new VBox(10, volHBox, compagnieHBox, departHBox, arriveeHBox, prixHBox, serviceHBox);
            card.setPadding(new Insets(15));
            card.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-padding: 15px; -fx-border-color: #ccc;");
            card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #e9ecef; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-padding: 15px; -fx-border-color: #bbb;"));
            card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-padding: 15px; -fx-border-color: #ccc;"));

            return card;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Fonction de statistiques : calcule la répartition des réservations par type de service et met à jour le PieChart
    @FXML
    public void afficherStatistiques(ActionEvent event) {
        try {
            List<reservationvol> reservations = reservationService.getAllReservations();
            Map<String, Integer> stats = new HashMap<>();
            // Comptage des réservations par type (en utilisant le nom de la classe de service)
            for (reservationvol r : reservations) {
                String type = r.getClasse_service().name();
                stats.put(type, stats.getOrDefault(type, 0) + 1);
            }

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            int totalReservations = reservations.size(); // Nombre total de réservations

            // Calcul des pourcentages et ajout des données dans le PieChart
            for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                double percentage = (entry.getValue() * 100.0) / totalReservations; // Calcul du pourcentage
                PieChart.Data data = new PieChart.Data(entry.getKey() + " (" + String.format("%.2f", percentage) + "%)", entry.getValue());
                pieChartData.add(data);
            }

            pieChart.setData(pieChartData);
            pieChart.setTitle("Statistiques de réservations par service");

            // Utilisation de Platform.runLater pour attendre que le graphique soit affiché
            Platform.runLater(() -> {
                for (PieChart.Data data : pieChart.getData()) {
                    // Ajouter un événement de survol pour afficher les pourcentages
                    data.getNode().setOnMouseEntered(mouseEvent -> {
                        Tooltip tooltip = new Tooltip(String.format("%.2f%%", data.getPieValue() * 100 / totalReservations));
                        Tooltip.install(data.getNode(), tooltip);
                    });
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Une erreur s'est produite lors du calcul des statistiques.");
            alert.showAndWait();
        }
    }
    private boolean isAdminUser() {
        Utilisateur utilisateur = SessionManager.getUtilisateur();
        return utilisateur != null && utilisateur.getTypeUtilisateur() == TypeUtilisateur.ADMIN;
    }

}
