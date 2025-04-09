package be.helha.labos.crystalclash.Object;

public abstract class ObjectBase {
    private String Name;
    protected int Price;
    protected int RequiredLevel;
    protected int reliability;

    public ObjectBase(String Name, int Price, int RequiredLevel, int reliability) {
        this.Name = Name;
        this.Price = Price;
        this.RequiredLevel = RequiredLevel;
        this.reliability = reliability;
    }

    public abstract String use();

   public boolean IsUsed(){
       return reliability > 0 ;
   }

   public void Reducereliability(){
       if(reliability > 0){
           reliability--;
       }
   }
    public String getName() {
        return Name;
    }
    public void setName(String name) {
        Name = name;
    }
    public int getPrice() {
        return Price;
    }
    public void setPrice(int price) {
        Price = price;
    }
    public int getRequiredLevel() {
        return RequiredLevel;
    }
    public void setRequiredLevel(int requiredLevel) {
        RequiredLevel = requiredLevel;
    }
    public int getReliability() {
        return reliability;

   }
}
