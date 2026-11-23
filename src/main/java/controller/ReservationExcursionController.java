package controller;
import javafx.concurrent.Worker;

import entities.Excursion;
import entities.ReservationExcursion;
import entities.Utilisateur;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import services.ReservationExcursionService;
import services.ServicePaiementStripe;
import services.UtilisateurService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class ReservationExcursionController {
    private final ReservationExcursionService reservationService = new ReservationExcursionService();
    private final UtilisateurService utilisateurService = new UtilisateurService();
    private Excursion excursion; // Excursion sélectionnée
    private Utilisateur utilisateurActuel; // Utilisateur connecté

    @FXML
    private Label excursionLabel, montantTotalLabel;

    @FXML
    private Spinner<Integer> nbrPlacesSpinner;

    @FXML
    private Button reserverButton;

    public ReservationExcursionController() throws SQLException {}

    @FXML
    public void initialize() {
        // Charger l'utilisateur connecté
        setUtilisateur();

    }

    // Définir l'excursion sélectionnée
    public void setExcursion(Excursion excursion) {
        if (excursion == null) {
            System.err.println("Erreur : Excursion non définie !");
            return;
        }

        this.excursion = excursion;
        excursionLabel.setText("Excursion : " + excursion.getNom());

        // Initialisation du Spinner (minimum 1, maximum = nb places disponibles)
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, excursion.getNbPlace(), 1);
        nbrPlacesSpinner.setValueFactory(valueFactory);

        // Mise à jour automatique du montant total
        nbrPlacesSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                double montantTotal = newValue * excursion.getPrix();
                montantTotalLabel.setText("Montant Total: " + String.format("%.2f", montantTotal) + " TND");
            }
        });
    }

    // Charger l'utilisateur connecté
    public void setUtilisateur() {
        this.utilisateurActuel = utilisateurService.getUtilisateurConnecte();
        if (utilisateurActuel != null) {
            System.out.println("Utilisateur connecté : " + utilisateurActuel.getNom());
        } else {
            System.out.println("Aucun utilisateur connecté.");
        }
    }

    @FXML
    public void reserver() {
        if (excursion == null) {
            System.out.println("Erreur : Aucune excursion sélectionnée !");
            showAlert("Erreur", "Aucune excursion sélectionnée. Veuillez choisir une excursion.", Alert.AlertType.ERROR);
            return;
        }

        int nbrPlaces = nbrPlacesSpinner.getValue();
        double montantTotal = nbrPlaces * excursion.getPrix();

        // Créer une session de paiement Stripe
        String paymentUrl = ServicePaiementStripe.createPaymentSession(montantTotal);
        if (paymentUrl != null) {
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();
            webEngine.load(paymentUrl);

            webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    String currentUrl = webEngine.getLocation();
                    if (currentUrl.contains("success")) {
                        System.out.println("Paiement réussi !");
                        Stage stage = (Stage) webView.getScene().getWindow();
                        stage.close();

                        // Ajouter la réservation
                        ReservationExcursion reservation = new ReservationExcursion(
                                utilisateurActuel, new Date(), "CONFIRMEE", montantTotal, excursion, nbrPlaces
                        );

                        try {
                            reservationService.ajouter(reservation);
                        } catch (SQLException e) {
                            e.printStackTrace();
                            showAlert("Erreur", "Impossible d'effectuer la réservation.", Alert.AlertType.ERROR);
                            return;
                        }

                        showAlert("Réservation Confirmée",
                                "Vous avez réservé " + nbrPlaces + " places pour l'excursion : " + excursion.getNom(),
                                Alert.AlertType.INFORMATION);

                        generateAndShowQRCode(utilisateurActuel.getIdUtilisateur(), utilisateurActuel.getNom(),
                                excursion.getNom(), nbrPlaces, excursion.getDateDepart(), excursion.getDateRetour());

                        // Fermer la fenêtre de réservation après succès
                        Stage reservationStage = (Stage) reserverButton.getScene().getWindow();
                        reservationStage.close();
                    } else if (currentUrl.contains("cancel")) {
                        System.out.println("Paiement annulé.");
                        showAlert("Annulation", "Le paiement a été annulé.", Alert.AlertType.WARNING);
                    }
                }
            });

            Stage paymentStage = new Stage();
            StackPane root = new StackPane();
            root.getChildren().add(webView);
            Scene scene = new Scene(root, 800, 600);
            paymentStage.setTitle("Paiement Stripe");
            paymentStage.setScene(scene);
            paymentStage.show();
        } else {
            System.out.println("Erreur lors de la création de la session de paiement.");
            showAlert("Erreur", "Impossible de générer la session de paiement.", Alert.AlertType.ERROR);
        }
    }



    private void generateAndShowQRCode(int userId, String utilisateurNom, String excursionName, int placesReserved, LocalDate dateAller, LocalDate dateRetour) {
        try {
            // Formater les dates en chaîne de caractères
            String qrText = "Billet électronique pour " +
                    "\n👤 Nom: " + utilisateurNom +
                    "\n🏕️ Excursion: " + excursionName +
                    "\n🎟️ Places réservées: " + placesReserved +
                    "\n📅 Date d'aller: " + dateAller +
                    "\n📅 Date de retour: " + dateRetour;

            // Générer le QR Code à partir du texte
            String qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=" + URLEncoder.encode(qrText, StandardCharsets.UTF_8);

            // Charger l’image du QR code
            Image qrImage = new Image(qrCodeUrl);
            ImageView imageView = new ImageView(qrImage);
            imageView.setFitWidth(250);
            imageView.setFitHeight(250);



            // Créer une nouvelle fenêtre pour afficher le QR code
            Stage qrStage = new Stage();
            VBox root = new VBox(10, imageView);
            root.setStyle("-fx-padding: 20px; -fx-alignment: center;");

            Scene scene = new Scene(root);
            qrStage.setTitle("QR Code de Réservation");
            qrStage.setScene(scene);
            qrStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de générer le QR Code.", Alert.AlertType.ERROR);
        }
    }


    // Afficher le texte lorsque le bouton est cliqué
    private void showTextDetails(String qrText) {
        // Afficher le texte directement dans une nouvelle fenêtre
        Stage textStage = new Stage();
        TextArea textArea = new TextArea();
        textArea.setEditable(false);  // Ne pas permettre à l'utilisateur d'éditer
        textArea.setText("Détails de la réservation : \n\n" + qrText);

        VBox root = new VBox(10);
        root.getChildren().add(textArea);

        Scene scene = new Scene(root, 400, 300);
        textStage.setTitle("Détails de la Réservation");
        textStage.setScene(scene);
        textStage.show();
    }


    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
