package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.Object.ObjectBase;
import be.helha.labos.crystalclash.Service.CharacterService;
import be.helha.labos.crystalclash.Service.RegistreService;
import be.helha.labos.crystalclash.Service.RouletteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur pour gérer les opérations liées à la roulette dans le jeu.
 * Ce contrôleur fournit un point d'accès pour permettre aux utilisateurs
 * de jouer à la roulette et de tenter de gagner des objets.
 */
@RestController
@RequestMapping("/roulette")
public class RouletteController {

    @Autowired
    private RouletteService rouletteService;

    /**
     * Jouer à la roulette
     * @return ResponseEntity contenant le message de succès ou d'erreur
     * pas de paremetre la
     * appelle juste roulette service qui contient la logique de jeu
     * **/
    @PostMapping("/play")
    public ResponseEntity<?> playRoulette() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        try {
            ObjectBase result = rouletteService.PlayRoulette(username);

            Map<String, Object> data = new HashMap<>();
            data.put("objet", result);

            return ResponseEntity.ok(
                Map.of(
                       "success", true,
                        "message", "object gangé bien joué",
                    "data", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(
                            Map.of(
                                    "success", false,
                                    "message", e.getMessage()
                            )
                    );
        }
    }
    //pour les tests
    public void setRouletteService(RouletteService rouletteService) {
        this.rouletteService = rouletteService;
    }
}
