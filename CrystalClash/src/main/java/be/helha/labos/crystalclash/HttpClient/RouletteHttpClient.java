package be.helha.labos.crystalclash.HttpClient;


import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class RouletteHttpClient {


    private static final String BASE_URL = HttpConfig.getBaseUrl();

    /**
     * envoie requete pour jouer a la roulette
     *
     * @param username
     * @param token
     **/
    public static String PlayRoulette(String username, String token) throws Exception {
        String json = new Gson().toJson(Map.of(
            "username", username
        ));
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/roulette/play"))
            .timeout(Duration.ofSeconds(5))
            .header("Authorization", "Bearer " + token)
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Erreur lors du tirage de la roulette : " + response.body());
        }

        return response.body();
    }


}
