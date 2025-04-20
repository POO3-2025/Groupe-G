package be.helha.labos.crystalclash.DeserialiseurCustom;

import be.helha.labos.crystalclash.Factory.ObjectFactory;
import be.helha.labos.crystalclash.Object.ObjectBase;
import com.google.gson.*;

import java.lang.reflect.Type;

//classe implementant JsonDeserializer diisant que je veux déserialiser vers objectBase
public class ObjectBaseDeserializer implements JsonDeserializer<ObjectBase> {

    private int levelPlayer; // tu peux injecter dynamiquement ce niveau

    //Contient level du user neccesaire car certain objet neccesite un level ducoup ne peuvent pas etre instanciés
    //et le passe au moment de déserialiser
    public ObjectBaseDeserializer(int levelPlayer) {
        this.levelPlayer = levelPlayer;
    }

    //Appelé par GSON, convertir JsonElement en ObjectBase
    @Override
    public ObjectBase deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        //lecture du json en brut pour acceder au champ
        JsonObject jsonObject = json.getAsJsonObject();

        // Récupération des champs de base
        String name = jsonObject.get("name").getAsString();
        String type = jsonObject.get("type").getAsString();

        // Utilisation de la factory
        try {
            //Appelle le factory qui retourne le type et name
            return ObjectFactory.CreateObject(name, type, levelPlayer);
        } catch (IllegalArgumentException e) {
            throw new JsonParseException("Impossible de créer l'objet : " + e.getMessage(), e);
        }
    }
}
