package be.helha.labos.crystalclash.DeserialiseurCustom;

import be.helha.labos.crystalclash.Object.ObjectBase;
import com.google.gson.*;

import java.lang.reflect.Type;

//Utile quand json va déserialiser un objet json en ObjectBase
//Lit l object tel qu'il est ds le json
//dese depuis mongo
//Lit tout les champs
public class ObjectBasePolymorphicDeserializer implements JsonDeserializer<ObjectBase> {

    /**
     * @param json bloc brute que Gson lit
     * @param typeOfT type de jave que gson essaye de construire (objectBase,weapon, ect, ...
     * @param context permet de désérialiser  un json imbriqué
     * Appellé par Gson chaque fois que ObjectBase doit construire un JsonElement
     * */

    @Override
    public ObjectBase deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        //Convertit l objet json but en objet jsonObject pour avoir accés a ses champ
        JsonObject jsonObject = json.getAsJsonObject();

        //Lit champ type ds le json permet de savoir quelle sous classe
        String type = jsonObject.get("type").getAsString(); //  "Weapon", "Armor" et autre

        try {
            //Obtenir dynamiquement la classe correspondantes au type voulu
            //Gson sait maintenant qu elle classe réellement a instancier avec l'objet Json complet
            Class<?> clazz = Class.forName("be.helha.labos.crystalclash.Object." + type);
            //Injection des tous ses champs
            return context.deserialize(json, clazz);
        } catch (ClassNotFoundException e) {
            throw new JsonParseException("Classe inconnue : " + type, e);
        }
    }
}
