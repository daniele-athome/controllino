package it.casaricci.controllino.data;


public class ServerProfileInfo {
    private final int resId;
    private final String data;

    public ServerProfileInfo(int resId, String data) {
        this.resId = resId;
        this.data = data;
    }

    public int getResourceId() {
        return resId;
    }

    public String getData() {
        return data;
    }

}
