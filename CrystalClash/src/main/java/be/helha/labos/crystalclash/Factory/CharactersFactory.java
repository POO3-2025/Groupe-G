package be.helha.labos.crystalclash.Factory;

import be.helha.labos.crystalclash.Characters.*;

public class CharactersFactory {
    public static Personnage CreateCharacters(String Type, int Level){
        switch(Type.toLowerCase()){
            case "elf": return new Elf();
            case "troll": return new Troll();
            case "dragon": if(Level >= 8) return new Dragon();
            else throw new IllegalArgumentException("insufficient level for Dragon");
            case "aquaman": if(Level >=6) return new Aquaman();
            else throw new IllegalArgumentException("insufficient level for Aquaman");
            default: throw new IllegalArgumentException("Character type unknown: " +Type);

        }
    }
}
