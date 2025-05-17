package be.helha.labos.crystalclash.LanternaApp;


import be.helha.labos.crystalclash.Characters.Personnage;
import be.helha.labos.crystalclash.DTO.StateCombat;
import be.helha.labos.crystalclash.DTO.Trophee;
import be.helha.labos.crystalclash.DeserialiseurCustom.ObjectBasePolymorphicDeserializer;
import be.helha.labos.crystalclash.Factory.CharactersFactory;
import be.helha.labos.crystalclash.HttpClient.*;
import be.helha.labos.crystalclash.DTO.Inventory;
import be.helha.labos.crystalclash.Object.*;
import be.helha.labos.crystalclash.Service.TropheeService;
import be.helha.labos.crystalclash.HttpClient.*;
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
import org.bson.Document;
import org.hibernate.boot.model.internal.ListBinder;

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
                String json = Login_Register_userHttpClient.login(usernameBox.getText(), passwordBox.getText());
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
                        String userJson = Login_Register_userHttpClient.getUserInfo(Session.getUsername(), Session.getToken());
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
                String json = Login_Register_userHttpClient.register(usernameBox.getText(), passwordBox.getText());
                if (json.contains("Inscription réussie")) {
                    try {
                        String loginReponse = Login_Register_userHttpClient.login(usernameBox.getText(), passwordBox.getText());
                        //Parser le loginReposne pour le manipuler en java
                        // JsonParser.parseString(loginReponse) = convertit la chaine json en 1 objet Json
                        JsonObject response = JsonParser.parseString(loginReponse).getAsJsonObject(); //OK c bien un object json

                        if (response.has("token") && !response.get("token").isJsonNull()) {
                            String token = response.get("token").getAsString();
                            Session.setToken(token);
                            Session.setUsername(usernameBox.getText());

                            //Recup des infos uti
                            String UserJson = Login_Register_userHttpClient.getUserInfo(Session.getUsername(), token);
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


        mainPanel.addComponent(createSectionLabel("Règles du jeu"));
        mainPanel.addComponent(new Button("Voir les règles du jeu", ()->showRules(gui)));

        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        // Section Profil
        mainPanel.addComponent(createSectionLabel("Profil"));
        mainPanel.addComponent(new Button("Voir profil", () -> afficherMonProfil(gui)));
        mainPanel.addComponent(new Button("Voir BackPack", () -> afficherBackPack(gui, () -> {
            afficherBackPack(gui, () -> {
            }); // on relance une fois pour rafraîchir le contenu
        })));
        mainPanel.addComponent(new Button("Voir Equipement", () -> afficherEquipement(gui, () -> {
            afficherEquipement(gui, () -> {
            });
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

        mainPanel.addComponent(new Button("Mes trophées", () -> Displaytrophy(gui)));

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
        mainPanel.addComponent(new Button("Lancer un combat", () -> openCombatWindow(gui)));
        mainPanel.addComponent(new Button("Changer de personnage", () -> afficherChoixPersonnage(gui)));

        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));


        mainPanel.addComponent(new Button("Se déconnecter", () -> {
            try {
                Connected_LogoutHttpClient.logout(Session.getUsername(), Session.getToken());
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
            List<UserInfo> connectedUsers = Connected_LogoutHttpClient.getConnectedUsers();

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
                CharacterHttpClient.selectCharacter(Session.getUsername(), personnage, Session.getToken());
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
    /**
     * Affiche le personnage
     * @param gui
     */
    private static void afficherPersonnage(WindowBasedTextGUI gui) {
        BasicWindow persoWindow = new BasicWindow("Mon Personnage");
        persoWindow.setHints(Arrays.asList(Hint.CENTERED));

        Panel panel = new Panel(new GridLayout(1));
        panel.addComponent(new Label("Personnage de " + Session.getUsername()));

        try {
            // Récupérer les informations du personnage
            String json = CharacterHttpClient.getCharacter(Session.getUsername(), Session.getToken());
            JsonElement element = JsonParser.parseString(json);

            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();

                if (obj.has("data") && !obj.get("data").isJsonNull()) {
                    // Afficher le type de personnage
                    panel.addComponent(new Label("Type : " + obj.get("data").getAsString()));
                    Personnage perso = CharactersFactory.getCharacterByType(obj.get("data").getAsString());

                    // Afficher les PV de base
                    String pvText = "PV : " + perso.getPV();

                    // Récupérer l'équipement et calculer le bonus de PV si l'armure est présente
                    String equipmentJson = CharacterHttpClient.getEquipment(Session.getUsername(), Session.getToken());
                    JsonObject equipmentResponse = JsonParser.parseString(equipmentJson).getAsJsonObject();
                    JsonArray dataArray = equipmentResponse.getAsJsonArray("data");

                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                            .create();
                    ObjectBase[] objets = gson.fromJson(dataArray, ObjectBase[].class);

                    // Calcul du bonus de PV (si l'armure existe)
                    int bonusPV = 0;
                    for (ObjectBase objEquip : objets) {
                        if (objEquip instanceof Armor) {
                            Armor armor = (Armor) objEquip;
                            bonusPV += armor.getBonusPV();
                        }
                    }

                    // Créer un label qui contient les PV de base et le bonus à côté
                    String bonusText = "";
                    if (bonusPV > 0) {
                        bonusText = " (+" + bonusPV + ")";
                    }

                    // Créer un label avec les PV de base et le bonus
                    String fullText = pvText + bonusText;
                    Label pvLabel = new Label(fullText);

                    // Mettre la couleur verte pour le bonus uniquement
                    if (bonusPV > 0) {
                        // On utilise TextColor.ANSI.DEFAULT pour les PV de base (pas de couleur)
                        // et TextColor.ANSI.GREEN pour le bonus
                        String formattedText = pvText + " " + bonusText;
                        pvLabel = new Label(formattedText);
                        pvLabel.setForegroundColor(TextColor.ANSI.GREEN); // Couleur par défaut pour les PV de base
                        pvLabel.addStyle(SGR.BOLD); // Appliquer le style gras à tout le label
                    }

                    // Afficher le label avec les PV et bonus dans le panel
                    panel.addComponent(pvLabel);

                    // Afficher les autres informations du personnage
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
            String json = InventoryHttpCLient.getInventory(username, Session.getToken());

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
                String reponse = InventoryHttpCLient.sellObjetc(obj.getName(), obj.getType(), Session.getToken());
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
                String result = CharacterHttpClient.putInBackpack(Session.getUsername(), obj.getName(), obj.getType(), Session.getToken());
                JsonObject resultJson = JsonParser.parseString(result).getAsJsonObject();
                String message = resultJson.get("message").getAsString();
                MessageDialog.showMessageDialog(gui, "BackPack", message);
                window.close();
                refreshInventory.run();
            } catch (Exception e) {
                MessageDialog.showMessageDialog(gui, "Erreur", "Impossible de mettre dans le BackPack : " + e.getMessage());
            }
        }));

        // Vérification si l'objet est une armure avant d'ajouter le bouton "S'équiper"
        if (obj.getType().equals("Armor")) {
            panel.addComponent(new Button("S'équiper", () -> {
                try {
                    String result = CharacterHttpClient.putInEquipment(Session.getUsername(), obj.getName(), obj.getType(), Session.getToken());
                    JsonObject resultJson = JsonParser.parseString(result).getAsJsonObject();
                    String message = resultJson.get("message").getAsString();
                    MessageDialog.showMessageDialog(gui, "Equipement", message);
                    window.close();
                    refreshInventory.run();
                } catch (Exception e) {
                    MessageDialog.showMessageDialog(gui, "Erreur", "Impossible de s'équiper de l'objet : " + e.getMessage());
                }
            }));
        }
        if (hasCoffre && !(obj instanceof CoffreDesJoyaux)) {
            panel.addComponent(new Button("Mettre dans le coffre", () -> {
                try {
                    String result = InventoryHttpCLient.putInCoffre(Session.getUsername(), obj.getName(), obj.getType(), Session.getToken());
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
            String userJson = Login_Register_userHttpClient.getUserInfo(Session.getUsername(), Session.getToken());
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

     Affiche la boutique*
     @param gui*/
    private static void DisplayShop(WindowBasedTextGUI gui) {
        BasicWindow shopWindow = new BasicWindow("Boutique");
        shopWindow.setHints(Arrays.asList(Hint.CENTERED));

        int level = Session.getUserInfo().getLevel();

        Panel panel = new Panel(new GridLayout(1));
        //pour mettre a jour les cristaux
        try {
            String userJson = Login_Register_userHttpClient.getUserInfo(Session.getUsername(), Session.getToken());
            UserInfo updatedInfo = new Gson().fromJson(userJson, UserInfo.class);
            Session.setUserInfo(updatedInfo);
        } catch (Exception e) {
            System.out.println("Impossible de rafraîchir les infos du joueur : " + e.getMessage());
        }
        UserInfo info = Session.getUserInfo();
        panel.addComponent(new Label("Cristaux : " + info.getCristaux()));
        panel.addComponent(new EmptySpace());

        try {
            String json = ShopHttpClient.getShops(Session.getToken());
            List<Map<String, Object>> shopItems = new Gson().fromJson(json, List.class);

            //Groupe juste les object par type
            Map<String,List<Map<String,Object>>> itemByType = new HashMap<>();
            for (Map<String, Object> item : shopItems) {
                String type = (String) item.get("type");
                itemByType.putIfAbsent(type, new ArrayList<>());
                itemByType.get(type).add(item);
            }

            //Affiche de chaque groupe
            for (String type : itemByType.keySet()) {
                panel.addComponent(new Separator(Direction.HORIZONTAL));
                panel.addComponent(new Label("Type : " + type).addStyle(SGR.BOLD));
                panel.addComponent(new EmptySpace());

                Panel column = new Panel(new LinearLayout(Direction.VERTICAL));

                for (Map<String,Object> item : itemByType.get(type)) {
                    String name = (String) item.get("name");
                    double price = (double) item.get("price");
                    double requiredLevel = (double) item.get("requiredLevel");


                    if (level < requiredLevel) continue;

                    String label = name + ": " + price + "cristaux, niveau" + requiredLevel ;
                    column.addComponent(new Button(label, () -> {
                        try{
                            String Resultjson = ShopHttpClient.buyItem(name,type,Session.getToken());
                            JsonObject js = JsonParser.parseString(Resultjson).getAsJsonObject();
                            String message = js.get("message").getAsString();
                            MessageDialog.showMessageDialog(gui, "Achat", message);

                            String userJson = Login_Register_userHttpClient.getUserInfo(Session.getUsername(),Session.getToken());
                            Session.setUserInfo(new Gson().fromJson(userJson, UserInfo.class));

                            //mis a jour cristaux apres achat
                            shopWindow.close();
                            DisplayShop(gui);
                        } catch (Exception e) {
                            MessageDialog.showMessageDialog(gui, "Erreur", "Impossible d'acheter : " + e.getMessage());
                        }
                    }));
                }
                panel.addComponent(column);
            }

        } catch (Exception e) {
            panel.addComponent(new Label("Erreur lors du chargement de la boutique : " + e.getMessage()));
        }

        panel.addComponent(new EmptySpace());
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
            String json = CharacterHttpClient.getBackpack(username, Session.getToken());

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
                                String reponse = CharacterHttpClient.removeFromBackpack(Session.getUsername(), obj.getName(), obj.getType(), Session.getToken());
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
                        if (hasCoffre && !(obj instanceof CoffreDesJoyaux)) {
                            detailsPanel.addComponent(new Button("Mettre dans le coffre", () -> {
                                try {
                                    String result = CharacterHttpClient.putInCoffreBackPack(Session.getUsername(), obj.getName(), obj.getType(), Session.getToken());
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
    /**
     * Affiche l'équipement du joueur
     *
     * @param gui => pour afficher les messages
     */
    private static void afficherEquipement(WindowBasedTextGUI gui, Runnable refreshEquipement) {
        BasicWindow window = new BasicWindow("Mon Equipement");
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
            String json = CharacterHttpClient.getEquipment(username, Session.getToken());

            // Parse le JSON pour extraire "data"
            JsonObject response = JsonParser.parseString(json).getAsJsonObject();
            JsonArray dataArray = response.getAsJsonArray("data");

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                    .create();

            ObjectBase[] objets = gson.fromJson(dataArray, ObjectBase[].class);

            if (objets.length == 0) {
                panel.addComponent(new Label("Votre équipement est vide."));
            } else {
                panel.addComponent(new Label("Contenu de l'équipement :"));
                for (ObjectBase obj : objets) {
                    String label = obj.getName() + " (" + obj.getType() + ")";
                    panel.addComponent(new Button(label, () -> {
                        BasicWindow detailsWindow = new BasicWindow("Détails de l'objet");
                        detailsWindow.setHints(Arrays.asList(Hint.CENTERED));

                        Panel detailsPanel = new Panel(new GridLayout(1));
                        detailsPanel.addComponent(new Label(obj.getDetails()));
                        detailsPanel.addComponent(new EmptySpace());

                        detailsPanel.addComponent(new Button("Retirer de l'équipement", () -> {
                            try {
                                String reponse = CharacterHttpClient.removeFromEquipment(Session.getUsername(), obj.getName(), obj.getType(), Session.getToken());
                                JsonObject result = JsonParser.parseString(reponse).getAsJsonObject();
                                String message = result.get("message").getAsString();
                                MessageDialog.showMessageDialog(gui, "Retrait", message);
                                detailsWindow.close();
                                window.close();
                                refreshEquipement.run();
                            } catch (Exception e) {
                                MessageDialog.showMessageDialog(gui, "Erreur", "Impossible de retirer de l'équipement : " + e.getMessage());
                            }
                        }));

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

    /**
     * jouer à la roulette
     * @param gui
     */
    private static void PLayRoulette(WindowBasedTextGUI gui) {
        try {
            String json = RouletteHttpClient.PlayRoulette(Session.getUsername(), Session.getToken());
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


            /**
             * Mettre a jour le coffre depuis le backpack si il y a un combat en cours
             * SI oui on récup l'etat du coffre pdt le combat (depuis StateCombat)
             **/
            try{
                String jsonCombat = FightHttpCLient.getCombatState(Session.getUsername(), Session.getToken());

                //Création d'un objet Gson avec le désérialiseur custom pour objectBase pour reconstruire correctement les objets du coffre
                Gson gsonCombat = new GsonBuilder()
                        .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                        .create();

                //Déserialise le JSON en objet stateConbat, lui simple il contient toutes les infos du combat en cours dont le coffre
                StateCombat combat = gson.fromJson(jsonCombat, StateCombat.class);

                //Si bien recu le combat et si pas termimé
                if(combat != null && !combat.isFinished())
                {
                    List<ObjectBase> chest = combat.getChest(Session.getUsername()); //Récup object présent dans le coffre
                    if(combat != null ){
                        //Crée un chest temporaire que je remplis avec les objets restant recup depuis le combat
                        CoffreDesJoyaux coffreTemporaire = new CoffreDesJoyaux();
                        coffreTemporaire.setContenu(chest);
                        coffreTemporaire.setCapaciteMax();
                        //ON dit que le coffre est = au coffre temp, c'est comme ça su'on aura le coffre mis a jour
                        coffre = coffreTemporaire;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


            // 1. Vérifie l'inventaire
            String jsonInventaire = InventoryHttpCLient.getInventory(Session.getUsername(), Session.getToken());
            Inventory inventory = gson.fromJson(jsonInventaire, Inventory.class);

            for (ObjectBase obj : inventory.getObjets()) {
                if (obj instanceof CoffreDesJoyaux) {
                    coffre = (CoffreDesJoyaux) obj;
                    break;
                }
            }

            // 2. Si pas trouvé dans l'inventaire, vérifie dans le backpack
            if (coffre == null) {
                String jsonBackpack = CharacterHttpClient.getBackpack(Session.getUsername(), Session.getToken());
                JsonElement root = JsonParser.parseString(jsonBackpack);

                // Vérifie si la racine du JSON est un objet (donc commence par { ... })
                if (root.isJsonObject()) {
                    // Récupère l'objet JSON principal
                    JsonObject obj = root.getAsJsonObject();
                    // Vérifie que cet objet contient une clé "data" ET que cette clé est un tableau JSON
                    if (obj.has("data") && obj.get("data").isJsonArray()) {
                        // Récupère le tableau "data" qui contient les objets du backpack
                        JsonArray dataArray = obj.getAsJsonArray("data");
                        // Convertit ce tableau JSON en tableau d'objets Java de type ObjectBase[]
                        ObjectBase[] objetsBackpack = gson.fromJson(dataArray, ObjectBase[].class);
                        // Parcourt chaque objet du backpack
                        for (ObjectBase objBase : objetsBackpack) {
                            // Vérifie si l'objet est un CoffreDesJoyaux (ton coffre)
                            if (objBase instanceof CoffreDesJoyaux) {
                                // Si oui, on le cast et on le stocke
                                coffre = (CoffreDesJoyaux) objBase;
                                // On indique qu'il vient du backpack
                                depuisBackpack = true;
                                // Et on arrête la boucle car on a trouvé ce qu'on cherchait
                                break;
                            }
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

    /**
     * Affiche les détails d'un objet dans le coffre
     * @param gui
     * @param obj
     * @param depuisBackpack
     * @param refreshCoffre
     */
    public static void detailContenuCoffre(WindowBasedTextGUI gui, ObjectBase obj, Boolean depuisBackpack, Runnable refreshCoffre) {
        BasicWindow detailsWindow = new BasicWindow("Détails de l'objet dans le coffre");
        detailsWindow.setHints(Arrays.asList(Hint.CENTERED));

        Panel panel = new Panel(new GridLayout(1));
        panel.addComponent(new Label(obj.getDetails()));
        panel.addComponent(new EmptySpace());
        if (depuisBackpack) {
            panel.addComponent(new Button("Mettre dans l'inventaire", () -> {
                try {
                    String result = CharacterHttpClient.removeFromBackpack(Session.getUsername(), obj.getName(), obj.getType(), Session.getToken());
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
                    String result = CharacterHttpClient.putInBackpack(Session.getUsername(), obj.getName(), obj.getType(), Session.getToken());
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

    /**
     * Affiche la salle de matchmaking
     * @param gui
     */
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

        Panel usersPanel = new Panel(new GridLayout(1)); //Ajout dynamiquement les autres joueurs dispo
        panel.addComponent(usersPanel);

        panel.addComponent(new Button("Quitter la salle", () -> {
            shouldRun.set(false); // Demande d'arrêt,passe a faut si on clique
            try {
                Map<String, String> data = new HashMap<>();
                data.put("username", Session.getUsername());
                FightHttpCLient.exitMatchmakingRoom(Session.getUsername(), Session.getToken());
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
            FightHttpCLient.enterMatchmakingRoom(Session.getUserInfo(), Session.getToken());
        } catch (Exception e) {
            MessageDialog.showMessageDialog(gui, "Erreur", "Impossible d'entrer dans la salle : " + e.getMessage());
            combatWindow.close();
            return;
        }

        //Thread pour rafraichir la liste
        //Utile coté client pour ne pas crache
        new Thread(() -> {
            //thread secondaire, ici j vais effectuer 2 taches toutes les 2 secondes
            //La premiere est si un combat a été lancé contre le joueur A ou B
            //LE second c est de mettre a jour la liste des users dispo
            while (shouldRun.get()) {
                try {
                    //A partir d'ici c un thread secondaire
                    Thread.sleep(2000); //attend 2 sec

                    // Ajout en haut de la boucle pour détecter si l'utilisateur est défié
                    String json = FightHttpCLient.getCombatState(Session.getUsername(), Session.getToken());
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                            .create();
                    StateCombat state = gson.fromJson(json, StateCombat.class);

                    //Quitte la salle et la fentre combat s'ouvre
                    if (state != null && state.getPlayerNow() != null) {
                        gui.getGUIThread().invokeLater(() -> {
                            System.out.println("===> Passage dans invokeLater : lancement du combat");
                            shouldRun.set(false);
                            combatWindow.close();
                            LanternaApp.StartCombatLan(gui, state);
                        });
                        break;
                    }


                    //Secondes taches mettre a jour
                    //Appelle du endpoint
                    List<UserInfo> opponents = FightHttpCLient.getAvailableOpponents(Session.getUsername(), Session.getToken());

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
                                        //Appelle du backend pour lancer le combat avec le joueur
                                        FightHttpCLient.challengePlayer(Session.getUsername(), opponent.getUsername(), Session.getToken());
                                        MessageDialog.showMessageDialog(gui, "Défi lancé", "Vous avez défié " + opponent.getUsername() + " !");
                                        shouldRun.set(false);

                                        // Relancer un thread qui vérifie le combat côté joueur initiateur
                                        new Thread(() -> {
                                            try {
                                                Thread.sleep(1500); // Laisse au serveur le temps de créer le combat
                                                String json1 = FightHttpCLient.getCombatState(Session.getUsername(), Session.getToken());
                                                Gson gson1 = new GsonBuilder()
                                                        .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                                                        .create();
                                                StateCombat state1 = gson.fromJson(json, StateCombat.class);

                                                if (state != null && state.getPlayerNow() != null) {
                                                    gui.getGUIThread().invokeLater(() -> {
                                                        combatWindow.close();
                                                        LanternaApp.StartCombatLan(gui, state);
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

    /**
     * Met à jour le label des tours restants pour l'attaque spéciale
     * @param perso
     * @param label
     */
    private static void updateToursRestants(Personnage perso, Label label) {
        int toursRestants = perso.getRestrictionAttackSpecial() - perso.getCompteurAttack();
        if (toursRestants <= 0) {
            label.setText("Attaque spéciale disponible !");
        } else {
            label.setText("Il reste " + toursRestants + " tour" + (toursRestants > 1 ? "s" : "") + " avant l’attaque spéciale.");
        }
    }

    /**
     * Ouvre la fenêtre de combat
     *
     * @param gui
     */
    private static void openCombatWindow(WindowBasedTextGUI gui) {
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

        Label combatTitle = new Label(" Combat contre le bot d'entrainement ");
        combatTitle.setForegroundColor(TextColor.ANSI.RED);
        combatTitle.addStyle(SGR.BOLD);
        statsPanel.addComponent(combatTitle);

        Label combatDescription = new Label("Votre adversaire : ");
        combatDescription.setForegroundColor(TextColor.ANSI.YELLOW);
        statsPanel.addComponent(combatDescription);

        statsPanel.addComponent(new Label(""));

        AtomicInteger enemyHP = new AtomicInteger(100);
        int enemyhpmax = 100;
        Label enemyHealth = new Label("La santé de l'ennemi : " + enemyHP.get() + "/" + enemyhpmax + " HP");
        enemyHealth.setForegroundColor(TextColor.ANSI.RED);
        statsPanel.addComponent(enemyHealth);

        statsPanel.addComponent(new Label(""));

        AtomicInteger tourCounter = new AtomicInteger(1);
        Label tourLabel = new Label(" Tour : " + tourCounter.get());
        tourLabel.setForegroundColor(TextColor.ANSI.CYAN);
        tourLabel.addStyle(SGR.BOLD);
        statsPanel.addComponent(tourLabel);

        Panel playerInfoPanel = new Panel(new GridLayout(1));
        playerInfoPanel.addComponent(new Label("Personnage de " + Session.getUsername()));
        statsPanel.addComponent(playerInfoPanel);


        Label toursRestantsLabel = new Label(""); // Label pour l’attaque spéciale
        statsPanel.addComponent(toursRestantsLabel);


        AtomicInteger bonusNextAttack = new AtomicInteger(0);
        AtomicBoolean turnPotionForce = new AtomicBoolean(false);   // Tour de la potion active
        Label bonusattaque = new Label("Le bonus d'attaque est de " + bonusNextAttack.get());
        statsPanel.addComponent(bonusattaque);


        try {
            String json = CharacterHttpClient.getCharacter(Session.getUsername(), Session.getToken());
            JsonElement element = JsonParser.parseString(json);

            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();

                if (obj.has("data") && !obj.get("data").isJsonNull()) {
                    String characterType = obj.get("data").getAsString();
                    playerInfoPanel.addComponent(new Label("Type : " + characterType));

                    Personnage perso = CharactersFactory.getCharacterByType(characterType);

                    // Récupérer l'équipement et calculer le bonus de PV si l'armure est présente
                    String equipmentJson = CharacterHttpClient.getEquipment(Session.getUsername(), Session.getToken());
                    JsonObject equipmentResponse = JsonParser.parseString(equipmentJson).getAsJsonObject();
                    JsonArray dataArray = equipmentResponse.getAsJsonArray("data");

                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                            .create();
                    ObjectBase[] objets = gson.fromJson(dataArray, ObjectBase[].class);

                    // Calcul du bonus de PV (si l'armure existe)
                    int bonusPV = 0;
                    for (ObjectBase objEquip : objets) {
                        if (objEquip instanceof Armor) {
                            Armor armor = (Armor) objEquip;
                            bonusPV += armor.getBonusPV();
                        }
                    }

                    // Initialiser les PV avec le bonus
                    AtomicInteger playerHP = new AtomicInteger(perso.getPV() + bonusPV);
                    int playerHPmax = perso.getPV() + bonusPV;

                    // Afficher la santé avec le bonus
                    Label playerHealth = new Label("Votre santé : " + playerHP.get() + "/" + playerHPmax + " HP");
                    statsPanel.addComponent(playerHealth);

                    // Afficher dans l'historique le bonus appliqué
                    if(bonusPV > 0) {
                        history.append("Bonus d'armure appliqué : +" + bonusPV + " PV.\n");
                        historyLabel.setText(history.toString());
                    }

                    statsPanel.addComponent(playerHealth);


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


                            history.append("Vous avez infligé " + playerDamage + " PV avec " + attaqueNormale + ".\n");
                            historyLabel.setText(history.toString());

                            perso.CompteurAttack(perso.getCompteurAttack() + 1);
                            updateToursRestants(perso, toursRestantsLabel);

                            enemyTurn(gui, playerHealth, enemyHealth, combatWindow,
                                    playerHP, enemyHP, historyLabel, history, tourCounter, tourLabel,
                                    actionsPanel, showNormalAttacks[0], showSpecialAttacks[0], objectButton[0], perso, playerHPmax, enemyhpmax);

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


                                history.append("Vous avez infligé " + playerDamage + " PV avec " + attaqueSpeciale + ".\n");
                                historyLabel.setText(history.toString());

                                perso.CompteurAttack(0);
                                updateToursRestants(perso, toursRestantsLabel);

                                enemyTurn(gui, playerHealth, enemyHealth, combatWindow,
                                        playerHP, enemyHP, historyLabel, history, tourCounter, tourLabel,
                                        actionsPanel, showNormalAttacks[0], showSpecialAttacks[0], objectButton[0],  perso, playerHPmax, enemyhpmax);

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
                                playerHealth, enemyHealth, perso,
                                historyLabel, history, tourCounter, tourLabel, combatWindow,
                                showNormalAttacks[0], showSpecialAttacks[0], objectButton[0],
                                bonusNextAttack, turnPotionForce, bonusattaque, playerHPmax, enemyhpmax);

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

    /**
     * Affiche les actions principales du joueur
     * @param actionsPanel
     * @param normalAttack
     * @param specialAttack
     * @param objectButton
     */
    private static void showMainActions(Panel actionsPanel, Button normalAttack, Button specialAttack, Button objectButton) {
        actionsPanel.removeAllComponents();
        actionsPanel.addComponent(normalAttack);
        actionsPanel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        actionsPanel.addComponent(specialAttack);
        actionsPanel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        actionsPanel.addComponent(objectButton);
    }

    /**
     * Gère le tour de l'ennemi
     * @param gui
     * @param playerHealth
     * @param enemyHealth
     * @param combatWindow
     * @param playerHP
     * @param enemyHP
     * @param historyLabel
     * @param history
     * @param tourCounter
     * @param tourLabel
     * @param actionsPanel
     * @param showNormalAttacks
     * @param showSpecialAttacks
     * @param objectButton
     * @param perso
     * @param playerHPmax
     * @param enemyhpmax
     */
    private static void enemyTurn(WindowBasedTextGUI gui,
                                  Label playerHealth, Label enemyHealth, BasicWindow combatWindow,
                                  AtomicInteger playerHP, AtomicInteger enemyHP,
                                  Label historyLabel, StringBuilder history,
                                  AtomicInteger tourCounter, Label tourLabel,
                                  Panel actionsPanel,
                                  Button showNormalAttacks, Button showSpecialAttacks, Button objectButton, Personnage perso, int playerHPmax, int enemyhpmax) {


        enemyHealth.setText("La santé de l'ennemi : " + enemyHP.get() + "/" + enemyhpmax + " HP");
        int enemyDamage = 5; // Dégâts infligés par l'ennemi
        playerHP.addAndGet(-enemyDamage);
        playerHealth.setText("Votre santé : " + playerHP.get() + "/" + playerHPmax + " HP");

        // Ajouter l’attaque ennemie dans l’historique
        history.append("L'ennemi vous a infligé " + enemyDamage + " PV.\n");



        historyLabel.setText(history.toString());

        UpdateReliabilityArmorPanel (history);
        historyLabel.setText(history.toString());


        // Vérification de la fin du combat
        if (playerHP.get() <= 0) {
            history.append("\nVous avez été vaincu par l'ennemi .\n");
            historyLabel.setText(history.toString());

            MessageDialog.showMessageDialog(gui, "Défaite", "Vous avez été vaincu par le bot !");
            combatWindow.close();
            afficherMenuPrincipal(gui);
            return;
        } else if (enemyHP.get() <= 0) {
            history.append("\nVous avez vaincu l'ennemi !\n");
            historyLabel.setText(history.toString());

            MessageDialog.showMessageDialog(gui, "Victoire", "Vous avez vaincu le bot !");
            combatWindow.close();
            afficherMenuPrincipal(gui);
            return;
        }

        // Passer au tour suivant
        int currentTour = tourCounter.incrementAndGet();
        tourLabel.setText("Tour : " + currentTour);

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

    /**
     * Met à jour la fiabilité de l'armure
     * @param history
     */
    private static void UpdateReliabilityArmorPanel (StringBuilder history) {

        String username = Session.getUsername();
        try {
            String jsonequipment = CharacterHttpClient.getEquipment(username, Session.getToken());

            JsonObject response = JsonParser.parseString(jsonequipment).getAsJsonObject();
            JsonArray dataArray = response.getAsJsonArray("data");

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                    .create();

            ObjectBase[] objets = gson.fromJson(dataArray, ObjectBase[].class);
            for (ObjectBase objlist : objets) {
                String objectId = objlist.getId();
                Armor armor = (Armor) objlist;
                // Utilisation de l'armure
                String armorUseMessage = armor.use();

                try {
                    String responseupdateobject = CharacterHttpClient.updateArmorReliability(
                            username,
                            objectId,
                            armor.getReliability(),
                            Session.getToken()
                    );
                    System.out.println("MAJ fiabilité armure : " + responseupdateobject);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    history.append("Erreur de synchro fiabilité.\n");
                }

                // Vérification si l'arme est cassée et affichage du message après l'attaque
                if (armor.getReliability() == 0) {
                    history.append("Malheureusement " + armor.getName() + " s'est brisée.\n");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Crée le panel du backpack
     * @param gui
     * @param actionsPanel
     * @param playerHP
     * @param enemyHP
     * @param playerHealth
     * @param enemyHealth
     * @param perso
     * @param historyLabel
     * @param history
     * @param tourCounter
     * @param tourLabel
     * @param combatWindow
     * @param showNormalAttacks
     * @param showSpecialAttacks
     * @param objectButton
     * @param bonusNextAttack
     * @param turnPotionForce
     * @param bonusattaque
     * @param playerHPmax
     * @param enemyhpmax
     * @return
     */
    private static Panel createBackpackPanel(WindowBasedTextGUI gui, Panel actionsPanel, AtomicInteger playerHP, AtomicInteger enemyHP,
                                             Label playerHealth, Label enemyHealth, Personnage perso,
                                             Label historyLabel, StringBuilder history, AtomicInteger tourCounter, Label tourLabel,
                                             BasicWindow combatWindow,
                                             Button showNormalAttacks, Button showSpecialAttacks, Button objectButton,
                                             AtomicInteger bonusNextAttack, AtomicBoolean turnPotionForce, Label bonusattaque,
                                             int playerHPmax, int enemyhpmax)
    {
        Panel backpackPanel = new Panel(new GridLayout(1));
        String username = Session.getUsername();
        try {
            String jsonbackpack = CharacterHttpClient.getBackpack(username, Session.getToken());

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

                                try {
                                    String responseupdateobject = CharacterHttpClient.updateObjectReliability(
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
                                enemyTurn(gui, playerHealth, enemyHealth, combatWindow,
                                        playerHP, enemyHP, historyLabel, history, tourCounter, tourLabel,
                                        actionsPanel, showNormalAttacks, showSpecialAttacks, objectButton,  perso, playerHPmax, enemyhpmax);

                                break;

                            case "HealingPotion":
                                HealingPotion potion = (HealingPotion) objlist;
                                int healAmount = potion.getHeal();
                                int currentHP = playerHP.get();

                                // Calcule le soin réel sans dépasser les PV max
                                int actualHeal = Math.min(healAmount, playerHPmax - currentHP);

                                if (actualHeal > 0) {
                                    playerHP.addAndGet(actualHeal);
                                    history.append("Vous avez utilisé " + potion.getName() + " et récupéré " + actualHeal + " PV.\n");
                                } else {
                                    history.append("Vos PV sont déjà au maximum. La potion n’a eu aucun effet.\n");
                                }

                                if (turnPotionForce.get()){
                                    bonusNextAttack.set(0);
                                    turnPotionForce.set(false);
                                }


                                // 🔥 Supprimer la potion de la base de données (Backpack MongoDB)
                                try {
                                    String responseDelete = CharacterHttpClient.deleteObjectFromBackpack(username, objectId, Session.getToken());
                                    System.out.println("Suppression potion : " + responseDelete);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    history.append("Erreur lors de la suppression de la potion.\n");
                                }

                                // Vider et recharger l’affichage du backpack après suppression
                                backpackPanel.removeAllComponents();
                                Panel refreshedBackpack = createBackpackPanel(gui, actionsPanel, playerHP, enemyHP,
                                        playerHealth, enemyHealth, perso,
                                        historyLabel, history, tourCounter, tourLabel,
                                        combatWindow,
                                        showNormalAttacks, showSpecialAttacks, objectButton, bonusNextAttack,
                                        turnPotionForce, bonusattaque, playerHPmax, enemyhpmax);

                                actionsPanel.removeAllComponents();
                                actionsPanel.addComponent(refreshedBackpack);

                                // L'ennemi joue ensuite
                                enemyTurn(gui, playerHealth, enemyHealth, combatWindow,
                                        playerHP, enemyHP, historyLabel, history, tourCounter, tourLabel,
                                        actionsPanel, showNormalAttacks, showSpecialAttacks, objectButton,  perso, playerHPmax, enemyhpmax);
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
                                    String responseDelete = CharacterHttpClient.deleteObjectFromBackpack(username, objectId, Session.getToken());
                                    System.out.println("Suppression potion de force : " + responseDelete);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    history.append("Erreur lors de la suppression de la potion.\n");
                                }

                                // Vider et recharger l’affichage du backpack après suppression
                                backpackPanel.removeAllComponents();
                                refreshedBackpack = createBackpackPanel(gui, actionsPanel, playerHP, enemyHP,
                                        playerHealth, enemyHealth, perso,
                                        historyLabel, history, tourCounter, tourLabel,
                                        combatWindow, showNormalAttacks, showSpecialAttacks, objectButton,
                                        bonusNextAttack, turnPotionForce, bonusattaque, playerHPmax, enemyhpmax);

                                actionsPanel.removeAllComponents();
                                actionsPanel.addComponent(refreshedBackpack);

                                // L'ennemi joue ensuite
                                enemyTurn(gui, playerHealth, enemyHealth, combatWindow,
                                        playerHP, enemyHP, historyLabel, history, tourCounter, tourLabel,
                                        actionsPanel, showNormalAttacks, showSpecialAttacks, objectButton,  perso, playerHPmax, enemyhpmax);
                                break;




                            case "CoffreDesJoyaux":
                                CoffreDesJoyaux coffre = (CoffreDesJoyaux) objlist;

                                // Crée le sous-panel pour le contenu (initialement masqué)
                                Panel contenuPanel = new Panel(new GridLayout(1));
                                contenuPanel.setVisible(false); // caché au début

                                List<ObjectBase> contenu = coffre.getContenu();
                                if (contenu == null || contenu.isEmpty()) {
                                    contenuPanel.addComponent(new Label("→ Le coffre est vide."));
                                } else {
                                    for (ObjectBase item : contenu) {
                                        String itemLabel = "→ " + item.getName() + " (" + item.getType() + ")";
                                            /*
                                        // Crée un bouton pour chaque objet dans le coffre
                                        Button itemButton = new Button(itemLabel, () -> {
                                            try {
                                                // 1️⃣ Supprimer l'objet du contenu du coffre (MongoDB)
                                                String responseRemoveFromCoffre = HttpService.removeObjectFromCoffre(username, coffre.getId(), item.getId(), Session.getToken());
                                                System.out.println("Suppression de l'objet du coffre : " + responseRemoveFromCoffre);

                                                // 2️⃣ Ajouter l'objet au backpack (MongoDB)
                                                String responseAddToBackpack = HttpService.addObjectToBackpack(username, item.getId(), Session.getToken());
                                                System.out.println("Ajout de l'objet au backpack : " + responseAddToBackpack);

                                                history.append("Vous avez récupéré " + item.getName() + " du coffre et l’avez ajouté au backpack.\n");

                                                // 3️⃣ Recharger l'affichage
                                                backpackPanel.removeAllComponents();
                                                Panel refreshedBackpack = createBackpackPanel(gui, actionsPanel, playerHP, enemyHP,
                                                        playerHealth, enemyHealth, perso,
                                                        historyLabel, history, tourCounter, tourLabel,
                                                        combatWindow, showNormalAttacks, showSpecialAttacks, objectButton,
                                                        bonusNextAttack, turnPotionForce, bonusattaque, playerHPmax, enemyhpmax);

                                                actionsPanel.removeAllComponents();
                                                actionsPanel.addComponent(refreshedBackpack);

                                            } catch (Exception ex) {
                                                ex.printStackTrace();
                                                history.append("⚠️ Erreur lors du transfert de l’objet.\n");
                                            }

                                            historyLabel.setText(history.toString());
                                        });

                                        contenuPanel.addComponent(itemButton);
                                    }
                                */
                                    }
                                }
                                // Bouton Coffre qui toggle l'affichage du contenu
                                Button coffreButton = new Button(coffre.getName() + " (Coffre)", () -> {
                                    contenuPanel.setVisible(!contenuPanel.isVisible());
                                    contenuPanel.invalidate(); // 🟢 Rafraîchit uniquement le contenu
                                });

                                // Optionnel : entoure le bouton coffre + contenu avec une bordure
                                Panel coffreContainer = new Panel(new GridLayout(1));

                                coffreContainer.addComponent(coffreButton);
                                coffreContainer.addComponent(contenuPanel);

                                backpackPanel.addComponent(coffreContainer);
                                backpackPanel.addComponent(new EmptySpace(new TerminalSize(1, 1))); // espacement
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
    public static void StartCombatLan (WindowBasedTextGUI gui, StateCombat state){
        AtomicBoolean shouldRun = new AtomicBoolean(true);//Le thread l'utilse pour savoir quand stopé
        boolean[] forfaitEffectue = {false}; //boolean pour savoir si le user a quitter le combat
        int[] lasttour = {state.getTour()}; //Retient juste le tour précédent pour éviter de recréer des bouton inutilement


        String adversaire = state.getOpponent(Session.getUsername());


        BasicWindow combatWindow = new BasicWindow("Combat contre " + adversaire);
        combatWindow.setHints(List.of(Window.Hint.CENTERED));

        //Appelle a chaque a state car il contient les données du combat en cours

        Panel mainPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        Label tourLabel = new Label("Tour : " + state.getTour());
        mainPanel.addComponent(tourLabel);

        Label horizontalLine = new Label("----------------------------------------");
        mainPanel.addComponent(horizontalLine);

        Panel pvPanel = new Panel(new GridLayout(3));

        Label labelPvAdversaire = new Label("PV adversaire : " + state.getPv(adversaire));
        Label labelMesPv = new Label("Vos PV : " + state.getPv(Session.getUsername()));
        EmptySpace espace = new EmptySpace(new TerminalSize(5, 1)); // espace horizontal


        labelMesPv.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.CENTER, true, false));
        labelPvAdversaire.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER, true, false));

        pvPanel.addComponent(labelMesPv);
        pvPanel.addComponent(espace);
        pvPanel.addComponent(labelPvAdversaire);

        mainPanel.addComponent(pvPanel);

        Label horizontalLine1 = new Label("----------------------------------------");
        mainPanel.addComponent(horizontalLine1);

        Panel historyPanel = new Panel(new GridLayout(1));

        Panel actionPanel = new Panel(new GridLayout(1));


        historyPanel.addComponent(new Label("Historique :"));
        for (String entry : state.getLog()) {
            historyPanel.addComponent(new Label(entry));
        }

        mainPanel.addComponent(historyPanel);
        mainPanel.addComponent(actionPanel);

        //Si le user clique sur quitter le combat alors on fait appel au backend et passe a true
        //Arret proprement du thread et ferme la fenetre
        mainPanel.addComponent(new Button("Quitter le combat", () -> {
            try {
                FightHttpCLient.forfait(Session.getUsername(), Session.getToken());
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
                    String json = FightHttpCLient.getCombatState(Session.getUsername(), Session.getToken());
                    //Quand ça rencontre un objet de type ObjectBase, utilise le deserialiseur custom
                    //comme ça Gson saure instancier les bonnes sous-classes
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(ObjectBase.class, new ObjectBasePolymorphicDeserializer())
                            .create();
                    //Transforme en objet java SateCombat(permet a l'interface de savoir le bon état du combat)
                    StateCombat updated = gson.fromJson(json, StateCombat.class);
                    //Si null alors le joueur a quitté
                    //SI pas je récup le dernier winner
                    if (updated == null) {
                        gui.getGUIThread().invokeLater(() -> {
                            String winner = null;
                            try {
                                String reponseJson = FightHttpCLient.getLastWinner(Session.getUsername(), Session.getToken());
                                //parser la chaine JSON recu de reponseJson en un objet JsonObject (on peux la acceder a chaque champ)
                                JsonObject jsonObject = JsonParser.parseString(reponseJson).getAsJsonObject();
                                //extrait le champs winner de l'objet JSON et stock dans winner
                                winner = jsonObject.get("winner").getAsString();
                            } catch (Exception e) {
                                System.out.println("Erreur récup du gagnant : " + e.getMessage());
                            }

                            String message;
                            if (forfaitEffectue[0]) {
                                message = "Vous avez quitté le combat, votre adversaire a gagné";
                            } else if (winner != null && winner.equals(Session.getUsername())) {
                                message = "Combat terminé, vous avez gagné";
                            } else if (winner != null) {
                                message = "Combat terminé, " + winner + " a gagné";
                            } else {
                                message = "Combat terminé, mais le gagnant est inconnu";
                            }

                            MessageDialog.showMessageDialog(gui, "Fin du combat", message);
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


                           //Thread pour afficher les pv 0
                            new Thread(() ->{
                                try{
                                    Thread.sleep(600);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                           gui.getGUIThread().invokeLater(() ->{
                            String winner = null;
                            try {
                                String reponseJson  = FightHttpCLient.getLastWinner(Session.getUsername(), Session.getToken());
                                JsonObject jsonObject = JsonParser.parseString(reponseJson).getAsJsonObject();
                                winner = jsonObject.get("winner").getAsString();
                            } catch (Exception e) {
                            }

                            String message;
                            if (winner == null) {
                                message = "Combat terminé, mais le gagnant est inconnu.";
                            } else if ("Egalité".equals(winner)) {
                                message = "Combat terminé sur une égalité !";
                            } else if (winner.equals(Session.getUsername())) {
                                message = "Combat terminé, vous avez gagné !";
                            } else {
                                message = "Vous êtes mort, le ccmbat est terminé, " + winner + " a gagné.";
                            }
                            combatWindow.close();
                            MessageDialog.showMessageDialog(gui, "Fin du comabt", message);
                            afficherMenuPrincipal(gui);
                        });
                            }).start();
                        });
                        break;
                    }

                    //Ici a chaque invokeLater il y aura une mise a jour visuelle du tour, les pvs et historique
                    //Ici je mets a jour l'interface sans la recréer avec invokeLater
                    gui.getGUIThread().invokeLater(() -> {
                        tourLabel.setText("Tour : " + updated.getTour());
                        labelPvAdversaire.setText("PV adversaire : " + updated.getPv(adversaire));
                        labelMesPv.setText("Vos PV : " + updated.getPv(Session.getUsername()));


                        // Rafraîchi l'historique à chaque update
                        historyPanel.removeAllComponents();
                        historyPanel.addComponent(new Label("Historique :"));
                        for (String entry : updated.getLog()) {
                            historyPanel.addComponent(new Label(entry));
                        }

                        // Met à jour les actions si le tour change
                        //lasttour : apres apprel au back, getTour compare avec lasttour si tour diff alors il regarde getPlayerNow pour know a qui le tour
                        if (updated.getTour() != lasttour[0]) {
                            lasttour[0] = updated.getTour(); //Met a jour le cpt
                            actionPanel.removeAllComponents();
                            if (updated.getPlayerNow().equals(Session.getUsername())) {
                                updateActionPanel(actionPanel, updated, gui); //affiche bouton
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

    /***
     * @param actionPanel
     * @param gui
     * @param state
     * Rafraichissemnt auto des bouton essaie
     * Affiche les actions dispo pour le joueur
     * */
    private static void updateActionPanel(Panel actionPanel, StateCombat state, WindowBasedTextGUI gui) {

        actionPanel.addComponent(new Label("Vos actions :"));
        actionPanel.addComponent(new Button("Attaque normale ", () -> {
            try {
                FightHttpCLient.combatAttack(Session.getUsername(), "normal", Session.getToken());
            } catch (Exception e) {
                MessageDialog.showMessageDialog(gui, "Erreur", e.getMessage());
            }
        }));
        actionPanel.addComponent(new Button("Attaque spéciale ", () -> {
            try {
                FightHttpCLient.combatAttack(Session.getUsername(), "special", Session.getToken());
            } catch (Exception e) {
                MessageDialog.showMessageDialog(gui, "Erreur", e.getMessage());
            }
        }));
        actionPanel.addComponent(new Button("Ouvrir le Coffre des Joyaux", () -> {
            displayChest(gui, state.getChest(Session.getUsername()));
        }));

        // Objets du backpack
        for (ObjectBase obj : state.getBackpack(Session.getUsername())) {
            if(obj instanceof  CoffreDesJoyaux) continue; //Cache le bouton utiliser pour le coffer
            actionPanel.addComponent(new Button("Utiliser objet : " + obj.getName(), () -> {
                try {
                    FightHttpCLient.combatUseObject(Session.getUsername(), obj.getId(), Session.getToken());
                } catch (Exception e) {
                    MessageDialog.showMessageDialog(gui, "Erreur", e.getMessage());
                }
            }));
        }
    }

    /**
     * @param gui
     * @param chest
     * Display coffre
     * affiche tous les objest dans state.getchest
     * **/
    private static  void displayChest(WindowBasedTextGUI gui, List<ObjectBase> chest){
        BasicWindow chestWindow = new BasicWindow("Coffre des Joyaux");
        chestWindow.setHints(List.of(Window.Hint.CENTERED));

        Panel panel = new Panel(new GridLayout(1));
        panel.addComponent(new Label("Objets disponibles dans le coffre :"));

        if(chest.isEmpty()){
            panel.addComponent(new Label("Coffre vide"));
        }
        else{
            for (ObjectBase obj : chest) {
                panel.addComponent(new Button("Utiliser : " + obj.getName(), () -> {
                    try {
                        FightHttpCLient.combatUseObject(Session.getUsername(), obj.getId(),Session.getToken());
                        MessageDialog.showMessageDialog(gui, "Objet utilisé", obj.getName() + " a été utilisé !");
                        chestWindow.close();
                    }
                    catch (Exception e) {
                        MessageDialog.showMessageDialog(gui, "Erreur", e.getMessage());
                    }
                }));
            }
        }
        panel.addComponent(new Button("Fermer", chestWindow::close));

        chestWindow.setComponent(panel);
        gui.addWindow(chestWindow);
    }

    /**
     * Affiche le classement
     * @param gui
     */
    private static void DisplayClassement (WindowBasedTextGUI gui){
        BasicWindow profileWindow = new BasicWindow("Classement");
        profileWindow.setHints(Arrays.asList(Hint.CENTERED));
        Panel panel = new Panel(new GridLayout(1));
        panel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.BEGINNING));

        panel.addComponent(new EmptySpace(new TerminalSize(0, 1)));


        try {
            String userJson = FightHttpCLient.getClassementPlayer(Session.getToken());
            if (userJson == null) {
                panel.addComponent(new Label("Erreur : impossible de récupérer le classement"));
            } else {
                Type list = new TypeToken<List<UserInfo>>() {
                }.getType();
                List<UserInfo> classement = new Gson().fromJson(userJson, list);
                if (classement.isEmpty()) {
                    panel.addComponent(new Label("classement vide"));
                } else {
                    List<UserInfo> top3 = classement.size() > 3 ? classement.subList(0, 3) : classement; //Affiche les 3 meilleurs
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
     * Generer une bare de progression pour trophée
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
        return barre +Math.min( actuel, objectif) + "/"  + objectif; //Jsute texte de progression
    }

    /**
     * @param gui
     * Afficher les trophées en faisant appele au service fetchUserInfo ()(direct la reponse objet userJson pas besoin de la parser)
     *
     * */
    public static void Displaytrophy(WindowBasedTextGUI gui) {
        BasicWindow window = new BasicWindow("Vos Trophées");
        window.setHints(List.of(Window.Hint.CENTERED)); //Centrés
        Panel panel = new Panel(new GridLayout(1));

        panel.addComponent(new Label("Progression des trophées :"));

        try {
            UserInfo user = Login_Register_userHttpClient.fetchUserInfo(Session.getUsername(), Session.getToken()); //Pour winConcecutive ds mysql
            Document stats = Login_Register_userHttpClient.fetchUserStats(Session.getUsername(), Session.getToken()); //Pour stat dans mongo
            NotifThropy(gui, stats, user);
            int cristaux = stats.getInteger("cristauxWin", 0);
            int nombreTours = stats.getInteger("derniercombattour", 0);
            int bazooka = stats.getInteger("utilisationBazooka", 0);
            int win = user.getGagner();
            int winConcecutive = user.getWinconsecutive();

            boolean bronze = stats.getBoolean("bronze",false);
            boolean silver = stats.getBoolean("silver",false);
            boolean or = stats.getBoolean("or",false);

            String bronzeLabel = "bronze : " + CalculateProgression(List.of(
              win >= 1,
                win >= 1 && nombreTours <= 15
            ));
            if (bronze) bronzeLabel += " obtenu";

            String sliverLabel = "silver : " + CalculateProgression(List.of(
                winConcecutive >= 5,
                cristaux >= 200,
                win >= 1 && nombreTours <=10
            ));
            if (silver) sliverLabel += "obtenu";


            String orLabel = "or : " + CalculateProgression(List.of(
                winConcecutive >= 10,
                cristaux >= 500,
                win >=1 && nombreTours <= 6,
                bazooka > 0
            ));
            if (or) orLabel += "obtenu";

            panel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

            //   BRONZE
            Button bronzeBtn = new Button(bronzeLabel, () -> {
                Panel details = new Panel(new GridLayout(1));
                details.addComponent(new Label("Objectif : Gagner 1 combat et le finir en 15 tours ou moins."));
                details.addComponent(new EmptySpace());
                details.addComponent(new Label("Progression :"));
                details.addComponent(new Label("Gagner 1 combat : " + generateBar(win, 1)));
                details.addComponent(new Label("Combat en ≤ 15 tours : " + generateBar(user.getGagner() > 0 && nombreTours <= 15 ? 1 : 0, 1)));
                MessageDialog.showMessageDialog(gui, "Trophée Bronze", detailsToString(details));
            });
            panel.addComponent(bronzeBtn);

            panel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

            Button SilverBtn = new Button(sliverLabel, () -> {
                Panel details = new Panel(new GridLayout(1));
                details.addComponent(new Label("Objectif : Gagner 5 combats consécutifs, collecter 200 cristaux et finir un combat en 10 tours ou moins."));
                details.addComponent(new EmptySpace());
                details.addComponent(new Label("Progression :"));
                details.addComponent(new Label("5 victoires consécutives : " + generateBar(winConcecutive, 5)));
                details.addComponent(new Label("Gagnez 200 cristaux : " + generateBar(cristaux, 200)));
                details.addComponent(new Label("Combat en ≤ 10 tours : " + generateBar(user.getGagner() > 0 && nombreTours <= 10 ? 1 : 0, 1)));
                MessageDialog.showMessageDialog(gui, "Trophée Silver", detailsToString(details));
            });

            panel.addComponent(SilverBtn);

            panel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

            Button OrBtn = new Button(orLabel, () -> {
                Panel details = new Panel(new GridLayout(1));
                details.addComponent(new Label("Objectif : Gagner 10 combats consécutifs, collecter 500 cristaux, finir un combat en 6 tours ou moins, et utiliser un bazooka."));
                details.addComponent(new EmptySpace());
                details.addComponent(new Label("Progression :"));
                details.addComponent(new Label("10 victoires consécutives : " + generateBar(winConcecutive, 10)));
                details.addComponent(new Label("Gagnez 500 cristaux : " + generateBar(cristaux, 500)));
                details.addComponent(new Label("Combat en ≤ 6 tours : " + generateBar(user.getGagner() > 0 && nombreTours <= 6 ? 1 : 0, 1)));
                details.addComponent(new Label("Avoir utilisé un bazooka : " + generateBar(bazooka > 0 ? 1 : 0, 1)));
                MessageDialog.showMessageDialog(gui, "Trophée Or", detailsToString(details));
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
     * @param condition
     * Calcule la preogression des trophés,obligé sinon les conditions sons foireusses (0,1)
     * **/
    public static String CalculateProgression(List<Boolean> condition){
        int totalCondition = condition.size();
        int valid = 0;

        for (boolean conditions : condition) {
            if (conditions) {
                valid++;
            }
        }
        return generateBar(valid,totalCondition); //1/2 avec barre
    }


    /**
     * Notif appelée dans DisplayTrphy qui signal au joueur qu'il a obtenu le trophé
     * **/
    public static String NotifThropy(WindowBasedTextGUI gui, Document state, UserInfo user) {
        int win = user.getGagner();
        int winconcec = user.getWinconsecutive();
        int cristaux = state.getInteger("cristauxWin", 0);
        int nbrtours = state.getInteger("derniercombattour", 0);
        int bazooka = state.getInteger("utilisationBazooka", 0);

        boolean bronze = state.getBoolean("bronze", false);
        boolean silver = state.getBoolean("silver", false);
        boolean or = state.getBoolean("or", false);

        //bronze
        boolean bronzeOk = (win >= 1) && (win >= 1 && nbrtours <= 15);
        if (bronzeOk && !Session.getTrophyNoti("bronze")) {
            MessageDialog.showMessageDialog(gui, "Trophée débloqué !",
                "Trophé Bronze débloqué ! \n\n" +
                    "Conditions remplies : \n" +
                    "Gagnez 1 combat\nGagnez un combat en 15 tours ou moins \n\n " +
                    "Récompense : Epée en bois");
            Session.addTrophyNoti("bronze");
        }

        boolean silverok = (winconcec >= 5) && (cristaux >=200) && (win >= 1 && nbrtours <= 10);
        if (silverok && !Session.getTrophyNoti("silver")) {
            MessageDialog.showMessageDialog(gui, "Trophée débloqué !",
                "Trophé Silver  débloqué ! \n\n" +
                    "Conditions remplies : \n" +
                    "5 victoires concécutives\nGagnez un combat en 10 tours ou moins \nGagnez 200 cristaux " +
                    "Récompense : Couteau en diamant + 50 cristaux");
            Session.addTrophyNoti("or");

        }
        boolean orOK = (winconcec >= 10) && (cristaux >=500) && (win >= 1 && nbrtours <= 6) && (bazooka > 0);
        if (orOK && !Session.getTrophyNoti("or")) {
            MessageDialog.showMessageDialog(gui, "Trophée débloqué !",
                "Trophé Or débloqué ! \n\n" +
                    "Conditions remplies : \n" +
                    "10 victoires concécutives\nGagnez un combat en 6 tours ou moins \nGagnez 500 cristaux\nBazooka utilisé\n\n " +
                    "Récompense : Couteau en diamant + 75 cristaux");
            Session.addTrophyNoti("or");

        }

        return "";
    }



    /**
     * @return
     *  permet d'afficher les regles du jeu
     * **/
    private static void showRules(WindowBasedTextGUI gui) {
        BasicWindow window = new BasicWindow("Règles du jeu :");
        window.setHints(List.of(Window.Hint.CENTERED)); //Centrés

        Panel panel = new Panel(new GridLayout(1));
        panel.addComponent(new EmptySpace());
        panel.addComponent(new Label("Bienvenue dans Crystal Clash !"));
        panel.addComponent(new Label("Voici les règles du jeu :"));

        panel.addComponent(new EmptySpace());

        panel.addComponent(new Label("• Chaque joueur commence avec 100 PV."));
        panel.addComponent(new Label("• Les joueurs jouent à tour de rôle."));
        panel.addComponent(new Label("• Chaque joueur peut utiliser des objets depuis son sac à dos (backpack)."));
        panel.addComponent(new Label("• Les attaques de base sont toujours disponibles."));
        panel.addComponent(new Label("• Les attaques spéciales se débloquent après plusieurs attaques normales."));
        panel.addComponent(new Label("• Les objets ont une endurance limitée."));

        panel.addComponent(new EmptySpace());

        panel.addComponent(new Label("Récompenses en fin de combat :"));
        panel.addComponent(new Label("• Le gagnant reçoit 50 cristaux et monte d'un niveau."));
        panel.addComponent(new Label("• Le perdant ne reçoit rien."));
        panel.addComponent(new Label("• Si un joueur abandonne (forfait), il ne reçoit rien."));
        panel.addComponent(new Label("• L’adversaire d’un joueur forfait gagne 25 cristaux."));
        panel.addComponent(new EmptySpace());


        panel.addComponent(new Label("Matchmaking :"));
        panel.addComponent(new Label("• Pour jouer avec un ami, allez dans la salle d'attente et attendez qu'il vous rejoigne."));
        panel.addComponent(new Label("• Sinon, vous pouvez lancer un combat contre un bot en cliquant sur \"Lancer un combat\"."));
        panel.addComponent(new EmptySpace());

        panel.addComponent(new Label("Gérer l'équipement :"));
        panel.addComponent(new Label("• Inventaire : 30 emplacements."));
        panel.addComponent(new Label("• Sac à dos : 5 emplacements pour les objets actifs."));
        panel.addComponent(new Label("• Coffre : 10 emplacements, peut être mis dans l'inventaire ou le sac à dos."));


        panel.addComponent(new Label("Trophées :"));
        panel.addComponent(new Label("• Vous pouvez obtenir des trophées en réalisant des défis."));
        panel.addComponent(new Label("• Certains trophées offrent des récompenses spéciales comme des objets et des cristaux pour certains"));

        panel.addComponent(new EmptySpace());

        panel.addComponent(new Label("Roulette :"));
        panel.addComponent(new Label("• Une fois par jour, vous pouvez jouer à la roulette."));
        panel.addComponent(new Label("• Cela vous permet de gagner des objets aléatoires."));
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

