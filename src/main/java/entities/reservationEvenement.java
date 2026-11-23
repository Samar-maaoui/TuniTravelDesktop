package entities;

import java.time.LocalDateTime;

public class reservationEvenement {
    private int idReservation; // Reservation ID
    private int idEvenement;   // Event ID
    private int idUtilisateur; // User ID
    private int nbPlaces;      // Number of places reserved
   // Payment status
    private LocalDateTime dateInscription; // Date of reservation

    // Updated constructor with dateInscription
    public reservationEvenement(int idReservation, int idEvenement, int idUtilisateur, int nbPlaces, LocalDateTime dateInscription) {
        this.idReservation = idReservation;
        this.idEvenement = idEvenement;
        this.idUtilisateur = idUtilisateur;
        this.nbPlaces = nbPlaces;

        this.dateInscription = dateInscription;
    }

    // Getters and Setters
    public int getIdReservation() { return idReservation; }
    public void setIdReservation(int idReservation) { this.idReservation = idReservation; }

    public int getIdEvenement() { return idEvenement; }
    public void setIdEvenement(int idEvenement) { this.idEvenement = idEvenement; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }



    public int getNbPlaces() { return nbPlaces; }
    public void setNbPlaces(int nbPlaces) { this.nbPlaces = nbPlaces; }


    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }


}
