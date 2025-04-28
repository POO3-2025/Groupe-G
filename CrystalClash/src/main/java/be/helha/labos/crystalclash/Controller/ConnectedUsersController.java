package be.helha.labos.crystalclash.Controller;

import be.helha.labos.crystalclash.DTO.LogoutRequest;
import be.helha.labos.crystalclash.Service.UserService;
import be.helha.labos.crystalclash.User.ConnectedUsers;
import com.mysql.cj.x.protobuf.MysqlxDatatypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController

public class ConnectedUsersController {

    @Autowired
    private UserService userService ;

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


    /*Style de matchmaking simple test
     *on va recup les joueurs co ensuite on s'ejecte de la liste car null de se battre contre sois meme
     *  */
  /*  @PostMapping("/matchmaking/find")
    public ResponseEntity<String> matchmaking(@RequestBody String username){
        Set<String> users = ConnectedUsers.getConnectedUsers();
        users.remove(username);
        if (users.isEmpty()) {
            return ResponseEntity.badRequest().body("Erreur : username vide");
        }
        //Stream transfrorme le pseudo en flux
        //FinfAny va prendre 1 élément aux hasard
        String hasard = users.stream().findAny().get();
        return ResponseEntity.ok(hasard);
    }*/

    //Aleatoire
    /*
    *de base je passais que String username en commentaire mais le problème etait que Spring lisait username comme si c'etait tout le Json, le resultat etait que le matchMaking pouvait lancer le combat contre sois meme
    * La avec Map spirng va prendre ce qui est renvoyé en JSON et va le décode en Map<String....>
    *Exemple "username":"celio" mit dans une map et avec String username = request.get("username"); celio peut etre bien récup
    **/
    @PostMapping("/matchmaking/find")
    public ResponseEntity<String> matchmaking(@RequestBody Map<String, String> request){
        String username = request.get("username");
        Set<String> users =new HashSet<>( ConnectedUsers.getConnectedUsers());
        users.remove(username);

        if (users.isEmpty()) {
            return ResponseEntity.badRequest().body("");
        }

        List<String> userList = new ArrayList<>(users);

        Random user = new Random();
        String hasard = userList.get(user.nextInt(userList.size()));
        //Stream transfrorme le pseudo en flux
        //FinfAny va prendre 1 élément aux hasard
       // String hasard = users.stream().findAny().get();
        return ResponseEntity.ok(hasard);
    }
}
