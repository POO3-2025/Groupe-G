package be.helha.labos.crystalclash.Services;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.Object.*;
import be.helha.labos.crystalclash.Inventory.*;

import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class InventoryMongoServices {

    // Crée un inventaire vide (30 places) pour un nouvel utilisateur
    public static void CreateInvetoriesForUser(String username) {
        try {
            MongoDatabase mongoDB = ConfigManager.getMongoDatabase("MongoDBProduction");
            MongoCollection<Document> collection = mongoDB.getCollection("Inventory");

            Inventory inventory = new Inventory();
            Gson gson = new Gson();
            String json = gson.toJson(inventory);
            Document document = Document.parse(json);
            document.append("username", username);

            collection.insertOne(document);
            System.out.println("Inventaire créé en MongoDB pour " + username);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Récupère l'inventaire MongoDB et le transforme en objet Java
    public static Inventory getInventoryByUsername(String username){
        try {
            MongoDatabase mongoDB = ConfigManager.getMongoDatabase("MongoDBProduction");
            MongoCollection<Document> collection = mongoDB.getCollection("Inventory");

            Document document = collection.find(new Document("username", username)).first();

            if (document != null) {
                System.out.println("Inventaire récupéré en MongoDB pour " + username);
            } else {
                System.out.println("Inventaire non trouvé en MongoDB pour " + username);
            }

            document.remove("username");

            Gson gson = new Gson();
            String json = document.toJson();
            return gson.fromJson(json, Inventory.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Ajoute un objet dans l'inventaire Mongo d'un utilisateur
    public static void AjouterObjetInventaire(String username, ObjectBase objet) {
        try {
            MongoDatabase mongoDB = ConfigManager.getMongoDatabase("MongoDBProduction");
            MongoCollection<Document> collection = mongoDB.getCollection("Inventory");

            Document document = collection.find(new Document("username", username)).first();

            if (document == null) {
                System.out.println("Inventaire introuvable pour " + username);
                return;
            }

            document.remove("username");

            Gson gson = new Gson();
            Inventory inventory = gson.fromJson(document.toJson(), Inventory.class);

            if (inventory.getObjets().size() < inventory.getCapaciteMax()) {
                inventory.getObjets().add(objet);

                Document updatedDoc = Document.parse(gson.toJson(inventory));
                updatedDoc.append("username", username);

                collection.replaceOne(new Document("username", username), updatedDoc);
                System.out.println("Objet ajouté à l’inventaire de " + username);
            } else {
                System.out.println("Inventaire plein pour " + username);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
