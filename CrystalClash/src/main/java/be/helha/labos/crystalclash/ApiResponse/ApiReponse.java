package be.helha.labos.crystalclash.ApiResponse;

public class ApiReponse {
    private  String message;
    private Object data;

    public ApiReponse(String message, Object data) {
        this.message = message;

        this.data = data;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message){
        this.message = message;
    }



    public Object datda(){
        return data;
    }

    public void setData(Object data){
        this.data = data;
    }

}