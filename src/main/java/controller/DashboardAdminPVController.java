package controller;

import entities.PackVoyage;
import entities.Post;
import entities.Utilisateur;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import services.AdminPVService;
import utils.SessionManager;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import java.sql.SQLException;
import java.util.List;

public class DashboardAdminPVController {

    @FXML
    private Label labelBienvenue;

    @FXML
    private ListView<String> listTopPacks;

    @FXML
    private ListView<String> listPostsActifs;

    @FXML
    private Label labelTotalPacks;
    @FXML
    private PieChart pieChartPlaces;

    @FXML
    private BarChart<String, Number> barChartReservations;
    private final AdminPVService adminService = new AdminPVService();

    @FXML
    public void initialize() {
        Utilisateur utilisateur = SessionManager.getInstance().getUtilisateur();
        labelBienvenue.setText("Bienvenue, " + utilisateur.getNom());

        try {
            // Afficher top 3 packs
            List<PackVoyage> topPacks = adminService.getTopPacksParReservations();
            for (PackVoyage pack : topPacks) {
                listTopPacks.getItems().add(
                        pack.getNomPack() + " - " + pack.getNombrePlacesRestants() + " places restantes"
                );
            }

            // Afficher posts actifs
            List<Post> posts = adminService.listerPostsActifs();
            for (Post post : posts) {
                listPostsActifs.getItems().add(
                        post.getTitre() + " - expire le " + post.getDateExpiration()
                );
            }

            // Nombre total de packs
            labelTotalPacks.setText("Total de packs enregistrés : " + adminService.filtrerPacksSelonUtilisateur(utilisateur).size());

        } catch (SQLException e) {
            e.printStackTrace();
        }

        Platform.runLater(() -> {
            Stage stage = (Stage) labelBienvenue.getScene().getWindow();
            stage.setFullScreen(true);
            stage.setIconified(false); // empêcher la minimisation manuelle (dépend de la config OS)
        });
        // Afficher les graphiques
        afficherGraphiquePlacesRestantes();
        afficherTop3Reservations();

    }
    private void afficherGraphiquePlacesRestantes() {
        try {
            List<PackVoyage> packs = adminService.filtrerPacksSelonUtilisateur(SessionManager.getInstance().getUtilisateur());
            pieChartPlaces.getData().clear();

            for (PackVoyage pack : packs) {
                PieChart.Data slice = new PieChart.Data(pack.getNomPack(), pack.getNombrePlacesRestants());
                pieChartPlaces.getData().add(slice);
            }

            pieChartPlaces.setLegendVisible(true);
            pieChartPlaces.setLabelsVisible(true);
            pieChartPlaces.setTitle("Places restantes par Pack");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void afficherTop3Reservations() {
        try {
            List<PackVoyage> top3 = adminService.getTopPacksParReservations();

            barChartReservations.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Top Réservations");

            for (PackVoyage pack : top3) {
                int reservations = adminService.getNombreReservationsPack(pack.getId());
                series.getData().add(new XYChart.Data<>(pack.getNomPack(), reservations));
            }

            barChartReservations.getData().add(series);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleFermerDashboard() {
        Stage stage = (Stage) labelBienvenue.getScene().getWindow();
        stage.close();
    }

}
