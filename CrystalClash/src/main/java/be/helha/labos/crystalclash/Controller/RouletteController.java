package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.Object.ObjectBase;
import be.helha.labos.crystalclash.Service.RouletteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/roulette")
public class RouletteController {

    @Autowired
    private RouletteService rouletteService;

    /**
     * pas de paremetre la
     * apprelle juste roulette service qui contient la logique de jeu
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

    public void setRouletteService(RouletteService rouletteService) {
        this.rouletteService = rouletteService;
    }
}
