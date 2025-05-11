package be.helha.labos.crystalclash.User;

import be.helha.labos.crystalclash.DTO.Trophee;

import java.util.ArrayList;
import java.util.List;

public class UserInfo {
    private String username;
    private boolean isConnected; //Rajout pour le boolean
    private int level;
    private int cristaux;
    private String selectedCharacter;
    private int gagner ;
    private int perdu;
    private int victoiresConsecutives;

    //trophee
    public List<Trophee> trophees =  new ArrayList<Trophee>();



    public String getUsername() { return username; }
    public int getLevel() { return level; }
    public int getCristaux() { return cristaux; }
    public String getSelectedCharacter() { return selectedCharacter; }
    public void setUsername(String username) { this.username = username; }
    public void setLevel(int level) { this.level = level; }
    public void setCristaux(int cristaux) { this.cristaux = cristaux; }
    public void setSelectedCharacter(String selectedCharacter) { this.selectedCharacter = selectedCharacter; }
    public boolean isConnected() {   return isConnected; }
    public void setConnected(boolean connected) { this.isConnected = connected;}
    public void setGagner(int gagner) { this.gagner = gagner; }
    public void setPerdu(int perdu) { this.perdu = perdu; }
    public int getGagner() { return gagner; }
    public int getPerdu() { return perdu; }

    //trophee
    public void affTrophee(Trophee trophee) {
        trophees.add(trophee);
    }
    public boolean haveTrophee(String nom) {
        return trophees.stream().anyMatch(t -> t.getNom().equalsIgnoreCase(nom));
    }

    public List<Trophee> getTrophees() { return trophees; }

    //Nv pour les victoires concecutives trophé
    public int getWinconsecutive(){
        return victoiresConsecutives;
    }

    public void setWinconsecutive(int victoiresConsecutives){
        this.victoiresConsecutives = victoiresConsecutives;
    }

    public void incrementWinconsecutive(){
        victoiresConsecutives ++;
    }

    public void resetVictoiresConsecutives() {
        this.victoiresConsecutives = 0;
    }

    //Pour barre et trophés
    private int dernierCombatTours;
    private int utilisationBazooka; // incrémenté quand il le user use bazooka en combat

    public int getDernierCombatTours() { return dernierCombatTours; }
    public void setDernierCombatTours(int tours) { this.dernierCombatTours = tours; }

    //Utile pour le trophé Or, on regarde si le user a use le bazooka.
    public int getUtilisationBazooka() { return utilisationBazooka; }
    public void incrementUtilisationBazooka() { this.utilisationBazooka++; }
}
