package be.helha.labos.crystalclash.Factory;

import be.helha.labos.crystalclash.Characters.*;

import java.util.List;
import java.util.Map;

/**
 * Factory pour créer des personnages
 */
public class CharactersFactory {

    /**
     * Crée un personnage en fonction du type et du niveau
     * @param Type => Type de personnage (Elf, Troll, Dragon, Aquaman)
     * @param Level => Niveau qu'il faut avoir
     * **/
    public static Personnage CreateCharacters(String Type, int Level){
        switch(Type.toLowerCase().trim()){
            case "elf": return new Elf();
            case "troll": return new Troll();
            case "dragon": if(Level >= 8) return new Dragon();
            else  throw new IllegalArgumentException("Le niveau minimum pour jouer Dragon est 8.");
            case "aquaman": if(Level >=6) return new Aquaman();
            else throw new IllegalArgumentException("insufficient level for Aquaman");
            default: throw new IllegalArgumentException("Character type unknown: " +Type);

        }
    }
    /**
     * Crée un personnage en fonction du type
     * @param Type => Type de personnage (Elf, Troll, Dragon, Aquaman)
     * @return => Instance du personnage
     */
    /** Retourne une instance du personnage en fonction du type **/
    public static Personnage getCharacterByType(String type) {
        switch(type.toLowerCase().trim()) {
            case "elf": return new Elf();
            case "troll": return new Troll();
            case "dragon": return new Dragon();  // Tu peux aussi vérifier le niveau ici si nécessaire
            case "aquaman": return new Aquaman();
            default: throw new IllegalArgumentException("Type de personnage inconnu: " + type);
        }
    }

    /*
    * Liste des persos dispo dans le factory
    * List pour parcourir les persos
    * */

    /**
     * Retourne la liste des types de personnages disponibles
     * @return => Liste des types de personnages
     */
    public static List<String> getAvailableCharacterTypes() {
        return List.of("Elf", "Troll", "Dragon", "Aquaman");
    }

    /*
    * Donne niveau requis pour chaque perso
    * Map : table correspondante entre String(Perso) et integer (niveau)
    * et verif le niveau
    * */

    /**
     * Fournit un mapping entre chaque type de personnage et le niveau requis pour l'utiliser.
     * @return une map associant le type de personnage à son niveau requis
     */
    public static Map<String, Integer> getRequiredLevelByType() {
        return Map.of(
            "Elf", 1,
            "Troll", 1,
            "Aquaman", 6,
            "Dragon", 8
        );
    }

}
