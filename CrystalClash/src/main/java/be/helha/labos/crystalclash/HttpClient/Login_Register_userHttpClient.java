package be.helha.labos.crystalclash.HttpClient;

import be.helha.labos.crystalclash.User.UserInfo;
import com.google.gson.Gson;

import org.bson.Document;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class Login_Register_userHttpClient {

    private static final String BASE_URL = HttpConfig.getBaseUrl();

    /**
     * Envoie requete pour login user
     * @param password
     * @param username
     **/
    public static String login(String username, String password) throws Exception {
        String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    /**
     * envoie la requête à /register pour créer un nouvel utilisateur
     *poste un JSON avec pseudo + mot de passe
     * Le serveur vérifie que l’utilisateur n’existe pas
     *Le serveur vérifie que l’utilisateur n’existe pas
     *Il renvoie un message (pas de token)
     *
     * @param password
     * @param username
     **/
    public static String register(String username, String password) throws Exception {
        String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/register"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    /**
     * avoir info user
     * @param username
     * @param token
     **/
    public static String getUserInfo(String username, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/user/" + username))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token) // Envoie le token JWT dans l'en-tête
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        return response.body(); // Retourne le JSON brut : { "username": "...", "level": 1, "cristaux": 100 }
    }

    /**
     * @param username
     * @param token
     * recup stats dans mongo
     * **/
    public static Document fetchUserStats(String username, String token)throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/user/user/stats/" + username))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Réponse brute : " + response.body());
        if (response.statusCode() == 200) {
            return Document.parse(response.body());
        } else {
            throw new RuntimeException("Impossible de charger les stats Mongo : " + response.body());
        }
    }


}
