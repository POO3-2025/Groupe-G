package be.helha.labos.crystalclash.DAO;

/*
 * Voir si user existe
 * Inserer user dans la db
 * */

public interface RegistreDAO {

    /**
     * @param username
     * */
    boolean userExists(String username) throws Exception;
    /**
     * @param username
     * @param hashedPassword
     * */
    void insertUser(String username, String hashedPassword) throws Exception;
}
