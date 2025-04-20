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

    @Autowired
    public InventoryService(InventoryDAO inventoryDAO) {
        this.inventoryDAO = inventoryDAO;
    }

    public void createInventoryForUser(String username) {
        inventoryDAO.createInventoryForUser(username);
    }

    public Inventory getInventoryForUser(String username) {
        return inventoryDAO.getInventoryForUser(username);
    }

    public void saveInventoryForUser(String username, Inventory inventory){
        inventoryDAO.saveInventoryForUser(username, inventory);
    }

    public ApiReponse SellObject(String username, String name, String type){
        return inventoryDAO.SellObject(username, name, type);
    }

}
