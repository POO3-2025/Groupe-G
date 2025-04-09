package be.helha.labos.crystalclash.Services;

import be.helha.labos.crystalclash.server_auth.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

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
    public static String getInventory() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/inventory"))
            .timeout(Duration.ofSeconds(5))
            .header("Authorization", "Bearer " + Session.getToken())

            .GET()
            .build();
        System.out.println("Token envoyé : " + Session.getToken());

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status HTTP : " + response.statusCode());
        System.out.println("Corps : " + response.body());

        return response.body();
    }

}
