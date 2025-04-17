package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/user")
public class UserController {



    @Autowired
    private UserService userService;

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserInfo(@PathVariable String username) {
        try {
            return userService.getUserInfo(username)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("message", "Utilisateur introuvable")));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Erreur serveur"));
        }
    }
}
