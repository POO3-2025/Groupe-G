package be.helha.labos.crystalclash.DAOImpl;

import be.helha.labos.crystalclash.DAO.RouletteDAO;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.springframework.stereotype.Repository;
import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;

import javax.print.Doc;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Repository
public class RouletteDAOImpl implements RouletteDAO{

    //Constante de type DateTimeFormatter
    //Format la date actuelle formt iso sans l'heure.
    //Juste formate la date correctement
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Sauvegarde le personnage pour un utilisateur
     * @param username Nom d'utilisateur
     * @param date Date
     * Permet de mettre la derniere date a jour du J lors de sa derniere roulette
     */
    @Override
    public void UpdateLastPlayDate(String username, LocalDate date){
        MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
        MongoCollection<Document> collection = mongoDB.getCollection("Roulette");

        Document doc =new Document("username", username);
        //Cree un doc avec la nv date et set pour modi ou cree lastplay
        Document update = new Document("$set",new Document("lastPlay", date.format(FORMATTER)));

        //So doc trouvé correspond dans mongo alors mis a true
        collection.updateOne(doc,update, new UpdateOptions().upsert(true));

    }

    /**
    * @param username
     * Permet d'obtenir la denrier date ou le J a tourné la roulette
    * */
    @Override
    public LocalDate getLastPlayDate(String username){
        MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
        MongoCollection<Document> collection = mongoDB.getCollection("Roulette");


        Document doc = collection.find(new Document("username",username)).first();
        if(doc == null || !doc.containsKey("lastPlay")){
            return null;
        }
        //si pas null ou diff alors date dernier partie jouée
        return LocalDate.parse(doc.getString("lastPlay"), FORMATTER);
    }

}
