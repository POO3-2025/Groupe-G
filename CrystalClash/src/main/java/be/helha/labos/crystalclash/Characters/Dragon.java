package be.helha.labos.crystalclash.Characters;

/**
 * La classe Dragon représente un personnage spécifique du jeu CrystalClash.
 * Elle hérite de la classe Personnage et définit des comportements spécifiques
 * pour le Dragon, notamment son attaque spéciale.
 */
public class Dragon extends Personnage {

    /**
     * Constructeur de la classe Dragon.
     * Initialise les attributs spécifiques du Dragon avec des valeurs prédéfinies.
     */
    public Dragon() {
        super("Dragon", 75, 10, "Coup de griffes", 40, "Lance-flammes", 6);
    }

    /**
     * Effectue l'attaque spéciale du Dragon sur une cible donnée.
     * Si l'attaque spéciale est disponible, elle inflige 40 points de dégâts à la cible.
     * Sinon, un message indiquant que l'attaque spéciale est indisponible est affiché.
     *
     * @param target Le personnage cible de l'attaque spéciale.
     */
    @Override
    public void AttackSpecial(Personnage target) {
        if (CanUseSpecialAttack()) {
            target.receiveDamage(40);
        } else {
            System.out.println("Attaque spéciale indisponible !");
        }
    }

    /**
     * Vérifie si l'attaque spéciale du Dragon peut être utilisée.
     * L'attaque spéciale est disponible si le compteur d'attaques (CompteurAttack) est supérieur ou égal à 6.
     *
     * @return true si l'attaque spéciale est disponible, false sinon.
     */
    @Override
    public boolean CanUseSpecialAttack() {
        return CompteurAttack >= 6;
    }
}