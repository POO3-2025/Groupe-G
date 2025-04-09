package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.Services.InventoryMongoServices;
import be.helha.labos.crystalclash.server_auth.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@RestController
public class RegistreController {

    /*
     * Injection auto de l'encodeur de mot de passe Bcrypt
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /*
    * Endpoint /register = envoie une requete avec le username et password au format json
    */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        System.out.println("Tentative d'inscription de : " + request.getUsername()); //Log

        /*
        *Connection a mysqlproduction
        * Vérifie sir le joueur existe déjà
        * Si il existe on revoie alors erreur 400 avec un message clair
        * Hashe du MDP
        * Insertion du user si tout OK
        * Appelle InventoryMongoServices pour crée l'inventaire du nouveau uti stocké en MongDB
        */
        try (Connection conn = ConfigManager.getSQLConnection("mysqlproduction")) {
            // Vérifie si l'utilisateur existe déjà
            PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?");
            checkStmt.setString(1, request.getUsername());
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                return ResponseEntity.badRequest().body("Nom d'utilisateur déjà utilisé.");
            }
            String hashedPassword = passwordEncoder.encode(request.getPassword());

            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO users (username, password, level, cristaux) VALUES (?, ?, ?, ?)");
            stmt.setString(1, request.getUsername());
            stmt.setString(2, hashedPassword);
            stmt.setInt(3, 1); // niveau initial
            stmt.setInt(4, 100); // cristaux initiaux

            stmt.executeUpdate();

            // Crée l'inventaire MongoDB vide
            InventoryMongoServices.CreateInvetoriesForUser(request.getUsername());

            return ResponseEntity.ok(new AuthResponse(null, "Inscription réussie !"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erreur lors de l'inscription : " + e.getMessage());
        }
    }

    /*
    * Classe interne représente le corps du json
    */
    public static class RegisterRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
