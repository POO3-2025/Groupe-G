package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.Inventory.Inventory;
import be.helha.labos.crystalclash.Services.InventoryMongoServices;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @GetMapping("/{username}")
    public Inventory getInventory(@PathVariable String username) {
        return InventoryMongoServices.getInventoryForUser(username);
    }
}
