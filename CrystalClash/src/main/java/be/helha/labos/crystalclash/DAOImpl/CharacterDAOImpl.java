package be.helha.labos.crystalclash.DAOImpl;

import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAO.CharacterDAO;
import be.helha.labos.crystalclash.Inventory.Inventory;
import be.helha.labos.crystalclash.Object.*;
import be.helha.labos.crystalclash.Service.CharacterService;
import be.helha.labos.crystalclash.Service.InventoryService;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CharacterDAOImpl implements CharacterDAO {
    @Autowired
    private InventoryService inventoryService;

    /**
     * Récupère le type de personnage pour un utilisateur
     * @param username Nom d'utilisateur
     * @return Le type de personnage
     */
    @Override
    public String getCharacterForUser(String username) {
        MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
        MongoCollection<Document> collection = mongoDB.getCollection("Characters");
        Document doc = collection.find(new Document("username", username)).first();
        return (doc != null) ? doc.getString("type") : null;
    }


    /**
     * Sauvegarde le personnage pour un utilisateur
     * @param username Nom d'utilisateur
     * @param characterType Type de personnage
     */
    /*
     * Modif apportées, reiteré la ligne qui supprimait les anciens persos du joueur
     * ajoute le perso si il n'existe pas if (existing == null)
     * */
    @Override
    public void saveCharacterForUser(String username, String characterType) {
        MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
        MongoCollection<Document> collection = mongoDB.getCollection("Characters");
        Document doc = new Document("username", username).append("type", characterType);
        Document existing = collection.find(doc).first();

        if (existing == null) {
            // Ajout du personnage + backpack vide
            Document backpack = new Document("objets", List.of());
            Document docu = new Document("username", username)
                    .append("type", characterType)
                    .append("backpack", backpack);
            collection.insertOne(doc);
        }
    }

    /**
     * Crée un backPack pour un nouveau perso choisi
     * si deja BackPack alors on le garde
     * @param username Nom d'utilisateur
     * @param characterType Type de personnage
     */
    @Override
    public void createBackPackForCharacter(String username, String characterType) {
        MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
        MongoCollection<Document> collection = mongoDB.getCollection("Characters");

        // Filtre  retrouver le document du perso
        Document filtre = new Document("username", username).append("type", characterType);
        Document doc = collection.find(filtre).first();

        // S'il existe déjà et qu'il a un backpack, RIen faire
        if (doc != null && doc.containsKey("backpack")) {
            return; //si existe deja retourn rien
        }
        //Si pas deja, crée un
        Document backpack = new Document("objets", List.of());
        Document update = new Document("$set", new Document("backpack", backpack));
        collection.updateOne(filtre, update);
    }

    /**
     * Récupère le backpack du personnage du joueur
     * @param username Nom d'utilisateur
     * @return Le backpack du personnage
     */
    @Override
    public BackPack getBackPackForCharacter(String username) {
        try {
            MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
            MongoCollection<Document> collection = mongoDB.getCollection("Characters");

            Document doc = collection.find(new Document("username", username)).first();
            if (doc != null && doc.containsKey("backpack")) {
                Document backpackDoc = (Document) doc.get("backpack");
                List<Document> objetsDocs = (List<Document>) backpackDoc.get("objets");

                BackPack backpack = new BackPack();

                for (Document objDoc : objetsDocs) {
                    if (objDoc == null) continue;

                    String type = objDoc.getString("type");
                    String name = objDoc.getString("name");
                    int price = objDoc.getInteger("price", 0);
                    int requiredLevel = objDoc.getInteger("requiredLevel", 0);
                    int reliability = objDoc.getInteger("reliability", 0);

                    ObjectBase obj = null;

                    switch (type != null ? type.toLowerCase() : "") {
                        case "weapon" -> {
                            Weapon weapon = new Weapon(name, price, requiredLevel, reliability, objDoc.getInteger("damage", 0));
                            weapon.setType("Weapon");
                            obj = weapon;
                        }
                        case "armor" -> {
                            Armor armor = new Armor(name, price, requiredLevel, reliability, objDoc.getInteger("bonusPV", 0));
                            armor.setType("Armor");
                            obj = armor;
                        }
                        case "healingpotion" -> {
                            HealingPotion potion = new HealingPotion(name, price, requiredLevel, objDoc.getInteger("heal", 0));
                            potion.setType("HealingPotion");
                            obj = potion;
                        }
                        case "potionofstrenght" -> {
                            PotionOfStrenght potion = new PotionOfStrenght(name, price, requiredLevel, objDoc.getInteger("bonusATK", 0));
                            potion.setType("PotionOfStrenght");
                            obj = potion;
                        }
                        case "coffredesjoyaux" -> {
                            CoffreDesJoyaux coffre = new CoffreDesJoyaux();
                            coffre.setType("CoffreDesJoyaux");
                            obj = coffre;
                        }
                        default -> {
                            System.err.println("Type inconnu ou null pour objet '" + name + "'");
                        }
                    }

                    if (obj != null) {
                        backpack.ajouterObjet(obj);
                    }
                }

                return backpack;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new BackPack(); // retourne un backpack vide si erreur ou pas trouvé
    }

    /**
     * Met a jour le personnage sélectionné pour un utilisateur
     * @param username Nom d'utilisateur
     * @param characterType Type de personnage
     */
    @Override
    public void setSelectedCharacter(String username, String characterType) {
        MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
        MongoCollection<Document> collection = mongoDB.getCollection("Characters");

        //  désélectionner tous les persos de ce joueur
        //Prend tous les persos du user et met tout a faux
        //Met tout a faux car en passant par l'update mondo c'est lequel est selectionné avec le username et type
        collection.updateMany(
                new Document("username", username),
                new Document("$set", new Document("selected", false))
        );

        // Puis marquer le bon perso comme sélectionné
        //Cible le bon perso et son username pour le mettre a true
        //Mongo guidé avec ce filtre si dessous
        collection.updateOne(
                new Document("username", username).append("type", characterType),
                new Document("$set", new Document("selected", true))
        );
    }

    /**
     * sauve le backpack du personnage du joueur
     * @param username
     * @param backPack
     */
    @Override
    public void saveBackPackForCharacter(String username, BackPack backPack) {
        try {
            MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
            MongoCollection<Document> collection = mongoDB.getCollection("Characters");

            // Construction de la liste d'objets à insérer dans Mongo
            List<Document> objetsDocuments = new ArrayList<>();
            for (ObjectBase obj : backPack.getObjets()) {
                if (obj == null || obj.getType() == null) {
                    System.err.println("Objet null ou sans type dans le backpack de " + username);
                    continue;
                }

                Document doc = new Document()
                        .append("name", obj.getName())
                        .append("type", obj.getType())  // toujours présent
                        .append("price", obj.getPrice())
                        .append("requiredLevel", obj.getRequiredLevel());

                switch (obj.getType().toLowerCase()) {
                    case "weapon" -> {
                        Weapon weapon = (Weapon) obj;
                        doc.append("reliability", weapon.getReliability())
                                .append("damage", weapon.getDamage());
                    }
                    case "armor" -> {
                        Armor armor = (Armor) obj;
                        doc.append("reliability", armor.getReliability())
                                .append("bonusPV", armor.getBonusPV());
                    }
                    case "healingpotion" -> {
                        HealingPotion potion = (HealingPotion) obj;
                        doc.append("heal", potion.getHeal());
                    }
                    case "potionofstrenght" -> {
                        PotionOfStrenght potion = (PotionOfStrenght) obj;
                        doc.append("bonusATK", potion.getBonusATK());
                    }
                    case "coffredesjoyaux" -> {
                        // Pas de champs supplémentaires pour l’instant
                    }
                    default -> {
                        System.err.println("⚠️ Type d'objet inconnu : " + obj.getType());
                    }
                }

                objetsDocuments.add(doc);
            }

            // Construction et exécution de l’update
            Document filtre = new Document("username", username);
            Document backpackUpdate = new Document("objets", objetsDocuments);
            Document update = new Document("$set", new Document("backpack", backpackUpdate));

            collection.updateOne(filtre, update);

        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde du backpack : " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Ajoute un objet au backpack du personnage du joueur
     * @param username Nom d'utilisateur
     * @param name Nom de l'objet
     * @param type Type de l'objet
     * @return Réponse de l'API reponse
     */
    @Override
    public ApiReponse addObjectToBackPack(String username, String name, String type) {
        try {
            Inventory inventory = inventoryService.getInventoryForUser(username);
            BackPack backpack = getBackPackForCharacter(username); // 🔄 remplacement ici

            if (backpack.getObjets().size() >= 10) {
                return new ApiReponse("Backpack plein !", null);
            }

            ObjectBase objectToAdd = inventory.getObjets().stream()
                    .filter(obj -> obj.getName().equals(name) && obj.getType().equals(type))
                    .findFirst()
                    .orElse(null);

            if (objectToAdd == null) {
                return new ApiReponse("Objet non trouvé dans l'inventaire.", null);
            }

            if (!backpack.ajouterObjet(objectToAdd)) {
                return new ApiReponse("Erreur lors de l'ajout de l'objet au backpack.", null);
            }

            inventory.retirerObjet(objectToAdd);
            inventoryService.saveInventoryForUser(username, inventory);
            saveBackPackForCharacter(username, backpack);
            System.out.println("Objet ajouté au backpack : " + objectToAdd.getName());
            return new ApiReponse("Objet ajouté au backpack avec succès !", backpack);

        } catch (Exception e) {
            return new ApiReponse("Erreur lors de l'ajout de l'objet au backpack : " + e.getMessage(), null);
        }
    }
}