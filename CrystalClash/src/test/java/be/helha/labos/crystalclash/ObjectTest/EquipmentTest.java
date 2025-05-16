package be.helha.labos.crystalclash.ObjectTest;

import be.helha.labos.crystalclash.Object.Equipment;
import be.helha.labos.crystalclash.Object.ObjectBase;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EquipmentTest {
    private Equipment equipment;

    @BeforeEach
    void setUp() {
        equipment = new Equipment();
    }

    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }

    @Order(1)
    @Test
    @DisplayName("Ajout d'une armure dans l'equipement vide")
    public void testAddArmor() {
        ObjectBase obj = new ObjectBase("Armure du Chaos",70,15,15);
        boolean result = equipment.AddArmor(obj);
        assertTrue(result);
        assertEquals(1, equipment.getObjets().size());
        assertEquals(obj, equipment.getObjets().get(0));
    }

    @Order(2)
    @Test
    @DisplayName("Ne pas ajouter si capacité dépassée")
    public void testAddArmorInFull() {
        //Remplir l inventaire pour le test
        for (int i = 0; i < equipment.getCapaciteMax(); i++) {
            equipment.AddArmor(new ObjectBase("Armure du Chaos",70,15,15));
        }
        assertEquals(1, equipment.getObjets().size());

        //Et ici on essaie d'en rajouter un
        boolean result = equipment.AddArmor(new ObjectBase("Armure du Chaos",70,15,15));
        assertFalse(result);
        assertEquals(1, equipment.getObjets().size());
    }

    @Order(3)
    @Test
    @DisplayName("Retirer une armure existante")
    public void testRemoveExistArmor() {
        ObjectBase obj = new ObjectBase("Armure du Chaos",70,15,15);
        equipment.AddArmor(obj);
        boolean result = equipment.removeArmor(obj);
        assertTrue(result);
        assertTrue(equipment.getObjets().isEmpty());
    }

}
