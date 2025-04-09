package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.Inventory.*;
import be.helha.labos.crystalclash.Services.InventoryMongoServices;
import be.helha.labos.crystalclash.server_auth.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InventoryController {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private InventoryMongoServices inventoryMongoService;

    @GetMapping("/inventory")
    public ResponseEntity<?> getInventory(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extraire le token sans "Bearer "
            String token = authHeader.replace("Bearer ", "");
            String username = jwtUtils.getUsernameFromJwtToken(token);

            // Récupère l'inventaire MongoDB
            Inventory inv = inventoryMongoService.getInventoryByUsername(username);

            if (inv == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(inv);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération de l'inventaire : " + e.getMessage());
        }
    }
}
