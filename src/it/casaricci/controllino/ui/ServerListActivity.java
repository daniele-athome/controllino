package it.casaricci.controllino.ui;

import it.casaricci.controllino.Configuration;
import it.casaricci.controllino.ConnectorService;
import it.casaricci.controllino.ConnectorService.ConnectorInterface;
import it.casaricci.controllino.Executor;
import it.casaricci.controllino.R;
import it.casaricci.controllino.data.ServerData;
import android.app.AlertDialog;
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
public class ServerListActivity extends ListActivity implements ConnectorService.ConnectorListener, Executor.ExecuteListener {
    public static final int REQUEST_SERVER_EDITOR = 1;
    public static final int REQUEST_PROFILE_EDITOR = 2;

    private CursorAdapter mAdapter;
    private Configuration mConfig;

    /** Reusable status dialog. */
    private ProgressDialog mStatus;
    /** Action to be run when connected. */
    private ConnectedRunnable mConnectedAction;

    private ConnectedRunnable mShowStatusAction = new ConnectedRunnable() {
        @Override
        public void run(ConnectorInterface conn) {
            mStatus.dismiss();
            Intent i = new Intent(ServerListActivity.this, StatusActivity.class);
            i.putExtra(ConnectorService.EXTRA_HOST, conn.host);
            i.putExtra(ConnectorService.EXTRA_PORT, conn.port);
            i.putExtra(ConnectorService.EXTRA_PROFILE_ID, conn.profileId);
            i.putExtra(ConnectorService.EXTRA_USERNAME, conn.username);
            i.putExtra(ConnectorService.EXTRA_PASSWORD, conn.password);
            //i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);
        }
    };

    private ConnectorServiceConnection mConnection = new ConnectorServiceConnection();

    private final class ConnectorServiceConnection implements ServiceConnection {
        private ConnectorInterface connection;
        private String host;
        private int port;

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connection = null;
        }

        public void setParams(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ConnectorService.ConnectorBinder binder = (ConnectorService.ConnectorBinder) service;
            connection = binder.getConnector(host, port);
            connection.listener = ServerListActivity.this;
            if (!connection.isConnected())
                connection.connect();
            else
                connected(connection);
            disconnect();
        }

