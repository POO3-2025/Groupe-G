package be.helha.labos.crystalclash.ServiceTest;


import be.helha.labos.crystalclash.Characters.Personnage;
import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAO.CharacterDAO;
import be.helha.labos.crystalclash.DAO.FightDAO;
import be.helha.labos.crystalclash.DAO.InventoryDAO;
import be.helha.labos.crystalclash.DAO.UserDAO;
import be.helha.labos.crystalclash.DAOImpl.CharacterDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.FightDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.InventoryDAOImpl;
import be.helha.labos.crystalclash.DAOImpl.UserDAOImpl;
import be.helha.labos.crystalclash.DTO.StateCombat;
import be.helha.labos.crystalclash.Factory.CharactersFactory;
import be.helha.labos.crystalclash.Object.*;
import be.helha.labos.crystalclash.Service.CharacterService;
import be.helha.labos.crystalclash.Service.FightService;
import be.helha.labos.crystalclash.Service.InventoryService;
import be.helha.labos.crystalclash.Service.UserService;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNull;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

public class FightServiceTest {

    private FightService fightService;


    private final String user1 = "userTes1";
    private final String user2 = "userTes2";


    @BeforeEach
    public void setUp() throws Exception {
        var config = ConfigManager.getInstance().getConfig();
        var db = config.getAsJsonObject("db");

        var mysqlProductionConfig = db.getAsJsonObject("mysqlproduction");
        var mysqlTestConfig = db.getAsJsonObject("mysqltest");

        mysqlProductionConfig.getAsJsonObject("BDCredentials")
            .entrySet()
            .forEach(entry -> {
                String key = entry.getKey();
                mysqlProductionConfig.getAsJsonObject("BDCredentials")
                    .add(key, mysqlTestConfig.getAsJsonObject("BDCredentials").get(key));
            });

        var mongoProductionConfig = db.getAsJsonObject("MongoDBProduction");
        var mongoTestConfig = db.getAsJsonObject("MongoDBTest");

        //Accede aux credential de mongo
        mongoProductionConfig.getAsJsonObject("BDCredentials")
            .entrySet()//Récupe les clées, valeurs
            .forEach(entry -> { //Pour chaque clées
                String key = entry.getKey();  //ça récup la clé (hostname,...)
                mongoProductionConfig.getAsJsonObject("BDCredentials")
                    .add(key, mongoTestConfig.getAsJsonObject("BDCredentials").get(key)); //Et ça remplace la valeur prod pas celle de test
            });

        //Implementation Dao manuellement
        CharacterDAO characterDAO = new CharacterDAOImpl();
        UserDAO userDAO = new UserDAOImpl();
        InventoryDAO inventoryDAO = new InventoryDAOImpl();
        FightDAO fightDAO = new FightDAOImpl();

        //Service ici
        CharacterService characterService = new CharacterService(characterDAO);
        UserService userService = new UserService(userDAO);
        InventoryService inventoryService = new InventoryService(inventoryDAO);
        inventoryService.setUserService(userService);

        fightService = new FightService(fightDAO);
        fightService.setCharacterService(characterService);
        fightService.setUserService(userService);
        fightService.setInventoryService(inventoryService);


    }

