package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.Characters.Personnage;
import be.helha.labos.crystalclash.ApiResponse.*;
import be.helha.labos.crystalclash.Factory.CharactersFactory;
import be.helha.labos.crystalclash.Services.HttpService;
import be.helha.labos.crystalclash.User.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import be.helha.labos.crystalclash.Services.CharactersMongoService;

import java.util.Map;

@RestController
@RequestMapping("/characters")
public class CharactersController {
    // Service pour interagir avec MongoDB
    @Autowired
    private CharactersMongoService characterService;
    /**
     * Sélectionne un personnage pour l'utilisateur
     * @param payload => Map contenant le nom d'utilisateur, le type de personnage et le token
     * @return un json avec un message de succès ou d'erreur
     */
    @PostMapping("/select")
    public ResponseEntity<ApiReponse> selectCharacter(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String characterType = payload.get("characterType");
        String token = payload.get("token");

        if (username == null || characterType == null || token == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiReponse("Username, character type et token sont requis.", 1001,null));
        }

        try {
            // Appel API pour récupérer les infos utilisateur
            String userJson = HttpService.getUserInfo(username, token);

            UserInfo user = null;
            try {
                ObjectMapper mapper = new ObjectMapper();
                user = mapper.readValue(userJson, UserInfo.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Personnage character = CharactersFactory.CreateCharacters(characterType, user.getLevel());
            // Sauvegarde dans MongoDB le type du personnage sélectionné par l'utilisateur (ex: "Elf", "Dragon")
            // en utilisant le nom de la classe Java du personnage instancié.
            characterService.saveCharacterForUser(user.getUsername(), character.getClass().getSimpleName());

            return ResponseEntity.ok(new ApiReponse("Personnage sélectionné avec succès !", 0, character.getClass().getSimpleName()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiReponse("Désolé " + username + ", ton niveau est insuffisant pour ce personnage !", 4001,null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiReponse("Erreur serveur : " + e.getMessage(), 5000,null));
        }
    }

    /**
     * Récupère le personnage d'un utilisateur
     * @param username => le nom d'utilisateur
     * @return
     */
    @GetMapping("/{username}")
    public ResponseEntity<String> getCharacter(@PathVariable String username) {
        String characterType = characterService.getCharacterForUser(username);
        if (characterType == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Aucun personnage trouvé pour l'utilisateur : " + username);
        }
        return ResponseEntity.ok(characterType);
    }


}
