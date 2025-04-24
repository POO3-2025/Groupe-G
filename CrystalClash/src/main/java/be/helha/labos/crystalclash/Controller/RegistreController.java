package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.DAO.InventoryDAO;
import be.helha.labos.crystalclash.DAO.RegistreDAO;
import be.helha.labos.crystalclash.DTO.RegisterRequest;
import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.Service.RegistreService;
import be.helha.labos.crystalclash.server_auth.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistreController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RegistreService registreService;

    @Autowired
    private InventoryService inventoryService;

    /**
     * @param request
     * */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        System.out.println("Tentative d'inscription de : " + request.getUsername());

        try {
            if(request.getUsername() == null || request.getUsername().equals("")) {
                return ResponseEntity.badRequest().body("Nom d'utilisateur est requis.");
            }
            if(request.getPassword() == null || request.getPassword().equals("")) {
                return ResponseEntity.badRequest().body("Password d'utilisateur est requis.");
            }
            if (request.getPassword().length() < 4){
                return ResponseEntity.badRequest().body("Password doit contenir au moins 4 caracteres.");

            }

            if (registreService.userExists(request.getUsername())) {
                return ResponseEntity.badRequest().body("Nom d'utilisateur déjà utilisé.");
            }

            String hashedPassword = passwordEncoder.encode(request.getPassword());
            registreService.insertUser(request.getUsername(), hashedPassword);

            inventoryService.createInventoryForUser(request.getUsername());

            return ResponseEntity.ok(new AuthResponse(null, "Inscription réussie !"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erreur lors de l'inscription : " + e.getMessage());
        }
    }

}