    /*
     * Insertion d un user test pour le test userExist
     * */
    @BeforeEach
    public void ensureUserExists() throws Exception {
        // Crée un utilisateur test en base MySQL
        var conn = ConfigManager.getInstance().getSQLConnection("mysqltest");
        var stmt = conn.prepareStatement("""
            INSERT INTO users (username, password, level, cristaux, is_connected,gagner,perdu,Winconsecutive	)
          VALUES (?, ?, ?, ?, ?, ?, ?,?)
            ON DUPLICATE KEY UPDATE cristaux = VALUES(cristaux), level = VALUES(level)
        """);
        stmt.setString(1, "userTes1");
        stmt.setString(2, "password");
        stmt.setInt(3, 5);
        stmt.setInt(4, 100);
        stmt.setBoolean(5, false);
        stmt.setInt(6, 0); // valeur par défaut pour 'gagner'
        stmt.setInt(7, 0); // valeur par défaut pour 'perdu'
        stmt.setInt(8, 0);
        stmt.executeUpdate();
        stmt.close();
        conn.close();

        var conn2 = ConfigManager.getInstance().getSQLConnection("mysqltest");
        var stmt2 = conn2.prepareStatement("""
            INSERT INTO users (username, password, level, cristaux, is_connected,gagner,perdu,Winconsecutive)
    VALUES (?, ?, ?, ?, ?, ?, ?,?)
ON DUPLICATE KEY UPDATE cristaux = VALUES(cristaux), level = VALUES(level)
        """);
        stmt2.setString(1, "userTes2");
        stmt2.setString(2, "password");
        stmt2.setInt(3, 5);
        stmt2.setInt(4, 100);
        stmt2.setBoolean(5, false);
        stmt2.setInt(6, 0); // valeur par défaut pour 'gagner'
        stmt2.setInt(7, 0); // valeur par défaut pour 'perdu'
        stmt2.setInt(8, 0);
        stmt2.executeUpdate();
        stmt2.close();
        conn2.close();

        MongoDatabase mongo = ConfigManager.getInstance().getMongoDatabase("MongoDBTest");
        Document docu = new Document("username", "userTes1")
            .append("type", "troll")
            .append("backpack", "backpack")
            .append("selected", true);
        mongo.getCollection("Characters").insertOne(docu);

        Document docu2 = new Document("username", "userTes2")
            .append("type", "elf")
            .append("backpack", "backpack")
            .append("selected", true);
        mongo.getCollection("Characters").insertOne(docu2);
    }



    //Pour les noms des tests
    @BeforeEach
    public void displayTestName(TestInfo testInfo) {
        System.out.println("Exécution du test : " + testInfo.getDisplayName());
    }


    @Test
    @Order(1)
    @DisplayName("Test créer un combat et attaquer")
    public void TestCreateFightAndAttack(){

        //Création 2 persos
        Personnage p1 = CharactersFactory.getCharacterByType("elf");
        Personnage p2 = CharactersFactory.getCharacterByType("troll");

        //PAs objet dans le bp
        List<ObjectBase> bp1 = List.of();
        List<ObjectBase> bp2 = List.of();

        //Creation du combat
        fightService.createCombat(user1,user2,p1,p2,bp1,bp2);

        //Vérif combat enregistré
        StateCombat combat = fightService.getCombat(user1);
        assertNotNull(combat, "le combat devrait etre présent");
        assertEquals(user1, combat.getPlayerNow(), "Le joueur 1 devarait commencer");

        int pvInitialOppenent = combat.getPv(user2);

        //Attaque normal du user1
        fightService.HandleAttach(user1,"normal");

        //Assert pv user2 diminués
        StateCombat combat2 = fightService.getCombat(user2);
        int pvAfterAttack = combat2.getPv(user2);

        assertTrue(pvAfterAttack < pvInitialOppenent, "Les PV de l'adversaire doivent avoir baissé.");
        assertEquals(user2, combat2.getPlayerNow(), "C'est maintenant au joueur 2 de jouer.");
    }

    @Test
    @Order(2)
    @DisplayName("Test utiliser un objet")
    public void TestUseObject() throws Exception {

        //Création 2 persos
        Personnage p1 = CharactersFactory.getCharacterByType("elf");
        Personnage p2 = CharactersFactory.getCharacterByType("troll");

        Weapon weapon = new Weapon("Epee en bois",23,1,2,5);
        weapon.setId("test");

        List<ObjectBase> bp1 = List.of(weapon);
        List<ObjectBase> bp2 = List.of();

        //Cree combat
        fightService.createCombat(user1,user2,p1,p2,bp1,bp2);
        fightService.useObject(user1, "test");

        //CHercher etat combat pour user2
        StateCombat combat = fightService.getCombat(user2);
        assertTrue(combat.getPv(user2) < p2.getPV()); //verif si pv bien diminués

    }

