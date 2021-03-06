package it.casaricci.controllino;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;


/**
 * Configuration database.
 * @author Daniele Ricci
 */
public class Configuration extends SQLiteOpenHelper {
    private static Configuration instance;

    private static final String DATABASE_NAME = "config.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_PROFILES = "profiles";
    private static final String TABLE_SERVICES = "services";
    private static final String TABLE_PROFILE_SERVICES = "profile_services";
    private static final String TABLE_SERVERS = "servers";

    private static final String SCHEMA_PROFILES =
        "CREATE TABLE " + TABLE_PROFILES + " (" +
        "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "name TEXT NOT NULL," +
        "os_name TEXT NOT NULL," +
        "os_version TEXT" +
        ")";

    private static final String SCHEMA_SERVICES =
        "CREATE TABLE " + TABLE_SERVICES + " (" +
        "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "name TEXT NOT NULL," +
        "version TEXT," +
        "type TEXT NOT NULL," +
        "command TEXT NOT NULL," +
        "icon TEXT" +
        ")";

    // TODO foreign keys
    private static final String SCHEMA_PROFILE_SERVICES =
        "CREATE TABLE " + TABLE_PROFILE_SERVICES + " (" +
        "profile_id INTEGER NOT NULL," +
        "service_id INTEGER NOT NULL," +
        "PRIMARY KEY (profile_id, service_id)" +
        ")";

    // TODO foreign keys
    private static final String SCHEMA_SERVERS =
        "CREATE TABLE " + TABLE_SERVERS + " (" +
        "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "profile_id INTEGER NOT NULL," +
        "name TEXT NOT NULL," +
        "host TEXT NOT NULL," +
        "port INTEGER," +
        "username TEXT NOT NULL," +
        "password TEXT" +
        ")";

    public Configuration(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SCHEMA_PROFILES);
        db.execSQL(SCHEMA_SERVICES);
        db.execSQL(SCHEMA_PROFILE_SERVICES);
        db.execSQL(SCHEMA_SERVERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // version 1 - no upgrade yet
    }

    public static Configuration getInstance(Context context) {
        if (instance == null)
            instance = new Configuration(context.getApplicationContext());
        return instance;
    }

    public Cursor getServices() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_SERVICES, null, null, null, null, null, "name");
    }

    public Cursor getServices(long profileId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_SERVICES + " s JOIN " +
            TABLE_PROFILE_SERVICES + " ps ON s._id = ps.service_id",
            null,
            "ps.profile_id = " + profileId, null,
            null, null, "name");
    }

    public Cursor getService(long id) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_SERVICES, null, "_id = " + id,
            null, null, null, null);
    }

    public int getServiceUsageCount(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PROFILE_SERVICES, new String[] { "count(*)" },
            "service_id = " + id, null, null, null, null);
        try {
            if (c.moveToNext())
                return c.getInt(0);
        }
        finally {
            c.close();
        }

        return 0;
    }

    public long addService(String name, String version, String type, String command, String icon) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues(4);
        values.put("name", name);
        values.put("version", version);
        values.put("type", type);
        values.put("command", command);
        values.put("icon", icon);

        return db.insert(TABLE_SERVICES, null, values);
    }

    public void updateService(long id, String name, String version, String type, String command, String icon) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues(4);
        values.put("name", name);
        values.put("version", version);
        values.put("type", type);
        values.put("command", command);
        values.put("icon", icon);

        db.update(TABLE_SERVICES, values, "_id = " + id, null);
    }

    public void removeService(long id) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();

            // remove link to profiles
            db.delete(TABLE_PROFILE_SERVICES, "service_id = " + id, null);
            // remove service
            db.delete(TABLE_SERVICES, "_id = " + id, null);

            // commit!
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    public Cursor getProfiles() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_PROFILES, null, null, null, null, null, "name");
    }

    public Cursor getProfile(long id) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_PROFILES, null, "_id = " + id,
            null, null, null, null);
    }

    public int getProfileUsageCount(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_SERVERS, new String[] { "count(*)" },
            "profile_id = " + id, null, null, null, null);
        try {
            if (c.moveToNext())
                return c.getInt(0);
        }
        finally {
            c.close();
        }

        return 0;
    }

    public long addProfile(String name, String osName, String osVersion, long[] services) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();

            // insert profile
            ContentValues values = new ContentValues(3);
            values.put("name", name);
            values.put("os_name", osName);
            values.put("os_version", osVersion);
            long id = db.insert(TABLE_PROFILES, null, values);

            // insert profile services
            if (services != null && services.length > 0) {
                values.clear();
                for (long servId : services) {
                    values.put("profile_id", id);
                    values.put("service_id", servId);
                    db.insert(TABLE_PROFILE_SERVICES, null, values);
                }
            }

            // commit!
            db.setTransactionSuccessful();
            return id;
        }
        finally {
            db.endTransaction();
        }
    }

    public void updateProfile(long id, String name, String osName, String osVersion, long[] services) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();

            // insert profile
            ContentValues values = new ContentValues(3);
            values.put("name", name);
            values.put("os_name", osName);
            values.put("os_version", osVersion);
            db.update(TABLE_PROFILES, values, "_id = " + id, null);

            // reinsert profile services
            if (services != null) {
                db.delete(TABLE_PROFILE_SERVICES, "profile_id = " + id, null);
                if (services.length > 0) {
                    values.clear();
                    for (long servId : services) {
                        values.put("profile_id", id);
                        values.put("service_id", servId);
                        db.insert(TABLE_PROFILE_SERVICES, null, values);
                    }
                }
            }

            // commit!
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    public void removeProfile(long id) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();

            // delete profile services
            db.delete(TABLE_PROFILE_SERVICES, "profile_id = " + id, null);
            // delete profile
            db.delete(TABLE_PROFILES, "_id = " + id, null);

            // commit!
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    public Cursor getServers() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_SERVERS, null, null, null, null, null, "name");
    }

    public Cursor getServer(long id, Bundle profile) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_SERVERS, null, "_id = " + id,
            null, null, null, null);

        // profile data requested
        if (profile != null) {
            c.moveToFirst();
            Cursor pc = getProfile(c.getLong(1));
            if (pc.moveToFirst()) {
                profile.putLong("id", pc.getLong(0));
                profile.putString("name", pc.getString(1));
            }
            pc.close();

            // reset cursor
            c.moveToPosition(-1);
        }
        return c;
    }

    public long addServer(String name, String host, int port, String username, String password, long profileId) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues(4);
        values.put("name", name);
        values.put("host", host);
        values.put("port", port);
        values.put("username", username);
        values.put("password", password);
        values.put("profile_id", profileId);

        return db.insert(TABLE_SERVERS, null, values);
    }

    public void updateServer(long id, String name, String host, int port, String username, String password, long profileId) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues(4);
        values.put("name", name);
        values.put("host", host);
        values.put("port", port);
        values.put("username", username);
        values.put("password", password);
        values.put("profile_id", profileId);

        db.update(TABLE_SERVERS, values, "_id = " + id, null);
    }

    public void removeServer(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_SERVERS, "_id = " + id, null);
    }

}
