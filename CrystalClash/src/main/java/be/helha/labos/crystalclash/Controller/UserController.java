package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.Service.CharacterService;
import be.helha.labos.crystalclash.Service.ShopService;
import be.helha.labos.crystalclash.Service.UserService;
import be.helha.labos.crystalclash.User.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/user")
public class UserController {


    @Autowired
    private CharacterService characterService;
    @Autowired
    private UserService userService;

    /**
     * @param username
     * obtenir le user
     * il faut se connecter, récupere le token et apres on pouura l'obtenir
     * */
    @GetMapping("/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        try {
            Optional<UserInfo> optionalInfo = userService.getUserInfo(username);

            if (optionalInfo.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("message", "Utilisateur introuvable"));
            }

            UserInfo info = optionalInfo.get();
            String selectedCharacter = characterService.getCharacterForUser(username);
            info.setSelectedCharacter(selectedCharacter);

            return ResponseEntity.ok(info);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Erreur serveur"));
        }
    }

   public void setCharacterService(CharacterService characterService) {
        this.characterService = characterService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }





}
