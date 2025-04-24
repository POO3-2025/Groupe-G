package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.Service.CombatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/combat")
public class CombatController {

    @Autowired
    private CombatService combatService;

    @PostMapping("/start")
    public ResponseEntity<?> startCombat(@RequestBody Map<String, String> body, @RequestHeader("Authorization") String token) {
        String username = body.get("username");

        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Nom d'utilisateur manquant"));
        }

        try {
            // Démarre le combat avec le service
            String message = combatService.lancerCombat(username);

            // Réponse au client avec le message du combat
            return ResponseEntity.ok(Map.of("message", message));

        } catch (Exception e) {
            // Gestion des erreurs
            return ResponseEntity.status(500).body(Map.of("message", "Erreur lors du lancement du combat : " + e.getMessage()));
        }
    }
}
