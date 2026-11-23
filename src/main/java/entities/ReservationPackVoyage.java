package entities;

import java.sql.Timestamp;
import java.util.Date;

public class ReservationPackVoyage {

    private int idReservation;  // ID de la réservation
    private Utilisateur utilisateur;  // L'utilisateur qui a fait la réservation (objet Utilisateur)
    private Timestamp dateReservation;
    private StatutReservation statut;  // Statut de la réservation
    private StatutPaiement statutPaiement;  // Statut du paiement
    private PackVoyage packVoyage;  // Pack de voyage réservé (objet PackVoyage)
    private Post post;  // Post auquel la réservation est associée (objet Post)
    private int placesDemandees;  // Nombre de places demandées pour la réservation
    private double montantTotal;  // Montant total de la réservation
    private String reservationKey;  // Clé unique pour chaque réservation


    // Constructeur
    public ReservationPackVoyage(int idReservation, Utilisateur utilisateur, Date dateReservation, StatutReservation statut, StatutPaiement statutPaiement, PackVoyage packVoyage, Post post, int placesDemandees, double montantTotal) {
        this.idReservation = idReservation;
        this.utilisateur = utilisateur;
        this.dateReservation = (Timestamp) dateReservation;
        this.statut = statut;
        this.statutPaiement = statutPaiement;
        this.packVoyage = packVoyage;
        this.post = post;
        this.placesDemandees = placesDemandees;
        this.montantTotal = montantTotal;
    }



    public ReservationPackVoyage(int idReservation, int utilisateurId, Timestamp dateReservation,
                                 StatutReservation statutReservation, StatutPaiement statutPaiement,
                                 PackVoyage packVoyage, Object o, int placesDemandees, double montantTotal) {
        this.idReservation = idReservation; // ID de la réservation
        this.utilisateur = new Utilisateur(utilisateurId); // Créer un objet Utilisateur à partir de l'ID utilisateur
        this.dateReservation = dateReservation; // Date de la réservation
        this.statut = statutReservation; // Statut de la réservation (CONFIRMEE, ANNULEE, EN_ATTENTE)
        this.statutPaiement = statutPaiement; // Statut du paiement (PAYE, NON_PAYE)
        this.packVoyage = packVoyage; // Pack de voyage associé
        this.post = (Post) o; // Le Post associé (en castant l'objet o en Post)
        this.placesDemandees = placesDemandees; // Nombre de places demandées pour la réservation
        this.montantTotal = montantTotal; // Montant total de la réservation
    }

    public ReservationPackVoyage(int idReservation, int utilisateurId, Timestamp dateReservation,
                                 StatutReservation statutReservation, StatutPaiement statutPaiement,
                                 PackVoyage packVoyage, Post post, int placesDemandees,
                                 double montantTotal, String reservationKey) {
        this.idReservation = idReservation;
        this.utilisateur = new Utilisateur(utilisateurId); // Créer l'objet Utilisateur
        this.dateReservation = dateReservation;
        this.statut = statutReservation;
        this.statutPaiement = statutPaiement;
        this.packVoyage = packVoyage;
        this.post = post;
        this.placesDemandees = placesDemandees;
        this.montantTotal = montantTotal;
        this.reservationKey = reservationKey; // Ajouter la clé de réservation unique
    }



    // Getters and setters
    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Timestamp getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(Date dateReservation) {
        this.dateReservation = (Timestamp) dateReservation;
    }

    public StatutReservation getStatut() {
        return statut;
    }

    public void setStatut(StatutReservation statut) {
        this.statut = statut;
    }

    public StatutPaiement getStatutPaiement() {
        return statutPaiement;
    }

    public void setStatutPaiement(StatutPaiement statutPaiement) {
        this.statutPaiement = statutPaiement;
    }

    public PackVoyage getPackVoyage() {
        return packVoyage;
    }

    public void setPackVoyage(PackVoyage packVoyage) {
        this.packVoyage = packVoyage;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public int getPlacesDemandees() {
        return placesDemandees;
    }

    public void setPlacesDemandees(int placesDemandees) {
        this.placesDemandees = placesDemandees;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }

    // Calculer le montant total de la réservation en fonction des places demandées
    public double calculerMontant() {
        if (packVoyage != null) {
            return packVoyage.getPrixTotal() * placesDemandees;  // Calcul basé sur le prix du pack et les places demandées
        }
        return 0;
    }

    public String getReservationKey() {
        return reservationKey;
    }
}
