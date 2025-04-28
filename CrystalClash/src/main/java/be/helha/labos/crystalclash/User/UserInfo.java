package be.helha.labos.crystalclash.User;

public class UserInfo {
    private String username;
    private boolean isConnected;
    private int level;
    private int cristaux;
    private String selectedCharacter;
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

}
