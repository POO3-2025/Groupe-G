package be.helha.labos.crystalclash.LanternaApp;


import be.helha.labos.crystalclash.Characters.Personnage;
import be.helha.labos.crystalclash.DeserialiseurCustom.ObjectBaseDeserializer;
import be.helha.labos.crystalclash.Factory.CharactersFactory;
import be.helha.labos.crystalclash.Inventory.Inventory;
import be.helha.labos.crystalclash.Object.ObjectBase;
import be.helha.labos.crystalclash.Services.HttpService;
import be.helha.labos.crystalclash.User.UserInfo;
import be.helha.labos.crystalclash.User.UserManger;
import be.helha.labos.crystalclash.server_auth.Session;
import com.google.gson.*;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Window.Hint;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class LanternaApp {

    public static void main(String[] args) throws Exception {
        Screen screen = new DefaultTerminalFactory().createScreen();
        screen.startScreen();

        WindowBasedTextGUI gui = new MultiWindowTextGUI(screen);

        afficherEcranAccueil(gui, screen);
    }

    private static void afficherEcranAccueil(WindowBasedTextGUI gui, Screen screen) {
        BasicWindow window = new BasicWindow(" ");
        window.setHints(Arrays.asList(Hint.CENTERED));

        Panel panel = new Panel(new GridLayout(1));

        Label titre = new Label("✨ Crystal Clash : Connexion ✨");
        titre.setForegroundColor(TextColor.ANSI.BLACK);
        titre.addStyle(SGR.BOLD);
        panel.addComponent(titre);

        Label label = new Label("Bienvenue dans Crystal Clash !");
        label.setForegroundColor(TextColor.ANSI.CYAN_BRIGHT);
        label.setBackgroundColor(TextColor.ANSI.BLACK);
        label.addStyle(SGR.BOLD);

        panel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        panel.addComponent(new Button("1. Connexion", () -> afficherFormulaireConnexion(gui, window)));
        panel.addComponent(new Button("2. Inscription", () -> afficherFormulaireInscription(gui)));
        panel.addComponent(new Button("3. Quitter", () -> {
            try {
                screen.stopScreen();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.exit(0);
        }));

        window.setComponent(panel);
        gui.addWindowAndWait(window);
    }

    private static void afficherFormulaireConnexion(WindowBasedTextGUI gui, Window menuInitialWindow) {
        BasicWindow loginWindow = new BasicWindow("Connexion");
        loginWindow.setHints(Arrays.asList(Hint.CENTERED));

        Panel loginPanel = new Panel(new GridLayout(2));
        loginPanel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.BEGINNING));

        loginPanel.addComponent(new Label("Nom d'utilisateur :"));
        TextBox usernameBox = new TextBox();
        loginPanel.addComponent(usernameBox);

        loginPanel.addComponent(new Label("Mot de passe :"));
        TextBox passwordBox = new TextBox().setMask('*');
        loginPanel.addComponent(passwordBox);

        loginPanel.addComponent(new Button("Se connecter", () -> {
            try {
                String json = HttpService.login(usernameBox.getText(), passwordBox.getText());
                JsonObject response = JsonParser.parseString(json).getAsJsonObject();
                if (response.has("token") && !response.get("token").isJsonNull()) {
                    String token = response.get("token").getAsString();
                    Session.setToken(token);
                    Session.setUsername(usernameBox.getText());
                    // Ajout de l'utilisateur dans la liste des connectés après une connexion réussie
                    String username = usernameBox.getText();
                    UserManger.addUser(username);  // Ajoute le joueur à la liste des utilisateurs connectés
                    try {
                        //recoit un {"username":"toto","level":1,"cristaux":100}
                        //Gson pour le déserialiser en insatnce de userInfo
                        //ensuite Hop dans seesion pour l avoir dans tout lanterna
                        String userJson = HttpService.getUserInfo(Session.getUsername(), Session.getToken());
                        UserInfo info = new Gson().fromJson(userJson, UserInfo.class);
                        Session.setUserInfo(info); // stocke les infos dans la session
                    } catch (Exception ex) {
                        MessageDialog.showMessageDialog(gui, "Erreur", "Impossible de récupérer les infos joueur : " + ex.getMessage());
                    }

                    loginWindow.close();
                    menuInitialWindow.close();
                    afficherMenuPrincipal(gui);
                } else {
                    String message = response.has("message") ? response.get("message").getAsString() : "Réponse invalide.";
                    MessageDialog.showMessageDialog(gui, "Erreur", "Échec : " + message);
                }
            } catch (Exception e) {
                String message = e.getMessage();
                if (message == null || message.trim().isEmpty()) {
                    message = "Une erreur inconnue est survenue.";
                } else {
                    message = message.replaceAll("\\p{Cntrl}", " "); // Enlève tous les caractères de contrôle
                }
                MessageDialog.showMessageDialog(gui, "Exception", message);            }
        }));

        loginPanel.addComponent(new Button("Retour", loginWindow::close));
        loginWindow.setComponent(loginPanel);
        gui.addWindowAndWait(loginWindow);
    }

    private static void afficherFormulaireInscription(WindowBasedTextGUI gui) {
        BasicWindow registerWindow = new BasicWindow("Inscription");
        registerWindow.setHints(Arrays.asList(Hint.CENTERED));

        Panel registerPanel = new Panel(new GridLayout(2));
        registerPanel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.BEGINNING));

        registerPanel.addComponent(new Label("Nom d'utilisateur :"));
        TextBox usernameBox = new TextBox();
        registerPanel.addComponent(usernameBox);

        registerPanel.addComponent(new Label("Mot de passe :"));
        TextBox passwordBox = new TextBox().setMask('*');
        registerPanel.addComponent(passwordBox);

        registerPanel.addComponent(new Button("S'inscrire", () -> {
            try {
                String json = HttpService.register(usernameBox.getText(), passwordBox.getText());
                if (json.contains("Inscription réussie")) {
                    try {
                        String loginReponse = HttpService.login(usernameBox.getText(), passwordBox.getText());
                        //Parser le loginReposne pour le manipuler en java
                        // JsonParser.parseString(loginReponse) = convertit la chaine json en 1 objet Json
                        JsonObject response = JsonParser.parseString(loginReponse).getAsJsonObject(); //OK c bien un object json

                        if (response.has("token") && !response.get("token").isJsonNull()) {
                            String token = response.get("token").getAsString();
                            Session.setToken(token);
                            Session.setUsername(usernameBox.getText());

                            //Recup des infos uti
                            String UserJson = HttpService.getUserInfo(Session.getUsername(),token);
                            //gson convertit (deserialise) la chaine userJson en 1 insatnce de UserInfo
                            //Mapping auto
                            UserInfo info = new Gson().fromJson(UserJson, UserInfo.class);
                            Session.setUserInfo(info);
                            MessageDialog.showMessageDialog(gui, "Succès", "Compte créé et connecté !");
                            gui.getActiveWindow().close();
                            afficherChoixPersonnage(gui);
                        } else {
                            MessageDialog.showMessageDialog(gui, "Erreur", "Connexion automatique échouée.");
                        }
                    }catch (Exception e){
                        MessageDialog.showMessageDialog(gui, "Erreur", "Connexion auto impossible : " + e.getMessage());
                    }
                } else {
                    MessageDialog.showMessageDialog(gui, "Erreur", "Erreur : " + json);
                }
            } catch (Exception e) {
                MessageDialog.showMessageDialog(gui, "Exception", e.getMessage());
            }
        }));

        registerPanel.addComponent(new Button("Retour", registerWindow::close));
        registerWindow.setComponent(registerPanel);
        gui.addWindowAndWait(registerWindow);
    }

    private static void afficherMenuPrincipal(WindowBasedTextGUI gui) {
        BasicWindow menuWindow = new BasicWindow("Menu Principal");
        menuWindow.setHints(Arrays.asList(Hint.CENTERED));

        Panel mainPanel = new Panel(new GridLayout(1));
        mainPanel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.BEGINNING));

        mainPanel.addComponent(new Label(" Bienvenue dans Crystal Clash, " + Session.getUsername() + " !"));
        UserInfo info = Session.getUserInfo();
        //if (info != null) {
        //mainPanel.addComponent(new Label(" Niveau : " + info.getLevel()));
        //mainPanel.addComponent(new Label(" Cristaux : " + info.getCristaux()));
        // }
        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        mainPanel.addComponent(new Button("1. Voir profil", () -> afficherMonProfil(gui)));


        mainPanel.addComponent(new Button("2. Changer de personnage", () -> afficherChoixPersonnage(gui)));
        mainPanel.addComponent(new Button("3. Voir BackPack", () -> afficherBackPack(gui)));
        mainPanel.addComponent(new Button("4. Voir personnage", () -> afficherPersonnage(gui)));
        mainPanel.addComponent(new Button("5. Voir mon inventaire", () -> {
            displayInventory(gui);
        }));

        mainPanel.addComponent(new Button("6. Voir joueurs connectés", () -> DesplayUserConnected(gui)));
        mainPanel.addComponent(new Button("7. Accéder à la boutique", () -> DisplayShop(gui)));

        mainPanel.addComponent(new Button("8. Se déconnecter", () -> {
            Session.clear();
            MessageDialog.showMessageDialog(gui, "Déconnexion", "Vous avez été déconnecté !");
            gui.getActiveWindow().close();
            afficherEcranAccueil(gui, gui.getScreen());
        }));

        menuWindow.setComponent(mainPanel);
        gui.addWindowAndWait(menuWindow);
    }

    private static void DesplayUserConnected(WindowBasedTextGUI gui) {
        BasicWindow connectedUsersWindow = new BasicWindow("Joueurs Connectés");
        connectedUsersWindow.setHints(Arrays.asList(Hint.CENTERED));

        Panel panel = new Panel(new GridLayout(1));
        panel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.BEGINNING));

        // Récupérer la liste des utilisateurs connectés
        Set<String> connectedUsers = UserManger.getConnectedUsers();
        System.out.println("Utilisateurs connectés : " + connectedUsers);

        if (connectedUsers.isEmpty()) {
            panel.addComponent(new Label("Aucun joueur connecté."));
        } else {
            panel.addComponent(new Label("Joueurs connectés :"));
            for (String username : connectedUsers) {
                panel.addComponent(new Label(username));
            }
        }

        panel.addComponent(new Button("Retour", connectedUsersWindow::close));
        connectedUsersWindow.setComponent(panel);
        gui.addWindowAndWait(connectedUsersWindow);
    }

    /**
     * Affiche la fenêtre de choix de personnage
     * @param gui
     */
    private static void afficherChoixPersonnage(WindowBasedTextGUI gui) {
        BasicWindow characterChoiceWindow = new BasicWindow("Choix du personnage");
        characterChoiceWindow.setHints(Arrays.asList(Hint.CENTERED));

        Panel panel = new Panel(new GridLayout(1));
        panel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.BEGINNING));

        panel.addComponent(new Label("Choisissez votre personnage :"));

        Map<String, Integer> reqs = CharactersFactory.getRequiredLevelByType();
        for (String type : reqs.keySet()) {
            panel.addComponent(new Button(type + " (niveau " + reqs.get(type) + "+)",creerActionSelectionPersonnage(gui, type, characterChoiceWindow)
            ));

            try {
                // Création du personnage "exemple" via la factory
                Personnage perso = CharactersFactory.CreateCharacters(type, reqs.get(type));

                // Ajout des infos de ce perso
                panel.addComponent(new Label("   PV : " + perso.getPV()));
                panel.addComponent(new Label("   Attaque : " + perso.getNameAttackBase() + " (" + perso.getAttackBase() + ")"));
                panel.addComponent(new Label("   Attaque spéciale disponible après " + perso.getRestrictionAttackSpecial()+" attaques"));
                panel.addComponent(new Label("   Spéciale : " + perso.getNameAttaqueSpecial() + " (" + perso.getAttackSpecial() + ")"));
            } catch (IllegalArgumentException e) {
                // Si factory refuse (niveau trop bas), on skip l'affichage
                panel.addComponent(new Label("   [Indisponible à ce niveau]"));
            }
        }

        panel.addComponent(new Button("Retour", characterChoiceWindow::close));
        characterChoiceWindow.setComponent(panel);
        gui.addWindowAndWait(characterChoiceWindow);
    }

    /**
     * Crée une action pour sélectionner un personnage (pour pas repeter 1000 fois le même code)
     * @param gui => pour afficher les messages
     * @param personnage => le personnage à sélectionner
     * @return
     */
    private static Runnable creerActionSelectionPersonnage(WindowBasedTextGUI gui, String personnage, Window currentWindow) {
        return () -> {
            try {
                HttpService.selectCharacter(Session.getUsername(), personnage, Session.getToken());
                MessageDialog.showMessageDialog(gui, "Succès", "Personnage sélectionné : " + personnage);
                currentWindow.close(); //Ferme fenetre apres choix
                afficherMenuPrincipal(gui);
            } catch (Exception e) {
                String message = e.getMessage();

                if (message != null && message.toLowerCase().contains("déjà sélectionné")) {
                    MessageDialog.showMessageDialog(gui, "Info", "Ce personnage est déjà sélectionné.");
                } else if (message != null && message.toLowerCase().contains("niveau")) {
                    MessageDialog.showMessageDialog(gui, "Niveau insuffisant", message);
                } else {
                    MessageDialog.showMessageDialog(gui, "Erreur", message != null ? message : "Une erreur inconnue est survenue.");
                }
            }
        };
    }
    private static void afficherPersonnage(WindowBasedTextGUI gui) {
        BasicWindow persoWindow = new BasicWindow("Mon Personnage");
        persoWindow.setHints(Arrays.asList(Hint.CENTERED));

        Panel panel = new Panel(new GridLayout(1));
        panel.addComponent(new Label("Personnage de " + Session.getUsername()));

        try {
            String json = HttpService.getCharacter(Session.getUsername(), Session.getToken());
            JsonElement element = JsonParser.parseString(json);

            if (element.isJsonPrimitive()) {
                // On suppose que c'est juste un string comme "Troll"
                String characterType = element.getAsString();
                panel.addComponent(new Label("Type : " + characterType));

            } else if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();

                if (obj.has("message")) {
                    panel.addComponent(new Label("Level insuffisant : " + obj.get("message").getAsString()));
                } else {
                    panel.addComponent(new Label("Personnage non trouvé."));
                }
            } else {
                panel.addComponent(new Label("Format de réponse inconnu."));
            }
        } catch (Exception e) {
            panel.addComponent(new Label("Erreur de communication : " + e.getMessage()));
        }


        panel.addComponent(new Button("Retour", persoWindow::close));
        persoWindow.setComponent(panel);
        gui.addWindowAndWait(persoWindow);
    }
    private static void displayInventory(WindowBasedTextGUI gui) {
        try {
            String username = Session.getUsername();
            String json = HttpService.getInventory(username, Session.getToken());

            // Niveau du joueur pour filtrer les objets disponibles
            int levelPlayer = Session.getUserInfo().getLevel();

            // Création de Gson avec désérialiseur custom
            Gson gson = new GsonBuilder()
                .registerTypeAdapter(ObjectBase.class, new ObjectBaseDeserializer(levelPlayer))
                .create();

            // Désérialisation de l'inventaire avec le bon type d'objets
            Inventory inventory = gson.fromJson(json, Inventory.class);

            BasicWindow inventoryWindow = new BasicWindow("Inventaire de " + username);
            inventoryWindow.setHints(Arrays.asList(Hint.CENTERED));

            Panel panel = new Panel(new GridLayout(1));

            if (inventory.getObjets().isEmpty()) {
                panel.addComponent(new Label("Votre inventaire est vide."));
            } else {
                panel.addComponent(new Label("Objets dans l'inventaire :"));

                //Parcour liste objets de l'inventaire
                //obj représente object du joueur
                // Personnalisation selon le type
                for (ObjectBase obj : inventory.getObjets()) {
                    String label = obj.getName() + " (" + obj.getType() + ")";

                    panel.addComponent(new Button(label, () -> {

                        String details = obj.getDetails() + "\n\nSouhaitez-vous vendre cet objet ?";
                        MessageDialogButton response = MessageDialog.showMessageDialog(gui, "Détails de l'objet", details, MessageDialogButton.Yes, MessageDialogButton.No);

                        if (response == MessageDialogButton.Yes){
                            try{
                                //Appel de l'api
                                String reponse = HttpService.sellObjetc(obj.getName(), obj.getType(), Session.getToken());
                                JsonObject result = JsonParser.parseString(reponse).getAsJsonObject();
                                String message = result.get("message").getAsString();
                                MessageDialog.showMessageDialog(gui, "Ventre", message);
                                inventoryWindow.close();
                                displayInventory(gui);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }));
                }
            }
            panel.addComponent(new EmptySpace());
            panel.addComponent(new Button("Retour", inventoryWindow::close));
            inventoryWindow.setComponent(panel);
            gui.addWindowAndWait(inventoryWindow);

        } catch (Exception e) {
            MessageDialog.showMessageDialog(gui, "Erreur", "Impossible de charger l'inventaire : " + e.getMessage());
        }
    }

    private static void afficherBackPack(WindowBasedTextGUI gui) {
        BasicWindow window = new BasicWindow("Mon BackPack");
        window.setHints(Arrays.asList(Hint.CENTERED));

        Panel panel = new Panel(new GridLayout(1));
        String username = Session.getUsername();

        try {
            String json = HttpService.getBackpack(username, Session.getToken());
            ObjectBase[] objets = new Gson().fromJson(json, ObjectBase[].class);

            if (objets.length == 0) {
                panel.addComponent(new Label("Votre BackPack est vide."));
            } else {
                panel.addComponent(new Label("Contenu du BackPack :"));
                for (ObjectBase obj : objets) {
                    panel.addComponent(new Label("- " + obj.getName()));
                }
            }
        } catch (Exception e) {
            panel.addComponent(new Label("Erreur : " + e.getMessage()));
        }

        panel.addComponent(new Button("Retour", window::close));
        window.setComponent(panel);
        gui.addWindowAndWait(window);
    }
    /**

     Affiche le profil de l'utilisateur
     @param gui => pour afficher les messages*/
    public static void afficherMonProfil(WindowBasedTextGUI gui) {
        BasicWindow profileWindow = new BasicWindow("Mon Profil");
        profileWindow.setHints(Arrays.asList(Hint.CENTERED));

        Panel panel = new Panel(new GridLayout(1));
        panel.addComponent(new Label("Profil de " + Session.getUsername()));

        //Dés que le joueur clique sur mon profil ses crisatux son aut mis a jour
        try {
            String userJson = HttpService.getUserInfo(Session.getUsername(), Session.getToken());
            UserInfo updatedInfo = new Gson().fromJson(userJson, UserInfo.class);
            Session.setUserInfo(updatedInfo);
        } catch (Exception e) {
            System.out.println("Impossible de rafraîchir les infos du joueur : " + e.getMessage());
        }

        UserInfo info = Session.getUserInfo();
        if (info != null) {
            panel.addComponent(new Label("Niveau : " + info.getLevel()));
            panel.addComponent(new Label("Cristaux : " + info.getCristaux()));
            //pour mettre a jour les cristaux


            try {
                String personnageJson = HttpService.getCharacter(Session.getUsername(), Session.getToken());
                JsonElement element = JsonParser.parseString(personnageJson);
                String characterType = element.getAsString();
                panel.addComponent(new Label("Personnage choisi : " + characterType));
            } catch (Exception e) {
                panel.addComponent(new Label("Erreur lors du chargement du personnage."));
            }

        } else {
            panel.addComponent(new Label("Aucune information disponible."));
        }

        panel.addComponent(new Button("Retour", profileWindow::close));
        profileWindow.setComponent(panel);
        gui.addWindowAndWait(profileWindow);
    }

    private static void DisplayShop(WindowBasedTextGUI gui) {
        BasicWindow shopWindow = new BasicWindow("Boutique");
        shopWindow.setHints(Arrays.asList(Hint.CENTERED));

        int level = Session.getUserInfo().getLevel();

        Panel panel = new Panel(new GridLayout(1));
        //pour mettre a jour les cristaux
        try {
            String userJson = HttpService.getUserInfo(Session.getUsername(), Session.getToken());
            UserInfo updatedInfo = new Gson().fromJson(userJson, UserInfo.class);
            Session.setUserInfo(updatedInfo);
        } catch (Exception e) {
            System.out.println("Impossible de rafraîchir les infos du joueur : " + e.getMessage());
        }
        UserInfo info = Session.getUserInfo();
        panel.addComponent(new Label("Cristaux : " + info.getCristaux()));

        try {
            String json = HttpService.getShops(Session.getToken());
            List<Map<String, Object>> shopItems = new Gson().fromJson(json, List.class);

            panel.addComponent(new Label("Objets disponibles :"));
            for (Map<String, Object> item : shopItems) {
                String name = (String) item.get("name");
                String type = (String) item.get("type");
                double price = (double) item.get("price");
                double requiredLevel = (double) item.get("requiredLevel");
                if (level < requiredLevel) continue;
                panel.addComponent(new Button(name + " (" + type + ", " + price + "c, niv " + requiredLevel + "+)", () -> {
                    try {
                        String resultJson = HttpService.buyItem(name, type, Session.getToken());
                        JsonObject result = JsonParser.parseString(resultJson).getAsJsonObject();
                        String message = result.get("message").getAsString();
                        MessageDialog.showMessageDialog(gui, "Achat", message);
                        try{
                            String userJson = HttpService.getUserInfo(Session.getUsername(), Session.getToken());
                            UserInfo updateUserInfo = new Gson().fromJson(userJson, UserInfo.class);
                        }catch (Exception e){
                            System.out.println("Impossible de mettre a jour les info du joueur" + e.getMessage());
                        }
                    } catch (Exception e) {
                        MessageDialog.showMessageDialog(gui, "Erreur", "Impossible d'acheter : " + e.getMessage());
                    }
                }));
            }

        } catch (Exception e) {
            panel.addComponent(new Label("Erreur lors du chargement de la boutique : " + e.getMessage()));
        }

        panel.addComponent(new Button("Retour", shopWindow::close));
        shopWindow.setComponent(panel);
        gui.addWindowAndWait(shopWindow);
    }

}
