package be.helha.labos.crystalclash.HttpClient;

import java.io.IOException;
import java.util.Properties;

/**
 * Config pour l url lanterna
 * **/
public class HttpConfig {

    private static String BASE_URL = "http://localhost:8080";
    private static Properties properties = new Properties();

    static{
        try{
            properties.load(HttpConfig.class.getResourceAsStream("/application.properties"));
        } catch (IOException e) {
            System.err.println(" Impossible de charger http.properties. BASE_URL par défaut utilisée.");
        }
    }

    /**
     * Base url
     * **/
    public static String getBaseUrl() {
        return properties.getProperty("app.api.base-url", BASE_URL);
    }
}
