package be.helha.labos.crystalclash.Controller;


import be.helha.labos.crystalclash.Service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour gérer les opérations liées à la boutique dans le jeu.
 * Ce contrôleur fournit des points d'accès pour récupérer les objets disponibles
 * dans la boutique et permettre aux utilisateurs d'acheter des objets.
 */
@RestController
@RequestMapping("/shop")
public class ShopController {

    @Autowired
    private ShopService shopService;

    //Get shop

    /**
     * Get pour obtenir le shop garnit
     * **/
    @GetMapping
    public List<Map<String, Object>> getShops() {
        return shopService.getShopItems();
    }


    /**
     * Acheter un objet en POST
     * @param payload = le corps de la requete http POST
     * @return ResponseEntity avec le message de succès ou d'erreur
     * passe dans le json le type et nom de l'objet
     * */
    @PostMapping("/buy")
    public ResponseEntity<Map<String, Object>> buyItem(
        @RequestBody Map<String, String> payload
    ) {
        String name = payload.get("name");
        String type = payload.get("type");

        //Va récupe l objet Authentication mis dans JwUtils
        //appelle de GetName sur l'objet Authentication qui retourne par defaut le subject du token JWT
        ; Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();// ici Spring récupère le subject du JWT

        String resultMessage = shopService.buyItem(username, name, type);
        boolean success = resultMessage.contains("acheté avec succès");

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", resultMessage);
        return ResponseEntity.ok(response);
    }

    //Setter le controller pour le test
    public void setShopService(ShopService shopService) {
        this.shopService = shopService;
    }

}
