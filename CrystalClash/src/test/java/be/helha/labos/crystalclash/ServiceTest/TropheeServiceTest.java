package be.helha.labos.crystalclash.ServiceTest;

import be.helha.labos.crystalclash.DTO.Trophee;
import be.helha.labos.crystalclash.Object.ObjectBase;
import be.helha.labos.crystalclash.Object.Weapon;
import be.helha.labos.crystalclash.Service.TropheeService;
import be.helha.labos.crystalclash.User.UserInfo;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TropheeServiceTest {

    private TropheeService tropheeService;

    @BeforeEach
    public void setUp() {
        tropheeService = new TropheeService();
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
        assertEquals("Bronze", trophees.get(0).getNom(), "Le trophée débloqué doit être Bronze");
        assertTrue(user.haveTrophee("Bronze"), "L'utilisateur doit maintenant posséder le trophée Bronze");
    }

    @Test
    @Order(2)
    @DisplayName("Débloquer trophée Silver")
    public void testSilverTrophee() {
        UserInfo user = new UserInfo();
        user.setWinconsecutive(5);

        List<Trophee> trophees = tropheeService.getTrophees(user, 200, 10, List.of());

        assertEquals(1, trophees.size(), "Un seul trophée doit être débloqué");
        assertEquals("Silver", trophees.get(0).getNom(), "Le trophée débloqué doit être Silver");
        assertTrue(user.haveTrophee("Silver"), "L'utilisateur doit maintenant posséder le trophée Silver");
    }

    @Test
    @Order(3)
    @DisplayName("Débloquer trophée Or avec bazooka")
    public void testGoldTropheeWithBazooka() {
        UserInfo user = new UserInfo();
        user.setWinconsecutive(10);
        user.incrementUtilisationBazooka();
        List<ObjectBase> objetsUtilises = List.of(new Weapon("bazooka", 999, 1, 1, 1));
        List<Trophee> trophees = tropheeService.getTrophees(user, 500, 6, List.of(new Weapon("bazooka", 999, 1, 1, 1)));

        assertEquals(1, trophees.size(), "Un seul trophée doit être débloqué");
        assertEquals("Or", trophees.get(0).getNom(), "Le trophée débloqué doit être Or");
        assertTrue(user.haveTrophee("Or"), "L'utilisateur doit maintenant posséder le trophée Or");
    }

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
