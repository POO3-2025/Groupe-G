package be.helha.labos.crystalclash.ControllerTest;

import be.helha.labos.crystalclash.Controller.FightController;
import be.helha.labos.crystalclash.DAOImpl.*;
import be.helha.labos.crystalclash.Service.*;
import be.helha.labos.crystalclash.Object.BackPack;
import be.helha.labos.crystalclash.DTO.StateCombat;
import be.helha.labos.crystalclash.User.UserInfo;
import be.helha.labos.crystalclash.Factory.CharactersFactory;
import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;

import org.junit.jupiter.api.*;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class FightControllerTest{

    private FightController controller;

    private final String user1 = "userTes1";
    private final String user2 = "userTes2";

    @BeforeEach
    public void setUp() throws Exception {
        // Config test
        var config = ConfigManager.getInstance().getConfig();
        var db = config.getAsJsonObject("db");
        db.getAsJsonObject("mysqlproduction").getAsJsonObject("BDCredentials")
            .entrySet()
            .forEach(e -> db.getAsJsonObject("mysqlproduction").getAsJsonObject("BDCredentials")
                .add(e.getKey(), db.getAsJsonObject("mysqltest").getAsJsonObject("BDCredentials").get(e.getKey())));
        db.getAsJsonObject("MongoDBProduction").getAsJsonObject("BDCredentials")
            .entrySet()
            .forEach(e -> db.getAsJsonObject("MongoDBProduction").getAsJsonObject("BDCredentials")
                .add(e.getKey(), db.getAsJsonObject("MongoDBTest").getAsJsonObject("BDCredentials").get(e.getKey())));

        // DAO & Service

        var fightDAO = new FightDAOImpl();
        var userDAO = new UserDAOImpl();
        var inventoryDAO = new InventoryDAOImpl();
        var characterDAO = new CharacterDAOImpl();

        var fightService = new FightService(fightDAO);
        var userService = new UserService(userDAO);
        var inventoryService = new InventoryService(inventoryDAO);
        var characterService = new CharacterService(characterDAO);

        inventoryService.setUserService(userService);
        fightService.setUserService(userService);
        fightService.setInventoryService(inventoryService);
        fightService.setCharacterService(characterService);

        controller = new FightController();
        controller.setFightService(fightService);
        controller.setCharacterService(characterService);
        controller.setInventoryService(inventoryService);

        // Crée les utilisateurs dans MySQL
        var conn = ConfigManager.getInstance().getSQLConnection("mysqltest");
        var stmt = conn.prepareStatement("""
        INSERT INTO users (username, password, level, cristaux, is_connected, gagner, perdu, Winconsecutive)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE cristaux = VALUES(cristaux), level = VALUES(level)
    """);

        for (String user : List.of(user1, user2)) {
            stmt.setString(1, user);
            stmt.setString(2, "pass");
            stmt.setInt(3, 5);
            stmt.setInt(4, 100);
            stmt.setBoolean(5, false);
            stmt.setInt(6, 0);
            stmt.setInt(7, 0);
            stmt.setInt(8, 0);
            stmt.executeUpdate();
        }
        stmt.close();
        conn.close();


        characterDAO.saveCharacterForUser(user1, "elf");
        characterDAO.saveCharacterForUser(user2, "troll");
        characterDAO.saveBackPackForCharacter(user1, new BackPack());
        characterDAO.saveBackPackForCharacter(user2, new BackPack());

        inventoryDAO.createInventoryForUser(user1);
        inventoryDAO.createInventoryForUser(user2);
    }

    @Test
    @DisplayName("GET /state/{username} retourne le bon état")
    public void testGetState() {
        var body = Map.of("challenger", user1, "challenged", user2);
        controller.challenge(body); // Lance combat

        StateCombat combat = controller.getState(user1);
        assertNotNull(combat);
        assertEquals(user1, combat.getPlayerNow());
    }

    @Test
    @DisplayName("POST /challenge lance un combat entre deux joueurs")
    public void testChallenge() {
        var body = Map.of("challenger", user1, "challenged", user2);
        ResponseEntity<?> response = controller.challenge(body);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Combat lancé"));
    }

    @Test
    @DisplayName("POST /forfait termine bien le combat")
    public void testForfait() throws Exception {
        var body = Map.of("challenger", user1, "challenged", user2);
        controller.challenge(body);

        ResponseEntity<?> res = controller.forfait(Map.of("username", user1), "Bearer token");
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("Forfait accepté !", ((Map<?, ?>) res.getBody()).get("message"));
    }

    @Test
    @DisplayName("GET /Winner retourne le gagnant du dernier combat")
    public void testGetLastWinner() throws Exception {
        var body = Map.of("challenger", user1, "challenged", user2);
        controller.challenge(body);

        controller.forfait(Map.of("username", user2), "Bearer token"); // user1 doit gagner

        ResponseEntity<Map<String, String>> res = controller.getLastWinner(user1);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals(user1, res.getBody().get("winner"));
    }

    @Test
    @DisplayName("GET /classement retourne une liste d'utilisateurs")
    public void testGetClassement() {
        ResponseEntity<List<UserInfo>> res = controller.getClassementPlayer();
        assertEquals(200, res.getStatusCodeValue());
        assertNotNull(res.getBody());
    }
}
