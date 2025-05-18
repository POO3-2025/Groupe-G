package be.helha.labos.crystalclash.User;

import be.helha.labos.crystalclash.DTO.Trophee;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe représentant les informations d'un utilisateur.
 * Cette classe contient les données liées à un utilisateur, telles que son nom, son niveau,
 * ses cristaux, ses victoires, ses trophées, et d'autres statistiques.
*/
 public class UserInfo {
    private String username;
    private boolean isConnected; //Rajout pour le boolean
    private int level;
    private int cristaux;
    private String selectedCharacter;
    private int gagner ;
    private int perdu;

    private int winconsecutive;



    //trophee
    public List<Trophee> trophees =  new ArrayList<Trophee>();


    /**
     * Récupère le nom d'utilisateur.
     * @return Le nom d'utilisateur.
     */
    public String getUsername() { return username; }
    /**
     * Récupère le niveau de l'utilisateur.
     * @return Le niveau de l'utilisateur.
     */
    public int getLevel() { return level; }
    /**
     * Récupère le nombre de cristaux possédés par l'utilisateur.
     * @return Le nombre de cristaux.
     */
    public int getCristaux() { return cristaux; }
    /**
     * Récupère le personnage sélectionné par l'utilisateur.
     * @return Le personnage sélectionné.
     */
    public String getSelectedCharacter() { return selectedCharacter; }

    /**
     * change le nom d'utilisateur.
     * @param username Le nom d'utilisateur.
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * change le niveau de l'utilisateur.
     * @param level Le niveau de l'utilisateur.
     */
    public void setLevel(int level) { this.level = level; }

    /**
     * change le nombre de cristaux possédés par l'utilisateur.
     * @param cristaux Le nombre de cristaux.
     */
    public void setCristaux(int cristaux) { this.cristaux = cristaux; }

    /**
     * change le personnage sélectionné par l'utilisateur.
     * @param selectedCharacter Le personnage sélectionné.
     */
    public void setSelectedCharacter(String selectedCharacter) { this.selectedCharacter = selectedCharacter; }
    /**
     * Vérifie si l'utilisateur est connecté.
     * @return true si l'utilisateur est connecté, false sinon.
     */
    public boolean isConnected() {   return isConnected; }

    /**
     * change l'état de connexion de l'utilisateur.
     * @param connected true si l'utilisateur est connecté, false sinon.
     */
    public void setConnected(boolean connected) { this.isConnected = connected;}
    /**
     * change le nombre de victoires de l'utilisateur.
     * @param gagner Le nombre de victoires.
     */
    public void setGagner(int gagner) { this.gagner = gagner; }
    /**
     * change le nombre de défaites de l'utilisateur.
     * @param perdu Le nombre de défaites.
     */
    public void setPerdu(int perdu) { this.perdu = perdu; }

    /**
     * Récupère le nombre de victoires de l'utilisateur.
     * @return Le nombre de victoires.
     */
    public int getGagner() { return gagner; }
    /**
     * Récupère le nombre de défaites de l'utilisateur.
     * @return Le nombre de défaites.
     */
    public int getPerdu() { return perdu; }

    //trophee

    /**
     * Ajoute un trophée à la liste des trophées de l'utilisateur.
     * @param trophee Le trophée à ajouter.
     */
    public void affTrophee(Trophee trophee) {
        trophees.add(trophee);
    }

    /**
     * Vérifie si l'utilisateur possède un trophée donné.
     * @param nom Le nom du trophée.
     * @return true si l'utilisateur possède le trophée, false sinon.
     */
    public boolean haveTrophee(String nom) {
        return trophees.stream().anyMatch(t -> t.getNom().equalsIgnoreCase(nom));
    }



    /**
     * Récupère le nombre de victoires consécutives de l'utilisateur.
     * @return Le nombre de victoires consécutives.
     */
    public int getWinconsecutive(){
        return winconsecutive;
    }
    /**
     * change le nombre de victoires consécutives de l'utilisateur.
     * @param winconsecutive Le nombre de victoires consécutives.
     */
    public void setWinconsecutive(int winconsecutive){
        this.winconsecutive = winconsecutive;
    }

    /**
     * Incrémente le nombre de victoires consécutives de l'utilisateur.
     */
    public void incrementWinconsecutive(){
        winconsecutive ++;
    }
    /**
     * Réinitialise le nombre de victoires consécutives de l'utilisateur.
     */
    public void resetVictoiresConsecutives() {
        this.winconsecutive = 0;
    }

    //Pour barre et trophés


    private int utilisationBazooka; // incrémenté quand il le user use bazooka en combat


    //Utile pour le trophé Or, on regarde si le user a use le bazooka.

    /**
     * Récupère le nombre d'utilisations du bazooka par l'utilisateur.
     * @return Le nombre d'utilisations du bazooka.
     */
    public int getUtilisationBazooka() { return utilisationBazooka; }


}
