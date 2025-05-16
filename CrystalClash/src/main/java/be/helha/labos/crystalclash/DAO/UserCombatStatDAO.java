package be.helha.labos.crystalclash.DAO;


import org.bson.Document;

public interface UserCombatStatDAO {

    /**
     * @param username
     * Crée collecion pour state combat
     * **/
    void createStatsForUser(String username);

    /**
     * @param username obtenir les stats
     **/
    String getStats(String username);
    /**
     * @param nbTours
     * @param username
     * @param cristauxGagnes
     * Mettre a jou apres combat
     * **/
    void updateStatsAfterCombat(String username, int cristauxGagnes, int nbTours);

    /**
     * @param username
     * Changer la valeur pour savoir si bazooka use ou pas
     * **/
    void setBazookaUsed(String username);

    /**
     * @param username
     * @param nameTrophy
     * met a jour le boolean d un trophee sit debloqué
     * **/
    void updateStatsTrophy(String username, String nameTrophy);



    }
