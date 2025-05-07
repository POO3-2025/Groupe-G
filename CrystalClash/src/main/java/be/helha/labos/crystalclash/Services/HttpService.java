package be.helha.labos.crystalclash.Services;

import be.helha.labos.crystalclash.DeserialiseurCustom.ObjectBasePolymorphicDeserializer;
import be.helha.labos.crystalclash.Inventory.Inventory;
import be.helha.labos.crystalclash.Object.ObjectBase;
import be.helha.labos.crystalclash.User.UserInfo;
import be.helha.labos.crystalclash.server_auth.Session;
import com.google.gson.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;


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
    //   private static final String BASE_URL = "https://bf8e-94-109-202-55.ngrok-free.app";

    //private static final String BASE_URL = "http://192.168.68.56:8080";
    private static final String BASE_URL = "http://localhost:8080";


    /**
     * Envoie requete pour login user
     * @param password
     * @param username
     * **/
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

    /**
     * envoie la requête à /register pour créer un nouvel utilisateur
     *poste un JSON avec pseudo + mot de passe
     * Le serveur vérifie que l’utilisateur n’existe pas
     *Le serveur vérifie que l’utilisateur n’existe pas
     *Il renvoie un message (pas de token)
     *
     * @param password
     * @param username
     **/
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
    /**
     * Envoie requete pour avoir inventaire user
     * @param username
     * @param token
     * **/

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
     * avoir info user
     * @param username
     * @param token
     * **/
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


    /**
     * acheter un item avec requete passant name et type ds le json
     * @param name
     * @param type
     * @param token
     * **/
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
     * **/

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

    /**
     * vend object json le meme
     *  @param name
     *   @param type
     * @param token
     * **/
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
     * Met objet de l'inventaire ds le back en envoyant requete
     *  @param username
     * @param name
     * @param token
     * @param type
     * **/
    public static String putInBackpack(String username, String name, String type, String token) throws Exception {
        String json = new Gson().toJson(Map.of(
            "name", name,
            "type", type
        ));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/characters/" + username + "/backpack/add"))
            .timeout(Duration.ofSeconds(5))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /**
     * Retire de back pour mettre dans inventaire, evoie requete
     * @param token
     * @param name
     * @param username
     * @param type
     * **/
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

    /**
     * envoie requete pour jouer a la roulette
     @param username
     @param token
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

    public static String getCoffre(String username, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/inventory/" + username + "/coffre"))
            .timeout(Duration.ofSeconds(5))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

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

    public static String putInCoffreBackPack(String username, String name, String type, String token) throws Exception {
        String json = new Gson().toJson(Map.of(
                "name", name,
                "type", type
        ));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/characters/" + username + "/backpack/coffre/add"))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }


    /**
     * Démarre un combat en envoyant une requête au serveur.
     * @param username Le nom d'utilisateur du joueur qui lance le combat.
     * @param token Le token d'autorisation pour le joueur.
     * @return La réponse JSON du serveur indiquant le résultat du combat.
     * @throws Exception Si une erreur se produit lors de l'appel HTTP ou de la sérialisation.
     */
    public static String startCombat(String username, String token) throws Exception {
        // Préparer le corps de la requête en JSON
        String json = new Gson().toJson(Map.of("username", username));

        // Créer la requête HTTP avec une timeout de 5 secondes
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/combat/start"))
            .timeout(Duration.ofSeconds(5))
            .header("Authorization", "Bearer " + token)
            .POST(HttpRequest.BodyPublishers.ofString(json))  // Utilisation du JSON sérialisé
            .build();

        // Créer un client HTTP et envoyer la requête
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        // Vérifier si la réponse est réussie
        if (response.statusCode() != 200) {
            throw new Exception("Erreur serveur: " + response.statusCode() + " - " + response.body());
        }

        // Retourner le corps de la réponse JSON
        return response.body();
    }

    /*
     * appelle le serveur via httpService.getInventory
     * dése cusstom les objets de mongo vers txt json
     * et retourne bien tout
     * */
    public static Inventory refreshInventory() throws Exception {
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
            .create();
        String jsonInventaire = HttpService.getInventory(Session.getUsername(), Session.getToken());
        return gson.fromJson(jsonInventaire, Inventory.class);
    }

    public static String updateObjectReliability(String username, String objectId, int newReliability, String token) throws Exception {
        String json = new Gson().toJson(Map.of(
                "reliability", newReliability
        ));

        // Construire l'URL correcte
        String url = BASE_URL + "/characters/" + username + "/backpack/update/" + objectId;
        System.out.println("URL: " + url); // Afficher l'URL pour déboguer

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .method("PUT", HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /**
     * Supprime définitivement un objet du backpack d'un utilisateur
     * @param username nom de l'utilisateur
     * @param objectId identifiant unique de l'objet à supprimer
     * @param token JWT d'authentification
     * @return réponse brute du serveur
     * @throws Exception en cas d'erreur réseau ou serveur
     */
    public static String deleteObjectFromBackpack(String username, String objectId, String token) throws Exception {
        String json = new Gson().toJson(Map.of(
                "id", objectId
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/characters/" + username + "/backpack/delete/" + objectId)) //
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }











    /**********************Logout****************/
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

    public static String matcjmaking(String username, String token) throws Exception {
        //crée chaine json avec le username a partir d une map
        //Map.of juste une map qui ne peut etre modifiée apres, apres on seriale (objet java vers text json)
        String json = new Gson().toJson(Map.of("username", username));

        //Création de la requete avec de bons headers
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/matchmaking/find"))
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
            throw new RuntimeException(" " + response.body());
        }
        return response.body();
    }




}

