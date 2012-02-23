package it.casaricci.controllino.ui;

import it.casaricci.controllino.ConnectorService;
import it.casaricci.controllino.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Login activity.
 * @author Daniele Ricci
 */
public class LoginActivity extends Activity implements ConnectorService.ConnectorListener {

	/* Server connection data. */
	private TextView mServer;
	private TextView mUsername;
	private TextView mPassword;

	/** Reusable status dialog. */
	private ProgressDialog mStatus;
	/** Connector service instance. */
	private ConnectorService mConnector;

	private ConnectorServiceConnection mConnection = new ConnectorServiceConnection();

	private final class ConnectorServiceConnection implements ServiceConnection {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mConnector = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mConnector = ((ConnectorService.ConnectorInterface) service).getService();
			mConnector.setListener(LoginActivity.this);
			mConnector.connect();
		}

		public void disconnect() {
			if (mConnector != null) {
				unbindService(this);
				// invalidate connection immediately
				mConnector = null;
			}
		}
	};

	/** Reusable abort connection listener. */
	private DialogInterface.OnCancelListener mAbortConnectionListener = new DialogInterface.OnCancelListener() {
		@Override
		public void onCancel(DialogInterface dialog) {
			mConnection.disconnect();
			// we are aborting, so stop the service
			stopService(new Intent(LoginActivity.this, ConnectorService.class));
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.login_screen);

	    mServer = (TextView) findViewById(R.id.login_server);
	    mUsername = (TextView) findViewById(R.id.login_username);
	    mPassword = (TextView) findViewById(R.id.login_password);

	    // recover recent values - if any
	    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    String server = prefs.getString("recent.server", null);
	    String username = prefs.getString("recent.username", null);

	    mPassword.requestFocus();

	    if (!TextUtils.isEmpty(username))
	    	mUsername.setText(username);
	    else
	    	mUsername.requestFocus();

	    if (!TextUtils.isEmpty(server))
	    	mServer.setText(server);
	    else
	    	mServer.requestFocus();

	    mStatus = new ProgressDialog(this);
	    mStatus.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	}

	/** No search here. */
	@Override
	public boolean onSearchRequested() {
		return false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mConnection.disconnect();
	}

	public void login(View view) {
		String server = mServer.getText().toString();
		String username = mUsername.getText().toString();
		String password = mPassword.getText().toString();

		// save recent values in preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.edit()
			.putString("recent.server", server)
			.putString("recent.username", username)
			.commit();

		// status: connecting
		mStatus.setMessage("Connecting...");
	    mStatus.setOnCancelListener(mAbortConnectionListener);
	    mStatus.show();

	    // start connector
		Intent i = new Intent(this, ConnectorService.class);
		i.putExtra(ConnectorService.EXTRA_SERVER, server);
		i.putExtra(ConnectorService.EXTRA_USERNAME, username);
		i.putExtra(ConnectorService.EXTRA_PASSWORD, password);
		startService(i);

		// bind to connector
		if (!bindService(new Intent(this, ConnectorService.class),
				mConnection, 0)) {
			error("Unable to bind to connector service.");
			return;
		}
	}

	@Override
	public void connected() {
		mStatus.dismiss();
		startActivity(new Intent(this, StatusActivity.class));
		finish();
	}

	@Override
	public void connectionError(Throwable e) {
		StringBuilder sb = new StringBuilder(e.getClass().getName());
		if (e.getMessage() != null)
			sb.append(": ")
			.append(e.getMessage());
		error(sb.toString());
	}

	@Override
	public void disconnected() {
	}

	private void error(final String message) {
		mStatus.dismiss();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
			}
		});
	}

}
