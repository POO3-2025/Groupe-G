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
/**
 * La classe ConfigManager gère la configuration de l'application, notamment
 * les connexions aux bases de données SQL et MongoDB.
 * Elle utilise le pattern Singleton pour garantir une instance unique.
 */
public class ConfigManager {
    private static ConfigManager instance;
    private JsonObject config;

    /**
     * Constructeur privé pour empêcher l'instanciation externe.
     * Charge le fichier de configuration `config.json`.
     *
     * @throws RuntimeException si une erreur survient lors du chargement du fichier.
     */
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

    /**
     * Méthode statique pour obtenir l'instance unique de ConfigManager.
     * Utilise le pattern Singleton pour garantir qu'il n'y a qu'une seule instance.
     * Verif l'instance est null ça rentre dans un bloque synchronisé (ça bloque d'autre entrée)
     * Et une fois dans le bloque si instance etait null
     * dans la premiere verif on crée l'instance dans la second
     *
     * @return L'instance unique de ConfigManager.
     */
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
     *
     * @return La configuration complète.
     */
    public JsonObject getConfig() {
        return config;
    }

    /**
     *  Vérifie si un SQL existe et la crée si elle est absente
     * @param dbKey sert a choisir quelle db on utilise depuis le config
     * @return Une connexion SQL active.
     * @throws SQLException si une erreur survient lors de la connexion ou si la clé est invalide.
     */
    public Connection getSQLConnection(String dbKey) throws SQLException {
        try {
            JsonObject db = config.getAsJsonObject("db");
            JsonObject section = db.getAsJsonObject(dbKey);
            if (section == null) {
                throw new SQLException("Clé de base inconnue dans config.json : " + dbKey);
            }
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
            // juste un message utile
            throw new SQLException("Clé de base inconnue dans config.json : " + dbKey);
        }
    }

    /**
     * Vérifie si une base MongoDB existe et la crée si elle est absente
     * Modif pour permettre un affichage moins brute lors de test
     * @param dbKey sert a choisir quelle db on utilise depuis le config
     * @return Une instance de MongoDatabase.
     * @throws RuntimeException si une erreur survient lors de la connexion ou si la clé est invalide.
     */
    public MongoDatabase getMongoDatabase(String dbKey) {
        try {
            //Ajout cérif de clé, si dbKey pas ds le .json retourne null et appelle getAsJsonObject("BDCredentials"); pour un NullPointerException        try {
            JsonObject db = config.getAsJsonObject("db");
            JsonObject section = db.getAsJsonObject(dbKey);
            if (section == null) {
                throw new RuntimeException("Clé de base inconnue dans config.json : " + dbKey);
            }

            JsonObject dbConfig = section.getAsJsonObject("BDCredentials");

            String uri = "mongodb://" + dbConfig.get("UserName").getAsString() + ":" +
                dbConfig.get("Password").getAsString() + "@" +
                dbConfig.get("HostName").getAsString() + ":" +
                dbConfig.get("Port").getAsString();


            String dbName = dbConfig.get("DBName").getAsString();

            MongoClient mongoClient = MongoClients.create(uri);
            MongoDatabase database = mongoClient.getDatabase(dbName);

            boolean exists = mongoClient.listDatabaseNames().into(new java.util.ArrayList<>()).contains(dbName);
            if (!exists) {
                System.out.println("La base MongoDB " + dbName + " n'existe pas, elle sera créée à la première insertion.");
            }

            return database;
        } catch (Exception e) {
            throw new RuntimeException("Erreur de connexion à MongoDB ! " + e.getMessage());
        }
        }
    }

