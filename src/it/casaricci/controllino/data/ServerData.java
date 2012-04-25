package it.casaricci.controllino.data;

import it.casaricci.controllino.ui.ServerListAdapter;
import android.database.Cursor;


/**
 * Data item for {@link ServerListAdapter}.
 * @author daniele
 *
 */
public class ServerData {
    private final long id;
    private final long profileId;
    private final String name;
    private final String host;
    private final int port;
    private final String username;
    private final String password;

    public ServerData(long id, long profile, String name, String host, int port, String username, String password) {
        this.id = id;
        this.profileId = profile;
        this.name = name;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    public long getProfileId() {
        return profileId;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public static ServerData fromCursor(Cursor c) {
        return new ServerData(
            c.getLong(0),
            c.getLong(1),
            c.getString(2),
            c.getString(3),
            c.getInt(4),
            c.getString(5),
            c.getString(6));
    }

}
