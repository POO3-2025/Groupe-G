package be.helha.labos.crystalclash.Controller.LanternaApp;

import be.helha.labos.crystalclash.Object.ObjectBase;
import be.helha.labos.crystalclash.Services.HttpService;
import be.helha.labos.crystalclash.User.UserInfo;
import be.helha.labos.crystalclash.User.UserManger;
import be.helha.labos.crystalclash.server_auth.Session;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Window.Hint;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

public class LanternaApp {

    public static void main(String[] args) throws Exception {
        Screen screen = new DefaultTerminalFactory().createScreen();
        screen.startScreen();

        WindowBasedTextGUI gui = new MultiWindowTextGUI(screen);

        afficherEcranAccueil(gui, screen);
    }

    private static void afficherEcranAccueil(WindowBasedTextGUI gui, Screen screen) {
        BasicWindow window = new BasicWindow("Crystal Clash : Connexion");
        window.setHints(Arrays.asList(Hint.CENTERED));

        Panel panel = new Panel(new GridLayout(1));
        panel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.BEGINNING));

        panel.addComponent(new Label("Bienvenue dans Crystal Clash"));
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
                    MessageDialog.showMessageDialog(gui, "Succès", "Compte créé !");
                    gui.getActiveWindow().close();

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
        if (info != null) {
            mainPanel.addComponent(new Label(" Niveau : " + info.getLevel()));
            mainPanel.addComponent(new Label(" Cristaux : " + info.getCristaux()));
        }
        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        mainPanel.addComponent(new Button("1. Voir profil", () -> {
            MessageDialog.showMessageDialog(gui, "Profil", "Fonctionnalité à venir !");
        }));

       // mainPanel.addComponent(new Button("2. Accéder à la boutique", () -> afficherBoutique(gui)));

        mainPanel.addComponent(new Button("2. Changer de personnage", () -> afficherChoixPersonnage(gui)));
        mainPanel.addComponent(new Button("3. Voir inventaire", () -> afficherInventaire(gui)));


        mainPanel.addComponent(new Button("5. Voir joueurs connectés", () -> afficherJoueursConnectes(gui)));

        mainPanel.addComponent(new Button("6. Se déconnecter", () -> {
            Session.clear();
            MessageDialog.showMessageDialog(gui, "Déconnexion", "Vous avez été déconnecté !");
            gui.getActiveWindow().close();
            afficherEcranAccueil(gui, gui.getScreen());
        }));

        menuWindow.setComponent(mainPanel);
        gui.addWindowAndWait(menuWindow);
    }

    /*
     * Crée fenetre lanterna avec inventaire comme titre
     * bon apres ce n est que du visuel mais on aligne verticalement la colonne
     * affiche le nom de luti connecter
     * requete http vers le serv springbbot = String json = HttpService.getInventoryFromServer(Session.getUsername(), Session.getToken());
     * parse le json c est a dire du json en objet java (clé-valeur)
     * si inventaire ok est là alors converit inventory en tb objets de objectBase
     * puis on affiche le nombre d objets dedans
     * si il y a objet on les affiche sur ligne
     * si erreur ben erreur
     * */
    private static void afficherInventaire(WindowBasedTextGUI gui) {
        BasicWindow invWindow = new BasicWindow("Inventaire");
        invWindow.setHints(Arrays.asList(Hint.CENTERED));

        Panel panel = new Panel(new GridLayout(1));
        panel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.BEGINNING));

        panel.addComponent(new Label("Inventaire de " + Session.getUsername()));

        try {
            String json = HttpService.getInventoryFromServer(Session.getUsername(), Session.getToken());

            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            if (jsonObject.has("inventory")) {
                Gson gson = new Gson();
                ObjectBase[] objets = gson.fromJson(json, ObjectBase[].class);

                panel.addComponent(new Label("Objets : " + objets.length + " / 30"));

                if (objets.length == 0) {
                    panel.addComponent(new Label("Aucun objet dans l'inventaire."));
                } else {
                    for (ObjectBase obj : objets) {
                        panel.addComponent(new Label("- " + obj.getName()));
                    }
                }
            } else if (jsonObject.has("message")) {
                panel.addComponent(new Label("Erreur : " + jsonObject.get("message").getAsString()));
            } else {
                panel.addComponent(new Label("Réponse invalide."));
            }

        } catch (Exception e) {
            panel.addComponent(new Label("Erreur de communication : " + e.getMessage()));
        }

        panel.addComponent(new Button("Retour", invWindow::close));
        invWindow.setComponent(panel);
        gui.addWindowAndWait(invWindow);
    }

    private static void afficherJoueursConnectes(WindowBasedTextGUI gui) {
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
}
