package be.helha.labos.crystalclash.Services;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.Inventory.Inventory;
import be.helha.labos.crystalclash.Object.ObjectBase;
import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.stereotype.Service;
@Service
public class InventoryMongoServices {

    // Crée un inventaire vide (30 places) pour un nouvel utilisateur
    /*
     * Crée inventaire vide avec new Inventory
     *converit en json avec Gson
     * le convetit en doc mongo
     * ajoute le champ username pour savoir a qui il appartient
     * et insere le doc ds la collection Inventory
     */
    public static void CreateInvetoriesForUser(String username) {
        try {
            MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
            MongoCollection<Document> collection = mongoDB.getCollection("Inventory");

            /*
             * Convertir un objet java en json
             * sérialiser un objet java en doc mongodb
             * String json = gson.toJson(inventory);
             */
            Inventory inventory = new Inventory();
            Gson gson = new Gson();
            String json = gson.toJson(inventory); // Sérialisation : Java → JSON
            Document document = Document.parse(json); //JSON → Document MongoDB
            document.append("username", username);

            collection.insertOne(document);
            System.out.println("Inventaire créé en MongoDB pour " + username);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static Inventory getInventoryForUser(String username) {
        try {
            MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
            MongoCollection<Document> collection = mongoDB.getCollection("Inventory");

            Document doc = collection.find(new Document("username", username)).first();
            if (doc != null) {
                doc.remove("_id"); // on retire l’id MongoDB si présent
                Gson gson = new Gson();
                return gson.fromJson(doc.toJson(), Inventory.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Inventory(); // Retourne inventaire vide si problème
    }

    }

