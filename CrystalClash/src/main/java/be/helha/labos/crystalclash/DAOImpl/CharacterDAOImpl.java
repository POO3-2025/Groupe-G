package be.helha.labos.crystalclash.DAOImpl;

import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAO.CharacterDAO;
import be.helha.labos.crystalclash.DeserialiseurCustom.ObjectBasePolymorphicDeserializer;
import be.helha.labos.crystalclash.Inventory.Inventory;
import be.helha.labos.crystalclash.Object.*;
import be.helha.labos.crystalclash.Service.CharacterService;
import be.helha.labos.crystalclash.Service.InventoryService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
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
     *
     * @param username Nom d'utilisateur
     * @return Le type de personnage
     */
    @Override
    public String getCharacterForUser(String username) {
        MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
        MongoCollection<Document> collection = mongoDB.getCollection("Characters");
        // Cherche le personnage sélectionné pour cet utilisateur
        Document result = collection.find(
                //eq = equivalent à, comme un where en sql
                Filters.and(
                        Filters.eq("username", username),
                        Filters.eq("selected", true)
                )
        ).first(); //retourne le premier touvé

        if (result != null) {
            return result.getString("type");
        }
        return null;
    }


    /**
     * Sauvegarde le personnage pour un utilisateur
     *
     * @param username      Nom d'utilisateur
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
            //Si premier perso du user on le met a 1
            long count = collection.countDocuments(new Document("username", username));
            boolean IsFirst = (count == 0);
            Document docu = new Document("username", username)
                    .append("type", characterType)
                    .append("backpack", backpack)
                    .append("selected",IsFirst);
            collection.insertOne(doc);
        }
    }

    /**
     * Crée un backPack pour un nouveau perso choisi
     * si deja BackPack alors on le garde
     *
     * @param username      Nom d'utilisateur
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
     *
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
                doc.remove("_id"); // important
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                        .create();
                return gson.fromJson(backpackDoc.toJson(), BackPack.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new BackPack(); // retourne un backpack vide si erreur ou pas trouvé
    }

    /**
     * Met a jour le personnage sélectionné pour un utilisateur
     *
     * @param username      Nom d'utilisateur
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
     *
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
     *
     * @param username Nom d'utilisateur
     * @param name     Nom de l'objet
     * @param type     Type de l'objet
     * @return Réponse de l'API reponse
     */
    @Override
    public ApiReponse addObjectToBackPack(String username, String name, String type) {
        try {
            Inventory inventory = inventoryService.getInventoryForUser(username);
            BackPack backpack = getBackPackForCharacter(username);

            if (backpack.getObjets().size() >= 10) {
                return new ApiReponse("Backpack plein !", null);
            }
            ObjectBase objectToAdd = null;

            // 1. Rechercher l'objet dans l'inventaire direct
            objectToAdd = inventory.getObjets().stream()
                    .filter(obj -> obj.getName().equals(name) && obj.getType().equals(type))
                    .findFirst()
                    .orElse(null);

            if (objectToAdd != null) {
                inventory.retirerObjet(objectToAdd); // le retirer de l'inventaire
            } else {
                // 2. Rechercher dans les coffres
                for (ObjectBase obj : inventory.getObjets()) {
                    if (obj instanceof CoffreDesJoyaux) {
                        CoffreDesJoyaux coffre = (CoffreDesJoyaux) obj;
                        objectToAdd = coffre.getContenu().stream()
                                .filter(o -> o.getName().equals(name) && o.getType().equals(type))
                                .findFirst()
                                .orElse(null);
                        if (objectToAdd != null) {
                            coffre.getContenu().remove(objectToAdd); // retirer du coffre
                            break;
                        }
                    }
                }
            }

            if (objectToAdd == null) {
                return new ApiReponse("Objet non trouvé dans l'inventaire ni dans un coffre.", null);
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


    /**
     * Supprime un objet du backpack du personnage du joueur
     *
     * @param username Nom d'utilisateur
     * @param name     Nom de l'objet
     * @return Réponse de l'API reponse
     */
    @Override
    public ApiReponse removeObjectFromBackPack(String username, String name) {
        try {
            BackPack backpack = getBackPackForCharacter(username);
            Inventory inventory = inventoryService.getInventoryForUser(username);

            if (backpack == null) {
                return new ApiReponse("Backpack introuvable.", null);
            }
            if (backpack.getObjets().isEmpty()) {
                return new ApiReponse("Backpack vide !", null);
            }

            ObjectBase objectToRemove = backpack.getObjets().stream()
                    .filter(obj -> obj.getName().equals(name))
                    .findFirst()
                    .orElse(null);

            if (objectToRemove == null) {
                return new ApiReponse("Objet non trouvé dans le backpack.", null);
            }

            backpack.retirerObjet(objectToRemove);
            if (inventory.getObjets().size() > 30) {
                return new ApiReponse("Inventaire plein !", null);
            }
            if (!inventory.ajouterObjet(objectToRemove)) {
                return new ApiReponse("Erreur lors de l'ajout de l'objet à l'inventaire.", null);
            }
            saveBackPackForCharacter(username, backpack);
            inventoryService.saveInventoryForUser(username, inventory);
            System.out.println("Objet retiré du backpack : " + objectToRemove.getName());
            return new ApiReponse("Objet retiré du backpack avec succès !", backpack);

        } catch (Exception e) {
            return new ApiReponse("Erreur lors de la suppression de l'objet du backpack : " + e.getMessage(), null);
        }
    }}



