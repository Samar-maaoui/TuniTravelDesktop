package entities;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.WriterException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class ReservationExcursion extends Reservation {
    private Excursion excursion;
    private int nbrPlaceReservee;
    private Guide guide;

    // ✅ Constructeur avec montantTotal
    public ReservationExcursion(Utilisateur utilisateur, Date dateReservation, String statut, double montantTotal, Excursion excursion, int nbrPlaceReservee) {
        super(utilisateur.getIdUtilisateur(), dateReservation, StatutReservation.valueOf(statut.toUpperCase()), StatutPaiement.NON_PAYE, montantTotal);
        this.excursion = excursion;
        this.nbrPlaceReservee = nbrPlaceReservee;
        this.guide = excursion.getGuide();
    }

    // ✅ Constructeur sans montantTotal
    public ReservationExcursion(Utilisateur utilisateur, Date dateReservation, String statut, Excursion excursion, int nbrPlaceReservee) {
        super(utilisateur.getIdUtilisateur(), dateReservation, StatutReservation.valueOf(statut.toUpperCase()), StatutPaiement.NON_PAYE);
        this.excursion = excursion;
        this.nbrPlaceReservee = nbrPlaceReservee;
        this.guide = excursion.getGuide();
    }

    // ✅ Constructeur avec idReservation pour récupérer une réservation
    public ReservationExcursion(int idReservation, Excursion excursion, String statut, int nbrPlaceReservee, Double montantTotal) {
        super(idReservation, new Date(), StatutReservation.valueOf(statut.toUpperCase()), StatutPaiement.NON_PAYE);
        this.excursion = excursion;
        this.nbrPlaceReservee = nbrPlaceReservee;
        this.montantTotal = montantTotal;
    }

    // Getters et Setters
    public Excursion getExcursion() {
        return excursion;
    }

    public void setExcursion(Excursion excursion) {
        this.excursion = excursion;
    }

    public int getNbrPlaceReservee() {
        return nbrPlaceReservee;
    }

    public void setNbrPlaceReservee(int nbrPlaceReservee) {
        this.nbrPlaceReservee = nbrPlaceReservee;
    }

    public Guide getGuide() {
        return guide;
    }

    public void setGuide(Guide guide) {
        this.guide = guide;
    }

    public int getExcursionId() {
        return excursion.getIdExcursion();
    }

    public int getGuideId() {
        return guide.getIdGuide();
    }

    // Méthode pour générer un QR Code pour la réservation
    public void generateQRCodeForReservation(String reservationId) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 1); // marge pour le QR code

            // Générer un QR code pour l'ID de réservation
            var bitMatrix = qrCodeWriter.encode(reservationId, BarcodeFormat.QR_CODE, 200, 200, hints);

            // Code pour convertir le bitMatrix en image et l'afficher ou sauvegarder l'image ici...
            System.out.println("QR Code généré pour la réservation ID : " + reservationId);
        } catch (WriterException e) {
            System.err.println("Erreur lors de la génération du QR code : " + e.getMessage());
        }
    }

    // Implémentation des méthodes abstraites
    @Override
    public void afficherDetails() {
        System.out.println("Réservation Excursion : ID = " + idReservation +
                ", Excursion = " + excursion.getNom() +
                ", Guide = " + (guide != null ? guide.getNom() : "Aucun") +
                ", Places réservées = " + nbrPlaceReservee +
                ", Date = " + dateReservation +
                ", Statut = " + statut +
                ", Paiement = " + statutPaiement +
                ", Montant Total = " + (montantTotal != null ? montantTotal : "Non spécifié"));

        // Générer QR code pour cette réservation
        generateQRCodeForReservation(String.valueOf(idReservation)); // Utiliser l'ID de la réservation
    }

    @Override
    public double calculerMontant() {
        return excursion.getPrix() * nbrPlaceReservee;
    }

    @Override
    public void annulerReservation() {
        this.statut = StatutReservation.ANNULEE;
        System.out.println("La réservation d'excursion ID " + idReservation + " a été annulée.");
    }

}
