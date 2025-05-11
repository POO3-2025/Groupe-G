package be.helha.labos.crystalclash.Factory;

import be.helha.labos.crystalclash.Object.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * Pq Un factory, c'est un disign pattern qui sert a centraliser la création d'objets
 * ça evite de faire par exemple new Weapon() la ou il faut l'itiliser
 * Creation d'objets en fct de leurs parametres
 * et facile a changer
 * */

public class ObjectFactory {

    /**
     * @param Name
     * @param Type
     * @param LevelPlayer
     * */
    public static ObjectBase CreateObject(String Name, String Type, int LevelPlayer) {
        ObjectBase obj;

        switch (Name.toLowerCase()) {
            case "epee en bois":
                obj = new Weapon("Epee en bois", 50, 1, 5, 4);
                break;
            case "couteau en bois":
                obj = new Weapon("Couteau en bois", 50, 1, 5, 4);
                break;
            case "hache en bois":
                obj = new Weapon("Hache en bois", 50, 1, 5, 4);
                break;

            case "epee en fer":
                if (LevelPlayer >= 3) obj = new Weapon("Epee en fer", 70, 3, 10, 8);
                else throw new IllegalArgumentException("Niveau insuffisant pour Epée en fer");
                break;
            case "couteau en fer":
                if (LevelPlayer >= 3) obj = new Weapon("Couteau en fer", 70, 3, 10, 6);
                else throw new IllegalArgumentException("Niveau insuffisant pour Couteau en fer");
                break;
            case "hache en fer":
                if (LevelPlayer >= 3) obj = new Weapon("Hache en fer", 70, 3, 10, 10);
                else throw new IllegalArgumentException("Niveau insuffisant pour Hache en fer");
                break;

            case "epee en diamant":
                if (LevelPlayer >= 5) obj = new Weapon("Epee en diamant", 150, 5, 20, 13);
                else throw new IllegalArgumentException("Niveau insuffisant pour Epée en diamant");
                break;
            case "couteau en diamant":
                if (LevelPlayer >= 5) obj = new Weapon("Couteau en diamant", 150, 5, 20, 10);
                else throw new IllegalArgumentException("Niveau insuffisant pour Couteau en diamant");
                break;
            case "hache en diamant":
                if (LevelPlayer >= 5) obj = new Weapon("Hache en diamant", 150, 5, 20, 15);
                else throw new IllegalArgumentException("Niveau insuffisant pour Hache en diamant");
                break;

            case "fusil":
                if (LevelPlayer >= 7) obj = new Weapon("Fusil", 150, 7, 5, 10);
                else throw new IllegalArgumentException("Niveau insuffisant pour Fusil");
                break;
            case "bazooka":
                if (LevelPlayer >= 7) obj = new Weapon("Bazooka", 350, 7, 1, 70);
                else throw new IllegalArgumentException("Niveau insuffisant pour Bazooka");
                break;
            case "arme de poing":
                if (LevelPlayer >= 7) obj = new Weapon("Arme de poing", 175, 7, 3, 20);
                else throw new IllegalArgumentException("Niveau insuffisant pour Arme de poing");
                break;
            case "ak-47":
                if (LevelPlayer >= 7) obj = new Weapon("AK-47", 275, 7, 1, 50);
                else throw new IllegalArgumentException("Niveau insuffisant pour AK-47");
                break;

            case "elixir d aube":
                if (LevelPlayer >= 2) obj = new HealingPotion("Elixir d Aube", 20, 2, 15);
                else throw new IllegalArgumentException("Niveau insuffisant pour Elixir d'Aube");
                break;
            case "larme de licorne":
                if (LevelPlayer >= 4) obj = new HealingPotion("Larme de Licorne", 30, 4, 25);
                else throw new IllegalArgumentException("Niveau insuffisant pour Larme de Licorne");
                break;
            case "fluide du phenix":
                if (LevelPlayer >= 6) obj = new HealingPotion("Fluide du Phénix", 60, 6, 50);
                else throw new IllegalArgumentException("Niveau insuffisant pour Fluide du Phénix");
                break;

            case "venin dombre":
                if (LevelPlayer >= 2) obj = new PotionOfStrenght("Venin d Ombre", 25, 2, 5);
                else throw new IllegalArgumentException("Niveau insuffisant pour Venin d'Ombre");
                break;
            case "fiole des abysses":
                if (LevelPlayer >= 4) obj = new PotionOfStrenght("Fiole des Abysses", 35, 4, 10);
                else throw new IllegalArgumentException("Niveau insuffisant pour Fiole des Abysses");
                break;
            case "colere du dragon":
                if (LevelPlayer >= 6) obj = new PotionOfStrenght("Colere du Dragon", 40, 6, 20);
                else throw new IllegalArgumentException("Niveau insuffisant pour Colère du Dragon");
                break;

            case "ecaille du vent":
                obj = new Armor("Écaille du Vent", 25, 2, 3, 10);
                break;
            case "cuirasse du colosse":
                obj = new Armor("Cuirasse du Colosse", 35, 3, 6, 25);
                break;
            case "traqueur des ombres":
                obj = new Armor("Traqueur des Ombres", 50, 5, 15, 35);
                break;

            case "coffre des joyaux":
                obj = new CoffreDesJoyaux();
                break;

            default:
                throw new IllegalArgumentException("Objet inconnu: " + Name);
        }

        // Ajoute dynamiquement le type à l'objet (nom réel de la classe)
        obj.setType(obj.getClass().getSimpleName());
        obj.setId(UUID.randomUUID().toString());
        return obj;
    }

