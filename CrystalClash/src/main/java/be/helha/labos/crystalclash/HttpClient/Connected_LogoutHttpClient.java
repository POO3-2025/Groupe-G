package be.helha.labos.crystalclash.HttpClient;

import be.helha.labos.crystalclash.User.UserInfo;
import be.helha.labos.crystalclash.server_auth.Session;
import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Connected_LogoutHttpClient {

    private static final String BASE_URL = HttpConfig.getBaseUrl();



    /**********************Logout****************/
    /**
     * @param username
     * @param token
     * Permet une vrai deco du serveur
     * **/
    public static void logout(String username, String token) throws Exception {
        String json = new Gson().toJson(Map.of("username", username));

        //Création de la requete avec de bons headers
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users/logout"))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)) //Recoit la réponse en string
                .build();
        System.out.println("Envoi de logout pour JSON: " + json);
        //Envoie de la requete
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Erreur déconnexion du user: " + response.body());
        }
    }

    /**
     * Obtient la liste de tout les joueurs co
     * **/
    public static List<UserInfo> getConnectedUsers() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users/connected/list"))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + Session.getToken()) // si besoin token pour sécuriser
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Erreur récupération utilisateurs connectés : " + response.body());
        }

        //Récup la reponse du Htpp (en json brute)
        String json = response.body();
        Gson gson = new Gson(); //Instancie Gson
        //juste prendre le json, le convertir en tb de userInfo, ici chaque objet J ds le tb sont mappés auto sur UserInfo (username,level,..)
        UserInfo[] users = gson.fromJson(json, UserInfo[].class);
        return Arrays.asList(users); //retourne le tb en liste modifiable si y a besoin
    }
}
