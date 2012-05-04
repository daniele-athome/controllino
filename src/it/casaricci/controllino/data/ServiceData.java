package it.casaricci.controllino.data;

import it.casaricci.controllino.R;
import it.casaricci.controllino.ui.ProfileEditor;

import java.lang.reflect.Field;
import java.util.List;

import android.database.Cursor;


/**
 * Item class used by {@link ProfileServicesAdapter} in {@link ProfileEditor}.
 * And more :)
 * @author Daniele Ricci
 */
public class ServiceData {
    private long id;
    private String name;
    private String version;
    private String type;
    private String command;
    private String icon;

    public ServiceData(long id) {
        this.id = id;
    }

    public ServiceData(long id, String name, String version, String type, String command, String icon) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.type = type;
        this.command = command;
        this.icon = icon;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public String getCommand() {
        return command;
    }

    public String getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return name + " " + version;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ServiceData)
            return ((ServiceData) o).id == this.id;
        return false;
    }

    public static ServiceData fromCursor(Cursor c) {
        return new ServiceData(c.getLong(0),
            c.getString(1), c.getString(2), c.getString(3),
            c.getString(4), c.getString(5));
    }

    public void fillFromCursor(Cursor c) {
        id = c.getLong(0);
        name = c.getString(1);
        version = c.getString(2);
        type = c.getString(3);
        command = c.getString(4);
        icon = c.getString(5);
    }

    public static long[] toIdList(List<ServiceData> list) {
        long[] out = new long[list.size()];
        for (int i = 0; i < out.length; i++)
            out[i] = list.get(i).id;
        return out;
    }

    public static int getTypeString(String type) {
        try {
            Field _stringId = R.string.class.getField("script_" + type);
            return _stringId.getInt(null);
        }
        catch (Exception e) {
            // ignore
            return 0;
        }
    }

    public static int getIconDrawable(String icon) {
        try {
            Field _iconId = R.drawable.class.getField(icon);
            return _iconId.getInt(null);
        }
        catch (Exception e) {
            // ignore
            return 0;
        }
    }

}