        public void disconnect() {
            if (connection != null) {
                unbindService(this);
                // invalidate connection immediately
                connection = null;
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

        mConfig = Configuration.getInstance(this);
        mAdapter = new ServerListAdapter(this,
            R.layout.preference,
            android.R.id.title, android.R.id.summary, null);
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
    protected void onResume() {
        super.onResume();
        // reload data
        refresh();
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
                editServer(null);
                return true;

            case R.id.menu_settings:
                startActivity(new Intent(this, MainPreferences.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static final int MENU_STATUS = 1;
    private static final int MENU_SHUTDOWN = 2;
    private static final int MENU_REBOOT = 3;
    private static final int MENU_EDIT = 4;
    private static final int MENU_EDIT_PROFILE = 5;
    private static final int MENU_DELETE = 6;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        ServerData data = (ServerData) mAdapter.getItem(info.position);

        menu.setHeaderTitle(data.getName());
        menu.add(Menu.NONE, MENU_STATUS, MENU_STATUS, R.string.menu_show_status);
        menu.add(Menu.NONE, MENU_SHUTDOWN, MENU_SHUTDOWN, R.string.menu_shutdown);
        menu.add(Menu.NONE, MENU_REBOOT, MENU_REBOOT, R.string.menu_reboot);
        menu.add(Menu.NONE, MENU_EDIT, MENU_EDIT, R.string.menu_edit_server);
        menu.add(Menu.NONE, MENU_EDIT_PROFILE, MENU_EDIT_PROFILE, R.string.menu_edit_server_profile);
        menu.add(Menu.NONE, MENU_DELETE, MENU_DELETE, R.string.menu_delete_server);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
            .getMenuInfo();
        ServerData data = (ServerData) mAdapter.getItem(info.position);

        switch (item.getItemId()) {
            case MENU_STATUS:
                connect(data, mShowStatusAction);
                return true;

            case MENU_SHUTDOWN:
                connectConfirm(data, new ConnectedRunnable() {
                    @Override
                    public void run(ConnectorInterface conn) {
                        Executor exec = new Executor(conn, "shutdown -h -P now");
                        exec.setListener(ServerListActivity.this);
                        exec.start();
                    }
                }, R.string.status_shutting_down, R.string.menu_shutdown, R.string.confirm_shutdown);
                return true;

            case MENU_REBOOT:
                connectConfirm(data, new ConnectedRunnable() {
                    @Override
                    public void run(ConnectorInterface conn) {
                        Executor exec = new Executor(conn, "shutdown -r now");
                        exec.setListener(ServerListActivity.this);
                        exec.start();
                    }
                }, R.string.status_rebooting, R.string.menu_reboot, R.string.confirm_reboot);
                return true;

            case MENU_EDIT:
                // edit server
                editServer(data);
                return true;

            case MENU_EDIT_PROFILE:
                editProfile(data.getProfileId());
                return true;

            case MENU_DELETE:
                // delete server
                delete(data.getId());
                return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SERVER_EDITOR) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.msg_server_saved, Toast.LENGTH_SHORT).show();
            }
            else if (resultCode == ServerEditor.RESULT_DELETED) {
                Toast.makeText(this, R.string.msg_server_deleted, Toast.LENGTH_SHORT).show();
            }
            // onResume will refresh()
        }

        if (requestCode == REQUEST_PROFILE_EDITOR) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.msg_profile_saved, Toast.LENGTH_SHORT).show();
            }
            else if (resultCode == ProfileEditor.RESULT_DELETED) {
                Toast.makeText(this, R.string.msg_profile_deleted, Toast.LENGTH_SHORT).show();
            }
            // onResume will refresh()
        }
    }

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        connect((ServerData) mAdapter.getItem(position), mShowStatusAction);
    }

    private void connectConfirm(final ServerData item, final ConnectedRunnable action,
            final int connectingStatus, int title, int prompt) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
            .setTitle(title)
            .setMessage(prompt)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    connect(item, action, connectingStatus);
                }
            })
            .show();
    }

    private void connect(ServerData item, ConnectedRunnable action) {
        connect(item, action, R.string.status_connecting);
    }

    private void connect(ServerData item, ConnectedRunnable action, int connectingStatus) {
        mStatus.setMessage(getString(connectingStatus));
        mStatus.setOnCancelListener(mAbortConnectionListener);
        mStatus.show();

        mConnectedAction = action;
        mConnection.setParams(item.getHost(), item.getPort());

        // start service first
        Intent i = new Intent(this, ConnectorService.class);
        i.putExtra(ConnectorService.EXTRA_PROFILE_ID, item.getProfileId());
        i.putExtra(ConnectorService.EXTRA_HOST, item.getHost());
        i.putExtra(ConnectorService.EXTRA_PORT, item.getPort());
        i.putExtra(ConnectorService.EXTRA_USERNAME, item.getUsername());
        i.putExtra(ConnectorService.EXTRA_PASSWORD, item.getPassword());
        startService(i);

        // bind to connector
        if (!bindService(new Intent(this, ConnectorService.class), mConnection, 0)) {
            error(getString(R.string.err_bind_connector));
            return;
        }
    }

    private void editServer(ServerData item) {
        Intent i;
        if (item == null)
            i = ServerEditor.newEditor(this);
        else
            i = ServerEditor.fromServerId(this, item.getId());
        startActivityForResult(i, REQUEST_SERVER_EDITOR);
    }

    private void editProfile(long id) {
        startActivityForResult(ProfileEditor.fromProfileId(this, id), REQUEST_PROFILE_EDITOR);
    }

    private void delete(final long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
            .setTitle(R.string.menu_delete_server)
            .setMessage(R.string.msg_server_delete_confirm)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mConfig.removeServer(id);
                    refresh();
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .show();
    }

    private void refresh() {
        Cursor old = mAdapter.getCursor();
        Cursor c = mConfig.getServers();
        startManagingCursor(c);
        mAdapter.changeCursor(c);
        if (old != null)
            stopManagingCursor(old);
    }

    @Override
    public void connected(ConnectorInterface conn) {
        if (mConnectedAction != null)
            mConnectedAction.run(conn);
    }

    @Override
    public void connectionError(ConnectorInterface conn, Throwable e) {
        StringBuilder sb = new StringBuilder(e.getClass().getName());
        if (e.getMessage() != null)
            sb.append(": ")
            .append(e.getMessage());
        error(sb.toString());
        mConnection.disconnect();
    }

    @Override
    public void disconnected(ConnectorInterface conn) {
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

    @Override
    public void onExecuteFinish(Executor exec, int exitStatus) {
        mStatus.dismiss();
    }

    @Override
    public void onError(Executor exec, Throwable e) {
        // TODO
    }

    private interface ConnectedRunnable {
        public void run(ConnectorInterface conn);
    }
}
