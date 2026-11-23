package services;

import entities.Feedback;
import entities.Utilisateur;

import java.sql.SQLException;
import java.util.List;

public interface IFeedback<T> {
    void ajouterFeedback(T f)throws SQLException;
    public void modifierFeedback(T f) throws SQLException;
    public void supprimerFeedback(T f )throws SQLException;
    List<Feedback> trouverFeedbackPar(int cin)throws SQLException;
}
