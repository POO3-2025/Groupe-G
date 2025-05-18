package be.helha.labos.crystalclash.Service;

import be.helha.labos.crystalclash.DAO.UserDAO;
import be.helha.labos.crystalclash.User.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserDAO userDAO;


    /**
     * Constructeur avec injection du DAO des utilisateurs.
     *
     * @param userDAO DAO pour accéder aux données des utilisateurs.
     */
    @Autowired
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;

    }






    /**
     * Récupère les informations d'un utilisateur à partir de son nom d'utilisateur.
     *
     * @param username Le nom d'utilisateur.
     * @return
     */
    public Optional<UserInfo> getUserInfo(String username) {
        return userDAO.getUserByUsername(username);
    }


    /**
     * Met à jour le nombre de cristaux d'un utilisateur.
     *
     * @param username Le nom d'utilisateur.
     * @param newCristaux Le nouveau nombre de cristaux.
     * @throws Exception En cas d'erreur lors de la mise à jour.
     */
    public void updateCristaux(String username, int newCristaux)throws Exception  {
        userDAO.updateCristaux(username, newCristaux);
    }

    /**
     * Vérifie si un utilisateur est déjà connecté.
     *
     * @param username Le nom d'utilisateur.
     * @return `true` si l'utilisateur est connecté, `false` sinon.
     * @throws Exception En cas d'erreur lors de la vérification.
     */
     public boolean isAlreadyConnected(String username) throws Exception{
        return userDAO.isAlreadyConnected(username);
     }

    /**
     * Met à jour l'état de connexion d'un utilisateur.
     *
     * @param username Le nom d'utilisateur.
     * @param isConnected `true` si l'utilisateur est connecté, `false` sinon.
     * @throws Exception En cas d'erreur lors de la mise à jour.
     */
     public void updateIsConnected(String username, boolean isConnected)throws Exception{
        userDAO.updateIsConnected(username, isConnected);
     }
    /**
     * Récompense un utilisateur gagnant en augmentant ses cristaux et son niveau.
     *
     * @param username Le nom d'utilisateur.
     * @param cristauxBonus Le bonus de cristaux à ajouter.
     * @param levelUp Le nombre de niveaux à ajouter.
     */
    public void rewardWinner(String username, int cristauxBonus, int levelUp) {
        try {
            Optional<UserInfo> optional = userDAO.getUserByUsername(username);
            if (optional.isEmpty()) return;

            UserInfo user = optional.get();

            int newCristaux = user.getCristaux() + cristauxBonus;
            int newLevel = user.getLevel() + levelUp;

            userDAO.updateCristaux(username, newCristaux);
            userDAO.updateLevel(username, newLevel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Incrémente le nombre de victoires d'un utilisateur.
     *
     * @param username Le nom d'utilisateur.
     * @throws Exception En cas d'erreur lors de l'incrémentation.
     */
    public void IncrementWinner(String username) throws Exception {
        userDAO.IncrementWinner(username);

    }

    /**
     * Incrémente le nombre de défaites d'un utilisateur.
     *
     * @param username Le nom d'utilisateur.
     * @throws Exception En cas d'erreur lors de l'incrémentation.
     */
    public void incrementDefeat(String username) throws Exception {
        userDAO.IncrementDefeat(username);

    }

    //Trophé
    /**
     * Incrémente le nombre de victoires consécutives d'un utilisateur.
     *
     * @param username Le nom d'utilisateur.
     * @throws Exception En cas d'erreur lors de l'incrémentation.
     */
    public void incrementWimConsecutive(String username) throws Exception {
        userDAO.incrementWinconsecutive(username);
    }
    /**
     * Réinitialise le nombre de victoires consécutives d'un utilisateur.
     *
     * @param username Le nom d'utilisateur.
     * @throws Exception En cas d'erreur lors de la réinitialisation.
     */
    public void resetWinConsecutives(String username) throws Exception {
        userDAO.resetWinconsecutiveConsecutive(username);
    }


}
