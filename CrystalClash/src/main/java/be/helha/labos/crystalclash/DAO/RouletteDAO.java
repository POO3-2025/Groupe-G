package be.helha.labos.crystalclash.DAO;

//Va nous servire a savoir quand le joueur a joué a la roulette pour la derniere fois
import java.time.LocalDate;

/**
 * Interface permettant de gérer les données liées à la roulette du jeu,
 * notamment la date à laquelle un utilisateur a joué pour la dernière fois.
 */
public interface RouletteDAO {

    /**
     * Met à jour la dernière date à laquelle l'utilisateur a joué à la roulette.
     *
     * @param username le nom d'utilisateur
     * @param date la date à enregistrer comme dernière utilisation
     */
    void UpdateLastPlayDate(String username, LocalDate date);

    /**
     * Récupère la dernière date à laquelle l'utilisateur a joué à la roulette.
     *
     * @param username le nom d'utilisateur
     * @return la date du dernier jeu de roulette de l'utilisateur
     */
    LocalDate getLastPlayDate(String username);
}