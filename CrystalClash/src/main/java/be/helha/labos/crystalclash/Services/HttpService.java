package be.helha.labos.crystalclash.Services;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/*
 * CLeint http qui permet au jeu d'
 * envoyer des requetes d'inscription/connexion au /login, /register
 * "http://localhost:8080" URL de serveur spring boot
 * Construction du json a envoyer + de la requête HTTP POST a envoyer
 * Envoie de la réponse HttpClient lit la réponse sous forme de texte (JSON en string)
 * retourne la rép en brute
 *
 * */
public class HttpService {
  //  private static final String BASE_URL = "http://192.168.28.146:8080/";
    private static final String BASE_URL = "http://localhost:8080";

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

    /*
     * envoie la requête à /register pour créer un nouvel utilisateur
     *poste un JSON avec pseudo + mot de passe
     * Le serveur vérifie que l’utilisateur n’existe pas
     *Le serveur vérifie que l’utilisateur n’existe pas
     *Il renvoie un message (pas de token)
     * */
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


    public static String get(String endpoint, String token) throws IOException {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + token);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            return content.toString();
        }
    }

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

}
