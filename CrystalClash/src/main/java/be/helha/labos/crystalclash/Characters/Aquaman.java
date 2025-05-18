package be.helha.labos.crystalclash.Characters;

/**
 * La classe Aquaman représente un personnage spécifique du jeu CrystalClash.
 * Elle hérite de la classe Personnage et définit des comportements spécifiques
 * pour Aquaman, notamment son attaque spéciale.
 */
public class Aquaman extends Personnage {

    /**
     * Constructeur de la classe Aquaman.
     * Initialise les attributs spécifiques d'Aquaman avec des valeurs prédéfinies.
     */
    public Aquaman(){
        super("Aquaman", 85, 10, "Lancer de poison", 30, "Tsunami", 5);
    }

    /**
     * Effectue l'attaque spéciale d'Aquaman sur une cible donnée.
     * Si l'attaque spéciale est disponible, elle inflige 30 points de dégâts à la cible.
     * Sinon, un message indiquant que l'attaque spéciale est indisponible est affiché.
     *
     * @param target Le personnage cible de l'attaque spéciale.
     */
    @Override
    public void AttackSpecial(Personnage target){
        if(CanUseSpecialAttack()) target.receiveDamage(30);
        else {
            System.out.println("Attaque spéciale indisponible !");
        }
    }

    /**
     * Vérifie si l'attaque spéciale d'Aquaman peut être utilisée.
     * L'attaque spéciale est disponible si le compteur d'attaques (CompteurAttack) est supérieur ou égal à 5.
     *
     * @return true si l'attaque spéciale est disponible, false sinon.
     */
    @Override
    public boolean CanUseSpecialAttack(){
        return CompteurAttack >= 5;
    }

}