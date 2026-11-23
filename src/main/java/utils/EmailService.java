package utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {
    // Identifiants Gmail lus depuis config.properties (non versionné)
    private static final String EMAIL = Config.get("mail.user");
    private static final String PASSWORD = Config.get("mail.password"); // Mot de passe d'application Gmail

    public static void sendVerificationEmail(String recipientEmail, String verificationCode) {
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
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Vérification de votre compte");
            message.setText("Bonjour,\n\nVotre code de vérification est : " + verificationCode + "\n\nMerci.");

            Transport.send(message);
            System.out.println("E-mail de vérification envoyé à : " + recipientEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}