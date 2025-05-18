package be.helha.labos.crystalclash.ObjectTest;

import be.helha.labos.crystalclash.Object.CoffreDesJoyaux;
import be.helha.labos.crystalclash.Object.ObjectBase;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
public class CoffreDesJoyauxTest {

    private CoffreDesJoyaux coffreDesJoyaux;

    @BeforeEach
    void setUp() {
        coffreDesJoyaux = new CoffreDesJoyaux();
    }

    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }

    @Test
    @Order(1)
    @DisplayName("Test méthode use > 0")
    public void testUsedWhenUsable(){
        ObjectBase obj = new ObjectBase("fusil", 15, 5, 100);
        boolean result = coffreDesJoyaux.AddObjects(obj);
        assertTrue(result);
        assertEquals(1, coffreDesJoyaux.getContenu().size());
    }

    @Order(2)
    @Test
    @DisplayName("Ajout d'un objet dans le BackPacj vide")
    public void testAddObject() {
        ObjectBase obj = new ObjectBase("Epee en bois",15,5,5);
        boolean result = coffreDesJoyaux.AddObjects(obj);
        assertTrue(result);
        assertEquals(1, coffreDesJoyaux.getContenu().size());
        assertEquals(obj, coffreDesJoyaux.getContenu().get(0));
    }


    @Order(3)
    @Test
    @DisplayName("Ne pas ajouter si capacité dépassée")
    public void testAddObjectInFull() {
        //Remplir l invetaire pour le test
        for (int i = 0; i < coffreDesJoyaux.getMaxCapacity(); i++) {
            coffreDesJoyaux.AddObjects(new ObjectBase("fusil",15,5,100));
        }
        assertEquals(10, coffreDesJoyaux.getContenu().size());

        //Et ici on essaie d'en rajouter un
        boolean result = coffreDesJoyaux.AddObjects(new ObjectBase("fusil",15,5,100));
        assertFalse(result);
        assertEquals(10, coffreDesJoyaux.getContenu().size());
    }

    @Order(4)
    @Test
    @DisplayName("Test getter getContenu()")
    void testGetContenu() {
        assertNotNull(coffreDesJoyaux.getContenu());
        assertTrue(coffreDesJoyaux.getContenu().isEmpty());
    }

    @Order(5)
    @Test
    @DisplayName("Test getter de capacité")
    void testGetMaxCapacity() {
        assertEquals(10, coffreDesJoyaux.getMaxCapacity());
    }



}
