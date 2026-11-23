package controller;

import entities.ReservationExcursion;
import entities.StatutReservation;
import entities.TypeUtilisateur;
import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import services.ReservationExcursionService;
import services.UtilisateurService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ListeReservation {

    @FXML
    private ListView<ReservationExcursion> listViewReservations;
    @FXML
    private Button backButton;

    private final ReservationExcursionService reservationExcursionService = new ReservationExcursionService();
    private final UtilisateurService utilisateurService = new UtilisateurService();
    private Utilisateur utilisateurActuel;

    public ListeReservation() throws SQLException {
    }

    @FXML
    public void initialize() {
        setUtilisateur();
        if (utilisateurActuel == null) {
            System.out.println("❌ Erreur : Aucun utilisateur connecté.");
            return;
        }

        int id = utilisateurActuel.getIdUtilisateur();
        System.out.println("Utilisateur ID: " + id);
        loadReservations(id);
    }

    private void setUtilisateur() {
        utilisateurActuel = utilisateurService.getUtilisateurConnecte();
        if (utilisateurActuel != null) {
            System.out.println("✅ Utilisateur connecté : " + utilisateurActuel.getNom());
        } else {
            System.out.println("❌ Aucun utilisateur connecté.");
        }
    }

    private void loadReservations(int userId) {
        try {
            List<ReservationExcursion> reservations;

            if (utilisateurActuel.getTypeUtilisateur() == TypeUtilisateur.ADMIN){
                reservations = reservationExcursionService.lire(); // admin : toutes les réservations
            } else {
                reservations = reservationExcursionService.lireParUtilisateur(userId); // client : réservations personnelles
            }

            ObservableList<ReservationExcursion> observableReservations = FXCollections.observableArrayList(reservations);
            listViewReservations.setItems(observableReservations);

            listViewReservations.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(ReservationExcursion reservation, boolean empty) {
                    super.updateItem(reservation, empty);
                    if (empty || reservation == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        Text excursionText = new Text("📌 Excursion: " + reservation.getExcursion().getNom());
                        excursionText.setFill(Color.ORANGE);
                        excursionText.setFont(Font.font("Arial", 14));

                        Text dateText = new Text("📅 Date: " + reservation.getExcursion().getDateDepart());
                        dateText.setFont(Font.font("Arial", 14));

                        Text placesText = new Text("🎟 Places réservées: " + reservation.getNbrPlaceReservee());
                        placesText.setFont(Font.font("Arial", 14));

                        Text montantText = new Text("💰 Montant: " + reservation.getMontantTotal() + " DT");
                        montantText.setFill(Color.GREEN);
                        montantText.setFont(Font.font("Arial", 14));

                        Text statutText = new Text("⚠️ Statut: " + reservation.getStatut());
                        statutText.setFont(Font.font("Arial", 14));

                        Button cancelButton = new Button("❌ Annuler");
                        cancelButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5px 10px; -fx-border-radius: 5px;");

                        boolean isStatutEnAttente = reservation.getStatut() == StatutReservation.EN_ATTENTE;

                        cancelButton.setDisable(!isStatutEnAttente);

                        DropShadow shadow = new DropShadow();
                        shadow.setColor(Color.DARKRED);
                        cancelButton.setEffect(shadow);

                        cancelButton.setOnAction(event -> cancelReservation(reservation));

                        VBox reservationBox = new VBox(5);
                        reservationBox.getChildren().addAll(excursionText, dateText, placesText, montantText, statutText, cancelButton);

                        setGraphic(reservationBox);
                    }
                }
            });

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors du chargement des réservations.");
            e.printStackTrace();
        }
    }



    private void cancelReservation(ReservationExcursion reservation) {
        try {
            reservationExcursionService.supprimer(reservation.getIdReservation());
            System.out.println("✅ Réservation annulée: " + reservation.getIdReservation());
            loadReservations(utilisateurActuel.getIdUtilisateur());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void retour(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/MenuClient.fxml"));
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
