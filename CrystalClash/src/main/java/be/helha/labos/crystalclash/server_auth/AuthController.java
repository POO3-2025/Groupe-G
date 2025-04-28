package be.helha.labos.crystalclash.server_auth;

import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.User.ConnectedUsers;
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
     * Attend un Json avec username et password
     * si tout ok, stock le user
     * lui crée un token
     * et renvoie la reponse du serveur
     * **/
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // Stocker l'authentification dans le SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Génération du token JWT
            String jwtToken = jwtUtils.generateToken(authentication);

            //Ajoute user a chaque co
            ConnectedUsers.addUser(loginRequest.getUsername());

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
