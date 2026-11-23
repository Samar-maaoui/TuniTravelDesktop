package services;

import entities.Hotel;
import java.sql.SQLException;
import java.util.List;

public interface IService<T> {
    void ajouterhotel(T t) throws SQLException;
    void modifierhotel(T t) throws SQLException;
    void supprimerhotel(int id) throws SQLException;
    List<T> afficherhotel() throws SQLException;
    Hotel trouverParId(int id) throws SQLException;
    List<Hotel> lister() throws SQLException;
}
