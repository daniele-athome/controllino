package it.casaricci.controllino.data;

import it.casaricci.controllino.ui.ProfilesListAdapter;
import android.database.Cursor;


/**
 * Data item for {@link ProfilesListAdapter}.
 * @author daniele
 *
 */
public class ServerProfileData {
    private final long id;
    private final String name;
    private final String osName;
    private final String osVersion;

    public ServerProfileData(long id, String name, String osName, String osVersion) {
        this.id = id;
        this.name = name;
        this.osName = osName;
        this.osVersion = osVersion;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOsName() {
        return osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public static ServerProfileData fromCursor(Cursor c) {
        return new ServerProfileData(
            c.getLong(0),
            c.getString(1),
            c.getString(2),
            c.getString(3));
    }

}
