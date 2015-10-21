package it.casaricci.controllino.ui;

import it.casaricci.controllino.ConnectorService;
import it.casaricci.controllino.ConnectorService.ConnectorInterface;
import it.casaricci.controllino.R;
import it.casaricci.controllino.controller.BaseController;
import it.casaricci.controllino.controller.BaseController.ResultListener;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;


/**
 * Server status monitoring activity.
 * @author Daniele Ricci
 */
public class StatusActivity extends ListActivity {

    /** The list view. */
    private ListView mListView;
	/** The list adapter. */
	private ServiceStatusListAdapter mAdapter;
	/** Connector interface. */
	private ConnectorInterface mConnector;
	/** Reusable status dialog. */
	//private ProgressDialog mStatus;

	private String mHost;
	private int mPort;
	private long mProfileId;
	private String mUsername;
	private String mPassword;

    private ResultListener mResultListener = new ResultListener() {
		@Override
		public void onError(BaseController ctrl, Throwable e) {
			StringBuilder sb = new StringBuilder(e.getClass().getName());
			if (e.getMessage() != null)
				sb.append(": ")
				.append(e.getMessage());
			error(sb.toString());
		}

		@Override
		public void onSuccess(BaseController ctrl) {
			//hideProgress();
		}
	};


	private ConnectorServiceConnection mConnection = new ConnectorServiceConnection();

	private final class ConnectorServiceConnection implements ServiceConnection {
	    private ConnectorService.ConnectorBinder binder;

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mConnector = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
		    binder = (ConnectorService.ConnectorBinder) service;
			mConnector = binder.getConnector(mHost, mPort);
			if (!mConnector.isConnected()) {
			    // TODO reconnection??
			}
			mAdapter.update(mConnector);
		}

		public void disconnect() {
			if (mConnector != null) {
				unbindService(this);
				// invalidate connection immediately
				mConnector = null;
				binder.cleanup();
				binder = null;
			}
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.status_screen);

	    // create list adapter
	    mAdapter = new ServiceStatusListAdapter(this);
	    setListAdapter(mAdapter);
	    mListView = getListView();

	    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // show context menu
                final int start = parent.getFirstVisiblePosition();
                final int index = position - start;
                final View childView = parent.getChildAt(index);
                if (childView != null && childView.isEnabled()) {
                    parent.showContextMenuForChild(childView);
                }
            }
        });

	    registerForContextMenu(mListView);
	    mListView.setLongClickable(false);

	    Intent i = getIntent();
	    mHost = i.getStringExtra(ConnectorService.EXTRA_HOST);
	    mPort = i.getIntExtra(ConnectorService.EXTRA_PORT, ConnectorService.DEFAULT_PORT);
	    mProfileId = i.getLongExtra(ConnectorService.EXTRA_PROFILE_ID, 0);
        mUsername = i.getStringExtra(ConnectorService.EXTRA_USERNAME);
        mPassword = i.getStringExtra(ConnectorService.EXTRA_PASSWORD);

	    /*
	    mStatus = new ProgressDialog(this);
	    mStatus.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	    */
	}

	private static final int MENU_START = 1;
	private static final int MENU_STOP = 2;
	private static final int MENU_RESTART = 3;
	private static final int MENU_RELOAD = 4;

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	    if (v.getId() == android.R.id.list) {
	        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
	        BaseController ctrl = (BaseController) mListView.getItemAtPosition(info.position);
	        menu.setHeaderTitle(ctrl.getServiceName());
	        menu.add(Menu.NONE, MENU_START, MENU_START, R.string.menu_start);
	        menu.add(Menu.NONE, MENU_STOP, MENU_STOP, R.string.menu_stop);
            menu.add(Menu.NONE, MENU_RESTART, MENU_RESTART, R.string.menu_restart);
            menu.add(Menu.NONE, MENU_RELOAD, MENU_RELOAD, R.string.menu_reload);
	    }
	}

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        BaseController ctrl = (BaseController) mListView.getItemAtPosition(info.position);

        switch (item.getItemId()) {
            case MENU_START:
                info.targetView.setEnabled(false);
                ctrl.setResultListener(mResultListener);
                ctrl.start();
                return true;

            case MENU_STOP:
                info.targetView.setEnabled(false);
            	ctrl.setResultListener(mResultListener);
                ctrl.stop();
                return true;

            case MENU_RESTART:
                info.targetView.setEnabled(false);
                ctrl.setResultListener(mResultListener);
                ctrl.restart();
                return true;

            case MENU_RELOAD:
                info.targetView.setEnabled(false);
                ctrl.setResultListener(mResultListener);
                ctrl.reload();
                return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // start service
        Intent i = new Intent(this, ConnectorService.class);
        i.putExtra(ConnectorService.EXTRA_HOST, mHost);
        i.putExtra(ConnectorService.EXTRA_PORT, mPort);
        i.putExtra(ConnectorService.EXTRA_PROFILE_ID, mProfileId);
        i.putExtra(ConnectorService.EXTRA_USERNAME, mUsername);
        i.putExtra(ConnectorService.EXTRA_PASSWORD, mPassword);
        startService(i);

        // bind to connector
        if (!bindService(new Intent(this, ConnectorService.class), mConnection, 0)) {
            error(getString(R.string.err_bind_connector));
            finish();
        }
    }

	@Override
	protected void onStop() {
	    super.onStop();
        mConnection.disconnect();
	}

	/** No search here. */
	@Override
	public boolean onSearchRequested() {
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.status_menu, menu);
	    return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.status_update:
                mAdapter.update(mConnector);
                return true;

            case R.id.disconnect:
                mConnector.disconnect();
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    private void showProgressStartStop(String message) {
		mStatus.setMessage(message);
	    mStatus.setCancelable(false);
	    mStatus.setCanceledOnTouchOutside(false);
	    mStatus.show();
    }

    private void hideProgress() {
    	mStatus.dismiss();
    }
    */

	private void error(final String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//hideProgress();
				Toast.makeText(StatusActivity.this, message, Toast.LENGTH_LONG).show();
			}
		});
	}
}
