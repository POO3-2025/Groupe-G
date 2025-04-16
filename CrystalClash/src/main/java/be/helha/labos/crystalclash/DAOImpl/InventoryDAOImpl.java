package be.helha.labos.crystalclash.DAOImpl;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAO.InventoryDAO;
import be.helha.labos.crystalclash.Inventory.Inventory;
import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryDAOImpl implements InventoryDAO {

    @Override
    public void createInventoryForUser(String username) {
        try {
            MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
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

    @Override
    public Inventory getInventoryForUser(String username) {
        try {
            MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
            MongoCollection<Document> collection = mongoDB.getCollection("Inventory");

            Document doc = collection.find(new Document("username", username)).first();
            if (doc != null) {
                doc.remove("_id");
                Gson gson = new Gson();
                return gson.fromJson(doc.toJson(), Inventory.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Inventory();
    }
}
