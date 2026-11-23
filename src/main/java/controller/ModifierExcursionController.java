package controller;

import java.sql.Date;
import java.time.LocalDate;
import java.sql.SQLException;
import java.util.List;

import entities.Excursion;
import entities.Guide;
import services.ExcursionService;
import services.GuideService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class ModifierExcursionController {

    @FXML
    private DatePicker datealler;
    @FXML
    private DatePicker dateretour;  // Ajout du champ Date de retour
    @FXML
    private TextField prix;
    @FXML
    private Button save;
    @FXML
    private TextField nom;
    @FXML
    private ComboBox<Guide> guide; // ComboBox typé pour éviter les erreurs
    @FXML
    private TextField lieu;
    @FXML
    private TextField desc;  // Ajout du champ Description

    private AfficherExcursionsController afficherExcursionsController;

    private ExcursionService excursionService = new ExcursionService();
    private GuideService guideService = new GuideService();

    private Excursion excursionToModify;
    @FXML
    private TextField lieudepart;


    public void setAfficherExcursionsController(AfficherExcursionsController controller) {
        this.afficherExcursionsController = controller;
    }

    public ModifierExcursionController() throws SQLException {
    }

    public void initialize() {
        try {
            // Charger tous les guides et les ajouter à la ComboBox
            List<Guide> guides = guideService.lire();

            // Personnaliser l'affichage dans le ComboBox (nom et prénom du guide)
            guide.setCellFactory(new Callback<ListView<Guide>, ListCell<Guide>>() {
                @Override
                public ListCell<Guide> call(ListView<Guide> param) {
                    return new ListCell<Guide>() {
                        @Override
                        protected void updateItem(Guide item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item != null) {
                                setText(item.getNom() + " " + item.getPrenom()); // Afficher le nom et prénom
                            } else {
                                setText(null);
                            }
                        }
                    };
                }
            });

            guide.setButtonCell(new ListCell<Guide>() {
                @Override
                protected void updateItem(Guide item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(item.getNom() + " " + item.getPrenom()); // Afficher dans le bouton
                    } else {
                        setText(null);
                    }
                }
            });

            // Utiliser un StringConverter pour afficher le nom et prénom dans le ComboBox
            guide.setConverter(new StringConverter<Guide>() {
                @Override
                public String toString(Guide guide) {
                    if (guide != null) {
                        return guide.getNom() + " " + guide.getPrenom(); // Afficher le nom et prénom
                    }
                    return "";
                }

                @Override
                public Guide fromString(String string) {
                    return null;  // Nous n'avons pas besoin de cette méthode dans ce cas
                }
            });

            guide.getItems().addAll(guides); // Ajouter les guides à la liste

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** 🌟 Méthode pour pré-remplir les champs lors de la modification **/
    public void setExcursionToModif(Excursion excursion) {
        this.excursionToModify = excursion;

        if (excursion != null) {
            nom.setText(excursion.getNom());
            lieudepart.setText(excursion.getVilleDepart().toString());
            lieu.setText(excursion.getLieu());
            prix.setText(String.valueOf(excursion.getPrix()));
            desc.setText(excursion.getDescription());  // Pré-remplir la description

            // Vérifier la date avant d'affecter
            if (excursion.getDateDepart() != null) {
                datealler.setValue(excursion.getDateDepart());
            }

            if (excursion.getDateRetour() != null) {
                dateretour.setValue(excursion.getDateRetour());  // Pré-remplir la date de retour
            }

            // Sélectionner automatiquement le guide de l'excursion
            if (excursion.getGuide() != null) {
                guide.setValue(excursion.getGuide());
            }
        }
    }

    @FXML
    public void modifierExcursion(MouseEvent event) {
        if (excursionToModify != null) {
            try {

                // Récupérer les valeurs du formulaire
                String nomValue = nom.getText();
                String lieuValue = lieu.getText();
                String villedepart = lieudepart.getText();
                double prixValue = Double.parseDouble(prix.getText());
                String descValue = desc.getText();

                LocalDate dateDepartValue = datealler.getValue();
                LocalDate dateRetourValue = dateretour.getValue();

                Guide selectedGuide = guide.getValue();

                // Vérification si tous les champs sont correctement remplis
                if (nomValue.isEmpty() || lieuValue.isEmpty() || descValue.isEmpty() || dateDepartValue == null || selectedGuide == null) {
                    System.out.println("⚠️ Tous les champs doivent être remplis !");
                    return;
                }

                // Vérification des dates
                LocalDate today = LocalDate.now();
                if (dateDepartValue.isBefore(today.plusDays(10))) {
                    System.out.println("⚠️ La date de départ doit être au moins 10 jours après aujourd'hui !");
                    return;
                }

                if (dateRetourValue != null && dateRetourValue.isBefore(dateDepartValue)) {
                    System.out.println("⚠️ La date de retour doit être égale ou postérieure à la date de départ !");
                    return;
                }

                // Mettre à jour l'excursion avec les nouvelles valeurs
                excursionToModify.setNom(nomValue);
                excursionToModify.setLieu(lieuValue);
                excursionToModify.setPrix(prixValue);
                excursionToModify.setDescription(descValue);
                excursionToModify.setDateDepart(Date.valueOf(dateDepartValue).toLocalDate());
                excursionToModify.setDateRetour(dateRetourValue != null ? Date.valueOf(dateRetourValue).toLocalDate() : null);
                excursionToModify.setGuide(selectedGuide);
                excursionToModify.setVilleDepart(villedepart);

                // Mise à jour de l'excursion dans la base de données
                excursionService.modifier(excursionToModify);

                System.out.println("✅ Excursion modifiée avec succès !");
                resetForm();
            } catch (NumberFormatException e) {
                System.out.println("❌ Prix invalide. Veuillez entrer un nombre.");
            } catch (Exception e) {
                System.out.println("❌ Une erreur s'est produite : " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ Aucune excursion sélectionnée.");
        }
    }


    /** 🌟 Méthode pour réinitialiser les champs après modification **/
    private void resetForm() {
        nom.clear();
        lieu.clear();
        prix.clear();
        datealler.setValue(null);
        dateretour.setValue(null);
        desc.clear();
        guide.setValue(null);
        ((Stage) nom.getScene().getWindow()).close();
        if (afficherExcursionsController != null) {
            afficherExcursionsController.refreshExcursions();
        }
    }
}
