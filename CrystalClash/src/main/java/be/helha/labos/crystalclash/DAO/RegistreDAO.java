package be.helha.labos.crystalclash.DAO;

import java.util.List;

/*
* Voir si user existe
* Inserer user dans la db
* */

public interface RegistreDAO {

    boolean userExists(String username) throws Exception;
    void insertUser(String username, String hashedPassword) throws Exception;
}
