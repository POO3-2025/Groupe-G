package be.helha.labos.crystalclash.Service;

import be.helha.labos.crystalclash.DAO.ShopDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ShopService {

    private final ShopDAO shopDAO;

    @Autowired
    public ShopService(ShopDAO shopDAO) {
        this.shopDAO = shopDAO;
    }

    public List<Map<String, Object>> getShopItems(){
        return shopDAO.getShopItems();
    }

    public boolean buyItem(String username, String itemName, String type){
        return shopDAO.buyItem(username, itemName, type);
    }


}
