package utils;


import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.File;
import java.util.Properties;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;


public class NotificationEmailService {
    // Identifiants Gmail lus depuis config.properties (non versionné)
    private static final String EMAIL = Config.get("mail.user");
    private static final String PASSWORD = Config.get("mail.password");

    public static void sendNotificationEmailWithAttachment(String recipientEmail, String subject, String messageText, String attachmentPath, String attachmentCid) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL, PASSWORD);
            }
        });

        try {
            // Création du message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);

            // Vérification du fichier de pièce jointe
            File file = new File(attachmentPath);
            if (!file.exists()) {
                System.err.println("Erreur : Le fichier joint n'existe pas à l'emplacement : " + attachmentPath);
                return;
            }

            // Partie du message HTML
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(messageText, "text/html; charset=utf-8");

            // Ajout du logo en pièce jointe
            MimeBodyPart imagePart = new MimeBodyPart();
            DataSource fds = new FileDataSource(attachmentPath);
            DataHandler dataHandler = new DataHandler(fds);
            imagePart.setDataHandler(dataHandler);


            imagePart.setHeader("Content-ID", "<" + attachmentCid + ">");
            imagePart.setDisposition(MimeBodyPart.INLINE); // Image affichée directement dans l'e-mail

            // Création de la structure de l'e-mail
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(imagePart);

            message.setContent(multipart);

            // Envoi de l'e-mail
            Transport.send(message);
            System.out.println("E-mail de notification envoyé avec succès à : " + recipientEmail);

        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'e-mail : " + e.getMessage());
            e.printStackTrace();
        }
    }
}