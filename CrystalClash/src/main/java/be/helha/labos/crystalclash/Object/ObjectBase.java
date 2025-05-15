
package be.helha.labos.crystalclash.Object;


import java.util.UUID;

/**
 * Classe de base pour les objets du jeu.
 * Cette classe contient les attributs et méthodes communs à tous les objets.
 */
public class ObjectBase {
    private String name;
    protected int price;
    protected int requiredLevel;
    protected int reliability;
    private String type;
    private String id = UUID.randomUUID().toString(); //Génereation automatique d'UID pour les objects
    public ObjectBase() {
        // Constructeur par défaut requis pour la désérialisation
    }

    /**
     * Constructeur de la classe ObjectBase.
     * @param name
     * @param price
     * @param reliability
     * @param requiredLevel
     * **/
    public ObjectBase(String name, int price, int requiredLevel, int reliability) {
        this.name = name;
        this.price = price;
        this.requiredLevel = requiredLevel;
        this.reliability = reliability;

    }
    /**
     * Récupère le type de l'objet.
     * @return le type de l'objet
     * **/
    public String getType() {return type;}
    /**
     * CHange le type de l'objet.
     * @param type
     * **/
    public void setType(String type) {  this.type = type; }

    /**
     * Utilise l'objet.
     * @return une chaîne de caractères décrivant l'utilisation de l'objet
     * **/
    public String use() {
        return "Objet utilisé.";
    }

    /**
     * Vérifie si l'objet est utilisé.
     * @return true si l'objet est utilisé, false sinon
     * **/
    public boolean IsUsed(){
        return reliability > 0 ;
    }

    /**
     * Réduit la fiabilité de l'objet.
     * Si la fiabilité est supérieure à 0, elle est réduite de 1.
     * **/
    public void Reducereliability(){
        if(reliability > 0){
            reliability--;
        }
    }
    /**
     * Récupère le nom de l'objet.
     * @return le nom de l'objet
     * **/
    public String getName() {
        return name;
    }
    /**
     * Change le nom de l'objet.
     * @param name
     * **/
    public void setName(String name) {
        this.name = name;
    }
    /**
     * Récupère le prix de l'objet.
     * @return le prix de l'objet
     * **/
    public int getPrice() {
        return price;
    }
    /**
     * Change le prix de l'objet.
     * @param price
     * **/
    public void setPrice(int price) {
        this.price = price;
    }
    /**
     * Récupère le niveau requis pour utiliser l'objet.
     * @return le niveau requis
     * **/
    public int getRequiredLevel() {
        return requiredLevel;
    }

    /**
     * Récupère l'ID de l'objet.
     * @return l'ID de l'objet
     * **/
    public String getId() {
        return id;
    }

    /**
     * Change l'ID de l'objet.
     * @param id
     * **/
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Change le niveau requis pour utiliser l'objet.
     * @param requiredLevel
     * **/
    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }
    /**
     * Récupère la fiabilité de l'objet.
     * @return la fiabilité de l'objet
     * **/
    public int getReliability() {
        return reliability;
    }
    /**
     * Change la fiabilité de l'objet.
     * @param reliability
     * **/
    public void setReliability(int reliability) {
        this.reliability = reliability;
    }
    /**
     * toString de la classe ObjectBase.
     * @return une chaîne de caractères décrivant l'objet
     * **/
    public String getDetails() {
        return "Nom : " + name + "\n"
            + "Type : " + type + "\n"
            + "Prix : " + price + " cristaux\n"
            + "Niveau requis : " + requiredLevel + "\n"
            + "Endurance : " + reliability;

    }
}
