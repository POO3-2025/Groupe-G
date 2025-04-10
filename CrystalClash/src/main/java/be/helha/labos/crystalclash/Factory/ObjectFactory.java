package be.helha.labos.crystalclash.Factory;

import be.helha.labos.Object.*;

import java.util.ArrayList;
import java.util.List;

public class ObjectFactory {
    public static ObjectBase CreateObject(String Name, String Type, int LevelPlayer) {
        switch(Name.toLowerCase()) {
            case "epee en bois":
                return new Weapon("Epée en bois", 50, 1, 5, 4);
            case "couteau en bois":
                return new Weapon("Couteau en bois", 50, 1, 5, 4);
            case "hache en bois":
                return new Weapon("Hache en bois", 50, 1, 5, 4);

            case "epee en fer":
                if (LevelPlayer >= 3) return new Weapon("Epée en fer", 70, 3, 10, 8);
                else throw new IllegalArgumentException("Niveau insuffisant pour Epée en fer");
            case "couteau en fer":
                if (LevelPlayer >= 3) return new Weapon("Couteau en fer", 70, 3, 10, 6);
                else throw new IllegalArgumentException("Niveau insuffisant pour Couteau en fer");
            case "hache en fer":
                if (LevelPlayer >= 3) return new Weapon("Hache en fer", 70, 3, 10, 10);
                else throw new IllegalArgumentException("Niveau insuffisant pour Hache en fer");

            case "epee en diamant":
                if (LevelPlayer >= 5) return new Weapon("Epée en diamant", 150, 5, 20, 13);
                else throw new IllegalArgumentException("Niveau insuffisant pour Epée en diamant");
            case "couteau en diamant":
                if (LevelPlayer >= 5) return new Weapon("Couteau en diamant", 150, 5, 20, 10);
                else throw new IllegalArgumentException("Niveau insuffisant pour Couteau en diamant");
            case "hache en diamant":
                if (LevelPlayer >= 5) return new Weapon("Hache en diamant", 150, 5, 20, 15);
                else throw new IllegalArgumentException("Niveau insuffisant pour Hache en diamant");

            case "fusil":
                if (LevelPlayer >= 7) return new Weapon("Fusil", 150, 7, 5, 10);
                else throw new IllegalArgumentException("Niveau insuffisant pour Fusil");
            case "bazooka":
                if (LevelPlayer >= 7) return new Weapon("Bazooka", 350, 7, 1, 70);
                else throw new IllegalArgumentException("Niveau insuffisant pour Bazooka");
            case "arme de poing":
                if (LevelPlayer >= 7) return new Weapon("Arme de poing", 175, 7, 3, 20);
                else throw new IllegalArgumentException("Niveau insuffisant pour Arme de poing");
            case "ak-47":
                if (LevelPlayer >= 7) return new Weapon("AK-47", 275, 7, 1, 50);
                else throw new IllegalArgumentException("Niveau insuffisant pour AK-47");

            case "elixir daube":
                if (LevelPlayer >= 2) return new HealingPotion("Elixir d'Aube", 20, 2, 15);
                else throw new IllegalArgumentException("Niveau insuffisant pour Elixir d'Aube");
            case "larme de licorne":
                if (LevelPlayer >= 4) return new HealingPotion("Larme de Licorne", 30, 4, 25);
                else throw new IllegalArgumentException("Niveau insuffisant pour Larme de Licorne");
            case "fluide du phenix":
                if (LevelPlayer >= 6) return new HealingPotion("Fluide du Phénix", 60, 6, 50);
                else throw new IllegalArgumentException("Niveau insuffisant pour Fluide du Phénix");

            case "venin dombre":
                if (LevelPlayer >= 2) return new PotionOfStrenght("Venin d'Ombre", 25, 2, 5);
                else throw new IllegalArgumentException("Niveau insuffisant pour Venin d'Ombre");
            case "fiole des abysses":
                if (LevelPlayer >= 4) return new PotionOfStrenght("Fiole des Abysses", 35, 4, 10);
                else throw new IllegalArgumentException("Niveau insuffisant pour Fiole des Abysses");
            case "colere du dragon":
                if (LevelPlayer >= 6) return new PotionOfStrenght("Colère du Dragon", 40, 6, 20);
                else throw new IllegalArgumentException("Niveau insuffisant pour Colère du Dragon");

            case "ecaille du vent":
                return new Armor("Écaille du Vent", 25, 1, 3, 10);
            case "cuirasse du colosse":
                return new Armor("Cuirasse du Colosse", 35, 1, 6, 25);
            case "traqueur des ombres":
                return new Armor("Traqueur des Ombres", 50, 1, 15, 35);

            case "coffre des joyaux":
                return new CoffreDesJoyaux();

            default:
                throw new IllegalArgumentException("Objet inconnu: " + Name);
        }
    }
    public static List<ObjectBase> getAllObjectsForLevel(int level) {
        List<ObjectBase> objetsDisponibles = new ArrayList<>();

        String[] allNames = {
            "epee en bois", "couteau en bois", "hache en bois",
            "epee en fer", "couteau en fer", "hache en fer",
            "epee en diamant", "couteau en diamant", "hache en diamant",
            "fusil", "bazooka", "arme de poing", "ak-47",
            "elixir daube", "larme de licorne", "fluide du phenix",
            "venin dombre", "fiole des abysses", "colere du dragon",
            "ecaille du vent", "cuirasse du colosse", "traqueur des ombres",
            "coffre des joyaux"
        };

        for (String name : allNames) {
            try {
                objetsDisponibles.add(CreateObject(name, "", level));
            } catch (IllegalArgumentException e) {
                // Objet non dispo pour ce niveau → on l'ignore
            }
        }

        return objetsDisponibles;
    }
}

