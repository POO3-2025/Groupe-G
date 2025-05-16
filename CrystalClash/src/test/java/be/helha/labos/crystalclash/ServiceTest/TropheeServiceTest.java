package be.helha.labos.crystalclash.ServiceTest;

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
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe de test pour TropheeService.
 * Vérifie que les trophées Bronze, Silver et Or sont correctement attribués
 * en fonction des conditions (victoires, tours, cristaux, objets utilisés).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TropheeServiceTest {

    private TropheeService tropheeService;

    /**
     * Initialise manuellement les dépendances de TropheeService avant chaque test.
     */
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

    /**
     * Affiche dans la console le nom du test en cours.
     */
    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }

    /**
     * Teste l’attribution du trophée Bronze après une seule victoire.
     * Doit débloquer uniquement Bronze.
     */
    @Test
    @Order(1)
    @DisplayName("Débloquer trophée Bronze")
    public void testBronzeTrophee() {
        UserInfo user = new UserInfo();
        user.setGagner(1); // 1 victoire

        List<Trophee> trophees = tropheeService.getTrophees(user, 100, 10, List.of());

        assertEquals(1, trophees.size(), "Un seul trophée doit être débloqué");
        assertEquals("bronze", trophees.get(0).getNom(), "Le trophée débloqué doit être Bronze");
        assertTrue(user.haveTrophee("Bronze"), "L'utilisateur doit maintenant posséder le trophée Bronze");
    }

    /**
     * Teste l’attribution du trophée Silver après 5 victoires consécutives,
     * 200 cristaux et un combat en 10 tours ou moins.
     */
    @Test
    @Order(2)
    @DisplayName("Débloquer trophée Silver")
    public void testSilverTrophee() {
        UserInfo user = new UserInfo();
        user.setWinconsecutive(5);
        user.setLevel(5); // nécessaire pour la récompense

        List<Trophee> trophees = tropheeService.getTrophees(user, 200, 10, List.of());

        assertEquals(1, trophees.size(), "Un seul trophée doit être débloqué");
        assertEquals("silver", trophees.get(0).getNom(), "Le trophée débloqué doit être Silver");
        assertTrue(user.haveTrophee("silver"), "L'utilisateur doit maintenant posséder le trophée Silver");
    }

    /**
     * Teste l’attribution du trophée Or avec :
     * - 10 victoires consécutives
     * - 500 cristaux
     * - 1 utilisation de bazooka
     * - un combat en 6 tours ou moins
     *
     * Le trophée Silver est déjà attribué pour éviter qu'il ne soit débloqué en plus.
     */
    @Test
    @Order(3)
    @DisplayName("Débloquer trophée Or avec bazooka")
    public void testGoldTropheeWithBazooka() {
        UserInfo user = new UserInfo();
        user.setWinconsecutive(10);
        user.setLevel(10);
        user.incrementUtilisationBazooka();

        // On simule que Silver est déjà débloqué
        user.affTrophee(new Trophee("silver", ""));

        List<ObjectBase> objetsUtilises = List.of(new Weapon("bazooka", 999, 1, 1, 1));
        List<Trophee> trophees = tropheeService.getTrophees(user, 500, 6, objetsUtilises);

        assertEquals(1, trophees.size(), "Un seul trophée doit être débloqué");
        assertEquals("or", trophees.get(0).getNom(), "Le trophée débloqué doit être Or");
        assertTrue(user.haveTrophee("or"), "L'utilisateur doit maintenant posséder le trophée Or");
    }

    /**
     * Vérifie qu'aucun trophée n’est débloqué si l’utilisateur ne remplit aucune condition.
     */
    @Test
    @Order(4)
    @DisplayName("Aucun trophée débloqué")
    public void testNoTropheeUnlocked() {
        UserInfo user = new UserInfo();

        List<Trophee> trophees = tropheeService.getTrophees(user, 50, 20, List.of());

        assertEquals(0, trophees.size(), "Aucun trophée ne doit être débloqué");
        assertFalse(user.haveTrophee("Bronze"), "Le trophée Bronze ne doit pas être attribué");
        assertFalse(user.haveTrophee("Silver"), "Le trophée Silver ne doit pas être attribué");
        assertFalse(user.haveTrophee("Or"), "Le trophée Or ne doit pas être attribué");
    }
}
