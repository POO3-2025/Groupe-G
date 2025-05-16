package be.helha.labos.crystalclash.Service;

import be.helha.labos.crystalclash.DAO.UserCombatStatDAO;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserCombatStatService {

    private final UserCombatStatDAO userCombatStatDAO;

    @Autowired
    public UserCombatStatService(UserCombatStatDAO userCombatStatDAO) {
        this.userCombatStatDAO = userCombatStatDAO;
    }

    /**
     * Crée un document Mongo vide pour un nouvel utilisateur
     */
    public void createStatsForUser(String username) {
        userCombatStatDAO.createStatsForUser(username);
    }

    /**
     * Marque que le joueur a utilisé un bazooka pendant ce combat
     */
    public void setBazookaUsed(String username) {
        userCombatStatDAO.setBazookaUsed(username);
    }

    /**
     * Mise à jour des stats après un combat gagné
     */
    public void updateStatsAfterCombat(String username, int cristauxGagnes, int nbTours,String dernierVainqueur) {
        userCombatStatDAO.updateStatsAfterCombat(username, cristauxGagnes, nbTours, dernierVainqueur);
    }

    public void updateStatsTrophy(String username, String nameTrophy){
        userCombatStatDAO.updateStatsTrophy(username, nameTrophy);
    }

    /**
     * Récupère les statistiques d’un utilisateur
     */
    public String getStats(String username) {
        return userCombatStatDAO.getStats(username);
    }
}
