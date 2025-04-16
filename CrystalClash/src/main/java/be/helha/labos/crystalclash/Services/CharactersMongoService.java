package be.helha.labos.crystalclash.Services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import org.springframework.stereotype.Service;

@Service
public class CharactersMongoService {
    /**
     * Récupère le type de personnage pour un utilisateur donné
     * @param username le nom d'utilisateur
     * @return le type de personnage ou null si non trouvé
     */
    public  String getCharacterForUser(String username) {
        MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
        MongoCollection<Document> collection = mongoDB.getCollection("Characters");
        Document doc = collection.find(new Document("username", username)).first();
        return (doc != null) ? doc.getString("type") : null;
    }
    /**
     * Sauvegarde le type de personnage pour un utilisateur donné
     * @param username le nom d'utilisateur
     * @param characterType le type de personnage
     */
    public void saveCharacterForUser(String username, String characterType) {
        MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
        MongoCollection<Document> collection = mongoDB.getCollection("Characters");
        Document doc = new Document("username", username).append("type", characterType);
        collection.deleteMany(new Document("username", username)); // évite les doublons
        collection.insertOne(doc);
    }
}