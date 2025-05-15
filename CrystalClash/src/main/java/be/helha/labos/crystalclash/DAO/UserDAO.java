package be.helha.labos.crystalclash.DAO;

import be.helha.labos.crystalclash.User.UserInfo;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Interface définissant les opérations d'accès aux données liées à l'utilisateur,
 * notamment la récupération de ses informations, la mise à jour de ses statistiques,
 * de son niveau, de sa connexion, et de ses trophées.
 */
public interface UserDAO {

    /**
     * Récupère les informations d'un utilisateur à partir de son nom d'utilisateur.
     *
     * @param username le nom d'utilisateur
     * @return un objet contenant les données de l'utilisateur si elles existent
     */
    Optional<UserInfo> getUserByUsername(String username);

    /**
     * Met à jour le nombre de cristaux d’un utilisateur.
     *
     * @param username le nom d'utilisateur
     * @param newCristaux la nouvelle valeur des cristaux
     * @throws Exception en cas de problème d'accès aux données
     */
    void updateCristaux(String username, int newCristaux) throws Exception;

    /**
     * Vérifie si un utilisateur est déjà connecté.
     *
     * @param username le nom d'utilisateur
     * @return {@code true} si l'utilisateur est connecté, {@code false} sinon
     * @throws Exception en cas d'erreur d'accès aux données
     */
    boolean isAlreadyConnected(String username) throws Exception;

    /**
     * Met à jour l'état de connexion de l'utilisateur.
     *
     * @param username le nom d'utilisateur
     * @param isConnected {@code true} si connecté, {@code false} sinon
     * @throws Exception en cas d'erreur d'accès aux données
     */
    void updateIsConnected(String username, boolean isConnected) throws Exception;

    /**
     * Met à jour le niveau de l'utilisateur.
     *
     * @param username le nom d'utilisateur
     * @param newLevel le nouveau niveau
     * @throws Exception en cas d'erreur lors de la mise à jour
     */
    void updateLevel(String username, int newLevel) throws Exception;

    /**
     * Incrémente le nombre de victoires de l'utilisateur.
     *
     * @param username le nom d'utilisateur
     * @throws Exception en cas de problème d'accès aux données
     */
    void IncrementWinner(String username) throws Exception;

    /**
     * Incrémente le nombre de défaites de l'utilisateur.
     *
     * @param username le nom d'utilisateur
     * @throws Exception en cas de problème d'accès aux données
     */
    void IncrementDefeat(String username) throws Exception;

    /**
     * Met à jour les statistiques de victoires/défaites de l'utilisateur.
     *
     * @param username le nom d'utilisateur
     * @param New la nouvelle valeur (par exemple "win" ou "lose")
     * @throws Exception en cas de problème d'accès aux données
     */
    void updateWin_Lose(String username, String New) throws Exception;

    /**
     * Incrémente le compteur de victoires consécutives pour les trophées.
     *
     * @param username le nom d'utilisateur
     * @throws Exception en cas de problème d'accès aux données
     */
    void incrementWinconsecutive(String username) throws Exception;

    /**
     * Réinitialise le compteur de victoires consécutives de l'utilisateur.
     *
     * @param username le nom d'utilisateur
     * @throws Exception en cas de problème d'accès aux données
     */
    void resetWinconsecutiveConsecutive(String username) throws Exception;
}