    /**
     * @param Name
     * @param Type
     * */
    public static ObjectBase CreateObjectSansVerification(String Name, String Type) {
        ObjectBase obj;

        switch (Name.toLowerCase()) {
            case "epee en bois":
                obj = new Weapon("Epee en bois", 50, 1, 5, 4);
                break;
            case "couteau en bois":
                obj = new Weapon("Couteau en bois", 50, 1, 5, 4);
                break;
            case "hache en bois":
                obj = new Weapon("Hache en bois", 50, 1, 5, 4);
                break;

            case "epee en fer":
                obj = new Weapon("Epee en fer", 70, 3, 10, 8);
                break;
            case "couteau en fer":
                obj = new Weapon("Couteau en fer", 70, 3, 10, 6);
                break;
            case "hache en fer":
                obj = new Weapon("Hache en fer", 70, 3, 10, 10);
                break;

            case "epee en diamant":
                obj = new Weapon("Epee en diamant", 150, 5, 20, 13);
                break;
            case "couteau en diamant":
                obj = new Weapon("Couteau en diamant", 150, 5, 20, 10);
                break;
            case "hache en diamant":
                obj = new Weapon("Hache en diamant", 150, 5, 20, 15);
                break;

            case "fusil":
                obj = new Weapon("Fusil", 150, 7, 5, 10);
                break;
            case "bazooka":
                obj = new Weapon("Bazooka", 350, 7, 1, 70);
                break;
            case "arme de poing":
                obj = new Weapon("Arme de poing", 175, 7, 3, 20);
                break;
            case "ak-47":
                obj = new Weapon("AK-47", 275, 7, 1, 50);
                break;

            case "elixir d aube":
                obj = new HealingPotion("Elixir d Aube", 20, 2, 15);
                break;
            case "larme de licorne":
                obj = new HealingPotion("Larme de Licorne", 30, 4, 25);
                break;
            case "fluide du phenix":
                obj = new HealingPotion("Fluide du Phénix", 60, 6, 50);
                break;

            case "venin d'ombre":
                obj = new PotionOfStrenght("Venin d'Ombre", 25, 2, 5);
                break;
            case "fiole des abysses":
                obj = new PotionOfStrenght("Fiole des Abysses", 35, 4, 10);
                break;
            case "colere du dragon":
                obj = new PotionOfStrenght("Colere du Dragon", 40, 6, 20);
                break;

            case "ecaille du vent":
                obj = new Armor("Écaille du Vent", 25, 2, 3, 10);
                break;
            case "cuirasse du colosse":
                obj = new Armor("Cuirasse du Colosse", 35, 3, 6, 25);
                break;
            case "traqueur des ombres":
                obj = new Armor("Traqueur des Ombres", 50, 5, 15, 35);
                break;

            case "coffre des joyaux":
                obj = new CoffreDesJoyaux();
                break;

            default:
                throw new IllegalArgumentException("Objet inconnu: " + Name);
        }

        obj.setType(obj.getClass().getSimpleName());
        obj.setId(UUID.randomUUID().toString());
        return obj;
    }

    /**
     *Map pour lire tout les objets du jeu, de les indexers par le nom
     * */
    public static Map<String, ObjectBase> getAllObjectsByName() {
        Map<String, ObjectBase> map = new HashMap<>();

        map.put("epee en bois", new Weapon("Epee en bois", 50, 1, 5, 4));
        map.put("couteau en bois", new Weapon("Couteau en bois", 50, 1, 5, 4));
        map.put("hache en bois", new Weapon("Hache en bois", 50, 1, 5, 4));

        map.put("epee en fer", new Weapon("Epee en fer", 70, 3, 10, 8));
        map.put("couteau en fer", new Weapon("Couteau en fer", 70, 3, 10, 6));
        map.put("hache en fer", new Weapon("Hache en fer", 70, 3, 10, 3));

        map.put("epee en diamant", new Weapon("Epee en diamant", 150, 5, 20, 13));
        map.put("couteau en diamant", new Weapon("Couteau en diamant", 150, 5, 20, 10));
        map.put("hache en diamant", new Weapon("Hache en diamant", 150, 5, 20, 15));

        map.put("fusil", new Weapon("Fusil", 150, 7, 5, 10));
        map.put("bazooka", new Weapon("Bazooka", 350, 7, 1, 70));
        map.put("arme de poing", new Weapon("Arme de poing", 175, 7, 3, 20));
        map.put("ak-47", new Weapon("Ak-47", 275, 7, 1, 50));

        map.put("elixir d aube", new HealingPotion("Elixir d Aube", 20, 2, 15));
        map.put("larme de licorne", new HealingPotion("Larme de Licorne", 30, 4, 25));
        map.put("fluide du phenix", new HealingPotion("Fluide du Phenix", 60, 6, 50));

        map.put("venin d'ombre", new PotionOfStrenght("Venin d'Ombre", 25, 2, 5));
        map.put("fiole des abysses", new PotionOfStrenght("Fiole des Abysses", 35, 4, 10));
        map.put("colere du dragon", new PotionOfStrenght("Colere du Dragon", 40, 6, 20));

        map.put("ecaille du vent", new Armor("Ecaille du Vent", 25, 2, 3, 10));
        map.put("cuirasse du colosse", new Armor("Cuirasse du Colosse", 35, 3, 6, 25));
        map.put("traqueur des ombres", new Armor("Traqueur des Ombres", 50, 5, 15, 35));

        map.put("coffre des joyaux", new CoffreDesJoyaux());

        return map;
    }


}
