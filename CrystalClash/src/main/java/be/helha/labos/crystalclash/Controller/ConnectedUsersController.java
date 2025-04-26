package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.User.ConnectedUsers;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class ConnectedUsersController {

    @GetMapping("/users/connected/count")
    public int getConnectedUserCount() {
        return ConnectedUsers.getConnectedUserCount();
    }

    @GetMapping("/users/connected/list")
    public Set<String> getConnectedUsernames() {
        return ConnectedUsers.getConnectedUsers();
    }

    @PostMapping("/users/logout")
    public ResponseEntity<String> logoutUser(@RequestBody LogoutRequest request) {
        ConnectedUsers.removeUser(request.getUsername());
        return ResponseEntity.ok("Déconnecté avec succès");
    }

    // DTO pour lire le JSON { "username": "..." }
    public static class LogoutRequest {
        private String username;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

}
