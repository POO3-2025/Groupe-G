package be.helha.labos.crystalclash.ObjectTest;


import be.helha.labos.crystalclash.Object.Armor;
import be.helha.labos.crystalclash.Object.HealingPotion;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class HealingPotionTest {

    private HealingPotion healingPotion;

    @BeforeEach
    void setup(){
        healingPotion = new HealingPotion("elixir daube", 50, 2, 15);
    }

    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }

    @Test
    @Order(1)
    @DisplayName("Test des getters HealingPotion")
    public void gettersArmor() {
        assertEquals("elixir daube",healingPotion.getName());
        assertEquals(50,healingPotion.getPrice());
        assertEquals(2,healingPotion.getRequiredLevel());
        assertEquals(15,healingPotion.getHeal());
    }

    @Test
    @Order(2)
    @DisplayName("Test méthode use > 0")
    public void testUsedWhenUsable(){
        String result = healingPotion.use();
        assertEquals("You recovered 15 PV", result);
        assertEquals(0,healingPotion.getReliability());
    }

    @Test
    @Order(3)
    @DisplayName("Test méthode use == 0")
    public void testUsedWhenNotUsable(){
        healingPotion.setReliability(0);
        String result = healingPotion.use();
        assertEquals("Potion already used", result);
    }

    @Test
    @Order(4)
    @DisplayName("Test du ToString")
    public void testToString(){
        assertTrue(healingPotion.toString().contains("Pv : 15"));
    }

    @Test
    @Order(5)
    @DisplayName("Test de getDetails() personnalisé")
    void testGetDetails() {
        String details = healingPotion.getDetails();
        assertTrue(details.contains("Heal : 15"));
    }
}
