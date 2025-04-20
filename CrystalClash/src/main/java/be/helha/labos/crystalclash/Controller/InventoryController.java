package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.Inventory.Inventory;
import be.helha.labos.crystalclash.Service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import be.helha.labos.crystalclash.ApiResponse.ApiReponse;

import java.util.Map;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/{username}")
    public Inventory getInventory(@PathVariable String username) {
        return inventoryService.getInventoryForUser(username);
    }

    @PostMapping("/sell")
    public ResponseEntity<ApiReponse> sellObject(@RequestBody Map<String, String> payload) {
        //charge nom et type
        String name = payload.get("name");
        String type = payload.get("type");
// Récupère le nom d'utilisateur (subject du token JWT) via le contexte de sécurité
        //SecurityContextHolder accede a la secu lié a la requete
        //recup l'authentification
        //retourne le subject du JWT, le nom de l uti
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        ApiReponse response = inventoryService.SellObject(username, name, type);
        return ResponseEntity.ok(response);
    }
}
