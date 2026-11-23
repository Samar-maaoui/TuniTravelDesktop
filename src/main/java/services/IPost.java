package services;

import entities.Post;

import java.util.List;
import java.util.Map;

public interface IPost<T> {
    void ajouterPost(T post);
    public List<Post> listerPosts();
    T trouverParId(int id);
    void modifierPost(T post);
    void supprimerPost(T post);

    // ✅ Vérifier les posts liés aux packs dont les places sont épuisées
    void verifierStatutPosts();
}
