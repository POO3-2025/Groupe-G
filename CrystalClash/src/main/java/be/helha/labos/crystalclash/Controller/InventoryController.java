package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.DAO.InventoryDAO;
import be.helha.labos.crystalclash.Inventory.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryDAO inventoryDAO;

    @GetMapping("/{username}")
    public Inventory getInventory(@PathVariable String username) {
        return inventoryDAO.getInventoryForUser(username);
    }
}
