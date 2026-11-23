package entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Feedback {
    private int idFeedback;
    private String commentaire;
    private int note;
    private LocalDateTime dateFeedback;
    private Utilisateur utilisateur;



    public Feedback(Utilisateur utilisateur, int note, String commentaire, int feedback_code) {
        this.utilisateur = utilisateur;
        this.note = note;
        this.commentaire = commentaire;
    }

    public Feedback(int idFeedback, String commentaire, int note, LocalDateTime dateFeedback, Utilisateur utilisateur) {
        this.idFeedback = idFeedback;
        this.commentaire = commentaire;
        this.note = note;
        this.dateFeedback = dateFeedback;
        this.utilisateur = utilisateur;
    }

    public Feedback(Utilisateur utilisateur, int note, String commentaire) {
        this.utilisateur = utilisateur;
        this.note = note;
        this.commentaire = commentaire;
    }

    public Feedback(String commentaire, int note, int feedback_code) {
        this.commentaire = commentaire;
        this.note = note;
    }

    public Feedback(String commentaire, int note, LocalDateTime dateFeedback, Utilisateur utilisateur) {
        this.commentaire = commentaire;
        this.note = note;
        this.dateFeedback = dateFeedback;
        this.utilisateur = utilisateur;
    }

    public int getIdFeedback() {
        return idFeedback;
    }

    public void setIdFeedback(int idFeedback) {
        this.idFeedback = idFeedback;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public LocalDateTime getDateFeedback() {
        return dateFeedback;
    }

    public void setDateFeedback(LocalDateTime dateFeedback) {
        this.dateFeedback = dateFeedback;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    @Override
    public String toString() {
        return "📌 Feedback :\n" +
                "📝 Commentaire : " + commentaire + "\n" +
                "⭐ Note : " + note + "/5\n" +
                "📅 Date : " + dateFeedback + "\n" +
                "👤 Utilisateur : " + (utilisateur != null ? utilisateur.getNom() + " " + utilisateur.getPrenom() : "N/A");
    }

}
