package be.helha.labos.crystalclash.DAO;

import java.util.List;
import java.util.Map;

public interface ShopDAO {

    List<Map<String, Object>> getShopItems();
    boolean buyItem(String username, String itemName, String type);


}
