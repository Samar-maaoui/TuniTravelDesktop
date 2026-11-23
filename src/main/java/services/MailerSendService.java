package services;

import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.MailerSendResponse;
import com.mailersend.sdk.emails.Email;
import com.mailersend.sdk.exceptions.MailerSendException;

public class MailerSendService {

    // Remplacez cette clé par la vôtre
    private static final String API_KEY = "mlsn.43c5674fd10a7c3038068341440628e2be185fd480080073b553f3da6b3039e5";

    public static void sendEmail(String recipientEmail, String subject, String body) {
        // Crée une instance de l'email
        Email email = new Email();

        // Remplacez par votre adresse email et le nom de l'expéditeur
        email.setFrom("Aymen Abidi", "info@trial-pr9084z0xyelw63d.mlsender.net");  // Nom et email de l'expéditeur
        email.addRecipient("name", recipientEmail);  // Le destinataire

        email.setSubject(subject);  // L'objet du mail
        email.setPlain(body);  // Corps du mail en texte brut
        email.setHtml(body);  // Corps du mail en HTML

        // Crée une instance MailerSend et envoie l'email
        MailerSend mailerSend = new MailerSend();
        mailerSend.setToken(API_KEY);  // Clé API de MailerSend

        try {
            // Envoi de l'email
            MailerSendResponse response = mailerSend.emails().send(email);
            System.out.println("Email envoyé avec succès. ID du message : " + response.messageId);
        } catch (MailerSendException e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }
}
