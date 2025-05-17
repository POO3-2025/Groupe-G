package be.helha.labos.crystalclash.HttpClient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class CharacterHttpClient {

    private static final String BASE_URL = HttpConfig.getBaseUrl();


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
     * Met objet de l'inventaire ds le back en envoyant requete
     *  @param username
     * @param name
     * @param token
     * @param type
     **/
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
     **/
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
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }



    public static String updateObjectReliability(String username, String objectId, int newReliability, String token) throws Exception {
        String json = new Gson().toJson(Map.of(
                "reliability", newReliability
        ));

        // Construire l'URL correcte
        String url = BASE_URL + "/characters/" + username + "/backpack/update/" + objectId;


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

    public static String updateArmorReliability(String username, String objectId, int newReliability, String token) throws Exception {
        String json = new Gson().toJson(Map.of(
                "reliability", newReliability
        ));

        // Construire l'URL correcte
        String url = BASE_URL + "/characters/" + username + "/equipment/update/" + objectId;

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
                .uri(URI.create(BASE_URL + "/characters/" + username + "/backpack/delete/" + objectId))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }


    /**
     * Récupère l'armure du personnage d'un utilisateur
     *
     * @param username nom de l'utilisateur
     * @param token    JWT d'authentification
     * @return JSON brut contenant l'equipement
     * @throws Exception en cas d'erreur réseau
     */
    public static String getEquipment(String username, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/characters/" + username + "/equipment"))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Erreur lors de la récupération de l'equipement : " + response.body());
        }

        return response.body(); // Contient un tableau JSON : [{"name":"...","price":...}, ...]
    }


    /**
     * Met l'armure de l'inventaire ds l'equipement en envoyant requete
     *  @param username
     * @param name
     * @param token
     * @param type
     **/
    public static String putInEquipment(String username, String name, String type, String token) throws Exception {
        String json = new Gson().toJson(Map.of(
                "name", name,
                "type", type
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/characters/" + username + "/equipment/add"))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /**
     * Retire de l'equipement pour mettre dans inventaire, envoie requete
     * @param token
     * @param name
     * @param username
     * @param type
     **/
    public static String removeFromEquipment(String username, String name, String type, String token) throws Exception {
        String json = new Gson().toJson(Map.of(
                "name", name,
                "type", type
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/characters/" + username + "/equipment/remove")) // on utilise le bon endpoint
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
     * Permet de mettre le coffre dans le backpack
     *
     * **/
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
}