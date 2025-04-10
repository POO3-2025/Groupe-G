package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.Inventory.*;
import be.helha.labos.crystalclash.Services.InventoryMongoServices;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController //Controller Rest
@RequestMapping("/inventory")
public class InventoryController {

    /*
    * Recup userid depuis l url
    *Appel mongodb InventoryMongoServices pour lire
    * si inventaire trouvé alors construit le json
    * */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getInventory(@PathVariable String userId) {
        try {
            Inventory inv = InventoryMongoServices.getInventoryByUsername(userId);
            if (inv == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Utilisateur introuvable"));
            }

            Map<String, Object> response = new HashMap<>(); //Création du json a la main
            response.put("message", "Inventaire récupéré");
            response.put("userId", userId);
            response.put("inventory", inv.getObjets());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Erreur de serveur"));
        }
    }
}
//return ResponseEntity.ok(inv);
