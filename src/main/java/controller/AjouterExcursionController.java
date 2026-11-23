package controller;

import entities.Excursion;
import entities.Guide;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import services.ExcursionService;
import services.GuideService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;

public class AjouterExcursionController {
    private final ExcursionService excursionService = new ExcursionService();
    private AfficherExcursionsController afficherExcursionsController;
    private final GuideService guideService = new GuideService();

    @FXML
    private TextField prix;
    @FXML
    private TextField nom;
    @FXML
    private ComboBox<Guide> guide;
    @FXML
    private DatePicker datealler;
    @FXML
    private Button ajouterButton;
    @FXML
    private DatePicker dateretour;
    @FXML
    private ImageView imageView;
    @FXML
    private Button btnimage;
    @FXML
    private TextField desc;
    private String imageUrl = "";
    @FXML
    private TextField NbMax;
    @FXML
    private ChoiceBox VilleDepart;
    @FXML
    private ChoiceBox lieu;

    public AjouterExcursionController() throws SQLException {}

    public void setAfficherExcursionsController(AfficherExcursionsController controller) {
        this.afficherExcursionsController = controller;
    }

    @FXML
    public void initialize() {
        chargerGuides();
        ajouterButton.setOnAction(e -> ajouterExcursion());
        btnimage.setOnAction(e -> handleChoisirImage());
        VilleDepart.getItems().addAll(
                "Aucun", "Ariana", "Ain Drahem" , "Béja", "Ben Arous", "Bizerte", "Gabès", "Gafsa",
                "Jendouba", "Kairouan", "Kasserine", "Kébili", "Kef", "Mahdia", "Manouba",
                "Médenine", "Monastir", "Nabeul", "Sfax", "Sidi Bouzid", "Siliana", "Sousse",
                "Tataouine", "Tozeur", "Tunis", "Zaghouan"
        );

        lieu.getItems().addAll(
                "Aucun", "Ariana", "Ain Drahem" ,"Béja", "Ben Arous", "Bizerte", "Gabès", "Gafsa",
                "Jendouba", "Kairouan", "Kasserine", "Kébili", "Kef", "Mahdia", "Manouba",
                "Médenine", "Monastir", "Nabeul", "Sfax", "Sidi Bouzid", "Siliana", "Sousse",
                "Tataouine", "Tozeur", "Tunis", "Zaghouan"
        );
    }

    @FXML
    private void handleChoisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(btnimage.getScene().getWindow());

        if (file != null) {
            try {
                // Nom de fichier structuré : NomExcursion_Date.png
                String extension = file.getName().substring(file.getName().lastIndexOf("."));
                String baseName = nom.getText().trim().replaceAll("\\s+", "-") + "_" + datealler.getValue();
                String fileName = baseName + extension;

                // 🔁 Copie vers Symfony + JavaFX (double destination)
                utils.ImageUtils.copierVersSymfonyEtJavaFX(file, fileName);

                // Mise à jour du champ image
                imageUrl = fileName;

                // Affichage dans l'ImageView
                File javafxLocalPath = new File("src/main/resources/images/Excursions/" + fileName);
                if (javafxLocalPath.exists()) {
                    imageView.setImage(new Image(javafxLocalPath.toURI().toString()));
                } else {
                    showAlert("Erreur", "L'image a été copiée, mais n'est pas visible localement.");
                }

            } catch (Exception e) {
                showAlert("Erreur", "Impossible de copier l'image sélectionnée.");
                e.printStackTrace();
            }
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void ajouterExcursion() {
        String nomExcursion = nom.getText().trim();
        String lieuExcursion = lieu.getValue().toString();
        Guide selectedGuide = guide.getValue();
        LocalDate dateDepart = datealler.getValue();
        LocalDate dateRetour = dateretour.getValue();
        int nbMaxParticipants = Integer.parseInt(NbMax.getText().trim());
        String description = desc.getText().trim();
        String villeDepart = VilleDepart.getValue().toString();

        if (nomExcursion.isEmpty() || lieuExcursion.isEmpty() || selectedGuide == null || dateDepart == null || imageUrl.isEmpty()) {
            afficherAlerte("Erreur", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        // Vérifier que la ville d'arrivée est différente de la ville de départ
        if (villeDepart.equals(lieuExcursion)) {
            afficherAlerte("Erreur", "La ville de départ ne peut pas être la même que la ville d'arrivée.");
            return;
        }

        // Vérifier que la date de départ est au moins 10 jours après la date actuelle
        if (dateDepart.isBefore(LocalDate.now().plusDays(10))) {
            afficherAlerte("Erreur", "La date de départ doit être au moins 10 jours après aujourd'hui.");
            return;
        }

        // Vérifier que la date de départ est inférieure ou égale à la date de retour
        if (dateDepart.isAfter(dateRetour)) {
            afficherAlerte("Erreur", "La date de départ doit être inférieure ou égale à la date de retour.");
            return;
        }

        double prixExcursion;
        try {
            prixExcursion = Double.parseDouble(prix.getText().trim());
            if (prixExcursion < 0) {
                afficherAlerte("Erreur", "Le prix ne peut pas être négatif.");
                return;
            }
        } catch (NumberFormatException e) {
            afficherAlerte("Erreur", "Veuillez entrer un prix valide.");
            return;
        }

        Excursion nouvelleExcursion = new Excursion(
                nomExcursion,
                lieuExcursion,
                prixExcursion,
                selectedGuide,
                dateDepart,
                dateRetour,
                description,
                nbMaxParticipants,
                imageUrl,
                villeDepart // Ajouter la ville d'arrivée à la nouvelle excursion
        );
        System.out.println(nouvelleExcursion);
        excursionService.ajouter(nouvelleExcursion);

        if (afficherExcursionsController != null) {
            afficherExcursionsController.refreshExcursions();
        }

        fermerFenetre();
    }

    private void chargerGuides() {
        try {
            guide.getItems().clear();
            for (Guide g : guideService.getAllGuides()) {
                guide.getItems().add(g);
            }

            guide.setCellFactory(lv -> new ListCell<Guide>() {
                @Override
                protected void updateItem(Guide item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNom() + " " + item.getPrenom());
                    }
                }
            });

            guide.setButtonCell(new ListCell<Guide>() {
                @Override
                protected void updateItem(Guide item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNom() + " " + item.getPrenom());
                    }
                }
            });
        } catch (SQLException e) {
            afficherAlerte("Erreur", "Impossible de charger les guides.");
            e.printStackTrace();
        }
    }

    private void fermerFenetre() {
        Stage stage = (Stage) ajouterButton.getScene().getWindow();
        stage.close();
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}