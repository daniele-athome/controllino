package it.casaricci.controllino.ui;

import it.casaricci.controllino.Configuration;
import it.casaricci.controllino.ConnectorService;
import it.casaricci.controllino.R;
import it.casaricci.controllino.data.ServerData;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Main activity: the servers list.
 * @author Daniele Ricci
 */
public class ServerListActivity extends ListActivity implements ConnectorService.ConnectorListener {
    public static final int REQUEST_SERVER_EDITOR = 1;

    private CursorAdapter mAdapter;

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
            mConnector.setListener(ServerListActivity.this);
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
            stopService(new Intent(ServerListActivity.this, ConnectorService.class));
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_list_screen);
        TextView text = (TextView) findViewById(android.R.id.empty);
        text.setText(Html.fromHtml(getString(R.string.list_servers_empty)));

        Cursor c = Configuration.getInstance(this).getServers();
        startManagingCursor(c);

        mAdapter = new ServerListAdapter(this,
            R.layout.preference,
            android.R.id.title, android.R.id.summary, c);
        setListAdapter(mAdapter);
        registerForContextMenu(getListView());

        mStatus = new ProgressDialog(this);
        mStatus.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    /** No search here. */
    @Override
    public boolean onSearchRequested() {
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mConnection.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.servers_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_server:
                // add server
                startActivity(new Intent(this, ServerEditor.class));
                return true;

            case R.id.menu_settings:
                startActivity(new Intent(this, MainPreferences.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static final int MENU_STATUS = 1;
    private static final int MENU_EDIT = 2;
    private static final int MENU_DELETE = 3;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        ServerData data = (ServerData) mAdapter.getItem(info.position);

        menu.setHeaderTitle(data.getName());
        // TODO i18n
        menu.add(Menu.NONE, MENU_STATUS, MENU_STATUS, "Show status");
        menu.add(Menu.NONE, MENU_EDIT, MENU_EDIT, "Edit server");
        menu.add(Menu.NONE, MENU_DELETE, MENU_DELETE, "Delete server");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
            .getMenuInfo();
        ServerData data = (ServerData) mAdapter.getItem(info.position);

        switch (item.getItemId()) {
            case MENU_STATUS:
                showStatus(data);
                return true;
            case MENU_EDIT:
                editServer(data);
                return true;
            case MENU_DELETE:
                // TODO delete server
                return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SERVER_EDITOR) {
            if (resultCode == RESULT_OK) {
                // TODO i18n
                Toast.makeText(this, "Server saved.", Toast.LENGTH_SHORT).show();
            }
            else if (resultCode == ServerEditor.RESULT_DELETED) {
                // TODO i18n
                Toast.makeText(this, "Server deleted.", Toast.LENGTH_SHORT).show();
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        showStatus((ServerData) mAdapter.getItem(position));
    }

    private void showStatus(ServerData item) {
        // TODO i18n
        mStatus.setMessage("Connecting...");
        mStatus.setOnCancelListener(mAbortConnectionListener);
        mStatus.show();

        // start connector
        Intent i = new Intent(this, ConnectorService.class);
        i.putExtra(ConnectorService.EXTRA_PROFILE_ID, item.getProfileId());
        i.putExtra(ConnectorService.EXTRA_HOST, item.getHost());
        i.putExtra(ConnectorService.EXTRA_PORT, item.getPort());
        i.putExtra(ConnectorService.EXTRA_USERNAME, item.getUsername());
        i.putExtra(ConnectorService.EXTRA_PASSWORD, item.getPassword());
        startService(i);

        // bind to connector
        if (!bindService(new Intent(this, ConnectorService.class),
                mConnection, 0)) {
            // TODO i18n
            error("Unable to bind to connector service.");
            return;
        }
    }

    private void editServer(ServerData item) {
        startActivityForResult(ServerEditor.fromServerId(this, item.getId()), REQUEST_SERVER_EDITOR);
    }

    @Override
    public void connected() {
        mStatus.dismiss();
        Intent i = new Intent(this, StatusActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);
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
                Toast.makeText(ServerListActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

}
