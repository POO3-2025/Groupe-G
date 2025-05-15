package be.helha.labos.crystalclash.HttpClient;


import com.google.gson.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;


public class InventoryHttpClient {

    private static final String BASE_URL = HttpConfig.getBaseUrl();

    /**
     * Envoie requete pour avoir inventaire user
     * @param username
     * @param token
     **/

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

    /**
     * vend object json le meme
     *  @param name
     *   @param type
     * @param token
     **/
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

    /**
     * @param token
     * @param username
     * @param type
     * @param name
     * Met un objet dans le coffre
     * **/
    public static String putInCoffre(String username, String name, String type, String token) throws Exception {
        String json = new Gson().toJson(Map.of(
            "name", name,
            "type", type
        ));
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/inventory/" + username + "/coffre/add"))
            .timeout(Duration.ofSeconds(5))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

}