    @Test
    @Order(3)
    @DisplayName("Test Forfait d'un joueur")
    public void TestForfaitFight() throws Exception {

        Personnage p1 = CharactersFactory.getCharacterByType("elf");
        Personnage p2 = CharactersFactory.getCharacterByType("troll");

        List<ObjectBase> bp1 = List.of();
        List<ObjectBase> bp2 = List.of();

        fightService.createCombat(user1, user2, p1, p2, bp1, bp2);

        //USer1 abandonne
        fightService.forfait(user1);

        //1ere appel combat tjrs visible
        StateCombat combat = fightService.getCombat(user1);
        assertNotNull(combat, "Le combat doit être visible une première fois après le forfait");

        //Verif bon gagnant
        assertEquals(user2, combat.getWinner(), "Le gagnant doit être user2");
        assertEquals(user1, combat.getLoser(), "Le perdant doit être user1");

        //2eme appel combat supprimé
        StateCombat combatFinal = fightService.getCombat(user2);
        assertNull(combatFinal, "Le combat doit être supprimé après affichage");
    }

    @Test
    @Order(4)
    @DisplayName("Test attaque spéciale après nombre de tours requis")
    public void TestSpecialAttackAfterRequiredTurns() {

        // Création des personnages avec attaques configurées
        Personnage p1 = CharactersFactory.getCharacterByType("elf");
        Personnage p2 = CharactersFactory.getCharacterByType("troll");

        // forcer p1 à avoir assez de tours pour faire une attaque spéciale
        p1.CompteurAttack(p1.getRestrictionAttackSpecial()); // simu pour attack psecial ok

        List<ObjectBase> bp1 = List.of();
        List<ObjectBase> bp2 = List.of();

        // Création du combat
        fightService.createCombat(user1, user2, p1, p2, bp1, bp2);

        // Récup PV initiaux
        int pvInitial = fightService.getCombat(user1).getPv(user2);

        // Lancer attaque spéciale
        fightService.HandleAttach(user1, "special");

        // Vérif que dégâts ont été appliqués
        StateCombat combat = fightService.getCombat(user2);
        int pvAfterSpecial = combat.getPv(user2);

        assertTrue(pvAfterSpecial < pvInitial, "L'attaque spéciale doit infliger des dégâts");
        assertEquals(user2, combat.getPlayerNow(), "C'est maintenant à l'adversaire de jouer.");
    }

    @Test
    @Order(5)
    @DisplayName("Test attaque spéciale refusée si compteur insuffisant")
    public void TestSpecialAttackFailsIfNotReady() {
        Personnage p1 = CharactersFactory.getCharacterByType("elf");
        Personnage p2 = CharactersFactory.getCharacterByType("troll");

        // Forcer un compteur insuffisant
        p1.CompteurAttack(0); //0 donc trop tot

        List<ObjectBase> bp1 = List.of();
        List<ObjectBase> bp2 = List.of();

        fightService.createCombat(user1, user2, p1, p2, bp1, bp2);

        // PV de référence avant attaque spéciale refusée
        int pvAvant = fightService.getCombat(user1).getPv(user2);

        // Essayer attaque spéciale, rien faire en toute logique
        fightService.HandleAttach(user1, "special");

        // PV après tentative
        int pvApres = fightService.getCombat(user1).getPv(user2);

        // pas de prte de pv attack spe pas diospo
        assertEquals(pvAvant, pvApres, "L'attaque spéciale ne doit pas infliger de dégâts si le compteur est insuffisant");

        //tour doit pas etre incrementé
        assertEquals(user1, fightService.getCombat(user1).getPlayerNow(), "Le joueur conserve son tour après une attaque spéciale refusée");
    }

    @Test
    @Order(6)
    @DisplayName("Test victoire par KO (PV à 0)")
    public void testVictoireParKO() throws Exception {
        Personnage p1 = CharactersFactory.getCharacterByType("elf");
        Personnage p2 = CharactersFactory.getCharacterByType("troll");

        // Attack spe pour kill vite l adversaire
        p1.CompteurAttack(p1.getRestrictionAttackSpecial());

        // pv a 10 pour kill vite
        p2.setPV(5);

        List<ObjectBase> bp1 = List.of();
        List<ObjectBase> bp2 = List.of();

        fightService.createCombat(user1, user2, p1, p2, bp1, bp2);

        // lancer attack qui devrait vite kill l adversaire
        fightService.HandleAttach(user1, "special");

        //Premier appel combat encore visible
        StateCombat visible = fightService.getCombat(user2);
        assertNotNull(visible, "Le combat doit encore être visible une fois");

        // 2 appel : combat sup logiquement
        StateCombat shouldBeNull = fightService.getCombat(user2);
        assertNull(shouldBeNull, "Le combat doit être supprimé après affichage final");

        // verif bien que le dernier gagnant est enregistré
        String gagnant = fightService.getLastWinner(user1);
        assertEquals(user1, gagnant, "user1 doit être le gagnant enregistré");
    }

