package be.helha.labos.crystalclash.Services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

    public static String getInventory(String username, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/inventory/" + username))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token) // Envoie le token JWT dans l'en-tête
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        return response.body(); // Retourne le JSON brut : { "username": "...", "level": 1, "cristaux": 100 }
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


    /**
     * Envoie une requête pour sélectionner un personnage
     *
     * @param username
     * @param characterType
     * @param token
     * @throws Exception
     */
    public static void selectCharacter(String username, String characterType, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/characters/select"))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        new Gson().toJson(Map.of(
                                "username", username,
                                "characterType", characterType,
                                "token", token
                        ))
                ))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            String message = json.has("message") ? json.get("message").getAsString() : "Erreur inconnue";
            throw new RuntimeException(message);
        }
    }

    /**
     * Envoie une requête pour récupérer le personnage d'un utilisateur
     *
     * @param username
     * @param token
     * @return
     * @throws Exception
     */
    public static String getCharacter(String username, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/characters/" + username))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    /**
     * Récupère le backpack du personnage d'un utilisateur
     *
     * @param username nom de l'utilisateur
     * @param token    JWT d'authentification
     * @return JSON brut contenant la liste des objets dans le backpack
     * @throws Exception en cas d'erreur réseau
     */
    public static String getBackpack(String username, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/characters/" + username + "/backpack"))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Erreur lors de la récupération du backpack : " + response.body());
        }

        return response.body(); // Contient un tableau JSON : [{"name":"...","price":...}, ...]
    }


    public static String buyItem(String name, String type, String token) throws Exception {
        String json = new Gson().toJson(Map.of(
                "name", name,
                "type", type
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/shop/buy"))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body(); // {"success":true,"message":"Epée achetée avec succès !"}
    }

    public static String getShops(String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/shop"))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body(); // retour JSON brut : [{"name":"Epée", "type":"Weapon",...
    }

    public static String sellObjetc(String name, String type, String token) throws Exception {
        String json = new Gson().toJson(Map.of(
                "name", name,
                "type", type
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/inventory/sell"))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();


        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static String putInBackpack(String username, String name, String type, String token) throws Exception {
        String json = new Gson().toJson(Map.of(
                "name", name,
                "type", type
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/characters/" + username + "/backpack/add")) // on utilise le bon endpoint
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)) // POST car tu fais une action qui modifie
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static String removeFromBackpack(String username, String name, String type, String token) throws Exception {
        String json = new Gson().toJson(Map.of(
                "name", name,
                "type", type
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/characters/" + username + "/backpack/remove")) // on utilise le bon endpoint
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)) // POST car tu fais une action qui modifie
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
