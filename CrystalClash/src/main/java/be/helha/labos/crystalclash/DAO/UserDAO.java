package be.helha.labos.crystalclash.DAO;


import java.util.Map;
import java.util.Optional;

/*
 * Map Stocker les infos du J
 */

    public interface UserDAO {
        Optional<Map<String, Object>> getUserInfo(String username) throws Exception;
    }

