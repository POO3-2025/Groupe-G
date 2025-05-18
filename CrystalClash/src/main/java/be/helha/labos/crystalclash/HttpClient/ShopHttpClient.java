package be.helha.labos.crystalclash.HttpClient;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class ShopHttpClient {

    private static final String BASE_URL = HttpConfig.getBaseUrl();



    /**
     * acheter un item avec requete passant name et type ds le json
     * @param name
     * @param type
     * @param token
     **/
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

    /**
     * obtenir shop, token obligatoire car on afficher shop en focntion  du level du user
     * @param token
     **/

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



}