package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.Object.*;

import be.helha.labos.crystalclash.Characters.Personnage;
import be.helha.labos.crystalclash.DAO.CharacterDAO;
import be.helha.labos.crystalclash.Factory.CharactersFactory;
import be.helha.labos.crystalclash.Services.HttpService;
import be.helha.labos.crystalclash.User.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/characters")
public class CharactersController {


    @Autowired
    private CharacterDAO characterDAO;

    @PostMapping("/select")
    public ResponseEntity<ApiReponse> selectCharacter(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String characterType = payload.get("characterType");
        String token = payload.get("token");

        if (username == null || characterType == null || token == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiReponse("Username, character type et token sont requis.", null));
        }

        try {
            String userJson = HttpService.getUserInfo(username, token);
            UserInfo user = new ObjectMapper().readValue(userJson, UserInfo.class);

            Personnage character = CharactersFactory.CreateCharacters(characterType, user.getLevel());

            characterDAO.saveCharacterForUser(user.getUsername(), character.getClass().getSimpleName());

            // Création du backpack vide
            characterDAO.createBackPackForCharacter(user.getUsername());

            return ResponseEntity.ok(new ApiReponse("Personnage sélectionné avec succès !", character.getClass().getSimpleName()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiReponse("Désolé " + username + ", ton niveau est insuffisant pour ce personnage !", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiReponse("Erreur : " + e.getMessage(), null));
        }
    }


    @GetMapping("/{username}")
    public ResponseEntity<String> getCharacter(@PathVariable String username) {
        String characterType = characterDAO.getCharacterForUser(username);
        if (characterType == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Aucun personnage trouvé pour l'utilisateur : " + username);
        }
        return ResponseEntity.ok(characterType);
    }

    @GetMapping("/{username}/backpack")
    public ResponseEntity<?> getBackpack(@PathVariable String username) {
        try {
            BackPack backpack = characterDAO.getBackPackForCharacter(username);
            return ResponseEntity.ok(backpack.getObjets()); // retourne la liste d'objets
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération du backpack : " + e.getMessage());
        }
    }

}