package be.helha.labos.crystalclash.Factory;

import be.helha.labos.crystalclash.Characters.*;

import java.util.List;
import java.util.Map;

public class CharactersFactory {

    /**
     * @param Type
     * @param Level
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

    /*
    * Liste des persos dispo dans le factory
    * List pour parcourir les persos
    * */
    public static List<String> getAvailableCharacterTypes() {
        return List.of("Elf", "Troll", "Dragon", "Aquaman");
    }

    /*
    * Donne niveau requis pour chaque perso
    * Map : table correspondante entre String(Perso) et integer (niveau)
    * et verif le niveau
    * */
    public static Map<String, Integer> getRequiredLevelByType() {
        return Map.of(
            "Elf", 1,
            "Troll", 1,
            "Aquaman", 6,
            "Dragon", 8
        );
    }

}
