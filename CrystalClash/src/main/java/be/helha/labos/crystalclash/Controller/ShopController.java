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


}
