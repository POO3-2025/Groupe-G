package be.helha.labos.crystalclash.DAOImpl;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAO.FightHistoryDAO;
import be.helha.labos.crystalclash.DTO.FightHistory;
import be.helha.labos.crystalclash.Service.UserService;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.*; //Filtre

@Component
public class FightHistoryDAOImpl implements FightHistoryDAO {

    @Autowired
    private UserService userService;

     //Setter le winner,loser et heure
     private FightHistory documentToFight(Document doc) {
         FightHistory fight = new FightHistory();
         fight.setWinnerName(doc.getString("winnerName"));
         fight.setLoserName(doc.getString("loserName"));
         fight.setTimestamp(doc.getString("timestamp"));
         return fight;
     }

     //Sauver le combat et construire le doc
     public void saveFight(FightHistory fightHistory) {
         MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
         MongoCollection<Document> collection = mongoDB.getCollection("fightHistory");
         Document doc = new Document("winnerName", fightHistory.getWinnerName())
             .append("loserName", fightHistory.getLoserName())
             .append("timestamp", fightHistory.getTimestamp().toString());

         collection.insertOne(doc);
     }

    @Override
    public List<FightHistory> getFightsByPlayerId(int playerId) {
        MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
        MongoCollection<Document> collection = mongoDB.getCollection("fightHistory");
        List<FightHistory> fightHistoryList = new ArrayList<>();
        Bson filter = or(eq("winner", playerId), eq("loser",playerId));
        for(Document document : collection.find(filter)){
            fightHistoryList.add(documentToFight(document));
        }
        return fightHistoryList;
    }

    @Override
    public List<FightHistory> getAllFights(){
        MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
        MongoCollection<Document> collection = mongoDB.getCollection("fightHistory");
        List<FightHistory> fightHistoryList = new ArrayList<>();
        for(Document document : collection.find()){
            fightHistoryList.add(documentToFight(document));
        }
        return fightHistoryList;
    }


    @Override
    public List<Map<String, String>> getFightsByUsername(String username) {
        MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
        MongoCollection<Document> collection = mongoDB.getCollection("fightHistory");

        Bson filter = or(eq("winnerName", username), eq("loserName", username));
        List<Map<String, String>> results = new ArrayList<>();

        for (Document doc : collection.find(filter)) {
            Map<String, String> fight = new HashMap<>();
            fight.put("winnerName", doc.getString("winnerName"));
            fight.put("loserName", doc.getString("loserName"));
            fight.put("timestamp", doc.getString("timestamp"));
            results.add(fight);
        }

        return results;
    }


}
