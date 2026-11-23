package controller;

import entities.TypeUtilisateur;
import entities.Utilisateur;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import netscape.javascript.JSObject;
import services.UtilisateurService;
import utils.EmailService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class AjoutUtilisateurController {
    private static final List<String> VIP_EMAILS = Arrays.asList(
            "@terredailleurs", "@banque-tunisie", "@steg", "@tunisietelecom",
            "@stb", "@tts", "@esprit.tn"
    );
    private final UtilisateurService utilisateurService = new UtilisateurService();
    private TypeUtilisateur typeUtilisateur = TypeUtilisateur.NORMAL;
    @FXML
    private TextField cinField, prenomField, telephoneField, adresseField, emailField, nomField, verificationCodeField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private DatePicker dateNaissancePicker;
    @FXML
    private Button verifierBtn;
    private int codeVerification;
    private String emailTemp;

    private boolean recaptchaVerified = false;
    @FXML
    private WebView recaptchaWebView;

    private static final String SITE_KEY = "6LcpzekqAAAAAHBAf1e6osJ0MvGfuqDOlmr5NpPX"; // Clé publique reCAPTCHA

    @FXML
    private Button creerCompteBtn;
    @FXML
    private CheckBox showRecaptchaCheckbox;
    @FXML
    private Button selectAddressBtn;


    @FXML
    public void handleCheckboxAction() {
        if (showRecaptchaCheckbox.isSelected()) {
            // Créer une nouvelle fenêtre
            Stage recaptchaStage = new Stage();

            // Créer un WebView et récupérer son moteur WebEngine
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();
            webEngine.setJavaScriptEnabled(true);

            // Charger l'URL du reCAPTCHA
            String url = "http://localhost/captcha/recaptcha.html";
            System.out.println("Chargement de l'URL reCAPTCHA : " + url);
            webEngine.load(url);

            // Ajouter un pont JavaScript-Java pour détecter la validation du reCAPTCHA
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaApp", this);
                }
            });

            // Créer un layout pour afficher le WebView
            VBox vbox = new VBox(webView);
            Scene scene = new Scene(vbox, 500, 500);
            recaptchaStage.setScene(scene);

            // Configurer la fenêtre
            recaptchaStage.setTitle("Vérification reCAPTCHA");
            recaptchaStage.initModality(Modality.APPLICATION_MODAL);

            // Stocker la référence de la fenêtre pour la fermer plus tard
            this.recaptchaStage = recaptchaStage;

            // Afficher la fenêtre
            recaptchaStage.showAndWait();
        }
    }

    // Référence de la fenêtre reCAPTCHA pour la fermer
    private Stage recaptchaStage;
    private Stage mapStage;

    // Méthode appelée depuis JavaScript après validation du reCAPTCHA
    public void onRecaptchaVerified() {
        System.out.println("reCAPTCHA validé !");
        recaptchaVerified = true;

        // Fermer la fenêtre du reCAPTCHA
        Platform.runLater(() -> {
            if (recaptchaStage != null) {
                recaptchaStage.close();
            }
        });

        if (recaptchaStage != null) {
            recaptchaStage.close(); // Fermer la fenêtre reCAPTCHA
            System.out.println("Fenêtre reCAPTCHA fermée.");
        }
        // Activer le bouton de création de compte
        creerCompteBtn.setDisable(false);
    }

    @FXML
    public void initialize() {
        creerCompteBtn.setDisable(true);  // Disable submit button until captcha is verified

        // Load the reCAPTCHA HTML widget
        WebEngine webEngine = recaptchaWebView.getEngine();

        // Make the WebView initially invisible
        recaptchaWebView.setVisible(false);

        // Check if the WebEngine is available
        if (webEngine != null) {
            String captchaHtml = "<html><body>" +
                    "<script src='https://www.google.com/recaptcha/api.js'></script>" +
                    "<div class='g-recaptcha' data-sitekey='" + SITE_KEY + "'></div>" +
                    "</body></html>";
            webEngine.loadContent(captchaHtml);
        } else {
            System.out.println("WebEngine not available or failed to load.");
        }

    }

    // Method to be called from JavaScript





    @FXML
    public void AjouterUtilisateur() {
        // ✅ Récupération des champs
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String cinText = cinField.getText().trim();
        String email = emailField.getText().trim();
        String motDePasse = passwordField.getText();
        String adresse = adresseField.getText().trim();
        String telephone = telephoneField.getText().trim();
        LocalDate dateNaissance = dateNaissancePicker.getValue();

        if (nom.isEmpty() || prenom.isEmpty() || cinText.isEmpty() || email.isEmpty() ||
                motDePasse.isEmpty() || adresse.isEmpty() || telephone.isEmpty() || dateNaissance == null) {
            afficherAlerte(Alert.AlertType.ERROR, "Champs requis", "Veuillez remplir tous les champs !");
            return;
        }
        if (!recaptchaVerified) {
            afficherAlerte(Alert.AlertType.ERROR, "Vérification requise",
                    "Veuillez compléter la vérification reCAPTCHA.");
            return;
        }
        // ✅ Vérification du CIN (8 chiffres)
        if (!cinText.matches("\\d{8}")) {
            afficherAlerte(Alert.AlertType.ERROR, "CIN invalide", "Le CIN doit contenir exactement 8 chiffres.");
            return;
        }
        int cin = Integer.parseInt(cinText);

        // ✅ Vérification du format d'email
        if (!Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", email)) {
            afficherAlerte(Alert.AlertType.ERROR, "Email invalide", "Veuillez entrer une adresse email valide.");
            return;
        }

        // ✅ Vérification du mot de passe (minimum 6 caractères)
        if (motDePasse.length() < 6) {
            afficherAlerte(Alert.AlertType.ERROR, "Mot de passe trop court", "Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        // ✅ Vérification du numéro de téléphone (8 chiffres)
        if (!telephone.matches("\\d{8}")) {
            afficherAlerte(Alert.AlertType.ERROR, "Numéro invalide", "Le numéro de téléphone doit contenir exactement 8 chiffres.");
            return;
        }

        // ✅ Vérification de l'âge (au moins 18 ans)
        if (Period.between(dateNaissance, LocalDate.now()).getYears() < 18) {
            afficherAlerte(Alert.AlertType.ERROR, "Âge insuffisant", "Vous devez avoir au moins 18 ans pour vous inscrire.");
            return;
        }

        // 🔹 Vérifier si l'email appartient à une entreprise VIP
        this.typeUtilisateur = TypeUtilisateur.NORMAL;
        for (String domain : VIP_EMAILS) {
            if (email.contains(domain)) {
                System.out.println("++++++++++++++++++++" + email.contains(domain));
                typeUtilisateur = TypeUtilisateur.VIP;
                break;
            }
        }

        // 🔹 Stocker l'email pour vérification
        emailTemp = email;
        // 🔹 Génération du code de vérification
        codeVerification = new Random().nextInt(900000) + 100000; // Code à 6 chiffres

        // 🔹 Envoi du mail de vérification
        EmailService.sendVerificationEmail(emailTemp, String.valueOf(codeVerification));

        // 🔹 Affichage du champ de vérification avec animation
        showVerificationFields();

    }





    @FXML
    public void verifierCompte(ActionEvent actionEvent) {
        String codeSaisi = verificationCodeField.getText().trim();

        if (codeSaisi.isEmpty()) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer le code de vérification.");
            return;
        }

        boolean codeValide;
        try {
            codeValide = Integer.parseInt(codeSaisi) == codeVerification;
        } catch (NumberFormatException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Le code doit être un nombre valide.");
            return;
        }

        // 🔹 Si le code est bon => is_verified = 1, sinon 0
        boolean isVerified = codeValide;
        // 🔹 Création de l'utilisateur
        System.out.println("ahhhhhhhhhh" + this.typeUtilisateur);
        Utilisateur u = new Utilisateur(nomField.getText(), prenomField.getText(),
                Integer.parseInt(cinField.getText()), emailTemp, passwordField.getText(),
                adresseField.getText(), telephoneField.getText(), dateNaissancePicker.getValue(),
                this.typeUtilisateur, isVerified);

        try {
            utilisateurService.ajouterUtilisateur(u);

            if (codeValide) {
                afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Utilisateur ajouté et vérifié avec succès !");
            } else {
                afficherAlerte(Alert.AlertType.WARNING, "Validation requise",
                        "Code incorrect ! Vous pouvez vous connecter, mais un administrateur devra valider votre compte.");
            }

            // 🔹 Redirection vers la page de connexion
            chargerInterface("/LOGIN.fxml");

        } catch (SQLException | IOException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue : " + e.getMessage());
        }
    }

    private void showVerificationFields() {
        verificationCodeField.setVisible(true);
        verifierBtn.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), verificationCodeField);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        FadeTransition fadeInBtn = new FadeTransition(Duration.seconds(1), verifierBtn);
        fadeInBtn.setFromValue(0);
        fadeInBtn.setToValue(1);
        fadeInBtn.play();
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void chargerInterface(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Connexion");
        stage.show();

        Stage currentStage = (Stage) nomField.getScene().getWindow();
        currentStage.close();
    }


    @FXML
    public void openMapSelector() {
        mapStage = new Stage();
        mapStage.initModality(Modality.APPLICATION_MODAL);

        WebView mapWebView = new WebView();
        WebEngine mapEngine = mapWebView.getEngine();
        mapEngine.setJavaScriptEnabled(true);

        String mapUrl = "http://localhost/mapbox/map.html";
        mapEngine.load(mapUrl);

        // ✅ Attendre que la page soit chargée avant d'exposer l'objet Java
        mapEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                Platform.runLater(() -> {
                    System.out.println("✅ Page chargée !");

                    // Exposer l'instance actuelle à JavaScript
                    JSObject window = (JSObject) mapEngine.executeScript("window");
                    window.setMember("java", this);

                    System.out.println("✅ Interface Java exposée à JavaScript !");
                });
            }
        });

        Scene scene = new Scene(mapWebView, 800, 600);
        mapStage.setScene(scene);
        mapStage.setTitle("Sélectionner une adresse");
        mapStage.show();
    }

    // ✅ Méthode appelée par JavaScript pour mettre à jour l'adresse
    public void updateAddress(String address) {
        Platform.runLater(() -> {
            adresseField.setText(address);
            System.out.println("✅ Adresse reçue : " + address);
            closeMapWindow();
        });
    }

    // ✅ Fermer la WebView
    public void closeMapWindow() {
        Platform.runLater(() -> {
            if (mapStage != null && mapStage.isShowing()) {
                System.out.println("✅ Fermeture de la WebView !");
                mapStage.close();
            }
        });
    }


    @FXML
    private void annulerInscription(ActionEvent event) {
        try {
            // Charger la page de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LOGIN.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Remplacer la scène actuelle
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

