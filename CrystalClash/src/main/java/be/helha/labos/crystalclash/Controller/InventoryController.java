package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.Inventory.Inventory;
import be.helha.labos.crystalclash.Object.CoffreDesJoyaux;
import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.Service.UserService;
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

    /**
     * @param username obtenir l'inventaire du username
     */
    @GetMapping("/{username}")
    public Inventory getInventory(@PathVariable String username) {
        return inventoryService.getInventoryForUser(username);
    }

    /**
     * @param payload payload corps body de la requete http POST envoyé par le client
     *                name et type seront ganrit de ce que le user aura envoyé
     */
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

    @GetMapping("/{username}/coffre")
    public ResponseEntity<CoffreDesJoyaux> getCoffre(@PathVariable String username) {
        CoffreDesJoyaux coffre = inventoryService.getCoffreDesJoyauxForUser(username);
        if (coffre == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(coffre);
    }

    @PostMapping("/{username}/coffre/add")
    public ResponseEntity<ApiReponse> addObjectToCoffre(
            @PathVariable String username,
            @RequestBody Map<String, String> payload) {

        String name = payload.get("name");
        String type = payload.get("type");

        if (name == null || type == null) {
            return ResponseEntity.badRequest().body(new ApiReponse("Nom et type sont requis.", null));
        }

        ApiReponse response = inventoryService.addObjectToCoffre(username, name, type);
        String msg = response.getMessage().toLowerCase();

        if (msg.contains("utilisateur introuvable")) return ResponseEntity.status(404).body(response);
        if (msg.contains("inventaire")) return ResponseEntity.status(404).body(response);
        if (msg.contains("aucun coffre")) return ResponseEntity.status(404).body(response);
        if (msg.contains("objet non trouvé")) return ResponseEntity.status(404).body(response);
        if (msg.contains("plein") || msg.contains("brisé")) return ResponseEntity.status(409).body(response);
        if (msg.contains("erreur")) return ResponseEntity.status(500).body(response);


        return ResponseEntity.ok(response);
    }

    //Pour le test
    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

}
