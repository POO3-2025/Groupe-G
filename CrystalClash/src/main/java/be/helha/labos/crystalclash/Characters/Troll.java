package be.helha.labos.crystalclash.Characters;

/**
 * La classe Troll représente un personnage spécifique du jeu CrystalClash.
 * Elle hérite de la classe Personnage et définit des comportements spécifiques
 * pour le Troll, notamment son attaque spéciale.
 */
public class Troll extends Personnage {

    /**
     * Constructeur de la classe Troll.
     * Initialise les attributs spécifiques du Troll avec des valeurs prédéfinies.
     */
    public Troll() {
        super("Troll", 100, 2, "Coup de Bris’Troll", 10, "Vomi de trolls", 3);
    }

    /**
     * Effectue l'attaque spéciale du Troll sur une cible donnée.
     * Si l'attaque spéciale est disponible, elle inflige 10 points de dégâts à la cible.
     * Sinon, un message indiquant que l'attaque spéciale est indisponible est affiché.
     *
     * @param target Le personnage cible de l'attaque spéciale.
     */
    @Override
    public void AttackSpecial(Personnage target) {
        if (CanUseSpecialAttack()) target.receiveDamage(10);
        else {
            System.out.println("Attaque spéciale indisponible !");
        }
    }

    /**
     * Vérifie si l'attaque spéciale du Troll peut être utilisée.
     * L'attaque spéciale est disponible si le compteur d'attaques (CompteurAttack) est supérieur ou égal à 3.
     *
     * @return true si l'attaque spéciale est disponible, false sinon.
     */
    @Override
    public boolean CanUseSpecialAttack() {
        return CompteurAttack >= 3;
    }
}