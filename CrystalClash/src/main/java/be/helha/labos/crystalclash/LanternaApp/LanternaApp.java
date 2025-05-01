
package be.helha.labos.crystalclash.LanternaApp;


import be.helha.labos.crystalclash.Characters.Personnage;
import be.helha.labos.crystalclash.DeserialiseurCustom.ObjectBasePolymorphicDeserializer;
import be.helha.labos.crystalclash.Factory.CharactersFactory;
import be.helha.labos.crystalclash.Inventory.Inventory;
import be.helha.labos.crystalclash.Object.CoffreDesJoyaux;
import be.helha.labos.crystalclash.Object.ObjectBase;
import be.helha.labos.crystalclash.Services.HttpService;
import be.helha.labos.crystalclash.User.UserInfo;
import be.helha.labos.crystalclash.User.ConnectedUsers;
import be.helha.labos.crystalclash.server_auth.Session;
import com.google.gson.*;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Window.Hint;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import be.helha.labos.crystalclash.Combat.CombatManager;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;


public class LanternaApp {

    /**
     * Point d'entrée de l'application
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Screen screen = new DefaultTerminalFactory().createScreen();
        screen.startScreen();

        TextColor backgroundColor = TextColor.ANSI.BLACK; // Choisir couleur ici
        WindowBasedTextGUI gui = new MultiWindowTextGUI(
                screen,
                new DefaultWindowManager(),
                new EmptySpace(backgroundColor) // Fond global ici
        );

        afficherEcranAccueil(gui, screen);
    }

    /**
     * Affiche l'écran d'accueil
     *
     * @param gui
     * @param screen
     */
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

