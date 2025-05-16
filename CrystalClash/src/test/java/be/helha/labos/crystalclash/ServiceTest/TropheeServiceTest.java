package be.helha.labos.crystalclash.ServiceTest;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAO.InventoryDAO;
import be.helha.labos.crystalclash.DAO.UserCombatStatDAO;
import be.helha.labos.crystalclash.DAOImpl.InventoryDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.UserCombatStatDAOImpl;
import be.helha.labos.crystalclash.DTO.Trophee;
import be.helha.labos.crystalclash.Object.ObjectBase;
import be.helha.labos.crystalclash.Object.Weapon;
import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.Service.TropheeService;
import be.helha.labos.crystalclash.Service.UserCombatStatService;
import be.helha.labos.crystalclash.User.UserInfo;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TropheeServiceTest {

    private TropheeService tropheeService;

    @BeforeEach
    public void setUp() {
        UserCombatStatDAO dao = new UserCombatStatDAOImpl();
        UserCombatStatService userService = new UserCombatStatService(dao);

        InventoryDAO inventoryDAO = new InventoryDAOImpl();
        InventoryService inventoryService = new InventoryService(inventoryDAO);

        tropheeService = new TropheeService();
        tropheeService.setUserCombatStatService(userService);
        tropheeService.setInventoryService(inventoryService);
    }

    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }

    @Test
    @Order(1)
    @DisplayName("Débloquer trophée Bronze")
    public void testBronzeTrophee() {
        UserInfo user = new UserInfo();
        user.setGagner(1);

        List<Trophee> trophees = tropheeService.getTrophees(user, 100, 10, List.of());

        assertEquals(1, trophees.size(), "Un seul trophée doit être débloqué");
        assertEquals("bronze", trophees.get(0).getNom(), "Le trophée débloqué doit être Bronze");
        assertTrue(user.haveTrophee("Bronze"), "L'utilisateur doit maintenant posséder le trophée Bronze");
    }

    @Test
    @Order(2)
    @DisplayName("Débloquer trophée Silver")
    public void testSilverTrophee() {
        UserInfo user = new UserInfo();
        user.setWinconsecutive(5);
        user.setLevel(5);
        List<Trophee> trophees = tropheeService.getTrophees(user, 200, 10, List.of());

        assertEquals(1, trophees.size(), "Un seul trophée doit être débloqué");
        assertEquals("silver", trophees.get(0).getNom(), "Le trophée débloqué doit être Silver");
        assertTrue(user.haveTrophee("silver"), "L'utilisateur doit maintenant posséder le trophée Silver");
    }

    @Test
    @Order(3)
    @DisplayName("Débloquer trophée Or avec bazooka")
    public void testGoldTropheeWithBazooka() {
        UserInfo user = new UserInfo();
        user.setWinconsecutive(10);
        user.setLevel(10);
        user.incrementUtilisationBazooka();

        user.affTrophee(new Trophee("silver", "")); //Pas redebloquer silvre

        List<ObjectBase> objetsUtilises = List.of(new Weapon("bazooka", 999, 1, 1, 1));
        List<Trophee> trophees = tropheeService.getTrophees(user, 500, 6, List.of(new Weapon("bazooka", 999, 1, 1, 1)));

        assertEquals(1, trophees.size(), "Un seul trophée doit être débloqué");
        assertEquals("or", trophees.get(0).getNom(), "Le trophée débloqué doit être Or");
        assertTrue(user.haveTrophee("or"), "L'utilisateur doit maintenant posséder le trophée Or");
    }

    @Test
    @Order(4)
    @DisplayName("Aucun trophée débloqué")
    public void testNoTropheeUnlocked() {
        UserInfo user = new UserInfo();

        List<Trophee> trophees = tropheeService.getTrophees(user, 50, 20, List.of());

        assertEquals(0, trophees.size(), "Aucun tro phée ne doit être débloqué");
        assertFalse(user.haveTrophee("Bronze"), "Le trophée Bronze ne doit pas être attribué");
        assertFalse(user.haveTrophee("Silver"), "Le trophée Silver ne doit pas être attribué");
        assertFalse(user.haveTrophee("Or"), "Le trophée Or ne doit pas être attribué");
    }

    @AfterAll
    public static void resetMySQLUsers_AND_Mongo() throws Exception {
        var conn = ConfigManager.getInstance().getSQLConnection("mysqltest");
        var stmt = conn.prepareStatement("DELETE FROM users");
        stmt.executeUpdate();
        stmt.close();
        conn.close();
        System.out.println("Tous les utilisateurs MySQL ont été supprimés.");

        MongoDatabase db = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");


        db.getCollection("Characters").deleteMany(new Document());
        // Ajoute d'autres collections si besoin
        db.getCollection("Inventory").deleteMany(new Document());

        System.out.println("Toutes les données Mongo ont été supprimées.");
    }
}
