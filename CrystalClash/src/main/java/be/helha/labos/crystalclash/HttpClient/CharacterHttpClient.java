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
     * Envoie une requête pour sélectionner un personnage pour un utilisateur donné.
     *
<<<<<<< HEAD
     * @param username du user
     * @param characterType type de perso
     * @param token token user
     * @throws Exception exception rencontrée
=======
     * @param username      nom de l'utilisateur
     * @param characterType type du personnage à sélectionner
     * @param token         JWT d'authentification
     * @throws Exception en cas d'erreur réseau ou réponse serveur invalide
>>>>>>> 4af660bdac2875e32a9fc9c4ae8b4472dbed2e5b
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
     * Envoie une requête pour récupérer le personnage associé à un utilisateur.
     *
<<<<<<< HEAD
     * @param username username
     * @param token token user
     * @return response
     * @throws Exception exception
=======
     * @param username nom de l'utilisateur
     * @param token    JWT d'authentification
     * @return         JSON brut représentant le personnage de l'utilisateur
     * @throws Exception en cas d'erreur réseau ou serveur
>>>>>>> 4af660bdac2875e32a9fc9c4ae8b4472dbed2e5b
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
<<<<<<< HEAD
     * Met objet de l'inventaire ds le back en envoyant requete
     *  @param username user
     * @param name name object
     * @param token token
     * @param type type
     * @return  response
     **/
=======
     * Envoie une requête pour ajouter un objet de l'inventaire vers le backpack du personnage.
     *
     * @param username nom de l'utilisateur
     * @param name     nom de l'objet à ajouter
     * @param type     type de l'objet (ex: "Weapon", "Potion")
     * @param token    JWT d'authentification
     * @return         réponse brute du serveur
     * @throws Exception en cas d'erreur réseau ou serveur
     */
>>>>>>> 4af660bdac2875e32a9fc9c4ae8b4472dbed2e5b
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
     * Envoie une requête pour retirer un objet du backpack et le remettre dans l'inventaire.
     *
     * @param username nom de l'utilisateur
     * @param name     nom de l'objet à retirer
     * @param type     type de l'objet
     * @param token    JWT d'authentification
     * @return         réponse brute du serveur
     * @throws Exception en cas d'erreur réseau ou serveur
     */
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


    /**
     * Met à jour la fiabilité (durabilité) d’un objet se trouvant dans le backpack.
     *
     * @param username       nom de l'utilisateur
     * @param objectId       identifiant de l'objet
     * @param newReliability nouvelle valeur de fiabilité
     * @param token          JWT d'authentification
     * @return               réponse brute du serveur
     * @throws Exception en cas d'erreur réseau ou serveur
     */
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


    /**
     * Met à jour la fiabilité (durabilité) d’un objet d’armure équipé.
     *
     * @param username       nom de l'utilisateur
     * @param objectId       identifiant de l'objet d'armure
     * @param newReliability nouvelle valeur de fiabilité
     * @param token          JWT d'authentification
     * @return               réponse brute du serveur
     * @throws Exception en cas d'erreur réseau ou serveur
     */
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
     * Envoie une requête pour ajouter un objet de l'inventaire (armure) vers l'équipement du personnage.
     *
     * @param username nom de l'utilisateur
     * @param name     nom de l'objet à équiper
     * @param type     type de l'objet (ex: "Armor", "Weapon")
     * @param token    JWT d'authentification
     * @return         réponse brute du serveur
     * @throws Exception en cas d'erreur réseau ou serveur
     */
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
     * Envoie une requête pour retirer un objet de l'équipement et le remettre dans l'inventaire.
     *
     * @param username nom de l'utilisateur
     * @param name     nom de l'objet à retirer
     * @param type     type de l'objet
     * @param token    JWT d'authentification
     * @return         réponse brute du serveur
     * @throws Exception en cas d'erreur réseau ou serveur
     */
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
     * Envoie une requête pour transférer un objet du coffre au backpack du personnage.
     *
     * @param username nom de l'utilisateur
     * @param name     nom de l'objet à transférer
     * @param type     type de l'objet
     * @param token    JWT d'authentification
     * @return         réponse brute du serveur
     * @throws Exception en cas d'erreur réseau ou serveur
     */
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
