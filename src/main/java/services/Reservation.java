package services;

import java.util.ArrayList;
import java.util.List;

public class Reservation implements IReservation<Reservation> {
    private List<Reservation> reservations = new ArrayList<>();

    @Override
    public void ajouterReservation(Reservation reservation) {
        reservations.add(reservation);
        System.out.println("Réservation ajoutée avec succès !");
    }

    @Override
    public List<Reservation> listerReservations() {
        return reservations;
    }
}
