package be.helha.labos.crystalclash.Controller;
import be.helha.labos.crystalclash.DTO.FightHistory;
import be.helha.labos.crystalclash.Service.FightHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/fight-history")
public class FightHistoryController {

    @Autowired
    FightHistoryService fightHistoryService;

    //Save nouveau combat
    @PostMapping("/save")
   public String saveFight(@RequestBody FightHistory fightHistory) {
        fightHistoryService.saveFight(fightHistory);
        return "Combat souvegardé";
    }


    //Get tout les combats
    @GetMapping("/all")
    public List<FightHistory> getAllFights() {
        return fightHistoryService.getAllFights();
    }

    // GET Récupère les combats d'un joueur (via ID MySQL)
    @GetMapping("/player/{id}")
    public List<FightHistory> getFightsByPlayerId(@PathVariable("id") int id) {
        return fightHistoryService.getFightsByPlayerId(id);
    }

    @GetMapping("/username/{username}")
    public List<Map<String, String>> getFightsByUsername(@PathVariable String username) {
        return fightHistoryService.getFightsByUsername(username);
    }

}
