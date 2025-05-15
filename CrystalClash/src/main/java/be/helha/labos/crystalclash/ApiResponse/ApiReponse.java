package be.helha.labos.crystalclash.ApiResponse;

/**
 * Classe ApiReponse
 * Cette classe est utilisée pour encapsuler une réponse d'API
 * avec un message et des données.
 */
public class ApiReponse {
    private  String message;
    private Object data;
    /**
     * Constructeur de la classe ApiReponse
     * @param message
     * @param data
     */
    public ApiReponse(String message, Object data) {
        this.message = message;

        this.data = data;
    }

    /**
     * Getter du message
     * * @param message
     * @return message
     */
    public String getMessage() {
        return message;
    }
    /**
     * Setter du message
     * @param message
     */
    public void setMessage(String message){
        this.message = message;
    }


    /**
     * Getter de data
     * @return data
     */
    public Object getData(){
        return data;
    }
    /**
     * Setter de data
     * @param data
     */
    public void setData(Object data){
        this.data = data;
    }

}
