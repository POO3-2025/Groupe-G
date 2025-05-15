package be.helha.labos.crystalclash.HttpClient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class CombatHttpClient {

    private static final String BASE_URL = HttpConfig.getBaseUrl();

    /**
     * @param username
     * @param token
     * @param type = normal ou special
     * Lance une attaque normal ou spécial du perso
     * **/
    public static String combatAttack(String username, String type, String token) throws Exception {
        String json = new Gson().toJson(Map.of("username", username, "type", type));
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/combat/attack"))
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
     * @param objectId = object id de l'object souhaité utiliser pdt le combat
     * **/
    public static String combatUseObject(String username, String objectId, String token) throws Exception {
        String json = new Gson().toJson(Map.of("username", username, "objectId", objectId));
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/combat/use-object"))
            .timeout(Duration.ofSeconds(5))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /**
     * @param username
     * @param token
     * avoir l'etat du combat actuel
     * **/
    public static String getCombatState(String username, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/combat/state/" + username))
            .timeout(Duration.ofSeconds(5))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }


    /**
     * @param challenger = celui qui défie
     * @param challenged  = le défié
     * @param token
     * Permet de savoir qui defie qui
     * **/
    public static String challengePlayer(String challenger, String challenged, String token) throws Exception, InterruptedException {
        // Corps JSON
        JsonObject json = new JsonObject();
        json.addProperty("challenger", challenger);
        json.addProperty("challenged", challenged);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/combat/challenge"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + token)
            .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Erreur défi : " + response.body());
        }

        return response.body();

    }

    /**
     * @param username = celui qui fait forfait
     * @param token
     * Si forfait
     * **/
    public static void forfait(String username, String token) throws Exception {
        String json = new Gson().toJson(Map.of("username", username));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/combat/forfait"))
            .timeout(Duration.ofSeconds(5))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Erreur lors du forfait : " + response.body());
        }
    }

    /**
     * @param username
     * @param token
     * Obtenir le gagnant du combat
     * **/
    public static String getLastWinner(String username, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/combat/Winner?username=" + username))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("[DEBUG] Status winner = " + response.statusCode());
                System.out.println("[DEBUG] Body winner = " + response.body());
                return null;
            }

            return response.body();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param token
     * Récup le classement des joueur par rapport a leurs nbr de victoires.
     * **/
    public static String getClassementPlayer( String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/combat/classement"))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
            HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Statut HTTP : " + response.statusCode());
            System.out.println("Réponse brute : " + response.body());
            if (response.statusCode() != 200) {

                return null;
            }
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
