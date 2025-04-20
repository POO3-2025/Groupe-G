package be.helha.labos.crystalclash.DAOImpl;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAO.CharacterDAO;
import be.helha.labos.crystalclash.Object.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
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

    /*
     * Crée un backPack pour un nouveau perso choisi
     * SI deja BackPack alors on le garde
     * */
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