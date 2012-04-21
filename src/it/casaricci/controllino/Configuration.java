package it.casaricci.controllino;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


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
        "command TEXT NOT NULL" +
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
        "address TEXT NOT NULL," +
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

    public long addService(String name, String version, String type, String command) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues(4);
        values.put("name", name);
        values.put("version", version);
        values.put("type", type);
        values.put("command", command);

        long id = db.insert(TABLE_SERVICES, null, values);
        db.close();
        return id;
    }

    public void updateService(long id, String name, String version, String type, String command) {
        // TODO
    }

    public void removeService(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_SERVICES, "_id = ?", new String[] { String.valueOf(id) });
        db.close();
    }

    public Cursor getProfiles() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_PROFILES, null, null, null, null, null, "name");
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
            long profId = db.insert(TABLE_PROFILES, null, values);

            // insert profile services
            values.clear();
            for (long servId : services) {
                values.put("profile_id", profId);
                values.put("service_id", servId);
                db.insert(TABLE_PROFILE_SERVICES, null, values);
            }

            // commit!
            db.setTransactionSuccessful();
            return profId;
        }
        finally {
            db.endTransaction();
            db.close();
        }
    }

    public void updateProfile(long id, String name, String osName, String osVersion, long[] services) {
        // TODO
    }

    public void removeProfile(long id) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();

            String[] args = new String[] { String.valueOf(id) };
            // delete profile services
            db.delete(TABLE_PROFILE_SERVICES, "profile_id = ?", args);
            // delete profile
            db.delete(TABLE_PROFILES, "_id = ?", args);

            // commit!
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
            db.close();
        }
    }

    public Cursor getServers() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_SERVERS, null, null, null, null, null, "name");
    }

    public long addServer(String name, String address, int port, String username, String password, long profileId) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues(4);
        values.put("name", name);
        values.put("address", address);
        values.put("port", port);
        values.put("username", username);
        values.put("password", password);
        values.put("profile_id", profileId);

        long id = db.insert(TABLE_SERVERS, null, values);
        db.close();
        return id;
    }

    public void updateServer(long id, String name, String address, int port, String username, String password, long profile) {
        // TODO
    }

    public void removeServer(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_SERVERS, "_id = ?", new String[] { String.valueOf(id) });
        db.close();
    }

}
