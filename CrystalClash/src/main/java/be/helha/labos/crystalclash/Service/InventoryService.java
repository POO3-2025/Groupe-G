package be.helha.labos.crystalclash.Service;


import be.helha.labos.crystalclash.DAO.InventoryDAO;
import be.helha.labos.crystalclash.Inventory.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    }