    @Test
    @Order(7)
    @DisplayName("TTest échec si objet inexistant utilisé")
    public void testUseInvalidObjectFailsGracefully() throws Exception {

        Personnage p1 = CharactersFactory.getCharacterByType("elf");
        Personnage p2 = CharactersFactory.getCharacterByType("troll");

        List<ObjectBase> bp1 = List.of();
        List<ObjectBase> bp2 = List.of();

        fightService.createCombat(user1, user2, p1, p2, bp1, bp2);

        int pvAvant = fightService.getCombat(user1).getPv(user2);


        // use un ID d’objet inexistant
        fightService.useObject(user1, "objetQuiExistePas");

        // Les PV ne doivent pas avoir changé car pas object dispo
        int pvApres = fightService.getCombat(user1).getPv(user2);
        assertEquals(pvAvant, pvApres, "L'utilisation d'un objet inexistant ne doit rien faire");

        // Le tour ne doit pas avoir changé non plus car pas eu d'attaqUE
        assertEquals(user1, fightService.getCombat(user1).getPlayerNow(), "Le tour ne doit pas changer après une erreur d’objet");
    }

    @Test
    @Order(8)
    @DisplayName("Test victoire par KO avec objet du CoffreDesJoyaux")
    public void testKOAvecObjetDuCoffre() throws Exception {


        Personnage p1 = CharactersFactory.getCharacterByType("elf");
        Personnage p2 = CharactersFactory.getCharacterByType("troll");


        // pv a 5 pour kill vite
        p2.setPV(5);

        // Création de l’arme dans le coffre
        Weapon weapon = new Weapon("Epee en bois",23,1,2,5);
        weapon.setId("weapon");

        CoffreDesJoyaux coffre = new CoffreDesJoyaux();
        coffre.setId("coffre1");
        coffre.setContenu(List.of(weapon));


        // Le coffre est mis dans le backpack
        List<ObjectBase> bp1 = List.of(coffre);
        List<ObjectBase> bp2 = List.of();

        // Création du combat
        fightService.createCombat(user1, user2, p1, p2, bp1, bp2);

        // Utilisation de l'objet contenu dans le coffre
        fightService.useObject(user1, "weapon");


        // 1er combat encore visible (doit PAS être null)
        StateCombat combatVisible = fightService.getCombat(user2);
        assertNotNull(combatVisible, "Le combat doit encore être visible une fois");

// 2e cette fois supprimé
        StateCombat supprimé = fightService.getCombat(user2);
        assertNull(supprimé, "Le combat doit être supprimé après affichage");

    }
    @Test
    @Order(9)
    @DisplayName("Test effet de l'armure : bonus PV et réduction de fiabilité")
    public void testAeffectArmorAndreliability() throws Exception {
        // Création des personnages
        Personnage p1 = CharactersFactory.getCharacterByType("elf");
        Personnage p2 = CharactersFactory.getCharacterByType("troll");

        // créa d'une armure pour p2 avec fiabilité 2 et bonus de 10 PV
        Armor armure = new Armor("Armure de test", 10, 2,2,10);
        Equipment equipement = new Equipment();
        equipement.AddArmor(armure);

        // Sauvegarde de l'équipement pour user2
        fightService.getCharacterService().saveEquipmentForCharacter(user2, equipement);


        // PV initiaux du personnage sans armure
        int pvSansBonus = p2.getPV();

        // Création du combat
        fightService.createCombat(user1, user2, p1, p2, List.of(), List.of());

        // Vérifie que les PV du joueur 2 ont été augmentés par l'armure
        StateCombat combat = fightService.getCombat(user2);
        assertEquals(pvSansBonus + 10, combat.getPv(user2), "Les PV doivent inclure le bonus d'armure");

        // L'utilisateur 1 attaque avec une attaque normale
        fightService.HandleAttach(user1, "normal");

        // Recharge l'équipement pour vérifier la fiabilité de l'armure
        Equipment equipementApres = fightService.getCharacterService().getEquipmentForCharacter(user2);
        ObjectBase armureApres = equipementApres.getObjets().get(0);


        // 2eme attaque pour casser l’armure
        fightService.HandleAttach(user1, "normal");

        Equipment equipementFinal = fightService.getCharacterService().getEquipmentForCharacter(user2);
        ObjectBase armureFinale = equipementFinal.getObjets().get(0);

        // verif que les logs contiennent bien les messages sur la fiabilité
        List<String> logs = combat.getLog();
        boolean logFiabilitePresente = logs.stream().anyMatch(log -> log.contains("fiabilité"));
        assertTrue(logFiabilitePresente, "Un message sur la fiabilité de l'armure doit apparaître dans les logs");
    }
    @Test
    @Order(10)
    @DisplayName("Test utiliser une potion de soin")
    public void TestHealingPotion() throws Exception {
        Personnage p1 = CharactersFactory.getCharacterByType("elf");
        Personnage p2 = CharactersFactory.getCharacterByType("troll");

        //  réduit  les PV de p1
        p1.setPV(p1.getPV() - 20);

        HealingPotion potion = new HealingPotion("Petite potion", 15, 1, 2);
        potion.setId("potion");

        List<ObjectBase> bp1 = new ArrayList<>();
        bp1.add(potion);
        List<ObjectBase> bp2 = new ArrayList<>();

        fightService.createCombat(user1, user2, p1, p2, bp1, bp2);

        int pvAvant = fightService.getCombat(user1).getPv(user1);

        fightService.useObject(user1, "potion");

        int pvApres = fightService.getCombat(user1).getPv(user1);
        assertTrue(pvApres > pvAvant, "La potion doit soigner le joueur");

        // Vérifie qu'elle est bien supprimée après 1 utilisation
        Equipment equipFinal = fightService.getCharacterService().getEquipmentForCharacter(user1);
        assertTrue(fightService.getCombat(user1).getBackpack(user1).isEmpty(), "La potion doit être retirée après usage");
    }
    @Test
    @Order(10)
    @DisplayName("Test utiliser une potion de force applique bonus sur attaque")
    public void TestPotionOfStrenght() throws Exception {
        Personnage p1 = CharactersFactory.getCharacterByType("elf");
        Personnage p2 = CharactersFactory.getCharacterByType("troll");

        PotionOfStrenght potion = new PotionOfStrenght("grosse potion", 15, 1, 15);
        potion.setId("potion");

        List<ObjectBase> bp1 = new ArrayList<>(List.of(potion));
        List<ObjectBase> bp2 = new ArrayList<>();
        fightService.getCharacterService().saveEquipmentForCharacter(user2, new Equipment());

        fightService.createCombat(user1, user2, p1, p2, bp1, bp2);

        // force userTes1 à rester actif après la potion
        StateCombat state = fightService.getCombat(user1);
        int pvAvant = state.getPv(user2);

        // us la potion SANS faire NextTurn
        fightService.useObject(user1, "potion");

        //  restaur manuellement le tour car useObject() le fait passer à l'autre joueur
        fightService.getCombat(user1).NextTurn();


        //Attaque avec le bonus
        fightService.HandleAttach(user1, "normal");

        StateCombat combatFinal = fightService.getCombat(user2);
        assertNotNull(combatFinal, "Le combat ne doit pas être supprimé immédiatement");

        int pvApres = combatFinal.getPv(user2);
        int dmgInflige = pvAvant - pvApres;

        int minAttendu = p1.getAttackBase() + potion.getBonusATK();

        assertTrue(dmgInflige >= minAttendu,
            "Les dégâts doivent inclure le bonus de la potion de force (min " + minAttendu + ", mais infligé " + dmgInflige + ")");

        assertTrue(combatFinal.getBackpack(user1).isEmpty(),
            "La potion doit être retirée après usage");
    }

