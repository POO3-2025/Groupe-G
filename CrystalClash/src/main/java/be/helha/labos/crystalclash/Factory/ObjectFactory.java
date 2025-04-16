package be.helha.labos.crystalclash.Factory;

import be.helha.labos.crystalclash.Object.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                return new Armor("Écaille du Vent", 25, 2, 3, 10);
            case "cuirasse du colosse":
                return new Armor("Cuirasse du Colosse", 35, 3, 6, 25);
            case "traqueur des ombres":
                return new Armor("Traqueur des Ombres", 50,5 , 15, 35);

            case "coffre des joyaux":
                return new CoffreDesJoyaux();

            default:
                throw new IllegalArgumentException("Objet inconnu: " + Name);
        }
    }
    public static Map<String, ObjectBase> getAllObjectsByName() {
        Map<String, ObjectBase> map = new HashMap<>();

        map.put("epee en bois", new Weapon("Epée en bois", 50, 1, 5, 4));
        map.put("couteau en bois", new Weapon("Couteau en bois", 50, 1, 5, 4));
        map.put("hache en bois", new Weapon("Hache en bois", 50, 1, 5, 4));

        map.put("epee en fer", new Weapon("Epée en fer", 70, 3, 10, 8));
        map.put("couteau en fer", new Weapon("Couteau en fer", 70, 3, 10, 6));
        map.put("hache en fer", new Weapon("Hache en fer", 70, 3, 10, 3));

        map.put("epee en diamant", new Weapon("Epée en fer", 150, 5, 20, 13));
        map.put("couteau en diamant", new Weapon("Couteau en fer", 150, 5, 20, 10));
        map.put("hache en diamant", new Weapon("Hache en fer", 150, 5, 20, 15));


        map.put("fusil", new Weapon("Fusil", 150, 7, 5, 10));
        map.put("bazooka", new Weapon("Bazooka", 350, 7, 1, 70));
        map.put("arme de poing", new Weapon("Arme de poing", 175, 7, 3, 20));
        map.put("ak-47", new Weapon("Ak-47", 175, 7, 3, 20));

        map.put("elixir daube", new HealingPotion("Elexir d' Aube", 20, 2, 15));
        map.put("larme de licorne", new HealingPotion("Larme de Licorne", 30, 4, 25));
        map.put("fluide du phenix", new HealingPotion("Fluide du Phénix", 60, 6, 50));

        map.put("venin dombre", new PotionOfStrenght("Venin d' Ombre", 25, 2, 5));
        map.put("fiole des abysses", new PotionOfStrenght("Fiole des Abysses ", 35, 1, 25));
        map.put("colere du dragon", new PotionOfStrenght("Colère du Dragon", 35, 1, 25));

        map.put("ecaille du vent", new Armor("Ecaille du Vent", 25, 2, 3, 10));
        map.put("cuirasse du colosse", new Armor("Cuirasse du Colosse", 35, 3, 6, 25));
        map.put("traqueur des ombres", new Armor("Traqueur des Ombres", 35, 5, 15, 35));

        map.put("coffre des joyaux", new CoffreDesJoyaux());
        
        return map;
    }

}

