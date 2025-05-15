package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.Characters.Personnage;
import be.helha.labos.crystalclash.Factory.CharactersFactory;
import be.helha.labos.crystalclash.Object.BackPack;
import be.helha.labos.crystalclash.Object.Equipment;
import be.helha.labos.crystalclash.Service.CharacterService;
import be.helha.labos.crystalclash.HttpClient.Login_Register_UserHttpClient;
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


            String userJson = Login_Register_UserHttpClient.getUserInfo(username, token);
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
            characterService.createEquipmentForCharacter(user.getUsername(),character.getClass().getSimpleName());


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


    @GetMapping("/{username}")
    public ResponseEntity<Map<String, Object>> getCharacter(@PathVariable String username) {
        String characterType = characterService.getCharacterForUser(username);

        if (characterType == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Aucun personnage trouvé", "data", null));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Personnage récupéré avec succès !",
                "data", characterType
        ));
    }

    /**
     * @param username
     * @return
     * @throws Exception
     * Récupérer le backpack du personnage
     * */
    @GetMapping("/{username}/backpack")
    public ResponseEntity<Map<String, Object>> getBackpack(@PathVariable String username) {
        try {
            BackPack backpack = characterService.getBackPackForCharacter(username);

            return ResponseEntity.ok(Map.of(
                    "message", "Backpack récupéré avec succès",
                    "data", backpack.getObjets()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Erreur lors de la récupération du backpack : " + e.getMessage(),
                            "data", null
                    ));
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

    /*
     * Supprime un objet du backpack du personnage
     * */
    @PostMapping("/{username}/backpack/remove")
    public ResponseEntity<ApiReponse> removeObjectFromBackpack(@PathVariable String username, @RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        if (name == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiReponse("Name est requis.", null));
        }
        ApiReponse response = characterService.removeObjectFromBackPack(username, name);
        return ResponseEntity.ok(response);
    }

    /**
     * @param username
     * @return
     * @throws Exception
     * Récupérer le personnage sélectionné pour l'utilisateur
     * */
    @PostMapping("/{username}/backpack/coffre/add")
    public ResponseEntity<ApiReponse> addObjectToCoffreInBackPack(@PathVariable String username, @RequestBody Map<String, String> payload) { {
            String name = payload.get("name");
            String type = payload.get("type");

            if (name == null || type == null) {
                return ResponseEntity.badRequest().body(new ApiReponse("Nom et type sont requis.", null));
            }

            ApiReponse response = characterService.addObjectToCoffre(username, name, type);
            String msg = response.getMessage().toLowerCase();

            if ( msg.contains("brisé")) return ResponseEntity.status(409).body(response);

            return ResponseEntity.ok(response);
        }
    }
    /**
     * Modifie la reliability d'un objet (Weapon) dans le backpack d'un personnage
     */
    @PutMapping("/{username}/backpack/update/{objectId}")
    public ResponseEntity<ApiReponse> updateObjectReliability(
            @PathVariable String username,
            @PathVariable String objectId,
            @RequestBody Map<String, Integer> payload
    ) {
        Integer newReliability = payload.get("reliability");

        if (newReliability == null) {
            return ResponseEntity.badRequest().body(new ApiReponse("Le champ 'reliability' est requis.", null));
        }

        ApiReponse response = characterService.updateReliabilityInBackPack(username, objectId, newReliability);
        return ResponseEntity.ok(response);
    }

    /**
     * Modifie la reliability d'un objet (Armor) dans l'equipement d'un personnage
     */
    @PutMapping("/{username}/equipment/update/{objectId}")
    public ResponseEntity<ApiReponse> updateArmorReliability(
            @PathVariable String username,
            @PathVariable String objectId,
            @RequestBody Map<String, Integer> payload
    ) {
        Integer newReliability = payload.get("reliability");

        if (newReliability == null) {
            return ResponseEntity.badRequest().body(new ApiReponse("Le champ 'reliability' est requis.", null));
        }
        System.out.println("je passe par la, controller");
        ApiReponse response = characterService.updateReliabilityInEquipment(username, objectId, newReliability);
        return ResponseEntity.ok(response);
    }


    public void setCharacterServices(CharacterService characterService) {
        this.characterService = characterService;
    }


    @PostMapping("/{username}/backpack/delete/{objectId}")
    public ResponseEntity<ApiReponse> deleteObjectFromBackpack(
            @PathVariable String username,
            @PathVariable String objectId,
            @RequestHeader("Authorization") String token) {

        try {
            // Optionnel : vérifier que le token est valide (si tu veux le gérer côté controller)
            ApiReponse response = characterService.deleteObjectFromBackPack(username, objectId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiReponse("Erreur lors de la suppression de l'objet : " + e.getMessage(), null));
        }
    }


    /**
     * @param username
     * @return
     * @throws Exception
     * Récupérer l'equipement du personnage
     * */
    @GetMapping("/{username}/equipment")
    public ResponseEntity<Map<String, Object>> getEquipment(@PathVariable String username) {
        try {
            Equipment equipment = characterService.getEquipmentForCharacter(username);

            return ResponseEntity.ok(Map.of(
                    "message", "Equipement récupéré avec succès",
                    "data", equipment.getObjets()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Erreur lors de la récupération de l'equipement : " + e.getMessage(),
                            "data", null
                    ));
        }
    }

    /**
     * @param username
     * Ajoute une armure a l'equipement du personnage
     * */
    @PostMapping("/{username}/equipment/add")
    public ResponseEntity<ApiReponse> addArmorToEquipment(@PathVariable String username, @RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String type = payload.get("type");
        if (name == null || type == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiReponse("Name et type sont requis.", null));
        }
        ApiReponse response = characterService.addArmorToEquipment(username,name,type);
        return ResponseEntity.ok(response);
    }

    /*
     * Supprime un equipement du backpack du personnage
     * */
    @PostMapping("/{username}/equipment/remove")
    public ResponseEntity<ApiReponse> removeArmorToEquipment(@PathVariable String username, @RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        if (name == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiReponse("Name est requis.", null));
        }
        ApiReponse response = characterService.removeArmorFromEquipment(username,name);
        return ResponseEntity.ok(response);
    }


}
