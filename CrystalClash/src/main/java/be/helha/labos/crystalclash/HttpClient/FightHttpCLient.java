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

/**
 * Client HTTP dédié aux interactions de combat dans Crystal Clash.
 * Gère les actions telles qu'attaquer, utiliser un objet, entrer/sortir de la salle d'attente,
 * défier un adversaire, déclarer forfait, consulter le classement et suivre l'état du combat.
 */
public class FightHttpCLient {

    private static final String BASE_URL = HttpConfig.getBaseUrl();

    /**
     * Envoie une requête pour effectuer une attaque (base ou spéciale) durant un combat.
     *
     * @param username Nom du joueur attaquant.
     * @param type Type d'attaque ("base", "special", etc.).
     * @param token Jeton JWT d'authentification.
     * @return Réponse brute du serveur.
     * @throws Exception En cas d'échec de la requête.
     */
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
     * Envoie une requête pour utiliser un objet durant un combat.
     *
     * @param username Nom d'utilisateur.
     * @param objectId ID de l'objet à utiliser.
     * @param token Jeton JWT d'authentification.
     * @return Réponse brute du serveur.
     * @throws Exception En cas d'échec de la requête.
     */
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
     * Récupère l'état actuel du combat d'un utilisateur.
     *
     * @param username Nom du joueur.
     * @param token Jeton JWT.
     * @return JSON contenant l'état du combat.
     * @throws Exception En cas d'erreur de requête.
     */
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

    /**************************** Salle d'attente ***************************/

    /**
     * Envoie une requête pour entrer dans la salle de matchmaking.
     *
     * @param userInfo Informations du joueur.
     * @param token Jeton JWT.
     * @throws Exception En cas d'échec.
     */
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

    /**
     * Envoie une requête pour sortir de la salle de matchmaking.
     *
     * @param username Nom du joueur.
     * @param token Jeton JWT.
     * @throws Exception En cas d'échec.
     */
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

    /**
     * Récupère les adversaires disponibles pour être défiés.
     *
     * @param username Nom du joueur appelant.
     * @param token Jeton JWT.
     * @return Liste des joueurs disponibles.
     * @throws Exception En cas d'échec.
     */
    public static List<UserInfo> getAvailableOpponents(String username, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/matchmaking/available?username=" + username))
            .timeout(Duration.ofSeconds(5))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Erreur récupération adversaires : " + response.body());
        }
        return Arrays.asList(new Gson().fromJson(response.body(), UserInfo[].class));
    }

    /**
     * Déclenche un défi entre deux joueurs.
     *
     * @param challenger Le joueur qui défie.
     * @param challenged Le joueur défié.
     * @param token Jeton JWT.
     * @return Réponse brute du serveur.
     * @throws Exception En cas d'échec.
     */
    public static String challengePlayer(String challenger, String challenged, String token) throws Exception {
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
     * Déclare forfait dans un combat.
     *
     * @param username Nom du joueur.
     * @param token Jeton JWT.
     * @throws Exception En cas d'échec.
     */
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
     * Récupère le gagnant du dernier combat.
     *
     * @param username Nom du joueur.
     * @param token Jeton JWT.
     * @return Nom du gagnant ou null si inconnu.
     */
    public static String getLastWinner(String username, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/combat/Winner?username=" + username))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

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
     * Récupère le classement des joueurs basé sur leurs victoires.
     *
     * @param token Jeton JWT.
     * @return Réponse JSON contenant le classement ou null si erreur.
     */
    public static String getClassementPlayer(String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/combat/classement"))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

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
