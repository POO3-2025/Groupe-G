package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.DTO.StateCombat;
import be.helha.labos.crystalclash.Characters.Personnage;
import be.helha.labos.crystalclash.Factory.CharactersFactory;
import be.helha.labos.crystalclash.Object.ObjectBase;
import be.helha.labos.crystalclash.Service.CharacterService;
import be.helha.labos.crystalclash.Service.FightService;
import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.User.ConnectedUsers;
import be.helha.labos.crystalclash.User.UserInfo;
import org.springframework.security.core.Authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.util.*;
/**
 * Contrôleur pour gérer les opérations liées aux combats entre joueurs.
 * Ce contrôleur fournit des points d'accès pour lancer des combats, attaquer,
 * utiliser des objets, consulter l'état des combats, et gérer les classements.
 */
@RestController
@RequestMapping("/combat")
public class FightController {
    @PostConstruct
    public void init() {
        System.out.println("[DEBUG] FightController initialisé !");
    }
    @Autowired
    private FightService fightService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private CharacterService characterService;


    /**
     * Lance un combat entre 2 joueurs
     * @param body
     * @return Map clé message valeur combat lancé ...
     */
    @PostMapping("/start")
    public Map<String, Object> startCombat(@RequestBody Map<String, String> body) {
        String player = body.get("username");

        //true si null
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


    /**
     * Gère l'attaque d'un joueur
     * @param body
     * @return ReponseEntity avce un message d'erreur ou de succes
     */
    @PostMapping("/attack")
    public ResponseEntity<?> attack(@RequestBody Map<String, String> body) {
        String player = body.get("username");
        String type = body.get("type");

        fightService.HandleAttach(player, type);

        StateCombat state = fightService.getCombat(player);

        if (state == null) {
            return ResponseEntity.status(410).body("Combat terminé.");
        }

        if (player == null || type == null) {
            return ResponseEntity.badRequest().body("Paramètres manquants !");
        }

         //Map qui va stock la reponse du json (un custom car sinon trop gros)
        Map<String, Object> response = new HashMap<>();
        response.put("playerNow", state.getPlayerNow());
        response.put("tour", state.getTour());
        response.put("finished", state.isFinished());
        response.put("winner", state.getWinner());
        response.put("loser", state.getLoser());
        response.put("pv1", state.getPv(state.getPlayer1()));
        response.put("pv2", state.getPv(state.getPlayer2()));
        response.put("log", state.getLog());


        return ResponseEntity.ok(response);
    }

    /**
     * Gère l'utilisation d'un objet par un joueur
     * @param body
     * @return ReponseEntity avec un message d'erreur ou de succès
     */
    @PostMapping("/use-object")
    public ResponseEntity<?> useObject(@RequestBody Map<String, String> body) throws Exception {
        String player = body.get("username");
        String objectId = body.get("objectId");

        System.out.println("[DEBUG] /use-object appelé avec username=" + player + ", objectId=" + objectId);

        if (player == null || objectId == null) {
            return ResponseEntity.badRequest().body("Paramètres manquants !");
        }


        //récupère l'état avant modif
        //renvoie null (410) sonc a revoir
        StateCombat state = fightService.getCombat(player);
        if (state == null) {
            return ResponseEntity.status(410).body("Combat terminé ou introuvable.");
        }
        //appel useobjet
        fightService.useObject(player, objectId);

        //Map qui va stock la reponse du json (un custom car sinon trop gros)
        Map<String, Object> response = new HashMap<>();
        response.put("playerNow", state.getPlayerNow());
        response.put("tour", state.getTour());
        response.put("finished", state.isFinished());
        response.put("winner", state.getWinner());
        response.put("loser", state.getLoser());
        response.put("pv1", state.getPv(state.getPlayer1()));
        response.put("pv2", state.getPv(state.getPlayer2()));
        response.put("log", state.getLog());


        return ResponseEntity.ok(response);
    }

    /**
     * Récupère l'état du combat pour un joueur donné
     * @param username
     * @return le combat
     */
    @GetMapping("/state/{username}")
    public StateCombat getState(@PathVariable String username) {
        return fightService.getCombat(username);
    }

    /**
     * cree un comabt entre 2 joueurs
     * @param body
     * @return ReponseEntity avec un message d'erreur ou de succès
     */
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
            System.out.println("Création du combat entre " + challenger + " et " + challenged);
            System.out.println("Backpack joueur1 : " + bp1);
            System.out.println("Backpack joueur2 : " + bp2);
        fightService.createCombat(challenger,challenged,p1,p2,bp1,bp2);

            return ResponseEntity.ok(Map.of("message", "Combat lancé !"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("message", "Erreur : " + e.getMessage()));
        }
    }


    //Endpoint si un joeur quitte le combat.

    /**
     * Si un joueur quitte le combat, il doit faire un forfait
     * @param body
     * @param token
     * @return ReponseEntity avec un message d'erreur ou de succès
     * @throws Exception
     */
    @PostMapping("/forfait")
    public ResponseEntity<?> forfait(@RequestBody Map<String, String> body, @RequestHeader("Authorization") String token) throws Exception {
        String username = body.get("username");
        fightService.forfait(username);
        return ResponseEntity.ok(Map.of("message", "Forfait accepté !"));

    }

    /**
     * Récupère le dernier gagnant d'un combat associé à un utilisateur donné.
     * @param username
     * @return ReponseEntity avec le nom du gagnant ou un message d'erreur
     */
    @GetMapping("/Winner")
    public ResponseEntity<Map<String, String>> getLastWinner(@RequestParam String username) {
        String winner = fightService.getLastWinner(username);
        if (winner == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Aucun gagnant trouvé pour " + username));
        }
        return ResponseEntity.ok(Map.of("winner", winner));
    }


    //Petit GetMapping
    /**
     * Récupère le classement actuel des joueurs.
     * @return ReponseEntity avec la liste des joueurs triés par classement
     */
    @GetMapping("/classement")
    public ResponseEntity<List<UserInfo>> getClassementPlayer() {
      List<UserInfo> classement = fightService.getClassementPlayer();
        return ResponseEntity.ok(classement);

    }

    //Ajout encore de set pour y avoir acces dans les tests
    public void setFightService(FightService fightService) {
        this.fightService = fightService;
    }

    public void setCharacterService(CharacterService characterService) {
        this.characterService = characterService;
    }

    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

}





