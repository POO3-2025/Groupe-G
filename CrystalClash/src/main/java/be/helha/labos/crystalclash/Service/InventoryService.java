package be.helha.labos.crystalclash.Service;


import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.DAO.InventoryDAO;
import be.helha.labos.crystalclash.Inventory.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service

public class InventoryService {

    private InventoryDAO inventoryDAO;

    /**
     * @param inventoryDAO
     * */
    @Autowired
    public InventoryService(InventoryDAO inventoryDAO) {
        this.inventoryDAO = inventoryDAO;
    }
    /**
     * @param username
     * */
    public void createInventoryForUser(String username) {
        inventoryDAO.createInventoryForUser(username);
    }
    /**
     * @param username
     * */
    public Inventory getInventoryForUser(String username) {
        return inventoryDAO.getInventoryForUser(username);
    }
    /**
     * @param username
     * @param inventory
     * */
    public void saveInventoryForUser(String username, Inventory inventory){
        inventoryDAO.saveInventoryForUser(username, inventory);
    }
    /**
     * @param username
     * @param name
     * @param type
     * */
    public ApiReponse SellObject(String username, String name, String type){
        return inventoryDAO.SellObject(username, name, type);
    }

    public ApiReponse addObjectToCoffre(String username, String name, String type) {
        return inventoryDAO.addObjectToCoffre(username, name, type);
    }

}
