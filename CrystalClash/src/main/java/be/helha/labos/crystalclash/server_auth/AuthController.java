package be.helha.labos.crystalclash.server_auth;

import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.User.ConnectedUsers;
import be.helha.labos.crystalclash.User.UserInfo;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import be.helha.labos.crystalclash.Service.UserService;

import java.util.Optional;


@RestController
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtils jwtUtils; //Gere la generation du token

    private final AuthenticationManager authenticationManager; //Valide le couple login + mode de passe

    /**
     * @param authenticationManager
     * passe authenticationManager via le constructeur, on aurait pu faire une Autowired
     * **/
    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * @param loginRequest
     * But : authentification HTTP POST /login
     * recoit un loginRequest (Json avec username et password), vérif les infos, genere le token et marque l'uti comme connecté
     * et renvoie la reponse du serveur
     * LoginRequest loginRequest contient le username et password
     * **/
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {

            //Récup l'uti en db via le username,
            //Optional = gere si un uti existe pas
            Optional<UserInfo> optionalInfo = userService.getUserInfo(loginRequest.getUsername()); //chercher le user en base
            //SI compte deja a true, bloque la co et envoie un message
           if (optionalInfo.isPresent() && optionalInfo.get().isConnected()) { //si present ds la DBB et regarde le champ is_connected
               return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                   .body("{\"message\":\"Ce compte est déjà connecté.\"}");
           }

           //Verif les identifaint, demande a SpringSecu de valider username et password grave a authenticationManager
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // SI authen ok, stocke ds SecurityContextHolder (permet de savoir si le user est bien co durant la session)
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Génération du token JWT
            String jwtToken = jwtUtils.generateToken(authentication);

            //Ajoute user a chaque co
            //Jjuste un optionalInfo maintenant car plus haut il stock deja les infos du user
            ConnectedUsers.addUser(optionalInfo.get());

            //Try obligé car throw exception
            try {
                //  met à jour en base qu'il est connecté
                userService.updateIsConnected(loginRequest.getUsername(), true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return ResponseEntity.ok(new AuthResponse(jwtToken, "Authentification réussie !"));
        }catch (AuthenticationException e) {
            e.printStackTrace();
            JsonObject errorJson = new JsonObject();
            errorJson.addProperty("message", " de l'authentification : " + e.getMessage());
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                //On renvoie le message sous form de json ducoup lanterna et gson savent s'en occuper
                .header("Content-Type", "application/json")
                .body(errorJson.toString()); // .toString() pour s'assurer que c’est bien du JSON texte
        }

    }

    /**
     * Classe interne direct
     **/

    static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}
