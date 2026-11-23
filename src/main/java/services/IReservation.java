package services;

import entities.Reservation;

import java.util.List;

public interface IReservation <U>{
    void ajouterReservation(U reservation);
    List<U> listerReservations();
}
