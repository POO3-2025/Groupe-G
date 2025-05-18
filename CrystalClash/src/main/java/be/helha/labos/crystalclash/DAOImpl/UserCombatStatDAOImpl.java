package be.helha.labos.crystalclash.DAOImpl;

import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAO.UserCombatStatDAO;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserCombatStatDAOImpl implements UserCombatStatDAO {


    private static final Map<String, Boolean> bazookaUtilisation = new HashMap<>();

    /**
     * @param username
     * crée la collection et doc
     * */
    @Override
    public  void createStatsForUser(String username) {
        try {
            MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
            MongoCollection<Document> collection = mongoDB.getCollection("userCristauxWin");

            Document document = new Document();
            document.append("username", username);
            document.append("cristauxWin", 0);
            document.append("derniercombattour", 0);
            document.append("utilisationBazooka", 0);
            document.append("bronze",false );
            document.append("silver",false );
            document.append("or",false );
            document.append("dernierVainqueur", "");

            collection.insertOne(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param username
     * get state
     * */
    @Override
    public String getStats(String username) {
        try {

            MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
            MongoCollection<org.bson.Document> collection = mongoDB.getCollection("userCristauxWin");

            org.bson.Document result = collection.find(new org.bson.Document("username", username)).first();
            if (result != null) {
                return result.toJson();
            } else {
                System.out.println("[DEBUG] Aucun document trouvé pour username=" + username);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }

    /**
     * @param username
     * @param nbTours
     * @param cristauxGagnes
     * Mis a jour du doc souhaité de la collection userCristauxWin
     * **/
     @Override
    public void updateStatsAfterCombat(String username, int cristauxGagnes, int nbTours,String dernierVainqueur){
        try{
            MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
            MongoCollection<Document> collection = mongoDB.getCollection("userCristauxWin");

            Document filter = new Document("username", username);
            Document exist = collection.find(filter).first();

            int cristauxNow = 0;
            if (exist != null && exist.containsKey("cristauxWin")) {
                cristauxNow = exist.getInteger("cristauxWin",0);
            }

            int newTotal = cristauxNow + cristauxGagnes;

            Document updates = new Document()
                .append("cristauxWin", newTotal)
                .append("derniercombattour", nbTours)
            .append("dernierVainqueur", dernierVainqueur);

            if (bazookaUtilisation.getOrDefault(username, false)) {
                updates.append("utilisationBazooka", 1);
            }

            Document updateOperation = new Document("$set", updates);

            collection.updateOne(filter, updateOperation);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            bazookaUtilisation.remove(username);
        }
     }
    /**
     * @param username
     * @param nameTrophy
     * Mis a jour du doc souhaité de la collection userCristauxWin pour les trophees
     * **/
    @Override
    public void updateStatsTrophy(String username, String nameTrophy){
        try{
            MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
            MongoCollection<Document> collection = mongoDB.getCollection("userCristauxWin");

            Document filter = new Document("username", username);
            Document up = new Document("$set", new Document(nameTrophy, true));

         var result =   collection.updateOne(filter, up);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

     /**
      * @param username
      * met a true bazookaUtilisation si username l'a use
      * **/
     @Override
    public void setBazookaUsed(String username){
        bazookaUtilisation.put(username,true);
     }

}
