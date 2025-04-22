package be.helha.labos.crystalclash.DAO;

//Va nous servire a savoir quand le joueur a joué a la roulette pour la derniere fois
import java.time.LocalDate;

public interface RouletteDAO {

    /**
     * @param username
     * @param date
     *Mise a jour de la date du user avec la date
     * */
    void UpdateLastPlayDate(String username, LocalDate date);

    /**
     * @param username
     * Derniere date du joueur joué
     * */
    LocalDate getLastPlayDate(String username);

}
