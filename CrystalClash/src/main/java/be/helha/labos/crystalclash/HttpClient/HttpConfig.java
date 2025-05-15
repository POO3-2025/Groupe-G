package be.helha.labos.crystalclash.HttpClient;

import java.io.IOException;
import java.util.Properties;

public class HttpConfig {

    private  static final String DEFAULT_BASE_URL  = "http://localhost:8080/";
    private static final Properties props = new Properties();

    static {
        try{
            //va chercher le application.properties dans ressources/application.properties
            props.load(HttpConfig.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            System.err.println("[WARNING] Impossible de charger http.properties. BASE_URL par défaut utilisée.");
        }
    }

    public static String getBaseUrl() {
        return props.getProperty("http.base-url", DEFAULT_BASE_URL );
    }
}
