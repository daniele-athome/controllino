package it.casaricci.controllino;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;


/**
 * The connector service.
 * Bind to this service to initiate a SSH connection to a server.
 * @author Daniele Ricci
 */
public class ConnectorService extends Service {
	private static final String TAG = "SSHConnector"; //ConnectorService.class.getSimpleName();

	public static final String EXTRA_PROFILE_ID = "connection.profileId";
	public static final String EXTRA_HOST = "connection.address";
	public static final String EXTRA_PORT = "connection.port";
	public static final String EXTRA_USERNAME = "connection.username";
	public static final String EXTRA_PASSWORD = "connection.password";

	/** Default SSH port. */
	public static final int DEFAULT_PORT = 22;

	private final ConnectorBinder mBinder = new ConnectorBinder();

	/** Connections table. */
	private final Map<InetSocketAddress, ConnectorInterface> mConnections =
	    new HashMap<InetSocketAddress, ConnectorInterface>(1);

	private JSch mJsch = new JSch();

	@Override
	public void onStart(Intent intent, int startId) {
	    onStartCommand(intent, 0, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        String host = intent.getStringExtra(EXTRA_HOST);
        int port = intent.getIntExtra(EXTRA_PORT, DEFAULT_PORT);

        Log.v(TAG, "connection request to " + host + ":" + port);

        InetSocketAddress addr = InetSocketAddress.createUnresolved(host, port);
        ConnectorInterface conn = mConnections.get(addr);
        if (conn == null) {
            Log.v(TAG, "creating new instance for " + host + ":" + port);
            conn = new ConnectorInterface(mJsch);
            conn.host = host;
            conn.port = port;
            mConnections.put(addr, conn);
        }

        // update to latest configuration data
        conn.profileId = intent.getLongExtra(EXTRA_PROFILE_ID, 0);
        conn.username = intent.getStringExtra(EXTRA_USERNAME);
        conn.password = intent.getStringExtra(EXTRA_PASSWORD);

		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
	    return mBinder;
	}

	@Override
	public void onDestroy() {
		shutdown();
	}

	/** Shuts down all connections. */
	private void shutdown() {
	    synchronized (mConnections) {
            for (Map.Entry<InetSocketAddress, ConnectorInterface> entry : mConnections.entrySet()) {
                ConnectorInterface conn = entry.getValue();
                conn.disconnect();
            }

            mConnections.clear();
        }
	}

	public final class ConnectorBinder extends Binder {
	    public ConnectorInterface getConnector(String host, int port) {
	        InetSocketAddress addr = InetSocketAddress.createUnresolved(host, port);
	        return mConnections.get(addr);
	    }

	    public void cleanup() {
	        boolean stop = true;
	        for (ConnectorInterface conn : mConnections.values()) {
	            if (!conn.isClosed()) {
	                Log.d(TAG, "found active connection - aborting stop");
	                stop = false;
	                break;
	            }
	        }
	        if (stop)
	            stopSelf();
	    }
	}

	public static final class ConnectorInterface {
        public String host;
        public int port;
        public String username;
        public String password;
        public long profileId;

        public ConnectorListener listener;
        public Session session;

        private JSch mJsch;
        private Connector connector;
        private boolean closed;

	    public ConnectorInterface(JSch jsch) {
	        mJsch = jsch;
	    }

	    public void connect() {
	        closed = false;
	        connector = new Connector();
	        connector.start();
	    }

	    public boolean isConnected() {
	        return (session != null && session.isConnected());
	    }

	    public boolean isClosed() {
	        return closed;
	    }

	    public void disconnect() {
            closed = true;
	        if (connector != null) {
	            connector.close();
	            connector = null;
	        }
	    }

	    @Override
	    public String toString() {
	        return getClass().getSimpleName() + '@' + Integer.toHexString(hashCode()) +
	            " [host=" + host + ", port=" + port + ", username=" + username + "]";
	    }

	    private final class Connector extends Thread {

	        @Override
	        public void run() {
	            // connect to server
	            try {
	                session = mJsch.getSession(username, host, port);
	                session.setPassword(password);

	                // allow new hosts :)
	                Properties config = new Properties();
	                config.put("StrictHostKeyChecking", "no");
	                session.setConfig(config);

	                Log.d(TAG, "connecting to " + username + "@" + host);
	                session.connect();
	                if (listener != null)
	                    listener.connected(ConnectorInterface.this);

	                Log.d(TAG, "connected!");
	            }
	            catch (Exception e) {
	                Log.e(TAG, "connection error", e);
	                if (listener != null)
	                    listener.connectionError(ConnectorInterface.this, e);
	            }
	        }

	        public void close() {
	            interrupt();
	            session.disconnect();
	            if (listener != null) {
	                listener.disconnected(ConnectorInterface.this);
	                // discard listener so it won't be called again
	                listener = null;
	            }
	            // discard reference to session
	            session = null;
	        }
	    }
	}

	public interface ConnectorListener {
		void connected(ConnectorInterface connector);
		void connectionError(ConnectorInterface connector, Throwable e);
		void disconnected(ConnectorInterface connector);
	}

}
