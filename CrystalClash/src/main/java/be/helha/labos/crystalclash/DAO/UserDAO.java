package be.helha.labos.crystalclash.DAO;



import java.util.Optional;
import java.util.Map;

/*
 * Map Stocker les infos du J
 */

    public interface UserDAO {
        Optional<Map<String, Object>> getUserInfo(String username) throws Exception;
    }

