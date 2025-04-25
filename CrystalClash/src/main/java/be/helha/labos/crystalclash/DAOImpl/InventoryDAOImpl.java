package be.helha.labos.crystalclash.DAOImpl;

import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAO.InventoryDAO;
import be.helha.labos.crystalclash.DeserialiseurCustom.ObjectBasePolymorphicDeserializer;
import be.helha.labos.crystalclash.Inventory.Inventory;
import be.helha.labos.crystalclash.Object.*;
import be.helha.labos.crystalclash.Service.UserService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InventoryDAOImpl implements InventoryDAO {

    @Autowired
    private UserService userService;
    @Autowired
    private UserDAOImpl userDAOImpl;

    /**
     * @param username
     * Crée l inventaire au moment de l'inscription
     * **/
    @Override
    public void createInventoryForUser(String username) {
        try {
            MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
            MongoCollection<Document> collection = mongoDB.getCollection("Inventory");

            Inventory inventory = new Inventory();
            Gson gson = new Gson();
            String json = gson.toJson(inventory);
            Document document = Document.parse(json);
            document.append("username", username);

            collection.insertOne(document);
            System.out.println("Inventaire créé en MongoDB pour " + username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param username
     * Recup l'inventaire du user
     * si pas null ok, on retire id pour pas de confussion
     * ensuite on appelle Gson qui permettra grace au deserialiseurCustom
     * De déserialiser le doc mongo qui est un text json en soit en un objet java pour le monipuler
     * */
    @Override
    public Inventory getInventoryForUser(String username) {
        try {
            MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
            MongoCollection<Document> collection = mongoDB.getCollection("Inventory");

            Document doc = collection.find(new Document("username", username)).first();
            if (doc != null) {
                doc.remove("_id");
                Gson gson = new GsonBuilder()
                    .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                    .create();
                return gson.fromJson(doc.toJson(), Inventory.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Inventory();
    }

    /**
     * @param username
     * @param inventory
     * Permet de sauvegarde l inventaire du user apres toute modif
     * Gson va recup l'objet jave va le sérialiser et puis le mettre ds mongo
     * */
    @Override
    public void saveInventoryForUser(String username, Inventory inventory) {
        try {
            MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
            MongoCollection<Document> collection = mongoDB.getCollection("Inventory");

            //Transforme l'objet Inventory en JSON puis en Doc MongoDB et enregistre.
            //Mongo sait a qui l'inventaire appartient car rajout de username dans Inventory
            Gson gson = new Gson();
            Document updated = Document.parse(gson.toJson(inventory)); // contient la liste des objets

            collection.replaceOne(new Document("username", username), updated); // sauvegarde complète et met a jour l'inventaire du joueur.
            System.out.println("Inventaire mis à jour pour " + username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @param username
     * @param name nom de l'objet
     * @param type type de lobjet
     * Retire l'objet de l(inventaire
     * Donne le cristaux
     * Map parce que ça renvoie plusieurs infos voulues
     *Prend le usernamen, name de l'objet et son type
     * */
    @Override
    public ApiReponse  SellObject(String username, String name, String type) {

        //Stock la reposne
        Map<String, Object> response = new HashMap<>();

        try {
            Inventory inventory = getInventoryForUser(username);

            //cherche l'obejt a vendre ds l'inventaire du joueur en comparant nom et type et stock dans sellObject
            ObjectBase sellObject = null;
            //Parcout tout objet ds inventaire du user
            for (ObjectBase obj : inventory.getObjets()) {
                if (obj.getName() != null && obj.getType() != null &&// verif si type et name pas null
                    obj.getName().equalsIgnoreCase(name) && // si name de l objet egal au nom recherché
                    obj.getType().equalsIgnoreCase(type)) { // pareil ici
                    sellObject = obj; //SI tout ok on sauv ds sellObject
                    break;
                }
            }


            if (sellObject == null) {
                return new ApiReponse("Objet non trouvé dans l'inventaire.", null);
            }

            //retire l'objet de l'inventaire
            //Et appelle saveInventoryForUser pour sauvegarder
            inventory.getObjets().remove(sellObject);
            saveInventoryForUser(username, inventory);

            //Recup info du joueur pour modif ses cristaux
            var userOpt = userService.getUserInfo(username);
            if (userOpt.isEmpty()) {
                return new ApiReponse("Utilisateur introuvable.", null);
            }

            //logqiue pour le calcule de la vente
            int price = sellObject.getPrice();
            int valueSell = price / 2; // divise par deux la valeur de l'obejt
            int cristaux = userOpt.get().getCristaux();
            userService.updateCristaux(username, cristaux + valueSell);

            //Amusement mais au niveau du prix ca détecte sa rareté
            String rarity = "commun";
            if (price > 50 && price <= 100) {
                rarity = "medium";
            } else if (price > 100 && price <= 200) {
                rarity = "high";
            } else if (price > 200) {
                rarity = "Légendaire";
            }

            // Construit le contenu de data
            Map<String, Object> data = new HashMap<>();
            data.put("gain", valueSell);
            data.put("nouveau_solde", cristaux + valueSell);
            data.put("rarity", rarity);
            data.put("value", price);
            data.put("status", true);

            return new ApiReponse("Objet vendu avec succès pour " + valueSell + " cristaux.", data);
        } catch (Exception e) {
            return new ApiReponse("Erreur interne lors de la vente : " + e.getMessage(), null);
        }

    }

    public CoffreDesJoyaux getCoffreDesJoyauxForUser(String username) {
        try {
            Inventory inventory = getInventoryForUser(username);
            if (inventory == null || inventory.getObjets() == null) {
                System.err.println("Inventaire introuvable ou vide pour : " + username);
                return null;
            }
            for (ObjectBase obj : inventory.getObjets()) {
                if (obj instanceof CoffreDesJoyaux) {
                    return (CoffreDesJoyaux) obj;
                }
            }
            System.err.println("Aucun CoffreDesJoyaux trouvé pour : " + username);
            return null;
        } catch (Exception e) {
            System.err.println("Erreur dans getCoffreDesJoyauxForUser : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public ApiReponse addObjectToCoffre(String username, String name, String type) {
        try {
            Inventory inventory = getInventoryForUser(username);
            if (inventory == null) {
                return new ApiReponse("Inventaire introuvable.", null);
            }

            ObjectBase objectToAdd = null;
            CoffreDesJoyaux coffre = null;

            // Parcours des objets pour chercher l'objet ciblé et le coffre
            for (ObjectBase obj : inventory.getObjets()) {
                if (obj.getName().equalsIgnoreCase(name) && obj.getType().equalsIgnoreCase(type)) {
                    objectToAdd = obj;
                }

                if (obj instanceof CoffreDesJoyaux) {
                    coffre = (CoffreDesJoyaux) obj;
                }
            }
            if (objectToAdd == null) {
                return new ApiReponse("Objet non trouvé dans l'inventaire.", null);
            }

            if (coffre == null) {
                return new ApiReponse("Aucun Coffre des Joyaux trouvé dans votre inventaire.", null);
            }

            // Optionnel : simuler une fiabilité qui diminue à chaque ajout
            if (coffre.getReliability() <= 0) {
                return new ApiReponse("Le coffre est brisé et ne peut plus être utilisé.", null);
            }

            // Check de capacité
            if (coffre.getContenu().size() >= coffre.getMaxCapacity()) {
                return new ApiReponse("Le coffre est plein.", null);
            }

            // Suppression de l’objet de l’inventaire
            inventory.getObjets().remove(objectToAdd);

            // Ajout dans le coffre
            coffre.getContenu().add(objectToAdd);

            // Sauvegarde finale
            saveInventoryForUser(username, inventory);

            return new ApiReponse("Objet ajouté au Coffre des Joyaux avec succès.", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiReponse("Erreur lors de l'ajout au coffre : " + e.getMessage(), null);
        }
    }


}
