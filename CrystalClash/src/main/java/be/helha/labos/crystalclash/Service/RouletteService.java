package be.helha.labos.crystalclash.Service;


import be.helha.labos.crystalclash.DAO.RouletteDAO;
import be.helha.labos.crystalclash.Factory.ObjectFactory;
import be.helha.labos.crystalclash.DTO.Inventory;
import be.helha.labos.crystalclash.Object.ObjectBase;
import be.helha.labos.crystalclash.User.UserInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
@Service
public class RouletteService {

    //Coup de la participation
    private static final int COUT_PARTICIPATION = 25;

    @Autowired
    private UserService userService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private RouletteDAO rouletteDAO;

    /**
     * @param username
     * */
    public ObjectBase PlayRoulette(String username) throws Exception {

        UserInfo user = userService.getUserInfo(username)
            .orElseThrow(() -> new RuntimeException("User pas trouvé"));

        if (!CanPlayToDay(username)) {
            throw new RuntimeException ("Déjà joué aujourd'hui gourmand va !");
        }
        if (user.getCristaux() < COUT_PARTICIPATION) {
            throw new RuntimeException ("Pas assez de cristaux");
        }

        userService.updateCristaux(username, user.getCristaux() - COUT_PARTICIPATION);

        ObjectBase loot = pullObject();

        //On recup inven du user
        Inventory inven = inventoryService.getInventoryForUser(username);
        //Ajoute l'objet rouletté
        inven.ajouterObjet(loot);
        //Sauv l inven
        inventoryService.saveInventoryForUser(username, inven);
        //Et met a jour ma date du joueur joué a la roulette
        rouletteDAO.UpdateLastPlayDate(username, LocalDate.now());
        return loot;
    }

    public boolean CanPlayToDay(String username){
        //Recup depuis monga la drniere date joué par le user
        LocalDate LastPlay = rouletteDAO.getLastPlayDate(username);
        //Si pas jamais joué alors null
        //Si deja alors il y aura une date
        //true le joueur peut la tourner
        //False deja joué
        return LastPlay == null || !LastPlay.equals(LocalDate.now());
    }

    public ObjectBase pullObject (){
        //STacker les objets
        List<ObjectBase> objets = new ArrayList<>(ObjectFactory.getAllObjectsByName().values());
        //Stcke plusieur fois chaque objet en fct de leur risk
        //Ex un objet a 50 crist sera mit 10 fois dans pool
        List<ObjectBase> pool = new ArrayList<>();

        //Bouclé les objets et mettre des risk
        //<=60 le user a 10 fois plus de chance a tomber dessus, ....
        for (ObjectBase obj : objets){

            int risk;
            int prix = obj.getPrice();

            if (prix <= 60) risk = 10;
            else if (prix <= 120) risk = 5;
            else risk = 1 ;
            for (int i = 0; i < risk; i++){
                pool.add(obj);
            }
        }

        //Shuffle melange dans la liste aleatoirement
        Collections.shuffle(pool);
        //Melange liste
        //prend index au hasard
        //Bonne distri ou pas par rapport au risk au dessus
        ObjectBase tirage = pool.get(new Random().nextInt(pool.size()));
        return ObjectFactory.CreateObjectSansVerification(tirage.getName(), tirage.getType());


    }
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public void setRouletteDAO(RouletteDAO rouletteDAO) {
        this.rouletteDAO = rouletteDAO;
    }

}
