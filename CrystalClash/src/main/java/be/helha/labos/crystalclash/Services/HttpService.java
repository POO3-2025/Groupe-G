package be.helha.labos.crystalclash.Services;

import java.net.http.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import be.helha.labos.crystalclash.server_auth.*;

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

    /*
     * @param username et le token recu lors du login
     *construit l url final = http://localhost:8080/inventory/gege (gege username)
     * max de 5sec sinon requete echoue
     * .header("Authorization", "Bearer " + token) = ajout du token dans le header pour voir si le joueur est bien co
     *lire et construit l objet httprequest
     * envoie la requete http et recup sous forme de chaine json
     * retourne cette chaine
     * dans lanterna on l'utilise comme ça String json = HttpService.getInventoryFromServer(Session.getUsername(), Session.getToken());
     * */
    public static String getInventoryFromServer(String username, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/inventory/" + username))
            .timeout(Duration.ofSeconds(5))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body(); // Contient le JSON complet
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
    /*
     * @param username et le token recu lors du login
     *construit l url final = http://localhost:8080/inventory/gege (gege username)
     * max de 5sec sinon requete echoue
     * .header("Authorization", "Bearer " + token) = ajout du token dans le header pour voir si le joueur est bien co
     *lire et construit l objet httprequest
     * envoie la requete http et recup sous forme de chaine json
     * retourne cette chaine
     * dans lanterna on l'utilise comme ça String json = HttpService.getInventoryFromServer(Session.getUsername(), Session.getToken());
     * */
    public static String getInventoryFromServer(String username, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/inventory/" + username))
            .timeout(Duration.ofSeconds(5))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body(); // Contient le JSON complet
    }

}
