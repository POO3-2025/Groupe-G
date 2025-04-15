package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.Services.InventoryMongoServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private InventoryMongoServices inventoryMongoService;

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserInfo(@PathVariable String username) {
        try (Connection conn = ConfigManager.getInstance().getSQLConnection("mysqlproduction")) { // Utilisation du singleton
            PreparedStatement stmt = conn.prepareStatement("SELECT level, cristaux FROM users WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();



            if (rs.next()) {
                Map<String, Object> response = new HashMap<>();
                response.put("username", username);
                response.put("level", rs.getInt("level"));
                response.put("cristaux", rs.getInt("cristaux"));
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(404).body(Map.of("message", "Utilisateur introuvable"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Erreur serveur"));
        }
    }
}
