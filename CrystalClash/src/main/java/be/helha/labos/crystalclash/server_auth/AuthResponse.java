package be.helha.labos.crystalclash.server_auth;

public class AuthResponse {
    private String token;
    private String message;

    /**
     * La réponse va contenir si ok le token du user et un message true ou false
     * **/
    public AuthResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }

    // Getters et setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
