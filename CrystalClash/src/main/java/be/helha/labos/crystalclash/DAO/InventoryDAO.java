package be.helha.labos.crystalclash.DAO;

import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.DTO.Inventory;
import be.helha.labos.crystalclash.Object.CoffreDesJoyaux;

public interface InventoryDAO {
    /**
     * @param username
     * */
    Inventory getInventoryForUser(String username);
    /**
     * @param username
     * */
    void createInventoryForUser(String username);
    /**
     * @param username
     * @param inventory
     * */
    void saveInventoryForUser(String username, Inventory inventory);
    /**
     * @param username
     * @param name
     * @param type
     * */
    public ApiReponse SellObject(String username, String name, String type);

    public ApiReponse addObjectToCoffre(String username, String name, String type);
    public CoffreDesJoyaux getCoffreDesJoyauxForUser(String username);

}
