package it.casaricci.controllino.data;


public class RecordInfo {
    /** Normal plain text. */
    public static final int TYPE_TEXT = 1;
    /** ListView dialog. */
    public static final int TYPE_LIST = 2;
    /** ListView for choosing a server profile. */
    public static final int TYPE_SERVER_PROFILE = 3;
    /** Username text. */
    public static final int TYPE_USERNAME = 4;
    /** Password text. */
    public static final int TYPE_PASSWORD = 5;
    /** Numeric text. */
    public static final int TYPE_NUMBER = 6;
    /** Host address or URL. */
    public static final int TYPE_ADDRESS_URL = 7;

    /** Default record type. */
    public static final int TYPE_DEFAULT = TYPE_TEXT;

    private final String field;
    private String data;
    private long dataId;
    private final int resId;
    private final int type;

    public RecordInfo(String field, String data, int resId) {
        this(field, data, resId, TYPE_DEFAULT);
    }

    public RecordInfo(String field, String data, int resId, int type) {
        this(field, data, resId, 0, type);
    }

    public RecordInfo(String field, String data, int resId, long dataId, int type) {
        this.field = field;
        this.data = data;
        this.resId = resId;
        this.dataId = dataId;
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

    public long getDataId() {
        return dataId;
    }

    public void setDataId(long dataId) {
        this.dataId = dataId;
    }

}
