package it.casaricci.controllino.data;


public class RecordInfo {
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_LIST = 2;

    public static final int TYPE_DEFAULT = TYPE_TEXT;

    private final String field;
    private String data;
    private final int resId;
    private final int type;

    public RecordInfo(String field, String data, int resId) {
        this(field, data, resId, TYPE_DEFAULT);
    }

    public RecordInfo(String field, String data, int resId, int type) {
        this.field = field;
        this.data = data;
        this.resId = resId;
        this.type = type;
    }

    public int getResourceId() {
        return resId;
    }

    public int getType() {
        return type;
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
