package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.DAO.InventoryDAO;
import be.helha.labos.crystalclash.DAO.RegistreDAO;
import be.helha.labos.crystalclash.DTO.RegisterRequest;
import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.Service.RegistreService;
import be.helha.labos.crystalclash.Service.UserCombatStatService;
import be.helha.labos.crystalclash.server_auth.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur pour gérer les opérations liées à l'enregistrement des utilisateurs.
 * Ce contrôleur fournit un point d'accès pour permettre aux utilisateurs
 * de s'inscrire en fournissant un nom d'utilisateur et un mot de passe.
 */
@RestController
public class RegistreController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RegistreService registreService;

    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private UserCombatStatService userCombatStatService;
    /**
     * @param request => RegisterRequest contenant le nom d'utilisateur et le mot de passe
     * @return ResponseEntity contenant le message de succès ou d'erreur
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
            userCombatStatService.createStatsForUser(request.getUsername());

            return ResponseEntity.ok(new AuthResponse(null, "Inscription réussie !"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erreur lors de l'inscription : " + e.getMessage());
        }
    }

    //Ajout de ces setters pour les tests.
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void setRegistreService(RegistreService registreService) {
        this.registreService = registreService;
    }

    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public void setUserCombatStatService(UserCombatStatService userCombatStatService) {
        this.userCombatStatService =  userCombatStatService;
    }
}

