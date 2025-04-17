package be.helha.labos.crystalclash.DAOImpl;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAO.CharacterDAO;
import be.helha.labos.crystalclash.Inventory.Inventory;
import be.helha.labos.crystalclash.Object.ObjectBase;
import be.helha.labos.crystalclash.Object.*;
import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import be.helha.labos.crystalclash.Object.BackPack;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CharacterDAOImpl implements CharacterDAO {

    @Override
    public String getCharacterForUser(String username) {
        MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
        MongoCollection<Document> collection = mongoDB.getCollection("Characters");
        Document doc = collection.find(new Document("username", username)).first();
        return (doc != null) ? doc.getString("type") : null;
    }

    @Override
    public void saveCharacterForUser(String username, String characterType) {
        MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
        MongoCollection<Document> collection = mongoDB.getCollection("Characters");
        Document doc = new Document("username", username).append("type", characterType);
        collection.deleteMany(new Document("username", username));
        collection.insertOne(doc);
    }

    @Override
    public void createBackPackForCharacter(String username){
        MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
        MongoCollection<Document> collection = mongoDB.getCollection("Characters");

        Document BackPack = new Document("objets", List.of()); //vide
        Document update = new Document("$set", new Document("backpack", BackPack));
        collection.updateOne(new Document("username", username), update);

    }

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
                    String type = objDoc.getString("type");
                    String name = objDoc.getString("name");
                    int price = objDoc.getInteger("price");
                    int requiredLevel = objDoc.getInteger("requiredLevel");
                    int reliability = objDoc.getInteger("reliability");

                    ObjectBase obj = null;

                    switch (type.toLowerCase()) {
                        case "weapon" -> obj = new Weapon(name, price, requiredLevel, reliability, objDoc.getInteger("damage"));
                        case "armor" -> obj = new Armor(name, price, requiredLevel, reliability, objDoc.getInteger("bonusPV"));
                        case "healingpotion" -> obj = new HealingPotion(name, price, requiredLevel, objDoc.getInteger("heal"));
                        case "potionofstrenght" -> obj = new PotionOfStrenght(name, price, requiredLevel, objDoc.getInteger("bonusATK"));
                        case "coffredesjoyaux" -> obj = new CoffreDesJoyaux(); // à améliorer plus tard si contenu
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

}
