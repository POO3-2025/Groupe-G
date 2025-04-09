package be.helha.labos.crystalclash.LanternaApp;

import be.helha.labos.crystalclash.Services.*;
import be.helha.labos.crystalclash.server_auth.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;

public class LanternaApp {

    public static void main(String[] args) throws Exception {
        Screen screen = new DefaultTerminalFactory().createScreen();
        screen.startScreen();

        WindowBasedTextGUI gui = new MultiWindowTextGUI(screen);

        afficherEcranAccueil(gui, screen);

    }
    private static void afficherEcranAccueil(WindowBasedTextGUI gui, Screen screen) {
        BasicWindow window = new BasicWindow("Crystal Clash : Connexion");

        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(1));

        panel.addComponent(new Label("Bienvenue dans Crystal Clash"));
        panel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        panel.addComponent(new Button("1. Connexion", () -> {
            afficherFormulaireConnexion(gui, window);
        }));
        panel.addComponent(new Button("2. Inscription", () -> {
            afficherFormulaireInscription(gui);
        }));
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

    private static void afficherFormulaireConnexion(WindowBasedTextGUI gui, Window menuInitialWindow){
        Panel loginPanel = new Panel(new GridLayout(2));
        loginPanel.addComponent(new Label("Nom d'utilisateur :"));
        TextBox usernameBox = new TextBox();
        loginPanel.addComponent(usernameBox);

        loginPanel.addComponent(new Label("Mot de passe :"));
        TextBox passwordBox = new TextBox().setMask('*');
        loginPanel.addComponent(passwordBox);

        BasicWindow loginWindow = new BasicWindow("Connexion");
        loginWindow.setComponent(loginPanel);

        loginPanel.addComponent(new Button("Se connecter", () -> {
            try {
                String json = HttpService.login(usernameBox.getText(), passwordBox.getText());
                JsonObject response = JsonParser.parseString(json).getAsJsonObject();
                if (response.has("token") && !response.get("token").isJsonNull()) {
                    String token = response.get("token").getAsString();
                    Session.setToken(token);
                    Session.setUsername(usernameBox.getText());

                    // Fermer proprement la fenêtre de connexion
                    loginWindow.close();
                    menuInitialWindow.close();

                    // Afficher le menu principal juste après
                    afficherMenuPrincipal(gui);
                } else {
                    String message = response.has("message") ? response.get("message").getAsString() : "Réponse invalide.";
                    MessageDialog.showMessageDialog(gui, "Erreur", "Échec : " + message);
                }
            } catch (Exception e) {
                MessageDialog.showMessageDialog(gui, "Exception", e.getMessage());
            }
        }));
        loginPanel.addComponent(new Button("Retour", loginWindow::close));
        loginWindow.setComponent(loginPanel);
        gui.addWindowAndWait(loginWindow);
    }

    private static void afficherFormulaireInscription(WindowBasedTextGUI gui) {
        Panel registerPanel = new Panel(new GridLayout(2));
        registerPanel.addComponent(new Label("Nom d'utilisateur :"));
        TextBox usernameBox = new TextBox();
        registerPanel.addComponent(usernameBox);

        registerPanel.addComponent(new Label("Mot de passe :"));
        TextBox passwordBox = new TextBox().setMask('*');
        registerPanel.addComponent(passwordBox);
        BasicWindow registerWindow = new BasicWindow("Inscription");
        registerPanel.addComponent(new Button("S'inscrire", () -> {
            try {
                String json = HttpService.register(usernameBox.getText(), passwordBox.getText());
                if (json.contains("Inscription réussie")) {
                    MessageDialog.showMessageDialog(gui, "Succès", "Compte créé !");
                    gui.getActiveWindow().close(); //  ferme la fenêtre d'inscription
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
        Panel mainPanel = new Panel(new GridLayout(1));

        mainPanel.addComponent(new Label(" Bienvenue dans Crystal Clash, " + Session.getUsername() + " !"));
        mainPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        mainPanel.addComponent(new Button("1. Voir profil", () -> {
            MessageDialog.showMessageDialog(gui, "Profil", "Fonctionnalité à venir !");
        }));

        mainPanel.addComponent(new Button("2. Accéder à la boutique", () -> {
            MessageDialog.showMessageDialog(gui, "Boutique", "Fonctionnalité à venir !");
        }));
        mainPanel.addComponent(new Button("3. Voir inventaire", () -> {
            try {
                String json = HttpService.getInventory();
                System.out.println("Réponse brute : " + json);
                MessageDialog.showMessageDialog(gui, "Debug", json);
            } catch (Exception e) {
                MessageDialog.showMessageDialog(gui, "Erreur", e.getMessage());
            }
        }));
        mainPanel.addComponent(new Button("3. Se déconnecter", () -> {
            Session.clear();
            MessageDialog.showMessageDialog(gui, "Déconnexion", "Vous avez été déconnecté !");
            gui.getActiveWindow().close();
            afficherEcranAccueil(gui, gui.getScreen());
        }));

        BasicWindow menuWindow = new BasicWindow("Menu Principal");
        menuWindow.setComponent(mainPanel);
        gui.addWindowAndWait(menuWindow);
    }

}
