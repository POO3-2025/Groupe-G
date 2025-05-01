package be.helha.labos.crystalclash.User;

import be.helha.labos.crystalclash.Services.HttpService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

@WebServlet("/connected-users")
public class ConnectedUsersServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Récupérer les utilisateurs connectés
        Set<String> connectedUsers = null;
        try {
            connectedUsers = HttpService.getConnectedUsers();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Configurer la réponse
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Écrire les utilisateurs connectés dans la réponse
        PrintWriter out = response.getWriter();
        out.println("{ \"connectedUsers\": " + connectedUsers + " }");
    }
}
