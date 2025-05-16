package be.helha.labos.crystalclash.Characters;

import be.helha.labos.crystalclash.Object.Equipment;

/**
 * La classe Personnage représente un personnage générique dans le jeu CrystalClash.
 * Elle contient les attributs et comportements de base pour tous les personnages.
 */
public class Personnage {

    private String Name; // Nom du personnage
    private int PV; // Points de vie du personnage
    private int AttackBase; // Valeur de l'attaque de base
    private String NameAttackBase; // Nom de l'attaque de base
    private int AttackSpecial; // Valeur de l'attaque spéciale
    private String NameAttaqueSpecial; // Nom de l'attaque spéciale
    private int RestrictionAttackSpecial; // Restriction pour utiliser l'attaque spéciale
    protected int CompteurAttack = 0; // Compteur d'attaques effectuées

    /**
     * Constructeur par défaut requis pour la désérialisation JSON.
     */
    public Personnage() {
        // Requis pour désérialisation JSON en Gson ou Jackson
    }

    /**
     * Constructeur de la classe Personnage.
     *
     * @param Name Nom du personnage
     * @param PV Points de vie du personnage
     * @param AttackBase Valeur de l'attaque de base
     * @param NameAttackBase Nom de l'attaque de base
     * @param AttackSpecial Valeur de l'attaque spéciale
     * @param NameAttaqueSpecial Nom de l'attaque spéciale
     * @param RestrictionAttackSpecial Restriction pour utiliser l'attaque spéciale
     */
    public Personnage(String Name, int PV, int AttackBase,
                      String NameAttackBase, int AttackSpecial, String NameAttaqueSpecial, int RestrictionAttackSpecial) {
        this.Name = Name;
        this.PV = PV;
        this.AttackBase = AttackBase;
        this.NameAttackBase = NameAttackBase;
        this.AttackSpecial = AttackSpecial;
        this.NameAttaqueSpecial = NameAttaqueSpecial;
        this.RestrictionAttackSpecial = RestrictionAttackSpecial;
    }

    /**
     * Effectue une attaque de base sur une cible.
     *
     * @param target Le personnage cible de l'attaque.
     */
    public void tackle(Personnage target) {
        System.out.println(this.Name + " attaque " + target.getName() + " avec une attaque normale !");
        target.receiveDamage(AttackBase);
        CompteurAttack++;
    }

    /**
     * Effectue une attaque spéciale sur une cible.
     * Par défaut, cette méthode affiche un message indiquant que l'attaque spéciale
     * n'est pas disponible ou n'existe pas.
     *
     * @param target Le personnage cible de l'attaque spéciale.
     */
    public void AttackSpecial(Personnage target) {
        System.out.println(Name + " n’a pas d’attaque spéciale ou ne peut pas l’utiliser.");
    }

    /**
     * Vérifie si l'attaque spéciale peut être utilisée.
     * Par défaut, retourne toujours false.
     *
     * @return true si l'attaque spéciale est disponible, false sinon.
     */
    public boolean CanUseSpecialAttack() {
        return false;
    }

    /**
     * Réduit les points de vie du personnage en fonction des dégâts reçus.
     *
     * @param damage La quantité de dégâts infligés.
     */
    public void receiveDamage(int damage) {
        this.PV -= damage;
        System.out.println(Name + " subit " + damage + " points de dégâts. PV restants : " + PV);
    }

    // Getters et setters pour les attributs de la classe

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public int getPV() {
        return PV;
    }

    public void setPV(int PV) {
        this.PV = PV;
    }

    public int getAttackBase() {
        return AttackBase;
    }

    public void setAttackBase(int AttackBase) {
        this.AttackBase = AttackBase;
    }

    public int getCompteurAttack() {
        return CompteurAttack;
    }

    public void CompteurAttack(int CompteurAttack) {
        this.CompteurAttack = CompteurAttack;
    }

    public String getNameAttackBase() {
        return NameAttackBase;
    }

    public int getAttackSpecial() {
        return AttackSpecial;
    }

    public String getNameAttaqueSpecial() {
        return NameAttaqueSpecial;
    }

    public int getRestrictionAttackSpecial() {
        return RestrictionAttackSpecial;
    }


}
