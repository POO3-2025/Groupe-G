package be.helha.labos.crystalclash.LanternaApp;


import be.helha.labos.crystalclash.Characters.Personnage;
import be.helha.labos.crystalclash.DTO.StateCombat;
import be.helha.labos.crystalclash.DTO.Trophee;
import be.helha.labos.crystalclash.DeserialiseurCustom.ObjectBasePolymorphicDeserializer;
import be.helha.labos.crystalclash.Factory.CharactersFactory;
import be.helha.labos.crystalclash.Inventory.Inventory;
import be.helha.labos.crystalclash.Object.*;
import be.helha.labos.crystalclash.Service.TropheeService;
import be.helha.labos.crystalclash.Services.HttpService;
import be.helha.labos.crystalclash.User.UserInfo;
import be.helha.labos.crystalclash.User.ConnectedUsers;
import be.helha.labos.crystalclash.server_auth.Session;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Window.Hint;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


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
                    try {
                        //recoit un {"username":"toto","level":1,"cristaux":100}
                        //Gson pour le déserialiser en insatnce de userInfo
                        //ensuite Hop dans seesion pour l avoir dans tout lanterna
                        String userJson = HttpService.getUserInfo(Session.getUsername(), Session.getToken());
                        UserInfo info = new Gson().fromJson(userJson, UserInfo.class);
                        Session.setUserInfo(info); // stocke les infos dans la session
                        //Ajoute l'utilisateur dans la liste des connectés
                        ConnectedUsers.addUser(info);
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
            afficherBackPack(gui, () -> {
            }); // on relance une fois pour rafraîchir le contenu
        })));
        mainPanel.addComponent(new Button("Voir personnage", () -> afficherPersonnage(gui)));
        mainPanel.addComponent(new Button("Voir mon inventaire", () -> {
            displayInventory(gui);
        }));
        ;
        mainPanel.addComponent(new Button("Voir mon coffre", () -> afficherCoffre(gui, () -> {
            afficherCoffre(gui, () -> {
            });
        })));

        mainPanel.addComponent(new Button("Mes trophées", () -> afficherTrophees(gui)));

        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        //Setcion classement
        mainPanel.addComponent(createSectionLabel("Classement"));
        mainPanel.addComponent(new Button("Accéder au classement", () -> DisplayClassement(gui)));
        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        // Section Boutique
        mainPanel.addComponent(createSectionLabel("Boutique"));
        mainPanel.addComponent(new Button("Accéder à la boutique", () -> DisplayShop(gui)));
        mainPanel.addComponent(new Button("Jouer à la roulette (25 cristaux)", () -> PLayRoulette(gui)));

        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        // Section Communauté
        mainPanel.addComponent(createSectionLabel("Communauté"));
        mainPanel.addComponent(new Button("Voir joueurs connectés", () -> DesplayUserConnected(gui)));
        mainPanel.addComponent(new Button("Salle d'attente", () -> MatchMaking(gui)));

        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        // Section Combat
        mainPanel.addComponent(createSectionLabel("Combat"));
        mainPanel.addComponent(new Button("Lancer un combat", () -> openCombatWindow(gui, "testadversaire")));
        mainPanel.addComponent(new Button("Changer de personnage", () -> afficherChoixPersonnage(gui)));

        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));


        mainPanel.addComponent(new Button("Se déconnecter", () -> {
            try {
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
     *
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
        try {
            List<UserInfo> connectedUsers = HttpService.getConnectedUsers();

            if (connectedUsers.isEmpty()) {
                panel.addComponent(new Label("Aucun joueur connecté."));
            } else {
                panel.addComponent(new Label("Joueurs connectés :"));
                for (UserInfo user : connectedUsers) {
                    String line = user.getUsername() + " | Niveau : " + user.getLevel();
                    panel.addComponent(new Label(line));
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

                if (obj.has("data") && !obj.get("data").isJsonNull()) {
                    panel.addComponent(new Label("Type : " + obj.get("data").getAsString()));
                    Personnage perso = CharactersFactory.getCharacterByType(obj.get("data").getAsString());
                    panel.addComponent(new Label("PV : " + perso.getPV()));
                    panel.addComponent(new Label("Attaque Normale : " + perso.getNameAttackBase()));
                    panel.addComponent(new Label("Attaque Spéciale : " + perso.getNameAttaqueSpecial()));
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
    private static void afficherDetailsObjet(WindowBasedTextGUI gui, ObjectBase obj, Runnable refreshInventory, Boolean hasCoffre) {
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
        if (hasCoffre) {
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
            panel.addComponent(new Label("Combats gagnés : " + info.getGagner()));

            panel.addComponent(new Label("Comnbats perdus" + ": " + info.getPerdu()));

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

        UserInfo info = Session.getUserInfo();
        if (info != null) {
            panel.addComponent(new Label("Niveau : " + info.getLevel()));
            panel.addComponent(new Label("Cristaux : " + info.getCristaux()));
            panel.addComponent(new Label("Personnage choisi : " + info.getSelectedCharacter()));
        } else {
            panel.addComponent(new Label("Aucune information disponible."));
        }
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
                    .anyMatch(objet -> objet instanceof CoffreDesJoyaux);

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

    private static void PLayRoulette(WindowBasedTextGUI gui) {
        try {
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

        } catch (Exception e) {
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

        CoffreDesJoyaux coffre = null;
        boolean depuisBackpack = false;

        try {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                    .create();

            // 1. Vérifie l'inventaire
            String jsonInventaire = HttpService.getInventory(Session.getUsername(), Session.getToken());
            Inventory inventory = gson.fromJson(jsonInventaire, Inventory.class);

            for (ObjectBase obj : inventory.getObjets()) {
                if (obj instanceof CoffreDesJoyaux) {
                    coffre = (CoffreDesJoyaux) obj;
                    break;
                }
            }

            // 2. Si pas trouvé dans l'inventaire, vérifie dans le backpack
            if (coffre == null) {
                String jsonBackpack = HttpService.getBackpack(Session.getUsername(), Session.getToken());
                JsonElement root = JsonParser.parseString(jsonBackpack);

                if (root.isJsonArray()) {
                    ObjectBase[] objetsBackpack = gson.fromJson(root, ObjectBase[].class);
                    for (ObjectBase obj : objetsBackpack) {
                        if (obj instanceof CoffreDesJoyaux) {
                            coffre = (CoffreDesJoyaux) obj;
                            depuisBackpack = true;
                            break;
                        }
                    }
                } else {
                    panel.addComponent(new Label("Aucun coffre trouvé dans le sac à dos ou dans l'inventaire"));
                }
            }

            // 3. Affichage du contenu ou message vide
            if (coffre != null) {
                List<ObjectBase> contenu = coffre.getContenu();
                if (contenu == null || contenu.isEmpty()) {
                    panel.addComponent(new Label("Le coffre est vide."));
                } else {
                    panel.addComponent(new Label("Contenu :"));
                    panel.addComponent(new Label("Nombre de place : " + contenu.size() + " / " + coffre.getMaxCapacity()));
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
            } else if (!panel.getChildren().stream().anyMatch(c -> c instanceof Label && ((Label) c).getText().contains("coffre"))) {
                panel.addComponent(new Label("Vous ne possédez pas encore de Coffre des Joyaux."));
            }

        } catch (Exception e) {
            panel.addComponent(new Label("Erreur inattendue : " + e.getClass().getSimpleName()));
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
        BasicWindow combatWindow = new BasicWindow("Salle de matchMaking");
        combatWindow.setHints(Arrays.asList(Hint.CENTERED));
        //Boolean pour les thread
        //Utilise pour control u nthrad en cours d'execution. le thread seco verra obligatoirement la mise a jour
        AtomicBoolean shouldRun = new AtomicBoolean(true);

        Panel panel = new Panel(new GridLayout(1));
        Label infoLabel = new Label("Recherche d'autres joueurs dans la salle...");
        panel.addComponent(infoLabel);
        panel.addComponent(new EmptySpace());

        Panel usersPanel = new Panel(new GridLayout(1));
        panel.addComponent(usersPanel);

        panel.addComponent(new Button("Quitter la salle", () -> {
            shouldRun.set(false); // Demande d'arrêt,passe a faut si on clique
            try {
                Map<String, String> data = new HashMap<>();
                data.put("username", Session.getUsername());
                HttpService.exitMatchmakingRoom(Session.getUsername(), Session.getToken());
            } catch (Exception ignored) {
            }
            combatWindow.close();
        }));

        combatWindow.setComponent(panel);
        gui.addWindow(combatWindow);

        //Autre try pour entrer dans la salle d'attente
        try {
            UserInfo userInfo = Session.getUserInfo();
            String user = new Gson().toJson(userInfo);
            HttpService.enterMatchmakingRoom(Session.getUserInfo(), Session.getToken());
        } catch (Exception e) {
            MessageDialog.showMessageDialog(gui, "Erreur", "Impossible d'entrer dans la salle : " + e.getMessage());
            combatWindow.close();
            return;
        }

        //Thread pour rafraichir la liste
        //Utile coté client pour ne pas crache
        new Thread(() -> {
            while (shouldRun.get()) {
                try {
                    //A partir d'ici c un thread secondaire
                    Thread.sleep(2000); //attend 2 sec

                    // Ajout en haut de la boucle pour détecter si l'utilisateur est défié
                    String json = HttpService.getCombatState(Session.getUsername(), Session.getToken());
                    System.out.println("CombatState reçu (thread matchmaking) : " + json);

                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                            .create();
                    StateCombat state = gson.fromJson(json, StateCombat.class);

                    if (state != null && state.getPlayerNow() != null) {
                        gui.getGUIThread().invokeLater(() -> {
                            System.out.println("===> Passage dans invokeLater : lancement du combat");
                            shouldRun.set(false);
                            combatWindow.close();
                            LanternaApp.lancerCombat(gui, state);
                        });
                        break;
                    }


                    //Appelle du endpoint
                    List<UserInfo> opponents = HttpService.getAvailableOpponents(Session.getUsername(), Session.getToken());

                    //Lorsqu'on a une réponse du serveur la mise a jour de l'interface ce fait ici
                    //invokeLater permet de repasser sur le thread pricipale
                    gui.getGUIThread().invokeLater(() -> {
                        if (!shouldRun.get()) return;
                        usersPanel.removeAllComponents();
                        if (opponents.isEmpty()) {
                            usersPanel.addComponent(new Label("Aucun joueur dans la salle"));
                        } else {
                            for (UserInfo opponent : opponents) {
                                String label = "Défier " + opponent.getUsername() + " (Niv " + opponent.getLevel() + ")";
                                if (opponent.getSelectedCharacter() != null) {
                                    label += " - " + opponent.getSelectedCharacter();
                                }
                                Button challenge = new Button(label, () -> {
                                    try {
                                        HttpService.challengePlayer(Session.getUsername(), opponent.getUsername(), Session.getToken());
                                        MessageDialog.showMessageDialog(gui, "Défi lancé", "Vous avez défié " + opponent.getUsername() + " !");
                                        shouldRun.set(false);

                                        // Relancer un thread qui vérifie le combat côté joueur initiateur
                                        new Thread(() -> {
                                            try {
                                                Thread.sleep(1500); // Laisse au serveur le temps de créer le combat
                                                String json1 = HttpService.getCombatState(Session.getUsername(), Session.getToken());
                                                Gson gson1 = new GsonBuilder()
                                                        .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                                                        .create();
                                                StateCombat state1 = gson.fromJson(json, StateCombat.class);

                                                if (state != null && state.getPlayerNow() != null) {
                                                    gui.getGUIThread().invokeLater(() -> {
                                                        combatWindow.close();
                                                        LanternaApp.lancerCombat(gui, state);
                                                    });
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }).start();

                                    } catch (Exception e) {
                                        MessageDialog.showMessageDialog(gui, "Erreur", "Défi impossible : " + e.getMessage());
                                    }
                                });

                                usersPanel.addComponent(challenge);
                            }
                        }
                        combatWindow.setComponent(panel); // Re-render
                    });
                } catch (Exception ignored) {
                }
            }
        }).start();

    }




    private static void updateToursRestants(Personnage perso, Label label) {
        int toursRestants = perso.getRestrictionAttackSpecial() - perso.getCompteurAttack();
        if (toursRestants <= 0) {
            label.setText("✅ Attaque spéciale disponible !");
        } else {
            label.setText("⏳ Il reste " + toursRestants + " tour" + (toursRestants > 1 ? "s" : "") + " avant l’attaque spéciale.");
        }
    }

    /**
     * Ouvre la fenêtre de combat
     *
     * @param gui
     * @param adversaireNom
     */
    private static void openCombatWindow(WindowBasedTextGUI gui, String adversaireNom) {
        BasicWindow combatWindow = new BasicWindow("Combat");
        combatWindow.setHints(Arrays.asList(Hint.CENTERED));

        Panel combatPanel = new Panel(new GridLayout(2));
        combatPanel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.FILL));

        // Panel historique (gauche)
        Panel historyPanel = new Panel(new GridLayout(1));
        historyPanel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.FILL));
        Label historyLabel = new Label("Historique du Combat");
        historyPanel.addComponent(historyLabel);
        StringBuilder history = new StringBuilder();
        history.append("==== TOUR 1 ====\n");

        // Panel stats et actions (droite)
        Panel statsPanel = new Panel(new GridLayout(1));
        statsPanel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.FILL));

        Label combatTitle = new Label("🔥 Combat contre " + adversaireNom + " 🔥");
        combatTitle.setForegroundColor(TextColor.ANSI.RED);
        combatTitle.addStyle(SGR.BOLD);
        statsPanel.addComponent(combatTitle);

        Label combatDescription = new Label("Votre adversaire : " + adversaireNom + " - Niveau 5");
        combatDescription.setForegroundColor(TextColor.ANSI.YELLOW);
        statsPanel.addComponent(combatDescription);

        AtomicInteger tourCounter = new AtomicInteger(1);
        Label tourLabel = new Label("🕒 Tour : " + tourCounter.get());
        tourLabel.setForegroundColor(TextColor.ANSI.CYAN);
        tourLabel.addStyle(SGR.BOLD);
        statsPanel.addComponent(tourLabel);

        Panel playerInfoPanel = new Panel(new GridLayout(1));
        playerInfoPanel.addComponent(new Label("Personnage de " + Session.getUsername()));
        statsPanel.addComponent(playerInfoPanel);

        AtomicInteger enemyHP = new AtomicInteger(100);
        Label enemyHealth = new Label(adversaireNom + " santé : " + enemyHP.get() + " HP");

        Label toursRestantsLabel = new Label(""); // Label pour l’attaque spéciale
        statsPanel.addComponent(toursRestantsLabel);


        AtomicInteger bonusNextAttack = new AtomicInteger(0);
        AtomicBoolean turnPotionForce = new AtomicBoolean(false);   // Tour de la potion active
        Label bonusattaque = new Label("Le bonus d'attaque est de " + bonusNextAttack.get());
        statsPanel.addComponent(bonusattaque);


        try {
            String json = HttpService.getCharacter(Session.getUsername(), Session.getToken());
            JsonElement element = JsonParser.parseString(json);

            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();

                if (obj.has("data") && !obj.get("data").isJsonNull()) {
                    String characterType = obj.get("data").getAsString();
                    playerInfoPanel.addComponent(new Label("Type : " + characterType));

                    Personnage perso = CharactersFactory.getCharacterByType(characterType);

                    AtomicInteger playerHP = new AtomicInteger(perso.getPV());
                    Label playerHealth = new Label("Votre santé : " + playerHP.get() + " HP");

                    statsPanel.addComponent(playerHealth);
                    statsPanel.addComponent(enemyHealth);

                    String attaqueNormale = perso.getNameAttackBase();
                    String attaqueSpeciale = perso.getNameAttaqueSpecial();

                    // Initialiser le label tours restants
                    updateToursRestants(perso, toursRestantsLabel);

                    // --- Panel pour les actions ---
                    Panel actionsPanel = new Panel(new GridLayout(1));
                    actionsPanel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.FILL));
                    statsPanel.addComponent(new Label("Actions :"));

// Boutons principaux (déclarés plus haut)
                    final Button[] showNormalAttacks = new Button[1];
                    final Button[] showSpecialAttacks = new Button[1];
                    final Button[] objectButton = new Button[1];

// Attaque normale
                    showNormalAttacks[0] = new Button("Attaque Normale", () -> {
                        actionsPanel.removeAllComponents();

                        Button attackButton = new Button(attaqueNormale, () -> {
                            int playerDamage = perso.getAttackBase();
                            if (turnPotionForce.get()){
                                playerDamage += bonusNextAttack.get();
                                bonusNextAttack.set(0);
                                turnPotionForce.set(false);
                            }
                            bonusattaque.setText("Le bonus d'attaque est de " + bonusNextAttack.get());
                            enemyHP.addAndGet(-playerDamage);
                            playerHealth.setText("Votre santé : " + playerHP.get() + " HP");
                            enemyHealth.setText(adversaireNom + " santé : " + enemyHP.get() + " HP");

                            history.append("Vous avez infligé " + playerDamage + " PV avec " + attaqueNormale + ".\n");
                            historyLabel.setText(history.toString());

                            perso.CompteurAttack(perso.getCompteurAttack() + 1);
                            updateToursRestants(perso, toursRestantsLabel);

                            enemyTurn(gui, adversaireNom, playerHealth, enemyHealth, combatWindow,
                                    playerHP, enemyHP, historyLabel, history, tourCounter, tourLabel,
                                    actionsPanel, showNormalAttacks[0], showSpecialAttacks[0], objectButton[0]);

                        });

                        Button backButton = new Button("Retour", () -> {
                            showMainActions(actionsPanel, showNormalAttacks[0], showSpecialAttacks[0], objectButton[0]);
                        });

                        actionsPanel.addComponent(attackButton);
                        actionsPanel.addComponent(backButton);
                    });

// Attaque spéciale
                    showSpecialAttacks[0] = new Button("Attaque Spéciale", () -> {
                        actionsPanel.removeAllComponents();

                        Button attackButton = new Button(attaqueSpeciale, () -> {
                            if (perso.getCompteurAttack() >= perso.getRestrictionAttackSpecial()) {
                                int playerDamage = perso.getAttackSpecial();
                                if (turnPotionForce.get()){
                                    playerDamage += bonusNextAttack.get();
                                    bonusNextAttack.set(0);
                                    turnPotionForce.set(false);
                                }
                                bonusattaque.setText("Le bonus d'attaque est de " + bonusNextAttack.get());
                                enemyHP.addAndGet(-playerDamage);
                                playerHealth.setText("Votre santé : " + playerHP.get() + " HP");
                                enemyHealth.setText(adversaireNom + " santé : " + enemyHP.get() + " HP");

                                history.append("Vous avez infligé " + playerDamage + " PV avec " + attaqueSpeciale + ".\n");
                                historyLabel.setText(history.toString());

                                perso.CompteurAttack(0);
                                updateToursRestants(perso, toursRestantsLabel);

                                enemyTurn(gui, adversaireNom, playerHealth, enemyHealth, combatWindow,
                                        playerHP, enemyHP, historyLabel, history, tourCounter, tourLabel,
                                        actionsPanel, showNormalAttacks[0], showSpecialAttacks[0], objectButton[0]);

                            } else {
                                int toursRestants = perso.getRestrictionAttackSpecial() - perso.getCompteurAttack();
                                history.append("⏳ Il reste " + toursRestants + " tour" + (toursRestants > 1 ? "s" : "") + " avant l'attaque spéciale.\n");
                                historyLabel.setText(history.toString());
                                updateToursRestants(perso, toursRestantsLabel);
                            }
                        });

                        Button backButton = new Button("Retour", () -> {
                            showMainActions(actionsPanel, showNormalAttacks[0], showSpecialAttacks[0], objectButton[0]);
                        });

                        actionsPanel.addComponent(attackButton);
                        actionsPanel.addComponent(backButton);
                    });

// Objet
                    objectButton[0] = new Button("Objet", () -> {
                        actionsPanel.removeAllComponents();
                        bonusattaque.setText("Le bonus d'attaque est de " + bonusNextAttack.get());
                        Panel backpackPanel = createBackpackPanel(gui, actionsPanel, playerHP, enemyHP,
                                playerHealth, enemyHealth, adversaireNom, perso,
                                historyLabel, history, tourCounter, tourLabel, combatWindow,
                                showNormalAttacks[0], showSpecialAttacks[0], objectButton[0], bonusNextAttack, turnPotionForce, bonusattaque);

                        actionsPanel.addComponent(backpackPanel);

                        Button backButton = new Button("Retour", () -> {
                            showMainActions(actionsPanel, showNormalAttacks[0], showSpecialAttacks[0], objectButton[0]);
                        });

                        actionsPanel.addComponent(backButton);
                    });


// --- On ajoute les 3 boutons AVEC ESPACES ---
                    actionsPanel.addComponent(showNormalAttacks[0]);
                    actionsPanel.addComponent(new EmptySpace(new TerminalSize(1, 1)));

                    actionsPanel.addComponent(showSpecialAttacks[0]);
                    actionsPanel.addComponent(new EmptySpace(new TerminalSize(1, 1)));

                    actionsPanel.addComponent(objectButton[0]);

// On ajoute le panel d’actions au panel des stats (déjà fait dans ton code d’origine)
                    statsPanel.addComponent(actionsPanel);


                } else {
                    playerInfoPanel.addComponent(new Label("Aucune donnée de personnage disponible."));
                }
            } else {
                playerInfoPanel.addComponent(new Label("Réponse invalide du serveur."));
            }

        } catch (Exception e) {
            playerInfoPanel.addComponent(new Label("Erreur de communication : " + e.getMessage()));
        }

        Button fleeButton = new Button("Fuir", () -> {
            history.append("\nVous avez fui le combat.\n");
            historyLabel.setText(history.toString());

            MessageDialog.showMessageDialog(gui, "Fuite", "Vous avez choisi de fuir le combat.");
            combatWindow.close();
            afficherMenuPrincipal(gui);
        });

        statsPanel.addComponent(fleeButton);

        combatPanel.addComponent(historyPanel);
        combatPanel.addComponent(statsPanel);
        combatWindow.setComponent(combatPanel);
        gui.addWindowAndWait(combatWindow);
    }

    private static void showMainActions(Panel actionsPanel, Button normalAttack, Button specialAttack, Button objectButton) {
        actionsPanel.removeAllComponents();
        actionsPanel.addComponent(normalAttack);
        actionsPanel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        actionsPanel.addComponent(specialAttack);
        actionsPanel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        actionsPanel.addComponent(objectButton);
    }
    private static void enemyTurn(WindowBasedTextGUI gui, String adversaireNom,
                                  Label playerHealth, Label enemyHealth, BasicWindow combatWindow,
                                  AtomicInteger playerHP, AtomicInteger enemyHP,
                                  Label historyLabel, StringBuilder history,
                                  AtomicInteger tourCounter, Label tourLabel,
                                  Panel actionsPanel,
                                  Button showNormalAttacks, Button showSpecialAttacks, Button objectButton) {

        int enemyDamage = 5; // Dégâts infligés par l'ennemi
        playerHP.addAndGet(-enemyDamage);
        playerHealth.setText("Votre santé : " + playerHP.get() + " HP");

        // Ajouter l’attaque ennemie dans l’historique
        history.append(adversaireNom + " a infligé " + enemyDamage + " PV.\n");
        historyLabel.setText(history.toString());

        // Vérification de la fin du combat
        if (playerHP.get() <= 0) {
            history.append("\nVous avez été vaincu par " + adversaireNom + ".\n");
            historyLabel.setText(history.toString());

            MessageDialog.showMessageDialog(gui, "Défaite", "Vous avez été vaincu par " + adversaireNom + " !");
            combatWindow.close();
            afficherMenuPrincipal(gui);
            return;
        } else if (enemyHP.get() <= 0) {
            history.append("\nVous avez vaincu " + adversaireNom + " !\n");
            historyLabel.setText(history.toString());

            MessageDialog.showMessageDialog(gui, "Victoire", "Vous avez vaincu " + adversaireNom + " !");
            combatWindow.close();
            afficherMenuPrincipal(gui);
            return;
        }

        // Passer au tour suivant
        int currentTour = tourCounter.incrementAndGet();
        tourLabel.setText("🕒 Tour : " + currentTour);

        if (currentTour % 5 == 0) {
            // On veut garder le contenu du tour précédent (tour - 1)
            int previousTour = currentTour - 1;
            String marker = "==== TOUR " + previousTour + " ====";

            int lastTourIndex = history.lastIndexOf(marker);
            if (lastTourIndex != -1) {
                // Récupérer à partir de "==== TOUR X ===="
                String lastTourContent = history.substring(lastTourIndex);
                history.setLength(0); // Effacer tout
                history.append(lastTourContent); // Coller contenu du tour précédent
            } else {
                // Sécurité : si jamais on ne trouve pas (ça ne devrait pas arriver)
                history.setLength(0);
            }
        }

        // Ajouter le nouveau tour (que ce soit un reset ou pas)
        history.append("\n==== TOUR " + currentTour + " ====\n");

        historyLabel.setText(history.toString());

        // Réafficher les actions du joueur
        showMainActions(actionsPanel, showNormalAttacks, showSpecialAttacks, objectButton);
    }



    private static Panel createBackpackPanel(WindowBasedTextGUI gui, Panel actionsPanel, AtomicInteger playerHP, AtomicInteger enemyHP,
                                             Label playerHealth, Label enemyHealth, String adversaireNom, Personnage perso,
                                             Label historyLabel, StringBuilder history, AtomicInteger tourCounter, Label tourLabel,
                                             BasicWindow combatWindow,
                                             Button showNormalAttacks, Button showSpecialAttacks, Button objectButton,
                                             AtomicInteger bonusNextAttack, AtomicBoolean turnPotionForce, Label bonusattaque)
    {
        Panel backpackPanel = new Panel(new GridLayout(1));
        String username = Session.getUsername();
        try {
            String jsonbackpack = HttpService.getBackpack(username, Session.getToken());

            JsonObject response = JsonParser.parseString(jsonbackpack).getAsJsonObject();
            JsonArray dataArray = response.getAsJsonArray("data");

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                    .create();

            ObjectBase[] objets = gson.fromJson(dataArray, ObjectBase[].class);

            if (objets.length == 0) {
                backpackPanel.addComponent(new Label("Votre BackPack est vide."));
            } else {
                for (ObjectBase objlist : objets) {
                    String objectId = objlist.getId();

                    Button objButton = new Button(objlist.getName() + " (" + objlist.getType() + ")", () -> {
                        switch (objlist.getType()) {
                            case "Weapon":
                                Weapon weapon = (Weapon) objlist;

                                // Utilisation de l'arme
                                String weaponUseMessage = weapon.use();

                                // Calcul des dégâts
                                int weaponDamage = weapon.getDamage();
                                if (turnPotionForce.get()){
                                    weaponDamage += bonusNextAttack.get();
                                    bonusNextAttack.set(0);
                                    turnPotionForce.set(false);
                                    bonusattaque.setText("Le bonus d'attaque est de " + bonusNextAttack.get());
                                }
                                enemyHP.addAndGet(-weaponDamage);
                                history.append("Vous avez utilisé " + weapon.getName() + " et infligé " + weaponDamage + " PV à l'ennemi.\n");

                                // 🔥 MAJ MongoDB (fiabilité)
                                try {
                                    String responseupdateobject = HttpService.updateObjectReliability(
                                            username,
                                            objectId,
                                            weapon.getReliability(),
                                            Session.getToken()
                                    );
                                    System.out.println("MAJ fiabilité arme : " + responseupdateobject);

                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    history.append("⚠️ Erreur de synchro fiabilité.\n");
                                }

                                // Vérification si l'arme est cassée et affichage du message après l'attaque
                                if (weapon.getReliability()==0) {
                                    history.append("Malheureusement " + weapon.getName() + " s'est brisée.\n");
                                }

                                // Vider le panel du backpack
                                backpackPanel.removeAllComponents();

                                // L'ennemi joue ensuite
                                enemyTurn(gui, adversaireNom, playerHealth, enemyHealth, combatWindow,
                                        playerHP, enemyHP, historyLabel, history, tourCounter, tourLabel,
                                        actionsPanel, showNormalAttacks, showSpecialAttacks, objectButton);

                                break;

                            case "HealingPotion":
                                HealingPotion potion = (HealingPotion) objlist;
                                int healAmount = potion.getHeal();
                                if (turnPotionForce.get()){
                                    bonusNextAttack.set(0);
                                    turnPotionForce.set(false);
                                }
                                playerHP.addAndGet(healAmount);
                                history.append("Vous avez utilisé " + potion.getName() + " et récupéré " + healAmount + " PV.\n");

                                // 🔥 Supprimer la potion de la base de données (Backpack MongoDB)
                                try {
                                    String responseDelete = HttpService.deleteObjectFromBackpack(username, objectId, Session.getToken());
                                    System.out.println("Suppression potion : " + responseDelete);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    history.append("⚠️ Erreur lors de la suppression de la potion.\n");
                                }

                                // Vider et recharger l’affichage du backpack après suppression
                                backpackPanel.removeAllComponents();
                                Panel refreshedBackpack = createBackpackPanel(gui, actionsPanel, playerHP, enemyHP,
                                        playerHealth, enemyHealth, adversaireNom, perso,
                                        historyLabel, history, tourCounter, tourLabel,
                                        combatWindow,
                                        showNormalAttacks, showSpecialAttacks, objectButton, bonusNextAttack, turnPotionForce, bonusattaque);

                                actionsPanel.removeAllComponents();
                                actionsPanel.addComponent(refreshedBackpack);

                                // L'ennemi joue ensuite
                                enemyTurn(gui, adversaireNom, playerHealth, enemyHealth, combatWindow,
                                        playerHP, enemyHP, historyLabel, history, tourCounter, tourLabel,
                                        actionsPanel, showNormalAttacks, showSpecialAttacks, objectButton);
                                break;

                            case "PotionOfStrenght":
                                if (turnPotionForce.get()){
                                    bonusNextAttack.set(0);
                                    turnPotionForce.set(false);
                                }
                                PotionOfStrenght potionForce = (PotionOfStrenght) objlist;
                                int bonusAttack = potionForce.getBonusATK();
                                bonusNextAttack.set(bonusAttack);
                                bonusattaque.setText("Le bonus d'attaque est de " + bonusNextAttack.get());
                                turnPotionForce.set(true);
                                history.append("Vous avez utilisé " + potionForce.getName() + " et gagnerez +" + bonusAttack + " dégâts à votre prochaine attaque.\n");

                                // 🔥 Supprimer la potion de la base de données (Backpack MongoDB)
                                try {
                                    String responseDelete = HttpService.deleteObjectFromBackpack(username, objectId, Session.getToken());
                                    System.out.println("Suppression potion de force : " + responseDelete);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    history.append("⚠️ Erreur lors de la suppression de la potion.\n");
                                }

                                // Vider et recharger l’affichage du backpack après suppression
                                backpackPanel.removeAllComponents();
                                refreshedBackpack = createBackpackPanel(gui, actionsPanel, playerHP, enemyHP,
                                        playerHealth, enemyHealth, adversaireNom, perso,
                                        historyLabel, history, tourCounter, tourLabel,
                                        combatWindow, showNormalAttacks, showSpecialAttacks, objectButton,
                                        bonusNextAttack, turnPotionForce, bonusattaque);

                                actionsPanel.removeAllComponents();
                                actionsPanel.addComponent(refreshedBackpack);

                                // L'ennemi joue ensuite
                                enemyTurn(gui, adversaireNom, playerHealth, enemyHealth, combatWindow,
                                        playerHP, enemyHP, historyLabel, history, tourCounter, tourLabel,
                                        actionsPanel, showNormalAttacks, showSpecialAttacks, objectButton);
                                break;



                            default:
                                history.append("Objet inconnu : " + objlist.getName() + ".\n");
                                break;
                        }
                        historyLabel.setText(history.toString());
                        showMainActions(actionsPanel, showNormalAttacks, showSpecialAttacks, objectButton);
                    });

                    backpackPanel.addComponent(objButton);
                    backpackPanel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
                }
            }

        } catch (Exception e) {
            MessageDialog.showMessageDialog(gui, "Erreur", "Impossible de récupérer le BackPack : " + e.getMessage());
        }

        return backpackPanel;
    }
        /**
         * Afficher l'ecran de comabt et mettre a jour dynamiquement l'affichage des deux cotés
         * Pv joueur, historique, actions possibles, bouton quitter,detection fin de combat,passage de tour
         * */
        public static void lancerCombat (WindowBasedTextGUI gui, StateCombat state){
            AtomicBoolean shouldRun = new AtomicBoolean(true);//Le thread l'utilse pour savoir quand stopé
            boolean[] forfaitEffectue = {false}; //boolean pour savoir si le user a quitter le comabt
            int[] lasttour = {state.getTour()}; //Evite les relancements inutiles

            String adversaire = state.getOpponent(Session.getUsername());


            BasicWindow combatWindow = new BasicWindow("Combat contre " + adversaire);
            combatWindow.setHints(List.of(Window.Hint.CENTERED));

            Panel mainPanel = new Panel(new GridLayout(1));
            Label tourLabel = new Label("Tour : " + state.getTour());
            Label labelPvAdversaire = new Label("PV adversaire : " + state.getPv(adversaire));
            Label labelMesPv = new Label("Vos PV : " + state.getPv(Session.getUsername()));
            Panel historyPanel = new Panel(new GridLayout(1));
            Panel actionPanel = new Panel(new GridLayout(1));

            historyPanel.addComponent(new Label("Historique :"));
            for (String entry : state.getLog()) {
                historyPanel.addComponent(new Label(entry));
            }
            //Va appeller les labels plus haut
            mainPanel.addComponent(tourLabel);
            mainPanel.addComponent(labelPvAdversaire);
            mainPanel.addComponent(labelMesPv);
            mainPanel.addComponent(historyPanel);
            mainPanel.addComponent(actionPanel);

            mainPanel.addComponent(new Button("Quitter le combat", () -> {
                try {
                    HttpService.forfait(Session.getUsername(), Session.getToken());
                    forfaitEffectue[0] = true;
                } catch (Exception e) {
                    System.out.println("Erreur forfait : " + e.getMessage());
                }
                shouldRun.set(false);
                combatWindow.close();
                LanternaApp.afficherMenuPrincipal(gui);
            }));

            //Ajout bouton action, le joueur qui doit jouer les voit, l'autre non
            if (Session.getUsername().equals(state.getPlayerNow())) {
                updateActionPanel(actionPanel, state, gui); //Va appeller la méthode
            } else {
                actionPanel.addComponent(new Label("En attente du tour de l'adversaire..."));
            }

            combatWindow.setComponent(mainPanel);
            gui.addWindow(combatWindow);

            // Thread pour mise à jour sans tout refermer, afficher l'etat du comabt toute les 2 sec (tourne en arriere plan)
            //Récupe le StateComabt a jour ( de JSON via GSON)
            //Dans le cas de lan le Thread est utilse pour la mise a jour automatique de l'affichage, sans le Thread le joueur aurait du appuyer manuellement sur un bouton
            new Thread(() -> {
                while (shouldRun.get()) {
                    try {
                        Thread.sleep(2000);
                        String json = HttpService.getCombatState(Session.getUsername(), Session.getToken());
                        Gson gson = new GsonBuilder()
                                .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                                .create();
                        StateCombat updated = gson.fromJson(json, StateCombat.class);
                        if (updated == null) {
                            //invokeLater permet de revenir au thread principale de lanterna pour mettre a jour l interface
                            gui.getGUIThread().invokeLater(() -> {
                                String winner = null;
                                try {
                                    winner = HttpService.getLastWinner(Session.getUsername(), Session.getToken());
                                } catch (Exception e) {
                                    System.out.println("Erreur récup du gagnant : " + e.getMessage());
                                }
                                String message;
                                if (forfaitEffectue[0]) {
                                    message = "Vous avez quitté le comabt, votre adversaire a gagné";
                                } else if (winner != null) {
                                    if (winner.equals(Session.getUsername())) {
                                        message = "Comabt terminé, vous avez gagné";

                                    } else {
                                        message = "Combat terminé, " + winner + "a gagné";
                                    }
                                } else {
                                    message = "Comant terminé, mais le gagnat est inconnu";
                                }

                                MessageDialog.showMessageDialog(gui, "Fin du comabt", message);
                                combatWindow.close();
                                afficherMenuPrincipal(gui);
                            });
                            break;
                        }

                        if (updated.isFinished()) {
                            gui.getGUIThread().invokeLater(() -> {

                                //Mettre a jour l affich des pv a 0 avant fermeture de la fenetre
                                tourLabel.setText("Tour : " + updated.getTour());
                                labelPvAdversaire.setText("PV adversaire : " + updated.getPv(adversaire));
                                labelMesPv.setText("Vos PV : " + updated.getPv(Session.getUsername()));


                                String winner = updated.getWinner();
                                String loser = winner != null && winner.equals(Session.getUsername())
                                    ?updated.getOpponent(Session.getUsername())
                                    :Session.getUsername();
                                try {
                                    winner = HttpService.getLastWinner(Session.getUsername(), Session.getToken());
                                } catch (Exception e) {
                                    System.out.println("Erreur récup du gagnant : " + e.getMessage());
                                }
                                if (winner != null && winner.equals(loser)) {
                                    MessageDialog.showMessageDialog(gui, "Défaite", "Vous êtes mort... Vos PV sont tombés à 0.");
                                }

                                String message;
                                if (winner == null) {
                                    message = "Combat terminé, mais le gagnant est inconnu.";
                                } else if ("Egalité".equals(winner)) {
                                    message = "Combat terminé sur une égalité !";
                                } else if (winner.equals(Session.getUsername())) {
                                    message = "Combat terminé, vous avez gagné !";
                                } else {
                                    message = "Combat terminé, " + winner + " a gagné.";
                                }
                                combatWindow.close();
                                MessageDialog.showMessageDialog(gui, "Fin du comabt", message);
                                afficherMenuPrincipal(gui);
                            });
                            break;
                        }

                        //Ici a chaque invokeLater il y aura une mise a jour visuelle du tour, les pvs et historique
                        gui.getGUIThread().invokeLater(() -> {
                            tourLabel.setText("Tour : " + updated.getTour());
                            labelPvAdversaire.setText("PV adversaire : " + updated.getPv(adversaire));
                            labelMesPv.setText("Vos PV : " + updated.getPv(Session.getUsername()));

                            historyPanel.removeAllComponents();
                            historyPanel.addComponent(new Label("Historique :"));
                            for (String log : updated.getLog()) {
                                historyPanel.addComponent(new Label(log));
                            }

                            // relancer l’interface avec les boutons en focntion des tours, si c le meme tour alors les boutons sont affichés pour le bon user
                            //Si le tour a changé alors la on fait un remove soit pour retirer les boutons et afficher en attente..... soint pour retirer cette phrase et afficher les boutons.
                            if (updated.getTour() != lasttour[0]) {
                                lasttour[0] = updated.getTour();
                                actionPanel.removeAllComponents();
                                if (updated.getPlayerNow().equals(Session.getUsername())) {
                                    updateActionPanel(actionPanel, updated, gui); //Va appeller la méthode
                                } else {
                                    actionPanel.addComponent(new Label("En attente du tour de l'adversaire..."));
                                }
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        //Rafraichissemnt auto des bouton essaie
        private static void updateActionPanel(Panel actionPanel, StateCombat state, WindowBasedTextGUI gui){

            actionPanel.addComponent(new Label("Vos actions :"));
            actionPanel.addComponent(new Button("Attaque normale ", () -> {
                try {
                    HttpService.combatAttack(Session.getUsername(), "normal", Session.getToken());
                } catch (Exception e) {
                    MessageDialog.showMessageDialog(gui, "Erreur", e.getMessage());
                }
            }));
            actionPanel.addComponent(new Button("Attaque spéciale ", () -> {
                try {
                    HttpService.combatAttack(Session.getUsername(), "special", Session.getToken());
                } catch (Exception e) {
                    MessageDialog.showMessageDialog(gui, "Erreur", e.getMessage());
                }
            }));
            for (ObjectBase obj : state.getBackpack(Session.getUsername())) {
                actionPanel.addComponent(new Button("Utiliser objet : " + obj.getName(), () -> {
                    try {
                        HttpService.combatUseObject(Session.getUsername(), obj.getId(), Session.getToken());

                        //SI ouverture du coffre faut charger le new etat du combat
                        if(obj instanceof CoffreDesJoyaux){
                            actionPanel.addComponent(new Button("Ouvrir le coffre", () -> {
                                displayChest(gui, (CoffreDesJoyaux) obj);
                            }));
                        }
                    } catch (Exception e) {
                        MessageDialog.showMessageDialog(gui, "Erreur", e.getMessage());
                    }
                }));
            }
        }

        /**
         * Méthode pour un affiche propre lors du combat si coffre utilisé
         **/
         public static void displayChest(WindowBasedTextGUI gui, CoffreDesJoyaux coffre){
             BasicWindow coffreWindow = new BasicWindow("Coffre des Joyaux");

             Panel panel = new Panel(new GridLayout(1));
             panel.addComponent(new Label("Objets contenus dans le coffre :"));

             panel.removeAllComponents();

             //Regarde si new insatnce ou memes objets mais non dupliqués
             for (ObjectBase obj : coffre.getContenu()){
                 panel.addComponent(new Label("-" + obj.getName()));
             }

             panel.addComponent(new Button("Fermer", coffreWindow::close));

             coffreWindow.setComponent(panel);
             //attendre jusqué fermeture
             gui.addWindowAndWait(coffreWindow);
         }

        private static void DisplayClassement (WindowBasedTextGUI gui){
            BasicWindow profileWindow = new BasicWindow("Classement");
            profileWindow.setHints(Arrays.asList(Hint.CENTERED));
            Panel panel = new Panel(new GridLayout(1));
            panel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.BEGINNING));


            try {
                String userJson = HttpService.getClassementPlayer(Session.getToken());
                if (userJson == null) {
                    panel.addComponent(new Label("Erreur : impossible de récupérer le classement"));
                } else {
                    Type list = new TypeToken<List<UserInfo>>() {
                    }.getType();
                    List<UserInfo> classement = new Gson().fromJson(userJson, list);
                    if (classement.isEmpty()) {
                        panel.addComponent(new Label("classement vide"));
                    } else {
                        List<UserInfo> top3 = classement.size() > 3 ? classement.subList(0, 3) : classement;
                        int pos = 1;
                        for (UserInfo user : top3) {
                            String resul = user.getUsername() + " Victoire : " + user.getGagner();
                            panel.addComponent(new Label(resul));
                            pos++;
                            panel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Impossible de  charger le classement: " + e.getMessage());
            }
            panel.addComponent(new Button("Retour", profileWindow::close));
            profileWindow.setComponent(panel);
            gui.addWindowAndWait(profileWindow);
        }


        /**
         * @param actuel = value actuel atteinte
         * @param objectif = value cible a atteindre
         **/
        //Generer bar pour trophé premier test
        public static String generateBar(int actuel,int objectif){
            //longeur bar
            int total = 20; //20 caract
            //calcul la proportion atteinte et la transfomer en nombre de "blocs" a remplir
            int filling  = (int) ((double) actuel / objectif * total);
            //Pas dépasser la bar
            filling = Math.min(filling, total);
            String barre =  "[" + "█".repeat(filling) + " ".repeat(total - filling) + "] ";
            return barre + actuel + "/" + objectif; //Jsute texte de progression
        }

        /**
         * @param gui
         * Afficher les trophées en faisant appele au service fetchUserInfo ()(direct la reponse objet userJson pas besoin de la parser)
         *
         * */
        public static void afficherTrophees(WindowBasedTextGUI gui) {
        BasicWindow window = new BasicWindow("Vos Trophées");
        window.setHints(List.of(Window.Hint.CENTERED)); //Centrés
        Panel panel = new Panel(new GridLayout(1));

        panel.addComponent(new Label("Progression des trophées :"));

        try {
            UserInfo user = HttpService.fetchUserInfo(Session.getUsername(), Session.getToken());

            //nbr de tour
            int nombreTours = user.getDernierCombatTours();
            int win = user.getGagner();
            int winConcecutive = user.getWinconsecutive();
            int cristaux = user.getCristaux();


            //   BRONZE
            Button bronzeBtn = new Button("Bronze : " + generateBar(win, 1), () -> {
                Panel details = new Panel(new GridLayout(1));
                details.addComponent(new Label("Conditions du trophée Bronze :"));
                details.addComponent(new Label("Gagner 1 combat : " + generateBar(win, 1)));
                details.addComponent(new Label("Combat en ≤ 15 tours : " + generateBar(nombreTours <= 15 ? 1 : 0, 1)));
                MessageDialog.showMessageDialog(gui, "Trophée Bronze", detailsToString(details));
            });
            panel.addComponent(bronzeBtn);

            Button SilverBtn = new Button("Silver :" +generateBar(winConcecutive,1), () -> {
                Panel details = new Panel(new GridLayout(1));
                details.addComponent(new Label("Conditions du trophée Silver :"));
                details.addComponent(new Label("5 victoires consécutives : " + generateBar(winConcecutive, 5)));
                details.addComponent(new Label("Gagnez 200 cristaux : " + generateBar(cristaux,200)));
                details.addComponent(new Label("Combat en ≤ 10 tours : " + generateBar(nombreTours <= 10 ? 1 : 0, 1)));
                MessageDialog.showMessageDialog(gui, "Trophée Silver", detailsToString(details));
            });
            panel.addComponent(SilverBtn);

            Button OrBtn = new Button("Or :" +generateBar(winConcecutive,1), () -> {
                Panel details = new Panel(new GridLayout(1));
                details.addComponent(new Label("Conditions du trophée Or :"));
                details.addComponent(new Label("10 victoires consécutives : " + generateBar(winConcecutive, 10)));
                details.addComponent(new Label("Gagnez 500 cristaux : " + generateBar(cristaux,500)));
                details.addComponent(new Label("Combat en ≤ 6 tours : " + generateBar(nombreTours <= 6 ? 1 : 0, 1)));
                details.addComponent(new Label("Avoir utilisé un bazooka : " + generateBar(user.getUtilisationBazooka() > 0 ? 1 : 0, 1)));
                MessageDialog.showMessageDialog(gui, "Trophée Silver", detailsToString(details));
            });
            panel.addComponent(OrBtn);

        } catch (Exception e) {
            panel.addComponent(new Label("Erreur lors du chargement des trophées : " + e.getMessage()));
        }

        panel.addComponent(new EmptySpace());
        panel.addComponent(new Button("Retour", window::close));

        window.setComponent(panel);
        gui.addWindowAndWait(window);
    }

    /**
     * @param panel
     *juste a servir a convertir le contenu visuel  label en String
     * **/
    private static String detailsToString(Panel panel) {
        StringBuilder sb = new StringBuilder();
        //Recup les boutons,labels,...
        for (Component comp : panel.getChildren()) {
            //Verif que Component ont un label
            if (comp instanceof Label label) {
                sb.append(label.getText()).append("\n");//Recup le text et ajoute le text recup a la cahine finale avec retour ligne
            }
        }
        return sb.toString();
    }
}