    @Test
    @Order(11)
    @DisplayName("Test resolveWinnerAndLoser applique bien le gagnant et le perdant")
    public void testResolveWinnerAndLoserIndirect() throws Exception {

        Personnage p1 = CharactersFactory.getCharacterByType("elf");
        Personnage p2 = CharactersFactory.getCharacterByType("troll");

        p2.setPV(5);//pv p2 a 5
        p1.CompteurAttack(p1.getRestrictionAttackSpecial()); // permet attack spé

        fightService.createCombat(user1, user2, p1, p2, List.of(), List.of());

        // fait l'attack spe qui met KO le p2
        fightService.HandleAttach(user1, "special");

        // premier appel du combat ça ne sup pas encore la
        StateCombat combat = fightService.getCombat(user1);
        assertNotNull(combat, "Le combat devrait encore être visible une fois");

        // veri que resolveWinnerAndLoser a bien mis les valeurs
        assertEquals(user1, combat.getWinner(), "user1 doit être défini comme gagnant");
        assertEquals(user2, combat.getLoser(), "user2 doit être défini comme perdant");
    }
    @Test
    @Order(12)
    @DisplayName("Test resolveWinnerAndLoser déclenché par forfait")
    public void testResolveWinnerAndLoserParForfait() throws Exception {
        Personnage p1 = CharactersFactory.getCharacterByType("elf");
        Personnage p2 = CharactersFactory.getCharacterByType("troll");

        fightService.createCombat(user1, user2, p1, p2, List.of(), List.of());

        // user1 abandonne
        fightService.forfait(user1);

        // 1ere appel = combat visible avec winner et loser
        StateCombat combat = fightService.getCombat(user2);
        assertNotNull(combat, "Le combat doit être encore visible une fois");

        assertEquals(user2, combat.getWinner(), "Le gagnant doit être user2");
        assertEquals(user1, combat.getLoser(), "Le perdant doit être user1");
    }

