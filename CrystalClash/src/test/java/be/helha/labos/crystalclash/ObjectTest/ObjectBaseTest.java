package be.helha.labos.crystalclash.ObjectTest;

import be.helha.labos.crystalclash.Object.ObjectBase;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
public class ObjectBaseTest {

    private ObjectBase base;

    @BeforeEach
    void setUp() {
        base = new ObjectBase();
    }

    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }

    @Test
    @Order(1)
    @DisplayName("Test du constructeur et des getters")
    public void testConstructeurEtGetters(){

        ObjectBase base = new ObjectBase("Epee en bois",15,5,5);
        assertEquals("Epee en bois", base.getName());
        assertEquals(15,base.getPrice());
        assertEquals(5,base.getRequiredLevel());
        assertEquals(5,base.getReliability());
    }

    @Test
    @Order(2)
    @DisplayName("Test des setters")
    public void testSetters(){
        ObjectBase base = new ObjectBase("Epee en bois",15,5,5);
         base.setName("Epee en bois");
         base.setPrice(15);
         base.setRequiredLevel(5);
         base.setReliability(5);

         assertEquals("Epee en bois", base.getName());
         assertEquals(15,base.getPrice());
         assertEquals(5,base.getRequiredLevel());
         assertEquals(5,base.getReliability());

    }

    @Test
    @Order(3)
    @DisplayName("Test de la méthodes use")
    public void test_use(){

        ObjectBase base = new ObjectBase("Potion", 10, 1, 3);
        assertEquals("Objet utilisé.", base.use());
    }

    @Test
    @Order(4)
    @DisplayName("Test de la méthodes Is used")
    public void test_IsUsed(){

        ObjectBase base = new ObjectBase("Potion", 10, 1, 3);
        assertTrue(base.IsUsed(),"L'objet avec fiabilité > 0 doit être utilié");

        base.setReliability(0);
        assertFalse(base.IsUsed(),"L'objet avec fiabilité = 0 ne doit pas être utilisable");
    }

    @Test
    @Order(5)
    @DisplayName("Test de la méthodes Reducereliability")
    public void test_Reducereliability(){
        ObjectBase base = new ObjectBase("Potion", 10, 1, 2);

        base.Reducereliability();
        assertEquals(1,base.getReliability(), "La fiabilité doit diminuer de 1");

        base.Reducereliability();
        assertEquals(0,base.getReliability(),"La fiabilité doit encore diminuer ");

        base.Reducereliability();
        assertEquals(0,base.getReliability(),"La fiabilité ne doit devenir négative");

    }

}
