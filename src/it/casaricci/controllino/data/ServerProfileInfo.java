package it.casaricci.controllino.data;


public class ServerProfileInfo {
    private final String field;
    private String data;
    private final int resId;

    public ServerProfileInfo(String field, String data, int resId) {
        this.field = field;
        this.data = data;
        this.resId = resId;
    }

    public int getResourceId() {
        return resId;
    }

    public String getField() {
        return field;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
