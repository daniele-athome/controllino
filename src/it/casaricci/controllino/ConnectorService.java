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

	/** Connections table. */
	private Map<InetSocketAddress, ConnectorInterface> mConnections =
	    new HashMap<InetSocketAddress, ConnectorInterface>(1);

	private JSch mJsch = new JSch();

	@Override
	public void onStart(Intent intent, int startId) {
	    onStartCommand(intent, 0, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
        String host = intent.getStringExtra(EXTRA_HOST);
        int port = intent.getIntExtra(EXTRA_PORT, DEFAULT_PORT);

        Log.v(TAG, "connection request to " + host + ":" + port);

        InetSocketAddress addr = new InetSocketAddress(host, port);
        ConnectorInterface conn = mConnections.get(addr);
        if (conn == null) {
            Log.v(TAG, "creating new instance for " + host + ":" + port);
            conn = new ConnectorInterface();
            conn.host = host;
            conn.port = port;
            conn.profileId = intent.getLongExtra(EXTRA_PROFILE_ID, 0);
            conn.username = intent.getStringExtra(EXTRA_USERNAME);
            conn.password = intent.getStringExtra(EXTRA_PASSWORD);
            mConnections.put(addr, conn);
        }

		return conn;
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
                if (conn.connector != null) {
                    conn.connector.close();
                    conn.connector = null;
                }
            }

            mConnections.clear();
        }
	}

	public final class ConnectorInterface extends Binder {
        public String host;
        public int port;
        public String username;
        public String password;
        public long profileId;

        public ConnectorListener listener;
        public Session session;

        private Connector connector;

	    public ConnectorInterface() {
	    }

	    public void connect() {
	        connector = new Connector();
	        connector.start();
	    }

	    public boolean isConnected() {
	        return (session != null && session.isConnected());
	    }

	    public void disconnect() {
	        if (connector != null)
	            connector.close();
	    }

		public ConnectorService getService() {
			return ConnectorService.this;
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
	        }
	    }
	}

	public interface ConnectorListener {
		public void connected(ConnectorInterface connector);
		public void connectionError(ConnectorInterface connector, Throwable e);
		public void disconnected(ConnectorInterface connector);
	}

}
