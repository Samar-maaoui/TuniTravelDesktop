package controller;

import entities.TypeUtilisateur;
import entities.Utilisateur;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

public class UserListCell extends ListCell<Utilisateur> {

    private final MenuAdminController controller;

    public UserListCell(MenuAdminController controller) {
        this.controller = controller;
    }

    @Override
    protected void updateItem(Utilisateur utilisateur, boolean empty) {
        super.updateItem(utilisateur, empty);

        if (empty || utilisateur == null) {
            setText(null);
            setGraphic(null);
            setStyle("-fx-background-color: transparent;");
        } else {
            // Main container
            HBox container = new HBox();
            container.setSpacing(15);
            container.setPadding(new Insets(10));
            container.getStyleClass().add("user-list-cell");
            container.setAlignment(Pos.CENTER_LEFT);

            // Avatar circle with initials
            VBox avatarCircle = new VBox();
            avatarCircle.getStyleClass().add("user-avatar-circle");
            avatarCircle.setAlignment(Pos.CENTER);

            String initials = getInitials(utilisateur.getNom(), utilisateur.getPrenom());
            Label avatarText = new Label(initials);
            avatarText.getStyleClass().add("avatar-text");
            avatarCircle.getChildren().add(avatarText);

            // User details container
            VBox userDetails = new VBox();
            userDetails.setSpacing(5);
            userDetails.getStyleClass().add("user-details-container");

            // User name
            Label userName = new Label(utilisateur.getNom() + " " + utilisateur.getPrenom());
            userName.getStyleClass().add("user-name");

            // User email
            Label userEmail = new Label(utilisateur.getEmail());
            userEmail.getStyleClass().add("user-email");

            userDetails.getChildren().addAll(userName, userEmail);

            // Metadata container
            HBox metadata = new HBox();
            metadata.setSpacing(15);
            metadata.getStyleClass().add("user-metadata");
            metadata.setAlignment(Pos.CENTER_LEFT);

            // CIN with label
            HBox cinContainer = createMetadataItem("M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z",
                    "CIN: " + utilisateur.getCin());

            // Phone with label
            HBox phoneContainer = createMetadataItem("M2.25 6.75c0 8.284 6.716 15 15 15h2.25a2.25 2.25 0 002.25-2.25v-1.372c0-.516-.351-.966-.852-1.091l-4.423-1.106c-.44-.11-.902.055-1.173.417l-.97 1.293c-.282.376-.769.542-1.21.38a12.035 12.035 0 01-7.143-7.143c-.162-.441.004-.928.38-1.21l1.293-.97c.363-.271.527-.734.417-1.173L6.963 3.102a1.125 1.125 0 00-1.091-.852H4.5A2.25 2.25 0 002.25 4.5v2.25z",
                    "Tél: " + utilisateur.getTelephone());

            // Spacer
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // User type badge
            Label typeBadge = new Label(utilisateur.getTypeUtilisateur().toString());
            typeBadge.getStyleClass().addAll("user-type-badge", getBadgeStyleClass(utilisateur.getTypeUtilisateur()));

            // Action buttons container
            HBox actionButtons = new HBox(10);
            actionButtons.setAlignment(Pos.CENTER_RIGHT);

            // Modify button
            Button modifyButton = new Button("Modifier");
            modifyButton.getStyleClass().add("btn-action-edit");
            modifyButton.setOnAction(event -> controller.ouvrirFenetreModification(utilisateur));

            // Delete button
            Button deleteButton = new Button("Supprimer");
            deleteButton.getStyleClass().add("btn-action-delete");
            deleteButton.setOnAction(event -> controller.confirmerSuppression(utilisateur));

            actionButtons.getChildren().addAll(modifyButton, deleteButton);

            metadata.getChildren().addAll(cinContainer, phoneContainer, spacer, actionButtons, typeBadge);

            // Add all components to the main container
            container.getChildren().addAll(avatarCircle, userDetails);

            // Create a VBox to hold the main container and metadata
            VBox fullContainer = new VBox();
            fullContainer.setSpacing(10);
            fullContainer.getChildren().addAll(container, metadata);

            setGraphic(fullContainer);
            setText(null);
        }
    }

    private String getInitials(String nom, String prenom) {
        StringBuilder initials = new StringBuilder();
        if (nom != null && !nom.isEmpty()) {
            initials.append(nom.charAt(0));
        }
        if (prenom != null && !prenom.isEmpty()) {
            initials.append(prenom.charAt(0));
        }
        return initials.toString().toUpperCase();
    }

    private HBox createMetadataItem(String svgPath, String text) {
        HBox container = new HBox();
        container.setSpacing(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("metadata-item");

        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);
        icon.getStyleClass().add("metadata-icon");
        icon.setScaleX(0.03);
        icon.setScaleY(0.03);

        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("metadata-text");

        container.getChildren().addAll(icon, textLabel);
        return container;
    }

    private String getBadgeStyleClass(TypeUtilisateur type) {
        switch (type) {
            case ADMIN:
                return "Admin";
            case VIP:
                return "VIP";
            default:
                return "Normal";
        }
    }
}