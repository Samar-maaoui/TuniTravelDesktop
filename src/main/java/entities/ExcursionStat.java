package entities;

import java.sql.Timestamp;

public class ExcursionStat {

    private int id;
    private String name;  // Nouvelle colonne 'name'
    private int excursionId;  // Référence à l'ID de l'excursion
    private int reservationsCount;  // Nombre de réservations confirmées
    private int totalReservedPlaces;  // Nombre total de places réservées
    private Timestamp dateCreated;  // Date de création des statistiques

    // Constructeur avec le nouveau champ 'name'
    public ExcursionStat(String name, int excursionId, int reservationsCount, int totalReservedPlaces, Timestamp dateCreated) {
        this.name = name;
        this.excursionId = excursionId;
        this.reservationsCount = reservationsCount;
        this.totalReservedPlaces = totalReservedPlaces;
        this.dateCreated = dateCreated;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getExcursionId() {
        return excursionId;
    }

    public void setExcursionId(int excursionId) {
        this.excursionId = excursionId;
    }

    public int getReservationsCount() {
        return reservationsCount;
    }

    public void setReservationsCount(int reservationsCount) {
        this.reservationsCount = reservationsCount;
    }

    public int getTotalReservedPlaces() {
        return totalReservedPlaces;
    }

    public void setTotalReservedPlaces(int totalReservedPlaces) {
        this.totalReservedPlaces = totalReservedPlaces;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public String toString() {
        return "ExcursionStat{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", excursionId=" + excursionId +
                ", reservationsCount=" + reservationsCount +
                ", totalReservedPlaces=" + totalReservedPlaces +
                ", dateCreated=" + dateCreated +
                '}';
    }


}
