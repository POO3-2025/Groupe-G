package be.helha.labos.crystalclash.DAO;

import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.Inventory.Inventory;

import java.util.Map;

public interface InventoryDAO {
    Inventory getInventoryForUser(String username);
    void createInventoryForUser(String username);
    void saveInventoryForUser(String username, Inventory inventory);
    public ApiReponse SellObject(String username, String name, String type);

}
