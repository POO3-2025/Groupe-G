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
            MongoDatabase mongoDB = ConfigManager.getMongoDatabase("MongoDBProduction");
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

    // Récupère l'inventaire MongoDB et le transforme en objet Java
    /*
     * Cherche dans la collection Inventory l'inventaire par username
     *si trouvé le convertit en json
     *supprime le username pour eviter certaines erreurs gson
     * et reconstruit l'objet java
     * */
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
            String json = document.toJson(); //Doc mongo to Json
            return gson.fromJson(json, Inventory.class); //Json to objet java

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Ajoute un objet dans l'inventaire Mongo d'un utilisateur
    /*
     * A tester ça pas sur
     *
     * */
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
            /*
             * Inventory inv = gson.fromJson(json, Inventory.class);
             * ->Désérialiser le doc mongo en objet java
             */
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
