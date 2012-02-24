package it.casaricci.controllino.ui;

import it.casaricci.controllino.ConnectorService;
import it.casaricci.controllino.R;
import it.casaricci.controllino.controller.BaseController;
import it.casaricci.controllino.controller.BaseController.ResultListener;
import android.app.ListActivity;
import android.app.ProgressDialog;
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


public class StatusActivity extends ListActivity {

    /** The list view. */
    private ListView mListView;
	/** The list adapter. */
	private ServiceStatusListAdapter mAdapter;
	/** Connector service instance. */
	private ConnectorService mConnector;
	/** Reusable status dialog. */
	private ProgressDialog mStatus;

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
			hideProgress();
		}
	};


	private ConnectorServiceConnection mConnection = new ConnectorServiceConnection();

	private final class ConnectorServiceConnection implements ServiceConnection {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mConnector = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mConnector = ((ConnectorService.ConnectorInterface) service).getService();
			if (!mConnector.isConnected()) {
				// go back to login
				startActivity(new Intent(StatusActivity.this, LoginActivity.class));
				finish();
				return;
			}
			mAdapter.update(mConnector);
		}

		public void disconnect() {
			if (mConnector != null) {
				unbindService(this);
				// invalidate connection immediately
				mConnector = null;
			}
		}
	};

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

	    mStatus = new ProgressDialog(this);
	    mStatus.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		// bind to connector
		if (!bindService(new Intent(this, ConnectorService.class),
				mConnection, BIND_AUTO_CREATE)) {
			error("Unable to bind to connector service.");
			finish();
		}
	}

	private static final int MENU_START = 1;
	private static final int MENU_STOP = 2;

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	    if (v.getId() == android.R.id.list) {
	        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
	        BaseController ctrl = (BaseController) mListView.getItemAtPosition(info.position);
	        menu.setHeaderTitle(ctrl.getServiceName());
	        menu.add(Menu.NONE, MENU_START, MENU_START, "Start");
	        menu.add(Menu.NONE, MENU_STOP, MENU_STOP, "Stop");
	    }
	}

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        BaseController ctrl = (BaseController) mListView.getItemAtPosition(info.position);

        switch (item.getItemId()) {
            case MENU_START:
            	//showProgressStartStop("Starting " + ctrl.getServiceName());
                info.targetView.setEnabled(false);
                ctrl.setResultListener(mResultListener);
                ctrl.start();
                return true;

            case MENU_STOP:
            	//showProgressStartStop("Stopping " + ctrl.getServiceName());
                info.targetView.setEnabled(false);
            	ctrl.setResultListener(mResultListener);
                ctrl.stop();
                return true;
        }

        return super.onContextItemSelected(item);
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
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
            mConnection.disconnect();
            stopService(new Intent(this, ConnectorService.class));
            // exit the activity
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void showProgressStartStop(String message) {
		mStatus.setMessage(message);
	    mStatus.setCancelable(false);
	    mStatus.setCanceledOnTouchOutside(false);
	    mStatus.show();
    }

    private void hideProgress() {
    	mStatus.dismiss();
    }

	private void error(final String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				hideProgress();
				Toast.makeText(StatusActivity.this, message, Toast.LENGTH_LONG).show();
			}
		});
	}
}
