package be.helha.labos.crystalclash.DAO;

import be.helha.labos.crystalclash.Inventory.Inventory;

public interface InventoryDAO {
    Inventory getInventoryForUser(String username);
    void createInventoryForUser(String username);
}
