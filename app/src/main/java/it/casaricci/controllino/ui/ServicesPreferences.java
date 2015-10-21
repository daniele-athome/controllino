package it.casaricci.controllino.ui;

import it.casaricci.controllino.Configuration;
import it.casaricci.controllino.R;
import it.casaricci.controllino.data.ServiceData;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
 * Manages configured system services.
 * @author Daniele Ricci
 */
public class ServicesPreferences extends ListActivity {
    public static final int REQUEST_SERVICE_EDITOR = 1;

    private CursorAdapter mAdapter;
    private Configuration mConfig;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefs_services);
        TextView text = (TextView) findViewById(android.R.id.empty);
        text.setText(Html.fromHtml(getString(R.string.list_services_empty)));

        mConfig = Configuration.getInstance(this);
        mAdapter = new ServicesListAdapter(this,
            R.layout.preference_icon,
            android.R.id.title, android.R.id.summary, android.R.id.icon, null);
        setListAdapter(mAdapter);
        registerForContextMenu(getListView());
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.services_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_service:
                newService();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static final int MENU_EDIT = 1;
    private static final int MENU_DELETE = 2;
    private static final int MENU_CLONE = 3;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        ServiceData data = (ServiceData) mAdapter.getItem(info.position);

        menu.setHeaderTitle(data.toString());
        menu.add(Menu.NONE, MENU_EDIT, MENU_EDIT, R.string.menu_edit_service);
        menu.add(Menu.NONE, MENU_DELETE, MENU_DELETE, R.string.menu_delete_service);
        menu.add(Menu.NONE, MENU_CLONE, MENU_CLONE, R.string.menu_clone_service);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
            .getMenuInfo();
        ServiceData data = (ServiceData) mAdapter.getItem(info.position);

        switch (item.getItemId()) {
            case MENU_EDIT:
                edit(data);
                return true;

            case MENU_DELETE:
                delete(data.getId());
                return true;

            case MENU_CLONE:
                newService(data);
                return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SERVICE_EDITOR) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.msg_service_saved, Toast.LENGTH_SHORT).show();
            }
            else if (resultCode == ServiceEditor.RESULT_DELETED) {
                Toast.makeText(this, R.string.msg_service_deleted, Toast.LENGTH_SHORT).show();
            }
            // onResume will refresh()
        }
    }

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        ServiceData item = (ServiceData) list.getItemAtPosition(position);
        edit(item);
    }

    private void edit(ServiceData item) {
        startActivityForResult(ServiceEditor.fromServiceId(this, item.getId()), REQUEST_SERVICE_EDITOR);
    }

    private void delete(final long id) {
        ServiceEditor.delete(this, id, mConfig, new Runnable() {
            public void run() {
                refresh();
            }
        });
    }

    private void refresh() {
        Cursor old = mAdapter.getCursor();
        Cursor c = mConfig.getServices();
        startManagingCursor(c);
        mAdapter.changeCursor(c);
        if (old != null)
            stopManagingCursor(old);
    }

    /** Builtin Service templates. */
    // TODO should we i18n these?
    private static final String[][] serviceTemplates = {
        { "Apache HTTP server", "2.x", "sysvinit", "apache2", "ctrl_apache" },
        { "MySQL server", "5.x", "sysvinit", "mysql", "ctrl_mysql" },
        { "Postfix server", "2.x", "sysvinit", "postfix", "ctrl_postfix" },
        { "Deluge torrent daemon", "1.3.x", "sysvinit", "deluge-daemon", "ctrl_deluge" },
        { "Secure Shell", "", "sysvinit", "ssh", "ctrl_ssh" }
    };

    public void newService() {
        CharSequence[] items = new CharSequence[serviceTemplates.length + 1];
        items[0] = getString(R.string.service_tmpl_none);
        for (int i = 0; i < serviceTemplates.length; i++)
            items[i + 1] = serviceTemplates[i][0];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent;
                if (which > 0) {
                    String[] tmpl = serviceTemplates[which - 1];
                    intent = ServiceEditor.newEditor(ServicesPreferences.this,
                        tmpl[0], tmpl[1], tmpl[2], tmpl[3], tmpl[4], true);
                }
                else {
                    intent = ServiceEditor.newEditor(ServicesPreferences.this);
                }

                // start service editor
                startActivityForResult(intent, REQUEST_SERVICE_EDITOR);
            }
        };

        builder
            .setTitle(R.string.title_choose_service_template)
            .setItems(items, listener);

        builder.create().show();
    }

    public void newService(ServiceData data) {
        Intent intent = ServiceEditor.newEditor(ServicesPreferences.this,
            data.getName(), data.getVersion(), data.getType(),
            data.getCommand(), data.getIcon(), false);
        // start service editor
        startActivityForResult(intent, REQUEST_SERVICE_EDITOR);
    }

}
