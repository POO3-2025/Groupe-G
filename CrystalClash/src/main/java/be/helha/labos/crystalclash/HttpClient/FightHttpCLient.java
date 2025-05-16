package be.helha.labos.crystalclash.HttpClient;

import be.helha.labos.crystalclash.User.UserInfo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FightHttpCLient {

    private static final String BASE_URL = HttpConfig.getBaseUrl();



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



    /****************************Salle d'attente***************************/

    public static void enterMatchmakingRoom(UserInfo userInfo, String token) throws Exception {
        String json = new Gson().toJson(userInfo);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/matchmaking/enter"))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Erreur entrée matchmaking : " + response.body());
        }
    }

    public static void exitMatchmakingRoom(String username, String token) throws Exception {
        String json = new Gson().toJson(Map.of("username", username));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/matchmaking/exit"))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Erreur sortie matchmaking : " + response.body());
        }
    }

    public static List<UserInfo> getAvailableOpponents(String username, String token) throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/matchmaking/available?username=" + username))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Erreur entrée matchmaking : " + response.body());
        }
        return Arrays.asList(new Gson().fromJson(response.body(), UserInfo[].class));

    }

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

    //New
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
