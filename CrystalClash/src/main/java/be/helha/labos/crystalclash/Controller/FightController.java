package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.DTO.StateCombat       ;

import be.helha.labos.crystalclash.Characters.Personnage;
import be.helha.labos.crystalclash.Factory.CharactersFactory;
import be.helha.labos.crystalclash.Object.ObjectBase;
import be.helha.labos.crystalclash.Service.CharacterService;
import be.helha.labos.crystalclash.Service.CombatService;
import be.helha.labos.crystalclash.Service.FightService;
import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.User.ConnectedUsers;
import be.helha.labos.crystalclash.User.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/combat")
public class FightController  {

    @Autowired
    private FightService fightService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private CharacterService characterService;

    @PostMapping("/start")
    public Map<String, Object> startCombat(@RequestBody Map<String, String> body) {
        String player = body.get("username");
        Set<String> others = new HashSet<>(ConnectedUsers.getConnectedUsers().keySet());
        others.remove(player);

        if (others.isEmpty()) throw new RuntimeException("Aucun adversaire trouvé !");
        String opponent = others.stream().findAny().get();

        UserInfo info1 = ConnectedUsers.getUser(player);
        UserInfo info2 = ConnectedUsers.getUser(opponent);

        Personnage p1 = CharactersFactory.getCharacterByType(info1.getSelectedCharacter());
        Personnage p2 = CharactersFactory.getCharacterByType(info2.getSelectedCharacter());

        List<ObjectBase> bp1 = characterService.getBackPackForCharacter(player).getObjets();
        List<ObjectBase> bp2 = characterService.getBackPackForCharacter(player).getObjets();

        fightService.createCombat(player, opponent, p1, p2, bp1, bp2);

        return Map.of("message", "Combat lancé contre " + opponent);
    }

    @PostMapping("/attack")
    public StateCombat attack(@RequestBody Map<String, String> body) {
        String player = body.get("username");
        String type = body.get("type"); // "normal" ou "special"
        fightService.HandleAttach(player, type);
        return fightService.getCombat(player);
    }

    @PostMapping("/use-object")
    public StateCombat useObject(@RequestBody Map<String, String> body) {
        String player = body.get("username");
        String objectId = body.get("objectId");
        fightService.useObject(player, objectId);
        return fightService.getCombat(player);
    }

    @GetMapping("/state/{username}")
    public StateCombat getState(@PathVariable String username) {
        return fightService.getCombat(username);
    }
}
