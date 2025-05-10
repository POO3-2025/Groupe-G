package be.helha.labos.crystalclash.DTO;

public class Trophee {
    private String nom;
    private String description;
    private boolean obtenu ;

    public Trophee(String nom, String description, boolean obtenu) {
        this.nom = nom;
        this.description = description;
        this.obtenu = obtenu;
    }

    public void debloquer() {
        this.obtenu = true;
    }
    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public boolean isObtenu() {
        return obtenu;
    }
    public void setObtenu(boolean obtenu) {
        this.obtenu = obtenu;
    }

}
