package be.helha.labos.crystalclash.DAOImpl;

import be.helha.labos.crystalclash.ApiResponse.ApiReponse;
import be.helha.labos.crystalclash.ConfigManagerMysql_Mongo.ConfigManager;
import be.helha.labos.crystalclash.DAO.CharacterDAO;
import be.helha.labos.crystalclash.DeserialiseurCustom.ObjectBasePolymorphicDeserializer;
import be.helha.labos.crystalclash.DTO.Inventory;
import be.helha.labos.crystalclash.Object.*;
import be.helha.labos.crystalclash.Service.InventoryService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
            Document equipment = new Document("armor", List.of());

            // Si c'est le premier personnage de l'utilisateur, on le met à "selected" = true
            long count = collection.countDocuments(new Document("username", username));
            boolean isFirst = (count == 0);

            // Création du document du personnage
            Document docu = new Document("username", username)
                    .append("type", characterType)
                    .append("backpack", backpack)
                    .append("equipment", equipment) // Ajout de l'équipement
                    .append("selected", isFirst);

            // Insertion du document dans la collection
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
     * Crée un équipement pour un personnage
     * @param username le nom d'utilisateur
     * @param characterType le type de personnage
     */
    @Override
    public void createEquipmentForCharacter(String username, String characterType) {
        MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
        MongoCollection<Document> collection = mongoDB.getCollection("Characters");

        // Filtre pour retrouver le document du personnage
        Document filtre = new Document("username", username).append("type", characterType);
        Document doc = collection.find(filtre).first();

        // S'il existe déjà et qu'il a un équipement, ne rien faire
        if (doc != null && doc.containsKey("equipment")) {
            return; // Si un équipement existe déjà, on ne fait rien
        }

        // Si pas déjà, crée un équipement vide (pas d'armure initialement)
        Document equipment = new Document("armor", List.of());
        Document update = new Document("$set", new Document("equipment", equipment));
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
     * Récupère l'équipement du personnage du joueur
     *
     * @param username Nom d'utilisateur
     * @return L'équipement du personnage
     */
    @Override
    public Equipment getEquipmentForCharacter(String username) {
        Equipment equipment = new Equipment(); // On prépare l'équipement (même si vide en cas d'erreur)

        try {
            // Connexion à la base MongoDB
            MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
            MongoCollection<Document> collection = mongoDB.getCollection("Characters");

            // Recherche du personnage sélectionné par nom d'utilisateur
            Document doc = collection.find(new Document("username", username).append("selected", true)).first();

            if (doc != null && doc.containsKey("equipment")) {
                Document equipmentDoc = (Document) doc.get("equipment");

                // Vérifie la présence du champ "armor"
                if (equipmentDoc.containsKey("armor")) {
                    Object armorField = equipmentDoc.get("armor");
                    System.out.println("Raw armor field: " + armorField);
                    System.out.println("Class of armor field: " + armorField.getClass());

                    // Prépare Gson avec le désérialiseur polymorphique
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                            .create();

                    // Désérialisation propre de la liste d'armures
                    if (armorField instanceof List<?>) {
                        @SuppressWarnings("unchecked")
                        List<Document> armorDocs = (List<Document>) armorField;

                        // Conversion correcte en JSON
                        String armorJson = gson.toJson(armorDocs);
                        System.out.println("Armor JSON: " + armorJson);

                        // Désérialise la liste en objets Java
                        Type armorListType = new TypeToken<List<ObjectBase>>() {}.getType();
                        List<ObjectBase> armorList = gson.fromJson(armorJson, armorListType);
                        System.out.println("Armor list deserialized: " + armorList);

                        // Ajoute chaque armure dans l'équipement
                        for (ObjectBase armor : armorList) {
                            equipment.AddArmor(armor);
                        }

                        System.out.println("Equipment récupéré : " + equipment.getObjets());
                    } else {
                        System.out.println("Le champ 'armor' n'est pas une liste valide.");
                    }
                } else {
                    System.out.println("Le champ 'armor' est absent de l'équipement.");
                }
            } else {
                System.out.println("Aucun personnage trouvé ou pas d'équipement.");
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de l'équipement pour " + username);
            e.printStackTrace();
        }

        return equipment; // Retourne l'équipement (vide si erreur ou pas d'armure)
    }

    /**
     * Sauvegarde l'équipement du personnage du joueur
     * @param username le nom d'utilisateur
     * @param equipment l'objet Equipment à sauvegarder
     */
    @Override
    public void saveEquipmentForCharacter(String username, Equipment equipment) {
        try {
            MongoDatabase mongoDB = ConfigManager.getInstance().getMongoDatabase("MongoDBProduction");
            MongoCollection<Document> collection = mongoDB.getCollection("Characters");

            // Construction de la liste d'armures à insérer dans Mongo
            List<Document> objetsDocuments = new ArrayList<>();

            for (ObjectBase obj : equipment.getObjets()) {
                // Vérifier si l'objet est une instance d'Armor et si son type est valide
                if (obj == null || !(obj instanceof Armor)) {
                    System.err.println("Objet non valide ou non armure dans l'équipement de " + username);
                    continue;
                }

                Armor armor = (Armor) obj;  // Cast vers Armor

                // Si l'armure a une fiabilité de 0, on l'ignore
                if (armor.getReliability() == 0) {
                    System.out.println("Supprimer armure (fiabilité 0) : " + armor.getName());
                    continue;
                }

                // Construction du document de l'armure à insérer
                Document doc = new Document()
                        .append("id", armor.getId())
                        .append("name", armor.getName())
                        .append("type", armor.getType()) // Assurez-vous que c'est bien "armor"
                        .append("price", armor.getPrice())
                        .append("requiredLevel", armor.getRequiredLevel())
                        .append("reliability", armor.getReliability())
                        .append("bonusPV", armor.getBonusPV());  // Bonus de PV de l'armure

                objetsDocuments.add(doc);
            }

            Document filtre = new Document("username", username).append("selected", true);
            Document EquipmentUpdate = new Document("armor", objetsDocuments);
            Document update = new Document("$set", new Document("equipment", EquipmentUpdate));

            collection.updateOne(filtre, update);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la sauvegarde de l'équipement pour " + username);
        }
    }
    /**
     * Ajoute une armure à l'équipement du personnage du joueur
     *
     * @param username Nom d'utilisateur
     * @param name     Nom de l'objet
     * @param type     Type de l'objet
     * @return Réponse de l'API reponse
     */
    @Override
    public ApiReponse addArmorToEquipment(String username, String name, String type) {
        try {
            // Récupérer l'inventaire et l'équipement du personnage
            Inventory inventory = inventoryService.getInventoryForUser(username);
            Equipment equipment = getEquipmentForCharacter(username);
            System.out.println(equipment);
            // Vérifier si l'équipement contient déjà trop d'objets
            if (equipment.getObjets().size() > 0) {
                return new ApiReponse("L'équipement est plein !", null);
            }

            ObjectBase objectToAdd = null;
            boolean isCoffre = false; // Pour savoir si l'objet trouvé est dans un coffre

            // Chercher l'objet directement dans l'inventaire
            objectToAdd = inventory.getObjets().stream()
                    .filter(obj -> obj.getName().equals(name) && obj.getType().equals(type))
                    .filter(obj -> obj instanceof Armor) // Filtrer uniquement les armures
                    .findFirst()
                    .orElse(null);

            // Si l'objet n'est pas trouvé directement, on vérifie dans les coffres
            if (objectToAdd == null) {
                for (ObjectBase obj : inventory.getObjets()) {
                    if (obj instanceof CoffreDesJoyaux) {
                        CoffreDesJoyaux coffre = (CoffreDesJoyaux) obj;

                        // Chercher l'armure dans le coffre
                        ObjectBase foundInCoffre = coffre.getContenu().stream()
                                .filter(o -> o.getName().equals(name) && o.getType().equals(type))
                                .filter(o -> o instanceof Armor) // Filtrer uniquement les armures dans le coffre
                                .findFirst()
                                .orElse(null);

                        if (foundInCoffre != null) {
                            coffre.getContenu().remove(foundInCoffre); // Retirer l'armure du coffre
                            objectToAdd = foundInCoffre;
                            break;
                        }

                        // Si le coffre lui-même correspond à l'armure
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

            // Si l'objet n'est toujours pas trouvé, retourner une erreur
            if (objectToAdd == null) {
                return new ApiReponse("Armure non trouvée dans l'inventaire ni dans un coffre.", null);
            }

            // Ajouter l'armure à l'équipement
            if (!equipment.AddArmor(objectToAdd)) {
                return new ApiReponse("Erreur lors de l'ajout de l'armure à l'équipement.", null);
            }

            // Retirer l'armure de l'inventaire ou du coffre, si nécessaire
            if (!isCoffre) {
                inventory.retirerObjet(objectToAdd);
            } else {
                inventory.getObjets().removeIf(obj -> obj instanceof CoffreDesJoyaux && obj.getName().equals(name));
            }

            // Sauvegarder les modifications
            inventoryService.saveInventoryForUser(username, inventory);
            saveEquipmentForCharacter(username, equipment);

            return new ApiReponse("Armure ajoutée à l'équipement avec succès !", equipment);

        } catch (Exception e) {
            return new ApiReponse("Erreur lors de l'ajout de l'armure à l'équipement : " + e.getMessage(), null);
        }
    }

    /**
     * Retire une armure de l'équipement du personnage du joueur
     * @param username le nom d'utilisateur
     * @param name le nom de l'armure à retirer
     * @return
     */
    @Override
    public ApiReponse removeArmorFromEquipment(String username, String name) {
        try {
            // Récupérer l'équipement et l'inventaire du personnage
            Equipment equipment = getEquipmentForCharacter(username);
            Inventory inventory = inventoryService.getInventoryForUser(username);

            if (equipment == null) {
                return new ApiReponse("Équipement introuvable.", null);
            }
            if (equipment.getObjets().isEmpty()) {
                return new ApiReponse("Équipement vide !", null);
            }

            // Chercher l'armure dans l'équipement
            ObjectBase objectToRemove = equipment.getObjets().stream()
                    .filter(obj -> obj.getName().equals(name))
                    .filter(obj -> obj instanceof Armor) // Filtrer pour s'assurer que c'est une armure
                    .findFirst()
                    .orElse(null);

            if (objectToRemove == null) {
                return new ApiReponse("Armure non trouvée dans l'équipement.", null);
            }

            // Retirer l'armure de l'équipement
            equipment.removeArmor(objectToRemove);

            // Vérifier si l'inventaire peut accueillir l'armure
            if (inventory.getObjets().size() >= 30) {
                return new ApiReponse("Inventaire plein !", null);
            }

            // Ajouter l'armure à l'inventaire
            if (!inventory.ajouterObjet(objectToRemove)) {
                return new ApiReponse("Erreur lors de l'ajout de l'armure à l'inventaire.", null);
            }

            // Sauvegarder les changements dans l'équipement et l'inventaire
            saveEquipmentForCharacter(username, equipment);
            inventoryService.saveInventoryForUser(username, inventory);

            System.out.println("Armure retirée de l'équipement : " + objectToRemove.getName());
            return new ApiReponse("Armure retirée de l'équipement avec succès !", equipment);

        } catch (Exception e) {
            return new ApiReponse("Erreur lors de la suppression de l'armure de l'équipement : " + e.getMessage(), null);
        }
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

                            if ((item instanceof Weapon weapon && weapon.getReliability() == 0) ||
                                    (item instanceof Armor armor && armor.getReliability() == 0)) {
                                System.out.println("Suppression objet cassé dans coffre : " + item.getName());
                                continue;
                            }
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
            Document filtre = new Document("username", username).append("selected", true);
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

    /**
     * Ajoute un objet au coffre des joyaux du personnage du joueur
     * @param username le nom d'utilisateur
     * @param name le nom de l'objet
     * @param type le type de l'objet
     * @return
     */
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
                System.out.println("Objet backpack: " + obj.getId());

                // 1. Objet direct dans le backpack
                if (obj.getId().equals(objectId)) {
                    if (obj instanceof Weapon weapon) {
                        weapon.setReliability(newReliability);
                        found = true;
                        break;
                    } else if (obj instanceof Armor armor) {
                        armor.setReliability(newReliability);
                        found = true;
                        break;
                    } else {
                        return new ApiReponse("L'objet trouvé n'est ni une arme ni une armure.", null);
                    }
                }

                // 2. Si objet est un coffre, parcourir son contenu
                else if (obj instanceof CoffreDesJoyaux coffre) {
                    for (ObjectBase inner : coffre.getContenu()) {
                        System.out.println(" -> Contenu coffre: " + inner.getId());

                        if (inner.getId().equals(objectId)) {
                            if (inner instanceof Weapon w) {
                                w.setReliability(newReliability);
                                found = true;
                            } else if (inner instanceof Armor a) {
                                a.setReliability(newReliability);
                                found = true;
                            } else {
                                return new ApiReponse("L'objet dans le coffre n'est ni une arme ni une armure.", null);
                            }
                            break;
                        }
                    }
                    if (found) break;
                }
            }

            if (!found) {
                return new ApiReponse("Objet avec l'ID spécifié non trouvé dans le backpack ni dans les coffres.", null);
            }

            saveBackPackForCharacter(username, backpack);
            return new ApiReponse("Reliability de l'objet modifiée avec succès.", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiReponse("Erreur lors de la modification : " + e.getMessage(), null);
        }
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
    public ApiReponse updateReliabilityInEquipment(String username, String objectId, int newReliability) {
        try {
            Equipment equipment = getEquipmentForCharacter(username);

            boolean found = false;

            for (ObjectBase obj : equipment.getObjets()) {
                if (obj.getId().equals(objectId)) {
                    // Vérifie que c'est bien une Weapon ou Armor
                    if (obj instanceof Armor armor) {
                        System.out.println("Objet trouvé: " + obj.getName() + ", ID: " + obj.getId() + ", Classe: " + obj.getClass().getName());

                        armor.setReliability(newReliability);
                        System.out.println("Nouvelle reliability mise à jour: " + armor.getReliability());

                        found = true;
                        break;
                    } else {
                        return new ApiReponse("L'objet trouvé n'a pas de reliability.", null);
                    }
                }
            }
            if (!found) {
                return new ApiReponse("Objet avec l'ID spécifié non trouvé dans le backpack.", null);
            }

            // Sauvegarder le backpack après modification
            saveEquipmentForCharacter(username, equipment);

            return new ApiReponse("Reliability de l'objet modifiée avec succès.", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiReponse("Erreur lors de la modification de la reliability : " + e.getMessage(), null);
        }
    }

    /**
     * Supprime un objet du backpack du personnage du joueur
     * @param username le nom d'utilisateur
     * @param objectId l'identifiant de l'objet
     * @return
     */
    @Override
    public ApiReponse deleteObjectFromBackPack(String username, String objectId) {
        try {
            BackPack backpack = getBackPackForCharacter(username);

            if (backpack == null || backpack.getObjets().isEmpty()) {
                return new ApiReponse("Backpack vide ou introuvable.", null);
            }

            // 1. Supprimer si l'objet est directement dans le backpack
            Iterator<ObjectBase> iterator = backpack.getObjets().iterator();
            while (iterator.hasNext()) {
                ObjectBase obj = iterator.next();
                if (obj.getId().equals(objectId)) {
                    iterator.remove();
                    saveBackPackForCharacter(username, backpack);
                    return new ApiReponse("Objet supprimé du backpack.", obj);
                }

                // 2. Si c’est un coffre, chercher dedans
                if (obj instanceof CoffreDesJoyaux coffre) {
                    Iterator<ObjectBase> contenuIter = coffre.getContenu().iterator();
                    while (contenuIter.hasNext()) {
                        ObjectBase item = contenuIter.next();
                        if (item.getId().equals(objectId)) {
                            contenuIter.remove();
                            saveBackPackForCharacter(username, backpack);
                            return new ApiReponse("Objet supprimé du coffre.", item);
                        }
                    }
                }
            }

            return new ApiReponse("Objet non trouvé dans le backpack ou les coffres.", null);

        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression de l'objet du backpack : " + e.getMessage());
            e.printStackTrace();
            return new ApiReponse("Erreur interne lors de la suppression de l'objet.", null);
        }
    }


}



