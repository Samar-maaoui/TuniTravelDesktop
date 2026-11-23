package entities;

import java.util.Date;

public abstract class Reservation {
    protected int idReservation;
    protected int utilisateurId;
    protected Date dateReservation;
    protected StatutReservation statut;
    protected StatutPaiement statutPaiement;
    protected Double montantTotal; // ✅ Ajout de montantTotal mais optionnel

    // ✅ Constructeur SANS montantTotal (compatibilité avec l'ancien code)
    public Reservation(int idReservation, int utilisateurId, Date dateReservation, StatutReservation statut, StatutPaiement statutPaiement) {
        this.idReservation = idReservation;
        this.utilisateurId = utilisateurId;
        this.dateReservation = dateReservation;
        this.statut = statut;
        this.statutPaiement = statutPaiement;
        this.montantTotal = null; // Montant non obligatoire
    }

    // ✅ Constructeur AVEC montantTotal (nouvelle fonctionnalité)
    public Reservation(int idReservation, int utilisateurId, Date dateReservation, StatutReservation statut, StatutPaiement statutPaiement, double montantTotal) {
        this.idReservation = idReservation;
        this.utilisateurId = utilisateurId;
        this.dateReservation = dateReservation;
        this.statut = statut;
        this.statutPaiement = statutPaiement;
        this.montantTotal = montantTotal;
    }
    public Reservation(int idReservation, Date dateReservation, StatutReservation statut, StatutPaiement statutPaiement) {
        this.idReservation = idReservation;
        this.dateReservation = dateReservation;
        this.statut = statut;
        this.statutPaiement = statutPaiement;
    }
    public Reservation( Date dateReservation, StatutPaiement statutPaiement) {
        this.dateReservation = dateReservation;
        this.statutPaiement = statutPaiement;
    }


    // ✅ Constructeur AVEC un Utilisateur au lieu de l’ID
    public Reservation(int idReservation, Utilisateur utilisateur, Date dateReservation, StatutReservation statut, StatutPaiement statutPaiement, double montantTotal) {
        this.idReservation = idReservation;
        this.utilisateurId = utilisateur.getIdUtilisateur();
        this.dateReservation = dateReservation;
        this.statut = statut;
        this.statutPaiement = statutPaiement;
        this.montantTotal = montantTotal;
    }

    // ✅ Constructeur SANS montantTotal pour Utilisateur
    public Reservation(int idReservation, Utilisateur utilisateur, Date dateReservation, StatutReservation statut, StatutPaiement statutPaiement) {
        this.idReservation = idReservation;
        this.utilisateurId = utilisateur.getIdUtilisateur();
        this.dateReservation = dateReservation;
        this.statut = statut;
        this.statutPaiement = statutPaiement;
        this.montantTotal = null;
    }

    public Reservation(int idUtilisateur, Date dateReservation, StatutReservation statutReservation, StatutPaiement statutPaiement, double montantTotal) {
        this.utilisateurId = idUtilisateur;
        this.dateReservation = dateReservation;
        this.statut = statutReservation;
        this.statutPaiement = statutPaiement;
        this.montantTotal = montantTotal;
    }


    // ✅ Getter et Setter pour montantTotal
    public Double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(Double montantTotal) {
        this.montantTotal = montantTotal;
    }

    // Getters et Setters existants (inchangés)
    public int getIdReservation() {
        return idReservation;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public Date getDateReservation() {
        return dateReservation;
    }

    public StatutReservation getStatut() {
        return statut;
    }

    public StatutPaiement getStatutPaiement() {
        return statutPaiement;
    }

    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public void setDateReservation(Date dateReservation) {
        this.dateReservation = dateReservation;
    }

    public void setStatut(StatutReservation statut) {
        this.statut = statut;
    }

    public void setStatutPaiement(StatutPaiement statutPaiement) {
        this.statutPaiement = statutPaiement;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "idReservation=" + idReservation +
                ", utilisateurId=" + utilisateurId +
                ", dateReservation=" + dateReservation +
                ", statut=" + statut +
                ", statutPaiement=" + statutPaiement +
                ", montantTotal=" + (montantTotal != null ? montantTotal : "Non spécifié") +
                '}';
    }

    // Méthodes abstraites
    public abstract void afficherDetails();
    public abstract double calculerMontant();
    public abstract void annulerReservation();
}
