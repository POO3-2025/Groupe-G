package be.helha.labos.crystalclash.DAO;

/*
 * Voir si user existe
 * Inserer user dans la db
 * */

public interface RegistreDAO {

    boolean userExists(String username) throws Exception;
    void insertUser(String username, String hashedPassword) throws Exception;
}
