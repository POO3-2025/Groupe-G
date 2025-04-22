package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.Characters.Personnage;
import be.helha.labos.crystalclash.Factory.CharactersFactory;
import be.helha.labos.crystalclash.Object.BackPack;
import be.helha.labos.crystalclash.Service.CharacterService;
import be.helha.labos.crystalclash.Services.HttpService;
import be.helha.labos.crystalclash.User.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/characters")
public class CharactersController {


    @Autowired
    private CharacterService characterService;

    /**
     * @param payload
     *Permet de selectionné un personnage
     * passe plusieurs vérfi
     * */
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


            //Ajout pour savoir perso selectionné
            String currentCharacter = characterService.getCharacterForUser(username);
            if (currentCharacter != null && currentCharacter.equals(characterType)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiReponse("Personnage déjà sélectionné !", null));
            }

            Personnage character = CharactersFactory.CreateCharacters(characterType, user.getLevel());
            characterService.saveCharacterForUser(user.getUsername(), character.getClass().getSimpleName());
            characterService.createBackPackForCharacter(user.getUsername(), character.getClass().getSimpleName());


            //Appelle setSelectedCharacter, déselctionne tout les perso et met celui selectionné par le user a true
            characterService.setSelectedCharacter(user.getUsername(), character.getClass().getSimpleName());

            return ResponseEntity.ok(new ApiReponse("Personnage sélectionné avec succès !", character.getClass().getSimpleName()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiReponse("Désolé " + username + ", ton niveau est insuffisant pour ce personnage !", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiReponse("Erreur : " + e.getMessage(), null));
        }
    }


    /**
     * @param username
     * Obtenir le perso du user
     * */
    @GetMapping("/{username}")
    public ResponseEntity<String> getCharacter(@PathVariable String username) {
        String characterType = characterService.getCharacterForUser(username);
        if (characterType == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Aucun personnage trouvé pour l'utilisateur : " + username);
        }
        return ResponseEntity.ok(characterType);
    }

    /**
     * @param username
     *  recup le backPack du perso avec le username
     * */
    @GetMapping("/{username}/backpack")
    public ResponseEntity<?> getBackpack(@PathVariable String username) {
        try {
            BackPack backpack = characterService.getBackPackForCharacter(username);
            return ResponseEntity.ok(backpack.getObjets()); // retourne la liste d'objets
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération du backpack : " + e.getMessage());
        }
    }


    /**
     * @param username
     * Ajoute un objet au backpack du personnage
     * */
    @PostMapping("/{username}/backpack/add")
    public ResponseEntity<ApiReponse> addObjectToBackpack(@PathVariable String username, @RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String type = payload.get("type");

        if (name == null || type == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiReponse("Name et type sont requis.", null));
        }

        ApiReponse response = characterService.addObjectToBackPack(username, name, type);
        return ResponseEntity.ok(response);
    }


}
