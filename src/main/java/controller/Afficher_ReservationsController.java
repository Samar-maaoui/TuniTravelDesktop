package controller;

import entities.Hotel;
import entities.ReservationHotel;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import services.ServiceHotel;
import services.ServiceReservationHotel;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import java.io.IOException;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
public class Afficher_ReservationsController {

    @FXML
    private ListView<HBox> listViewReservations;
    @FXML
    private Button btnSupprimer, btnAjouterReservation, btnRetour;
    private final ServiceReservationHotel serviceReservationHotel = new ServiceReservationHotel();
    private final ServiceHotel serviceHotel = ServiceHotel.getInstance();
    private int id_hotel = -1;
    private Hotel hotelSelectionne;

    @FXML
    public void initialize() {
        btnSupprimer.setDisable(true);
        btnAjouterReservation.setDisable(id_hotel == -1);

        listViewReservations.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            btnSupprimer.setDisable(newSelection == null);
        });

        if (id_hotel > 0) {
            loadReservations();
        }
    }

    public void setId_hotel(int id_hotel) throws SQLException {
        this.id_hotel = id_hotel;
        this.hotelSelectionne = serviceHotel.trouverParId(id_hotel);
        System.out.println("📥 Hôtel sélectionné : ID = " + id_hotel);
        loadReservations();
    }

    private void loadReservations() {
        if (id_hotel <= 0) {
            showAlert(Alert.AlertType.WARNING, "ID de l'hôtel invalide.");
            return;
        }

        List<ReservationHotel> reservations = serviceReservationHotel.getReservationsParHotel(id_hotel);
        ObservableList<HBox> reservationDetails = FXCollections.observableArrayList();

        for (ReservationHotel reservation : reservations) {
            reservationDetails.add(createReservationBox(reservation));
        }

        listViewReservations.setItems(reservationDetails);
    }

    /**
     * Création d'un HBox identique à l'ancienne approche pour afficher chaque réservation.
     */
    private HBox createReservationBox(ReservationHotel reservation) {
        HBox reservationBox = new HBox(10);
        reservationBox.setStyle("-fx-padding: 10; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #ffffff;");

        Hotel hotel = null;
        double prixNuit = 0;
        double prixAffiche;

        try {
            hotel = serviceHotel.trouverParId(reservation.getId_hotel());
            prixNuit = (hotel != null) ? hotel.getPrix_nuit() : 0;
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors de la récupération des détails de l'hôtel : " + e.getMessage());
            return new HBox();
        }

        // Price calculation (inline)
        switch (reservation.getType_chambre()) {
            case "Simple":
                prixAffiche = prixNuit;
                break;
            case "Double":
                prixAffiche = prixNuit * 1.5;
                break;
            case "Suite":
                prixAffiche = prixNuit * 2;
                break;
            default:
                prixAffiche = 0;
                break;
        }

        Label lblDetails = new Label(String.format(
                "Check-in: %s | Check-out: %s | Chambre: %s | Prix: %.2f DT",
                reservation.getCheck_in_date(),
                reservation.getCheck_out_date(),
                reservation.getType_chambre(),
                prixAffiche
        ));
        lblDetails.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnEdit = new Button("Modifier");
        btnEdit.setStyle("-fx-background-color: #f0ad4e; -fx-text-fill: white;");
        btnEdit.setMinWidth(100);
        btnEdit.setOnAction(e -> openEditReservation(reservation));

        Button btnDelete = new Button("Supprimer");
        btnDelete.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");
        btnDelete.setMinWidth(100);
        btnDelete.setOnAction(e -> removeReservation(reservation));

        // PDF Export Button
        Button btnExportPDF = new Button("Exporter en PDF");
        btnExportPDF.setStyle("-fx-background-color: #5cb85c; -fx-text-fill: white;");
        btnExportPDF.setMinWidth(100);
        btnExportPDF.setOnAction(e -> exportReservationToPDF(reservation));

        reservationBox.getChildren().addAll(lblDetails, spacer, btnEdit, btnDelete, btnExportPDF);

        return reservationBox;
    }
    private void exportReservationToPDF(ReservationHotel reservation) {
        try {
            String userHome = System.getProperty("user.home");
            String filePath = userHome + File.separator + "Documents" + File.separator + "reservation_" + reservation.getId_reservation() + ".pdf";

            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(filePath));
            Document doc = new Document(pdfDoc);
            addReservationDetailsToPDF(doc, reservation);
            doc.close();

            showAlert(Alert.AlertType.INFORMATION, "Réservation exportée en PDF avec succès !\nFichier sauvegardé : " + filePath);

            // Ouvrir le fichier PDF (fonctionne sur la plupart des systèmes)
            Desktop.getDesktop().open(new File(filePath));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors de l'exportation en PDF : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addReservationDetailsToPDF(Document doc, ReservationHotel reservation) {
        Hotel hotel;
        try {
            hotel = serviceHotel.trouverParId(reservation.getId_hotel());
            if (hotel == null) {
                doc.add(new Paragraph("Hôtel introuvable"));
                return;
            }
        } catch (SQLException e) {
            doc.add(new Paragraph("Erreur lors de la récupération de l'hôtel : " + e.getMessage()));
            return;
        }

        double prixNuit = hotel.getPrix_nuit();
        double prixAffiche = calculerPrix(reservation, prixNuit);

        // Create Table
        Table table = new Table(new float[]{2, 3}); // Adjust column widths as needed

        // Add header row
        table.addHeaderCell(new Cell().add(new Paragraph("Champ").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Valeur").setBold()));

        // Add data rows
        table.addCell(new Cell().add(new Paragraph("Hôtel")));
        table.addCell(new Cell().add(new Paragraph(hotel.getNom())));
        table.addCell(new Cell().add(new Paragraph("Check-in")));
        table.addCell(new Cell().add(new Paragraph(reservation.getCheck_in_date().toString())));
        table.addCell(new Cell().add(new Paragraph("Check-out")));
        table.addCell(new Cell().add(new Paragraph(reservation.getCheck_out_date().toString())));
        table.addCell(new Cell().add(new Paragraph("Type de Chambre")));
        table.addCell(new Cell().add(new Paragraph(reservation.getType_chambre())));
        table.addCell(new Cell().add(new Paragraph("Prix")));
        table.addCell(new Cell().add(new Paragraph(String.format("%.2f DT", prixAffiche))));

        doc.add(table);
    }


    private double calculerPrix(ReservationHotel reservation, double prixNuit) {
        switch (reservation.getType_chambre()) {
            case "Simple":
                return prixNuit;
            case "Double":
                return prixNuit * 1.5;
            case "Suite":
                return prixNuit * 2;
            default:
                return 0;
        }
    }

    private void openEditReservation(ReservationHotel reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modfier_reservationhotel.fxml"));
            Parent root = loader.load();

            ModifierReservationController controller = loader.getController();
            controller.loadReservationInfo(reservation);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Réservation");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Impossible d'ouvrir la fenêtre de modification.");
            e.printStackTrace();
        }
    }

    private void removeReservation(ReservationHotel reservation) {
        serviceReservationHotel.supprimerReservation(reservation.getId_reservation());
        showAlert(Alert.AlertType.INFORMATION, "Réservation supprimée avec succès.");
        loadReservations();
    }

    @FXML
    private void ajouterReservation() {
        if (hotelSelectionne == null) {
            showAlert(Alert.AlertType.ERROR, "Aucun hôtel sélectionné !");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouter_reservationhotel.fxml"));
            Parent root = loader.load();

            ajouter_reservationhotelController controller = loader.getController();
            controller.setId_hotel(hotelSelectionne.getId_hotel(), hotelSelectionne.getPrix_nuit());

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Nouvelle Réservation - " + hotelSelectionne.getNom());
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture de la page d'ajout.");
        }
    }

    @FXML
    private void retour() {
        Stage stage = (Stage) btnRetour.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void supprimerReservation(int idReservation) {
        serviceReservationHotel.supprimerReservation(idReservation);
        showAlert(Alert.AlertType.INFORMATION, "Réservation supprimée avec succès.");
        loadReservations(); // Rafraîchir la liste après suppression
    }
    private ReservationHotel findReservationFromHBox(HBox box) {
        for (ReservationHotel reservation : serviceReservationHotel.getReservationsParHotel(id_hotel)) {
            double prix = getPrixReservation(reservation); // Récupération du prix réel en fonction de l'hôtel et du type de chambre

            String reservationText = String.format(
                    "ID: %d | Check-in: %s | Check-out: %s | Chambre: %s | Prix: %.2f DT",
                    reservation.getId_reservation(),
                    reservation.getCheck_in_date(),
                    reservation.getCheck_out_date(),
                    reservation.getType_chambre(),
                    prix // Prix calculé dynamiquement
            );

            if (!box.getChildren().isEmpty() && box.getChildren().get(0) instanceof Label) {
                Label label = (Label) box.getChildren().get(0);
                if (label.getText().equals(reservationText)) {
                    return reservation;
                }
            }
        }
        return null; // Aucune réservation trouvée
    }
    private double getPrixReservation(ReservationHotel reservation) {
        double prixNuit = 0;
        try {
            Hotel hotel = serviceHotel.trouverParId(reservation.getId_hotel());
            if (hotel != null) {
                prixNuit = hotel.getPrix_nuit();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors de la récupération du prix de l'hôtel : " + e.getMessage());
        }

        switch (reservation.getType_chambre()) {
            case "Simple":
                return prixNuit;
            case "Double":
                return prixNuit * 1.5;
            case "Suite":
                return prixNuit * 2;
            default:
                return 0;
        }
    }



    @FXML
    private void handleSupprimerReservation() {
        HBox selectedBox = listViewReservations.getSelectionModel().getSelectedItem();
        if (selectedBox != null) {
            ReservationHotel reservation = findReservationFromHBox(selectedBox);
            if (reservation != null) {
                supprimerReservation(reservation.getId_reservation());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Aucune réservation sélectionnée.");
        }
    }

}