    @Test
    @Order(13)
    @DisplayName("Test getArmure retourne bonus total des armures")
    public void testGetArmureReturnsCorrectBonus() {
        Armor armor1 = new Armor("Armure légère", 10, 1, 5, 10); // bonusPV = 10
        Armor armor2 = new Armor("Armure lourde", 20, 1, 10, 20); // bonusPV = 20
        Equipment equip = new Equipment();
        equip.setObjets(List.of(armor1, armor2));

        // Simule que le joueur a cet équipement
        fightService.getCharacterService().saveEquipmentForCharacter(user1, equip);

        // Vérifie le bonus total
        int totalBonus = fightService.getArmure(user1);
        assertEquals(10, totalBonus, "Le total du bonus PV doit être 10");
    }


    @Test
    @Order(15)
    @DisplayName("Test getArmoRelibility retourne bonne valeur")
    public void testGetArmoRelibilityReturnsCorrectValue() {
        Armor armor = new Armor("Armure moyenne", 10, 1, 5, 5); // Fiabilité = 8
        Equipment equip = new Equipment();
        equip.setObjets(List.of(armor));
        fightService.getCharacterService().saveEquipmentForCharacter(user1, equip);

        int reliability = fightService.getArmoRelibility(user1);
        assertEquals(5, reliability, "La fiabilité retournée doit être 8");
    }
    @Test
    @Order(14)
    @DisplayName("Test userArmorReliability réduit bien la fiabilité")
    public void testUserArmorReliabilityReducesReliability() {
        Personnage p1 = CharactersFactory.getCharacterByType("elf");
        Personnage p2 = CharactersFactory.getCharacterByType("troll");
        Armor armor = new Armor("Armure fragile", 10, 1, 5, 2); // Fiabilité = 2
        Equipment equip = new Equipment();
        equip.setObjets(List.of(armor));
        fightService.getCharacterService().saveEquipmentForCharacter(user1, equip);

        StateCombat combat =  new  StateCombat(user1, user2, p1, p2, List.of(), List.of());
        fightService.userArmorReliability(user1, combat);

        Equipment updatedEquip = fightService.getCharacterService().getEquipmentForCharacter(user1);
        Armor updatedArmor = (Armor) updatedEquip.getObjets().get(0);

        // La fiabilité doit avoir diminué de 4
        assertEquals(4, updatedArmor.getReliability(), "La fiabilité doit diminuer de 1");

        // Les logs doivent contenir l'info
        boolean logCorrect = combat.getLog().stream().anyMatch(log -> log.contains("fiabilité"));
        assertTrue(logCorrect, "Les logs doivent contenir l'info de perte de fiabilité");
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

