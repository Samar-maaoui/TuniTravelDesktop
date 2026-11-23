package entities;
import java.sql.Date;

public class ReservationHotel {
    private int id_reservation;
    private int id_hotel;
    private Date check_in_date;
    private Date check_out_date;
    private String type_chambre;
    public ReservationHotel() {
    }
    public ReservationHotel(int id_reservation, int id_hotel, Date check_in_date, Date check_out_date, String type_chambre) {
        this.id_reservation = id_reservation;
        this.id_hotel = id_hotel;
        this.check_in_date = check_in_date;
        this.check_out_date = check_out_date;
        this.type_chambre = type_chambre;
    }
    public int getId_reservation() {
        return id_reservation;
    }
    public void setId_reservation(int id_reservation) {
        this.id_reservation = id_reservation;
    }
    public int getId_hotel() {
        return id_hotel;
    }
    public void setId_hotel(int id_hotel) {
        this.id_hotel = id_hotel;
    }
    public Date getCheck_in_date() {
        return check_in_date;
    }
    public void setCheck_in_date(Date check_in_date) {
        this.check_in_date = check_in_date;
    }
    public Date getCheck_out_date() {
        return check_out_date;
    }
    public void setCheck_out_date(Date check_out_date) {
        this.check_out_date = check_out_date;
    }
    public String getType_chambre() {
        return type_chambre;
    }
    public void setType_chambre(String type_chambre) {
        this.type_chambre = type_chambre;
    }
    @Override
    public String toString() {
        return "ReservationHotel{" +
                "id_reservation=" + id_reservation +
                ", id_hotel=" + id_hotel +
                ", check_in_date=" + check_in_date +
                ", check_out_date=" + check_out_date +
                ", type_chambre='" + type_chambre + '\'' +
                '}';
    }
}