package it.casaricci.controllino.data;

import it.casaricci.controllino.ui.ProfileEditor;

import java.util.List;

import android.database.Cursor;
import android.widget.ArrayAdapter;


/**
 * Item class used by {@link ArrayAdapter} in {@link ProfileEditor}.
 * @author Daniele Ricci
 */
public class ServiceData {
    private long id;
    private String name;
    private String version;

    public ServiceData(long id) {
        this.id = id;
    }

    public ServiceData(long id, String name, String version) {
        this.id = id;
        this.name = name;
        this.version = version;
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
            c.getString(1), c.getString(2));
    }

    public static long[] toIdList(List<ServiceData> list) {
        long[] out = new long[list.size()];
        for (int i = 0; i < out.length; i++)
            out[i] = list.get(i).id;
        return out;
    }

}
