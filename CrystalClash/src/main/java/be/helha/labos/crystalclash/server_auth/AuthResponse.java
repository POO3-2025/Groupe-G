package be.helha.labos.crystalclash.server_auth;

/**
 * Reponse de la co
 * **/
public class AuthResponse {
    private String token;
    private String message;

    /**
     * La réponse va contenir si ok le token du user et un message true ou false
     * @param token token
     * @param message message
     * **/
    public AuthResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }

    /**
     * getToken
     * @return token
     * **/
    public String getToken() {
        return token;
    }

    /**
     * @param token token
     * **/
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * getMessage
     * @return message
     * **/
    public String getMessage() {
        return message;
    }

    /**
     * @param message message
     * **/
    public void setMessage(String message) {
        this.message = message;
    }
}
