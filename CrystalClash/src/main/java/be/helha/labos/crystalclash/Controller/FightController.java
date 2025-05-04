package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.DTO.StateCombat;
import be.helha.labos.crystalclash.Characters.Personnage;
import be.helha.labos.crystalclash.Factory.CharactersFactory;
import be.helha.labos.crystalclash.Object.ObjectBase;
import be.helha.labos.crystalclash.Service.CharacterService;
import be.helha.labos.crystalclash.Service.CombatService;
import be.helha.labos.crystalclash.Service.FightService;
import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.User.ConnectedUsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.util.*;

@RestController
@RequestMapping("/combat")
public class FightController {

    @Autowired
    private FightService fightService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private CharacterService characterService;


    @PostConstruct
    public void init() {
        System.out.println("=== FightController initialisé ===");
    }

    @PostMapping("/start")
    public Map<String, Object> startCombat(@RequestBody Map<String, String> body) {
        String player = body.get("username");

        if (player == null || player.isBlank()) {
            throw new RuntimeException("Nom d'utilisateur manquant dans la requête !");
        }

        System.out.println("[DEBUG] FightController - startCombat() appelé par " + player);
        Set<String> others = new HashSet<>(ConnectedUsers.getConnectedUsers().keySet());
        others.remove(player);

        if (others.isEmpty()) throw new RuntimeException("Aucun adversaire trouvé !");
        String opponent = others.stream().findAny().get();

        // Rechargement à jour du type de personnage sélectionné
        String charType1 = characterService.getCharacterForUser(player);
        String charType2 = characterService.getCharacterForUser(opponent);

        if (charType1 == null || charType2 == null) {
            throw new RuntimeException("Les personnages n'ont pas été sélectionnés !");
        }

        Personnage p1 = CharactersFactory.getCharacterByType(charType1);
        Personnage p2 = CharactersFactory.getCharacterByType(charType2);

        List<ObjectBase> bp1 = characterService.getBackPackForCharacter(player).getObjets();
        List<ObjectBase> bp2 = characterService.getBackPackForCharacter(opponent).getObjets();

        fightService.createCombat(player, opponent, p1, p2, bp1, bp2);

        return Map.of("message", "Combat lancé contre " + opponent);
    }

    @PostMapping("/attack")
    public ResponseEntity<?> attack(@RequestBody Map<String, String> body) {
        String player = body.get("username");
        String type = body.get("type");

        System.out.println("[DEBUG] /attack appelé avec username=" + player + ", type=" + type);

        if (player == null || type == null) {
            return ResponseEntity.badRequest().body("Paramètres manquants !");
        }

        fightService.HandleAttach(player, type);
        return ResponseEntity.ok(fightService.getCombat(player));
    }

    @PostMapping("/use-object")
    public ResponseEntity<?> useObject(@RequestBody Map<String, String> body) {
        String player = body.get("username");
        String objectId = body.get("objectId");

        System.out.println("[DEBUG] /use-object appelé avec username=" + player + ", objectId=" + objectId);

        if (player == null || objectId == null) {
            return ResponseEntity.badRequest().body("Paramètres manquants !");
        }

        fightService.useObject(player, objectId);
        return ResponseEntity.ok(fightService.getCombat(player));
    }

    @GetMapping("/state/{username}")
    public StateCombat getState(@PathVariable String username) {
        return fightService.getCombat(username);
    }

    @PostMapping("/challenge")
    public ResponseEntity<?> challenge(@RequestBody Map<String, String> body) {
        try{
         String challenger = body.get("challenger");
         String challenged = body.get("challenged");

         //Recup perso
        String charType1 = characterService.getCharacterForUser(challenger);
        String charType2 = characterService.getCharacterForUser(challenged);

        if(charType1 == null || charType2 == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Personnage manquant"));
        }

        //Instancier
        Personnage p1 = CharactersFactory.getCharacterByType(charType1);
        Personnage p2 = CharactersFactory.getCharacterByType(charType2);

        List<ObjectBase> bp1 = characterService.getBackPackForCharacter(challenger).getObjets();
        List<ObjectBase> bp2 = characterService.getBackPackForCharacter(challenged).getObjets();

        fightService.createCombat(challenger,challenged,p1,p2,bp1,bp2);

            return ResponseEntity.ok(Map.of("message", "Combat lancé !"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("message", "Erreur : " + e.getMessage()));
        }
    }
}



