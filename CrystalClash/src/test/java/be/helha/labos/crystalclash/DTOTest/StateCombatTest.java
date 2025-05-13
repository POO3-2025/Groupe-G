package be.helha.labos.crystalclash.DTOTest;

import be.helha.labos.crystalclash.Characters.Personnage;
import be.helha.labos.crystalclash.DTO.StateCombat;
import be.helha.labos.crystalclash.Object.CoffreDesJoyaux;
import be.helha.labos.crystalclash.Object.ObjectBase;
import org.junit.jupiter.api.*;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StateCombatTest {

    // Classes simples pour le test (à remplacer par tes vraies implémentations si existantes)
    static class TestPersonnage extends Personnage {
        public TestPersonnage(int pv) {
            this.setPV(pv);
        }
    }

    static class TestObjet extends ObjectBase {
        public TestObjet(String name) {
            this.setName(name);
            this.setType("Test");
        }
    }

    private StateCombat combat;

    @BeforeEach
    public void setUp() {
        Personnage char1 = new TestPersonnage(100);
        Personnage char2 = new TestPersonnage(80);

        // Coffre avec un objet à l’intérieur
        CoffreDesJoyaux coffre = new CoffreDesJoyaux();
        coffre.setReliability(30);
        coffre.setContenu(List.of(new TestObjet("Épée")));

        List<ObjectBase> bp1 = List.of(coffre);
        List<ObjectBase> bp2 = new ArrayList<>();

        combat = new StateCombat("Alice", "Bob", char1, char2, bp1, bp2);
    }

    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("\n");
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }

    @Order(1)
    @Test
    @DisplayName("Initialisation du combat")
    public void testInitialisation() {
        assertEquals("Alice", combat.getPlayer1());
        assertEquals("Bob", combat.getPlayer2());
        assertEquals("Alice", combat.getPlayerNow());
        assertEquals(100, combat.getPv("Alice"));
        assertEquals(80, combat.getPv("Bob"));
    }

    @Order(2)
    @Test
    @DisplayName("Changement de tour")
    public void testNextTurn() {
        combat.NextTurn();
        assertEquals("Bob", combat.getPlayerNow());
        assertEquals(2, combat.getTour());
    }

    @Order(3)
    @Test
    @DisplayName("Modification des PV")
    public void testPvModification() {
        combat.setPv("Alice", 45);
        combat.setPv("Bob", 20);

        assertEquals(45, combat.getPv("Alice"));
        assertEquals(20, combat.getPv("Bob"));
    }

    @Order(4)
    @Test
    @DisplayName("Détection de l'adversaire")
    public void testOpponentDetection() {
        assertEquals("Bob", combat.getOpponent("Alice"));
        assertEquals("Alice", combat.getOpponent("Bob"));
    }

    @Order(5)
    @Test
    @DisplayName("Ajout au journal de combat")
    public void testAddLog() {
        combat.addLog("Alice frappe Bob");
        List<String> log = combat.getLog();
        assertEquals(1, log.size());
        assertTrue(log.get(0).contains("Tour 1 - Alice frappe Bob"));
    }

    @Order(6)
    @Test
    @DisplayName("Fin du combat quand PV <= 0")
    public void testFinishCondition() {
        combat.setPv("Bob", 0);
        assertTrue(combat.isFinished());
    }

    @Order(7)
    @Test
    @DisplayName("Accès au sac et au coffre")
    public void testBackpackAndChest() {
        List<ObjectBase> aliceBackpack = combat.getBackpack("Alice");
        assertEquals(1, aliceBackpack.size());

        List<ObjectBase> aliceChest = combat.getChest("Alice");
        assertEquals(1, aliceChest.size());

        List<ObjectBase> bobChest = combat.getChest("Bob");
        assertTrue(bobChest.isEmpty());
    }

    @Order(8)
    @Test
    @DisplayName("Définir gagnant et perdant")
    public void testSetWinnerLoser() {
        combat.setWinner("Alice");
        combat.setLoser("Bob");
        assertEquals("Alice", combat.getWinner());
        assertEquals("Bob", combat.getLoser());
    }

    @Order(9)
    @Test
    @DisplayName("Affichage de l'état du combat")
    public void testCombatDisplayed() {
        assertFalse(combat.isCombatDisplayed());
        combat.setCombatDisplayed(true);
        assertTrue(combat.isCombatDisplayed());
    }






}
