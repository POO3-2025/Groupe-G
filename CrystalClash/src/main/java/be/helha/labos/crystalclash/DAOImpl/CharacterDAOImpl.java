package be.helha.labos.crystalclash.DAOImpl;

import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAO.CharacterDAO;
import be.helha.labos.crystalclash.DeserialiseurCustom.ObjectBasePolymorphicDeserializer;
import be.helha.labos.crystalclash.Inventory.Inventory;
import be.helha.labos.crystalclash.Object.*;
import be.helha.labos.crystalclash.Service.CharacterService;
import be.helha.labos.crystalclash.Service.InventoryService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class CharacterDAOImpl implements CharacterDAO {
    @Autowired
    private InventoryService inventoryService;

    /**
     * Récupère le type de personnage pour un utilisateur
     *
     * @param username Nom d'utilisateur
     * @return Le type de personnage
     */
    @Override
    public String getCharacterForUser(String username) {
        MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
        MongoCollection<Document> collection = mongoDB.getCollection("Characters");
        // Cherche le personnage sélectionné pour cet utilisateur
        Document result = collection.find(
                //eq = equivalent à, comme un where en sql
                Filters.and(
                        Filters.eq("username", username),
                        Filters.eq("selected", true)
                )
        ).first(); //retourne le premier touvé

        if (result != null) {
            return result.getString("type");
        }
        return null;
    }


    /**
     * Sauvegarde le personnage pour un utilisateur
     *
     * @param username      Nom d'utilisateur
     * @param characterType Type de personnage
     */
    /*
     * Modif apportées, reiteré la ligne qui supprimait les anciens persos du joueur
     * ajoute le perso si il n'existe pas if (existing == null)
     * */
    @Override
    public void saveCharacterForUser(String username, String characterType) {
        MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
        MongoCollection<Document> collection = mongoDB.getCollection("Characters");
        Document doc = new Document("username", username).append("type", characterType);
        Document existing = collection.find(doc).first();

        if (existing == null) {
            // Ajout du personnage + backpack vide
            Document backpack = new Document("objets", List.of());
            //Si premier perso du user on le met a 1
            long count = collection.countDocuments(new Document("username", username));
            boolean IsFirst = (count == 0);
            Document docu = new Document("username", username)
                    .append("type", characterType)
                    .append("backpack", backpack)
                    .append("selected",IsFirst);
            collection.insertOne(docu);
        }
    }

    /**
     * Crée un backPack pour un nouveau perso choisi
     * si deja BackPack alors on le garde
     *
     * @param username      Nom d'utilisateur
     * @param characterType Type de personnage
     */
    @Override
    public void createBackPackForCharacter(String username, String characterType) {
        MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
        MongoCollection<Document> collection = mongoDB.getCollection("Characters");

        // Filtre  retrouver le document du perso
        Document filtre = new Document("username", username).append("type", characterType);
        Document doc = collection.find(filtre).first();

        // S'il existe déjà et qu'il a un backpack, RIen faire
        if (doc != null && doc.containsKey("backpack")) {
            return; //si existe deja retourn rien
        }
        //Si pas deja, crée un
        Document backpack = new Document("objets", List.of());
        Document update = new Document("$set", new Document("backpack", backpack));
        collection.updateOne(filtre, update);
    }

    /**
    Récupère le backpack du personnage du joueur*
    @param username Nom d'utilisateur
            @return Le backpack du personnage*/
    @Override
    public BackPack getBackPackForCharacter(String username) {
        try {
            MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
            MongoCollection<Document> collection = mongoDB.getCollection("Characters");



            Document doc = collection.find(new Document("username", username).append("selected",true)).first();

            if (doc != null && doc.containsKey("backpack")) {
                Document backpackDoc = (Document) doc.get("backpack");
                doc.remove("_id"); // important
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                        .create();
                return gson.fromJson(backpackDoc.toJson(), BackPack.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new BackPack(); // retourne un backpack vide si erreur ou pas trouvé
    }

    /**
     * Met a jour le personnage sélectionné pour un utilisateur
     *
     * @param username      Nom d'utilisateur
     * @param characterType Type de personnage
     */
    @Override
    public void setSelectedCharacter(String username, String characterType) {
        MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
        MongoCollection<Document> collection = mongoDB.getCollection("Characters");

        //  désélectionner tous les persos de ce joueur
        //Prend tous les persos du user et met tout a faux
        //Met tout a faux car en passant par l'update mondo c'est lequel est selectionné avec le username et type
        collection.updateMany(
                new Document("username", username),
                new Document("$set", new Document("selected", false))
        );

        // Puis marquer le bon perso comme sélectionné
        //Cible le bon perso et son username pour le mettre a true
        //Mongo guidé avec ce filtre si dessous
        collection.updateOne(
                new Document("username", username).append("type", characterType),
                new Document("$set", new Document("selected", true))
        );
    }

    /**
     * sauve le backpack du personnage du joueur
     *
     * @param username
     * @param backPack
     */
    @Override
    public void saveBackPackForCharacter(String username, BackPack backPack) {
        try {
            MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
            MongoCollection<Document> collection = mongoDB.getCollection("Characters");

            // Construction de la liste d'objets à insérer dans Mongo
            List<Document> objetsDocuments = new ArrayList<>();
            for (ObjectBase obj : backPack.getObjets()) {


                if (obj == null || obj.getType() == null) {
                    System.err.println("Objet null ou sans type dans le backpack de " + username);
                    continue;
                }
   System.out.println("Objet cassé" + obj.getName());

                if ((obj instanceof Weapon weapon && weapon.getReliability() == 0) || (obj instanceof Armor armor && armor.getReliability() == 0)){
                    System.out.println("Supprimer objet" + obj.getName());

                    continue;
                }

                Document doc = new Document()
                        .append("id", obj.getId())
                        .append("name", obj.getName())
                        .append("type", obj.getType())
                        .append("price", obj.getPrice())
                        .append("requiredLevel", obj.getRequiredLevel());

                switch (obj.getType().toLowerCase()) {
                    case "weapon" -> {
                        Weapon weapon = (Weapon) obj;
                        doc.append("reliability", weapon.getReliability())
                                .append("damage", weapon.getDamage());
                    }
                    case "armor" -> {
                        Armor armor = (Armor) obj;
                        doc.append("reliability", armor.getReliability())
                                .append("bonusPV", armor.getBonusPV());
                    }
                    case "healingpotion" -> {
                        HealingPotion potion = (HealingPotion) obj;
                        doc.append("heal", potion.getHeal());
                    }
                    case "potionofstrenght" -> {
                        PotionOfStrenght potion = (PotionOfStrenght) obj;
                        doc.append("bonusATK", potion.getBonusATK());
                    }
                    case "coffredesjoyaux" -> {
                        CoffreDesJoyaux coffre = (CoffreDesJoyaux) obj;
                        List<Document> contenuDocs = new ArrayList<>();
                        for (ObjectBase item : coffre.getContenu()) {
                            Document itemDoc = new Document()
                                    .append("id", item.getId())
                                    .append("name", item.getName())
                                    .append("type", item.getType())
                                    .append("price", item.getPrice())
                                    .append("requiredLevel", item.getRequiredLevel());

                            // Ajouter les champs spécifiques selon le type
                            switch (item.getType().toLowerCase()) {
                                case "weapon" -> {
                                    Weapon weapon = (Weapon) item;
                                    itemDoc.append("reliability", weapon.getReliability())
                                            .append("damage", weapon.getDamage());
                                }
                                case "armor" -> {
                                    Armor armor = (Armor) item;
                                    itemDoc.append("reliability", armor.getReliability())
                                            .append("bonusPV", armor.getBonusPV());
                                }
                                case "healingpotion" -> {
                                    HealingPotion potion = (HealingPotion) item;
                                    itemDoc.append("heal", potion.getHeal());
                                }
                                case "potionofstrenght" -> {
                                    PotionOfStrenght potion = (PotionOfStrenght) item;
                                    itemDoc.append("bonusATK", potion.getBonusATK());
                                }
                            }

                            contenuDocs.add(itemDoc);
                        }
                        doc.append("contenu", contenuDocs);
                    }
                    default -> {
                        System.err.println("Type d'objet inconnu : " + obj.getType());
                    }
                }

                objetsDocuments.add(doc);
            }

            // Construction et exécution de l’update
            Document filtre = new Document("username", username);
            Document backpackUpdate = new Document("objets", objetsDocuments);
            Document update = new Document("$set", new Document("backpack", backpackUpdate));

            collection.updateOne(filtre, update);

        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde du backpack : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ajoute un objet au backpack du personnage du joueur
     *
     * @param username Nom d'utilisateur
     * @param name     Nom de l'objet
     * @param type     Type de l'objet
     * @return Réponse de l'API reponse
     */
    @Override
    public ApiReponse addObjectToBackPack(String username, String name, String type) {
        try {
            Inventory inventory = inventoryService.getInventoryForUser(username);
            BackPack backpack = getBackPackForCharacter(username);

            if (backpack.getObjets().size() >= 5) {
                return new ApiReponse("Backpack plein !", null);
            }

            ObjectBase objectToAdd = null;
            boolean isCoffre = false; // Pour savoir si c'était un coffre ou un simple objet

            // Chercher directement dans l'inventaire
            objectToAdd = inventory.getObjets().stream()
                    .filter(obj -> obj.getName().equals(name) && obj.getType().equals(type))
                    .findFirst()
                    .orElse(null);

            // 2. Si pas trouvé directement => chercher dans les coffres
            if (objectToAdd == null) {
                for (ObjectBase obj : inventory.getObjets()) {
                    if (obj instanceof CoffreDesJoyaux) {
                        CoffreDesJoyaux coffre = (CoffreDesJoyaux) obj;

                        ObjectBase foundInCoffre = coffre.getContenu().stream()
                                .filter(o -> o.getName().equals(name) && o.getType().equals(type))
                                .findFirst()
                                .orElse(null);

                        if (foundInCoffre != null) {
                            coffre.getContenu().remove(foundInCoffre); // Retire de l'intérieur du coffre
                            objectToAdd = foundInCoffre;
                            break;
                        }

                        // Si le coffre lui-même correspond
                        if (coffre.getName().equals(name) && coffre.getType().equals(type)) {
                            CoffreDesJoyaux copie = new CoffreDesJoyaux();
                            copie.setName(coffre.getName());
                            copie.setType(coffre.getType());
                            copie.setPrice(coffre.getPrice());
                            copie.setRequiredLevel(coffre.getRequiredLevel());
                            copie.setReliability(coffre.getReliability());
                            copie.setContenu(new ArrayList<>(coffre.getContenu()));

                            objectToAdd = copie;
                            isCoffre = true;
                            break;
                        }
                    }
                }
            }

            if (objectToAdd == null) {
                return new ApiReponse("Objet non trouvé dans l'inventaire ni dans un coffre.", null);
            }

            // Ajouter au backpack
            if (!backpack.AddObjects(objectToAdd)) {
                return new ApiReponse("Erreur lors de l'ajout de l'objet au backpack.", null);
            }

            // 4. Retirer de l'inventaire uniquement si ce n'était pas un objet du coffre
            if (!isCoffre) {
                inventory.retirerObjet(objectToAdd);
            } else {
                // Si c'était un coffre, retirer l'original
                inventory.getObjets().removeIf(obj -> obj instanceof CoffreDesJoyaux && obj.getName().equals(name));
            }

            // 5. Sauvegarder
            inventoryService.saveInventoryForUser(username, inventory);
            saveBackPackForCharacter(username, backpack);

            return new ApiReponse("Objet ajouté au backpack avec succès !", backpack);

        } catch (Exception e) {
            return new ApiReponse("Erreur lors de l'ajout de l'objet au backpack : " + e.getMessage(), null);
        }
    }


    /**
     * Supprime un objet du backpack du personnage du joueur
     *
     * @param username Nom d'utilisateur
     * @param name     Nom de l'objet
     * @return Réponse de l'API reponse
     */
    @Override
    public ApiReponse removeObjectFromBackPack(String username, String name) {
        try {
            BackPack backpack = getBackPackForCharacter(username);
            Inventory inventory = inventoryService.getInventoryForUser(username);

            if (backpack == null) {
                return new ApiReponse("Backpack introuvable.", null);
            }
            if (backpack.getObjets().isEmpty()) {
                return new ApiReponse("Backpack vide !", null);
            }

            ObjectBase objectToRemove = backpack.getObjets().stream()
                    .filter(obj -> obj.getName().equals(name))
                    .findFirst()
                    .orElse(null);

            if (objectToRemove == null) {
                return new ApiReponse("Objet non trouvé dans le backpack.", null);
            }

            backpack.removeObject(objectToRemove);
            if (inventory.getObjets().size() > 30) {
                return new ApiReponse("Inventaire plein !", null);
            }
            if (!inventory.ajouterObjet(objectToRemove)) {
                return new ApiReponse("Erreur lors de l'ajout de l'objet à l'inventaire.", null);
            }
            saveBackPackForCharacter(username, backpack);
            inventoryService.saveInventoryForUser(username, inventory);
            System.out.println("Objet retiré du backpack : " + objectToRemove.getName());
            return new ApiReponse("Objet retiré du backpack avec succès !", backpack);

        } catch (Exception e) {
            return new ApiReponse("Erreur lors de la suppression de l'objet du backpack : " + e.getMessage(), null);
        }
    }

    @Override
    public ApiReponse addObjectToCoffre(String username, String name, String type) {
        try {
             BackPack backPack = getBackPackForCharacter(username);
            if (backPack == null) {
                return new ApiReponse("BackPack introuvable.", null);
            }

            ObjectBase objectToAdd = null;
            CoffreDesJoyaux coffre = null;

            // Parcours des objets pour chercher l'objet ciblé et le coffre
            for (ObjectBase obj : backPack.getObjets()) {
                if (obj.getName().equalsIgnoreCase(name) && obj.getType().equalsIgnoreCase(type)) {
                    objectToAdd = obj;
                }

                if (obj instanceof CoffreDesJoyaux) {
                    coffre = (CoffreDesJoyaux) obj;
                }
            }
            if (objectToAdd == null) {
                return new ApiReponse("Objet non trouvé dans le BackPack.", null);
            }

            if (coffre == null) {
                return new ApiReponse("Aucun Coffre des Joyaux trouvé dans votre BackPack.", null);
            }

            if (coffre.getReliability() <= 0) {
                return new ApiReponse("Le coffre est brisé et ne peut plus être utilisé.", null);
            }

            if (coffre.getContenu().size() >= coffre.getMaxCapacity()) {
                return new ApiReponse("Le coffre est plein.", null);
            }

            backPack.getObjets().remove(objectToAdd);

            coffre.getContenu().add(objectToAdd);

            saveBackPackForCharacter(username, backPack);

            return new ApiReponse("Objet ajouté au BackPack des Joyaux avec succès.", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiReponse("Erreur lors de l'ajout au coffre : " + e.getMessage(), null);
        }
    }

    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Modifie la reliability d'un objet (weapon ou armor) dans le backpack du personnage du joueur
     *
     * @param username Nom d'utilisateur
     * @param objectId ID de l'objet à modifier
     * @param newReliability Nouvelle valeur de reliability
     * @return Réponse de l'API
     */
    @Override
    public ApiReponse updateReliabilityInBackPack(String username, String objectId, int newReliability) {
        try {
            BackPack backpack = getBackPackForCharacter(username);

            boolean found = false;

            for (ObjectBase obj : backpack.getObjets()) {
                if (obj.getId().equals(objectId)) {
                    // Vérifie que c'est bien une Weapon ou Armor
                    if (obj instanceof Weapon weapon) {
                        weapon.setReliability(newReliability);
                        found = true;
                        break;
                    } else if (obj instanceof Armor armor) {
                        armor.setReliability(newReliability);
                        found = true;
                        break;
                    } else {
                        return new ApiReponse("L'objet trouvé n'a pas de reliability (ce n'est ni une arme ni une armure).", null);
                    }
                }
            }

            if (!found) {
                return new ApiReponse("Objet avec l'ID spécifié non trouvé dans le backpack.", null);
            }

            // Sauvegarder le backpack après modification
            saveBackPackForCharacter(username, backpack);

            return new ApiReponse("Reliability de l'objet modifiée avec succès.", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiReponse("Erreur lors de la modification de la reliability : " + e.getMessage(), null);
        }
    }
    @Override
    public ApiReponse deleteObjectFromBackPack(String username, String objectId) {
        try {
            // Récupérer le backpack actuel du personnage sélectionné
            BackPack backpack = getBackPackForCharacter(username);

            if (backpack == null || backpack.getObjets().isEmpty()) {
                return new ApiReponse("Backpack vide ou introuvable.", null);
            }

            // Chercher l'objet à supprimer en utilisant l'objectId
            ObjectBase objectToRemove = backpack.getObjets().stream()
                    .filter(obj -> obj.getId().equals(objectId))  // On compare par l'ID de l'objet
                    .findFirst()
                    .orElse(null);

            if (objectToRemove == null) {
                return new ApiReponse("Objet non trouvé dans le backpack.", null);
            }

            // Supprimer l'objet
            backpack.getObjets().remove(objectToRemove);

            // Sauvegarder le nouveau backpack sans l'objet
            saveBackPackForCharacter(username, backpack);

            return new ApiReponse("Objet supprimé du backpack.", objectToRemove);

        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression de l'objet du backpack : " + e.getMessage());
            e.printStackTrace();
            return new ApiReponse("Erreur interne lors de la suppression de l'objet.", null);
        }
    }




}



