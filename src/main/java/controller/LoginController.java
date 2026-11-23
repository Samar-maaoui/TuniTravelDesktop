package controller;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import entities.TypeUtilisateur;
import entities.Utilisateur;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.json.JSONObject;
import services.UtilisateurService;
import utils.Config;
import utils.EmailService;
import utils.SessionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class LoginController {

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private int codeVerification;
    private String emailTemp;

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button googleLoginBtn;

    /**
     * 🔹 Vérifie les informations de connexion et redirige l'utilisateur.
     */
    @FXML
    public void handleLogin(ActionEvent event) throws SQLException {
        String email = emailField.getText().trim();
        String motDePasse = passwordField.getText().trim();

        // Vérification des champs vides
        if (email.isEmpty() || motDePasse.isEmpty()) {
            afficherMessageErreur("Veuillez remplir tous les champs.");
            return;
        }

        // Vérification du format de l'email
        if (!isValidEmail(email)) {
            afficherMessageErreur("Veuillez entrer une adresse email valide.");
            return;
        }

        int result = utilisateurService.loginUtilisateur(email, motDePasse);

        switch (result) {
            case 1 -> chargerInterface(event, "/InterfaceAdmin.fxml"); // ✅ Redirection Admin
            case 2 -> chargerInterface(event, "/InterfaceClient.fxml"); // ✅ Redirection Client
            case -2 -> afficherMessageErreur("Votre compte a été supprimé car il n'a pas été vérifié sous 24h.");
            case -3 -> envoyerCodeVerification(email);
            default -> afficherMessageErreur("Email ou mot de passe incorrect !");
        }
    }

    /**
     * 🔹 Envoie un code de vérification à l'utilisateur.
     */
    private void envoyerCodeVerification(String email) {
        emailTemp = email;
        codeVerification = new Random().nextInt(900000) + 100000; // Code à 6 chiffres
        EmailService.sendVerificationEmail(emailTemp, String.valueOf(codeVerification));
        afficherFenetreVerification();
    }

    /**
     * 🔹 Ouvre une boîte de dialogue pour la vérification du compte.
     */
    private void afficherFenetreVerification() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Réinitialisation du mot de passe");
        dialog.setHeaderText("Un code de vérification a été envoyé à votre email.\nVeuillez entrer le code et votre nouveau mot de passe.");

        ButtonType validerButtonType = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(validerButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField codeField = new TextField();
        codeField.setPromptText("Code de vérification");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Nouveau mot de passe");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirmez le mot de passe");

        grid.add(new Label("Code :"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Nouveau mot de passe :"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Confirmer le mot de passe :"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Node validerButton = dialog.getDialogPane().lookupButton(validerButtonType);
        validerButton.setDisable(true);

        codeField.textProperty().addListener((observable, oldValue, newValue) -> {
            validerButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == validerButtonType) {
                return new Pair<>(codeField.getText(), passwordField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(pair -> verifierMotDePasse(pair.getKey(), pair.getValue(), confirmPasswordField.getText()));
    }
    private void verifierMotDePasse(String codeSaisi, String nouveauMotDePasse, String confirmationMotDePasse) {
        try {
            // Vérification du code de vérification
            if (codeSaisi.trim().isEmpty()) {
                afficherMessageErreur("Veuillez entrer le code de vérification.");
                return;
            }

            if (!codeSaisi.equals(String.valueOf(codeVerification))) {
                afficherMessageErreur("Code incorrect. Veuillez réessayer.");
                return;
            }

            // Vérification de la longueur du mot de passe
            if (nouveauMotDePasse.length() < 6) {
                afficherMessageErreur("Le mot de passe doit contenir au moins 6 caractères.");
                return;
            }

            // Vérification de la correspondance des mots de passe
            if (!nouveauMotDePasse.equals(confirmationMotDePasse)) {
                afficherMessageErreur("Les mots de passe ne correspondent pas.");
                return;
            }

            // Mise à jour du mot de passe
            utilisateurService.mettreAJourMotDePasse(emailTemp, nouveauMotDePasse);
            afficherMessageSucces("Mot de passe réinitialisé avec succès !");

            // Tentative de connexion avec le nouveau mot de passe

        } catch (SQLException e) {
            afficherMessageErreur("Une erreur est survenue lors de la mise à jour du mot de passe.");
            e.printStackTrace();
        }
    }





    /**
     * 🔹 Vérifie le code entré par l'utilisateur.
     */
    private void verifierCompte(String codeSaisi) {
        if (codeSaisi.trim().isEmpty()) {
            afficherMessageErreur("Veuillez entrer le code de vérification.");
            return;
        }

        try {
            int codeEntre = Integer.parseInt(codeSaisi);
            if (codeEntre == codeVerification) {
                utilisateurService.validerCompte(emailTemp);
                afficherMessageSucces("Compte vérifié avec succès !");
            } else {
                afficherMessageErreur("Code incorrect. Veuillez réessayer.");
            }
        } catch (NumberFormatException e) {
            afficherMessageErreur("Le code doit être un nombre valide.");
        }
    }

    /**
     * 🔹 Vérifie si l'email est valide.
     */
    private boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return Pattern.matches(regex, email);
    }

    /**
     * 🔹 Charge une interface FXML.
     */
    private void chargerInterface(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Bienvenue");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            afficherMessageErreur("Erreur de chargement de l'interface !");
        }
    }

    /**
     * 🔹 Charge une interface via un événement de la souris.
     */
    private void chargerInterfaceMouse(MouseEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Bienvenue");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            afficherMessageErreur("Erreur de chargement de l'interface !");
        }
    }

    /**
     * 🔹 Redirige vers l'inscription.
     */

    @FXML
    public void GoTologin(MouseEvent event) {
        chargerInterfaceMouse(event, "/sign_in.fxml");
    }
    /**
     * 🔹 Redirige vers la connexion.
     */


    /**
     * 🔹 Affiche une alerte en cas d'erreur.
     */
    private void afficherMessageErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 🔹 Affiche une alerte en cas de succès.
     */
    private void afficherMessageSucces(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    public void handleForgotPassword(MouseEvent mouseEvent) {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            afficherMessageErreur("Veuillez entrer votre adresse email.");
            return;
        }

        if (!isValidEmail(email)) {
            afficherMessageErreur("Adresse email invalide.");
            return;
        }

        try {
            // 🔍 Recherche du numéro de téléphone dans la base de données
            String numeroTelephone = utilisateurService.recupererNumeroParEmail(email);

            if (numeroTelephone == null) {
                afficherMessageErreur("Aucun compte trouvé avec cet email.");
                return;
            }

            // 📌 Génération d'un code de vérification à 6 chiffres
            codeVerification = new Random().nextInt(900000) + 100000;
            emailTemp = email;

            // 📲 Envoi du code via WhatsApp
            envoyerCodeWhatsApp(numeroTelephone, codeVerification);

            // ⏳ Afficher la fenêtre de saisie du code
            afficherFenetreVerification();

        } catch (SQLException e) {
            afficherMessageErreur("Erreur lors de la récupération du numéro.");
            e.printStackTrace();
        }
    }
    /**
     * 🔹 Envoie un code de vérification via WhatsApp.
     */
    private void envoyerCodeWhatsApp(String numero, int code) {
        // 🚀 Ici, tu peux utiliser Twilio ou l'API WhatsApp Cloud
        System.out.println("📩 Envoi du code " + code + " à " + numero);

        // Identifiants Twilio lus depuis config.properties (non versionné)
        Twilio.init(Config.get("twilio.sid"), Config.get("twilio.token"));
        Message message = Message.creator(
                new PhoneNumber("whatsapp:+21656791908"), // Ton numéro WhatsApp
                new PhoneNumber("whatsapp:+14155238886"), // Numéro Twilio Sandbox
                "Votre code de réinitialisation pour ITINIREVES est: " + code+". Ce code est valable pendant 1 heure."
        ).create();

    }

    // Identifiants Google OAuth lus depuis config.properties (non versionné)
    private static final String CLIENT_ID = Config.get("google.client.id");
    private static final String CLIENT_SECRET = Config.get("google.client.secret");
    private static final String REDIRECT_URI = "http://localhost";

    @FXML
    private void connexionGoogle(ActionEvent event) {
        String authUrl = "https://accounts.google.com/o/oauth2/auth?"
                + "scope=email%20profile"
                + "&response_type=code"
                + "&client_id=" + CLIENT_ID
                + "&redirect_uri=" + REDIRECT_URI;

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        Stage stage = new Stage();
        stage.setScene(new Scene(webView, 600, 700));
        stage.setTitle("Connexion Google");

        webEngine.load(authUrl);

        webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.startsWith(REDIRECT_URI)) {
                stage.close();
                String code = extraireCode(newValue);
                if (code != null) {
                    String accessToken = echangerCodeContreToken(code);
                    if (accessToken != null) {
                        String email=obtenirInfosGoogle(accessToken);
                        Utilisateur u =utilisateurService.trouverUtilisateurParEmail(email);
                        if(u.getTypeUtilisateur()== TypeUtilisateur.ADMIN) {
                            chargerInterface(event, "/InterfaceAdmin.fxml");
                        }
                        else if(u.getTypeUtilisateur()== TypeUtilisateur.NORMAL)
                        {
                            chargerInterface(event, "/InterfaceClient.fxml");
                            SessionManager.setUtilisateur(u);                        }
                        else if(u.getTypeUtilisateur()== TypeUtilisateur.VIP)
                        {
                            chargerInterface(event, "/InterfaceClient.fxml");
                            SessionManager.setUtilisateur(u);
                        }
                    }

                } else {
                    afficherMessageErreur("Échec de l'authentification.");
                }
            }
        });

        stage.show();
    }

    /**
     * 🔹 Récupère le mot de passe haché d'un utilisateur à partir de son email.
     */
    private String recupererMotDePasseHashe(String email) {
        try {
            // Vérifier si l'email est valide
            if (!isValidEmail(email)) {
                afficherMessageErreur("Veuillez entrer une adresse email valide.");
                return null;
            }

            // Récupérer le mot de passe haché depuis la base de données
            String motDePasseHashe = utilisateurService.recupererMotDePasseParEmail(email);

            if (motDePasseHashe == null) {
                afficherMessageErreur("Aucun utilisateur trouvé avec cet email.");
                return null;
            }

            return motDePasseHashe;

        } catch (SQLException e) {
            afficherMessageErreur("Erreur lors de la récupération du mot de passe.");
            e.printStackTrace();
            return null;
        }
    }

    private String extraireCode(String url) {
        Pattern pattern = Pattern.compile("code=([^&]+)");
        Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String echangerCodeContreToken(String code) {
        try {
            String urlParameters = "code=" + code
                    + "&client_id=" + CLIENT_ID
                    + "&client_secret=" + CLIENT_SECRET
                    + "&redirect_uri=" + REDIRECT_URI
                    + "&grant_type=authorization_code";

            URL url = new URL("https://oauth2.googleapis.com/token");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            OutputStream os = conn.getOutputStream();
            os.write(urlParameters.getBytes());
            os.flush();
            os.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = in.lines().collect(Collectors.joining());
            in.close();

            JSONObject json = new JSONObject(response);
            return json.getString("access_token");
        } catch (IOException e) {
            afficherMessageErreur("Impossible d'échanger le code contre un token.");
            e.printStackTrace();
            return null;
        }
    }

    private String obtenirInfosGoogle(String accessToken) {
        String url = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.lines().collect(Collectors.joining());
            reader.close();

            JSONObject json = new JSONObject(response);
            String email = json.getString("email");
            String name = json.optString("name", "Inconnu");
            String picture = json.optString("picture", "");

            afficherMessageSucces("Connecté avec : " + email + "\nNom : " + name);
            System.out.println("Photo de profil : " + picture);

            return email;  // ✅ Retourne l'email

        } catch (IOException e) {
            afficherMessageErreur("Impossible d'obtenir les informations utilisateur.");
            e.printStackTrace();
            return null;  // ✅ Retourne null en cas d'erreur
        }
    }


    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}