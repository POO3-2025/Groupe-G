package be.helha.labos.crystalclash.DTO;

/**
 * DTO pour les trophés
 * contient le nom, description et un boolean si obtenu
 * **/
public class Trophee {
    private String nom;
    private String description;
    private boolean obtenu ;

    /**
     * Constructeur de la classe Trophee
     * @param description
     * @param nom
     *
     * **/
    public Trophee(String nom, String description) {
        this.nom = nom;
        this.description = description;
        this.obtenu = obtenu;
    }


    /**
     * Débloque le trophée en le marquant comme obtenu.
     */
    public void debloquer() {
        this.obtenu = true;
    }

    /**
     * Récupère le nom du trophée.
     *
     * @return Le nom du trophée.
     */
    public String getNom() {
        return nom;
    }

    /**
     * Modifie le nom du trophée.
     *
     * @param nom Le nouveau nom du trophée.
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * Récupère la description du trophée.
     *
     * @return La description du trophée.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Modifie la description du trophée.
     *
     * @param description La nouvelle description du trophée.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Vérifie si le trophée a été obtenu.
     *
     * @return true si le trophée a été obtenu, false sinon.
     */
    public boolean isObtenu() {
        return obtenu;
    }

    /**
     * Modifie l'état du trophée (obtenu ou non).
     *
     * @param obtenu true si le trophée est obtenu, false sinon.
     */
    public void setObtenu(boolean obtenu) {
        this.obtenu = obtenu;
    }
}
