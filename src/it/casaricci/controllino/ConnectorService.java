package it.casaricci.controllino;

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

	private final IBinder mBinder = new ConnectorInterface();

	private ConnectorListener mListener;

	private String mServer;
	private int mPort;
	private String mUsername;
	private String mPassword;
	private long mProfileId;

	private Connector mConnector;
	private JSch mJsch = new JSch();
	private Session mSession;

	@Override
	public void onStart(Intent intent, int startId) {
	    onStartCommand(intent, 0, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    mProfileId = intent.getLongExtra(EXTRA_PROFILE_ID, 0);
		mServer = intent.getStringExtra(EXTRA_HOST);
		mPort = intent.getIntExtra(EXTRA_PORT, DEFAULT_PORT);
		mUsername = intent.getStringExtra(EXTRA_USERNAME);
		mPassword = intent.getStringExtra(EXTRA_PASSWORD);
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
		if (mConnector != null) {
			mConnector.close();
			mConnector = null;
		}
	}

	public void setListener(ConnectorListener listener) {
		mListener = listener;
	}

	public void connect() {
		mConnector = new Connector();
		mConnector.start();
	}

	public boolean isConnected() {
		return (mSession != null && mSession.isConnected());
	}

	public Session getSession() {
	    return mSession;
	}

	public long getProfileId() {
	    return mProfileId;
	}

	private final class Connector extends Thread {
		@Override
		public void run() {
			// connect to server
			try {
				mSession = mJsch.getSession(mUsername, mServer, mPort);
				mSession.setPassword(mPassword);

				// allow new hosts :)
				Properties config = new Properties();
				config.put("StrictHostKeyChecking", "no");
				mSession.setConfig(config);

				Log.d(TAG, "connecting to " + mUsername + "@" + mServer);
				mSession.connect();
				if (mListener != null)
					mListener.connected();

				Log.d(TAG, "connected!");
			}
			catch (Exception e) {
				Log.e(TAG, "connection error", e);
				if (mListener != null)
					mListener.connectionError(e);
			}
		}

		public void close() {
			interrupt();
			mSession.disconnect();
			if (mListener != null) {
				mListener.disconnected();
				// discard listener so it won't be called again
				mListener = null;
			}
		}
	}

	public final class ConnectorInterface extends Binder {
		public ConnectorService getService() {
			return ConnectorService.this;
		}
	}

	public interface ConnectorListener {
		public void connected();
		public void connectionError(Throwable e);
		public void disconnected();
	}

}
