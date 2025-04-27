
package be.helha.labos.crystalclash.Object;


public class ObjectBase {
    private String name;
    protected int price;
    protected int requiredLevel;
    protected int reliability;
    private String type;

    public ObjectBase() {
        // Constructeur par défaut requis pour la désérialisation
    }

    /**
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
    public String getType() {return type;}
    /**
     * @param type
     * **/
    public void setType(String type) {  this.type = type; }

    public String use() {
        return "Objet utilisé.";
    }

    public boolean IsUsed(){
        return reliability > 0 ;
    }

    public void Reducereliability(){
        if(reliability > 0){
            reliability--;
        }
    }
    public String getName() {
        return name;
    }
    /**
     * @param name
     * **/
    public void setName(String name) {
        this.name = name;
    }
    public int getPrice() {
        return price;
    }
    /**
     * @param price
     * **/
    public void setPrice(int price) {
        this.price = price;
    }
    public int getRequiredLevel() {
        return requiredLevel;
    }
    /**
     * @param requiredLevel
     * **/
    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }
    public int getReliability() {
        return reliability;
    }
    public void setReliability(int reliability) {
        this.reliability = reliability;
    }
    public String getDetails() {
        return "Nom : " + name + "\n"
            + "Type : " + type + "\n"
            + "Prix : " + price + " cristaux\n"
            + "Niveau requis : " + requiredLevel + "\n"
            + "Endurance : " + reliability;

    }
}
