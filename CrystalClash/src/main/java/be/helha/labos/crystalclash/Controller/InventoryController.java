package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.Inventory.Inventory;
import be.helha.labos.crystalclash.Service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/{username}")
    public Inventory getInventory(@PathVariable String username) {
        return inventoryService.getInventoryForUser(username);
    }
}
