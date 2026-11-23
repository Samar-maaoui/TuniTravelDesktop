package controller;

import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import entities.Feedback;
import java.time.format.DateTimeFormatter;

public class FeedbackListCell2 extends ListCell<Feedback> {

    private HBox hbox;
    private VBox contentBox;
    private Text commentaireText;
    private Text utilisateurText;
    private Text dateText;
    private HBox ratingBox; // Conteneur pour la note (étoiles)
    private ImageView avatarView;
    private StackPane avatarContainer;
    private Button btnModifier;
    private Button btnSupprimer;
    private HBox buttonsBox;

    @Override
    protected void updateItem(Feedback feedback, boolean empty) {
        super.updateItem(feedback, empty);

        if (empty || feedback == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        // Conteneur principal
        hbox = new HBox();
        hbox.setSpacing(12);
        hbox.setPadding(new Insets(10));
        hbox.setStyle("-fx-background-color: #E8F0FE; -fx-background-radius: 15;");
        hbox.setAlignment(Pos.TOP_LEFT);
        hbox.setMaxWidth(500);

        // Avatar utilisateur (Image ou Initiales)
        avatarContainer = new StackPane();
        avatarContainer.setPrefSize(40, 40);

        Circle backgroundCircle = new Circle(20, Color.web("#A7C7E7")); // Cercle bleu clair
        backgroundCircle.setStroke(Color.web("#4B6584"));
        backgroundCircle.setStrokeWidth(1.5);

        if (feedback.getUtilisateur().getAvatarUrl() != null && !feedback.getUtilisateur().getAvatarUrl().isEmpty()) {
            avatarView = new ImageView(new Image(feedback.getUtilisateur().getAvatarUrl(), 40, 40, true, true));
            avatarView.setClip(new Circle(20, 20, 20));
            avatarContainer.getChildren().addAll(backgroundCircle, avatarView);
        } else {
            Text initiales = new Text(getInitials(feedback.getUtilisateur().getNom(), feedback.getUtilisateur().getPrenom()));
            initiales.setFill(Color.WHITE);
            initiales.setFont(Font.font("Montserrat", 16));

            avatarContainer.getChildren().addAll(backgroundCircle, initiales);
        }

        // Conteneur du commentaire
        contentBox = new VBox();
        contentBox.setSpacing(4);
        contentBox.setPadding(new Insets(8, 12, 8, 12));
        contentBox.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12;");
        contentBox.setMaxWidth(400);

        // Texte du commentaire
        commentaireText = new Text(feedback.getCommentaire());
        commentaireText.setFont(Font.font("Montserrat", 14));
        commentaireText.setFill(Color.BLACK);
        commentaireText.setWrappingWidth(380);

        // Nom de l'utilisateur
        utilisateurText = new Text(feedback.getUtilisateur().getNom() + " " + feedback.getUtilisateur().getPrenom()+ " Cin: " +feedback.getUtilisateur().getCin());
        utilisateurText.setFont(Font.font("Montserrat", 13));
        utilisateurText.setFill(Color.web("#4B6584"));

        // Date
        dateText = new Text(feedback.getDateFeedback().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        dateText.setFont(Font.font("Montserrat", 11));
        dateText.setFill(Color.GRAY);

        // Affichage de la note sous forme d'étoiles
        ratingBox = new HBox();
        ratingBox.setSpacing(3);

        int note = feedback.getNote(); // Supposons que getNote() retourne un entier entre 1 et 5
        for (int i = 0; i < 5; i++) {
            Text star = new Text(i < note ? "★" : "☆");
            star.setFont(Font.font("Montserrat", 14));
            star.setFill(i < note ? Color.GOLD : Color.LIGHTGRAY);
            ratingBox.getChildren().add(star);
        }

        // Création des boutons Modifier et Supprimer


        btnSupprimer = new Button("Supprimer");
        btnSupprimer.getStyleClass().add("btn-danger");
        btnSupprimer.setPrefWidth(100);
        btnSupprimer.setPrefHeight(30);



        btnSupprimer.setOnAction(event -> {
            if (getListView().getScene() != null) {
                FeedbackAdminController controller = (FeedbackAdminController) getListView().getScene().getUserData();
                if (controller != null) {
                    controller.supprimerFeedback(event);
                }
            }
        });

        // Conteneur pour les boutons
        buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        buttonsBox.getChildren().addAll( btnSupprimer);
        buttonsBox.setPadding(new Insets(5, 0, 0, 0));

        // Ajout des éléments dans le conteneur
        contentBox.getChildren().addAll(utilisateurText, ratingBox, commentaireText, dateText, buttonsBox);
        hbox.getChildren().addAll(avatarContainer, contentBox);

        setGraphic(hbox);

        // Effets de survol et de sélection
        hbox.setOnMouseEntered(e -> hbox.setStyle("-fx-background-color: #D6E6FF; -fx-background-radius: 15;"));
        hbox.setOnMouseExited(e -> hbox.setStyle("-fx-background-color: #E8F0FE; -fx-background-radius: 15;"));

        this.listViewProperty().addListener((obs, oldListView, newListView) -> {
            if (newListView != null) {
                newListView.getSelectionModel().selectedItemProperty().addListener((obs2, oldItem, newItem) -> {
                    if (newItem == getItem()) {
                        hbox.setStyle("-fx-background-color: #A7C7E7; -fx-background-radius: 15;");
                    } else {
                        hbox.setStyle("-fx-background-color: #E8F0FE; -fx-background-radius: 15;");
                    }
                });
            }
        });
    }

    // Fonction pour récupérer les initiales du nom et prénom
    private String getInitials(String nom, String prenom) {
        if (nom == null || prenom == null || nom.isEmpty() || prenom.isEmpty()) {
            return "??"; // Cas où le nom ou le prénom ne sont pas disponibles
        }
        return (nom.substring(0, 1) + prenom.substring(0, 1)).toUpperCase();
    }
}