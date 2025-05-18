package be.helha.labos.crystalclash.Characters;

/**
 * La classe Elf représente un personnage spécifique du jeu CrystalClash.
 * Elle hérite de la classe Personnage et définit des comportements spécifiques
 * pour l'Elf, notamment son attaque spéciale.
 */
public class Elf extends Personnage {

    /**
     * Constructeur de la classe Elf.
     * Initialise les attributs spécifiques de l'Elf avec des valeurs prédéfinies.
     */
    public Elf() {
        super("Elf", 95, 2, "Arc à flèche", 20, "Pluie de flèches", 4);
    }

    /**
     * Effectue l'attaque spéciale de l'Elf sur une cible donnée.
     * Si l'attaque spéciale est disponible, elle inflige 20 points de dégâts à la cible.
     * Sinon, un message indiquant que l'attaque spéciale est indisponible est affiché.
     *
     * @param target Le personnage cible de l'attaque spéciale.
     */
    @Override
    public void AttackSpecial(Personnage target) {
        System.out.println("Pluie de flèches ! " + getName() + " inflige 20 PV");
        if (CanUseSpecialAttack()) target.receiveDamage(20);
        else {
            System.out.println("Attaque spéciale indisponible !");
        }
    }

    /**
     * Vérifie si l'attaque spéciale de l'Elf peut être utilisée.
     * L'attaque spéciale est disponible si le compteur d'attaques (CompteurAttack) est supérieur ou égal à 4.
     *
     * @return true si l'attaque spéciale est disponible, false sinon.
     */
    @Override
    public boolean CanUseSpecialAttack() {
        return CompteurAttack >= 4;
    }
}