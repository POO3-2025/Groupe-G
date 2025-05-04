package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.DTO.LogoutRequest;
import be.helha.labos.crystalclash.Service.UserService;
import be.helha.labos.crystalclash.User.ConnectedUsers;
import be.helha.labos.crystalclash.User.UserInfo;
import com.mysql.cj.x.protobuf.MysqlxDatatypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController

public class ConnectedUsersController {

    // username, infos complètes du joueur, synchronizedMap va etre utilise pour les post,get,delete
    private static final Map<String, UserInfo> matchmakingWaitingRoom = Collections.synchronizedMap(new HashMap<>());


    @Autowired
    private UserService userService ;

    @GetMapping("/users/connected/count")
    public int getConnectedUserCount() {
        return ConnectedUsers.getConnectedUserCount();
    }

    @GetMapping("/users/connected/list")
    public Collection<UserInfo> getConnectedUsernames() {
        return ConnectedUsers.getConnectedUsers().values();
    }

    @PostMapping("/users/logout")
    public ResponseEntity<String> logoutUser(@RequestBody LogoutRequest request) {
        System.out.println("Réception de /users/logout avec username = " + request.getUsername());
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            return ResponseEntity.badRequest().body("Erreur : username vide");
        }
        ConnectedUsers.removeUser(request.getUsername());
        try {
            userService.updateIsConnected(request.getUsername(), false); //Déco alors mettre a faux
        }catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok("Déconnecté avec succès");
    }

    /**
    * @param userInfo = user renvoie le json avec ses infos
    * matchmakingWaitingRoom qui est Map<String, UserInfo>
    * */
    @PostMapping("/matchmaking/enter")
    public ResponseEntity<Void> enterMatchMaking(@RequestBody UserInfo userInfo){
        if(userInfo.getUsername() != null && !userInfo.getUsername().isBlank()){
            matchmakingWaitingRoom.put(userInfo.getUsername(), userInfo);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * @param request = user renvoie juste son username dans une map JSON
     * puis on supprimer ce user dans la map, matchmakingWaitingRoom.remove
     * */
    @PostMapping("/matchmaking/exit")
    public ResponseEntity<Void> exitMatchmaking(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        if (username != null && !username.isBlank()) {
            matchmakingWaitingRoom.remove(username);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * @param username
     * ici on va retourner tous les joueurs ds la salle sauf sois meme
     * synchronized = lui permet de faire une action a la fois sur la MAP matchmakingWaitingRoom pour garder une bonne cohérance quoi
     * que ce soit pour le put, remove ou le for dans getAvailableOpponents
     * permet de ne pas planter (ce n'est pas optimal mais je laisse comme ça pour le moment)
     * pq ? car bcp peuvent se co,deco,... si pas cette méthode une erreur aura lieu
     * */
    @GetMapping("/matchmaking/available")
    public List<UserInfo> getAvailableOpponents(@RequestParam("username") String username) {
        List<UserInfo> user = new ArrayList<>();

        //
        synchronized (matchmakingWaitingRoom) {
            for(Map.Entry<String, UserInfo> entry : matchmakingWaitingRoom.entrySet()) {
                if (!entry.getKey().equals(username)) {
                    user.add(entry.getValue());
                }
            }
            return user;
        }

    }


    }
