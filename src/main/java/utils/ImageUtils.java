package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ImageUtils {
    private static final String IMAGE_DIR = "src/main/resources/images/";
    private static final String EXCURSION_JAVAFX_DIR = "C:/Users/abidi/Desktop/aymen/PIDEV/src/main/resources/images/Excursions/";
    private static final String EXCURSION_SYMFONY_DIR = "C:/Users/abidi/Desktop/PIWEB/PIWEB/public/uploads/excursions/";

    // ✅ Méthode déjà existante — on ne la touche pas
    public static String enregistrerImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return "default.jpg"; // Image par défaut si aucune image fournie
        }

        if (imagePath.startsWith("file:/")) {
            imagePath = imagePath.substring(5);  // Supprimer "file:/"
        }

        File imageFile = new File(imagePath);

        if (!imageFile.exists()) {
            return "default.jpg";
        }

        String fileName = imageFile.getName();
        File destination = new File(IMAGE_DIR + fileName);

        try {
            Files.copy(imageFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ Image copiée avec succès : " + destination.getAbsolutePath());
            return fileName;
        } catch (IOException e) {
            System.out.println("❌ Erreur lors de la copie de l'image : " + e.getMessage());
            return "default.jpg";
        }
    }

    // ✅ Nouvelle méthode : copie vers JavaFX + Symfony
    public static void copierVersSymfonyEtJavaFX(File sourceImage, String nomFichier) {
        File destinationSymfony = new File(EXCURSION_SYMFONY_DIR + nomFichier);
        File destinationJavaFX = new File(EXCURSION_JAVAFX_DIR + nomFichier);

        try {
            Files.copy(sourceImage.toPath(), destinationSymfony.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(sourceImage.toPath(), destinationJavaFX.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ Copie réussie vers Symfony & JavaFX : " + nomFichier);
        } catch (IOException e) {
            System.out.println("❌ Erreur de copie : " + e.getMessage());
        }
    }

    // ✅ Nouvelle méthode : synchronisation depuis Symfony → JavaFX
    public static void synchroniserDepuisSymfony() {
        File dossierSymfony = new File(EXCURSION_SYMFONY_DIR);
        File dossierJavaFX = new File(EXCURSION_JAVAFX_DIR);

        if (!dossierSymfony.exists()) {
            System.out.println("❌ Dossier Symfony introuvable.");
            return;
        }

        for (File fichier : dossierSymfony.listFiles()) {
            if (fichier.isFile() && fichier.getName().matches(".*\\.(jpg|jpeg|png)")) {
                try {
                    Files.copy(fichier.toPath(),
                            new File(dossierJavaFX, fichier.getName()).toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("✔ Copié depuis Symfony : " + fichier.getName());
                } catch (IOException e) {
                    System.out.println("❌ Erreur lors de la synchro : " + fichier.getName() + " - " + e.getMessage());
                }
            }
        }
    }
    public static void synchroniserDepuisSymfonyHotel() {
        File source = new File("C:/Users/abidi/Desktop/PIWEB/PIWEB/public/images/");
        File destination = new File("C:/Users/abidi/Desktop/aymen/PIDEV/src/main/resources/images/");

        if (!source.exists()) {
            System.out.println("⚠️ Dossier Symfony des hôtels introuvable.");
            return;
        }

        for (File fichier : source.listFiles()) {
            if (fichier.isFile() && fichier.getName().matches(".*\\.(jpg|jpeg|png)")) {
                try {
                    Files.copy(fichier.toPath(),
                            new File(destination, fichier.getName()).toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("✔ Image hôtel synchronisée : " + fichier.getName());
                } catch (IOException e) {
                    System.out.println("❌ Erreur de copie pour l'image hôtel " + fichier.getName());
                }
            }
        }
    }
    public static void synchroniserDepuisSymfonyGuide() {
        File source = new File("C:/Users/abidi/Desktop/PIWEB/PIWEB/public/uploads/guides/");
        File destination = new File("C:/Users/abidi/Desktop/aymen/PIDEV/src/main/resources/images/");


        if (!source.exists()) {
            System.out.println("⚠️ Dossier Symfony des guides introuvable.");
            return;
        }

        for (File fichier : source.listFiles()) {
            if (fichier.isFile() && fichier.getName().toLowerCase().matches(".*\\.(jpg|jpeg|png)")) {
                try {
                    Files.copy(fichier.toPath(),
                            new File(destination, fichier.getName()).toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("✔ Image guide synchronisée : " + fichier.getName());
                } catch (IOException e) {
                    System.out.println("❌ Erreur de copie pour l'image guide " + fichier.getName() + " : " + e.getMessage());
                }
            }
        }
    }

    public static void copierImageGuide(File imageFile) {
        if (imageFile == null || !imageFile.exists()) return;

        try {
            String fileName = imageFile.getName();

            File destJavaFX = new File("src/main/resources/images/" + fileName);
            Files.copy(imageFile.toPath(), destJavaFX.toPath(), StandardCopyOption.REPLACE_EXISTING);

            File destSymfony = new File("C:/Users/abidi/Desktop/PIWEB/PIWEB/public/uploads/guides/" + fileName);
            Files.copy(imageFile.toPath(), destSymfony.toPath(), StandardCopyOption.REPLACE_EXISTING);

            System.out.println("✔ Guide copié localement et dans Symfony : " + fileName);
        } catch (IOException e) {
            System.err.println("❌ Erreur copie image guide : " + e.getMessage());
        }
    }

}
