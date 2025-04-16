package be.helha.labos.crystalclash.ApiResponse;

public class ApiReponse {
    private  String message;
    private int code;
    private Object data;

    public ApiReponse(String message, int code, Object data) {
        this.message = message;
        this.code = code;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message){
        this.message = message;
    }

    public int getCode(){
        return code;
    }

    public Object datda(){
        return data;
    }

    public void setData(Object data){
        this.data = data;
    }

}