    /**
     * Affiche le formulaire de connexion
     *
     * @param gui
     * @param menuInitialWindow
     */
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
                    ConnectedUsers.addUser(username);  // Ajoute le joueur à la liste des utilisateurs connectés
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
                MessageDialog.showMessageDialog(gui, "Exception", message);
            }
        }));

        loginPanel.addComponent(new Button("Retour", loginWindow::close));
        loginWindow.setComponent(loginPanel);
        gui.addWindowAndWait(loginWindow);
    }

    /**
     * Affiche le formulaire d'inscription
     *
     * @param gui
     */
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
                            String UserJson = HttpService.getUserInfo(Session.getUsername(), token);
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
                    } catch (Exception e) {
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

    /**
     * Affiche le menu principal après la connexion
     *
     * @param gui
     */
    private static void afficherMenuPrincipal(WindowBasedTextGUI gui) {
        BasicWindow menuWindow = new BasicWindow();
        menuWindow.setHints(Arrays.asList(Hint.CENTERED));

        Panel mainPanel = new Panel(new GridLayout(1));
        mainPanel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.BEGINNING));

        Label welcomeLabel = new Label(" Bienvenue dans Crystal Clash, " + Session.getUsername() + " !");
        welcomeLabel.setForegroundColor(new TextColor.RGB(0, 128, 128));
        welcomeLabel.addStyle(SGR.BOLD);
        mainPanel.addComponent(welcomeLabel);

        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        // Section Profil
        mainPanel.addComponent(createSectionLabel("Profil"));
        mainPanel.addComponent(new Button("Voir profil", () -> afficherMonProfil(gui)));
        mainPanel.addComponent(new Button("Voir BackPack", () -> afficherBackPack(gui, () -> {
            afficherBackPack(gui, () -> {}); // on relance une fois pour rafraîchir le contenu
        })));
        mainPanel.addComponent(new Button("Voir personnage", () -> afficherPersonnage(gui)));
        mainPanel.addComponent(new Button("Voir mon inventaire", () -> {
            displayInventory(gui);
        }));;
        mainPanel.addComponent(new Button("Voir mon coffre", () -> afficherCoffre(gui,()-> {
            afficherCoffre(gui, () -> {});
        })));

        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));


        // Section Boutique
        mainPanel.addComponent(createSectionLabel("Boutique"));
        mainPanel.addComponent(new Button("Accéder à la boutique", () -> DisplayShop(gui)));
        mainPanel.addComponent(new Button("Jouer à la roulette (25 cristaux)", () -> PLayRoulette(gui)));

        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        // Section Communauté
        mainPanel.addComponent(createSectionLabel("Communauté"));
        mainPanel.addComponent(new Button("Voir joueurs connectés", () -> DesplayUserConnected(gui)));
        mainPanel.addComponent(new Button("Lancer un matchMaking", () -> MatchMaking(gui)));

        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        // Section Combat
        mainPanel.addComponent(createSectionLabel("Combat"));
        //mainPanel.addComponent(new Button("Lancer un combat", () -> lancerCombat(gui)));
        mainPanel.addComponent(new Button("Changer de personnage", () -> afficherChoixPersonnage(gui)));

        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));


        mainPanel.addComponent(new Button("Se déconnecter", () -> {
            try{
                HttpService.logout(Session.getUsername(), Session.getToken());
            } catch (Exception e) {
                System.out.println("Erreur lors de la déconnexion");
            }

            MessageDialog.showMessageDialog(gui, "Déconnexion", "Vous avez été déconnecté !");
            gui.getActiveWindow().close();
            afficherEcranAccueil(gui, gui.getScreen());
        }));

        menuWindow.setComponent(mainPanel);
        gui.addWindowAndWait(menuWindow);
    }
    /**
     * Affiche les section du menu
     * avec le style et la couleur
     * @param text => le texte de la section
     */
    private static Label createSectionLabel(String text) {
        Label label = new Label("─ " + text + " ─");
        label.setForegroundColor(new TextColor.RGB(50, 50, 50));
        label.addStyle(SGR.BOLD);
        return label;
    }

    /**
     * Affiche la liste des utilisateurs connectés
     *
     * @param gui
     */
    private static void DesplayUserConnected(WindowBasedTextGUI gui) {
        BasicWindow connectedUsersWindow = new BasicWindow("Joueurs Connectés");
        connectedUsersWindow.setHints(Arrays.asList(Hint.CENTERED));

        Panel panel = new Panel(new GridLayout(1));
        panel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.BEGINNING));
        try{
            Set<String> connectedUsers = HttpService.getConnectedUsers();

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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Affiche la fenêtre de choix de personnage
     *
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
            panel.addComponent(new Button(type + " (niveau " + reqs.get(type) + "+)", creerActionSelectionPersonnage(gui, type, characterChoiceWindow)
            ));

            try {
                // Création du personnage "exemple" via la factory
                Personnage perso = CharactersFactory.CreateCharacters(type, reqs.get(type));

                // Ajout des infos de ce perso
                panel.addComponent(new Label("   PV : " + perso.getPV()));
                panel.addComponent(new Label("   Attaque : " + perso.getNameAttackBase() + " (" + perso.getAttackBase() + ")"));
                panel.addComponent(new Label("   Attaque spéciale disponible après " + perso.getRestrictionAttackSpecial() + " attaques"));
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
     *
     * @param gui        => pour afficher les messages
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

            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();

                if (obj.has("message")) {
                    panel.addComponent(new Label(obj.get("message").getAsString()));
                }

                if (obj.has("data") && !obj.get("data").isJsonNull()) {
                    panel.addComponent(new Label("Type : " + obj.get("data").getAsString()));
                }

            } else {
                panel.addComponent(new Label("Réponse invalide du serveur."));
            }

        } catch (Exception e) {
            panel.addComponent(new Label("Erreur de communication : " + e.getMessage()));
        }

        panel.addComponent(new Button("Retour", persoWindow::close));
        persoWindow.setComponent(panel);
        gui.addWindowAndWait(persoWindow);
    }
    /**
     * Affiche l'inventaire du joueur
     *
     * @param gui => pour afficher les messages
     */
    private static void displayInventory(WindowBasedTextGUI gui) {
        try {
            String username = Session.getUsername();
            String json = HttpService.getInventory(username, Session.getToken());

            // Création de Gson avec désérialiseur custom
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
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
                // Sert a voir si le joueur a un coffre
                boolean hasCoffre = inventory.getObjets().stream()
                        .anyMatch(obj -> obj instanceof CoffreDesJoyaux);
                //Parcour liste objets de l'inventaire
                //obj représente object du joueur
                // Personnalisation selon le type
                for (ObjectBase obj : inventory.getObjets()) {
                    String label = obj.getName() + " (" + obj.getType() + ")";

                    panel.addComponent(new Button(label, () -> {
                        afficherDetailsObjet(gui, obj, () -> {
                            inventoryWindow.close();
                            displayInventory(gui);
                        }, hasCoffre);
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

    /**
     * Affiche les détails d'un objet
     * et propose de le vendre ou de le mettre dans le BackPack
     *
     * @param gui              => pour afficher les messages
     * @param obj              => l'objet à afficher
     * @param refreshInventory => pour rafraîchir l'inventaire après une action
     */
    private static void afficherDetailsObjet(WindowBasedTextGUI gui, ObjectBase obj, Runnable refreshInventory,Boolean hasCoffre) {
        BasicWindow window = new BasicWindow("Détails de l'objet");
        window.setHints(Arrays.asList(Hint.CENTERED));

        Panel panel = new Panel(new GridLayout(1));
        panel.addComponent(new Label(obj.getDetails()));
        panel.addComponent(new EmptySpace());
        panel.addComponent(new Button("Vendre", () -> {
            try {
                String reponse = HttpService.sellObjetc(obj.getName(), obj.getType(), Session.getToken());
                JsonObject result = JsonParser.parseString(reponse).getAsJsonObject();
                String message = result.get("message").getAsString();
                MessageDialog.showMessageDialog(gui, "Vente", message);
                window.close();
                refreshInventory.run();
            } catch (Exception e) {
                MessageDialog.showMessageDialog(gui, "Erreur", "Vente impossible : " + e.getMessage());
            }
        }));

        panel.addComponent(new Button("Mettre dans le BackPack", () -> {
            try {
                String result = HttpService.putInBackpack(Session.getUsername(), obj.getName(), obj.getType(), Session.getToken());
                JsonObject resultJson = JsonParser.parseString(result).getAsJsonObject();
                String message = resultJson.get("message").getAsString();
                MessageDialog.showMessageDialog(gui, "BackPack", message);
                window.close();
                refreshInventory.run();
            } catch (Exception e) {
                MessageDialog.showMessageDialog(gui, "Erreur", "Impossible de mettre dans le BackPack : " + e.getMessage());
            }
        }));
        if (hasCoffre){
            panel.addComponent(new Button("Mettre dans le coffre", () -> {
                try {
                    String result = HttpService.putInCoffre(Session.getUsername(), obj.getName(), obj.getType(), Session.getToken());
                    JsonObject resultJson = JsonParser.parseString(result).getAsJsonObject();
                    String message = resultJson.get("message").getAsString();
                    MessageDialog.showMessageDialog(gui, "Coffre", message);
                    window.close();
                    refreshInventory.run();
                } catch (Exception e) {
                    MessageDialog.showMessageDialog(gui, "Erreur", "Impossible de mettre dans le coffre : " + e.getMessage());
                }
            }));
        }

        panel.addComponent(new Button("Retour", window::close));
        window.setComponent(panel);
        gui.addWindowAndWait(window);
    }


    /**
     * Affiche le profil de l'utilisateur
     *
     * @param gui => pour afficher les messages
     */
    public static void afficherMonProfil(WindowBasedTextGUI gui) {
        BasicWindow profileWindow = new BasicWindow("Mon Profil");
        profileWindow.setHints(Arrays.asList(Hint.CENTERED));

        Panel panel = new Panel(new GridLayout(1));
        panel.addComponent(new Label("Profil de " + Session.getUsername()));

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
            panel.addComponent(new Label("Personnage choisi : " + info.getSelectedCharacter()));
        } else {
            panel.addComponent(new Label("Aucune information disponible."));
        }

        panel.addComponent(new Button("Retour", profileWindow::close));
        profileWindow.setComponent(panel);
        gui.addWindowAndWait(profileWindow);
    }

    /**
     * Affiche la boutique
     *
     * @param gui
     */
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
                        try {
                            String userJson = HttpService.getUserInfo(Session.getUsername(), Session.getToken());
                            UserInfo updateUserInfo = new Gson().fromJson(userJson, UserInfo.class);
                        } catch (Exception e) {
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

    /**
     * Affiche le contenu du BackPack
     *
     * @param gui => pour afficher les messages
     */
    private static void afficherBackPack(WindowBasedTextGUI gui, Runnable refreshBackpack) {
        BasicWindow window = new BasicWindow("Mon BackPack");
        window.setHints(Arrays.asList(Hint.CENTERED));

        Panel panel = new Panel(new GridLayout(1));
        String username = Session.getUsername();

        try {
            String json = HttpService.getBackpack(username, Session.getToken());

            // Parse le JSON pour extraire "data"
            JsonObject response = JsonParser.parseString(json).getAsJsonObject();
            JsonArray dataArray = response.getAsJsonArray("data");

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                    .create();

            ObjectBase[] objets = gson.fromJson(dataArray, ObjectBase[].class);

            boolean hasCoffre = Arrays.stream(objets)
                    .anyMatch(obj -> obj instanceof CoffreDesJoyaux);

            if (objets.length == 0) {
                panel.addComponent(new Label("Votre BackPack est vide."));
            } else {
                panel.addComponent(new Label("Contenu du BackPack :"));
                for (ObjectBase obj : objets) {
                    String label = obj.getName() + " (" + obj.getType() + ")";
                    panel.addComponent(new Button(label, () -> {
                        BasicWindow detailsWindow = new BasicWindow("Détails de l'objet");
                        detailsWindow.setHints(Arrays.asList(Hint.CENTERED));

                        Panel detailsPanel = new Panel(new GridLayout(1));
                        detailsPanel.addComponent(new Label(obj.getDetails()));
                        detailsPanel.addComponent(new EmptySpace());

                        detailsPanel.addComponent(new Button("Retirer du backPack", () -> {
                            try {
                                String reponse = HttpService.removeFromBackpack(Session.getUsername(), obj.getName(), obj.getType(), Session.getToken());
                                JsonObject result = JsonParser.parseString(reponse).getAsJsonObject();
                                String message = result.get("message").getAsString();
                                MessageDialog.showMessageDialog(gui, "Retrait", message);
                                detailsWindow.close();
                                window.close();
                                refreshBackpack.run();
                            } catch (Exception e) {
                                MessageDialog.showMessageDialog(gui, "Erreur", "Impossible de retirer du backPack : " + e.getMessage());
                            }
                        }));

                        if (hasCoffre) {
                            detailsPanel.addComponent(new Button("Mettre dans le coffre", () -> {
                                try {
                                    String result = HttpService.putInCoffreBackPack(Session.getUsername(), obj.getName(), obj.getType(), Session.getToken());
                                    JsonObject resultJson = JsonParser.parseString(result).getAsJsonObject();
                                    String message = resultJson.get("message").getAsString();
                                    MessageDialog.showMessageDialog(gui, "Coffre", message);
                                    detailsWindow.close();
                                    refreshBackpack.run();
                                } catch (Exception e) {
                                    MessageDialog.showMessageDialog(gui, "Erreur", "Impossible de mettre dans le coffre : " + e.getMessage());
                                }
                            }));
                        }

                        detailsPanel.addComponent(new Button("Retour", detailsWindow::close));
                        detailsWindow.setComponent(detailsPanel);
                        gui.addWindowAndWait(detailsWindow);
                    }));
                }
            }

        } catch (Exception e) {
            panel.addComponent(new Label("Erreur : " + e.getMessage()));
        }

        panel.addComponent(new Button("Retour", window::close));
        window.setComponent(panel);
        gui.addWindowAndWait(window);
    }

    private static void PLayRoulette (WindowBasedTextGUI gui){
        try{
            String json = HttpService.PlayRoulette(Session.getUsername(), Session.getToken());
            //Convertit une chaine json recue depuis le serveur en 1 objet java que l'on peut jouer avec
            //et acceder au champ
            JsonObject result = JsonParser.parseString(json).getAsJsonObject();

            //extaction du message erreur ou pas et le recup
            String message = result.has("message") ? result.get("message").getAsString() : "";

            //Recup data dans la reponse Json qui contienr objet si le j a win
            // Vérifie si "data" est présent et contient un objet
            if (result.has("data") && result.getAsJsonObject("data").has("objet")) {
                JsonObject objet = result.getAsJsonObject("data").getAsJsonObject("objet");
                //Description
                String name = objet.get("name").getAsString();
                String type = objet.get("type").getAsString();
                String desc = " Vous avez gagné : " + name + " (" + type + ") !";

                //pour different type d'objet ça s'applique
                if (objet.has("damage")) desc += "\nDégâts : " + objet.get("damage").getAsInt();
                if (objet.has("defense")) desc += "\nDéfense : " + objet.get("defense").getAsInt();
                if (objet.has("reliability")) desc += "\nFiabilité : " + objet.get("reliability").getAsInt();

                MessageDialog.showMessageDialog(gui, "Roulette - Gain !", desc);
            } else {
                MessageDialog.showMessageDialog(gui, "Roulette", message);
            }

        }catch (Exception e){
            e.printStackTrace();
            MessageDialog.showMessageDialog(gui, " ", " " + e.getMessage());

        }
    }

    /**
     * Affiche le coffre du joueur
     *
     * @param gui => pour afficher les messages
     */
    private static void afficherCoffre(WindowBasedTextGUI gui, Runnable refreshCoffre) {
        BasicWindow coffreWindow = new BasicWindow("Mon Coffre");
        coffreWindow.setHints(Arrays.asList(Hint.CENTERED));

        Panel panel = new Panel(new GridLayout(1));
        panel.addComponent(new Label("Coffre de " + Session.getUsername()));

        try {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                    .create();

            String jsonInventaire = HttpService.getInventory(Session.getUsername(), Session.getToken());
            Inventory inventory = gson.fromJson(jsonInventaire, Inventory.class);

            CoffreDesJoyaux coffre = null;
            boolean depuisBackpack = false;

            for (ObjectBase obj : inventory.getObjets()) {
                if (obj instanceof CoffreDesJoyaux) {
                    coffre = (CoffreDesJoyaux) obj;
                    break;
                }
            }

            if (coffre == null) {
                String jsonBackpack = HttpService.getBackpack(Session.getUsername(), Session.getToken());
                ObjectBase[] objetsBackpack = gson.fromJson(jsonBackpack, ObjectBase[].class);
                for (ObjectBase obj : objetsBackpack) {
                    if (obj instanceof CoffreDesJoyaux) {
                        coffre = (CoffreDesJoyaux) obj;
                        depuisBackpack = true;
                        break;
                    }
                }
            }

            if (coffre != null) {
                List<ObjectBase> contenu = coffre.getContenu();
                if (contenu == null || contenu.isEmpty()) {
                    panel.addComponent(new Label("Le coffre est vide."));
                } else {
                    panel.addComponent(new Label("Contenu :"));
                    panel.addComponent(new Label("Nombre de place : " + coffre.getContenu().size() + " / " + coffre.getMaxCapacity()));
                    for (ObjectBase obj : contenu) {
                        String label = obj.getName() + " (" + obj.getType() + ")";
                        boolean finalDepuisBackpack = depuisBackpack;
                        panel.addComponent(new Button(label, () -> {
                            detailContenuCoffre(gui, obj, finalDepuisBackpack, () -> {
                                coffreWindow.close();
                                afficherCoffre(gui, refreshCoffre);
                            });
                        }));
                    }
                }
            } else {
                panel.addComponent(new Label("Vous ne possédez pas encore de Coffre des Joyaux."));
            }

        } catch (Exception e) {
            panel.addComponent(new Label("Erreur lors du chargement du coffre : " + e.getMessage()));
        }

        panel.addComponent(new Button("Retour", coffreWindow::close));
        coffreWindow.setComponent(panel);
        gui.addWindowAndWait(coffreWindow);
    }

    public static void detailContenuCoffre(WindowBasedTextGUI gui, ObjectBase obj, Boolean depuisBackpack, Runnable refreshCoffre) {
        BasicWindow detailsWindow = new BasicWindow("Détails de l'objet dans le coffre");
        detailsWindow.setHints(Arrays.asList(Hint.CENTERED));

        Panel panel = new Panel(new GridLayout(1));
        panel.addComponent(new Label(obj.getDetails()));
        panel.addComponent(new EmptySpace());
        if (depuisBackpack) {
            panel.addComponent(new Button("Mettre dans l'inventaire", () -> {
                try {
                    String result = HttpService.removeFromBackpack(Session.getUsername(), obj.getName(), obj.getType(), Session.getToken());
                    String message = JsonParser.parseString(result).getAsJsonObject().get("message").getAsString();
                    MessageDialog.showMessageDialog(gui, "Info", message);
                    detailsWindow.close();
                    refreshCoffre.run();
                } catch (Exception e) {
                    MessageDialog.showMessageDialog(gui, "Erreur", e.getMessage());
                }
            }));
        } else {
            panel.addComponent(new Button("Mettre dans le BackPack", () -> {
                try {
                    String result = HttpService.putInBackpack(Session.getUsername(), obj.getName(), obj.getType(), Session.getToken());
                    String message = JsonParser.parseString(result).getAsJsonObject().get("message").getAsString();
                    MessageDialog.showMessageDialog(gui, "Info", message);
                    detailsWindow.close();
                    refreshCoffre.run();
                } catch (Exception e) {
                    MessageDialog.showMessageDialog(gui, "Erreur", e.getMessage());
                }
            }));
        }
        panel.addComponent(new Button("Retour", detailsWindow::close));
        detailsWindow.setComponent(panel);
        gui.addWindowAndWait(detailsWindow);
    }


    public static void MatchMaking(WindowBasedTextGUI gui) {
        BasicWindow combatWindow = new BasicWindow("Matchmaking");
        combatWindow.setHints(Arrays.asList(Hint.CENTERED));

        Panel panel = new Panel(new GridLayout(1));
        Label loadingLabel = new Label("Recherche d'un adversaire...");
        panel.addComponent(loadingLabel);

        combatWindow.setComponent(panel);
        gui.addWindow(combatWindow);
       //lancage en arriere plan pour evite de figer
        //Thread normal quoi ca lance un new processus en arriere plan
        new Thread(() -> {
            try {
                Thread.sleep(2000); //  pause pour simuler un chargement

                //Thread secondaire
                String opponent = HttpService.matcjmaking(Session.getUsername(), Session.getToken());

                //Une fois trouver ici il aura la modif de linterface graphique
                //Car le thread secondaire(fait la recherche) ne peut pas modif de lui meme
                gui.getGUIThread().invokeLater(() -> {
                    combatWindow.close();
                    openCombatWindow(gui, opponent); // demarre directement le combat  encore rien la
                });
            } catch (Exception e) {
                gui.getGUIThread().invokeLater(() -> { //Serveur repond
                    MessageDialog.showMessageDialog(gui, "Erreur", "Adversaire introuvable " + e.getMessage());
                    combatWindow.close();
                });
            }
        }).start();
    }


    /**
     * Ouvre la fenêtre de combat
     * @param gui
     * @param opponent
     */
    public static void openCombatWindow(WindowBasedTextGUI gui, String opponent) {
        BasicWindow combatWindow = new BasicWindow("Combat contre " + opponent);
        combatWindow.setHints(Arrays.asList(Hint.CENTERED));

        // Crée une nouvelle interface pour le combat
        Panel panel = new Panel(new GridLayout(1));
        panel.addComponent(new Label("Combat en cours contre " + opponent + "..."));

        // Ajouter ici les composants du combat, comme des boutons pour attaquer, défendre, etc.

        panel.addComponent(new Button("Quitter", combatWindow::close));  // Permet à l'utilisateur de quitter le combat
        combatWindow.setComponent(panel);
        gui.addWindowAndWait(combatWindow);  // Affiche la fenêtre de combat
    }


}

