package be.helha.labos.crystalclash.ConfigManagerMysql_Mongo;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConfigManager {
    private static ConfigManager instance;
    private JsonObject config;

    // Constructeur privé pour empêcher l'instanciation externe
    private ConfigManager() {
        try {
            Gson gson = new Gson();
            InputStreamReader reader = new InputStreamReader(
                ConfigManager.class.getClassLoader().getResourceAsStream("config.json"),
                StandardCharsets.UTF_8
            );
            config = JsonParser.parseReader(reader).getAsJsonObject();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors du chargement de config.json !");
        }
    }

    // Méthode pour obtenir l'instance unique
    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    /**
     * Retourne la configuration complète
     */
    public JsonObject getConfig() {
        return config;
    }

    /**
     * Vérifie si une base de données SQL existe et la crée si elle est absente
     */
    public Connection getSQLConnection(String dbKey) throws SQLException {
        try {
            JsonObject db = config.getAsJsonObject("db");
            JsonObject section = db.getAsJsonObject(dbKey);
            JsonObject creds = section.getAsJsonObject("BDCredentials");

            String dbType = creds.get("DBType").getAsString();
            String host = creds.get("HostName").getAsString();
            String port = creds.get("Port").getAsString();
            String dbName = creds.get("DBName").getAsString();
            String user = creds.get("UserName").getAsString();
            String password = creds.get("Password").getAsString();

            String dbUrl = "jdbc:" + dbType + "://" + host + ":" + port + "/" + dbName + "?serverTimezone=UTC";

            return DriverManager.getConnection(dbUrl, user, password);

        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Erreur de connexion à SQL !");
        }
    }

    /**
     * Vérifie si une base MongoDB existe et la crée si elle est absente
     */
    public MongoDatabase getMongoDatabase(String dbKey) {
        try {
            JsonObject dbConfig = config.getAsJsonObject("db").getAsJsonObject(dbKey).getAsJsonObject("BDCredentials");
            String uri = "mongodb://" + dbConfig.get("UserName").getAsString() + ":" +
                dbConfig.get("Password").getAsString() + "@" +
                dbConfig.get("HostName").getAsString() + ":" +
                dbConfig.get("Port").getAsString();
            String dbName = dbConfig.get("DBName").getAsString();

            MongoClient mongoClient = MongoClients.create(uri);
            MongoDatabase database = mongoClient.getDatabase(dbName);

            // Vérifier si la base existe en listant les bases
            boolean exists = mongoClient.listDatabaseNames().into(new java.util.ArrayList<>()).contains(dbName);
            if (!exists) {
                System.out.println("La base MongoDB " + dbName + " n'existe pas, elle sera créée à la première insertion.");
            }
            return database;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur de connexion à MongoDB !");
        }
    }
}
