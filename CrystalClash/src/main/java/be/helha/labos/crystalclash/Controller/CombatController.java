package be.helha.labos.crystalclash.Controller;


import be.helha.labos.crystalclash.Service.CombatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import be.helha.labos.crystalclash.ApiResponse.ApiReponse;

import java.util.Map;

@RestController
@RequestMapping("/combat")
public class CombatController {

    @Autowired
    private CombatService combatService;

    @PostMapping("/start")
    public ResponseEntity<?> startCombat(@RequestBody Map<String, String> body, @RequestHeader("Authorization") String token) {
        String username = body.get("username");
        return ResponseEntity.ok(Map.of("message", combatService.lancerCombat(username)));
    }
}
