package controller;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class CarteExcursionController {

    @FXML
    private Label prixExcursion;
    @FXML
    private Label nomExcursion;
    @FXML
    private ImageView imageExcursion;
    @FXML
    private VBox infoContainer;
    @FXML
    private Label lieuExcursion;

    public void setNomExcursion(String nom) {
        this.nomExcursion.setText(nom);
    }

    public void setLieuExcursion(String lieu) {
        this.lieuExcursion.setText(lieu);
    }

    public void setPrixExcursion(String prix) {
        this.prixExcursion.setText(prix);
    }

    public void setImageExcursion(Image image) {
        this.imageExcursion.setImage(image);
    }
}
